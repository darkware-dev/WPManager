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
import com.google.common.base.Objects;
import org.darkware.wpman.wpcli.WPCLI;
import org.darkware.wpman.wpcli.WPCLIFieldsOption;

import java.time.LocalDateTime;

/**
 * A {@code WPUser} is an individual record of a user in relation to a given {@link WPBlog}.
 *
 * @author jeff
 * @since 2016-04-13
 */
public class WPUser implements Comparable<WPUser>
{
    /**
     * Set the required field options on the {@link WPCLI} command in order to support proper
     * deserialization of JSON objects.
     *
     * @param command The command to set fields on.
     * @return The command that was supplied, with field options now set.
     */
    public static WPCLI setFields(final WPCLI command)
    {
        WPCLIFieldsOption fields = new WPCLIFieldsOption();
        fields.add("ID");
        fields.add("user_login");
        fields.add("display_name");
        fields.add("user_email");
        fields.add("registered");
        fields.add("roles");
        command.setOption(fields);

        return command;
    }

    @JsonProperty("ID")
    private int id;

    @JsonProperty("user_login")
    private String login;

    @JsonProperty("display_name")
    private String name;

    @JsonProperty(value = "user_email", access = JsonProperty.Access.WRITE_ONLY)
    private String email;

    @JsonProperty("registered")
    private LocalDateTime registrationDate;

    @JsonProperty("roles")
    private String role;

    /**
     * Creates a new empty user. This is primarily used for serialization.
     */
    private WPUser()
    {
        super();
    }

    /**
     * Fetch the global ID for this user. This is guaranteed to be unique to the instance, even across deletion
     * and recreation events.
     *
     * @return The user's ID, as a non-negative integer.
     */
    public int getId()
    {
        return this.id;
    }

    public void setId(final int id)
    {
        this.id = id;
    }

    public String getLogin()
    {
        return this.login;
    }

    public void setLogin(final String login)
    {
        this.login = login;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getEmail()
    {
        return this.email;
    }

    public void setEmail(final String email)
    {
        this.email = email;
    }

    public LocalDateTime getRegistrationDate()
    {
        return this.registrationDate;
    }

    public void setRegistrationDate(final LocalDateTime registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    public String getRole()
    {
        return this.role;
    }

    public void setRole(final String role)
    {
        this.role = role;
    }

    @Override
    public int compareTo(final WPUser o)
    {
        return Integer.compare(this.id, o.id);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof WPUser)) return false;
        final WPUser wpUser = (WPUser) o;
        return this.id == wpUser.id;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(this.id);
    }
}
