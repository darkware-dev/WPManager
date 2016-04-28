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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.darkware.wpman.util.serialization.PermissiveBooleanModule;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jeff
 * @since 2016-03-28
 */
public abstract class UpdatableCollectionConfig<T extends UpdatableConfig>
{
    protected String collectionType;
    protected WordpressConfig wpConfig;

    private Path autoInstallList;
    private Path ignoreList;
    private Path baseDir;
    private Path gutterDir;
    private boolean removeUnknown = false;
    private Map<String, T> items = new HashMap<>();

    @JsonIgnore
    public String getCollectionType()
    {
        return collectionType;
    }

    @JsonIgnore
    public void setCollectionType(final String collectionType)
    {
        this.collectionType = collectionType;
    }

    @JsonIgnore
    public WordpressConfig getWpConfig()
    {
        return wpConfig;
    }

    @JsonIgnore
    public void setWpConfig(final WordpressConfig wpConfig)
    {
        this.wpConfig = wpConfig;
    }

    @JsonProperty("autoInstallList")
    public Path getAutoInstallList()
    {
        return autoInstallList;
    }

    @JsonProperty("autoInstallList")
    public void setAutoInstallList(final Path autoInstallList)
    {
        this.autoInstallList = autoInstallList;
    }

    /**
     * Check if unknown items should be removed from the instance.
     *
     * @return {@code true} if items that aren't in the list should be removed, {@code false} if they should
     * simply be ignored.
     */
    @JsonProperty("removeUnknown")
    @JsonDeserialize(using = PermissiveBooleanModule.PermissiveBooleanDeserializer.class)
    public boolean getRemoveUnknown()
    {
        return this.removeUnknown;
    }

    /**
     * Sets the behavior for handling items that aren't found in the list of known items.
     *
     * @param removeUnknown {@code true} if items that aren't in the list should be removed, {@code false} if
     * they should simply be ignored.
     */
    protected void setRemoveUnknown(final boolean removeUnknown)
    {
        this.removeUnknown = removeUnknown;
    }

    @JsonProperty("ignoreList")
    public Path getIgnoreList()
    {
        return ignoreList;
    }

    @JsonProperty("ignoreList")
    public void setIgnoreList(final Path ignoreList)
    {
        this.ignoreList = ignoreList;
    }

    @JsonProperty("dir")
    public Path getBaseDir()
    {
        if (this.gutterDir == null) this.gutterDir = this.wpConfig.getContentDir().resolve(this.collectionType);
        return baseDir;
    }

    @JsonProperty("dir")
    public void setBaseDir(final Path baseDir)
    {
        this.baseDir = (baseDir.isAbsolute()) ? baseDir : this.wpConfig.getContentDir().resolve(baseDir);
    }

    @JsonProperty("gutterDir")
    public Path getGutterDir()
    {
        if (this.gutterDir == null) this.gutterDir = this.wpConfig.getContentDir().resolve(this.collectionType + ".gutter");
        return gutterDir;
    }

    @JsonProperty("gutterDir")
    public void setGutterDir(final Path gutterDir)
    {
        this.gutterDir = (gutterDir.isAbsolute()) ? gutterDir : this.wpConfig.getContentDir().resolve(gutterDir);
    }

    @JsonProperty("details")
    public Map<String, T> getItems()
    {
        return items;
    }

    @JsonProperty("details")
    public void setItems(final Map<String, T> items)
    {
        this.items = items;
    }

    public T getDetails(final String itemId)
    {
        if (!this.getItems().containsKey(itemId)) this.getItems().put(itemId, this.defaultDetails(itemId));
        return this.getItems().get(itemId);
    }

    protected abstract T defaultDetails(final String itemId);
}
