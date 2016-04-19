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
import org.darkware.wpman.actions.WPCoreUpdate;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

/**
 *
 *
 * @author jeff
 * @since 2016-02-09
 */
public class WPCoreUpdateAgent extends WPPeriodicAgent
{
    private ScheduledFuture<Boolean> waitingFuture;

    /**
     * Create a plugin synchronization agent.
     */
    public WPCoreUpdateAgent()
    {
        super("Core Update Check", Duration.ofHours(4));
    }


    @Override
    public void executeAction()
    {
        if (this.waitingFuture != null)
        {
            if (this.waitingFuture.isCancelled()) this.waitingFuture = null;
            else if (this.waitingFuture.isDone()) this.waitingFuture = null;
            else
            {
                // We've got a task waiting to execute.
                return;
            }
        }

        if (this.getManager().getData().getCore().hasUpdate())
        {
            WPManager.log.info("Scheduling a core update in the core update window.");
            this.waitingFuture = this.getManager().scheduleAction(new WPCoreUpdate(), this.getManager().getConfig().getWordpress().getCoreUpdateWindow());
        }
    }
}
