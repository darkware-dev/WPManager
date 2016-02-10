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

package org.darkware.wpman.cron;

import org.darkware.wpman.WPManager;
import org.darkware.wpman.actions.WPCronHookExec;
import org.darkware.wpman.data.WPCronHook;
import org.darkware.wpman.data.WPSite;
import org.joda.time.DateTime;

/**
 * @author jeff
 * @since 2016-02-01
 */
public class WPCronAgentRoundRobin extends WPCronAgent
{
    private final int scanPeriod;
    private DateTime nextScan;

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
    protected void preSiteScan() throws InterruptedException
    {
        super.preSiteScan();

        this.nextScan = DateTime.now().plusSeconds(this.scanPeriod);
    }

    @Override
    protected void postSiteScan() throws InterruptedException
    {
        super.postSiteScan();

        long millisToNextScan = Math.max(0, this.nextScan.getMillis() - DateTime.now().getMillis());
        Thread.sleep(millisToNextScan);
    }


    @Override
    protected void handleCronEvents(final WPSite site) throws InterruptedException
    {
        for (WPCronHook hook : site.getCron().getWaitingHooks())
        {
            WPCronHookExec action = new WPCronHookExec(site, hook);
            WPManager.log.info("Scheduling cron run for hook: {}::{}", site.getDomain(), hook.getHook());
            this.getManager().scheduleAction(action);
        }
    }
}
