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

package org.darkware.wpman.actions;

import org.darkware.wpman.data.WPPlugin;

import java.nio.file.Path;

/**
 * This is a {@link WPAction} which aims to ensure that the highest available and
 * configured plugin version is installed for a given plugin. The plugin does not
 * need to be previously loaded.
 *
 * @author jeff
 * @since 2016-02-10
 */
public class WPPluginAutoInstall extends WPItemAutoInstall<WPPlugin>
{
    /**
     * Create a new agent which will attempt to automatically install or update
     * a plugin.
     *
     * @param installToken The ID of the plugin to install or update.
     */
    public WPPluginAutoInstall(final String installToken)
    {
        super("plugin", installToken);
    }

    @Override
    protected WPPlugin getItem()
    {
        return this.getManager().getData().getPlugins().get(this.installToken);
    }

    @Override
    protected void expireItemContainer()
    {
        // Trigger a refresh of plugin data the next time its needed.
        this.getManager().getData().getPlugins().expire();
    }

    @Override
    protected Path getContainerPath()
    {
        return this.getManager().getPluginDir();
    }
}
