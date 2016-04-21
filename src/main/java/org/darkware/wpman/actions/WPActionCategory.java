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

/**
 * This enum is used to categorize the purpose or scope of {@link WPAction}s. The assigned
 * category should have no functional effect on the execution of the action, but it may be
 * used for reporting or priority assignment.
 *
 * @author jeff
 * @since 2016-04-20
 */
public enum WPActionCategory
{
    /** Actions that are performing routine maintenance. */
    MAINTENANCE,
    /** Actions to inspect, enforce or gather information on policy. */
    POLICY,
    /** Actions to find or fix security concerns. */
    SECURITY,
    /** Actions gathering internal data. */
    DATA,

    /** Actions to execute waiting cron hooks. */
    CRON,
    /** Actions to perform software installation or updates. */
    INSTALL,

    /** Other random actions. */
    OTHER;
}
