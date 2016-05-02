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
 * This enumeration contains the set of updatable component types.
 *
 * @author jeff
 * @since 2016-05-01
 */
public enum WPUpdatableType
{
    /** A WordPress plugin */
    PLUGIN,
    /** A WordPress theme */
    THEME;

    private final String plural;

    /**
     * Defines a new updatable type with the given plural name.
     *
     * @param plural The plural string to use.
     */
    WPUpdatableType(final String plural)
    {
        this.plural = plural;
    }

    /**
     * Defines a new updatable type with the default plural form.
     */
    WPUpdatableType()
    {
        this.plural = this.name().toLowerCase().concat("s");
    }

    /**
     * Fetches the canonical token to identify this type. Typically, this is a singular form of the
     * component name used by WordPress.
     *
     * @return The token as a {@code String}.
     */
    public String getToken()
    {
        return this.name().toLowerCase();
    }

    /**
     * Fetches the plural form of the token. This is used in some paths and configuration for improved
     * compatibility with WordPress and WP-CLI.
     *
     * @return The plural form of the token, as a {@code String}.
     */
    public String getPlural()
    {
        return this.plural;
    }
}
