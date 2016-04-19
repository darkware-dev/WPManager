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

import org.darkware.wpman.WPManager;
import org.darkware.wpman.actions.WPCronHookExec;
import org.darkware.wpman.data.WPBlog;
import org.darkware.wpman.data.WPCronHook;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author jeff
 * @since 2016-02-01
 */
public class WPCronAgentRoundRobin extends WPCronAgent
{
    private final int scanPeriod;
    private LocalDateTime nextScan;

    /**
     * Creates a new periodic round-robin {@link WPCronAgent}. The agent will only scan for
     * new hooks to run on the declared period. If the time to process a given scan exceeds
     * the period, the scan will run continuously.
     *
     * @param scanPeriod The number of seconds between scans.
     */
    public WPCronAgentRoundRobin(final int scanPeriod)
    {
        super();

        this.scanPeriod = scanPeriod;
    }

    /**
     * Creates a new periodic round-robin {@link WPCronAgent}. The agent will only scan for
     * new hooks to run on the default time period.
     *
     * @see #WPCronAgentRoundRobin(int)
     */
    public WPCronAgentRoundRobin()
    {
        this(300);
    }

    @Override
    protected void preBlogScan() throws InterruptedException
    {
        super.preBlogScan();

        this.nextScan = LocalDateTime.now().plusSeconds(this.scanPeriod);
    }

    @Override
    protected void postBlogScan() throws InterruptedException
    {
        super.postBlogScan();

        long millisToNextScan = Math.max(0, LocalDateTime.now().until(this.nextScan, ChronoUnit.MILLIS));
        Thread.sleep(millisToNextScan);
    }


    @Override
    protected void handleCronEvents(final WPBlog blog) throws InterruptedException
    {
        for (WPCronHook hook : blog.getCron().getWaitingHooks())
        {
            WPCronHookExec action = new WPCronHookExec(blog, hook);
            WPManager.log.info("Scheduling cron run for hook: {}::{}", blog.getDomain(), hook.getHook());
            this.getManager().scheduleAction(action);
        }
    }
}
