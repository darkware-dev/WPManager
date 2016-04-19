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

package org.darkware.wpman.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author jeff
 * @since 2016-01-23
 */
public class WPPlugin extends WPUpdatableComponent
{
     @JsonProperty("title") private String name;
    private String description;
    private WPPluginStatus status;

    public WPPlugin()
    {
        super();
    }

    @Override
    protected String getConfigSection()
    {
        return "plugin";
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public WPPluginStatus getStatus()
    {
        return status;
    }

    public void setStatus(final WPPluginStatus status)
    {
        this.status = status;
    }
}
