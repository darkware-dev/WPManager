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

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link WPCLIBasicOption} which accepts an ordered list of field names for a {@link WPCLI}
 * handler. This is often used to control the data that is output by various query commands.
 *
 * <p>The order of the fields is often significant. While JSON output and serialization handles
 * any ordering, other formats may require linkage between field order and parsing routines.</p>
 *
 * @author jeff
 * @since 2016-01-22
 */
public class WPCLIFieldsOption extends WPCLIBasicOption
{
    private final List<String> fields;

    /**
     * Creates a new {@code WPCLIFieldsOption} with an empty list of fields.
     */
    public WPCLIFieldsOption()
    {
        super("fields");

        this.fields = new ArrayList<>();
    }

    /**
     * Creates a new {@code WPCLIFieldsOption} with the given list of fields. This is equivalent
     * to calling {@link #add(String)} with each of the supplied field names in the order they
     * are given.
     *
     * @param fields A list of fields to add.
     */
    public WPCLIFieldsOption(String ... fields)
    {
        this();

        for (String field : fields) this.add(field);
    }

    /**
     * Adds the given field name to the end of the current list.
     *
     * @param fieldName The name of the field to add.
     */
    public void add(final String fieldName)
    {
        if (fieldName == null) return;
        if (fieldName.length() < 1) return;
        this.fields.add(fieldName);
    }

    @Override
    protected CharSequence renderValue()
    {
        if (this.fields.size() < 1) return null;
        StringBuilder value = new StringBuilder();
        for (String field : this.fields)
        {
            if (value.length() > 0) value.append(',');
            value.append(field);
        }

        return value;
    }
}
