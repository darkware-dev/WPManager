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

import com.google.common.reflect.TypeToken;
import org.darkware.lazylib.LazyLoadedMap;
import org.darkware.wpman.wpcli.WPCLI;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This is a managed data component acting as a collection of {@link WPBlog} objects.
 *
 * @author jeff
 * @since 2016-01-23
 */
public class WPBlogs extends WPComponent implements Iterable<WPBlog>
{
    private final LazyLoadedMap<Integer, WPBlog> blogs;
    private final LazyLoadedMap<String, WPBlog> blogsByDomain;

    /**
     * Create a new {@code WPBlogs} collection. At creation time, the collection is empty.
     */
    public WPBlogs()
    {
        super();

        this.blogs = new LazyLoadedMap<Integer, WPBlog>(Duration.ofHours(24))
        {
            @Override
            protected Map<Integer, WPBlog> loadValues() throws Exception
            {
                try
                {
                    WPCLI listCmd = WPBlogs.this.buildCommand("site", "list");
                    listCmd.loadPlugins(false);
                    listCmd.loadThemes(false);
                    WPBlog.setFields(listCmd);

                    List<WPBlog> rawBlogs = listCmd.readJSON(new TypeToken<List<WPBlog>>(){});
                    if (rawBlogs == null) throw new RuntimeException("Failed to load the blog list.");

                    Map<Integer, WPBlog> blogMap = new HashMap<>();
                    rawBlogs.stream().forEach(b -> blogMap.put(b.getBlogId(), b));

                    return blogMap;
                }
                finally
                {
                    WPBlogs.this.blogsByDomain.expire();
                }
            }
        };

        this.blogsByDomain = new LazyLoadedMap<String, WPBlog>()
        {
            @Override
            protected Map<String, WPBlog> loadValues() throws Exception
            {
                Map<String, WPBlog> nameMap = new HashMap<>();

                for (WPBlog blog : WPBlogs.this.blogs)
                {
                    nameMap.put(blog.getDomain(), blog);
                    nameMap.put(blog.getSubDomain(), blog);
                }

                return nameMap;
            }
        };
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
        return this.blogsByDomain.map().get(identifier);
    }

    public Iterator<WPBlog> iterator()
    {
        return this.blogs.iterator();
    }

    /**
     * Fetch a stream of the blogs in this instance.
     *
     * @return A {@link Stream} of {@code WPBlog} objects.
     */
    public Stream<WPBlog> stream()
    {
        return this.blogs.stream();
    }
}
