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
public class WPThemes extends WPDataComponent implements Iterable<WPTheme>
{
    private final Map<String, WPTheme> themes;

    public WPThemes()
    {
        super();

        this.themes = new ConcurrentSkipListMap<>();
    }

    @Override
    protected void refreshBaseData()
    {
        List<WPTheme> rawList = this.getManager().getDataManager().getThemes();

        for (WPTheme plug : rawList)
        {
            WPData.log.debug("Loaded themes info: {}", plug.getId());
            this.themes.put(plug.getId(), plug);
        }
    }

    @Override
    public Iterator<WPTheme> iterator()
    {
        return this.themes.values().iterator();
    }
}
