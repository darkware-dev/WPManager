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
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        // Get a set of all new plugin ids
        Set<String> newIds = rawList.stream().map(WPUpdatableComponent::getId).collect(Collectors.toSet());

        // Remove all current themes not in the new set
        this.themes.keySet().stream().filter(id -> !newIds.contains(id)).forEach(this.themes::remove);

        // Update all new theme data
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

    /**
     * Get a stream of the currently installed plugins.
     *
     * @return A {@link Stream} of {@link WPTheme}s.
     */
    public Stream<WPTheme> stream()
    {
        this.checkRefresh();
        return this.themes.values().stream();
    }
}
