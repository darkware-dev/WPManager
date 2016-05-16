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

/**
 * This enumeration tracks the status of a single {@link WPPlugin}'s enabled state with respect to a
 * single blog.
 *
 * @author jeff
 * @since 2016-01-23
 */
public enum WPPluginStatus
{
    /** Enabled on the local blog. */
    ACTIVE("active"),
    /** Not enabled. */
    INACTIVE("inactive"),
    /** Enabled on the network level. */
    NETWORK_ACTIVE("active-network");

    private final String internal;

    WPPluginStatus(final String internal)
    {
        this.internal = internal;
    }

    /**
     * Checks to see if this status is currently enabled. This handles both local and network activations.
     *
     * @return {@code true} if the plugin is either {@link #ACTIVE} or {@link #NETWORK_ACTIVE}.
     */
    public boolean isEnabled()
    {
        return this != WPPluginStatus.INACTIVE;
    }

    @Override
    public String toString()
    {
        return this.internal;
    }
}
