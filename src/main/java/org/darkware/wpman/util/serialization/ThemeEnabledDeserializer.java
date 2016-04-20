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

package org.darkware.wpman.util.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

import java.io.IOException;

/**
 * @author jeff
 * @since 2016-04-19
 */
public class ThemeEnabledDeserializer  extends StdScalarDeserializer<Boolean>
{
    public ThemeEnabledDeserializer()
    {
        super(Boolean.class);
    }

    @Override
    public Boolean deserialize(final JsonParser jsonParser,
                               final DeserializationContext deserializationContext) throws IOException, JsonProcessingException
    {
        // Make sure the value isn't already a boolean-ish string.
        String strValue = jsonParser.getValueAsString();

        if ("network".equals(strValue)) return Boolean.TRUE;
        if ("no".equals(strValue)) return Boolean.FALSE;

        return Boolean.FALSE;
    }
}
