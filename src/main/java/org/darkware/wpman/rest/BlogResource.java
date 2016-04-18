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
import org.darkware.wpman.data.WPBlog;
import org.darkware.wpman.data.WPBlogUsers;
import org.darkware.wpman.data.WPBlogs;

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
@Path("/blog")
public class BlogResource
{
    private final WPManager manager;
    private final WPManagerConfiguration config;
    private final WPBlogs blogs;

    public BlogResource(final WPManager manager)
    {
        super();

        this.manager = manager;
        this.config = manager.getConfig();
        this.blogs = manager.getData().getBlogs();
    }

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<WPBlog> list()
    {
        return this.blogs.stream().collect(Collectors.toList());
    }

    @GET
    @Path("{blog}/users")
    @Produces(MediaType.APPLICATION_JSON)
    public WPBlogUsers users(@PathParam("blog") final String blogDomain)
    {
        return this.blogs.get(blogDomain).getUsers();
    }
}
