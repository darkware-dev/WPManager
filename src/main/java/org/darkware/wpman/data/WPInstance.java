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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code WPInstance} is an abstraction of a single installation of WordPress.
 * <p>
 * This operates as something of a combination of model and controller in the classic MVC paradigm.
 * Components include both their own internal data and the logic required to fetch or update it. The goal
 * is to allow for fairly granular control over how often the data is fetched and to allow for internal
 * references to fill in as much contextual data as possible.
 * <p>
 * For now, the instance is limited to representing multisite instances.
 *
 * @author jeff
 * @since 2016-01-23
 */
public class WPInstance extends WPComponent
{
    /** A shared logger for all instance objects. */
    protected static final Logger log = LoggerFactory.getLogger("WordPress");

    private final WPCore core;
    private final WPBlogs blogs;
    private final WPPlugins plugins;
    private final WPThemes themes;

    /**
     * Create a new object to track and manipulate a WordPress instance.
     */
    public WPInstance()
    {
        super();

        this.core = new WPCore();
        this.plugins = new WPPlugins();
        this.themes = new WPThemes();
        this.blogs = new WPBlogs();
    }

    /**
     * Fetch the model for the core WordPress software.
     *
     * @return A {@link WPCore} object for this instance.
     */
    public WPCore getCore()
    {
        return core;
    }

    /**
     * Fetch the collection of blogs in this instance.
     *
     * @return A {@link WPBlogs} object ready to retrieve blog information.
     */
    public WPBlogs getBlogs()
    {
        return this.blogs;
    }

    /**
     * Fetch the collection of plugins currently installed on this instance.
     *
     * @return A {@link WPPlugins} object.
     */
    public WPPlugins getPlugins()
    {
        return plugins;
    }

    /**
     * Fetch the collection of themes currently installed on this instance.
     *
     * @return A {@link WPThemes} object.
     */
    public WPThemes getThemes()
    {
        return themes;
    }
}
