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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Objects;
import com.google.common.reflect.TypeToken;
import org.darkware.lazylib.LazyLoaded;
import org.darkware.lazylib.LazyLoadedMap;
import org.darkware.wpman.util.serialization.MinimalUpdatableSerializer;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIFieldsOption;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A {@code WPBlog} represents a single blog ("site" in the legacy terminology) within a WordPress installation.
 * In single-site installs, this should be roughly equivalent to the entire install, and most queries will return
 * the same set of data for the blog and the instance as a whole. In multisite installs, there may be significant
 * differences between a blog and the encompassing network (sometimes confusingly termed a "site"). In multisite
 * instances, a blog has a set of users which is a strict subset of the network user list, an identical set of
 * plugins with different activation states, and a set of themes which matches just those themes activated on
 * the network level.
 *
 * @author jeff
 * @since 2016-01-23
 */
public class WPBlog extends WPComponent
{
    /**
     * Set the required field options on the {@link WPCLI} command in order to support proper
     * deserialization of JSON objects.
     *
     * @param blogCommand The command to set fields on.
     * @return The command that was supplied, with field options now set.
     */
    public static WPCLI setFields(final WPCLI blogCommand)
    {
        WPCLIFieldsOption fields = new WPCLIFieldsOption();
        fields.add("blog_id");
        fields.add("domain");
        fields.add("url");
        fields.add("last_updated");
        fields.add("registered");
        fields.add("public");
        fields.add("deleted");
        blogCommand.setOption(fields);

        return blogCommand;
    }

    @JsonProperty("blog_id")
    private int blogId;
    @JsonProperty("domain")
    private String domain;
    @JsonProperty("url")
    private String url;
    @JsonProperty("last_updated")
    private LocalDateTime lastModified;
    @JsonProperty("registered")
    private LocalDateTime creationDate;

    @JsonProperty("public")
    private boolean searchable;
    @JsonProperty("deleted")
    private boolean deleted;

    @JsonProperty("users")
    private final WPBlogUsers users;

    private transient String subDomain;

    @JsonIgnore
    private final WPBlogPlugins plugins;
    @JsonIgnore
    private final WPCron cron;

    @JsonProperty("theme")
    @JsonSerialize(using = MinimalUpdatableSerializer.class)
    private LazyLoaded<WPTheme> theme;

    @JsonIgnore
    private LazyLoadedMap<String, WPTaxonomy> taxonomies;

    /**
     * Create a new blog instance. The initial state is incomplete and requires setting at least the
     * domain in order to fully support normal functionality.
     */
    public WPBlog()
    {
        super();

        this.plugins = new WPBlogPlugins(this);
        this.cron = new WPCron(this);
        this.users = new WPBlogUsers(this);

        this.theme = new LazyLoaded<WPTheme>()
        {
            @Override
            protected WPTheme loadValue() throws Exception
            {
                WPCLI themeListCmd = WPBlog.this.buildCommand("theme", "list");
                themeListCmd.loadThemes(false);
                themeListCmd.loadPlugins(false);
                WPTheme.setFields(themeListCmd);

                themeListCmd.setBlog(WPBlog.this);
                themeListCmd.restrictList("status", WPThemeStatus.ACTIVE);

                List<WPTheme> activeThemes = themeListCmd.readJSON(new TypeToken<List<WPTheme>>(){});

                return activeThemes.get(0);
            }
        };

        this.taxonomies = new LazyLoadedMap<String, WPTaxonomy>()
        {
            @Override
            protected Map<String, WPTaxonomy> loadValues() throws Exception
            {
                WPCLI taxCmd = WPBlog.this.buildCommand("taxonomy", "list");
                taxCmd.setBlog(WPBlog.this);
                taxCmd.loadThemes(false);
                taxCmd.loadPlugins(false);
                WPTaxonomy.setFields(taxCmd);

                Map<String, WPTaxonomy> taxMap = new HashMap<>();
                for (WPTaxonomy tax : taxCmd.readJSON(new TypeToken<Set<WPTaxonomy>>(){}))
                {
                    tax.setBlog(WPBlog.this);
                    taxMap.put(tax.getName(), tax);
                }

                return taxMap;
            }
        };
    }

    /**
     * Fetch the numeric ID assigned to this blog. This is mostly used for various internal references and
     * some linking (such as the media library). In general, it is not publicly advertised.
     *
     * @return The ID of this blog as a positive integer.
     */
    public int getBlogId()
    {
        return this.blogId;
    }

    /**
     * Set the ID for this blog. The ID is required to be unique within the installation or network.
     *
     * @param blogId A positive integer.
     * @throws IllegalArgumentException If the blog ID is zero or negative.
     */
    protected void setBlogId(final int blogId)
    {
        if (blogId < 1) throw new IllegalArgumentException("Blog ID cannot be zero or negative.");
        this.blogId = blogId;
    }

    /**
     * Fetch the full domain assigned to this blog. In multisite installations this is a critical identifying
     * characteristic of the blog. It acts as the unique identifier of the blog within the multisite network,
     * and will be used by various other methods and objects for retrieving blog-specific data sets.
     * <p>
     * <em>Note:</em> This is the internal, canonical domain name assigned to the blog. Alternative domain names
     * assigned by rewrite rules or plugins such as Domain Mapping are not reflected here and must not be used.
     *
     * @return The blog's official, internal domain name.
     */
    public String getDomain()
    {
        return this.domain;
    }

    /**
     * Sets the official domain for this blog. This is required to be unique within the multisite network. It
     * is further required to be a strict subdomain of the network domain. For example, if the network's
     * configured domain name is {@code examplewp.com}, the all blogs in the network <em>must</em> use
     * domains which end in {@code examplewp.com}.
     * <p>
     * Updating this field during runtime <em>will not</em> modify any actual domain mapping within the
     * installation. This is solely used for identifying the blog.
     *
     * @param domain The full, canonical domain name for the blog.
     */
    protected void setDomain(final String domain)
    {
        this.domain = domain;
    }

    /**
     * Fetch the official subdomain for this blog. This is defined to be the first hostname group of the
     * official domain obtained by {@link #getDomain()}. This can be treated as a short hand for the domain
     * since the remainder of the domain is defined to be the network domain and is shared by every blog
     * on the network. Thus, the subdomain shares the same uniqueness as the domain.
     *
     * @return The subdomain for the blog.
     */
    public final String getSubDomain()
    {
        if (this.subDomain == null)
        {
            if (this.domain == null) return "<unknown>";
            this.subDomain = this.domain.split("\\.", 2)[0];
        }
        return this.subDomain;
    }

    /**
     * Fetch the URL for this blog. This is mostly useful as a convenience. It should match the blog's domain
     * returned by {@link #getDomain()}, with the additional URI components to indicate an empty directory and
     * the default protocol specification.
     * <p>
     * <em>Future Implementation:</em> This field would gain more value if support is added for directory-based
     * multisite installations. In that case, the URL would provide a single-method way of fetching the
     * official URL to contact the blog via, incorporating changes for both subdomains and subdirectories.
     *
     * @return The official URL as a {@code String}.
     */
    public String getUrl()
    {
        return this.url;
    }

    /**
     * Set the URL for this blog. Since subdirectory-based multisite installs are not supported, this should
     * always roughly match the domain name.
     *
     * @param url The official URL for this blog.
     */
    protected void setUrl(final String url)
    {
        this.url = url;
    }

    /**
     * Fetch the time that this blog was last modified. Modifications appear to include various actions including
     * changing settings, activating plugins, creating posts, or uploading media.
     *
     * @return The last modification time, in UTC time.
     */
    public LocalDateTime getLastModified()
    {
        return this.lastModified;
    }

    /**
     * Set the last time this blog was modified.
     *
     * @param lastModified The last modification time.
     */
    protected void setLastModified(final LocalDateTime lastModified)
    {
        this.lastModified = lastModified;
    }

    /**
     * Fetch the time and date that this blog was created.
     *
     * @return The creation date, in UTC time.
     */
    public LocalDateTime getCreationDate()
    {
        return this.creationDate;
    }

    /**
     * Set the creation date and time for this blog.
     *
     * @param creationDate A date and time, in UTC time.
     */
    protected void setCreationDate(final LocalDateTime creationDate)
    {
        this.creationDate = creationDate;
    }

    /**
     * Check if this blog is set to encourage Internet search engine indexing. This is usually realized as
     * changes to the response for URLs requesting the {@code robots.txt} file. Searchable blogs will reply with
     * permissive settings. Non-searchable blogs will reply with settings discouraging indexing. Ultimately, it
     * is up to the search engine to honor these settings.
     *
     * @return {@code true} if the site encourages search engine indexing, {@code false} if it attempts to
     * discourage search engines.
     */
    public boolean isSearchable()
    {
        return this.searchable;
    }

    /**
     * Set the flag declaring the blogs behavior toward search engines.
     *
     * @param searchable {@code true} if the site encourages search engine indexing, {@code false} if it
     * attempts to discourage search engines.
     * @see #isSearchable()
     */
    protected void setSearchable(final boolean searchable)
    {
        this.searchable = searchable;
    }

    /**
     * Check if this blog has been marked for deletion. Being marked for deletion does not trigger immediate
     * deletion. Rather, it is treated more like a filesystem "dirty" flag or a desktop "trash" feature. The blog
     * will continue to exist until some other process decides to actually delete it. Until that point, the flag
     * can be unset and the blog would continue to exist as normal.
     *
     * @return {@code true} if the blog is marked to be deleted in the future, otherwise {@code false}.
     */
    public boolean isDeleted()
    {
        return this.deleted;
    }

    /**
     * Set the flag to mark this blog as waiting to be deleted. This does not actually cause deletion.
     *
     * @param deleted {@code true} if the blog should be marked as waiting for deletion, otherwise {@code false}.
     */
    public void setDeleted(final boolean deleted)
    {
        this.deleted = deleted;
    }

    /**
     * Fetch the users assigned to this blog. This includes only users who have been directly assigned to the
     * blog and may not include users who have access to the site via superadmin privileges.
     *
     * @return A {@link WPBlogUsers} object attached to this blog.
     */
    public final WPBlogUsers getUsers()
    {
        return this.users;
    }

    /**
     * Fetch the collection of plugins recognized by this blog, along with the state of those plugins with
     * respect to this blog.
     *
     * @return The collection of plugins as a {@link WPBlogPlugins} object.
     */
    public final WPBlogPlugins getPlugins()
    {
        return this.plugins;
    }

    /**
     * Fetch the theme currently being used by this blog.
     *
     * @return The currently activated {@link WPTheme}.
     */
    public WPTheme getTheme()
    {
        return this.theme.value();
    }

    /**
     * Fetch cron subsystem data specific to this blog. This includes a list of registered events scheduled
     * for execution within the blog.
     *
     * @return The cron data as a {@link WPCron} object associated with this blog.
     */
    public WPCron getCron()
    {
        return this.cron;
    }

    /**
     * Fetch the {@link WPTaxonomy} for the given name.
     *
     * @param taxonomyName The internal name for the taxonomy.
     * @return A {@link WPTaxonomy}.
     * @throws IllegalArgumentException If the taxonomy doesn't exist.
     */
    @JsonIgnore
    public WPTaxonomy getTaxonomy(final String taxonomyName)
    {
        WPTaxonomy tax = this.taxonomies.map().get(taxonomyName);
        if (tax == null) throw new IllegalArgumentException("The taxonomy '" + taxonomyName + "' does not exist.");
        return tax;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof WPBlog)) return false;
        final WPBlog wpBlog = (WPBlog) o;
        return this.blogId == wpBlog.blogId;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(this.blogId);
    }
}
