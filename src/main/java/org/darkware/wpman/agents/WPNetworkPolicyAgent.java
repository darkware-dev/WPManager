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
import org.darkware.wpman.config.PluginConfig;
import org.darkware.wpman.config.ThemeConfig;
import org.darkware.wpman.config.ThemeListConfig;
import org.darkware.wpman.data.WPPlugin;
import org.darkware.wpman.data.WPPluginStatus;
import org.darkware.wpman.data.WPPlugins;
import org.darkware.wpman.data.WPTheme;
import org.darkware.wpman.data.WPThemes;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIFlag;

import java.time.Duration;

/**
 * A {@code WPNetworkPolicyAgent} is a {@link WPPeriodicAgent} which enforces general network policy
 * settings.
 *
 * @author jeff
 * @since 2016-04-19
 */
public class WPNetworkPolicyAgent extends WPPeriodicAgent
{
    private final WPPlugins plugins;
    private final WPThemes themes;

    /**
     * Creates a new agent to enforce network plugin and theme policy.
     */
    public WPNetworkPolicyAgent()
    {
        super("network-policy", Duration.ofMinutes(15));

        this.plugins = this.getManager().getData().getPlugins();
        this.themes = this.getManager().getData().getThemes();
    }

    @Override
    public void executeAction()
    {
        this.examinePlugins();
        this.examineThemes();
    }

    /**
     * Examine all plugins, applying plugin policy as needed.
     */
    private void examinePlugins()
    {
        this.plugins.stream().forEach(this::examinePlugin);
    }

    /**
     * Examine the given plugin, applying plugin policy. This will automatically resolve the configuration.
     *
     * @param plugin The {@link WPPlugin} to examine.
     * @see #examinePlugin(WPPlugin, PluginConfig)
     */
    private void examinePlugin(final WPPlugin plugin)
    {
        this.examinePlugin(plugin, this.getManager().getConfig().getPluginListConfig().getConfig(plugin.getId()));
    }

    /**
     * Examine the given plugin, applying the policy from the supplied configuration.
     *
     * @param plugin The {@link WPPlugin} to examine.
     * @param config The {@link PluginConfig} configuration to read policy from.
     */
    private void examinePlugin(final WPPlugin plugin, final PluginConfig config)
    {
        // We only enforce plugin status if the config declares a status
        if (config.getStatus() != WPPluginStatus.UNDECLARED)
        {
            // Check if the status is not what the config declares
            if (config.getStatus() != plugin.getStatus())
            {
                if (config.getStatus() == WPPluginStatus.NETWORK_ACTIVE)
                {
                    WPCLI activatePlugin = this.getManager().getBuilder().build("plugin", "activate", plugin.getId());
                    activatePlugin.loadThemes(false);
                    activatePlugin.loadPlugins(false);
                    activatePlugin.setOption(new WPCLIFlag("network"));

                    if (activatePlugin.checkSuccess())
                    {
                        WPManager.log.info("Activated the plugin '{}' on the network (via policy)", plugin.getName());
                    }
                    else
                    {
                        WPManager.log.error("Failed to network activate '{}'", plugin.getName());
                    }
                }
                else if (config.getStatus() == WPPluginStatus.NETWORK_ACTIVE)
                {
                    WPCLI deactivatePlugin = this.getManager().getBuilder().build("plugin", "deactivate", plugin.getId());
                    deactivatePlugin.loadThemes(false);
                    deactivatePlugin.loadPlugins(false);
                    deactivatePlugin.setOption(new WPCLIFlag("network"));

                    if (deactivatePlugin.checkSuccess())
                    {
                        WPManager.log.info("Deactivated the plugin '{}' on the network (via policy)", plugin.getName());
                    }
                    else
                    {
                        WPManager.log.error("Failed to deactivate '{}'", plugin.getName());
                    }
                }
            }
        }
    }

    /**
     * Examine all themes, applying plugin policy as needed.
     */
    private void examineThemes()
    {
        this.themes.stream().forEach(this::examineTheme);
    }

    /**
     * Examine the given theme, applying theme policy. This will automatically resolve the configuration.
     *
     * @param theme The {@link WPTheme} to examine.
     */
    private void examineTheme(final WPTheme theme)
    {
        ThemeListConfig listConfig = this.getManager().getConfig().getThemeListConfig();
        this.examineTheme(theme, listConfig, listConfig.getConfig(theme.getId()));
    }

    /**
     * Examine the given theme, applying the policy from the supplied configuration.
     *
     * @param theme The {@link WPTheme} to examine.
     * @param listConfig The {@link ThemeListConfig} which supplies global theme configuration
     * @param config The {@link ThemeConfig} for the given theme.
     */
    private void examineTheme(final WPTheme theme, final ThemeListConfig listConfig, final ThemeConfig config)
    {
        Boolean enable = listConfig.getDefaultEnabled();
        if (config.getEnabled() != null) enable = config.getEnabled();

        if (enable != null)
        {
            if (enable && !theme.isEnabled())
            {
                WPCLI enableTheme = this.getManager().getBuilder().build("theme", "enable", theme.getId());
                enableTheme.loadThemes(false);
                enableTheme.loadPlugins(false);
                enableTheme.setOption(new WPCLIFlag("network"));

                if (enableTheme.checkSuccess())
                {
                    WPManager.log.info("Enabled the theme '{}' [{}]", theme.getName(), theme.getId());
                }
                else
                {
                    WPManager.log.error("Failed to enable the theme '{}' [{}]", theme.getName(), theme.getId());
                }
            }
            else if (!enable && theme.isEnabled())
            {
                WPCLI disableTheme = this.getManager().getBuilder().build("theme", "disable", theme.getId());
                disableTheme.loadThemes(false);
                disableTheme.loadPlugins(false);
                disableTheme.setOption(new WPCLIFlag("network"));

                if (disableTheme.checkSuccess())
                {
                    WPManager.log.info("Disabled the theme '{}' [{}]", theme.getName(), theme.getId());
                }
                else
                {
                    WPManager.log.error("Failed to disable the theme '{}' [{}]", theme.getName(), theme.getId());
                }
            }
        }
    }
}
