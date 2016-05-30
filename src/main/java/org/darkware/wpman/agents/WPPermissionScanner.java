/*******************************************************************************
 * Copyright (c) 2016. darkware.org and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.darkware.wpman.agents;

import org.darkware.wpman.WPManager;
import org.darkware.wpman.config.FilePermissionsConfig;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.time.Duration;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * A {@code WPPermissionScanner} is a simple agent that periodically scans the WordPress install directories
 * for directories and files that have improper filesystem permissions. It does this using a rather simple
 * system based on a list of example directories.
 * <p>
 * For each example directory, the permissions of that directory will be cloned down to all the files and
 * directories beneath it (clearing all execute bits for non-directories). Whenever other example directories
 * are encountered, they are ignored for that round of scanning.
 * <p>
 * There are a few directories which are always included by default:
 * <ul>
 *   <li>The root directory of the WordPress install</li>
 *   <li>The uploads directory</li>
 *   <li>The plugins directory</li>
 *   <li>The themes directory</li>
 * </ul>
 * <p>
 * Additional directories can be enabled by adding them to the {@code exampleDirectories} list in the
 * {@link FilePermissionsConfig} section of the profile. You can also populate a list of paths which should
 * never be processed by adding entries to the {@code ignorePaths} list. These items will never be processed, and
 * if they are directories, their contents will be ignored as well.
 *
 * @author jeff
 * @since 2016-05-05
 */
public class WPPermissionScanner extends WPPeriodicAgent
{
    /**
     * Create a new permission scanner.
     */
    public WPPermissionScanner()
    {
        super("permission-scanner", Duration.ofMinutes(30));
    }


    @Override
    public void executeAction()
    {
        final Path wpRoot = this.getManager().getConfig().getBasePath();

        // Fetch the example directory set
        Set<Path> exampleDirectories = this.getManager().getConfig().getPermissionsConfig().getExampleDirectories();
        // Ensure all paths are absolute
        exampleDirectories.stream().filter(p -> !p.isAbsolute()).forEach(p -> exampleDirectories.add(p.resolve(wpRoot)));
        exampleDirectories.removeIf(p -> !p.isAbsolute());
        // Add default entries
        exampleDirectories.add(this.getManager().getConfig().getBasePath());
        exampleDirectories.add(this.getManager().getConfig().getUploadDir());
        exampleDirectories.add(this.getManager().getConfig().getPluginListConfig().getBaseDir());
        exampleDirectories.add(this.getManager().getConfig().getThemeListConfig().getBaseDir());

        // Fetch the ignored paths
        Set<Path> ignoredPaths = this.getManager().getConfig().getPermissionsConfig().getIgnorePaths();
        // Add all the example directories. They're ignored as well.
        // When we pass the set to the processor, we will remove the top-level directory being processed.
        ignoredPaths.addAll(exampleDirectories);

        // Ignore some additional internal paths
        ignoredPaths.add(this.getManager().getConfig().getContentDir().resolve("upgrade"));

        // Process each example directory
        for (Path dir : exampleDirectories)
        {


            try
            {
                PermissionCloner cloner = new PermissionCloner(dir);
                ignoredPaths.stream().filter(p -> !p.equals(dir)).forEach(cloner::exclude);

                cloner.call();
            }
            catch (Exception e)
            {
                WPManager.log.error("Error while enforcing permissions under {}: {}", dir, e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * This is a helper class which provides a nice abstraction for the task of cloning
     * permissions and ownership of filesystem entries within a subdirectory.
     * <p>
     * As it processes filesystem entries, it will set the owner of all files and directories
     * to the owner of the base directory. Permissions will be modified to ensure that the
     * required permission flags are set or unset, as declared.
     */
    private static class PermissionCloner extends SimpleFileVisitor<Path> implements Callable<Boolean>
    {
        private final Path baseDirectory;
        private final UserPrincipal baseDirectoryOwner;
        private final GroupPrincipal baseDirectoryGroup;
        private final Set<Path> excludedDirectories;

        private final Set<PosixFilePermission> filePermissions;
        private final Set<PosixFilePermission> directoryPermissions;

        /**
         * Create a new {@code PermissionCloner} to scan the given directory. It will take the permissions
         * of the top level directory and clone those permissions down to the entries beneath it. All
         * execute permissions are removed for non-directory entries.
         *
         * @param baseDirectory The {@link Path} of the base directory to scan.
         */
        public PermissionCloner(final Path baseDirectory)
        {
            super();

            this.baseDirectory = baseDirectory;
            this.excludedDirectories = new HashSet<>();

            try
            {
                PosixFileAttributes baseDirAttrs = Files.readAttributes(baseDirectory, PosixFileAttributes.class);

                this.directoryPermissions = baseDirAttrs.permissions();
                this.filePermissions = new HashSet<>(this.directoryPermissions);
                this.filePermissions.remove(PosixFilePermission.OWNER_EXECUTE);
                this.filePermissions.remove(PosixFilePermission.GROUP_EXECUTE);
                this.filePermissions.remove(PosixFilePermission.OTHERS_EXECUTE);

                this.baseDirectoryOwner = baseDirAttrs.owner();
                this.baseDirectoryGroup = baseDirAttrs.group();
            }
            catch (IOException e)
            {
                throw new RuntimeException("Failed to discover base directory owner.", e);
            }

        }

        @Override
        public Boolean call() throws Exception
        {
            Files.walkFileTree(this.baseDirectory, EnumSet.of(FileVisitOption.FOLLOW_LINKS), 32, this);

            return true;
        }

        /**
         * Exclude a given {@link Path} from permissions and ownership updates. If this path is a directory,
         * all items underneath that directory will also be exempt and will not be processed at all.
         *
         * @param path The path to exclude.
         */
        public void exclude(final Path path)
        {
            this.excludedDirectories.add(path);
        }

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
        {
            if (Files.isSameFile(dir, this.baseDirectory)) return FileVisitResult.CONTINUE;
            else if (this.excludedDirectories.contains(dir)) return FileVisitResult.SKIP_SUBTREE;

            this.syncOwnerAndPermissions(dir);

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
        {
            if (this.excludedDirectories.contains(file)) return FileVisitResult.CONTINUE;
            if (Files.isSymbolicLink(file)) return FileVisitResult.CONTINUE;

            this.syncOwnerAndPermissions(file);

            return FileVisitResult.CONTINUE;
        }

        /**
         * Checks the given path and updates its owner, group and permissions as needed according to the state
         * of the base directory being clones.
         *
         * @param file The file to examine.
         * @throws IOException If there is an error while retrieving or setting file attributes.
         */
        protected void syncOwnerAndPermissions(final Path file) throws IOException
        {
            // Fetch file attributes
            PosixFileAttributes posixAttrs = Files.readAttributes(file, PosixFileAttributes.class);

            // Set the owner and group
            if (!posixAttrs.group().equals(this.baseDirectoryGroup))
            {
                WPManager.log.info("Updating group: {} -> {}", file, this.baseDirectoryGroup.getName());
                Files.getFileAttributeView(file, PosixFileAttributeView.class).setGroup(this.baseDirectoryGroup);
            }
            if (!posixAttrs.owner().equals(this.baseDirectoryOwner))
            {
                WPManager.log.info("Updating owner: {} -> {}", file, this.baseDirectoryOwner.getName());
                Files.setOwner(file, this.baseDirectoryOwner);
            }

            // Set permissions
            Set<PosixFilePermission> permissions = posixAttrs.permissions();
            Set<PosixFilePermission> targetPermissions = (Files.isDirectory(file)) ? this.directoryPermissions : this.filePermissions;

            if (permissions.size() != targetPermissions.size() || !permissions.containsAll(targetPermissions))
            {
                Files.setPosixFilePermissions(file, targetPermissions);
                WPManager.log.info("Updating permissions: {} -> {}", file, PosixFilePermissions.toString(targetPermissions));
            }
        }
    }
}
