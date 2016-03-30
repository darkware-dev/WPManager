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
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.nio.file.Path;
import java.util.Map;

/**
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
    private PluginListConfig pluginListConfig;

    @Valid
    private ThemeListConfig themeListConfig;

    private NotificationConfig notification;

    private Map<String, Path> dataFiles;

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

    @JsonProperty("defaultHost")
    public String getDefaultHost()
    {
        return defaultHost;
    }

    @JsonProperty("defaultHost")
    public void setDefaultHost(final String defaultHost)
    {
        this.defaultHost = defaultHost;
    }

    @JsonProperty("plugins")
    public PluginListConfig getPluginListConfig()
    {
        return pluginListConfig;
    }

    @JsonProperty("plugins")
    public void setPluginListConfig(final PluginListConfig pluginListConfig)
    {
        this.pluginListConfig = pluginListConfig;
        this.pluginListConfig.setWpConfig(this);
        this.pluginListConfig.setCollectionType("plugins");

    }

    @JsonProperty("themes")
    public ThemeListConfig getThemeListConfig()
    {
        return themeListConfig;
    }

    @JsonProperty("themes")
    public void setThemeListConfig(final ThemeListConfig themeListConfig)
    {
        this.themeListConfig = themeListConfig;
        this.themeListConfig.setWpConfig(this);
        this.themeListConfig.setCollectionType("themes");
    }

    @JsonProperty("contentDir")
    public Path getContentDir()
    {
        if (this.contentDir == null) this.contentDir = this.getBasePath().resolve("wp-content");
        return contentDir;
    }

    @JsonProperty("contentDir")
    public void setContentDir(final Path contentDir)
    {
        this.contentDir = (contentDir.isAbsolute()) ? contentDir : this.getBasePath().resolve(contentDir);
    }

    @JsonProperty("uploadDir")
    public Path getUploadDir()
    {
        if (this.uploadDir == null) this.uploadDir = this.getContentDir().resolve("uploads");
        return uploadDir;
    }

    @JsonProperty("uploadDir")
    public void setUploadDir(final Path uploadDir)
    {
        this.uploadDir = (uploadDir.isAbsolute()) ? uploadDir : this.getContentDir().resolve("uploads");
    }

    @JsonProperty("dataFiles")
    public Map<String, Path> getDataFiles()
    {
        return dataFiles;
    }

    @JsonProperty("dataFiles")
    public void setDataFiles(final Map<String, Path> dataFiles)
    {
        this.dataFiles = dataFiles;
    }

    @JsonProperty("notification")
    public NotificationConfig getNotification()
    {
        return notification;
    }

    @JsonProperty("notification")
    public void setNotification(final NotificationConfig notification)
    {
        this.notification = notification;
    }

    // Special Accessors

    @JsonIgnore
    public UpdatableCollectionConfig getUpdateableCollection(final String name)
    {
        if (name.equals("plugin")) return this.getPluginListConfig();
        else if (name.equals("theme")) return this.getThemeListConfig();
        else throw new IllegalArgumentException("Unknown updatable collection: " + name);
    }

    @JsonIgnore
    public Path getDataFile(final String id)
    {
        return this.getDataFiles().get(id);
    }
}
