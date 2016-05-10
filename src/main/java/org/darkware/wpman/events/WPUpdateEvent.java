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

import org.darkware.wpman.data.Version;
import org.darkware.wpman.data.WPUpdatableComponent;

/**
 * A {@link WPUpdateEvent} is broadcast whenever an {@link WPUpdatableComponent} is updated on the
 * instance.
 *
 * @author jeff
 * @since 2016-05-06
 */
public abstract class WPUpdateEvent<T extends WPUpdatableComponent> extends WPInstallEvent<T>
{
    private final Version previousVersion;

    /**
     * Create a new update event.
     *
     * @param item The item which was updated.
     * @param previousVersion The previously installed version.
     */
    public WPUpdateEvent(final T item, final Version previousVersion)
    {
        super(item);

        this.previousVersion = previousVersion;
    }

    /**
     * Fetch the previously installed item version. This should be assumed to be different from
     * the version of the installed item.
     *
     * @return The previous {@link Version}.
     */
    public Version getPreviousVersion()
    {
        return this.previousVersion;
    }
}
