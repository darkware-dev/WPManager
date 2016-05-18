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

package org.darkware.lazylib;

import org.eclipse.jetty.util.ConcurrentHashSet;

import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author jeff
 * @since 2016-05-16
 */
public abstract class LazyLoadedSet<T> extends LazyLoader implements Iterable<T>
{
    private final Set<T> values;

    /**
     * Create a new lazy loaded value handler. It will store a Set of the parameterized type. The value
     * will not be fetched until needed, and won't be fetched again until it expires. This particular value
     * does not automatically expire, but it can be made to manually expire.
     */
    public LazyLoadedSet()
    {
        this(null);
    }

    /**
     * Create a new lazy loaded value handler. It will store a Set of the parameterized type. The value
     * will not be fetched until needed, and won't be fetched again until it expires.
     *
     * @param ttl The amount of time the value should be allowed to be stored before the value automatically
     * expires.
     */
    public LazyLoadedSet(final Duration ttl)
    {
        super(ttl);

        this.values = new ConcurrentHashSet<>();
    }

    @Override
    public final void load() throws Exception
    {
        Collection<T> items = this.loadValues();

        this.values.removeIf(i -> !items.contains(i));
        items.stream().filter(i -> !this.values.add(i)).forEach(this.values::add);

        this.renew();
    }

    /**
     * Fetch the value. If the value has not been fetched or if the value has expired, a new copy will be
     * retrieved.
     *
     * @return A value of the declared type. The value may be {@code null} or any assignable subtype.
     */
    public final Set<T> values()
    {
        synchronized (this)
        {
            this.loadIfExpired();
            return this.values;
        }
    }

    @Override
    public final Iterator<T> iterator()
    {
        return this.values().iterator();
    }

    /**
     * Fetch a stream of the data map.
     *
     * @return A {@link Stream} object.
     */
    public Stream<T> stream()
    {
        return this.values().stream();
    }

    /**
     * Load the collection of map.
     *
     * @return A {@code Collection} of map.
     * @throws Exception If there is an error while loading the map.
     */
    protected abstract Collection<T> loadValues() throws Exception;
}
