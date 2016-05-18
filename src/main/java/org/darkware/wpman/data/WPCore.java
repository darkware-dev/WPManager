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

package org.darkware.wpman.data;

import com.google.common.eventbus.Subscribe;
import com.google.common.reflect.TypeToken;
import org.darkware.lazylib.LazyLoaded;
import org.darkware.wpman.events.WPCoreUpdateEvent;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIFieldsOption;
import org.darkware.wpman.wpcli.WPCLIOption;

import java.time.Duration;
import java.util.List;

/**
 * The {@code WPCore} class models interactions with the core WordPress software installed for a
 * given instance.
 *
 * @author jeff
 * @since 2016-01-23
 */
public class WPCore extends WPComponent
{
    private LazyLoaded<Version> version;
    private LazyLoaded<Version> updateVersion;
    private LazyLoaded<WPLanguage> language;

    public WPCore()
    {
        super();

        this.version = new LazyLoaded<Version>()
        {
            @Override
            protected Version loadValue() throws Exception
            {
                return WPCore.this.loadVersion();
            }
        };

        this.updateVersion = new LazyLoaded<Version>(Duration.ofHours(4))
        {
            @Override
            protected Version loadValue() throws Exception
            {
                return WPCore.this.loadUpdateVersion();
            }
        };

        this.language = new LazyLoaded<WPLanguage>()
        {
            @Override
            protected WPLanguage loadValue() throws Exception
            {
                WPCLI languageCmd = WPCore.this.buildCommand("core", "language", "list");
                languageCmd.loadPlugins(false);
                languageCmd.loadThemes(false);
                languageCmd.setOption(new WPCLIFieldsOption("language", "native_name"));
                languageCmd.setOption(new WPCLIOption<>("status", "active"));

                List<WPLanguage> languages = languageCmd.readJSON(new TypeToken<List<WPLanguage>>() {});

                return languages.get(0);
            }
        };

        this.getManager().registerForEvents(this);
    }

    protected Version loadVersion()
    {
        WPCLI versionCmd = this.buildCommand("core", "version");
        return new Version(versionCmd.readValue());
    }

    protected Version loadUpdateVersion()
    {
        WPInstance.log.debug("Checking for core update.");

        WPCLI updateCmd = this.buildCommand("core", "check-update");
        updateCmd.loadPlugins(false);
        updateCmd.loadThemes(false);

        List<List<String>> updateData = updateCmd.readCSV();
        if (updateData.size() < 2) return this.getCoreVersion();

        return new Version(updateData.get(1).get(0));
    }

    public Version getCoreVersion()
    {
        return this.version.value();
    }

    public Version getUpdateVersion()
    {
        return this.updateVersion.value();
    }

    public boolean hasUpdate()
    {
        return this.getUpdateVersion().greaterThan(this.getCoreVersion());
    }

    public WPLanguage getLanguage()
    {
        return language.value();
    }

    /**
     * This method is invoked when the core software is updated. Normally, this is done naturally
     * through the event queue.
     *
     * @param event The update event.
     */
    @Subscribe
    public void onCoreUpdate(final WPCoreUpdateEvent event)
    {
        this.version.expire();
        this.updateVersion.expire();
    }
}
