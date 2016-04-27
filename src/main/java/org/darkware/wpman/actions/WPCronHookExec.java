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

import org.darkware.wpman.data.WPBlog;
import org.darkware.wpman.data.WPCronHook;
import org.darkware.wpman.wpcli.WPCLIFormat;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author jeff
 * @since 2016-01-28
 */
public class WPCronHookExec extends WPCLIAction
{
    private final Set<WPCronHook> hooks;

    public WPCronHookExec(final WPBlog blog, final WPCronHook cron)
    {
        super(WPActionCategory.CRON, blog, "cron", "event", "run", cron.getHook());

        this.hooks = new HashSet<>();
        this.addHook(cron);

        this.getCommand().setBlog(blog);
        this.getCommand().loadThemes(false);
        this.getCommand().setFormat(WPCLIFormat.DEFAULT);

        this.requestTimeout(20);
    }

    public void addHook(final WPCronHook hook)
    {
        this.hooks.add(hook);
    }

    @Override
    protected void beforeExec()
    {
        this.hooks.stream().map(h -> h.getHook()).forEach(this.getCommand()::addArgument);
    }

    @Override
    public String getDescription()
    {
        return this.hooks.stream().map(h -> h.getHook()).collect(Collectors.joining(", "));
    }
}
