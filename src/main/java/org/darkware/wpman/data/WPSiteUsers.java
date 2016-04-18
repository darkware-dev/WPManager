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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Stream;

/**
 * The {@code WPSiteUsers} object acts as a proxy collection of {@link WPUser WPUsers} that are associated
 * with a particular {@link WPSite}.
 *
 * @author jeff
 * @since 2016-04-15
 */
public class WPSiteUsers extends WPDataComponent implements Iterable<WPUser>
{
    private final WPSite site;
    private final Map<Integer, WPUser> users;

    /**
     * Creates a new collection of users attached to a {@code WPSite}.
     *
     * @param site The site to collect users for.
     */
    public WPSiteUsers(final WPSite site)
    {
        super();

        this.site = site;
        this.users = new ConcurrentSkipListMap<>();
    }

    /**
     * Fetch a {@code Stream} users assigned to the attached site.
     *
     * @return A {@link Stream} of {@link WPUser}s.
     */
    public Stream<WPUser> stream()
    {
        this.checkRefresh();
        return this.users.values().stream();
    }

    @Override
    public Iterator<WPUser> iterator()
    {
        this.checkRefresh();
        return this.users.values().iterator();
    }

    @Override
    protected void refreshBaseData()
    {
        Set<WPUser> current = this.getManager().getDataManager().getUsersForSite(this.site);

        // Remove all items not in the current set
        this.users.entrySet().removeIf(e -> !current.contains(e.getValue()));

        // Store all items in the set.
        current.stream().forEach(u -> this.users.put(u.getId(), u));
    }
}
