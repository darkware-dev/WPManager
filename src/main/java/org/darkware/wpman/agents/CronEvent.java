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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Objects;
import org.darkware.wpman.actions.WPCronHookExec;
import org.darkware.wpman.data.WPBlog;
import org.darkware.wpman.data.WPCronHook;
import org.darkware.wpman.util.serialization.MinimalBlogSerializer;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.util.Comparator;

/**
 * A helper class for storing {@link WPCronHook}s and associated objects.
 */
public class CronEvent
{
    @JsonSerialize(using = MinimalBlogSerializer.class)
    private final WPBlog blog;
    private final String hook;
    private final DateTime execTime;
    @JsonIgnore
    private WPCronHookExec action;

    /**
     * Creates a new event associating the blog, hook, and execution time.
     *
     * @param blog The {@link WPBlog} the hook executes against.
     * @param hook The name of the hook to execute.
     * @param execTime The requested execution time of the hook.
     */
    public CronEvent(final WPBlog blog, final String hook, final DateTime execTime)
    {
        super();

        this.blog = blog;
        this.hook = hook;
        this.execTime = execTime.withMillisOfSecond(0);
    }

    /**
     * Creates a new event associating the blog and a {@link WPCronHook} instance.
     *
     * @param blog The {@link WPBlog} the hook executes against.
     * @param cronHook The {@link WPCronHook} encapsulating the hook execution event.
     */
    public CronEvent(final WPBlog blog, final WPCronHook cronHook)
    {
        this(blog, cronHook.getHook(), cronHook.getNextRun());
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
     * @return {@code true} if the event is from the same blog and is reasonably close to the same
     * execution time, otherwise {@code false}.
     */
    public boolean reasonablyCloseTo(final CronEvent check)
    {
        // Check the blog
        if (!this.blog.equals(check.blog)) return false;

        // Check the time
        if (Math.abs(Seconds.secondsBetween(this.execTime, check.execTime).getSeconds()) > 15) return false;

        return true;
    }

    public WPBlog getBlog()
    {
        return blog;
    }

    public DateTime getExecTime()
    {
        return execTime;
    }

    public String getHook()
    {
        return hook;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof CronEvent)) return false;
        final CronEvent cronEvent = (CronEvent) o;
        return Objects.equal(blog, cronEvent.blog) &&
               Objects.equal(hook, cronEvent.hook) &&
               Objects.equal(execTime, cronEvent.execTime);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(blog, hook, execTime);
    }

    @Override
    public String toString()
    {
        return this.hook + "@" + this.execTime;
    }

    public static class ExecTimeComparator implements Comparator<CronEvent>
    {
        @Override
        public int compare(final CronEvent o1, final CronEvent o2)
        {
            return o1.getExecTime().compareTo(o2.getExecTime());
        }
    }
}
