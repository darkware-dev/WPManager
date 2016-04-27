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

import org.darkware.cltools.utils.FileSystemTools;
import org.darkware.wpman.WPManager;
import org.darkware.wpman.data.WPPlugin;
import org.darkware.wpman.wpcli.WPCLI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author jeff
 * @since 2016-02-10
 */
public class WPPluginRemove extends WPBasicAction<Boolean>
{
    private final String pluginToken;

    public WPPluginRemove(final String pluginToken)
    {
        super(WPActionCategory.MAINTENANCE);

        this.pluginToken = pluginToken;
    }

    @Override
    public String getDescription()
    {
        return "Auto-install plugin: " + this.pluginToken;
    }

    @Override
    public Boolean exec()
    {
        try
        {
            // Check to see if the plugin is already installed.
            WPPlugin preInstall = this.getManager().getData().getPlugins().get(this.pluginToken);

            if (preInstall == null)
            {
                WPManager.log.info("Plugin is not loaded: {}", this.pluginToken);
            }
            else
            {
                // Run an removal
                WPCLI delete = this.getWPCWpcliFactory().build("plugin", "delete", this.pluginToken);

                if (delete.checkSuccess())
                {
                    WPManager.log.info("Plugin removed: {}", this.pluginToken);
                }
                else
                {
                    WPManager.log.warn("Failed to remove plugin: {}", this.pluginToken);
                }
            }

            // Check to see if the directory remains. If so, remove it.
            Path pluginDir = this.getManager().getPluginDir().resolve(this.pluginToken);
            if (Files.exists(pluginDir))
            {
                WPManager.log.warn("Plugin directory still exists: {}", pluginDir);
                try
                {
                    FileSystemTools.deleteTree(pluginDir);
                }
                catch (IOException e)
                {
                    WPManager.log.warn("Error while trying to remove plugin directory: {}", pluginDir);
                    Path pluginGutter = this.getManager().getConfig().getPluginListConfig().getGutterDir();

                    try
                    {
                        if (Files.notExists(pluginGutter)) Files.createDirectories(pluginGutter);
                        Files.move(pluginDir, pluginGutter.resolve(this.pluginToken));
                        WPManager.log.info("Plugin directory moved to the gutter: {}", pluginDir);
                    }
                    catch (IOException e2)
                    {
                        WPManager.log.warn("Error while trying to move plugin directory to the gutter: {}", pluginDir);
                    }
                }
            }
        }
        catch (Throwable t)
        {
            WPManager.log.warn("Unexpected error while removing '{}': {}", this.pluginToken, t.getLocalizedMessage(), t);
        }
        finally
        {
            this.getManager().getData().getPlugins().expire();
        }

        return true;
    }
}
