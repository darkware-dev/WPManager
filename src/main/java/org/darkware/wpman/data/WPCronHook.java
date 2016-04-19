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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import java.time.LocalDateTime;

/**
 * @author jeff
 * @since 2016-01-27
 */
public class WPCronHook implements Comparable<WPCronHook>
{
    private String hook;
    @JsonProperty("next_run") private LocalDateTime nextRun;

    public WPCronHook()
    {
        super();
    }

    public LocalDateTime getNextRun()
    {
        return this.nextRun;
    }

    protected void setNextRun(final LocalDateTime nextRun)
    {
        this.nextRun = nextRun;
    }

    public String getHook()
    {
        return hook;
    }

    protected void setHook(final String hook)
    {
        this.hook = hook;
    }

    public boolean isWaiting()
    {
        return (this.nextRun.isBefore(LocalDateTime.now()));
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof WPCronHook)) return false;
        final WPCronHook that = (WPCronHook) o;
        return Objects.equal(hook, that.hook);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(hook);
    }


    @Override
    public int compareTo(final WPCronHook that)
    {
        return this.hook.compareTo(that.hook);
    }
}
