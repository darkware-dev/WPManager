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

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.darkware.wpman.actions.WPAction;
import org.darkware.wpman.actions.WPActionService;
import org.darkware.wpman.actions.WPCronHookExec;
import org.darkware.wpman.actions.WPPluginUpdate;
import org.darkware.wpman.actions.WPThemeUpdate;
import org.darkware.wpman.cron.WPCronAgent;
import org.darkware.wpman.cron.WPLowLatencyCronAgent;
import org.darkware.wpman.data.Version;
import org.darkware.wpman.data.WPCronHook;
import org.darkware.wpman.data.WPData;
import org.darkware.wpman.data.WPPlugin;
import org.darkware.wpman.data.WPSite;
import org.darkware.wpman.data.WPSiteTheme;
import org.darkware.wpman.data.WPTheme;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

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
    private final Path configPath;

    private final Config config;
    private final WPData data;
    private final WPCLIFactory builder;
    private final WPActionService actionService;
    private WPCronAgent cron;
    private final WPDataManager dataManager;

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

        this.configPath = configPath;

        this.context = ContextManager.local();

        context.registerInstance(this);

        this.config = new Config();
        context.registerInstance(this.config);
        context.registerInstance(this.config.getBuilder());

        this.data = new WPData();
        context.registerInstance(this.data);

        this.dataManager = new WPDataManager();
        context.registerInstance(this.dataManager);

        this.builder = this.config.getBuilder();
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

    @Override
    public void run()
    {
        WPManager.log.info("Starting up.");

        WPManager.log.info("Attaching context.");
        ContextManager.attach(this.context);

        this.loadConfig(this.configPath);

        this.checkWPCLI();

        WPData report = new WPData();
        report.refresh();

        WPManager.log.info("Starting action execution service.");

        if (report.getCore().hasUpdate())
        {
            WPManager.log.info("Update available for core: {}", report.getCore().getUpdateVersion());
        }

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

    /**
     * Check the configured WP-CLI tool to ensure that it exists and is ready for execution. This will
     * also check to see if there is a new version of the tool. If available and not disabled by
     * configuration, it will be automatically updated.
     */
    protected void checkWPCLI()
    {
        if (!Files.exists(this.config.getWPCLIBinary())) this.updateWPCLI();
        else
        {
            WPCLI checkUpdate = this.config.getBuilder().build("cli", "check-update");
            Version wpcliUpdate = checkUpdate.readJSON(Version.class);
            if (wpcliUpdate != null)
            {
                WPManager.log.info("Updated WP-CLI available: {}", wpcliUpdate);
                this.updateWPCLI();
            }
        }
    }

    /**
     * Update the local WP-CLI tool to the most recent version.
     */
    public void updateWPCLI()
    {
        try
        {
            WPManager.log.info("Downloading new version of WP-CLI.");

            CloseableHttpClient httpclient = HttpClients.createDefault();

            URI pharURI = new URIBuilder().setScheme("http")
                                          .setHost("raw.githubusercontent.com")
                                          .setPath("/wp-cli/builds/gh-pages/phar/wp-cli.phar").build();

            WPManager.log.info("Downloading from: {}", pharURI);
            HttpGet downloadRequest = new HttpGet(pharURI);

            CloseableHttpResponse response = httpclient.execute(downloadRequest);

            WPManager.log.info("Download response: {}", response.getStatusLine());
            WPManager.log.info("Download content type: {}", response.getFirstHeader("Content-Type").getValue());

            FileChannel wpcliFile = FileChannel.open(this.config.getWPCLIBinary(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

            response.getEntity().writeTo(Channels.newOutputStream(wpcliFile));
            wpcliFile.close();

            Set<PosixFilePermission> wpcliPerms = new HashSet<>();
            wpcliPerms.add(PosixFilePermission.OWNER_READ);
            wpcliPerms.add(PosixFilePermission.OWNER_WRITE);
            wpcliPerms.add(PosixFilePermission.OWNER_EXECUTE);
            wpcliPerms.add(PosixFilePermission.GROUP_READ);
            wpcliPerms.add(PosixFilePermission.GROUP_EXECUTE);

            Files.setPosixFilePermissions(this.config.getWPCLIBinary(), wpcliPerms);
        }
        catch (URISyntaxException e)
        {
            WPManager.log.error("Failure building URL for WPCLI download.", e);
            System.exit(1);
        }
        catch (IOException e)
        {
            WPManager.log.error("Error while downloading WPCLI client.", e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
