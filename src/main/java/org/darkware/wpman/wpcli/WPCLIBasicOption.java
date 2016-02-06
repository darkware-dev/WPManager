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
 * This is a base class for most options to a {@link WPCLI} call. The implementation makes no
 * assumptions and sets no limitation on the type of the value of the option.
 *
 * @author jeff
 * @since 2016-01-22
 */
public abstract class WPCLIBasicOption
{
    private final String name;
    private boolean enabled;

    /**
     * Creates a new basic option.
     *
     * @param name The name of the option.
     */
    public WPCLIBasicOption(final String name)
    {
        super();

        this.name = name;
    }

    /**
     * Fetch the name of the option. This is the actual logical name of the option as used on the
     * command line.
     *
     * @return The name of the option.
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * Checks to see if this options is enabled. Disabled options won't be rendered into the
     * {@code WPCLI} command.
     *
     * @return {@code true} if the option is enabled for use, {@code false} if it isn't.
     */
    public final boolean isEnabled()
    {
        return this.enabled && this.checkValid();
    }

    /**
     * Enable this option.
     */
    public final void enable()
    {
        this.enabled = true;
    }

    /**
     * Disable this option, preventing it from being included in the command.
     */
    public final void disable()
    {
        this.enabled = false;
    }

    /**
     * Check to see if the option value is valid.
     *
     * @return {@code true} if the option is valid, {@code false} if its not.
     */
    protected boolean checkValid()
    {
        return true;
    }

    /**
     * Render the option for inclusion in a command.
     *
     * @return The option rendered as a {@code String}.
     */
    public final String render()
    {
        StringBuilder optString = new StringBuilder();

        optString.append("--");
        optString.append(this.name);
        CharSequence value = this.renderValue();
        if (value != null)
        {
            optString.append('=');
            optString.append(value);
        }

        return optString.toString();
    }

    /**
     * Render the the value portion of the option.
     *
     * @return The value, rendered as a {@code String}.
     */
    protected abstract CharSequence renderValue();
}
