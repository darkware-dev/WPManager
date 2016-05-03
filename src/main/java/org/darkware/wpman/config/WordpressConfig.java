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

package org.darkware.wpman.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.darkware.wpman.data.WPUpdatableType;
import org.darkware.wpman.util.TimeWindow;

import javax.validation.Valid;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author jeff
 * @since 2016-05-03
 */
public interface WordpressConfig
{
    /**
     * Make a best effort to reload as much of the configuration data as possible. The reload must
     * allow for continuously available configuration data. For example, at no time should an attempt
     * to fetch data be fetching data that was configured in the data files at some point, but is not
     * reflected in the current configuration data. It's acceptable for data to be out-of-date, but
     * non-existent.
     */
    void reload();

    /**
     * Fetch the path to the top level of the WordPress install
     *
     * @return A {@code Path} to the WordPress root directory.
     */
    @JsonProperty("root")
    @Valid
    Path getBasePath();

    /**
     * Fetches the default hostname to use when issuing WP-CLI commands. This will be overridden for
     * most blog-related invocations, but for instance-wide commands WordPress still likes getting a
     * valid, configured host declaration.
     *
     * @return The default hostname to use.
     */
    @JsonProperty("defaultHost")
    String getDefaultHost();

    /**
     * Fetch the root directory for storing modular policy fragments.
     *
     * @return The root directory as a {@code Path}.
     */
    @JsonProperty("policyRoot")
    Path getPolicyRoot();

    /**
     * Fetch the configuration container for plugin configuration.
     *
     * @return A populated {@link PluginListConfig} object.
     */
    @JsonProperty("plugins")
    PluginListConfig getPluginListConfig();

    /**
     * Fetch the configuration container for theme configuration.
     *
     * @return A populated {@link ThemeListConfig} object.
     */
    @JsonProperty("themes")
    ThemeListConfig getThemeListConfig();

    /**
     * Fetch the path to the WordPress content directory. By default this would point to the
     * {@code wp-content} directory under the installation root.
     *
     * @return The path to the WordPress content directory.
     */
    @JsonProperty("contentDir")
    Path getContentDir();

    /**
     * Fetch the path to the directory WordPress uses for uploaded media. This is the root directory for
     * uploads. Uploaded files may be stored under one or multiple levels of subdirectories. This is especially
     * the case for multisite installations.
     *
     * @return A {@code Path} pointing to the root upload directory.
     */
    @JsonProperty("uploadDir")
    Path getUploadDir();

    /**
     * Fetch the dictionary of named data files. These is a generic storage mechanism for data files used
     * by services or agents that don't warrant a dedicated configuration section.
     *
     * @return A {@code Map} of {@code Path} objects, indexed by unique names.
     */
    @JsonProperty("dataFiles")
    Map<String, Path> getDataFiles();

    /**
     * Fetch the notifications configuration container. This declares various configurations for how
     * WPManager attempts to notify humans about the actions it takes.
     *
     * @return The notifications configuration container.
     */
    @JsonProperty("notification")
    NotificationConfig getNotification();

    /**
     * Fetch the {@link TimeWindow} to use for normal WordPress core updates. Core updates are more disruptive
     * than other updates and this configuration setting allows WPManager to do the update at times that are
     * less likely to inconvenience users.
     *
     * @return A {@code TimeWindow} to use, or {@code null} if no window is defined.
     */
    TimeWindow getCoreUpdateWindow();

    /**
     * Fetch the {@link UpdatableCollectionConfig} for the given collection name. Currently, there are only
     * two named collections: {@code plugin} and {@code theme}. This method supports some level of abstraction
     * for code wishing to handle these collections in a generic manner.
     *
     * @param componentType The type of component to fetch the collection for.
     * @return An {@code UpdatableCollection}.
     */
    @JsonIgnore
    UpdatableCollectionConfig getUpdatableCollection(WPUpdatableType componentType);

    /**
     * Fetch the named data file. The files are declared in the set returned by {@link #getDataFiles()}.
     *
     * @param id The name or ID of the data file to fetch.
     * @return A {@code Path} to the data file, or {@code null} if no matching file was found.
     */
    @JsonIgnore
    Path getDataFile(String id);
}
