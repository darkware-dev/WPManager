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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.darkware.wpman.ContextManager;
import org.darkware.wpman.WPManager;
import org.darkware.wpman.data.WPUpdatableType;
import org.darkware.wpman.util.TimeWindow;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is a container and serialization object for WordPress and {@link WPManager} configuration.
 *
 * @author jeff
 * @since 2016-03-28
 */
public class WordpressConfigData implements WordpressConfig
{
    /** The Jackson ObjectMapper from the ContextManager. */
    private final ObjectMapper mapper;

    @NotNull
    private Path basePath;
    private Path contentDir;
    private Path uploadDir;

    @NotNull
    private Path policyRoot;

    @NotEmpty
    private String defaultHost;

    @Valid
    private TimeWindow coreUpdateWindow = new TimeWindow(0, 2);

    @Valid
    private PluginListConfig pluginListConfig = new PluginListConfig();
    @Valid
    private ThemeListConfig themeListConfig = new ThemeListConfig();
    @Valid
    private UploadsConfig uploadsConfig = new UploadsConfig();
    @Valid
    private FilePermissionsConfig permissionsConfig = new FilePermissionsConfig();

    private NotificationConfig notification = new NotificationConfig();
    private Map<String, Path> dataFiles = new HashMap<>();

    /**
     * Create a new core configuration data container.
     */
    public WordpressConfigData()
    {
        super();

        this.mapper = ContextManager.local().getContextualInstance(ObjectMapper.class);
    }

    /**
     * Fetch the internal {@link ObjectMapper} for parsing YAML configuration files.
     *
     * @return An {@code ObjectMapper} which has been configured for configuration reading.
     */
    protected final ObjectMapper getObjectMapper()
    {
        return this.mapper;
    }

    @Override
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
    public void setBasePath(final Path basePath)
    {
        this.basePath = basePath;
    }

    @Override
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

    @Override
    @JsonProperty("policyRoot")
    public Path getPolicyRoot()
    {
        return this.policyRoot;
    }

    /**
     * Sets the root directory for discovering modular policy fragments. Not all policy settings can
     * be stored as modular fragments in files. Setting this value may trigger a reload of supported
     * fragments.
     *
     * @param policyRoot The root directory to use.
     */
    public void setPolicyRoot(final Path policyRoot)
    {
        this.policyRoot = policyRoot;
        //TODO: optionally reload fragments
    }

    @Override
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

    @Override
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

    @Override
    @JsonProperty("uploads")
    public UploadsConfig getUploadsConfig()
    {
        return this.uploadsConfig;
    }
    /**
     * Sets the uploads configuration object for this configuration container.
     *
     * @param uploadsConfig The uploads configuration to use.
     */
    protected void setUploadsConfig(final UploadsConfig uploadsConfig)
    {
        this.uploadsConfig = uploadsConfig;
    }

    @Override
    public FilePermissionsConfig getPermissionsConfig()
    {
        return this.permissionsConfig;
    }

    /**
     * Set the file permissions config for this container.
     *
     * @param permissionsConfig A {@link FilePermissionsConfig} object.
     */
    public void setPermissionsConfig(final FilePermissionsConfig permissionsConfig)
    {
        this.permissionsConfig = permissionsConfig;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    @JsonIgnore
    public UpdatableCollectionConfig getUpdatableCollection(final WPUpdatableType componentType)
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

    @Override
    @JsonIgnore
    public Path getDataFile(final String id)
    {
        return this.getDataFiles().get(id);
    }

    @Override
    public void reload()
    {
        throw new IllegalStateException("Configuration reloading on WordpressDataConfig is not supported.");
    }
}
