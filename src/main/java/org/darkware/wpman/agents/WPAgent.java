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

package org.darkware.wpman.agents;

import org.darkware.wpman.ContextManager;
import org.darkware.wpman.WPManager;

/**
 * A {@code WPAgent} is a foundation class providing a basic implementation of an agent which
 * is scheduled for execution based on some asynchronous trigger.
 *
 * @author jeff
 * @since 2016-02-07
 */
public abstract class WPAgent implements Runnable
{
    private final String name;
    private final WPManager manager;

    /**
     * Create a new agent with the given name.
     *
     * @param name A display name for the agent.
     */
    public WPAgent(final String name)
    {
        super();

        this.name = name;
        this.manager = ContextManager.local().getContextualInstance(WPManager.class);
    }

    /**
     * Fetch the display name for this agent.
     *
     * @return The name as a {@code String}.
     */
    public final String getName()
    {
        return name;
    }

    /**
     * Fetch the {@code WPManager} associated with this agent.
     *
     * @return A {@code WPManager} instance.
     */
    public final WPManager getManager()
    {
        return manager;
    }

    @Override
    public final void run()
    {
        try
        {
            this.executeAction();
        }
        catch (Throwable t)
        {
            WPManager.log.warn("The agent '{}' terminated with an exception: {}", this.name, t.getLocalizedMessage(), t);
        }
    }

    /**
     * Perform the actions of this agent. This may be executed multiple times, but can
     * be assumed to never execute concurrently.
     */
    public abstract void executeAction();
}
