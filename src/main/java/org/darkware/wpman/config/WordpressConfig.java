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
import com.sun.istack.internal.NotNull;
import org.darkware.wpman.WPManager;
import org.darkware.wpman.data.WPUpdatableType;
import org.darkware.wpman.util.TimeWindow;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is a container and serialization object for WordPress and {@link WPManager} configuration.
 *
 * @author jeff
 * @since 2016-03-28
 */
public class WordpressConfig
{
    @NotNull
    private Path basePath;
    private Path contentDir;
    private Path uploadDir;

    @NotEmpty
    private String defaultHost;

    @Valid
    private TimeWindow coreUpdateWindow = new TimeWindow(0, 2);

    @Valid
    private PluginListConfig pluginListConfig = new PluginListConfig();

    @Valid
    private ThemeListConfig themeListConfig = new ThemeListConfig();

    private NotificationConfig notification = new NotificationConfig();

    private Map<String, Path> dataFiles = new HashMap<>();

    /**
     * Fetch the path to the top level of the WordPress install
     *
     * @return A {@code Path} to the WordPress root directory.
     */
    @JsonProperty("root")
    @Valid
    public Path getBasePath()
    {
        return this.basePath;
    }

    /**
     * Sets the path to the WordPress root directory.
     *
     * @param basePath The path to the WordPress root directory.
     */
    @JsonProperty("root")
    public void setBasePath(final Path basePath)
    {
        this.basePath = basePath;
    }

    /**
     * Fetches the default hostname to use when issuing WP-CLI commands. This will be overridden for
     * most blog-related invocations, but for instance-wide commands WordPress still likes getting a
     * valid, configured host declaration.
     *
     * @return The default hostname to use.
     */
    @JsonProperty("defaultHost")
    public String getDefaultHost()
    {
        return this.defaultHost;
    }

    /**
     * Sets the default hostname to use for WP-CLI requests. This should be a valid hostname recognized
     * by the WordPress installation.
     *
     * @param defaultHost A valid WordPress hostname
     */
    @JsonProperty("defaultHost")
    public void setDefaultHost(final String defaultHost)
    {
        this.defaultHost = defaultHost;
    }

    /**
     * Fetch the configuration container for plugin configuration.
     *
     * @return A populated {@link PluginListConfig} object.
     */
    @JsonProperty("plugins")
    public PluginListConfig getPluginListConfig()
    {
        return this.pluginListConfig;
    }

    /**
     * Sets the plugin configuration object for this configuration container.
     *
     * @param pluginListConfig The plugin configuration to use.
     */
    @JsonProperty("plugins")
    protected void setPluginListConfig(final PluginListConfig pluginListConfig)
    {
        this.pluginListConfig = pluginListConfig;
        this.pluginListConfig.setWpConfig(this);
    }

    /**
     * Fetch the configuration container for theme configuration.
     *
     * @return A populated {@link ThemeListConfig} object.
     */
    @JsonProperty("themes")
    public ThemeListConfig getThemeListConfig()
    {
        return this.themeListConfig;
    }

    /**
     * Sets the theme configuration object for this configuration container.
     *
     * @param themeListConfig The theme configuration to use.
     */
    @JsonProperty("themes")
    protected void setThemeListConfig(final ThemeListConfig themeListConfig)
    {
        this.themeListConfig = themeListConfig;
        this.themeListConfig.setWpConfig(this);
    }

    /**
     * Fetch the path to the WordPress content directory. By default this would point to the
     * {@code wp-content} directory under the installation root.
     *
     * @return The path to the WordPress content directory.
     */
    @JsonProperty("contentDir")
    public Path getContentDir()
    {
        if (this.contentDir == null) this.contentDir = this.getBasePath().resolve("wp-content");
        return this.contentDir;
    }

    /**
     * Set the path to the WordPress content directory.
     *
     * @param contentDir The path to the content directory.
     */
    @JsonProperty("contentDir")
    public void setContentDir(final Path contentDir)
    {
        this.contentDir = (contentDir.isAbsolute()) ? contentDir : this.getBasePath().resolve(contentDir);
    }

    /**
     * Fetch the path to the directory WordPress uses for uploaded media. This is the root directory for
     * uploads. Uploaded files may be stored under one or multiple levels of subdirectories. This is especially
     * the case for multisite installations.
     *
     * @return A {@code Path} pointing to the root upload directory.
     */
    @JsonProperty("uploadDir")
    public Path getUploadDir()
    {
        if (this.uploadDir == null) this.uploadDir = this.getContentDir().resolve("uploads");
        return this.uploadDir;
    }

    /**
     * Sets the base upload directory used the WordPress media library.
     *
     * @param uploadDir The root directory for all blogs' uploaded files.
     */
    @JsonProperty("uploadDir")
    public void setUploadDir(final Path uploadDir)
    {
        this.uploadDir = (uploadDir.isAbsolute()) ? uploadDir : this.getContentDir().resolve("uploads");
    }

    /**
     * Fetch the dictionary of named data files. These is a generic storage mechanism for data files used
     * by services or agents that don't warrant a dedicated configuration section.
     *
     * @return A {@code Map} of {@code Path} objects, indexed by unique names.
     */
    @JsonProperty("dataFiles")
    public Map<String, Path> getDataFiles()
    {
        return this.dataFiles;
    }

    /**
     * Set or replace the {@code Map} of extra data files.
     *
     * @param dataFiles A {@code Map} of {@code Path} objects, indexed by unique names.
     */
    @JsonProperty("dataFiles")
    public void setDataFiles(final Map<String, Path> dataFiles)
    {
        this.dataFiles = dataFiles;
    }

    /**
     * Fetch the notifications configuration container. This declares various configurations for how
     * WPManager attempts to notify humans about the actions it takes.
     *
     * @return The notifications configuration container.
     */
    @JsonProperty("notification")
    public NotificationConfig getNotification()
    {
        return this.notification;
    }

    /**
     * Sets the notifications configuration container.
     *
     * @param notification The notifications configuration container to use.
     */
    @JsonProperty("notification")
    protected void setNotification(final NotificationConfig notification)
    {
        this.notification = notification;
    }

    /**
     * Fetch the {@link TimeWindow} to use for normal WordPress core updates. Core updates are more disruptive
     * than other updates and this configuration setting allows WPManager to do the update at times that are
     * less likely to inconvenience users.
     *
     * @return A {@code TimeWindow} to use, or {@code null} if no window is defined.
     */
    public TimeWindow getCoreUpdateWindow()
    {
        return this.coreUpdateWindow;
    }

    /**
     * Declares the {@code TimeWindow} to use for WordPress core updates. Setting this value to {@code null}
     * will allow updates to occur at any time.
     *
     * @param coreUpdateWindow The {@code TimeWindow} to use, or {@code null} if no window is desired.
     */
    public void setCoreUpdateWindow(final TimeWindow coreUpdateWindow)
    {
        this.coreUpdateWindow = coreUpdateWindow;
    }

    // Special Accessors

    /**
     * Fetch the {@link UpdatableCollectionConfig} for the given collection name. Currently, there are only
     * two named collections: {@code plugin} and {@code theme}. This method supports some level of abstraction
     * for code wishing to handle these collections in a generic manner.
     *
     * @param componentType The type of component to fetch the collection for.
     * @return An {@code UpdatableCollection}.
     */
    @JsonIgnore
    //TODO: This is apparently misspelled.
    public UpdatableCollectionConfig getUpdateableCollection(final WPUpdatableType componentType)
    {
        switch (componentType)
        {
            case PLUGIN:
                return this.getPluginListConfig();
            case THEME:
                return this.getThemeListConfig();
            default:
                throw new IllegalArgumentException("Unknown updatable collection: " + componentType);
        }
    }

    /**
     * Fetch the named data file. The files are declared in the set returned by {@link #getDataFiles()}.
     *
     * @param id The name or ID of the data file to fetch.
     * @return A {@code Path} to the data file, or {@code null} if no matching file was found.
     */
    @JsonIgnore
    public Path getDataFile(final String id)
    {
        return this.getDataFiles().get(id);
    }
}
