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

import java.nio.file.attribute.PosixFilePermissions;

/**
 * This is a container for configuration regarding the WordPress Media Library, more commonly recognized as
 * the shared uploads directory.
 *
 * @author jeff
 * @since 2016-05-05
 */
public class UploadsConfig
{
    private String requirePermissions;
    private String forbidPermissions;

    public UploadsConfig()
    {
        super();

        this.requirePermissions = "rw-r-----";
        this.forbidPermissions = "--x--xrwx";
    }

    /**
     * Fetch the set of permissions to force on all filesystem items within the collection directory. This applies
     * to both files and directories, though additional changes will be made to directories to retain sane
     * filesystem behavior.
     *
     * @return The set of permissions, formatted according to {@link PosixFilePermissions#fromString(String)}.
     */
    @JsonProperty("requirePermissions")
    public String getRequirePermissions()
    {
        return this.requirePermissions;
    }

    /**
     * Set the permissions to force on all filesystem items in the collection directory.
     *
     * @param requirePermissions The set of permissions, formatted according to {@link PosixFilePermissions#fromString(String)}.
     */
    public void setRequirePermissions(final String requirePermissions)
    {
        this.requirePermissions = requirePermissions;
    }

    /**
     * Fetch the set of permissions to forbid on all filesystem items within the collection directory. This applies
     * to both files and directories, though additional changes will be made to directories to retain sane
     * filesystem behavior.
     *
     * @return The set of permissions, formatted according to {@link PosixFilePermissions#fromString(String)}.
     */
    @JsonProperty("forbidPermissions")
    public String getForbidPermissions()
    {
        return this.forbidPermissions;
    }

    /**
     * Set the permissions to forbid on all filesystem items in the collection directory.
     *
     * @param forbidPermissions The set of permissions, formatted according to {@link PosixFilePermissions#fromString(String)}.
     */
    public void setForbidPermissions(final String forbidPermissions)
    {
        this.forbidPermissions = forbidPermissions;
    }
}
