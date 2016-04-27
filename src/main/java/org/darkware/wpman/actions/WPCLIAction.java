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
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIError;

/**
 * A {@code WPAction} is an executable action suitable for scheduling on a {@link WPActionService}.
 *
 * @author jeff
 * @since 2016-01-28
 */
public abstract class WPCLIAction extends WPBasicAction<Boolean>
{
    private final WPCLI command;

    /**
     * Creates a new {@code WPAction}.
     *
     * @param category The {@link WPActionCategory} for this action.
     * @param blog The {@link WPBlog} this action is associated with, or {@code null} if no particular
     * blog is linked.
     * @param actionGroup The WP-CLI command group
     * @param command The WP-CLI command
     * @param args Extra command arguments
     */
    public WPCLIAction(final WPActionCategory category, final WPBlog blog, final String actionGroup, final String command, final String ... args)
    {
        super(category, blog);

        this.command = this.getManager().getBuilder().build(actionGroup, command, args);
    }

    public WPCLI getCommand()
    {
        return command;
    }

    /**
     * Perform any last-millisecond changes or additions to the action or command before
     * it is executed.
     */
    protected void beforeExec()
    {
        // Nothing to do by default
    }

    @Override
    public Boolean exec()
    {
        try
        {
            this.command.execute();
            return true;
        }
        catch (WPCLIError error)
        {
            return false;
        }
    }
}
