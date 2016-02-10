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

package org.darkware.wpman;

import org.darkware.wpman.actions.WPAction;
import org.darkware.wpman.actions.WPActionService;
import org.darkware.wpman.actions.WPCronHookExec;
import org.darkware.wpman.actions.WPPluginUpdate;
import org.darkware.wpman.actions.WPThemeUpdate;
import org.darkware.wpman.agents.WPPluginSync;
import org.darkware.wpman.cron.WPCronAgent;
import org.darkware.wpman.cron.WPLowLatencyCronAgent;
import org.darkware.wpman.data.Version;
import org.darkware.wpman.data.WPCronHook;
import org.darkware.wpman.data.WPData;
import org.darkware.wpman.data.WPPlugin;
import org.darkware.wpman.data.WPSite;
import org.darkware.wpman.data.WPSiteTheme;
import org.darkware.wpman.data.WPTheme;
import org.darkware.wpman.events.WPEvent;
import org.darkware.wpman.events.WPEventManager;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The {@code WPManager} is the central agent of the WPManager package. It represents the central controller
 * for all of the actions against a single instance of WordPress.
 *
 * <p>Upon startup, the manager sets up a {@link ContextManager} and attaches it to the current thread. This
 * context is used to perform dependency resolution through multiple levels of data components and child
 * agents. This will be set for the thread which constructs the manager as well, though any other
 * {@code WPManager} objects created in the same thread will create their own contexts.</p>
 *
 * @author jeff
 * @since 2016-01-27
 */
public class WPManager extends Thread
{
    /**
     * A global logger. This is provided as a public facility for any code that doesn't have
     * a more local facility available.
     */
    public static final Logger log = LoggerFactory.getLogger("WPManager");

    private final ContextManager context;

    private final Config config;
    private final WPData data;
    private final WPCLIFactory builder;
    private final WPActionService actionService;
    private WPCronAgent cron;
    private final WPDataManager dataManager;
    private final ConfigWatcher configWatcher;
    private final WPEventManager eventManager;

    /**
     * Creates a new {@code WPManager} reading configuration from the given path.
     *
     * @param configPath The path to read configuration from.
     */
    public WPManager(final String configPath)
    {
        this(Paths.get(configPath));
    }

    /**
     * Creates a new {@code WPManager} reading configuration from the given path.
     *
     * @param configPath The path to read configuration from.
     */
    public WPManager(final Path configPath)
    {
        super();

        // Create a new context
        this.context = ContextManager.local();
        ContextManager.attach(this.context);

        // Register this manager
        context.registerInstance(this);

        this.config = new Config(configPath);
        context.registerInstance(this.config);

        this.configWatcher = new ConfigWatcher(configPath);
        context.registerInstance(this.configWatcher);

        this.eventManager = new WPEventManager();
        context.registerInstance(this.eventManager);

        this.builder = new WPCLIFactory(this.config);
        context.registerInstance(this.builder);

        this.dataManager = new WPDataManager();
        context.registerInstance(this.dataManager);

        this.data = new WPData();
        context.registerInstance(this.data);

        this.actionService = new WPActionService();
    }

    /**
     * Fetch the active configuration for this manager. This contains both translated configuration
     * values and general facilities for reading the raw configuration.
     *
     * @return The current configuration.
     */
    public Config getConfig()
    {
        return this.config;
    }

    /**
     * Fetch the data collection for the targeted WordPress instance. This is a semi-active data
     * store that may lazy-load or fetch-on-request extra data as needed.
     *
     * @return A {@code WPData} object tied to the WordPress instance described by the active
     * configuration.
     */
    public WPData getData()
    {
        return this.data;
    }

    /**
     * Fetch the attached {@link WPDataManager} for this manager. The data manager acts as a
     * data-access and transformation controller.
     *
     * @return A {@code WPDataManager} object with pre-configured utilities for this manager.
     */
    public WPDataManager getDataManager()
    {
        return this.dataManager;
    }

    /**
     * Load configuration data from the given {@link Path}. This may update or replace the current
     * configuration, which in turn, may trigger other components to reconfigure themselves.
     * Efforts are taken to retain thread-safe behavior, but some configuration modifications do not
     * have defined correct behaviors in the case of reconfiguration during active actions. Whenever
     * possible, configuration loading should be done before major interactions.
     *
     * @param configPath The path to load configuration from.
     */
    public void loadConfig(final Path configPath)
    {
        this.config.load(configPath);
    }

    /**
     * Dispatches an event for any objects that have subscribed to events of that type.
     *
     * @param event The event to dispatch.
     */
    public void dispatchEvent(final WPEvent event)
    {
        this.eventManager.dispatch(event);
    }

    /**
     * Subscribes the given {@code Object} for any events it is annotated to accept.
     *
     * @param subscriber The object to subscribe.
     * @see WPEventManager#register(Object)
     */
    public void registerForEvents(final Object subscriber)
    {
        this.eventManager.register(subscriber);
    }

    @Override
    public void run()
    {
        WPManager.log.info("Starting up.");

        WPManager.log.info("Attaching context.");
        ContextManager.attach(this.context);

        Version wpcliUpdate = WPCLI.checkForUpdate();
        if (wpcliUpdate != null)
        {
            WPManager.log.info("New version of WP-CLI available.");
            //TODO: Check config to see if wP-CLI updates are allowed
            WPCLI.update();
        }
        WPManager.log.info("WP Root at: {}", this.config.readVariable("wp.root"));

        WPData report = new WPData();
        report.refresh();

        WPManager.log.info("Starting action execution service.");

        if (report.getCore().hasUpdate())
        {
            WPManager.log.info("Update available for core: {}", report.getCore().getUpdateVersion());
        }

        WPManager.log.info("Starting config monitoring service.");
        this.configWatcher.start();

        // Starting up agents
        WPPluginSync pluginSync = new WPPluginSync();
        this.actionService.schedule(pluginSync);

        WPManager.log.info("Starting cron runner.");
        this.cron = new WPLowLatencyCronAgent();
        this.cron.start();

        for (WPSite site : report.getSites())
        {
            WPSiteTheme siteTheme = site.getTheme();
            WPTheme theme = siteTheme.activeTheme();
            WPManager.log.info("Site [{}] is using theme: {}", site.getUrl(), theme.getId());
        }

        for (WPPlugin plugin : report.getPlugins())
        {
            if (plugin.hasUpdate())
            {
                WPManager.log.info("Update available for plugin: {} => {}", plugin.getId(), plugin.getLatestVersion());
                WPPluginUpdate plugUpdate = new WPPluginUpdate(this, plugin);
                this.actionService.scheduleAction(plugUpdate);
            }
        }

        for (WPTheme theme : report.getThemes())
        {
            if (theme.hasUpdate())
            {
                WPManager.log.info("Update available for theme: {} => {}", theme.getId(), theme.getLatestVersion());
                WPThemeUpdate themeUpdate = new WPThemeUpdate(this, theme);
                this.actionService.scheduleAction(themeUpdate);
            }
        }

        for (WPSite site : report.getSites())
        {
            for (WPCronHook hook : site.getCron().getWaitingHooks())
            {
                WPCronHookExec action = new WPCronHookExec(this, site, hook);
                WPManager.log.info("Scheduling cron run for hook: {}::{}", site.getDomain(), hook.getHook());
                this.actionService.scheduleAction(action);
            }
        }
    }

    /**
     * Request shutdown of this manager. The effect may not be instantaneous. No new actions should
     * be initiated, but some ongoing actions may run to completion.
     */
    public void shutdown()
    {
        this.actionService.shutdown();
        WPManager.log.info("WPManager is shutting down.");
    }

    /**
     * Fetch the {@code WPCLIBuilder} helper object for this manager. The builder will be pre-configured
     * for the most recently loaded configuration.
     *
     * @return A pre-configured {@code WPCLIBuilder}.
     */
    public WPCLIFactory getBuilder()
    {
        return this.builder;
    }

    /**
     * Schedule a new action against the associated WordPress instance. The action will be executed at
     * the next available instant.
     *
     * @param action The {@code WPAction} to schedule.
     */
    public void scheduleAction(final WPAction action)
    {
        this.actionService.scheduleAction(action);
    }

}
