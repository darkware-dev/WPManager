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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Stream;

/**
 * This is a managed data component acting as a collection of {@link WPBlog} objects.
 *
 * @author jeff
 * @since 2016-01-23
 */
public class WPBlogs extends WPDataComponent implements Iterable<WPBlog>
{
    private final Map<Integer, WPBlog> blogs;

    /**
     * Create a new {@code WPBlogs} collection. At creation time, the collection is empty.
     */
    public WPBlogs()
    {
        super();

        this.blogs = new ConcurrentSkipListMap<>();
    }

    /**
     * Fetch a specific blog, given a unique string identifier. Currently, the recognized identifiers are:
     * <ul>
     *   <li>The full internal domain name</li>
     *   <li>The initial sub-domain portion of the domain name</li><
     * </ul>
     *
     * @param identifier A unique identifier for the blog to fetch.
     * @return The {@link WPBlog} associated with the identifier, or {@code null} if no blog matched.
     */
    public WPBlog get(final String identifier)
    {
        this.checkRefresh();

        for (WPBlog blog : this)
        {
            if (blog.getDomain().equals(identifier)) return blog;
            if (blog.getSubDomain().equals(identifier)) return blog;
        }
        return null;
    }

    @Override
    protected void refreshBaseData()
    {
        List<WPBlog> rawBlogs = this.getManager().getDataManager().getBlogs();

        for (WPBlog blog : rawBlogs)
        {
            WPData.log.debug("Loaded blog: #{}: {}", blog.getBlogId(), blog.getUrl());
            this.blogs.put(blog.getBlogId(), blog);
        }
    }

    @Override
    public Iterator<WPBlog> iterator()
    {
        this.checkRefresh();
        return this.blogs.values().iterator();
    }

    public Stream<WPBlog> stream()
    {
        this.checkRefresh();
        return this.blogs.values().stream();
    }
}
