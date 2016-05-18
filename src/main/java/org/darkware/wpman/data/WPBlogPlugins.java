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

import com.google.common.reflect.TypeToken;
import org.darkware.lazylib.LazyLoaded;
import org.darkware.wpman.wpcli.WPCLI;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@code WPBlogPlugins} object is a specialized collection of {@link WPPlugin} objects that are
 * fetched specifically to reflect their relationship with a specific {@link WPBlog}.
 *
 * @author jeff
 * @since 2016-01-31
 */
public class WPBlogPlugins extends WPComponent
{
    private final WPBlog blog;
    private final LazyLoaded<Map<String, WPPlugin>> plugins;

    public WPBlogPlugins(final WPBlog blog)
    {
        super();

        this.blog = blog;
        this.plugins = new LazyLoaded<Map<String, WPPlugin>>(Duration.ofHours(1))
        {
            @Override
            protected Map<String, WPPlugin> loadValue() throws Exception
            {
                WPCLI pluginListCmd = WPBlogPlugins.this.buildCommand("plugin", "list");
                pluginListCmd.loadPlugins(false);
                pluginListCmd.loadThemes(false);
                pluginListCmd.setBlog(WPBlogPlugins.this.blog);
                WPPlugin.setFields(pluginListCmd);

                Map<String, WPPlugin> pluginMap = new HashMap<>();
                pluginListCmd.readJSON(new TypeToken<List<WPPlugin>>(){}).forEach(p -> pluginMap.put(p.getId(), p));

                return pluginMap;
            }
        };
    }

    /**
     * Fetch the list of plugins and their state for this blog.
     *
     * @return A {@code Set} of {@code WPPlugin} objects with their correct status for the targeted blog.
     */
    public Set<WPPlugin> getPlugins()
    {
        return Collections.unmodifiableSet(this.plugins.value().values().stream().collect(Collectors.toSet()));
    }

    /**
     * Fetch the record for the plugin matching the given ID. The plugin data will reflect the plugin's state
     * with respect to the targeted blog.
     *
     * @param id The plugin ID to fetch.
     * @return A {@code WPPlugin} object, or {@code null} if the plugin is not installed.
     */
    public WPPlugin get(final String id)
    {
        return this.plugins.value().get(id);
    }

    /**
     *
     */
    public boolean isEnabled(final String id)
    {
        return this.get(id).getStatus().isEnabled();
    }
}
