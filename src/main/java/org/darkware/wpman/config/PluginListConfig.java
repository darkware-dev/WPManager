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
 * This is a configuration container for various configuration data controlling the plugins that
 * are installed and how the list should be managed. Individual plugin configuration is handled by
 * a collection of {@link PluginConfig} objects.
 *
 * @author jeff
 * @since 2016-03-28
 */
public class PluginListConfig extends UpdatableCollectionConfig<PluginConfig>
{
    /**
     * Create a new plugin list configuration container.
     */
    public PluginListConfig()
    {
        super(WPUpdatableType.PLUGIN);
    }

    @Override
    protected PluginConfig defaultOverrides(final String itemId)
    {
        return new PluginConfig(itemId);
    }
}
