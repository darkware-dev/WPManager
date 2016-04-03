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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author jeff
 * @since 2016-03-31
 */
final class TimeoutCop<T> extends Thread
{
    private final WPAction<T> action;
    private final Future<T> actionFuture;

    public TimeoutCop(final WPAction<T> action, final Future<T> actionFuture)
    {
        super();

        this.action = action;
        this.actionFuture = actionFuture;
    }

    public void run()
    {
        try
        {
            if (action.hasTimeout())
            {
                this.actionFuture.get(action.getTimeout(), TimeUnit.SECONDS);
            }
        }
        catch (TimeoutException e)
        {
            if (!this.action.getState().isComplete())
            {
                WPActionService.log.warn("Canceled action due to timeout: " + this.action.getDescription());
                this.action.cancel();
            }
        }
        catch (InterruptedException e)
        {
            if (!this.action.getState().isComplete())
            {
                WPActionService.log.warn("Canceled action due to interrupt: " + this.action.getDescription());
                this.action.cancel();
            }
        }
        catch (ExecutionException e)
        {
            WPActionService.log.warn("Timeout canceled due to action termination: " + this.action.getDescription());
        }
        catch (Throwable t)
        {
            WPActionService.log.warn(
                    "Timeout canceled due to abnormal error: " + this.action.getDescription() + " (" +
                    t.getLocalizedMessage() + ")");
        }
    }
}
