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

package org.darkware.wpman.rest;

import org.darkware.wpman.WPManager;
import org.darkware.wpman.WPManagerConfiguration;
import org.darkware.wpman.data.WPSite;
import org.darkware.wpman.data.WPSiteUsers;
import org.darkware.wpman.data.WPSites;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jeff
 * @since 2016-03-30
 */
@Path("/site")
public class SiteResource
{
    private final WPManager manager;
    private final WPManagerConfiguration config;
    private final WPSites sites;

    public SiteResource(final WPManager manager)
    {
        super();

        this.manager = manager;
        this.config = manager.getConfig();
        this.sites = manager.getData().getSites();
    }

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<WPSite> list()
    {
        return this.sites.stream().collect(Collectors.toList());
    }

    @GET
    @Path("{site}/users")
    @Produces(MediaType.APPLICATION_JSON)
    public WPSiteUsers users(@PathParam("site") final String siteDomain)
    {
        return this.sites.get(siteDomain).getUsers();
    }
}
