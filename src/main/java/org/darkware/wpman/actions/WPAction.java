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

import org.darkware.wpman.WPManager;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIError;

/**
 * A {@code WPAction} is an executable action suitable for scheduling on a {@link WPActionService}.
 *
 * @author jeff
 * @since 2016-01-28
 */
public abstract class WPAction implements Runnable
{
    private final WPManager manager;
    private final WPCLI command;

    /**
     * Creates a new {@code WPAction}.
     *
     * @param manager The manager to associate with this action.
     * @param actionGroup The WP-CLI command group
     * @param command The WP-CLI command
     * @param args Extra command arguments
     */
    public WPAction(final WPManager manager, final String actionGroup, final String command, final String ... args)
    {
        super();

        this.manager = manager;

        this.command = manager.getBuilder().build(actionGroup, command, args);
    }

    public WPManager getManager()
    {
        return manager;
    }

    public WPCLI getCommand()
    {
        return command;
    }

    abstract protected String getDescription();

    @Override
    public void run()
    {
        WPActionService.log.info("Starting action: {}", this.getDescription());

        try
        {
            this.command.execute();
            WPActionService.log.info("Completed action: {}", this.getDescription());
        }
        catch (WPCLIError error)
        {
            WPActionService.log.error("Error running action: {}", error.getLocalizedMessage());
        }
    }
}
