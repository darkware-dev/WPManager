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
 * A generic implementation of the {@link WPCLIBasicOption} class. This allows for options of
 * any generic type to be easily created, assuming that the rendering can be delegated to the
 * {@link Object#toString()} method.
 *
 * @author jeff
 * @since 2016-01-22
 */
public class WPCLIOption<T> extends WPCLIBasicOption
{
    private T value;
    private boolean requireValue;

    /**
     * Create a new option with the given name and value.
     *
     * @param name The option name.
     * @param value The initial value of the option.
     */
    public WPCLIOption(final String name, final T value)
    {
        super(name);

        this.setValue(value);
        this.requireValue = true;
    }

    /**
     * Fetch the currently stored value for this option.
     *
     * @return The current value
     */
    public T getValue()
    {
        return value;
    }

    /**
     * Sets the value of this option.
     *
     * @param value The value to set.
     */
    public void setValue(final T value)
    {
        this.value = value;
    }

    /**
     * Declares if this option requires a value or not.
     *
     * @param flag {@code true} if the option must have a value to valid, {@code false} if the option can
     * have an empty value.
     */
    protected final void requireValue(final boolean flag)
    {
        this.requireValue = flag;
    }

    @Override
    protected boolean checkValid()
    {
        return super.checkValid() && (this.requireValue && (this.renderValue() != null));
    }

    @Override
    protected CharSequence renderValue()
    {
        if (this.value == null) return null;

        String valueString = this.value.toString();
        if (valueString.length() < 1) return null;

        return valueString;
    }
}
