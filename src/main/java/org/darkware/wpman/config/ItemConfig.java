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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;

/**
 * @author jeff
 * @since 2016-05-03
 */
public class ItemConfig
{
    protected transient Path src;

    /**
     * Create a new item configuration.
     */
    public ItemConfig()
    {
        super();

        this.src = null;
    }

    /**
     * Create a new item configuration attached to the given fragment file.
     *
     * @param srcFile The {@link Path} to the fragment file which generated this configuration.
     */
    public ItemConfig(final Path srcFile)
    {
        super();

        this.src = srcFile;
    }

    /**
     * Fetches the path to the file which provided the configuration for this item.
     *
     * @return A {@link Path} to the configuration fragment, or {@code null} if the item is configured in the
     * master configuration.
     */
    @JsonProperty("srcFile")
    public Path getPolicyFile()
    {
        return this.src;
    }

    /**
     * Sets the path to the file which provided the configuration for the item.
     *
     * @param src The {@link Path} to the originating file.
     */
    public void setPolicyFile(final Path src)
    {
        this.src = src;
    }

    /**
     * Checks to see if this item was configured via policy fragment file, or the global policy.
     * @return {@code true} if the item has a corresponding policy fragment, {@code false} if it was configured
     * in the global policy file.
     */
    @JsonIgnore
    public boolean hasPolicyFile()
    {
        return this.src != null;
    }
}
