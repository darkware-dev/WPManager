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
import org.darkware.wpman.data.Version;

/**
 * @author jeff
 * @since 2016-03-28
 */
public class UpdatableConfig
{
    private Boolean updatable = true;

    private Version maxVersion;

    @JsonProperty("updatable")
    public Boolean getUpdatable()
    {
        return updatable;
    }

    @JsonProperty("updatable")
    public void setUpdatable(final Boolean updatable)
    {
        this.updatable = updatable;
    }

    @JsonProperty("maxVersion")
    public Version getMaxVersion()
    {
        return maxVersion;
    }

    @JsonProperty("maxVersion")
    public void setMaxVersion(final Version maxVersion)
    {
        this.maxVersion = maxVersion;
    }
}
