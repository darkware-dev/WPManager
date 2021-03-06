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

package org.darkware.wpman.rest;

import org.darkware.wpman.WPManager;
import org.darkware.wpman.config.PluginConfig;
import org.darkware.wpman.config.WordpressConfig;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * This is a simple REST interface for exposing configuration data.
 *
 * @author jeff
 * @since 2016-04-11
 */
@Path("config")
public class ConfigResource
{
    @SuppressWarnings("unused")
    private final WPManager manager;
    private final WordpressConfig config;

    /**
     * Create a new Configuration REST resource attached to the given manager.
     *
     * @param manager The {@link WPManager} to report configuration data for.
     */
    public ConfigResource(final WPManager manager)
    {
        super();

        this.manager = manager;
        this.config = manager.getConfig();
    }

    /**
     * Fetch the entire active policy.
     *
     * @return The active policy as a {@link WordpressConfig} instance.
     */
    @GET
    @Path("wpman")
    @Produces(MediaType.APPLICATION_JSON)
    public WordpressConfig getConfig()
    {
        return this.config;
    }

    /**
     * Fetch the set of policy fragments for plugins.
     *
     * @return A {@link Map} of plugin slugs to their {@link PluginConfig} fragments.
     */
    @GET
    @Path("plugins")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, PluginConfig> getPluginPolicy()
    {
        return this.config.getPluginListConfig().getItems();
    }

}
