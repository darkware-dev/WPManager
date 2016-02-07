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

import org.darkware.wpman.data.WPPlugin;
import org.darkware.wpman.wpcli.WPCLIFactory;
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
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author jeff
 * @since 2016-01-24
 */
public class Config
{
    public static String buildKey(String ... parts)
    {
        if (parts.length == 1) return parts[0];
        if (parts.length == 0) return null;
        return Arrays.asList(parts).stream().map(p -> p.toString()).collect(Collectors.joining("."));
    }

    private static Set<String> trueWords = new HashSet<>(Arrays.asList(new String[] { "yes", "true", "1", "allow" }));
    protected static boolean translateBoolean(String word)
    {
        if (Config.trueWords.contains(word.toLowerCase())) return true;
        else return false;
    }

    private static final Logger log = LoggerFactory.getLogger("Config");

    private final Properties raw;

    private Path WPCLIBinary;

    /* Composed Values */
    private Map<Class<?>, Duration> refreshDurations;
    private final WPCLIFactory builder;

    /* Helper fields */

    private final Set<WeakReference<ConfigListener>> listeners;


    public Config()
    {
        super();

        this.listeners = new HashSet<>();
        this.raw = new Properties();

        this.builder = new WPCLIFactory(this);
        this.refreshDurations = new HashMap<>();

    }

    protected void setDefaults()
    {

    }

    public void addListener(ConfigListener listener)
    {
        this.listeners.add(new WeakReference<>(listener));
    }

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

    public void load(Path configPath)
    {
        try
        {
            Config.log.info("Loading configuration: {}", configPath);
            InputStream propStream = Channels.newInputStream(FileChannel.open(configPath, StandardOpenOption.READ));

            this.raw.load(propStream);
            this.refreshComposedInfo();
            this.notifyChange();
        }
        catch (IOException e)
        {
            Config.log.error("Failure reading configuration: ", e);
        }
    }

    protected void refreshComposedInfo()
    {
        this.setWPCLIBinary(Paths.get(this.getConfigValue("wpcli.bin")));
    }

    public String getConfigValue(String ... parts)
    {
        return this.raw.getProperty(Config.buildKey(parts));
    }

    public String readVariable(final String var)
    {
        String value = this.readVariable(var, null);
        if (value == null) throw new MissingConfigurationException(var);

        return value;
    }

    public String readVariable(final String var, String defaultValue)
    {
        if (var == null || var.length() < 1) throw new IllegalConfigurationException("Attempt to fetch configuration with empty variable name.");
        if (!this.raw.contains(var)) return defaultValue;
        return this.raw.getProperty(var);
    }

    public boolean readVariable(final String var, final boolean defaultValue)
    {
        String value = this.readVariable(var, null);
        if (value == null) return defaultValue;
        else return Config.translateBoolean(value);
    }

    public String getPluginConfig(final WPPlugin plugin, final String subvar, String defaultValue)
    {
        String key = Config.buildKey("plugin", plugin.getId(), subvar);

        //TODO: This gets double-checked. It'd be nice to not pay that cost.
        if (this.raw.contains(key)) return this.readVariable(key);
        else return defaultValue;
    }

    public int getPluginConfig(final WPPlugin plugin, final String subvar, int defaultValue)
    {
        String key = Config.buildKey("plugin", plugin.getId(), subvar);

        //TODO: This gets double-checked. It'd be nice to not pay that cost.
        if (this.raw.contains(key)) return Integer.parseInt(this.readVariable(key));
        else return defaultValue;
    }

    public Path getWPCLIBinary()
    {
        return this.WPCLIBinary;
    }

    public void setWPCLIBinary(final Path WPCLIBinary)
    {
        Config.log.debug("Using WP-CLI at: {}", WPCLIBinary);
        this.WPCLIBinary = WPCLIBinary;
    }

    public WPCLIFactory getBuilder()
    {
        return this.builder;
    }
}
