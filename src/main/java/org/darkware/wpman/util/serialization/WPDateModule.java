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

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This is a Jackson serialization {@link SimpleModule} which is designed to parse the date formats and
 * conventions used by WordPress. This includes the rather unhelpful practice of returning nonsense dates
 * of year, month, and day set to zero.
 *
 * @author jeff
 * @since 2016-03-30
 */
public class WPDateModule extends SimpleModule
{
    private final static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Create a new module to handle serialization of WordPress date formats.
     */
    public WPDateModule()
    {
        super();

        this.addSerializer(LocalDateTime.class, new PluginStatusSerializer());
        this.addDeserializer(LocalDateTime.class, new PluginStatusDeserializer());
    }

    /**
     * This is a serializer capable of writing dates into WordPress's date formats.
     */
    public static class PluginStatusSerializer extends JsonSerializer<LocalDateTime>
    {
        @Override
        public void serialize(final LocalDateTime version, final JsonGenerator jsonGenerator,
                              final SerializerProvider serializerProvider) throws IOException, JsonProcessingException
        {
            jsonGenerator.writeString(version.toString());
        }
    }

    /**
     * This is a deserializer capable of parsing WordPress data formats.
     */
    public static class PluginStatusDeserializer extends StdScalarDeserializer<LocalDateTime>
    {
        /**
         * Creates a new Jackson deserializer for WordPress date formats.
         */
        public PluginStatusDeserializer()
        {
            super(LocalDateTime.class);
        }

        @Override
        public LocalDateTime deserialize(final JsonParser jsonParser,
                                   final DeserializationContext deserializationContext) throws IOException, JsonProcessingException
        {
            String dateString = jsonParser.getValueAsString();

            if (dateString.startsWith("0000")) return null;

            try
            {
                return LocalDateTime.parse(dateString, WPDateModule.format);
            }
            catch (Throwable t)
            {
                WPManager.log.error("Error parsing date '" + dateString + "': " + t.getLocalizedMessage());
                return LocalDateTime.now();
            }
        }
    }
}
