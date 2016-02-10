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

/**
 * @author jeff
 * @since 2016-02-10
 */
public abstract class WPBasicAction implements WPAction
{
    protected final WPManager manager;

    public WPBasicAction(final WPManager manager)
    {
        super();
        this.manager = manager;
    }

    public WPManager getManager()
    {
        return manager;
    }

    abstract public String getDescription();

    @Override
    public abstract void run();
}
