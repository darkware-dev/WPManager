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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author jeff
 * @since 2016-01-23
 */
public class WPPlugins extends WPDataComponent implements Iterable<WPPlugin>
{
    private final Map<String, WPPlugin> plugins;

    public WPPlugins()
    {
        super();

        this.plugins = new ConcurrentSkipListMap<>();
    }

    @Override
    protected void refreshBaseData()
    {
        List<WPPlugin> rawList = this.getManager().getDataManager().getPlugins();

        for (WPPlugin plug : rawList)
        {
            WPData.log.debug("Loaded plugin info: {}", plug.getId());
            this.plugins.put(plug.getId(), plug);
        }
    }

    @Override
    public Iterator<WPPlugin> iterator()
    {
        return this.plugins.values().iterator();
    }
}
