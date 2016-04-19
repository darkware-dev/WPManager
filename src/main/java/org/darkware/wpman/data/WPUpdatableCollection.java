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
 * A {@code WPDataCollection} is an intermediate level of convenient classes
 *
 * @author jeff
 * @since 2016-02-15
 */
public abstract class WPUpdatableCollection<T extends WPUpdatableComponent> extends WPDataComponent implements Iterable<T>
{
    private final String collectionName;
    private final Map<String, T> internalList;

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
        this.internalList = new ConcurrentSkipListMap<>();
    }

    @Override
    protected final void refreshBaseData()
    {
        WPData.log.info("Refreshing collection: {}", this.collectionName);
        List<T> rawList = this.fetchNewItems();

        // Get a set of all new plugin ids
        Set<String> newIds = rawList.stream().map(WPUpdatableComponent::getId).collect(Collectors.toSet());

        // Remove all current plugins not in the new set
        this.internalList.keySet().removeIf(id -> !newIds.contains(id));

        // Update all new plugin data
        rawList.stream().forEach(p -> this.internalList.put(p.getId(), p));
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
        this.checkRefresh();
        return this.internalList.containsKey(pluginId);
    }

    /**
     * Fetch the plugin with the given id.
     *
     * @param pluginId The plugin id to search for.
     * @return A {@link WPPlugin}, or {@code null} if the plugin isn't found.
     */
    public final T get(final String pluginId)
    {
        this.checkRefresh();
        return this.internalList.get(pluginId);
    }


    @Override
    public final Iterator<T> iterator()
    {
        this.checkRefresh();
        return this.internalList.values().iterator();
    }

    /**
     * Get a stream of the collection items.
     *
     * @return A {@link Stream} of items.
     */
    public final Stream<T> stream()
    {
        this.checkRefresh();
        return this.internalList.values().stream();
    }
}
