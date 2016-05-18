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

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * This is a base implementation of a lazy loading helper class. This class is mostly useless by itself,
 * but it simplifies and normalizes the creation of child classes for lazy loading particular types of
 * data.
 *
 * @author jeff
 * @since 2016-05-16
 */
public abstract class LazyLoader
{
    private final Duration ttl;
    private LocalDateTime expiration;

    public LazyLoader(final Duration ttl)
    {
        super();

        this.ttl = ttl;
        this.expiration = null;
    }

    public abstract void load() throws Exception;

    /**
     * Force the expiration of the value. Following this call, the next call to retrieve the data will
     * trigger a fresh fetch of the data.
     */
    public final void expire()
    {
        synchronized (this)
        {
            this.expiration = null;
        }
    }

    /**
     * Checks if the lazy loaded data is expired or not. Data that has never been loaded is considered to
     * be expired.
     *
     * @return {@code true} if the data is expired or never loaded, otherwise {@code false}.
     */
    public final boolean isExpired()
    {
        return this.expiration == null || this.expiration.isBefore(LocalDateTime.now());
    }

    /**
     * Extend the expiration another generation.
     */
    protected void renew()
    {
        if (ttl == null) this.expiration = LocalDateTime.MAX;
        else this.expiration = LocalDateTime.now().plus(this.ttl);
    }

    /**
     * Load the data if it's expired.
     */
    protected final void loadIfExpired()
    {
        if (this.isExpired())
        {
            try
            {
                this.load();
            }
            catch (Throwable t)
            {
                this.reportLoadError(t);
            }
        }
    }

    /**
     * Report any errors encountered while trying to fetch the backend value.
     *
     * @param t The {@link Throwable} which was caught during value loading.
     */
    protected void reportLoadError(final Throwable t)
    {
        // Do nothing by default.
    }
}
