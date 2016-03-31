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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

/**
 * @author jeff
 * @since 2016-03-30
 */
public class PermissiveBooleanModule extends SimpleModule
{
    public PermissiveBooleanModule()
    {
        super();

        this.addSerializer(Boolean.class, new PermissiveBooleanSerializer());
        this.addDeserializer(Boolean.class, new PermissiveBooleanDeserializer());
        this.addSerializer(boolean.class, new PermissiveBooleanSerializer());
        this.addDeserializer(boolean.class, new PermissiveBooleanDeserializer());
    }

    public static class PermissiveBooleanSerializer extends JsonSerializer<Boolean>
    {
        @Override
        public void serialize(final Boolean value, final JsonGenerator jsonGenerator,
                              final SerializerProvider serializerProvider) throws IOException, JsonProcessingException
        {
            jsonGenerator.writeString(value.toString());
        }
    }

    public static class PermissiveBooleanDeserializer extends StdScalarDeserializer<Boolean>
    {
        public PermissiveBooleanDeserializer()
        {
            super(Boolean.class);
        }

        @Override
        public Boolean deserialize(final JsonParser jsonParser,
                                   final DeserializationContext deserializationContext) throws IOException, JsonProcessingException
        {
            // Make sure the value isn't already a boolean-ish string.
            String strValue = jsonParser.getValueAsString();
            if (strValue.equals("true")) return Boolean.TRUE;
            if (strValue.equals("false")) return Boolean.FALSE;
            if (strValue.equals("yes")) return Boolean.TRUE;
            if (strValue.equals("no")) return Boolean.FALSE;
            if (strValue.equals("on")) return Boolean.TRUE;
            if (strValue.equals("off")) return Boolean.FALSE;

            // Okay, try the integer version
            long value = jsonParser.getValueAsLong();

            if (value == 0) return Boolean.FALSE;
            else return Boolean.TRUE;
        }
    }
}
