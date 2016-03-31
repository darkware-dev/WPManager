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
import org.darkware.wpman.WPManager;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

/**
 * @author jeff
 * @since 2016-03-30
 */
public class WPDateModule extends SimpleModule
{
    private final static DateTimeFormatter format = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss");

    public WPDateModule()
    {
        super();

        this.addSerializer(DateTime.class, new PluginStatusSerializer());
        this.addDeserializer(DateTime.class, new PluginStatusDeserializer());
    }

    public static class PluginStatusSerializer extends JsonSerializer<DateTime>
    {
        @Override
        public void serialize(final DateTime version, final JsonGenerator jsonGenerator,
                              final SerializerProvider serializerProvider) throws IOException, JsonProcessingException
        {
            jsonGenerator.writeString(version.toString());
        }
    }

    public static class PluginStatusDeserializer extends StdScalarDeserializer<DateTime>
    {
        public PluginStatusDeserializer()
        {
            super(DateTime.class);
        }

        @Override
        public DateTime deserialize(final JsonParser jsonParser,
                                   final DeserializationContext deserializationContext) throws IOException, JsonProcessingException
        {
            String dateString = jsonParser.getValueAsString();

            if (dateString.startsWith("0000")) return null;

            try
            {
                return DateTime.parse(dateString, WPDateModule.format);
            }
            catch (Throwable t)
            {
                WPManager.log.error("Error parsing date '" + dateString + "' in cron entry: " + t.getLocalizedMessage());
                return DateTime.now();
            }
        }
    }
}
