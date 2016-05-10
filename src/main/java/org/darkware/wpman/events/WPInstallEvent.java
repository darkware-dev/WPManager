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
import org.darkware.wpman.data.WPPlugin;
import org.darkware.wpman.data.WPTheme;
import org.darkware.wpman.data.WPUpdatableComponent;

/**
 * A {@code WPInstallEvent} is broadcast whenever a {@link WPUpdatableComponent} is installed (including
 * updates).
 *
 * @author jeff
 * @since 2016-05-06
 */
public class WPInstallEvent<T extends WPUpdatableComponent> implements WPEvent
{
    /**
     * Create a {@link WPUpdateEvent} for the update of an item.
     *
     * @param item The item which was updated.
     * @param previousVersion The previously installed version.
     * @return A {@code WPUpdateEvent}.
     */
    public static WPUpdateEvent create(WPUpdatableComponent item, final Version previousVersion)
    {
        if (item instanceof WPPlugin)
        {
            return new WPPluginUpdateEvent((WPPlugin)item, previousVersion);
        }
        else if (item instanceof WPTheme)
        {
            return new WPThemeUpdateEvent((WPTheme)item, previousVersion);
        }

        throw new IllegalArgumentException("Unsupported item type: " + item.getClass());
    }

    /**
     * Create a {@link WPUpdateEvent} for the update of an item.
     *
     * @param item The item which was updated.
     * @return A {@code WPUpdateEvent}.
     */
    public static WPInstallEvent create(WPUpdatableComponent item)
    {
        if (item instanceof WPPlugin)
        {
            return new WPPluginInstallEvent((WPPlugin)item);
        }
        else if (item instanceof WPTheme)
        {
            return new WPThemeInstallEvent((WPTheme)item);
        }

        throw new IllegalArgumentException("Unsupported item type: " + item.getClass());
    }

    private final T item;

    /**
     * Create a new install event.
     *
     * @param item The item which was installed.
     */
    public WPInstallEvent(final T item)
    {
        super();

        this.item = item;
    }

    /**
     * Fetch the item which was installed.
     *
     * @return The installed item.
     */
    public T getItem()
    {
        return this.item;
    }
}
