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
 * This interface abstracts the concept of items available from a component repository. This is primarily for
 * plugins and themes.
 *
 * @author jeff@darkware.org
 * @since 2016-06-08
 */
public interface WPRepositoryItem
{
    /**
     * Fetch the unique identifier for this component. In most cases, this is the component's "slug".
     *
     * @return An identifier unique to components of this type.
     */
    String getId();

    /**
     * Set the unique identifier for this component.
     *
     * @param id The {@code String} to set as the id.
     */
    void setId(final String id);

    /**
     * Fetch the repository "slug" for the item. This is a unique identifier for the item type within the
     * repository.
     *
     * @return The slug as a {@code String}
     */
    default String getSlug()
    {
        return this.getId();
    }

    /**
     * Set the repository slug used for this item.
     *
     * @param slug The slug as a {@code String}.
     */
    default void setSlug(final String slug)
    {
        this.setId(slug);
    }

    /**
     * Fetch the human-readable name for the item.
     *
     * @return A brief but descriptive name.
     */
    String getName();

    /**
     * Sets the human-readable name for the item.
     *
     * @param name A {@code String} name. Whitespace and punctuation is allowed.
     */
    void setName(String name);

    /**
     * Fetch a block of descriptive text providing extra information about the item.
     *
     * @return A {@code String} of free-form text.
     */
    String getDescription();

    /**
     * Set the description of the item.
     *
     * @param description
     */
    void setDescription(String description);
}
