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
 * A configuration container for a single installed theme on the WordPress instance.
 *
 * @author jeff
 * @since 2016-03-28
 */
public class ThemeConfig extends UpdatableConfig
{
    private Boolean enabled;

    /**
     * Creates a new theme configuration container with default settings.
     */
    public ThemeConfig()
    {
        super();

        this.enabled = null;
    }

    /**
     * Creates a new theme configuration container with the given ID.
     *
     * @param id The theme ID for the configuration.
     */
    public ThemeConfig(final String id)
    {
        this();
    }

    /**
     * Check to see if this theme should be enabled on the network.
     *
     * @return {@code true} if the theme should be enabled for network use, {@code false} if it should
     * be denied for all use on the network, and {@code null} if the default policy should be used.
     */
    public Boolean getEnabled()
    {
        return this.enabled;
    }

    /**
     * Sets the network policy for this theme.
     *
     * @param enabled {@code true} if the theme should be enabled for network use, {@code false} if it should
     * be denied for all use on the network, and {@code null} if the default policy should be used.
     */
    public void setEnabled(final Boolean enabled)
    {
        this.enabled = enabled;
    }
}
