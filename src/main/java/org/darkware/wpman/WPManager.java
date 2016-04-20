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
import org.darkware.wpman.agents.WPCoreUpdateAgent;
import org.darkware.wpman.agents.WPCronAgent;
import org.darkware.wpman.agents.WPIntegrityCheckAgent;
import org.darkware.wpman.agents.WPLowLatencyCronAgent;
import org.darkware.wpman.agents.WPNetworkPolicyAgent;
import org.darkware.wpman.agents.WPPluginSync;
import org.darkware.wpman.agents.WPThemeSync;
import org.darkware.wpman.data.Version;
import org.darkware.wpman.data.WPData;
import org.darkware.wpman.events.WPEvent;
import org.darkware.wpman.events.WPEventManager;
import org.darkware.wpman.events.WPStartupEvent;
import org.darkware.wpman.services.PostNotificationService;
import org.darkware.wpman.services.UpdateService;
import org.darkware.wpman.util.TimeWindow;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

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

    private final WPManagerConfiguration config;
    private final WPData data;
    private final WPCLIFactory builder;
    private final WPActionService actionService;
    private final WPCronAgent cron;
    private final WPDataManager dataManager;
    private final WPEventManager eventManager;

    /**
     * Creates a new {@code WPManager} with the given configuration.
     *
     * @param config The configuration for the manager.
     */
    public WPManager(final WPManagerConfiguration config)
    {
        super();

        // Create a new context
        this.context = ContextManager.local();
        ContextManager.attach(this.context);

        // Register this manager
        context.registerInstance(this);

        this.config = config;
        context.registerInstance(this.config);

        this.eventManager = new WPEventManager();
        context.registerInstance(this.eventManager);

        this.builder = new WPCLIFactory(this.config);
        context.registerInstance(this.builder);

        this.dataManager = new WPDataManager();
        context.registerInstance(this.dataManager);

        this.data = new WPData();
        context.registerInstance(this.data);

        this.cron = new WPLowLatencyCronAgent();
        this.actionService = new WPActionService();
    }

    /**
     * Fetch the active configuration for this manager. This contains both translated configuration
     * values and general facilities for reading the raw configuration.
     *
     * @return The current configuration.
     */
    public WPManagerConfiguration getConfig()
    {
        return this.config;
    }

    /**
     * Fetch the current agent responsible for handling cron executions.
     *
     * @return A {@link WPCronAgent} instance.
     */
    public WPCronAgent getCronAgent()
    {
        return this.cron;
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
    protected Object clone() throws CloneNotSupportedException
    {
        return super.clone();
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
        WPManager.log.info("WP Root at: {}", this.config.getWordpress().getBasePath());

        WPData report = new WPData();

        WPManager.log.info("Starting action execution service.");

        if (report.getCore().hasUpdate())
        {
            WPManager.log.info("Update available for core: {}", report.getCore().getUpdateVersion());
        }

        // Initialize services
        PostNotificationService postNotify = new PostNotificationService();
        postNotify.activate();
        UpdateService updateService = new UpdateService();
        updateService.activate();

        // Starting up agents
        WPPluginSync pluginSync = new WPPluginSync();
        this.actionService.schedule(pluginSync);
        WPThemeSync themeSync = new WPThemeSync();
        this.actionService.schedule(themeSync);
        WPCoreUpdateAgent coreUpdater = new WPCoreUpdateAgent();
        this.actionService.schedule(coreUpdater);
        WPIntegrityCheckAgent integrity = new WPIntegrityCheckAgent();
        this.actionService.schedule(integrity);
        WPNetworkPolicyAgent networkPolicy = new WPNetworkPolicyAgent();
        this.actionService.schedule(networkPolicy);

        WPManager.log.info("Starting cron runner.");
        this.cron.startThread();

        this.dispatchEvent(new WPStartupEvent());
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
     * Fetch the {@link WPActionService} used by this manager.
     *
     * @return A {@code WPActionService} which is initialized and attached to this manager.
     */
    public WPActionService getActionService()
    {
        return this.actionService;
    }

    /**
     * Schedule a new action against the associated WordPress instance. The action will be executed at
     * the next available instant.
     *
     * @param action The {@code WPAction} to schedule.
     * @return The {@link Future} of the scheduled action.
     */
    public <T> Future<T> scheduleAction(final WPAction<T> action)
    {
        return this.actionService.scheduleAction(action);
    }

    /**
     * Schedule a new action against the associated WordPress instance. The action will be executed at
     * the next available instant.
     *
     * @param action The {@code WPAction} to schedule.
     * @param seconds The number of seconds to delay execution of the action.
     * @return The {@link ScheduledFuture} of the action.
     */
    public <T> ScheduledFuture<T> scheduleAction(final WPAction<T> action, final long seconds)
    {
        return this.actionService.scheduleAction(action, seconds);
    }

    /**
     * Schedule a new action against the associated WordPress instance. The action will be executed at
     * a random time within the given {@code TimeWindow}.
     *
     * @param action The {@code WPAction} to schedule.
     * @param window The {@code TimeWindow} to execute the action within.
     * @return The {@link ScheduledFuture} of the action.
     */
    public <T> ScheduledFuture<T> scheduleAction(final WPAction<T> action, final TimeWindow window)
    {
        return this.actionService.scheduleAction(action, window);
    }

    /**
     * Fetches the root path of the associate WordPress install.
     *
     * @return A {@code Path} containing the absolute path to the WordPress installation.
     */
    //TODO: Deprecate and remove this method
    public Path getWPRoot()
    {
        return this.config.getWordpress().getBasePath();
    }

    /**
     * Fetch the directory where plugins are stored.
     *
     * @return An absolute {@code Path} to the plugin storage directory.
     */
    //TODO: Deprecate and remove this method
    public Path getPluginDir()
    {
        return this.getWPRoot().resolve("wp-content/plugins");
    }

    /**
     * Fetch the directory where themes are stored.
     *
     * @return An absolute {@code Path} to the plugin storage directory.
     */
    //TODO: Deprecate and remove this method
    public Path getThemeDir()
    {
        return this.getWPRoot().resolve("wp-content/themes");
    }

}
