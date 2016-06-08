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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
    ACTIVE("active", "enable"),
    /** Not enabled. */
    INACTIVE("inactive", "disable"),
    /** Enabled on the network level. */
    NETWORK_ACTIVE("active-network", "network-active", "network-enable", "network"),
    /** An undeclared status. */
    UNDECLARED("undeclared");

    private final String internal;
    private final Set<String> aliases;

    /**
     * Initializes a status.
     *
     * @param internal The canonical internal name for this status.
     * @param aliases Additional aliases this status is also known by.
     */
    WPPluginStatus(final String internal, final String ... aliases)
    {
        this.internal = internal;

        Set<String> aliasSet = new HashSet<>();
        Collections.addAll(aliasSet, aliases);
        this.aliases = Collections.unmodifiableSet(aliasSet);
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

    /**
     * Fetch an additional set of names this status is known as. Generally, these are used only for recognizing
     * (eg: deserializing) a given status. They won't be used in reporting the status.
     *
     * @return An unmodifiable {@link Set} of alternative names for this status.
     */
    public Set<String> getAliases()
    {
        return this.aliases;
    }

    @Override
    public String toString()
    {
        return this.internal;
    }
}
