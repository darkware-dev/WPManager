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
import org.darkware.wpman.config.ReloadableWordpressConfig;
import org.darkware.wpman.events.ConfigurationFileChange;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a {@link WPPeriodicAgent} which checks for changes to the configuration and reloads the profile
 * as needed.
 *
 * @author jeff
 * @since 2016-06-05
 */
public class WPConfigWatcher extends WPPeriodicAgent
{
    private final ReloadableWordpressConfig reloadableConfig;
    private final WatchService watcher;

    /**
     * Creates a new configuration watching agent with the default scan time.
     *
     * @throws IOException If there is an error while setting up a {@link WatchService}.
     */
    public WPConfigWatcher() throws IOException
    {
        super("ConfigWatcher", Duration.ofMinutes(1));

        Set<Path> watchPaths = new HashSet<>();

        if (this.getManager().getConfig() instanceof ReloadableWordpressConfig)
        {
            this.reloadableConfig = (ReloadableWordpressConfig)this.getManager().getConfig();
            this.watcher = FileSystems.getDefault().newWatchService();

            // Build the watch paths
            watchPaths.add(this.reloadableConfig.getPolicyRoot());
            watchPaths.add(this.reloadableConfig.getThemeListConfig().getPolicyRoot());
            watchPaths.add(this.reloadableConfig.getPluginListConfig().getPolicyRoot());

            for (Path watchPath: watchPaths)
            {
                watchPath.register(this.watcher,
                                   StandardWatchEventKinds.ENTRY_CREATE,
                                   StandardWatchEventKinds.ENTRY_DELETE,
                                   StandardWatchEventKinds.ENTRY_MODIFY);
            }
        }
        else
        {
            this.reloadableConfig = null;
            this.watcher = null;
        }
    }

    @Override
    public void executeAction()
    {
        try
        {
            Set<Path> changedFiles = new HashSet<>();
            while (true)
            {
                WatchKey key = this.watcher.poll();
                if (key == null) break;

                key.pollEvents().stream().map(e -> (WatchEvent<Path>)e).forEach(event ->
                {
                    WatchEvent.Kind kind = event.kind();

                    Path file = event.context();

                    if (kind == StandardWatchEventKinds.OVERFLOW) return;
                    else if (this.isConfigFile(file))
                    {
                        changedFiles.add(file);
                    }

                    if (!key.reset()) return;
                });

                Thread.yield();
            }

            // Reload the config if we found changed files
            if (!changedFiles.isEmpty())
            {
                WPManager.log.info("Found {} changed config file{}", changedFiles.size(), (changedFiles.size() == 1) ? "" : "s");
                this.getManager().dispatchEvent(new ConfigurationFileChange(changedFiles));
            }
        }
        catch (Throwable t)
        {
            WPManager.log.error("Error while checking for configuration changes: {}", t.getLocalizedMessage(), t);
        }
    }

    /**
     * Checks to see if the path appears to be a file that would be loaded as configuration.
     *
     * @param file The {@link Path} to check.
     * @return {@code true} if the path appears to be a configuration file.
     */
    private boolean isConfigFile(final Path file)
    {
        String filename = file.getName(file.getNameCount()-1).toString();

        if (filename.startsWith(".")) return false;
        if (filename.endsWith(".yml")) return true;

        return false;
    }
}
