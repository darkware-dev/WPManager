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
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The {@code WPBlogUsers} object acts as a proxy collection of {@link WPUser WPUsers} that are associated
 * with a particular {@link WPBlog}.
 *
 * @author jeff
 * @since 2016-04-15
 */
public class WPBlogUsers extends WPComponent implements Iterable<WPUser>
{
    private final WPBlog blog;
    private final LazyLoadedMap<Integer, WPUser> users;

    /**
     * Creates a new collection of users attached to a {@code WPBlog}.
     *
     * @param blog The blog to collect users for.
     */
    public WPBlogUsers(final WPBlog blog)
    {
        super();

        this.blog = blog;
        this.users = new LazyLoadedMap<Integer, WPUser>(Duration.ofMinutes(20))
        {
            @Override
            protected Map<Integer, WPUser> loadValues() throws Exception
            {
                WPCLI userListCmd = WPBlogUsers.this.buildCommand("user", "list");
                userListCmd.loadPlugins(false);
                userListCmd.loadThemes(false);
                userListCmd.setBlog(WPBlogUsers.this.blog);
                WPUser.setFields(userListCmd);

                Set<WPUser> users = userListCmd.readJSON(new TypeToken<Set<WPUser>>() {});

                Map<Integer, WPUser> userMap = new HashMap<>();
                users.forEach(u -> userMap.put(u.getId(), u));
                return userMap;
            }
        };
    }

    /**
     * Fetch a {@code Stream} users assigned to the attached blog.
     *
     * @return A {@link Stream} of {@link WPUser}s.
     */
    public Stream<WPUser> stream()
    {
        return this.users.stream();
    }

    @Override
    public Iterator<WPUser> iterator()
    {
        return this.users.iterator();
    }
}
