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

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jeff
 * @since 2016-03-08
 */
public class ScanResults
{
    private final Set<Path> missingFiles;
    private final Set<Path> changedFiles;
    private final Set<Path> newFiles;

    public ScanResults(final Set<Path> expectedFiles)
    {
        super();

        this.missingFiles = new HashSet<>(expectedFiles);
        this.changedFiles = new HashSet<>();
        this.newFiles = new HashSet<>();
    }

    /**
     * Report a file as being found. This removes it from the set of expected files.
     *
     * @param file The file which was found.
     */
    public void reportFound(final Path file)
    {
        this.missingFiles.remove(file);
    }

    /**
     * Report a file as being changed from the version previously recorded.
     *
     * @param file The file which was changed.
     */
    public void reportChanged(final Path file)
    {
        this.changedFiles.add(file);
    }

    /**
     * Report a file that was not found in the database.
     *
     * @param file The file that was found.
     */
    public void reportNew(final Path file)
    {
        this.newFiles.add(file);
    }

    /**
     * Fetch the set of files with integrity checksums that weren't encountered in the scan.
     *
     * @return A {@code Set} of {@code Path Paths} of all files in the database that weren't scanned.
     */
    public Set<Path> getMissingFiles()
    {
        return Collections.unmodifiableSet(missingFiles);
    }

    /**
     * Fetch the set of files whose checksums are different from what was recorded in the database.
     *
     * @return A {@code Set} of {@code Path Paths} of all files that have changed.
     */
    public Set<Path> getChangedFiles()
    {
        return Collections.unmodifiableSet(changedFiles);
    }

    /**
     * Fetch the set of files that weren't found in the integrity database.
     *
     * @return A {@code Set} of {@code Path Paths} of all new files.
     */
    public Set<Path> getNewFiles()
    {
        return Collections.unmodifiableSet(newFiles);
    }
}
