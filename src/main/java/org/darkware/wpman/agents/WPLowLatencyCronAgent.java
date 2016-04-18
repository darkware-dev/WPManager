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

import org.darkware.wpman.actions.WPCronHookExec;
import org.darkware.wpman.data.WPBlog;
import org.darkware.wpman.data.WPCronHook;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * This {@link WPCronAgent} is designed to execute cron hooks in a manner that
 * minimizes the delay between the moment the hook is scheduled to be run and
 * the time when the hook is actually executed.
 *
 * <p>Other agents and external systems often employ periodic checks which run
 * all available hooks at the moment, then sleep for a period of time before
 * checking again. While this works, it can produce execution spikes and implicitly
 * induces delays in scheduled execution.
 * </p>
 * <p>This agent keeps track of upcoming requests and schedules them to be run
 * asynchronously on the minute of their scheduling. The result is a natural
 * distribution of cron executions that should avoid the concentrated spiking of
 * a traditional agent while keeping a low database query load and very responsive
 * execution of hooks.
 * </p>
 *
 * @author jeff
 * @since 2016-02-02
 */
public class WPLowLatencyCronAgent extends WPCronAgent
{
    private final ScheduledExecutorService cronExecutor;
    private DateTime nextScan;

    /**
     * Creates a new {@link WPCronAgent} which attempts to execute cron hooks with a
     * minimum delay from the time it is scheduled to run.
     */
    public WPLowLatencyCronAgent()
    {
        super();

        this.cronExecutor = Executors.newScheduledThreadPool(8);
    }

    @Override
    protected void handleCronEvents(final WPBlog blog) throws InterruptedException
    {
        DateTime now = DateTime.now();
        for (WPCronHook hook : blog.getCron())
        {
            CronEvent event = new CronEvent(blog, hook);
            if (this.isEventScheduled(event)) continue;

            ScheduledFuture future = null;

            // Look for an existing event to group with
            CronEvent matching = null;
            for (CronEvent e : this.getScheduledEvents())
            {
                if (e.reasonablyCloseTo(event))
                {
                    matching = e;
                    break;
                }
            }

            if (matching == null)
            {
                WPCronHookExec action = new WPCronHookExec(blog, hook);
                event.attachAction(action);
                int secondsUntilExec = Seconds.secondsBetween(now, hook.getNextRun()).getSeconds();
                future = this.getManager().scheduleAction(action, secondsUntilExec);
            }
            else
            {
                WPCronAgent.log.info("Found existing similar hook: {}", matching);
                WPCronHookExec action = matching.getAction();
                event.attachAction(action);
                future = this.getScheduledFuture(matching);
            }

            WPCronAgent.log.info("Scheduling cron hook: {}::{} @ {}", blog.getSubDomain(), hook.getHook(), hook.getNextRun());

            // Don't schedule the event again
            this.addToSchedule(event, future);
        }
    }

    @Override
    protected void preBlogScan() throws InterruptedException
    {
        super.preBlogScan();

        this.nextScan = DateTime.now().plusMinutes(5);
    }

    @Override
    protected void postBlogScan() throws InterruptedException
    {
        super.postBlogScan();

        this.cleanHookCache();

        long millisToNextScan = Math.max(0, this.nextScan.getMillis() - DateTime.now().getMillis());
        Thread.sleep(millisToNextScan);
    }



}
