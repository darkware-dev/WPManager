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
import com.google.common.reflect.TypeToken;
import org.darkware.lazylib.LazyLoadedSet;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIFieldsOption;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A {@code WPTaxonomy} is a group of {@link WPTerm}s recognized by a given {@link WPBlog}.
 *
 * @author jeff
 * @since 2016-05-18
 */
public class WPTaxonomy extends WPComponent
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
        fields.add("name");
        fields.add("label");
        fields.add("description");
        fields.add("object_type");
        fields.add("hierarchical");
        fields.add("public");
        command.setOption(fields);

        return command;
    }

    @JsonProperty("name")
    private String name;
    @JsonProperty("label")
    private String label;
    @JsonProperty("description")
    private String description;
    @JsonProperty("object_type")
    private WPObjectType objectType;
    @JsonIgnore
    private boolean publicTaxonomy;
    @JsonProperty("hierarchical")
    private boolean hierarchical;

    @JsonIgnore
    private WPBlog blog;

    @JsonIgnore
    private final LazyLoadedSet<WPTerm> terms;
    @JsonIgnore
    private transient Map<String, WPTerm> termsBySlug;
    @JsonIgnore
    private transient Map<Integer, WPTerm> termsById;

    /**
     * Create a new taxonomy object.
     */
    public WPTaxonomy()
    {
        super();

        this.objectType = WPObjectType.UNKNOWN;

        this.termsById = null;
        this.termsBySlug = null;

        this.terms = new LazyLoadedSet<WPTerm>()
        {
            @Override
            protected Collection<WPTerm> loadValues() throws Exception
            {
                return WPTaxonomy.this.fetchTerms();
            }
        };
    }

    /**
     * Fetch the blog this taxonomy belongs to.
     *
     * @return A {@link WPBlog}.
     */
    public WPBlog getBlog()
    {
        return this.blog;
    }

    /**
     * Set the {@link WPBlog} associated with this taxonomy. This is needed to perform automatic loading of
     * the taxonomy terms.
     *
     * @param blog The {@link WPBlog} for this taxonomy.
     */
    public void setBlog(final WPBlog blog)
    {
        this.blog = blog;
    }

    /**
     * Fetch the internal name for this taxonomy.
     *
     * @return The name as a {@code String}.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Set the name for this taxonomy.
     *
     * @param name The name as a {@code String}.
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /**
     * Fetch the display-worthy label for this taxonomy.
     *
     * @return The label, as a {@code String}.
     */
    public String getLabel()
    {
        return this.label;
    }

    /**
     * Set the label to display for this taxonomy.
     *
     * @param label The label to use.
     */
    public void setLabel(final String label)
    {
        this.label = label;
    }

    /**
     * Fetch the long description of this taxonomy, if supplied.
     *
     * @return A text description.
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Set the long description of this taxonomy.
     *
     * @param description The description as a {@code String}.
     */
    public void setDescription(final String description)
    {
        this.description = description;
    }

    /**
     * Fetch the {@link WPObjectType} this taxonomy can be applied to. If the type is not set
     * or not recognized, this will be {@link WPObjectType#UNKNOWN}.
     *
     * @return A non-null {@link WPObjectType}.
     */
    public WPObjectType getObjectType()
    {
        return this.objectType;
    }

    /**
     * Set the {@link WPObjectType} this taxonomy can be applied to.
     *
     * @param objectType A non-null {@link WPObjectType}.
     */
    public void setObjectType(final WPObjectType objectType)
    {
        this.objectType = objectType;
    }

    /**
     * Checks if this taxonomy is available for public examination.
     *
     * @return {@code true} if the taxonomy is public.
     */
    @JsonProperty("public")
    public boolean isPublic()
    {
        return this.publicTaxonomy;
    }

    /**
     * Declares if this taxonomy should be public.
     *
     * @param publicTaxonomy {@code true} if the taxonomy should be public, otherwise {@code false}
     */
    public void setPublic(final boolean publicTaxonomy)
    {
        this.publicTaxonomy = publicTaxonomy;
    }

    /**
     * Checks if this taxonomy stores terms in a hierarchy.
     *
     * @return {@code true} if the terms form a tree structure, otherwise {@code false}.
     */
    public boolean isHierarchical()
    {
        return this.hierarchical;
    }

    /**
     * Declares if this taxonomy uses hierarchical terms.
     *
     * @param hierarchical {@code true} if the terms form a tree structure, otherwise {@code false}.
     */
    public void setHierarchical(final boolean hierarchical)
    {
        this.hierarchical = hierarchical;
    }

    /**
     * Fetch the map of terms within this taxonomy.
     *
     * @return A {@link Map} of term IDs to the terms associated with them.
     */
    public Set<WPTerm> getTerms()
    {
        return this.terms.values();
    }

    /**
     * Fetch the term for a given slug.
     *
     * @param slug The slug to search for.
     * @return A {@link WPTerm} matching the slug, or {@code null} if the term was not found.
     */
    public WPTerm getTerm(final String slug)
    {
        if (slug == null) throw new IllegalArgumentException("Cannot search for a null slug.");
        if (this.termsBySlug == null) this.buildTermMaps();
        return this.termsBySlug.get(slug);
    }

    /**
     * Fetch the term for a given term ID.
     *
     * @param id The id to search for.
     * @return A {@link WPTerm} matching the id, or {@code null} if the term was not found.
     */
    public WPTerm getTerm(final Integer id)
    {
        if (id == null) throw new IllegalArgumentException("Cannot search for a null id.");
        if (this.termsById == null) this.buildTermMaps();
        return this.termsById.get(id);
    }

    /**
     * Rebuilds the maps of terms for easy lookup.
     */
    private synchronized void buildTermMaps()
    {
        if (this.termsById == null || this.termsBySlug == null)
        {
            this.termsById = new HashMap<>();
            this.termsBySlug = new HashMap<>();

            for (WPTerm term : this.terms)
            {
                this.termsById.put(term.getId(), term);
                this.termsBySlug.put(term.getSlug(), term);
            }
        }
    }

    /**
     * Fetch the terms for this taxonomy from the WordPress backend.
     *
     * @return A {@link Set} of {@link WPTerm}s.
     */
    private Set<WPTerm> fetchTerms()
    {
        if (this.blog == null) throw new IllegalStateException("The blog must be set before terms can be fetched.");

        WPCLI termCommand = this.buildCommand("term", "list", this.name);
        termCommand.setBlog(this.blog);
        termCommand.loadPlugins(false);
        termCommand.loadThemes(false);
        WPTerm.setFields(termCommand);

        return termCommand.readJSON(new TypeToken<Set<WPTerm>>(){});
    }
}
