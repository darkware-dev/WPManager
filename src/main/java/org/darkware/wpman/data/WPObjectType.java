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
 * The {@code WPObjectType} enumeration contains a list of all the recognized primary object types
 * within WordPress.
 *
 * @author jeff
 * @since 2016-05-18
 */
public enum WPObjectType
{
    /** A blog post */
    POST,
    /** A static page. */
    PAGE,
    /** A navigation item, such as a menu */
    NAVITEM,
    /** An external link. */
    LINK,
    /** A post comment. */
    COMMENT,
    /** An unrecognized object. */
    UNKNOWN;

    private final String slug;

    /**
     * Declare an object type with a specific slug.
     *
     * @param slug
     */
    WPObjectType(final String slug)
    {
        this.slug = slug;
    }

    /**
     * Declare an object type with the default slug (the lowercase version of the name)
     */
    WPObjectType()
    {
        this.slug = this.name().toLowerCase();
    }

    /**
     * Fetch the internal key string for referencing this type.
     *
     * @return The internal key as a {@code String}.
     */
    public final String getSlug()
    {
        return this.slug;
    }
}
