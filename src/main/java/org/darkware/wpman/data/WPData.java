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

import org.darkware.wpman.WPComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jeff
 * @since 2016-01-23
 */
public class WPData extends WPComponent
{
    protected static final Logger log = LoggerFactory.getLogger("Data");

    private final WPCore core;
    private final WPSites sites;
    private final WPPlugins plugins;
    private final WPThemes themes;

    public WPData()
    {
        super();

        this.core = new WPCore();
        this.sites = new WPSites();
        this.plugins = new WPPlugins();
        this.themes = new WPThemes();
    }

    public final void refresh()
    {
        this.core.refresh();
        this.sites.refresh();
        this.plugins.refresh();
        this.themes.refresh();
    }

    public WPCore getCore()
    {
        return core;
    }

    public WPSites getSites()
    {
        return sites;
    }

    public WPPlugins getPlugins()
    {
        return plugins;
    }

    public WPThemes getThemes()
    {
        return themes;
    }
}
