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

package org.darkware.wpman.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a configuration container for the permissions configuration. It contains various options
 * for controlling how permissions are scanned and set, along with important directories and various
 * exceptions to the normal behavior.
 *
 * @author jeff
 * @since 2016-05-24
 */
public class FilePermissionsConfig
{
    @JsonProperty("exampleDirectories")
    private Set<Path> exampleDirectories;
    @JsonProperty("ignorePaths")
    private Set<Path> ignorePaths;
    @JsonProperty("scanFrequency")
    private int scanFrequency;

    /**
     * Create a new configuration container with no directories or paths registered.
     */
    public FilePermissionsConfig()
    {
        super();

        this.exampleDirectories = new HashSet<>();
        this.ignorePaths = new HashSet<>();
    }

    /**
     * Fetch the set of example directories to use for permission scanning. These are the directories
     * which set the policy for every directory entry beneath them.
     *
     * @return A {@link Set} of {@link Path}s.
     */
    public Set<Path> getExampleDirectories()
    {
        return this.exampleDirectories;
    }

    /**
     * Set the example directories set. This will completely replace the current set.
     *
     * @param exampleDirectories A {@code Set} of {@code Path}s.
     */
    protected void setExampleDirectories(final Set<Path> exampleDirectories)
    {
        this.exampleDirectories = exampleDirectories;
    }

    /**
     * Fetch the set of file system items that should be ignored during permission setting. If the path
     * points to a file, just that file will be ignored. If it points to a directory, the entire directory
     * tree under that path will be ignored.
     *
     * @return A {@link Set} of {@link Path}s.
     */
    public Set<Path> getIgnorePaths()
    {
        return this.ignorePaths;
    }

    /**
     * Set the ignored paths set. This will completely replace the current set.
     *
     * @param ignorePaths A {@code Set} of {@code Path}s.
     */
    protected void setIgnorePaths(final Set<Path> ignorePaths)
    {
        this.ignorePaths = ignorePaths;
    }

    /**
     * Sets the frequency of permission scans.
     *
     * @return The delay between permission scans, in minutes.
     */
    public int getScanFrequency()
    {
        return this.scanFrequency;
    }

    /**
     * Set the frequency of permission scans. This is interpreted as the number of minutes between scans.
     *
     * @param scanFrequency The number of minutes to delay between scans.
     */
    protected void setScanFrequency(final int scanFrequency)
    {
        this.scanFrequency = scanFrequency;
    }
}
