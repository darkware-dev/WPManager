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
import org.darkware.wpman.data.WPBlog;
import org.darkware.wpman.data.WPBlogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@code WPCronAgent} is a thread-based agent which collects and executes WordPress
 * cron hook events in a continuous, daemon-like manner. An agent is attached to a
 * specific {@link WPManager} and interacts with that manager's data collection to
 * discover and analyze the hooks to run.
 *
 * @author jeff
 * @since 2016-02-01
 */
public abstract class WPCronAgent extends WPAgent
{
    /** The public logging facility for all agents. */
    protected final static Logger log = LoggerFactory.getLogger("Cron");

    private WPBlogs blogs;
    private final AtomicBoolean enabled;
    private final Map<CronEvent, ScheduledFuture> scheduledEvents;

    /**
     * Creates a new agent, attached to the {@link WPManager} from the current thread's
     * {@link ContextManager}.
     */
    public WPCronAgent()
    {
        super("cron");

        this.enabled = new AtomicBoolean(true);
        this.scheduledEvents = new ConcurrentHashMap<>();
    }

    public Set<CronEvent> getScheduledEvents()
    {
        return this.scheduledEvents.keySet();
    }

    public boolean isEventScheduled(final CronEvent event)
    {
        return this.scheduledEvents.containsKey(event);
    }

    public ScheduledFuture getScheduledFuture(final CronEvent event)
    {
        return this.scheduledEvents.get(event);
    }

    protected void addToSchedule(final CronEvent event, final ScheduledFuture future)
    {
        this.scheduledEvents.put(event, future);
    }

    /**
     * Fetch the collection of {@link WPBlog}s to inspect for cron hooks.
     *
     * @return A {@code WPBlogs} collection object.
     */
    public WPBlogs getBlogs()
    {
        if (this.blogs == null) this.blogs = this.getManager().getData().getBlogs();

        return this.blogs;
    }

    /**
     * Checks if the agent is currently enabled to execute cron events. If the agent is
     * not enabled, it will shut down at the next available moment.
     *
     * @return {@code true} if the agent is enabled for processing, {@code false} if it
     * is not and should shut down.
     */
    public boolean isEnabled()
    {
        return this.enabled.get();
    }

    /**
     * Cleans the cache of any hooks which have completed or are overly stale. Stale events
     * are forcefully canceled.
     */
    protected void cleanHookCache()
    {
        Set<CronEvent> expired = new HashSet<>();
        for (CronEvent event : this.getScheduledEvents())
        {
            ScheduledFuture future = this.getScheduledFuture(event);

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

    @Override
    public void executeAction()
    {
        try
        {
            while (this.enabled.get())
            {
                this.preBlogScan();
                for (WPBlog blog : this.getBlogs())
                {
                    if (!this.enabled.get())
                    {
                        WPCronAgent.log.info("Noticed shutdown request. Aborting cron processing.");
                        return;
                    }

                    WPCronAgent.log.debug("Processing cron events for {}", blog.getDomain());
                    this.handleCronEvents(blog);
                }
                this.postBlogScan();
            }
        }
        catch (InterruptedException e)
        {
            WPCronAgent.log.info("Noticed service interrupt. Aborting cron processing.");
        }
        finally
        {
            WPCronAgent.log.info("Cron processing is shut down.");
        }
    }

    /**
     * Performs any necessary operations prior to beginning a scan of blogs for cron events.
     * This is a popular place for performing initialization or bookkeeping prior to a blog scan.
     *
     * @throws InterruptedException If the thread is interrupted
     */
    protected void preBlogScan() throws InterruptedException
    {
        // Do nothing
    }

    /**
     * Performs any necessary operations following the completion a scan of blogs for cron events.
     * This is a popular place to put per-scan-round time delays or cleanup routines.
     *
     * @throws InterruptedException If the thread is interrupted
     */
    protected void postBlogScan() throws InterruptedException
    {
        // Do nothing
    }

    /**
     * Analyzes the given blog for cron events to run.
     *
     * @param blog The {@link WPBlog} to analyze.
     * @throws InterruptedException If the thread is interrupted
     */
    protected abstract void handleCronEvents(final WPBlog blog) throws InterruptedException;
}
