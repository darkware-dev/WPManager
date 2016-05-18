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
import org.darkware.wpman.WPManager;
import org.darkware.wpman.util.serialization.MinimalUpdatableSerializer;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIFieldsOption;

import java.time.LocalDateTime;
import java.util.List;

/**
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

    @JsonProperty("blog_id") private int blogId;
    @JsonProperty("domain") private String domain;
    @JsonProperty("url") private String url;
    @JsonProperty("last_updated") private LocalDateTime lastModified;
    @JsonProperty("registered") private LocalDateTime creationDate;

    @JsonProperty("public") private boolean searchable;
    @JsonProperty("deleted") private boolean deleted;

    @JsonProperty("users")
    private final WPBlogUsers users;

    private String subDomain;
    private final WPBlogPlugins plugins;
    private final WPCron cron;
    private LazyLoaded<WPTheme> theme;

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
    }

    public int getBlogId()
    {
        return this.blogId;
    }

    public void setBlogId(final int blogId)
    {
        this.blogId = blogId;
    }

    public String getDomain()
    {
        return this.domain;
    }

    public void setDomain(final String domain)
    {
        this.domain = domain;
    }

    public String getSubDomain()
    {
        if (this.subDomain == null)
        {
            if (this.domain == null) return "<unknown>";
            this.subDomain = domain.split("\\.", 2)[0];
        }
        return this.subDomain;
    }

    public String getUrl()
    {
        return this.url;
    }

    public void setUrl(final String url)
    {
        this.url = url;
    }

    public LocalDateTime getLastModified()
    {
        return this.lastModified;
    }

    public void setLastModified(final LocalDateTime lastModified)
    {
        this.lastModified = lastModified;
    }

    public LocalDateTime getCreationDate()
    {
        return this.creationDate;
    }

    public void setCreationDate(final LocalDateTime creationDate)
    {
        this.creationDate = creationDate;
    }

    public boolean isSearchable()
    {
        return this.searchable;
    }

    public void setSearchable(final boolean searchable)
    {
        this.searchable = searchable;
    }

    public boolean isDeleted()
    {
        WPManager.log.info("Blog:" + this.getBlogId() + " deleted=" + this.deleted);
        return this.deleted;
    }

    public void setDeleted(final boolean deleted)
    {
        this.deleted = deleted;
    }

    /**
     * Fetch the users assigned to this blog.
     *
     * @return A {@link WPBlogUsers} object attached to this blog.
     */
    public WPBlogUsers getUsers()
    {
        return this.users;
    }

    @JsonIgnore
    public WPBlogPlugins getPlugins()
    {
        return plugins;
    }

    @JsonProperty("theme")
    @JsonSerialize(using = MinimalUpdatableSerializer.class)
    public WPTheme getTheme()
    {
        return this.theme.value();
    }

    @JsonIgnore
    public WPCron getCron()
    {
        return this.cron;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof WPBlog)) return false;
        final WPBlog wpBlog = (WPBlog) o;
        return blogId == wpBlog.blogId;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(blogId);
    }
}
