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

import java.time.LocalDateTime;
import java.util.concurrent.Future;

/**
 * @author jeff
 * @since 2016-02-10
 */
public abstract class WPBasicAction<T> implements WPAction<T>
{
    protected final WPManager manager;
    private Integer timeout;
    private WPActionState state;
    protected Future<T> execFuture;

    protected final LocalDateTime creationTime;
    protected LocalDateTime startTime;
    protected LocalDateTime completionTime;

    public WPBasicAction()
    {
        super();

        this.manager = ContextManager.local().getContextualInstance(WPManager.class);
        this.state = WPActionState.INITIALIZING;

        this.creationTime = LocalDateTime.now();
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
    public LocalDateTime getCreationTime()
    {
        return this.creationTime;
    }

    @Override
    public LocalDateTime getStartTime()
    {
        return this.startTime;
    }

    @Override
    public LocalDateTime getCompletionTime()
    {
        return this.completionTime;
    }

    @Override
    public WPActionState getState()
    {
        return this.state;
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

    @Override
    public void registerFuture(final Future<T> future)
    {
        this.execFuture = future;
        this.state = WPActionState.SCHEDULED;
    }

    @Override
    public Future<T> getFuture()
    {
        return this.execFuture;
    }

    /**
     * Fetch a description of this action.
     *
     * @return The description as a {@code String}.
     */
    abstract public String getDescription();

    @Override
    public void cancel()
    {
        this.state = WPActionState.CANCELLED;
        this.completionTime = LocalDateTime.now();
        if (this.getFuture() != null && !this.getFuture().isCancelled())
        {
            this.getFuture().cancel(true);
        }
    }

    /**
     * Execute this action.
     *
     * @return The result of the action.
     */
    public abstract T exec();

    @Override
    public final T call() throws Exception
    {
        if (this.state.equals(WPActionState.CANCELLED)) return null;
        this.state = WPActionState.RUNNING;
        this.startTime = LocalDateTime.now();

        TimeoutCop<T> watchdog = null;
        if (this.hasTimeout() && this.execFuture != null)
        {
            watchdog = new TimeoutCop<>(this, this.execFuture);
            watchdog.start();
        }

        try
        {
            T returnValue = this.exec();
            this.state = WPActionState.COMPLETE;
            return returnValue;
        }
        catch (Throwable t)
        {
            this.state = WPActionState.ERROR;
            throw t;
        }
        finally
        {
            this.completionTime = LocalDateTime.now();
            if (watchdog != null && watchdog.isAlive()) watchdog.interrupt();
        }
    }
}
