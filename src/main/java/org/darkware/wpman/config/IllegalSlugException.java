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

/**
 * An {@code IllegalSlugException} is thrown when code encounters a reference to a plugin or theme slug
 * that doesn't conform to the required conventions for slugs.
 *
 * @author jeff
 * @since 2016-05-30
 */
public class IllegalSlugException extends RuntimeException
{
    /**
     * Creates a new {@code IllegalSlugException} reporting the problematic string.
     *
     * @param slug The {@code String} which was rejected as a slug.
     */
    public IllegalSlugException(final String slug)
    {
        super("Encountered illegal slug: [" + slug + "]");
    }
}
