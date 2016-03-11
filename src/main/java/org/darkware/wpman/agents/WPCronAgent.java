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
import org.darkware.wpman.data.WPSite;
import org.darkware.wpman.data.WPSites;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final WPSites sites;
    private final AtomicBoolean enabled;

    /**
     * Creates a new agent, attached to the {@link WPManager} from the current thread's
     * {@link ContextManager}.
     */
    public WPCronAgent()
    {
        super("cron");

        this.enabled = new AtomicBoolean(true);
        this.sites = this.getManager().getData().getSites();
    }

    /**
     * Fetch the collection of {@link WPSite}s to inspect for cron hooks.
     *
     * @return A {@code WPSites} collection object.
     */
    public WPSites getSites()
    {
        return sites;
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

    @Override
    public void executeAction()
    {
        try
        {
            while (this.enabled.get())
            {
                this.preSiteScan();
                for (WPSite site : this.sites)
                {
                    if (!this.enabled.get())
                    {
                        WPCronAgent.log.info("Noticed shutdown request. Aborting cron processing.");
                        return;
                    }

                    WPCronAgent.log.debug("Processing cron events for {}", site.getDomain());
                    this.handleCronEvents(site);
                }
                this.postSiteScan();
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
     * Performs any necessary operations prior to beginning a scan of sites for cron events.
     * This is a popular place for performing initialization or bookkeeping prior to a site scan.
     *
     * @throws InterruptedException If the thread is interrupted
     */
    protected void preSiteScan() throws InterruptedException
    {
        // Do nothing
    }

    /**
     * Performs any necessary operations following the completion a scan of sites for cron events.
     * This is a popular place to put per-scan-round time delays or cleanup routines.
     *
     * @throws InterruptedException If the thread is interrupted
     */
    protected void postSiteScan() throws InterruptedException
    {
        // Do nothing
    }

    /**
     * Analyzes the given site for cron events to run.
     *
     * @param site The {@link WPSite} to analyze.
     * @throws InterruptedException If the thread is interrupted
     */
    protected abstract void handleCronEvents(final WPSite site) throws InterruptedException;
}
