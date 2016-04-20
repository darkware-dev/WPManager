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

/**
 *
 *
 * @author jeff
 * @since 2016-03-28
 */
public class ThemeListConfig extends UpdatableCollectionConfig<ThemeConfig>
{
    private Boolean defaultEnabled;

    public ThemeListConfig()
    {
        super();

        this.defaultEnabled = null;
    }

    @Override
    protected ThemeConfig defaultDetails(final String itemId)
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
