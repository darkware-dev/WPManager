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

package org.darkware.wpman.config;

import org.darkware.wpman.data.WPUpdatableType;

/**
 * This is a configuration container for various configuration data controlling the themes that
 * are installed and how the list should be managed. Individual theme configuration is handled by
 * a collection of {@link ThemeConfig} objects.
 *
 * @author jeff
 * @since 2016-03-28
 */
public class ThemeListConfig extends UpdatableCollectionConfig<ThemeConfig>
{
    private Boolean defaultEnabled;

    /**
     * Create a new theme list configuration object. In order to be fully functional, it will need
     * to be registered with a {@link WordpressConfig} object via {@link #setWpConfig(WordpressConfig)}.
     */
    public ThemeListConfig()
    {
        super(WPUpdatableType.THEME);

        this.defaultEnabled = null;
    }

    @Override
    protected ThemeConfig defaultOverrides(final String itemId)
    {
        return new ThemeConfig(itemId);
    }

    /**
     * Fetch the default activation state for themes on the network.
     *
     * @return {@code true} if themes are activated by default, {@code false} if they are deactivated
     * by default, and {@code null} if no default policy should be used.
     */
    public Boolean getDefaultEnabled()
    {
        return this.defaultEnabled;
    }

    /**
     * Set the default activation state for themes on the network.
     *
     * @param defaultEnabled {@code true} if themes are activated by default, {@code false} if they
     * are deactivated by default, and {@code null} if no default policy should be used.
     */
    public void setDefaultEnabled(final Boolean defaultEnabled)
    {
        this.defaultEnabled = defaultEnabled;
    }
}
