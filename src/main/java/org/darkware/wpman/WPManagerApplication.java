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

import com.google.gson.GsonBuilder;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.darkware.wpman.data.Version;
import org.darkware.wpman.rest.ActionResource;
import org.darkware.wpman.rest.CoreResource;
import org.darkware.wpman.rest.CronResource;
import org.darkware.wpman.rest.PluginResource;
import org.darkware.wpman.rest.SiteResource;
import org.darkware.wpman.rest.ThemeResource;
import org.darkware.wpman.rest.UtilityResource;
import org.darkware.wpman.rest.health.NoopHealthCheck;
import org.darkware.wpman.util.JSONHelper;
import org.darkware.wpman.util.serialization.PathModule;
import org.darkware.wpman.util.serialization.PermissiveBooleanModule;
import org.darkware.wpman.util.serialization.PluginStatusModule;
import org.darkware.wpman.util.serialization.VersionModule;
import org.darkware.wpman.util.serialization.WPActionModule;
import org.darkware.wpman.util.serialization.WPDateModule;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.json.DateTimeSerializer;
import org.darkware.wpman.wpcli.json.VersionSerializer;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;

/**
 * @author jeff
 * @since 2016-03-26
 */
public class WPManagerApplication extends Application<WPManagerConfiguration>
{
    public static Logger log = LoggerFactory.getLogger("Startup");

    public static void main(String[] args) throws Exception
    {
        new WPManagerApplication().run(args);
    }

    @Override
    public String getName() {
        return "WPManager";
    }

    @Override
    public void initialize(Bootstrap<WPManagerConfiguration> bootstrap)
    {
        // Register serialization helpers
        bootstrap.getObjectMapper().registerModule(new PathModule());
        bootstrap.getObjectMapper().registerModule(new VersionModule());
        bootstrap.getObjectMapper().registerModule(new PluginStatusModule());
        bootstrap.getObjectMapper().registerModule(new WPDateModule());
        bootstrap.getObjectMapper().registerModule(new PermissiveBooleanModule());
        bootstrap.getObjectMapper().registerModule(new WPActionModule());

        ContextManager.local().registerInstance(bootstrap.getObjectMapper());

        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeSerializer());
        gsonBuilder.registerTypeAdapter(Version.class, new VersionSerializer());

        //JSONHelper.use(gsonBuilder.create());
        JSONHelper.use(bootstrap.getObjectMapper());
    }

    @Override
    public void run(WPManagerConfiguration configuration, Environment environment)
    {
        WPManagerApplication.log.info("WP-CLI is at: " + configuration.getWpcli().getBinaryPath());
        WPManager manager = new WPManager(configuration);

        environment.jersey().register(new UtilityResource());
        environment.jersey().register(new PluginResource(manager));
        environment.jersey().register(new ThemeResource(manager));
        environment.jersey().register(new SiteResource(manager));
        environment.jersey().register(new CronResource(manager));
        environment.jersey().register(new ActionResource(manager));
        environment.jersey().register(new CoreResource(manager));

        final NoopHealthCheck healthCheck =  new NoopHealthCheck();
        environment.healthChecks().register("noop", healthCheck);

        // Initialize a WPManager
        WPCLI.setPath(configuration.getWpcli().getBinaryPath());
        if (Files.notExists(configuration.getWpcli().getBinaryPath())) WPCLI.update();
        manager.start();
    }
}
