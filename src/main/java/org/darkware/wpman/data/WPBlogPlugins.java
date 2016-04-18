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

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author jeff
 * @since 2016-01-31
 */
public class WPBlogPlugins extends WPDataComponent
{
    private final WPBlog blog;
    private final List<WPPlugin> plugins;

    public WPBlogPlugins(final WPBlog blog)
    {
        super();

        this.blog = blog;
        this.plugins = new ArrayList<>();
    }

    @Override
    protected void refreshBaseData()
    {
        this.plugins.clear();
        this.plugins.addAll(this.getManager().getDataManager().getPluginsForBlog(this.blog));
    }
}
