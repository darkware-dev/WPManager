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

import org.darkware.cltools.utils.ListFile;
import org.darkware.wpman.Config;
import org.darkware.wpman.WPManager;
import org.darkware.wpman.actions.WPPluginAutoInstall;
import org.darkware.wpman.actions.WPPluginRemove;
import org.darkware.wpman.data.WPPlugins;
import org.darkware.wpman.data.WPUpdatableComponent;
import org.joda.time.Duration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 *
 * @author jeff
 * @since 2016-02-09
 */
public class WPPluginSync extends WPPeriodicAgent
{
    private final static Path defaultPath = Paths.get("plugin.list");

    /**
     * Create a plugin synchronization agent.
     */
    public WPPluginSync()
    {
        super("Plugin Sync", Duration.standardMinutes(1));
    }

    private void installPlugin(final String pluginId)
    {
        WPManager.log.info("INSTALLING: {}", pluginId);
        this.getManager().scheduleAction(new WPPluginAutoInstall(pluginId));
    }

    private void removePlugin(final String pluginId)
    {
        WPManager.log.info("REMOVING: {}", pluginId);
        this.getManager().scheduleAction(new WPPluginRemove(pluginId));
    }

    @Override
    public void executeAction()
    {
        WPManager.log.info("Starting plugin synchronization.");

        // Some helpful objects
        Config config = this.getManager().getConfig();
        WPPlugins plugins = this.getManager().getData().getPlugins();

        Path pluginListPath = config.readVariable("plugins.autoinstall", Paths.get("plugin.list"));
        if (!pluginListPath.isAbsolute()) pluginListPath = config.getRootPath().resolve(pluginListPath);

        ListFile pluginListFile = new ListFile(pluginListPath);
        pluginListFile.setCommentTokens("#", ";", "//");

        // Collect the set of installed plugin ids
        Set<String> installedPlugins = new TreeSet<>();
        plugins.stream().map(WPUpdatableComponent::getId).forEach(installedPlugins::add);

        // Collect the set of requested plugin ids
        Set<String> requestedPlugins = new TreeSet<>();
        pluginListFile.stream().forEach(requestedPlugins::add);

        // Install missing plugins
        requestedPlugins.stream().filter(p -> !installedPlugins.contains(p)).forEach(this::installPlugin);

        // Remove extraneous plugins
        installedPlugins.stream().filter(p -> !requestedPlugins.contains(p)).forEach(this::removePlugin);

        // Find plugins to update, ignoring any plugins scheduled for removal
        plugins.stream().filter(WPUpdatableComponent::hasUpdate).map(WPUpdatableComponent::getId).forEach(this::installPlugin);
    }
}
