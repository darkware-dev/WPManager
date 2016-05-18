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

import org.darkware.lazylib.LazyLoadedMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A {@code WPDataCollection} is an intermediate level of convenient classes
 *
 * @author jeff
 * @since 2016-02-15
 */
public abstract class WPUpdatableCollection<T extends WPUpdatableComponent> extends WPComponent implements Iterable<T>
{
    private final String collectionName;
    private final LazyLoadedMap<String, T> internalList;

    /**
     * Create a new collection of updatable items.
     *
     * @param collectionName An identifiable name for this collection. This is commonly used in logging or
     * various outputs to identify the collection being used.
     */
    public WPUpdatableCollection(final String collectionName)
    {
        super();

        this.collectionName = collectionName;
        this.internalList = new LazyLoadedMap<String, T>()
        {
            @Override
            protected Map<String, T> loadValues() throws Exception
            {
                WPInstance.log.info("Refreshing collection: {}", WPUpdatableCollection.this.collectionName);
                List<T> rawList = WPUpdatableCollection.this.fetchNewItems();

                Map<String, T> freshItems = new HashMap<>();
                rawList.forEach(i -> freshItems.put(i.getId(), i));
                return freshItems;
            }
        };
    }

    /**
     * Declare that the current list is out-of-date, triggering a reload of the data the next time
     * any code asks for the collection.
     */
    public final void expire()
    {
        this.internalList.expire();
    }

    /**
     * Fetch an updated copy of the collection from the WordPress instance.
     *
     * @return A {@link List} of items.
     */
    protected abstract List<T> fetchNewItems();

    /**
     * Check to see if an item with the given ID is included in this collection.
     *
     * @param pluginId The item's unique ID string
     * @return {@code true} if the item is included, {@code false} if it's not found.
     */
    public final boolean isInstalled(final String pluginId)
    {
        return this.internalList.map().containsKey(pluginId);
    }

    /**
     * Fetch the plugin with the given id.
     *
     * @param pluginId The plugin id to search for.
     * @return A {@link WPPlugin}, or {@code null} if the plugin isn't found.
     */
    public final T get(final String pluginId)
    {
        return this.internalList.map().get(pluginId);
    }

    @Override
    public final Iterator<T> iterator()
    {
        return this.internalList.iterator();
    }

    /**
     * Get a stream of the collection items.
     *
     * @return A {@link Stream} of items.
     */
    public final Stream<T> stream()
    {
        return this.internalList.stream();
    }
}
