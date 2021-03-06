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
import org.darkware.wpman.data.WPCore;
import org.darkware.wpman.events.WPCoreUpdateRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * This is a REST resource servicing requests for WordPress core functionality.
 *
 * @author jeff
 * @since 2016-04-05
 */
@Path("/core")
public class CoreResource
{
    private final WPManager manager;

    /**
     * Create a new core REST handler.
     *
     * @param manager The {@link WPManager} to link to.
     */
    public CoreResource(final WPManager manager)
    {
        super();

        this.manager = manager;
    }

    /**
     * Fetch various information about WordPress core software and the installation as a whole.
     *
     * @return A {@link WPCore} data object.
     */
    @GET
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public WPCore info()
    {
        return this.manager.getData().getCore();
    }

    /**
     * Request a WordPress core software update. The method does not wait for the update to complete
     * or even start. The request is dispatched and acknowledged, but may be canceled based on the lack
     * of available updates or other restrictions.
     *
     * @return A message declaring the success or failure of the request dispatching.
     */
    @GET
    @Path("update")
    @Produces(MediaType.TEXT_PLAIN)
    public String update()
    {
        WPManager.log.info("Requesting WordPress Core update via REST");
        this.manager.dispatchEvent(new WPCoreUpdateRequest(true));
        return "Update Scheduled.";
    }
}
