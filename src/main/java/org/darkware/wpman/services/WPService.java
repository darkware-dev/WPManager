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

package org.darkware.wpman.services;

import org.darkware.wpman.ContextManager;
import org.darkware.wpman.WPManager;
import org.darkware.wpman.WPManagerConfiguration;
import org.darkware.wpman.events.WPEvent;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@code WPService} is an interface to a set of shared capabilities which are triggered
 * by external events. This is usually done either by {@link WPEvent WPEvents} or direct
 * method invocation after retrieving an instance from the {@link ContextManager}.
 * <p>
 * {@code WPService WPServices} automatically subscribe to the event bus used by their
 * attached {@code WPManager}.
 *
 * @author jeff
 * @since 2016-03-12
 */
public abstract class WPService
{
    private final WPManager manager;
    private final WPManagerConfiguration config;
    private final AtomicBoolean active;

    public WPService()
    {
        super();

        this.manager = ContextManager.local().getContextualInstance(WPManager.class);
        this.config = ContextManager.local().getContextualInstance(WPManagerConfiguration.class);
        this.active = new AtomicBoolean(false);
    }

    /**
     * Fetch the {@code WPManager} attached to this service.
     *
     * @return A local {@code WPManager}.
     */
    protected final WPManager getManager()
    {
        return manager;
    }

    /**
     * Fetch the {@code Config} attached to this service.
     *
     * @return A {@code Config} collection.
     */
    protected final WPManagerConfiguration getConfig()
    {
        return config;
    }

    /**
     * Activates this service and prepares it to handle external events.
     *
     * @return {@code true} if the service was activated, {@code false} if it was not.
     */
    public final boolean activate()
    {
        synchronized (this.active)
        {
            if (active.get()) return true;
            try
            {
                this.beforeActivation();

                this.getManager().registerForEvents(this);

                this.active.set(true);
                return true;
            }
            catch (Throwable t)
            {
                this.active.set(false);
                return false;
            }
        }
    }

    protected void beforeActivation()
    {
        // Nothing to do by default
    }
}
