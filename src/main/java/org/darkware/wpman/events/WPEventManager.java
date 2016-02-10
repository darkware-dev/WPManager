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

package org.darkware.wpman.events;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jeff
 * @since 2016-02-09
 */
public class WPEventManager
{
    /** A shared logger for event-handling-related log messages. */
    protected static final Logger log = LoggerFactory.getLogger("Events");

    private final EventBus eventBus;

    /**
     * Create a new event manager that dispatches events to registered objects.
     */
    public WPEventManager()
    {
        super();

        this.eventBus = new EventBus();
    }

    /**
     * Register a subscriber for {@code WPEvent} dispatches. This implementation uses
     * Guava's {@link EventBus} as a backing library, so subscribing objects should
     * annotate listening events with the {@code @Subscribe} annotation.
     *
     * @param subscriber The object to register for event dispatches.
     * @see Subscribe
     */
    public void register(final Object subscriber)
    {
        this.eventBus.register(subscriber);
    }

    /**
     * Submit an event to dispatch to any listening objects.
     *
     * @param event The event to place on the bus.
     */
    public void dispatch(final WPEvent event)
    {
        this.eventBus.post(event);
    }
}
