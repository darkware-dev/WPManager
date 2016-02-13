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

package org.darkware.wpman.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author jeff
 * @since 2016-01-27
 */
public class WPCron extends WPDataComponent implements Iterable<WPCronHook>
{
    private final WPSite site;
    private final Set<WPCronHook> hooks;

    public WPCron(final WPSite site)
    {
        super();

        this.site = site;
        this.hooks = new ConcurrentSkipListSet<>();
    }

    @Override
    protected void refreshBaseData()
    {
        List<WPCronHook> hookData = this.getManager().getDataManager().getCronEvents(site);

        this.hooks.clear();
        if (hookData == null) return;

        for (WPCronHook hook : hookData)
        {
            WPData.log.debug("Loaded cron hook: {} @ {}", hook.getHook(), hook.getNextRun());
        }
        this.hooks.addAll(hookData);
    }

    @Override
    public Iterator<WPCronHook> iterator()
    {
        this.checkRefresh();
        return this.hooks.iterator();
    }

    public List<WPCronHook> getWaitingHooks()
    {
        List<WPCronHook> hooks = new ArrayList<>();

        for (WPCronHook hook : this)
        {
            if (hook.isWaiting()) hooks.add(hook);
        }

        return hooks;
    }

}
