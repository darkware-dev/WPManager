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

import com.google.common.eventbus.Subscribe;
import com.google.common.reflect.TypeToken;
import org.darkware.wpman.events.WPPluginInstallEvent;
import org.darkware.wpman.events.WPPluginUpdateEvent;
import org.darkware.wpman.wpcli.WPCLI;

import java.util.List;

/**
 * @author jeff
 * @since 2016-01-23
 */
public class WPPlugins extends WPUpdatableCollection<WPPlugin> implements Iterable<WPPlugin>
{
    public WPPlugins()
    {
        super("plugins");
    }

    @Override
    protected List<WPPlugin> fetchNewItems()
    {
        WPCLI pluginListCmd = this.buildCommand("plugin", "list");
        pluginListCmd.loadPlugins(false);
        pluginListCmd.loadThemes(false);
        WPPlugin.setFields(pluginListCmd);

        return pluginListCmd.readJSON(new TypeToken<List<WPPlugin>>(){});
    }

    /**
     * This method is automatically triggered when a plugin is installed.
     *
     * @param event The installation event.
     */
    @Subscribe
    public void onPluginInstall(final WPPluginInstallEvent event)
    {
        this.expire();
    }

    /**
     * This method is automatically triggered when a plugin is updated.
     *
     * @param event The update event.
     */
    @Subscribe
    public void onPluginUpdate(final WPPluginUpdateEvent event)
    {
        this.expire();
    }
}
