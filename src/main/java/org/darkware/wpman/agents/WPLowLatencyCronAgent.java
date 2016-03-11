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

import com.google.common.base.Objects;
import org.darkware.wpman.actions.WPCronHookExec;
import org.darkware.wpman.data.WPCronHook;
import org.darkware.wpman.data.WPSite;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
    private final Map<CronEvent, ScheduledFuture> scheduledEvents;
    private DateTime nextScan;

    /**
     * Creates a new {@link WPCronAgent} which attempts to execute cron hooks with a
     * minimum delay from the time it is scheduled to run.
     */
    public WPLowLatencyCronAgent()
    {
        super();

        this.scheduledEvents = new HashMap<>();
        this.cronExecutor = Executors.newScheduledThreadPool(8);
    }

    @Override
    protected void handleCronEvents(final WPSite site) throws InterruptedException
    {
        DateTime now = DateTime.now();
        for (WPCronHook hook : site.getCron())
        {
            CronEvent event = new CronEvent(site, hook);
            if (this.scheduledEvents.containsKey(event)) continue;

            ScheduledFuture future = null;

            // Look for an existing event to group with
            CronEvent matching = null;
            for (CronEvent e : scheduledEvents.keySet())
            {
                if (e.reasonablyCloseTo(event))
                {
                    matching = e;
                    break;
                }
            }

            if (matching == null)
            {
                WPCronHookExec action = new WPCronHookExec(site, hook);
                event.attachAction(action);
                int secondsUntilExec = Seconds.secondsBetween(now, hook.getNextRun()).getSeconds();
                future = this.cronExecutor.schedule(action, secondsUntilExec, TimeUnit.SECONDS);
            }
            else
            {
                WPCronAgent.log.info("Found existing similar hook: {}", matching);
                WPCronHookExec action = matching.getAction();
                event.attachAction(action);
                future = this.scheduledEvents.get(matching);
            }

            WPCronAgent.log.info("Scheduling cron hook: {}::{} @ {}", site.getSubDomain(), hook.getHook(), hook.getNextRun());

            // Don't schedule the event again
            this.scheduledEvents.put(event, future);
        }
    }

    @Override
    protected void preSiteScan() throws InterruptedException
    {
        super.preSiteScan();

        this.nextScan = DateTime.now().plusMinutes(5);
    }

    @Override
    protected void postSiteScan() throws InterruptedException
    {
        super.postSiteScan();

        this.cleanHookCache();

        long millisToNextScan = Math.max(0, this.nextScan.getMillis() - DateTime.now().getMillis());
        Thread.sleep(millisToNextScan);
    }

    /**
     * Cleans the cache of any hooks which have completed or are overly stale. Stale events
     * are forcefully canceled.
     */
    protected void cleanHookCache()
    {
        Set<CronEvent> expired = new HashSet<>();
        for (CronEvent event : this.scheduledEvents.keySet())
        {
            ScheduledFuture future = this.scheduledEvents.get(event);

            // Check for overly stale events
            if (future.getDelay(TimeUnit.SECONDS) < -120)
            {
                future.cancel(true);
            }

            if (future.isCancelled()) expired.add(event);
            else if (future.isDone()) expired.add(event);
        }

        expired.stream().forEach(this.scheduledEvents::remove);
    }

    /**
     * A helper class for storing {@link WPCronHook}s and associated objects.
     */
    private static class CronEvent
    {
        private final WPSite site;
        private final String hook;
        private final DateTime execTime;
        private WPCronHookExec action;

        /**
         * Creates a new event associating the site, hook, and execution time.
         *
         * @param site The {@link WPSite} the hook executes against.
         * @param hook The name of the hook to execute.
         * @param execTime The requested execution time of the hook.
         */
        public CronEvent(final WPSite site, final String hook, final DateTime execTime)
        {
            super();

            this.site = site;
            this.hook = hook;
            this.execTime = execTime.withMillisOfSecond(0);
        }

        /**
         * Creates a new event associating the site and a {@link WPCronHook} instance.
         *
         * @param site The {@link WPSite} the hook executes against.
         * @param cronHook The {@link WPCronHook} encapsulating the hook execution event.
         */
        public CronEvent(final WPSite site, final WPCronHook cronHook)
        {
            this(site, cronHook.getHook(), cronHook.getNextRun());
        }

        /**
         * Attach a {@code WPCronHookExec} action to this event.
         *
         * @param action The action that executes this event.
         */
        public void attachAction(final WPCronHookExec action)
        {
            synchronized (this)
            {
                if (this.action != null) throw new IllegalStateException("Attempted to attach an action to an already attached event.");

                this.action = action;
            }
        }

        /**
         * Fetch the {@code WPCronHookExec} action that is registered to execute this event.
         *
         * @return The {@code WPAction} that executes this event.
         */
        public WPCronHookExec getAction()
        {
            return this.action;
        }

        /**
         * Checks to see if a given {@code CronEvent} is reasonably close to this event. Presumably
         * this would be used to decide if the other event could piggyback onto the same execution action.
         *
         * @param check The {@code CronEvent} to check against.
         * @return {@code true} if the event is from the same site and is reasonably close to the same
         * execution time, otherwise {@code false}.
         */
        public boolean reasonablyCloseTo(final CronEvent check)
        {
            // Check the site
            if (!this.site.equals(check.site)) return false;

            // Check the time
            if (Math.abs(Seconds.secondsBetween(this.execTime, check.execTime).getSeconds()) > 15) return false;

            return true;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) return true;
            if (!(o instanceof CronEvent)) return false;
            final CronEvent cronEvent = (CronEvent) o;
            return Objects.equal(site, cronEvent.site) &&
                   Objects.equal(hook, cronEvent.hook) &&
                   Objects.equal(execTime, cronEvent.execTime);
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode(site, hook, execTime);
        }

        @Override
        public String toString()
        {
            return this.hook + "@" + this.execTime;
        }
    }
}
