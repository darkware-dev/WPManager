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

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a base implementation for configuration classes that contain configuration for a
 * collection of unique items.
 *
 * @author jeff
 * @since 2016-05-03
 */
public abstract class CollectionConfig<T>
{
    private WordpressConfig wpConfig;
    private Path policyRoot;
    private Map<String, T> items = new HashMap<>();

    /**
     * Create a new empty collection configuration.
     */
    public CollectionConfig()
    {
        super();
    }

    /**
     * Fetch the {@link WordpressConfigData} attached to this configuration. Though it cannot be strictly
     * enforced, it is assumed that this is the same {@code WordpressConfig} instance which contains
     * this config object.
     *
     * @return A {@code WordpressConfig} object.
     * @throws IllegalStateException If the {@code WordpressConfig} hasn't been registered yet.
     * @see #setWpConfig(WordpressConfig)
     */
    @JsonIgnore
    public WordpressConfig getWpConfig()
    {
        if (this.wpConfig == null) throw new IllegalStateException("Attempted to use internal WordpressConfig before it was set.");
        return this.wpConfig;
    }

    /**
     * Registers an existing {@link WordpressConfigData} with this configuration. This is a required action
     * before performing some of the more advanced functions of the configuration. It is assumed that the
     * supplied {@code WordpressConfig} is the same configuration container which this configuration object
     * belongs to.
     *
     * @param wpConfig The {@code WordpressConfig} which owns this configuration.
     */
    @JsonIgnore
    public void setWpConfig(final WordpressConfig wpConfig)
    {
        this.wpConfig = wpConfig;
    }

    /**
     * Fetch the directory to scan for modular config fragments. If no path is explicitly configured,
     * a default configuration is used.
     *
     * @return A {@link Path} to the directory to scan for config fragments.
     */
    @JsonProperty("policyDir")
    public Path getPolicyRoot()
    {
        if (this.policyRoot == null) return this.getWpConfig().getPolicyRoot().resolve(this.getDefaultPolicySubdirectory());
        else return this.policyRoot;
    }

    /**
     * Set the directory to scan for modular config fragments.
     *
     * @param policyRoot A {@link Path} to the directory.
     */
    public void setPolicyRoot(final Path policyRoot)
    {
        this.policyRoot = policyRoot;
    }

    /**
     * Fetch the default name of the subdirectory to scan for config fragments. This is only used to
     * resolve a default path if no path is explicitly set.
     *
     * @return The subdirectory name as a string.
     */
    @JsonIgnore
    protected abstract String getDefaultPolicySubdirectory();

    /**
     * Fetch the full {@link Map} of items in this configuration. The key of map is the item's
     * unique identifier, as a {@code String}.
     *
     * @return The live {@code Map} of item identifier to the contained items.
     */
    @JsonProperty("items")
    public Map<String, T> getItems()
    {
        return items;
    }

    /**
     * Set the collection of items in this configuration. This will completely override and replace
     * the existing set.
     *
     * @param items A {@link Map} of unique identifiers to the items associated with them.
     */
    protected void setItems(final Map<String, T> items)
    {
        this.items = items;
    }

    /**
     * Fetch the configuration object for a given item.
     *
     * @param itemId The unique identifier for the item to fetch.
     * @return A configuration object identified by the given ID, or {@code null} if no object was found.
     */
    @JsonIgnore
    public T getConfig(final String itemId)
    {
        if (!this.getItems().containsKey(itemId)) this.getItems().put(itemId, this.defaultOverrides(itemId));
        return this.getItems().get(itemId);
    }

    /**
     * Create a default configuration object for the given identifier. This is used to lazily create
     * configuration for objects that have no explicit configuration.
     *
     * @param itemId The identifier of the object to create.
     * @return A non-null configuration object.
     */
    protected abstract T defaultOverrides(final String itemId);
}
