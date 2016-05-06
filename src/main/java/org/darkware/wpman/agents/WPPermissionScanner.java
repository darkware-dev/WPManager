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
import org.darkware.wpman.config.WordpressConfig;

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
 * @author jeff
 * @since 2016-05-05
 */
public class WPPermissionScanner extends WPPeriodicAgent
{
    public WPPermissionScanner()
    {
        super("permission-scanner", Duration.ofMinutes(45));
    }


    @Override
    public void executeAction()
    {
        WordpressConfig config = this.getManager().getConfig();

        try
        {
            // Enforce base directory permissions
            PermissionCloner baseCloner = new PermissionCloner(config.getBasePath(),
                                                               config.getRequirePermissions(),
                                                               config.getForbidPermissions());
            baseCloner.exclude(config.getPluginListConfig().getBaseDir());
            baseCloner.exclude(config.getThemeListConfig().getBaseDir());
            baseCloner.exclude(config.getUploadDir());

            baseCloner.call();
        }
        catch (Exception e)
        {
            WPManager.log.error("Error while enforcing permissions for base directory: {}", e.getLocalizedMessage(), e);
        }

        try
        {
            // Enforce plugin directory permissions
            PermissionCloner pluginCloner = new PermissionCloner(config.getPluginListConfig().getBaseDir(),
                                                                 config.getPluginListConfig().getRequirePermissions(),
                                                                 config.getPluginListConfig().getForbidPermissions());
            pluginCloner.exclude(config.getThemeListConfig().getBaseDir());
            pluginCloner.exclude(config.getUploadDir());

            pluginCloner.call();
        }
        catch (Exception e)
        {
            WPManager.log.error("Error while enforcing permissions for plugin directory: {}", e.getLocalizedMessage(), e);
        }

        try
        {
            PermissionCloner themeCloner = new PermissionCloner(config.getThemeListConfig().getBaseDir(),
                                                                config.getThemeListConfig().getRequirePermissions(),
                                                                config.getThemeListConfig().getForbidPermissions());
            themeCloner.exclude(config.getPluginListConfig().getBaseDir());
            themeCloner.exclude(config.getUploadDir());

            themeCloner.call();
        }
        catch (Exception e)
        {
            WPManager.log.error("Error while enforcing permissions for theme directory: {}", e.getLocalizedMessage(), e);
        }

        try
        {
            PermissionCloner uploadCloner = new PermissionCloner(config.getUploadDir(),
                                                                 config.getUploadsConfig().getRequirePermissions(),
                                                                 config.getUploadsConfig().getForbidPermissions());
            uploadCloner.exclude(config.getPluginListConfig().getBaseDir());
            uploadCloner.exclude(config.getThemeListConfig().getBaseDir());

            uploadCloner.call();
        }
        catch (Exception e)
        {
            WPManager.log.error("Error while enforcing permissions for upload directory: {}", e.getLocalizedMessage(), e);
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
        private final Set<PosixFilePermission> requirePermissions;
        private final Set<PosixFilePermission> forbidPermissions;

        /**
         * Create a new {@code PermissionCloner} to scan the given directory and enforce the required and
         * forbidden permissions declared. User and group ownership will be copied from the base directory
         * to all descendant entries.
         *
         * @param baseDirectory The {@link Path} of the base directory to scan.
         * @param permSet The set of permissions to require on all entries, in the format required for
         * {@link PosixFilePermissions#fromString(String)}.
         * @param permMask The set of permissions to forbid on all entries, in the format required for
         * {@link PosixFilePermissions#fromString(String)}.
         */
        public PermissionCloner(final Path baseDirectory, final String permSet, final String permMask)
        {
            super();

            this.baseDirectory = baseDirectory;

            // Setup permission sets
            this.requirePermissions = PosixFilePermissions.fromString(permSet);
            this.forbidPermissions = PosixFilePermissions.fromString(permMask);

            this.excludedDirectories = new HashSet<>();

            try
            {
                PosixFileAttributes baseDirAttrs = Files.readAttributes(baseDirectory, PosixFileAttributes.class);
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
            if (this.excludedDirectories.contains(dir)) return FileVisitResult.SKIP_SUBTREE;
            else return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
        {
            if (this.excludedDirectories.contains(file)) return FileVisitResult.CONTINUE;

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

            // These are done separately to prevent logical short-circuiting from preventing the
            // later call from being evaluated.
            boolean changed = permissions.addAll(this.requirePermissions);
            changed |= permissions.removeAll(this.forbidPermissions);
            if (posixAttrs.isDirectory()) changed |= this.applyDirectoryPermissions(permissions);

            if (changed)
            {
                WPManager.log.info("Updating permissions: {} -> {}", file, PosixFilePermissions.toString(permissions));
                Files.setPosixFilePermissions(file, permissions);
            }

            return FileVisitResult.CONTINUE;
        }

        /**
         * Adjust the given set of {@link PosixFilePermission}s to ensure sane directory permissions. Directory
         * owners are always allowed to use a directory (ie: {@link PosixFilePermission#OWNER_EXECUTE}). A
         * group is allowed to use the directory if and only if it can also read the directory. Other users are
         * allowed to use the directory if and only if they can also read the directory.
         *
         * @param permissions The set of permissions to adjust.
         * @return {@code true} if the set of permissions was changed, {@code false} if it was not.
         */
        private boolean applyDirectoryPermissions(final Set<PosixFilePermission> permissions)
        {
            boolean changed = false;

            // Force owner usability
            if (!permissions.contains(PosixFilePermission.OWNER_EXECUTE))
            {
                permissions.add(PosixFilePermission.OWNER_EXECUTE);
                changed = true;
            }

            // Make sure that group execute ability matches the group read ability
            if (permissions.contains(PosixFilePermission.GROUP_READ))
            {
                // The group can read the directory
                if (!permissions.contains(PosixFilePermission.GROUP_EXECUTE))
                {
                    permissions.add(PosixFilePermission.GROUP_EXECUTE);
                    changed = true;
                }
            }
            else
            {
                // Group cannot read the directory
                if (permissions.contains(PosixFilePermission.GROUP_EXECUTE))
                {
                    permissions.remove(PosixFilePermission.GROUP_EXECUTE);
                    changed = true;
                }
            }

            // Make sure that global execute ability matches the global read ability
            if (permissions.contains(PosixFilePermission.OTHERS_READ))
            {
                // Everyone can read the directory
                if (!permissions.contains(PosixFilePermission.OTHERS_EXECUTE))
                {
                    permissions.add(PosixFilePermission.OTHERS_EXECUTE);
                    changed = true;
                }
            }
            else
            {
                // Everyone cannot read the directory
                if (permissions.contains(PosixFilePermission.OTHERS_EXECUTE))
                {
                    permissions.remove(PosixFilePermission.OTHERS_EXECUTE);
                    changed = true;
                }
            }

            return changed;
        }
    }
}
