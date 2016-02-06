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

import org.darkware.wpman.WPComponent;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author jeff
 * @since 2016-01-25
 */
public class WPActionService extends WPComponent
{
    protected static final Logger log = LoggerFactory.getLogger("Actions");

    private final ScheduledExecutorService execService;

    public WPActionService()
    {
        super();

        //TODO: Make this configurable
        this.execService = Executors.newScheduledThreadPool(4);
    }

    public void scheduleAction(final WPAction action)
    {
        this.execService.submit(action);
    }

    public ScheduledFuture scheduleAction(final WPAction action, Seconds delay)
    {
        return this.execService.schedule(action, delay.getSeconds(), TimeUnit.SECONDS);
    }

    public void shutdown()
    {
        try
        {
            WPActionService.log.info("Waiting for action service to complete any queued actions.");
            this.execService.shutdown();
            boolean allDone = this.execService.awaitTermination(30, TimeUnit.SECONDS);
            if (!allDone) WPActionService.log.info("Timeout exceeded. Forcing shutdown.");
        }
        catch (InterruptedException e)
        {
            WPActionService.log.warn("Shutdown wait terminated via thread interrupt. Forcing shutdown.");
        }

        this.execService.shutdownNow();
        WPActionService.log.info("Action service has shut down.");
    }
}
