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

/**
 * @author jeff
 * @since 2016-01-31
 */
public class WPSiteTheme extends WPDataComponent
{
    private final WPSite site;
    private WPTheme theme;

    public WPSiteTheme(final WPSite site)
    {
        super();

        this.site = site;
    }

    public WPTheme activeTheme()
    {
        this.checkRefresh();
        return this.theme;
    }

    @Override
    protected void refreshBaseData()
    {
        WPData.log.info("Loading active theme for site: {}", this.site.getBlogId());
        this.theme = this.getManager().getDataManager().getThemeForSite(this.site);
    }
}
