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

package org.darkware.wpman.agents;

import org.darkware.wpman.WPManager;
import org.joda.time.Duration;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 *
 * @author jeff
 * @since 2016-02-09
 */
public class WPPluginSync extends WPPeriodicAgent
{
    private final static Path defaultPath = Paths.get("plugin.list");

    private final Path pluginListPath;

    /**
     * Create a plugin synchronization agent.
     */
    public WPPluginSync()
    {
        super("Plugin Sync", Duration.standardMinutes(1));

        this.pluginListPath = this.resolvePluginListPath();
    }

    private Path resolvePluginListPath()
    {
        Path pluginListPath = this.getManager().getConfig().readVariable("plugins.autoinstall", defaultPath);

        if (pluginListPath.isAbsolute()) return pluginListPath;
        else return this.getManager().getConfig().getRootPath().resolve(pluginListPath);
    }

    @Override
    public void executeAction()
    {
        WPManager.log.info("Starting plugin synchronization: {}", this.pluginListPath);

    }
}
