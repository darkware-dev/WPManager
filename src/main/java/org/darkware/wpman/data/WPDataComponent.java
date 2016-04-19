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

import org.darkware.wpman.WPComponent;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author jeff
 * @since 2016-01-23
 */
public abstract class WPDataComponent extends WPComponent
{
    private LocalDateTime nextRefresh;
    private Duration refreshDuration;

    public WPDataComponent()
    {
        super();

        this.expire();
        this.refreshDuration = this.loadRefreshDuration();
    }

    public void expire()
    {
        this.nextRefresh = LocalDateTime.now().minusSeconds(1);
    }

    protected Duration loadRefreshDuration()
    {
        return Duration.ofSeconds(60);
    }

    protected final void refresh()
    {
        synchronized (this)
        {
            this.refreshBaseData();
            this.nextRefresh = LocalDateTime.now().plus(this.refreshDuration);
        }
    }

    protected final void checkRefresh()
    {
        synchronized (this)
        {
            if (this.nextRefresh.isBefore(LocalDateTime.now())) this.refresh();
        }
    }

    protected abstract void refreshBaseData();
}
