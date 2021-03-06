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
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.darkware.wpman.data.WPPluginStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a Jackson {@link Module} for serializing and deserializing {@link WPPluginStatus} instances.
 *
 * @author jeff
 * @since 2016-03-30
 */
public class PluginStatusModule extends SimpleModule
{
    /**
     * Create a new module for handling serialization of the {@link WPPluginStatus} enum.
     */
    public PluginStatusModule()
    {
        super();

        this.addSerializer(WPPluginStatus.class, new PluginStatusSerializer());
        this.addDeserializer(WPPluginStatus.class, new PluginStatusDeserializer());
    }

    /**
     * A serializer for {@link WPPluginStatus} instances.
     */
    public static class PluginStatusSerializer extends JsonSerializer<WPPluginStatus>
    {
        @Override
        public void serialize(final WPPluginStatus version, final JsonGenerator jsonGenerator,
                              final SerializerProvider serializerProvider) throws IOException, JsonProcessingException
        {
            jsonGenerator.writeString(version.toString());
        }
    }

    /**
     * A deserializer for {@link WPPluginStatus} instances.
     */
    public static class PluginStatusDeserializer extends StdScalarDeserializer<WPPluginStatus>
    {
        private final Map<String, WPPluginStatus> statusMap;

        /**
         * Creates a new deserializer. This builds a map of all statuses and the various strings that are associated
         * with them.
         */
        public PluginStatusDeserializer()
        {
            super(WPPluginStatus.class);

            this.statusMap = new HashMap<>();

            // Populate the name map
            for (WPPluginStatus status : WPPluginStatus.values())
            {
                this.statusMap.put(status.toString(), status);
                status.getAliases().forEach(a -> this.statusMap.put(a, status));
            }
        }

        @Override
        public WPPluginStatus deserialize(final JsonParser jsonParser,
                                   final DeserializationContext deserializationContext) throws IOException, JsonProcessingException
        {
            String statusString = jsonParser.getValueAsString().trim().toLowerCase();
            if (this.statusMap.containsKey(statusString)) return this.statusMap.get(statusString);
            else return WPPluginStatus.UNDECLARED;
        }
    }
}
