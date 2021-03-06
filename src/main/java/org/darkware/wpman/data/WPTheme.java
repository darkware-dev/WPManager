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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.darkware.wpman.config.ThemeConfig;
import org.darkware.wpman.util.serialization.ThemeEnabledDeserializer;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIFieldsOption;

/**
 * This class models a WordPress theme and its associated relationship with the configured repositories.
 *
 * @author jeff
 * @since 2016-01-23
 */
public class WPTheme extends WPUpdatableComponent<ThemeConfig> implements WPRepositoryItem
{
    /**
     * Set the required field options on the {@link WPCLI} command in order to support proper deserialization of JSON
     * objects.
     *
     * @param command The command to set fields on.
     *
     * @return The command that was supplied, with field options now set.
     */
    public static WPCLI setFields(final WPCLI command)
    {
        WPCLIFieldsOption fields = new WPCLIFieldsOption();
        fields.add("name");
        fields.add("description");
        fields.add("title");
        fields.add("status");
        fields.add("version");
        fields.add("update_version");
        fields.add("enabled");
        command.setOption(fields);

        return command;
    }

    @JsonProperty("title")
    private String name;
    private String description;
    private WPThemeStatus status;
    @JsonDeserialize(using = ThemeEnabledDeserializer.class)
    private boolean enabled;

    /**
     * Create a new, unconfigured theme. As created, this model will be essentially non-functional. Outside of
     * serialization processes, calling code should favor {@link WPTheme#WPTheme(String, String)}.
     */
    protected WPTheme()
    {
        super(WPUpdatableType.THEME);
    }

    /**
     * Create a new theme model.
     *
     * @param id The unique identifier ("slug") for this theme.
     * @param name The human-readable name for this theme.
     */
    public WPTheme(final String id, final String name)
    {
        this();

        this.setId(id);
        this.setName(name);
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public void setName(final String name)
    {
        this.name = name;
    }

    @Override
    public String getDescription()
    {
        return this.description;
    }

    @Override
    public void setDescription(final String description)
    {
        this.description = description;
    }

    /**
     * Fetch the current activation status of this theme.
     *
     * @return The status as a {@link WPThemeStatus} instance.
     */
    public WPThemeStatus getStatus()
    {
        return this.status;
    }

    /**
     * Sets the reported status of this theme.
     * <p>
     * <em>Note:</em> (For now?) This is just for reporting. Changing the status of the model will not trigger a
     * complementary change in the WordPress instance.
     *
     * @param status The new {@link WPThemeStatus} to report.
     */
    public void setStatus(final WPThemeStatus status)
    {
        this.status = status;
    }

    /**
     * Checks to see if this theme is enabled for network usage.
     *
     * @return {@code true} if the theme is enabled for use across the network.
     */
    public boolean isEnabled()
    {
        return this.enabled;
    }

    /**
     * Sets the reported enablement of this theme on the network.
     * <p>
     * <em>Note:</em> (For now?) This is just for reporting. Changing the status of the model will not trigger a
     * complementary change in the WordPress instance.
     *
     * @param enabled {@code true} if the theme is configured for use on any blog in the network, {@code false} if use
     * of the theme is prohibited on the network.
     */
    public void setEnabled(final boolean enabled)
    {
        this.enabled = enabled;
    }
}
