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

package org.darkware.wpman.events;

import org.darkware.wpman.security.ChecksumDatabase;
import org.darkware.wpman.security.ScanResults;

import java.nio.file.Path;
import java.util.Set;

/**
 * An {@code InstallationFileChange} is an {@code WPEvent} that is dispatched when the WPManager detects
 * new, deleted, or changed files in the WordPress installation.
 *
 * @author jeff
 * @since 2016-03-18
 */
public class InstallationFileChange implements WPEvent
{
    private final ScanResults scanResults;

    /**
     * Create a new {@code InstallationFileChange}.
     *
     * @param scanResults The results of a scan against a {@link ChecksumDatabase}.
     */
    public InstallationFileChange(final ScanResults scanResults)
    {
        super();

        this.scanResults = scanResults;
    }

    /**
     * Fetch a {@code Set} of files which changed from the last scanned state.
     *
     * @return A {@code Set} of {@code Path Paths} that have changed.
     */
    public Set<Path> getChangedFiles()
    {
        return this.scanResults.getChangedFiles();
    }

    /**
     * Fetch a {@code Set} of files which are missing from the last scanned state.
     *
     * @return A {@code Set} of {@code Path Paths} that have been recently deleted.
     */
    public Set<Path> getMissingFiles()
    {
        return scanResults.getMissingFiles();
    }

    /**
     * Fetch a {@code Set} of files which were not previously recorded.
     *
     * @return A {@code Set} of {@code Path Paths} that have been created recently.
     */
    public Set<Path> getNewFiles()
    {
        return scanResults.getNewFiles();
    }
}
