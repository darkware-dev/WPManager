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

package org.darkware.wpman.wpcli;

/**
 * A {@code WPCLIFlag} is a very simple {@link WPCLIBasicOption} that takes no value. All required
 * information is supplied simply by the existence of the flag.
 *
 * @author jeff
 * @since 2016-01-22
 */
public class WPCLIFlag extends WPCLIBasicOption
{
    /**
     * Creates a new {@code WPCLIFlag} with the given name.
     *
     * @param name The name of the flag.
     */
    public WPCLIFlag(final String name)
    {
        super(name);
    }

    @Override
    protected CharSequence renderValue()
    {
        return null;
    }
}
