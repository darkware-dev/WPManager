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

package org.darkware.wpman.events;

/**
 * This is a simple {@link WPEvent} indicating a request to perform (or at least attempt) a
 * WordPress core software update.
 *
 * @author jeff
 * @since 2016-04-10
 */
public class WPCoreUpdateRequest implements WPEvent
{
    private final boolean immediate;

    /**
     * Create a new update request.
     *
     * @param immediate A flag declaring whether the update should ignore normal time windows or not. A
     * {@code true} value indicates that the update will be attempted at the next possible moment, otherwise,
     * it will be scheduled to run within the configured time window.
     */
    public WPCoreUpdateRequest(final boolean immediate)
    {
        super();

        this.immediate = immediate;
    }

    /**
     * Checks to see if this request is for immediate execution, or if it should be delayed to occur within
     * the configured update time window.
     *
     * @return {@code true} if the request should be run immediately, {@code false} if it should be run within the
     * update window.
     */
    public boolean isImmediate()
    {
        return immediate;
    }
}
