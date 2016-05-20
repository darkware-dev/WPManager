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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIFieldsOption;

/**
 * A {@code WPTerm} represents a single item within a {@link WPTaxonomy}.
 *
 * @author jeff
 * @since 2016-05-18
 */
public class WPTerm
{
    /**
     * Set the required field options on the {@link WPCLI} command in order to support proper
     * deserialization of JSON objects.
     *
     * @param command The command to set fields on.
     * @return The command that was supplied, with field options now set.
     */
    public static WPCLI setFields(final WPCLI command)
    {
        WPCLIFieldsOption fields = new WPCLIFieldsOption();
        fields.add("term_id");
        fields.add("name");
        fields.add("slug");
        fields.add("description");
        fields.add("parent");
        fields.add("count");
        command.setOption(fields);

        return command;
    }

    @JsonProperty("term_id")
    private int id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("slug")
    private String slug;
    @JsonProperty("description")
    private String description;
    @JsonProperty("parent")
    private int parentId;
    @JsonProperty("count")
    private int count;

    /**
     * Create a new undefined term.
     */
    public WPTerm()
    {
        super();

        // Some reasonable defaults
        this.parentId = 0;
        this.count = 0;
    }

    /**
     * Fetch the unique ID for this term. The ID should be unique across all other terms in this
     * blog.
     * <p>
     * <em>Note:</em> There seems to be some amount of movement on how terms and taxonomies are implemented
     * in recent versions of WordPress. Some of the rules, relationships, and procedures for resolving them
     * may change in upcoming versions.
     *
     * @return The unique ID for this term.
     */
    public int getId()
    {
        return this.id;
    }

    /**
     * Sets the unique identifier for this term.
     *
     * @param id A positive integer.
     */
    public void setId(final int id)
    {
        this.id = id;
    }

    /**
     * Fetch the display name for this term.
     *
     * @return A {@code String} containing a display version of this term.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Set the display name for this term.
     *
     * @param name A {@code String} containing a display version of this term.
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /**
     * Fetch the internal string identifier for this term. This is the representation used internally for
     * handling URLs and symbolic relationships.
     *
     * @return A simple string containing no whitespace.
     */
    public String getSlug()
    {
        return this.slug;
    }

    /**
     * Set the slug for this term. This should be unique to the term within the taxonomy it belongs to.
     *
     * @param slug A simple string containing no whitespace.
     */
    public void setSlug(final String slug)
    {
        this.slug = slug;
    }

    /**
     * Fetch the long description of this term.
     *
     * @return A {@code String} containing the description. This is often an empty string.
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Set the description of this term.
     *
     * @param description A {@code String} containing the description.
     */
    public void setDescription(final String description)
    {
        this.description = description;
    }

    /**
     * Fetch the parent term of this term. A value of zero indicates that the term has no parent.
     * <p>
     * This is only truly meaningful for hierarchical taxonomies. For non-hierarchical taxonomies, the
     * parent ID has no meaning, but is usually set to zero.
     *
     * @return The ID of parent term, or zero if the term has no parent.
     */
    public int getParentId()
    {
        return this.parentId;
    }

    /**
     * Set the ID of the parent term of this term. This should only be done for terms in hierarchical
     * taxonomies.
     *
     * @param parentId The ID of parent term, or zero to indicate that the term has no parent.
     */
    public void setParentId(final int parentId)
    {
        this.parentId = parentId;
    }

    /**
     * Fetch the number of times this term has been applied to objects.
     *
     * @return The count of uses of this term.
     */
    public int getCount()
    {
        return this.count;
    }

    /**
     * Update the usage count of this term.
     *
     * @param count The usage count as a non-negative integer.
     */
    public void setCount(final int count)
    {
        this.count = count;
    }
}
