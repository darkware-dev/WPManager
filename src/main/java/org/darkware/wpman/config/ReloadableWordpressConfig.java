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
import org.darkware.wpman.WPManager;
import org.darkware.wpman.data.WPUpdatableType;
import org.darkware.wpman.util.TimeWindow;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * This is a simple delegating implementation of the {@link WordpressConfig} interface that allows for
 * quick, low-cost reloading of configuration data. This is accomplished by supplying references to
 * an instance of this class to various other objects. When the configuration is reloaded, a new
 * concrete instance is parsed and the internal delegate is replaced. All objects using a reference to
 * this object will immediately see the new configuration.
 *
 * @author jeff
 * @since 2016-05-03
 */
public class ReloadableWordpressConfig implements WordpressConfig
{
    private final ObjectMapper mapper;
    private final Path policyFile;
    private WordpressConfig data;

    /**
     * Create a new {@link WordpressConfig} which can be reloaded cleanly.
     *
     * @param policyFile The global policy file to load and reload data from.
     * @param mapper An {@link ObjectMapper} configured to read YAML configuration data.
     */
    public ReloadableWordpressConfig(final Path policyFile, final ObjectMapper mapper)
    {
        super();

        this.mapper = mapper;
        this.policyFile = policyFile;

        this.reload();
    }

    public void reload()
    {
        try
        {
            WordpressConfigData newData = this.mapper.readValue(this.policyFile.toFile(), WordpressConfigData.class);

            if (newData != null)
            {
                this.data = newData;
            }
        }
        catch (IOException e)
        {
            WPManager.log.error("Failed to load policy configuration: {}", this.policyFile);
        }
    }

    @Override
    @JsonProperty("root")
    @Valid
    public Path getBasePath()
    {
        return data.getBasePath();
    }

    @Override
    @JsonProperty("defaultHost")
    public String getDefaultHost()
    {
        return data.getDefaultHost();
    }

    @Override
    @JsonProperty("policyRoot")
    public Path getPolicyRoot()
    {
        return data.getPolicyRoot();
    }

    @Override
    @JsonProperty("plugins")
    public PluginListConfig getPluginListConfig()
    {
        return data.getPluginListConfig();
    }

    @Override
    @JsonProperty("themes")
    public ThemeListConfig getThemeListConfig()
    {
        return data.getThemeListConfig();
    }

    @Override
    @JsonProperty("uploads")
    public UploadsConfig getUploadsConfig()
    {
        return data.getUploadsConfig();
    }

    @Override
    @JsonProperty("contentDir")
    public Path getContentDir()
    {
        return data.getContentDir();
    }

    @Override
    @JsonProperty("uploadDir")
    public Path getUploadDir()
    {
        return data.getUploadDir();
    }

    @Override
    @JsonProperty("permissions")
    public FilePermissionsConfig getPermissionsConfig()
    {
        return data.getPermissionsConfig();
    }

    @Override
    @JsonProperty("dataFiles")
    public Map<String, Path> getDataFiles()
    {
        return data.getDataFiles();
    }

    @Override
    @JsonProperty("notification")
    public NotificationConfig getNotification()
    {
        return data.getNotification();
    }

    @Override
    public TimeWindow getCoreUpdateWindow()
    {
        return data.getCoreUpdateWindow();
    }

    @Override
    @JsonIgnore
    public UpdatableCollectionConfig getUpdatableCollection(final WPUpdatableType componentType)
    {
        return data.getUpdatableCollection(componentType);
    }

    @Override
    @JsonIgnore
    public Path getDataFile(final String id)
    {
        return data.getDataFile(id);
    }
}
