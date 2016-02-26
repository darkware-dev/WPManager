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

package org.darkware.wpman.actions;

import org.darkware.wpman.ContextManager;
import org.darkware.wpman.WPManager;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIFactory;

/**
 * @author jeff
 * @since 2016-02-10
 */
public abstract class WPBasicAction<T> implements WPAction<T>
{
    protected final WPManager manager;
    private Integer timeout;

    public WPBasicAction()
    {
        super();
        this.manager = ContextManager.local().getContextualInstance(WPManager.class);
    }

    /**
     * Fetch the {@link WPManager} associated with this action.
     *
     * @return A {@code WPManager} instance.
     */
    public WPManager getManager()
    {
        return manager;
    }

    /**
     * Fetch a suitable {@link WPCLIFactory} for creating {@link WPCLI} command.
     *
     * @return A pre-configured {@code WPCLIFactory} object.
     */
    public WPCLIFactory getWPCWpcliFactory()
    {
        return this.getManager().getBuilder();
    }

    /**
     * Request a timeout value for this action. Actions exceeding this runtime will be
     * cancelled.
     *
     * @param seconds The maximum number of seconds before the action is cancelled, or zero
     * if no timeout is desired.
     * @throws IllegalArgumentException If the timeout value is negative.
     */
    protected void requestTimeout(final int seconds)
    {
        if (seconds == 0) this.timeout = null;
        else if (seconds < 1) throw new IllegalArgumentException("Timeout cannot be negative.");
        this.timeout = new Integer(seconds);
    }

    @Override
    public boolean hasTimeout()
    {
        return (this.timeout != null);
    }

    @Override
    public int getTimeout()
    {
        if (this.hasTimeout()) return this.timeout;
        else return 0;
    }

    /**
     * Fetch a description of this action.
     *
     * @return The description as a {@code String}.
     */
    abstract public String getDescription();

    /**
     * Execute this action.
     *
     * @return The result of the action.
     */
    public abstract T exec();

    @Override
    public final T call() throws Exception
    {
        return this.exec();
    }
}
