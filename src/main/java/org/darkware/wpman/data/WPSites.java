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
 * @author jeff
 * @since 2016-01-23
 */
public class WPSites extends WPDataComponent implements Iterable<WPSite>
{
    private final Map<Integer, WPSite> sites;

    public WPSites()
    {
        super();

        this.sites = new ConcurrentSkipListMap<>();
    }

    public WPSite get(final String identifier)
    {
        for (WPSite site : this)
        {
            if (site.getDomain().equals(identifier)) return site;
            if (site.getSubDomain().equals(identifier)) return site;
        }
        return null;
    }

    @Override
    protected void refreshBaseData()
    {
        List<WPSite> rawSites = this.getManager().getDataManager().getSites();

        for (WPSite site : rawSites)
        {
            WPData.log.debug("Loaded site: #{}: {}", site.getBlogId(), site.getUrl());
            this.sites.put(site.getBlogId(), site);
        }
    }

    @Override
    public Iterator<WPSite> iterator()
    {
        this.checkRefresh();
        return this.sites.values().iterator();
    }

    public Stream<WPSite> stream()
    {
        this.checkRefresh();
        return this.sites.values().stream();
    }
}
