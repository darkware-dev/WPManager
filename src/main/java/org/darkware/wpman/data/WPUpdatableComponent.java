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

package org.darkware.wpman.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.darkware.lazylib.LazyLoaded;
import org.darkware.wpman.config.UpdatableCollectionConfig;
import org.darkware.wpman.config.UpdatableConfig;

import java.time.Duration;

/**
 * This is a base implementation of a single updatable WordPress component. All updatable
 * components share some similar behavior, particularly with respect to the handling of
 * updates and the metadata used to support them (<em>eg:</em> versions).
 *
 * @author jeff
 * @since 2016-01-25
 */
public abstract class WPUpdatableComponent<C extends UpdatableConfig> extends WPComponent
{
    @JsonProperty("name")
    private String id;
    @JsonProperty("version")
    private Version version;
    @JsonProperty("update_version")
    private Version latestVersion;

    @JsonIgnore
    private final WPUpdatableType componentType;
    @JsonIgnore
    private final LazyLoaded<C> config;

    /**
     * Create a new base instance of an updatable component.
     *
     * @param componentType The type of component in the concrete implementation
     */
    public WPUpdatableComponent(final WPUpdatableType componentType)
    {
        super();

        this.componentType = componentType;
        this.config = new LazyLoaded<C>(Duration.ofMinutes(2))
        {
            @Override
            protected C loadValue() throws Exception
            {
                return WPUpdatableComponent.this.getConfig();
            }
        };
    }

    /**
     * Fetch the active configuration for this component.
     *
     * @return The component configuration section for this component.
     */
    protected C getConfig()
    {
        @SuppressWarnings("unchecked")
        UpdatableCollectionConfig<C> collectionConfig = (UpdatableCollectionConfig<C>)this.getManager().getConfig().getUpdatableCollection(this.componentType);
        return collectionConfig.getConfig(this.getId());
    }

    /**
     * Fetch the unique identifier for this component. In most cases, this is the component's "slug".
     *
     * @return An identifier unique to components of this type.
     */
    public final String getId()
    {
        return this.id;
    }

    /**
     * Set the unique identifier for this component.
     *
     * @param id The {@code String} to set as the id.
     */
    public final void setId(final String id)
    {
        this.id = id;
    }

    /**
     * Fetch a pleasantly readable name for the item. This is often used for simple, human readable
     * presentation, since the ID strings are not always so obvious.
     *
     * @return A name for the item.
     */
    public abstract String getName();

    /**
     * Fetch the current version of this component.
     *
     * @return The most recently fetched {@link Version} assigned to this component.
     */
    public final Version getVersion()
    {
        return this.version;
    }

    /**
     * Sets the current version assigned to this component.
     *
     * @param version The new version to use.
     */
    public final void setVersion(final Version version)
    {
        this.version = version;
    }

    /**
     * Fetches the most recently noticed version for this component. There is a decent amount of announcement lag
     * involved in the reporting of this version. It requires refreshes of caches in the local store, the WordPress
     * instance, and the remote repository.
     *
     * @return The most recent {@link Version} discovered for this component.
     */
    public final Version getLatestVersion()
    {
        return this.latestVersion;
    }

    /**
     * Sets the highest available version for this component. In general, this should not be used by active code. It
     * is primarily provided as a serialization endpoint.
     *
     * @param latestVersion The most recent {@link Version}.
     */
    protected final void setLatestVersion(final Version latestVersion)
    {
        this.latestVersion = latestVersion;
    }

    /**
     * Checks to see if there is a newer version of this component. This simply checks for the existence of a
     * newer version, without respect to whether configuration supports applying that update. Most code seeking
     * to make decisions on applying updates will want to use {@link #canUpdate()} instead of this method.
     *
     * @return {@code true} if a newer version of the component is available.
     * @see #canUpdate()
     */
    public final boolean hasUpdate()
    {
        return this.latestVersion != null;
    }

    /**
     * Checks to see if this component has an update which can be applied. This relies upon configuration supporting
     * the ability to upload and not restricting the updated version level.
     *
     * @return {@code true} if there is an update available that can be applied according to the current configuration.
     */
    public final boolean canUpdate()
    {
        if (!this.config.value().isUpdatable()) return false;
        if (!this.hasUpdate()) return false;

        return this.version.lessThan(this.getUpdateVersion());
    }

    /**
     * Fetches the highest version which can be updated to.
     * <p>
     * <em>Potential Problems:</em> This may not work exactly the way it was intended. In cases where a version limit
     * is applied via config, if the most recent version exceeds the limit, the limit value will be reported here. This
     * may cause some weird behavior when the limit doesn't correspond to an actual released component version. It will
     * still correctly restrict updates up to that version, but the reporting might be confusing and more sophisticated
     * attempts to set the installed version might fail if they don't account for this behavior.
     *
     * @return The highest {@link Version} target permitted for updates.
     */
    @JsonIgnore
    public final Version getUpdateVersion()
    {
        if (this.hasUpdate())
        {
            if (this.config.value().getMaxVersion() != null &&
                this.latestVersion.greaterThan(this.config.value().getMaxVersion()))
                    return this.config.value().getMaxVersion();
            else return this.latestVersion; 
        }
        else return this.version;
    }

}
