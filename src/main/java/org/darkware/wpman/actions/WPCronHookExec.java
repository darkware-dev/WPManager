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

import org.darkware.wpman.WPManager;
import org.darkware.wpman.data.WPCronHook;
import org.darkware.wpman.data.WPSite;
import org.darkware.wpman.wpcli.WPCLIFormat;

/**
 * @author jeff
 * @since 2016-01-28
 */
public class WPCronHookExec extends WPAction
{
    private final WPSite site;
    private final WPCronHook cron;

    public WPCronHookExec(final WPManager manager, final WPSite site, final WPCronHook cron)
    {
        super(manager, "cron", "event", "run", cron.getHook());

        this.site = site;
        this.cron = cron;

        this.getCommand().setSite(site);
        this.getCommand().loadThemes(false);
        this.getCommand().setFormat(WPCLIFormat.DEFAULT);
    }

    @Override
    protected String getDescription()
    {
        return "Execute cron hook [" + this.cron.getHook() + "] @ " + this.site.getDomain();
    }
}
