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

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;
import org.joda.time.DateTime;

/**
 * @author jeff
 * @since 2016-01-23
 */
public class WPSite
{
    @SerializedName("blog_id") private int blogId;
    @SerializedName("domain") private String domain;
    @SerializedName("url") private String url;
    @SerializedName("last_updated") private DateTime lastModified;
    @SerializedName("registered") private DateTime creationDate;

    @SerializedName("public") private boolean searchable;
    @SerializedName("deleted") private boolean deleted;

    private String subDomain;
    private final WPSitePlugins plugins;
    private final WPCron cron;
    private WPSiteTheme theme;

    public WPSite()
    {
        super();

        this.plugins = new WPSitePlugins(this);
        this.theme = new WPSiteTheme(this);
        this.cron = new WPCron(this);
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

    public DateTime getLastModified()
    {
        return this.lastModified;
    }

    public void setLastModified(final DateTime lastModified)
    {
        this.lastModified = lastModified;
    }

    public DateTime getCreationDate()
    {
        return this.creationDate;
    }

    public void setCreationDate(final DateTime creationDate)
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
        return this.deleted;
    }

    public void setDeleted(final boolean deleted)
    {
        this.deleted = deleted;
    }

    public WPSitePlugins getPlugins()
    {
        return plugins;
    }

    public WPSiteTheme getTheme()
    {
        return this.theme;
    }

    public WPCron getCron()
    {
        return this.cron;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof WPSite)) return false;
        final WPSite wpSite = (WPSite) o;
        return blogId == wpSite.blogId;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(blogId);
    }
}
