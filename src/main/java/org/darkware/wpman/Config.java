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

import org.darkware.cltools.utils.ObjectFactory;
import org.darkware.wpman.data.WPPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@code Config} object acts a facade to the raw configuration data which controls how a
 * {@link WPManager} and composed classes should operate. The primary goal is to provide a
 * single reference to be shared which contains the raw data, supplies utility methods for
 * extracting that data, and supports methods of notifying other objects of changes in the
 * configuration.
 *
 * @author jeff
 * @since 2016-01-24
 */
public class Config
{
    /**
     * Build a configuration variable key (ie: name) from a set of parts. Each part is required
     * to be "full". Combining the parts {@code foo} and {@code bar} will result in {@code foo.bar}
     * and not {@code foobar}. However, parts are allowed to be "compound". Combining
     * {@code foo.bar} and {@code baz} is legal and produces the expected result: {@code far.bar.baz}.
     *
     * @param parts The parts to combine.
     * @return A String containing the combined parts formatted as a configuration key.
     */
    public static String buildKey(String ... parts)
    {
        if (parts.length == 1) return parts[0];
        if (parts.length == 0) return null;
        return Arrays.asList(parts).stream().collect(Collectors.joining("."));
    }

    private static Set<String> trueWords = new HashSet<>(Arrays.asList(new String[] { "yes", "true", "1", "allow" }));

    /**
     * Translates a {@code String} into a boolean using a library of expected values. The translation is
     * not case sensitive and whitespace is ignored.
     *
     * @param word The word to translate
     * @return {@code true} if the word matches a recognized "true" word, {@code false} if it does not.
     */
    protected static boolean translateBoolean(String word)
    {
        return Config.trueWords.contains(word.trim().toLowerCase());
    }

    private static final Logger log = LoggerFactory.getLogger("Config");

    private final Path rootPath;
    private final Properties raw;
    private Path WPCLIBinary;

    /* Helper fields */
    private final Set<WeakReference<ConfigListener>> listeners;

    /**
     * Create a new {@code Config} facade. The global configuration will be loaded with derived defaults.
     */
    public Config(final Path rootPath)
    {
        super();

        this.rootPath = rootPath;
        this.listeners = new HashSet<>();
        this.raw = new Properties();

        this.load(this.rootPath);
    }

    /**
     * Fetches the root path of the configuration.
     *
     * @return The top level directory, as a {@code Path}.
     */
    public Path getRootPath()
    {
        return rootPath;
    }

    /**
     * Register an object for notifications when configuration data changes.
     *
     * @param listener The object to notify on changes.
     */
    public void addListener(ConfigListener listener)
    {
        this.listeners.add(new WeakReference<>(listener));
    }

    /**
     * Notify all listeners that configuration data has changed. If you know that several values are
     * going to change, it's more efficient to wait until all changes are complete before calling
     * this method, as some objects may re-initialize themselves when configuration changes.
     */
    protected void notifyChange()
    {
        Iterator<WeakReference<ConfigListener>> listenerIterator = this.listeners.iterator();

        while(listenerIterator.hasNext())
        {
            ConfigListener listener = listenerIterator.next().get();
            if (listener == null) listenerIterator.remove();
            else listener.configChanged();
        }
    }

    //TODO: This can probably be dropped to protected and have its parameter removed.
    /**
     * Load configuration data from the supplied {@code Path}.
     *
     * @param configPath The top-level directory of a management target.
     */
    public void load(Path configPath)
    {
        Config.log.info("Loading configuration: {}", configPath);
        try
        {
            // Config sources
            Path configData = configPath.resolve("config.properties");

            Config.log.info("Reading configuration: {}", configData);
            InputStream propStream = Channels.newInputStream(FileChannel.open(configData, StandardOpenOption.READ));
            this.raw.load(propStream);
            propStream.close();

            this.refreshComposedInfo();
            this.notifyChange();
        }
        catch (IOException e)
        {
            Config.log.error("Failure reading configuration: {}", e.getLocalizedMessage());
        }
    }

    /**
     * Rebuild or update any locally stored values that are based on raw configuration.
     */
    protected void refreshComposedInfo()
    {
        // Common, important directories.
        Path wpRoot = this.readPath("wp.root");
        Path wpContent = this.storePath("wp.content", wpRoot.resolve("wp-content"), true);
        Path wpPlugins = this.storePath("wp.plugin", wpContent.resolve("plugins"), true);
        Path wpThemes = this.storePath("wp.theme", wpContent.resolve("themes"), true);
        Path wpUploads = this.storePath("wp.upload", wpContent.resolve("uploads"), true);
        Path wpGutterPlugins = this.storePath("wp.gutter.plugin", wpContent.resolve("plugins.gutter"), true);
        Path wpGutterThemes = this.storePath("wp.gutter.theme", wpContent.resolve("themes.gutter"), true);
    }

    /**
     * Fetch a configuration value based on the given key parts.
     *
     * @param parts The parts of the configuration key to fetch.
     * @return The value as a {@code String} or {@code null} if the key does not exist.
     */
    //TODO: Deprecate this
    public String getConfigValue(String ... parts)
    {
        if (parts.length < 1) throw new IllegalArgumentException("Configuration key cannot be empty");
        return this.raw.getProperty(Config.buildKey(parts));
    }

    /**
     * Store a default value for a key if no other value exists.
     *
     * @param key The key to store the value for.
     * @param value An {@code Object} value to store. The value will automatically be converted
     * to a String for storage.
     */
    protected void storeDefault(String key, Object value)
    {
        if (!this.exists(key)) this.raw.setProperty(key, value.toString());
    }

    protected Path storePath(String key, Path path, boolean weak)
    {
        if (this.exists(key) && weak) return this.readPath(key);

        this.raw.setProperty(key, path.toString());
        return path;
    }

    protected Path storePath(String key, Path path, Path defaultRoot, boolean weak)
    {
        if (path.isAbsolute()) return this.storePath(key, path, weak);
        else return this.storePath(key, defaultRoot.resolve(path), weak);
    }

    /**
     * Checks to see if a given configuration key has been set.
     *
     * @param key The key to check.
     * @return {@code true} if the key has a value, {@code false} if it does not.
     */
    public boolean exists(String key)
    {
        return this.raw.containsKey(key);
    }

    /**
     * Fetch a configuration value for the given key. If the key does not exist, an
     * exception is thrown.
     *
     * @param key The configuration key to fetch.
     * @return The value as a {@code String}.
     * @throws MissingConfigurationException If the key does not exist.
     */
    public String readVariable(final String key) throws MissingConfigurationException
    {
        String value = this.readVariable(key, (String)null);
        if (value == null) throw new MissingConfigurationException(key);

        return value;
    }

    /**
     * Fetch a configuration value for the given key. If the key does not exist, the
     * supplied default value is returned.
     *
     * @param key The configuration key to fetch.
     * @param defaultValue The default value to use if the key isn't found.
     * @return The value or default as a {@code String}
     */
    public String readVariable(final String key, final String defaultValue)
    {
        if (key == null || key.length() < 1) throw new IllegalConfigurationException("Attempt to fetch configuration with empty variable name.");
        if (!this.raw.containsKey(key)) return defaultValue;
        return this.raw.getProperty(key);
    }

    /**
     * Fetch a configuration value for the given key. If the key does not exist, the
     * supplied default value is returned.
     *
     * @param key The configuration key to fetch.
     * @param defaultValue The default value to use if the key isn't found.
     * @return The value or default as a {@code boolean}
     */
    public boolean readVariable(final String key, final boolean defaultValue)
    {
        String value = this.readVariable(key, (String)null);
        if (value == null) return defaultValue;
        else return Config.translateBoolean(value);
    }

    /**
     * Fetch a configuration value for the given key as a {@code Path}.
     *
     * @param key The configuration key to fetch.
     * @return The value or default as a {@code boolean}
     */
    public Path readPath(final String key)
    {
        return Paths.get(this.readVariable(key));
    }

    /**
     * Fetch a configuration value for the given key. If the key does not exist, the
     * supplied default value is returned.
     *
     * @param key The configuration key to fetch.
     * @param defaultValue The default value to use if the key isn't found.
     * @return The value or default as a {@code boolean}
     */
    public Path readVariable(final String key, final Path defaultValue)
    {
        String value = this.readVariable(key, (String)null);
        if (value == null) return defaultValue;
        else return Paths.get(value);
    }

    /**
     * Fetch a configuration value for the given key. If the key does not exist, the
     * supplied default value is returned.
     *
     * @param key The configuration key to fetch.
     * @param defaultValue The default value to use if the key isn't found.
     * @return The value or default as a {@code boolean}
     */
    public <T> T readVariableObject(final String key, final T defaultValue)
    {
        if (key == null || key.length() < 1) throw new IllegalConfigurationException("Attempt to fetch configuration with empty variable name.");
        if (this.raw.containsKey(key)) return defaultValue;

        if (defaultValue instanceof String) return (T)this.raw.getProperty(key);
        return ObjectFactory.fromString(this.raw.getProperty(key), (Class<T>)defaultValue.getClass());
    }

    /**
     * Fetch a configuration value for a specific plugin. This is a specialized case of
     * {@link #readVariable(String, String)} with built-in translation of the base configuration
     * key for the given plugin. Whenever possible, the plugin-specific methods should be preferred
     * over the generic forms to adapt for any future configuration refactoring.
     *
     * @param plugin The {@code WPPlugin} to fetch configuration for.
     * @param subvar The plugin-specific configuration key fragment to fetch.
     * @param defaultValue The default value to use if the key is not found.
     * @return The value of the appropriate configuration key, or the default.
     */
    public String getPluginConfig(final WPPlugin plugin, final String subvar, String defaultValue)
    {
        String key = Config.buildKey("plugin", plugin.getId(), subvar);

        //TODO: This gets double-checked. It'd be nice to not pay that cost.
        if (this.raw.containsKey(key)) return this.readVariable(key);
        else return defaultValue;
    }

    /**
     * Fetch a configuration value for a specific plugin. This is a specialized case of
     * {@link #readVariable(String, String)} with built-in translation of the base configuration
     * key for the given plugin. Whenever possible, the plugin-specific methods should be preferred
     * over the generic forms to adapt for any future configuration refactoring.
     *
     * @param plugin The {@code WPPlugin} to fetch configuration for.
     * @param subvar The plugin-specific configuration key fragment to fetch.
     * @param defaultValue The default value to use if the key is not found.
     * @return The value of the appropriate configuration key, or the default.
     */
    public int getPluginConfig(final WPPlugin plugin, final String subvar, int defaultValue)
    {
        String key = Config.buildKey("plugin", plugin.getId(), subvar);

        //TODO: This gets double-checked. It'd be nice to not pay that cost.
        if (this.raw.containsKey(key)) return Integer.parseInt(this.readVariable(key));
        else return defaultValue;
    }

    /**
     * Fetch a configuration value for a specific plugin. This is a specialized case of
     * {@link #readVariable(String, String)} with built-in translation of the base configuration
     * key for the given plugin. Whenever possible, the plugin-specific methods should be preferred
     * over the generic forms to adapt for any future configuration refactoring.
     *
     * @param plugin The {@code WPPlugin} to fetch configuration for.
     * @param subvar The plugin-specific configuration key fragment to fetch.
     * @param defaultValue The default value to use if the key is not found.
     * @return The value of the appropriate configuration key, or the default.
     */
    public boolean getPluginConfig(final WPPlugin plugin, final String subvar, boolean defaultValue)
    {
        String key = Config.buildKey("plugin", plugin.getId(), subvar);

        //TODO: This gets double-checked. It'd be nice to not pay that cost.
        if (this.raw.containsKey(key)) return Config.translateBoolean(this.readVariable(key));
        else return defaultValue;
    }

    @Deprecated
    public Path getWPCLIBinary()
    {
        return this.WPCLIBinary;
    }

    @Deprecated
    public void setWPCLIBinary(final Path WPCLIBinary)
    {
        Config.log.debug("Using WP-CLI at: {}", WPCLIBinary);
        this.WPCLIBinary = WPCLIBinary;
    }
}
