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
import org.darkware.wpman.data.WPObjectType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a Jackson module for supporting bidirectional serialization for the {@link WPObjectType}
 * enumeration.
 *
 * @author jeff
 * @since 2016-05-18
 */
public class WPObjectTypeModule extends SimpleModule
{
    /**
     * Create a new module for supporting the {@link WPObjectType} enumeration.
     */
    public WPObjectTypeModule()
    {
        super();

        this.addSerializer(WPObjectType.class, new WPObjectTypeModule.WPObjectTypeSerializer());
        this.addDeserializer(WPObjectType.class, new WPObjectTypeModule.WPObjectTypeDeserializer());
    }

    /**
     * A serializer for {@link WPObjectType} fields.
     */
    public static class WPObjectTypeSerializer extends JsonSerializer<WPObjectType>
    {
        @Override
        public void serialize(final WPObjectType version, final JsonGenerator jsonGenerator,
                              final SerializerProvider serializerProvider) throws IOException, JsonProcessingException
        {
            jsonGenerator.writeString(version.toString());
        }
    }

    /**
     * A Deserializer for {@link WPObjectType} fields.
     */
    public static class WPObjectTypeDeserializer extends StdScalarDeserializer<WPObjectType>
    {
        private final Map<String, WPObjectType> slugMap;

        /**
         * Create a new deserializer for {@link WPObjectType}s.
         */
        public WPObjectTypeDeserializer()
        {
            super(WPObjectType.class);

            this.slugMap = new HashMap<>();
            for (WPObjectType type : WPObjectType.values())
            {
                this.slugMap.put(type.getSlug(), type);
            }
        }

        @Override
        public WPObjectType deserialize(final JsonParser jsonParser,
                                          final DeserializationContext deserializationContext) throws IOException, JsonProcessingException
        {
            String typeString = jsonParser.getValueAsString().trim().toLowerCase();

            if (this.slugMap.containsKey(typeString)) return this.slugMap.get(typeString);

            return WPObjectType.UNKNOWN;
        }
    }
}
