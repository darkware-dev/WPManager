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
 * The {@code WPCLIFormatOption} is a simplified way of specifying the output format for a
 * {@link WPCLI} execution.
 *
 * @author jeff
 * @since 2016-01-23
 */
public class WPCLIFormatOption extends WPCLIOption<WPCLIFormat>
{
    /**
     * Creates a new format option with the given initial value.
     *
     * @param value The initial {@code WPCLIFormat} to use.
     */
    public WPCLIFormatOption(final WPCLIFormat value)
    {
        super("format", value);
        this.requireValue(true);
    }

    @Override
    protected CharSequence renderValue()
    {
        return this.getValue().name().toLowerCase();
    }
}
