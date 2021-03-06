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
import org.darkware.wpman.data.WPUpdatableType;
import org.darkware.wpman.util.serialization.PermissiveBooleanModule;

import java.nio.file.Path;

/**
 * This is a base configuration implementation for any updatable list of objects. For now (and
 * the foreseeable future) this is just themes and plugins. The concrete implementations of this
 * are the {@link PluginListConfig} and {@link ThemeListConfig} classes. Common configuration
 * facilities for these updatable lists include some directory fields, an external, simplified list
 * of items to sync the installed list against, another list of items that should be excluded from
 * list syncs or other management actions, and some extra configuration for the handling of those lists.
 * <p>
 * This base implementation includes support for a generic list of {@link UpdatableConfig} objects
 * with lazy-creation of default config objects on attempt to access a non-existent configuration
 * object.
 *
 * @author jeff
 * @since 2016-03-28
 */
public abstract class UpdatableCollectionConfig<T extends UpdatableConfig> extends CollectionConfig<T>
{
    private final WPUpdatableType collectionType;
    private Path baseDir;
    private Path gutterDir;
    private boolean removeUnknown;

    /**
     * Creates a new collection of {@link UpdatableConfig} objects.
     *
     * @param collectionType The type of components being configured.
     */
    public UpdatableCollectionConfig(final WPUpdatableType collectionType)
    {
        super();

        this.collectionType = collectionType;
        this.removeUnknown = true;
    }

    /**
     * Fetch the human-readable type token for this collection. Currently supported map are
     * either "themes" or "plugins".
     *
     * @return The type as a simple {@code String}.
     */
    @JsonIgnore
    public WPUpdatableType getCollectionType()
    {
        return this.collectionType;
    }

    /**
     * Fetch the default subdirectory name for configuration fragments.
     *
     * @return The subdirectory name as a simple {@code String}.
     */
    @Override
    @JsonIgnore
    protected String getDefaultPolicySubdirectory()
    {
        return this.getCollectionType().getPlural();
    }

    /**
     * Check if unknown items should be removed from the instance.
     *
     * @return {@code true} if items that aren't in the list should be removed, {@code false} if they should
     * simply be ignored.
     */
    @JsonProperty("removeUnknown")
    @JsonDeserialize(using = PermissiveBooleanModule.PermissiveBooleanDeserializer.class)
    public final boolean getRemoveUnknown()
    {
        return this.removeUnknown;
    }

    /**
     * Sets the behavior for handling items that aren't found in the list of known items.
     *
     * @param removeUnknown {@code true} if items that aren't in the list should be removed, {@code false} if
     * they should simply be ignored.
     */
    protected final void setRemoveUnknown(final boolean removeUnknown)
    {
        this.removeUnknown = removeUnknown;
    }

    /**
     * Fetch the directory where the configured components are installed to.
     *
     * @return The directory as an absolute {@link Path}.
     */
    @JsonProperty("dir")
    public final Path getBaseDir()
    {
        if (this.baseDir == null) this.baseDir = this.getWpConfig().getContentDir().resolve(this.collectionType.getPlural());
        return this.baseDir;
    }

    /**
     * Set the directory where the configured components are installed to.
     *
     * @param baseDir The directory as a {@link Path}, or {@code null} if the default path should be used.
     */
    @JsonProperty("dir")
    protected final void setBaseDir(final Path baseDir)
    {
        if (baseDir == null) this.baseDir = null;
        else this.baseDir = (baseDir.isAbsolute()) ? baseDir : this.getWpConfig().getContentDir().resolve(baseDir);
    }

    /**
     * Fetch the directory where old or problematic installs are moved to while performing updates or re-installs.
     *
     * @return The gutter directory as a {@link Path}.
     */
    @JsonProperty("gutterDir")
    public final Path getGutterDir()
    {
        if (this.gutterDir == null) this.gutterDir = this.getWpConfig().getContentDir().resolve(this.collectionType.getPlural() + ".gutter");
        return this.gutterDir;
    }

    /**
     * Set the gutter directory for this set of components.
     *
     * @param gutterDir The gutter directory as a {@link Path}.
     * @see #getGutterDir()
     */
    @JsonProperty("gutterDir")
    public final void setGutterDir(final Path gutterDir)
    {
        if (gutterDir == null) this.gutterDir = null;
        else this.gutterDir = (gutterDir.isAbsolute()) ? gutterDir : this.getWpConfig().getContentDir().resolve(gutterDir);
    }
}
