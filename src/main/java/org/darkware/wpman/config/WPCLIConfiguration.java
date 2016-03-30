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
import com.sun.istack.internal.NotNull;

import javax.validation.Valid;
import java.nio.file.Path;

/**
 * @author jeff
 * @since 2016-03-28
 */
public class WPCLIConfiguration
{
    @NotNull
    private Path binaryPath;

    /**
     * Fetch the path to the WP-CLI binary.
     *
     * @return A {@code Path} to the WP-CLI binary.
     */
    @JsonProperty("bin")
    @Valid
    public Path getBinaryPath()
    {
        return this.binaryPath;
    }

    /**
     * Sets the path to the WP-CLI binary.
     *
     * @param binaryPath The path to the binary.
     */
    @JsonProperty("bin")
    public void setBinaryPath(final Path binaryPath)
    {
        this.binaryPath = binaryPath;
    }
}
