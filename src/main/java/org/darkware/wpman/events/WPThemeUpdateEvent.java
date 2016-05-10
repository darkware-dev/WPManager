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
import org.darkware.wpman.data.WPTheme;

/**
 * A {@code WPPluginUpdateEvent} is broadcast whenever an installed theme is updated.
 *
 * @author jeff
 * @since 2016-05-06
 */
public class WPThemeUpdateEvent extends WPUpdateEvent<WPTheme>
{
    /**
     * Create a new theme update event.
     *
     * @param theme The theme that was updated.
     * @param previousVersion The previously installed version.
     */
    public WPThemeUpdateEvent(final WPTheme theme, final Version previousVersion)
    {
        super(theme, previousVersion);
    }
}
