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

import org.darkware.wpman.ContextManager;
import org.darkware.wpman.WPManager;
import org.darkware.wpman.WPManagerConfiguration;
import org.darkware.wpman.wpcli.WPCLI;

/**
 * A foundation class for all objects which act as primary components which might
 * initiate various external actions. This is meant to provide some basic facilities
 * for common actions that get used by many objects.
 *
 * @author jeff
 * @since 2016-01-25
 */
public class WPComponent
{
    private final transient WPManager manager;

    /**
     * Create a new component. This will implicitly attach a {@link WPManager} from the
     * current {@link ContextManager}.
     */
    public WPComponent()
    {
        super();

        this.manager = ContextManager.local().getContextualInstance(WPManager.class);
    }

    /**
     * The associated {@code WPManager}.
     *
     * @return A configured and initialized {@code WPManager} instance.
     */
    protected WPManager getManager()
    {
        return this.manager;
    }

    /**
     * Builds a new {@link WPCLI} command definition for the given command group and arguments. This
     * is built against the associated {@code WPManager} instance according to the active
     * {@link WPManagerConfiguration}. The {@code WPCLI} object will be ready for further configuration
     * or execution as needed.
     *
     * @param group The WP-CLI command group to invoke.
     * @param command The WP-CLI command or command cluster to invoke.
     * @param args The WP-CLI command arguments.
     * @return A {@code WPCLI} definition object.
     */
    public WPCLI buildCommand(final String group, final String command, final String ... args)
    {
        return this.manager.getBuilder().build(group, command, args);
    }
}
