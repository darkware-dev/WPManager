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

package org.darkware.wpman.security;

import org.darkware.wpman.ContextManager;
import org.darkware.wpman.config.WordpressConfig;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@code DirectoryScanner} is a utility class that contains logic for performing recursive directory
 * traversals while checking files against a {@link ChecksumDatabase}.
 *
 * @author jeff
 * @since 2016-03-08
 */
public class DirectoryScanner extends SimpleFileVisitor<Path>
{
    /** The configuration for the scanner. */
    protected final WordpressConfig config;
    /** The {@code ChecksumDatabase} this scanner checks files against. */
    protected final ChecksumDatabase checksums;
    private final Path root;
    private final Set<Path> pruneDirectories;
    private final Set<Path> ignoreFiles;
    private final ScanResults results;
    private boolean updateChecksums;

    /**
     * Create a new {@code DirectoryScanner} which scans files under the given {@code Path} against
     * the {@code ChecksumDatabase}.
     *
     * @param root The directory to use as the root of the scan.
     * @param checksums The {@code ChecksumDatabase} to check files against.
     */
    public DirectoryScanner(final Path root, final ChecksumDatabase checksums)
    {
        super();

        this.config = ContextManager.local().getContextualInstance(WordpressConfig.class);
        this.checksums = checksums;
        this.root = root;
        this.pruneDirectories = new HashSet<>();
        this.ignoreFiles = new HashSet<>();
        this.results = new ScanResults(this.checksums.entriesForPath(root));

        this.prune(this.config.getUploadDir());
        this.prune(this.config.getPluginListConfig().getGutterDir());
        this.prune(this.config.getThemeListConfig().getGutterDir());

        this.ignore(this.root.resolve("wp-content/debug.log"));
    }

    /**
     * Declare whether the scanner will request checksum updates during its scan. Updates will ensure
     * that changes aren't detected by future scans.
     *
     * @param update {@code true} if the scan should trigger updates.
     */
    public void updateChecksums(final boolean update)
    {
        this.updateChecksums = update;
    }

    /**
     * Register a directory that should be skipped by the scan. This will prevent all files and directories
     * under the given path from being checked.
     *
     * @param directory The directory to skip.
     */
    public void prune(final Path directory)
    {
        this.pruneDirectories.add(directory);
    }

    /**
     * Register a file that should be skipped during the scan. No changes will be recorded for this file.
     *
     * @param file The file to ignore.
     */
    public void ignore(final Path file)
    {
        this.ignoreFiles.add(file);
    }

    /**
     * Scan the directory and aggregate teh list of changes that were detected during the scan.
     *
     * @return A {@link ScanResults} object containing lists of changed, new, and deleted files.
     */
    public ScanResults scan()
    {
        Set<FileVisitOption> options = new HashSet<>();
        try
        {
            Files.walkFileTree(this.root, options, 20, this);

            // Pull out missing files that are suppressed
            this.results.filterSuppressedEntries(this.checksums);

            // Remove entries for missing files
            this.checksums.removeAll(this.results.getMissingFiles());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return this.results;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
    {
        if (this.pruneDirectories.contains(dir))
        {
            ChecksumDatabase.log.debug("Skipping directory: ", dir);
            return FileVisitResult.SKIP_SUBTREE;
        }
        else if (dir.toString().endsWith(".bak"))
        {
            ChecksumDatabase.log.debug("Ignoring directory: {}", dir);
            return FileVisitResult.SKIP_SUBTREE;
        }
        else if (this.checksums.isSuppressed(dir))
        {
            ChecksumDatabase.log.debug("Suppressed directory: {}", dir);
            return FileVisitResult.SKIP_SUBTREE;
        }
        else return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
    {
        // Check to see if the file should be ignored
        if (this.ignoreFiles.contains(file)) return FileVisitResult.CONTINUE;

        if (this.checksums.hasChecksum(file))
        {
            this.results.reportFound(file);
            if (!this.checksums.check(file) && !this.checksums.isSuppressed(file))
            {
                this.results.reportChanged(file);
                if (this.updateChecksums) this.checksums.update(file);
            }
        }
        else
        {
            this.results.reportNew(file);
            if (this.updateChecksums) this.checksums.update(file);
        }

        return FileVisitResult.CONTINUE;
    }
}
