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

import com.google.common.reflect.TypeToken;
import org.darkware.lazylib.LazyLoadedSet;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIFieldsOption;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A {@code WPCron} object acts as a general facade for inspecting cron events registered against a
 * specific blog.
 *
 * @author jeff
 * @since 2016-01-27
 */
public class WPCron extends WPComponent implements Iterable<WPCronHook>
{
    private final WPBlog blog;
    private final LazyLoadedSet<WPCronHook> hooks;

    /**
     * Create a new WPCron interface object for the given blog.
     *
     * @param blog The {@link WPBlog}
     */
    public WPCron(final WPBlog blog)
    {
        super();

        this.blog = blog;
        this.hooks = new LazyLoadedSet<WPCronHook>(Duration.ofMinutes(15))
        {
            @Override
            protected Collection<WPCronHook> loadValues() throws Exception
            {
                WPCLI eventListCmd = WPCron.this.buildCommand("cron", "event", "list");
                eventListCmd.loadThemes(false);
                eventListCmd.setBlog(WPCron.this.blog);
                eventListCmd.setOption(new WPCLIFieldsOption("hook", "next_run"));

                return eventListCmd.readJSON(new TypeToken<List<WPCronHook>>(){});
            }
        };
    }

    @Override
    public Iterator<WPCronHook> iterator()
    {
        return this.hooks.iterator();
    }

    /**
     * Fetch a list of cron hooks that are waiting to execute.
     *
     * @return A {@code List} of {@link WPCronHook} objects that are scheduled for execution in the future.
     */
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
