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
 * A {@code WPStartupEvent} is broadcast when the {@code WPManager} completes its startup. At that
 * point, all services should be initialized, all agents should be started or scheduled, an all required
 * components should be registered in the {@code ContextManager}.
 *
 * @author jeff
 * @since 2016-03-13
 */
public class WPStartupEvent implements WPEvent
{
}
