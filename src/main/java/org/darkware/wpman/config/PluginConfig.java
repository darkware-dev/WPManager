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
import org.darkware.wpman.data.WPPluginStatus;

/**
 * This class is a configuration container for an individual instance plugin.
 *
 * @author jeff
 * @since 2016-03-28
 */
public class PluginConfig extends UpdatableConfig
{
    @JsonProperty("status")
    private WPPluginStatus status;

    /**
     * Create a new configuration container with default settings.
     */
    public PluginConfig()
    {
        super();

        this.status = WPPluginStatus.UNDECLARED;
    }

    /**
     * Create a new configuration container for a plugin with the given ID.
     *
     * @param id The plugin ID.
     */
    @SuppressWarnings("unused")
    public PluginConfig(final String id)
    {
        this();
    }

    /**
     * Fetches the target status of this plugin. Any processes checking or enforcing the status will use this
     * as the "correct" status for comparisons or changes.
     *
     * @return A {@link WPPluginStatus}.
     */
    public WPPluginStatus getStatus()
    {
        return this.status;
    }

    /**
     * Set the target status for this plugin.
     *
     * @param status The target {@link WPPluginStatus}.
     */
    protected void setStatus(final WPPluginStatus status)
    {
        this.status = status;
    }
}
