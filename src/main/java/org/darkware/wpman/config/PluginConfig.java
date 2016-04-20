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

/**
 * This class is a configuration container for an individual instance plugin.
 *
 * @author jeff
 * @since 2016-03-28
 */
public class PluginConfig extends UpdatableConfig
{
    @JsonProperty("networkEnable")
    private boolean networkEnabled;

    /**
     * Create a new configuration container with default settings.
     */
    public PluginConfig()
    {
        super();

        this.networkEnabled = false;
    }

    /**
     * Create a new configuration container for a plugin with the given ID.
     *
     * @param id The plugin ID.
     */
    public PluginConfig(final String id)
    {
        this();
    }

    /**
     * Check if this plugin should be enabled across the network.
     *
     * @return {@code true} if the plugin should be network enabled, {@code false} if it should not.
     */
    public boolean isNetworkEnabled()
    {
        return this.networkEnabled;
    }

    /**
     * Declare if this plugin should be network enabled.
     *
     * @param networkEnabled A flag declaring if the plugin should be enabled for all sites.
     */
    protected void setNetworkEnabled(final boolean networkEnabled)
    {
        this.networkEnabled = networkEnabled;
    }
}
