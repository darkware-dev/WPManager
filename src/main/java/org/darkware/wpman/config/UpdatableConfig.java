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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.darkware.wpman.data.Version;

import java.nio.file.Path;

/**
 * @author jeff
 * @since 2016-03-28
 */
public class UpdatableConfig extends ItemConfig
{
    private Boolean updatable;
    private Boolean installable;
    private Version maxVersion;

    /**
     * Create a new item configuration.
     */
    public UpdatableConfig()
    {
        super();

        this.updatable = true;
        this.installable = true;
    }

    /**
     * Create a new item configuration attached to the given fragment file.
     *
     * @param srcFile The {@link Path} to the fragment file which generated this configuration.
     */
    public UpdatableConfig(final Path srcFile)
    {
        super(srcFile);
    }

    /**
     * Check to see if this component should allow updates. This may be disabled in order to lock a given component
     * at a specific version or to prevent checking for updates on a non-public component.
     *
     * @return {@code true} if the component can be updated, {@code false} if it should be excluded from any
     * automated updates.
     */
    @JsonProperty("updatable")
    public Boolean isUpdatable()
    {
        return this.updatable;
    }

    /**
     * Declare the update policy for this component. Components that do not allow updates should not be selected for
     * any automatic updates.
     *
     * @param updatable {@code true} if the component can be updated, {@code false} if it should be excluded from any
     * automated updates.
     */
    @JsonProperty("updatable")
    protected void setUpdatable(final Boolean updatable)
    {
        this.updatable = updatable;
    }

    /**
     * Check to see if this component should be installed if it's not currently installed.
     *
     * @return {@code true} if the component should be installed.
     */
    @JsonProperty("installable")
    public Boolean isInstallable()
    {
        return this.installable;
    }

    /**
     * Declare the install policy for this component. Components that don't allow installations won't be installed if
     * they aren't found on the targeted instance. <em>Note:</em> This doesn't affect the update policy controlled by
     * {@link #isUpdatable()}. If the component is already installed or is installed manually, the update policy will
     * still control whether additional updates are applied.
     *
     * @param updatable {@code true} if the component should be installed if its not found.
     */
    @JsonProperty("installable")
    protected void setInstallable(final Boolean updatable)
    {
        this.updatable = updatable;
    }

    /**
     * Fetch the highest version allowed for installation. This version is compared against update versions using
     * standard WordPress versioning semantics, without the need for the version declared here to actually correspond
     * to a real version of the component. For example, if a currently installed component is at version {@code 2.2.4}
     * and the configuration wishes to prevent moving to the {@code 3.x} branch, setting a maximum version of
     * {@code 2.99.99} will allow any conceivable version in the {@code 2.x} line, without advancing to {@code 3.x}.
     * <p>
     * <em>Note:</em> This may or may not apply to initial installs. If the current best version for a component is
     * already higher than this version, then the current version should be installed. However, no subsequent attempts
     * to update the component will be attempted.
     *
     * @return The highest {@link Version} to allow updates to.
     */
    @JsonProperty("maxVersion")
    public Version getMaxVersion()
    {
        return this.maxVersion;
    }

    /**
     * Set the highest version allowed. This uses an inclusive comparison.
     * <p>
     * For a discussion on how this is used, see {@link #getMaxVersion()}.
     * @param maxVersion The highest version to allow during update procedures on this component.
     */
    @JsonProperty("maxVersion")
    public void setMaxVersion(final Version maxVersion)
    {
        this.maxVersion = maxVersion;
    }
}
