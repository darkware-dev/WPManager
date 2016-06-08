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
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.darkware.wpman.WPManager;
import org.darkware.wpman.data.WPUpdatableType;
import org.darkware.wpman.util.TimeWindow;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
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
    /**
     * Parse the filename of the given path to extract a slug. The slug is defined as all text leading up to
     * the filename extension.
     *
     * @param file The path to extract a slug from.
     * @return The slug, as a {@code String}
     * @throws IllegalSlugException If the extracted portion of the filename is illegal for a slug.
     */
    private static String slugForFile(final Path file)
    {
        String filename = file.getName(file.getNameCount() - 1).toString();
        int extStart = filename.lastIndexOf('.');

        String slug;
        if (extStart == -1) slug = filename;
        else slug = filename.substring(0, extStart);

        // Do some verification
        if (slug.length() < 1) throw new IllegalSlugException(slug);
        if (slug.contains(".") || slug.contains(" ")) throw new IllegalSlugException(slug);

        return slug;
    }

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

    /**
     * Reload the profile data.
     */
    public void reload()
    {
        WPManager.log.debug("Loading profile data: {}", this.policyFile);
        try
        {
            WordpressConfigData newData = this.mapper.readValue(this.policyFile.toFile(), WordpressConfigData.class);

            if (newData != null)
            {
                // Set a smart default for the policy root
                if (newData.getPolicyRoot() == null) newData.setPolicyRoot(this.policyFile.getParent());

                // Set source files on existing plugins and themes
                newData.getPluginListConfig().getItems().values().forEach(p -> p.setPolicyFile(this.policyFile));
                newData.getThemeListConfig().getItems().values().forEach(t -> t.setPolicyFile(this.policyFile));

                // Reload plugin fragments
                Path pluginsDir = this.policyFile.getParent().resolve("plugins");
                this.loadPlugins(newData.getPluginListConfig(), pluginsDir);

                // Reload theme fragments
                Path themesDir = this.policyFile.getParent().resolve("themes");
                this.loadThemes(newData.getThemeListConfig(), themesDir);

                // Apply the new profile
                if (this.data != null) WPManager.log.info("Reloaded configuration.");
                this.data = newData;
            }
        }
        catch (UnrecognizedPropertyException e)
        {
            WPManager.log.error("Unrecognized property in {}: {} at {}", this.policyFile, e.getPropertyName(), e.getLocation());
        }
        catch (IOException e)
        {
            WPManager.log.error("Failed to load policy configuration: {}", this.policyFile, e);
        }
    }

    /**
     * Fetches the file containing the master policy data.
     *
     * @return The {@link Path} to the main policy data file.
     */
    public Path getPolicyFile()
    {
        return this.policyFile;
    }

    /**
     * Load all available plugin configuration profile fragments under the given directory. No recursion is done.
     * All profile fragments must end in {@code .yml}.
     *
     * @param plugins The {@link PluginListConfig} to override with the loaded fragments.
     * @param dir The {@link Path} of the directory to scan.
     * @throws IOException If there is an error while listing the directory contents
     */
    protected void loadPlugins(final PluginListConfig plugins, final Path dir) throws IOException
    {
        if (!Files.exists(dir)) return;
        if (!Files.isDirectory(dir))
        {
            WPManager.log.warn("Cannot load plugin overrides: {} is not a directory.", dir);
            return;
        }
        if (!Files.isReadable(dir))
        {
            WPManager.log.warn("Cannot load plugin overrides: {} is not readable.", dir);
            return;
        }

        Files.list(dir).filter(f -> Files.isRegularFile(f)).filter(f -> f.toString().endsWith(".yml")).forEach(f -> this.loadPlugin(plugins, f));
    }

    /**
     * Load a plugin profile fragment and apply it as an override.
     *
     * @param plugins The {@link PluginListConfig} to load the profile fragment into
     * @param pluginFile The file containing the profile fragment.
     */
    protected void loadPlugin(final PluginListConfig plugins, final Path pluginFile)
    {
        try
        {
            PluginConfig plug;
            if (Files.size(pluginFile) < 3) plug = new PluginConfig();
            else plug = this.mapper.readValue(pluginFile.toFile(), PluginConfig.class);

            // Set the source file
            plug.setPolicyFile(pluginFile);

            String slug = ReloadableWordpressConfig.slugForFile(pluginFile);

            plugins.overrideItem(slug, plug);
            WPManager.log.debug("Loaded configuration for plugin: {}", slug);
        }
        catch (JsonMappingException e)
        {
            WPManager.log.warn("Skipped loading plugin configuration (formatting): {}", pluginFile);
        }
        catch (IllegalSlugException e)
        {
            WPManager.log.warn("Skipped loading plugin configuration (illegal slug): {}", pluginFile, e);
        }
        catch (IOException e)
        {
            WPManager.log.error("Error loading plugin configuration: {}", pluginFile, e);
        }
    }

    /**
     * Load all available plugin configuration profile fragments under the given directory. No recursion is done.
     * All profile fragments must end in {@code .yml}.
     *
     * @param themes The {@link ThemeListConfig} to override with the loaded fragments.
     * @param dir The {@link Path} of the directory to scan.
     * @throws IOException If there is an error while listing the directory contents
     */
    protected void loadThemes(final ThemeListConfig themes, final Path dir) throws IOException
    {
        if (!Files.exists(dir)) return;
        if (!Files.isDirectory(dir))
        {
            WPManager.log.warn("Cannot load theme overrides: {} is not a directory.", dir);
            return;
        }
        if (!Files.isReadable(dir))
        {
            WPManager.log.warn("Cannot load theme overrides: {} is not readable.", dir);
            return;
        }


        Files.list(dir).filter(f -> Files.isRegularFile(f)).filter(f -> f.toString().endsWith(".yml")).forEach(f -> this.loadTheme(themes, f));
    }

    /**
     * Load a theme profile fragment and apply it as an override.
     *
     * @param themes The {@link ThemeListConfig} to load the profile fragment into
     * @param themeFile The file containing the theme fragment.
     */
    protected void loadTheme(final ThemeListConfig themes, final Path themeFile)
    {
        try
        {
            ThemeConfig theme;
            if (Files.size(themeFile) < 3) theme = new ThemeConfig();
            else theme = this.mapper.readValue(themeFile.toFile(), ThemeConfig.class);

            // Set the source file
            theme.setPolicyFile(themeFile);

            String slug = ReloadableWordpressConfig.slugForFile(themeFile);

            themes.overrideItem(slug, theme);
            WPManager.log.debug("Loaded configuration for theme: {}", slug);
        }
        catch (JsonMappingException e)
        {
            WPManager.log.warn("Skipped loading theme configuration (formatting): {}", themeFile);
        }
        catch (IllegalSlugException e)
        {
            WPManager.log.warn("Skipped loading theme configuration (illegal slug): {}", themeFile, e);
        }
        catch (IOException e)
        {
            WPManager.log.error("Error loading theme configuration: {}", themeFile, e);
        }
    }

    @Override
    @JsonProperty("root")
    @Valid
    public Path getBasePath()
    {
        return this.data.getBasePath();
    }

    @Override
    @JsonProperty("defaultHost")
    public String getDefaultHost()
    {
        return this.data.getDefaultHost();
    }

    @Override
    @JsonProperty("policyRoot")
    public Path getPolicyRoot()
    {
        return this.data.getPolicyRoot();
    }

    @Override
    @JsonProperty("plugins")
    public PluginListConfig getPluginListConfig()
    {
        return this.data.getPluginListConfig();
    }

    @Override
    @JsonProperty("themes")
    public ThemeListConfig getThemeListConfig()
    {
        return this.data.getThemeListConfig();
    }

    @Override
    @JsonProperty("uploads")
    public UploadsConfig getUploadsConfig()
    {
        return this.data.getUploadsConfig();
    }

    @Override
    @JsonProperty("contentDir")
    public Path getContentDir()
    {
        return this.data.getContentDir();
    }

    @Override
    @JsonProperty("uploadDir")
    public Path getUploadDir()
    {
        return this.data.getUploadDir();
    }

    @Override
    @JsonProperty("permissions")
    public FilePermissionsConfig getPermissionsConfig()
    {
        return this.data.getPermissionsConfig();
    }

    @Override
    @JsonProperty("dataFiles")
    public Map<String, Path> getDataFiles()
    {
        return this.data.getDataFiles();
    }

    @Override
    @JsonProperty("notification")
    public NotificationConfig getNotification()
    {
        return this.data.getNotification();
    }

    @Override
    public TimeWindow getCoreUpdateWindow()
    {
        return this.data.getCoreUpdateWindow();
    }

    @Override
    @JsonIgnore
    public UpdatableCollectionConfig getUpdatableCollection(final WPUpdatableType componentType)
    {
        return this.data.getUpdatableCollection(componentType);
    }

    @Override
    @JsonIgnore
    public Path getDataFile(final String id)
    {
        return this.data.getDataFile(id);
    }
}
