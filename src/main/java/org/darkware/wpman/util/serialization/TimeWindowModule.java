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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.darkware.wpman.util.TimeWindow;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a Jackson {@link Module} for handling serialization of {@link TimeWindow} objects.
 *
 * @author jeff
 * @since 2016-03-28
 */
public class TimeWindowModule extends SimpleModule
{
    /**
     * Create a new {@code TimeWindow} serialization module.
     */
    public TimeWindowModule()
    {
        super();

        this.addSerializer(TimeWindow.class, new TimeWindowSerializer());
        this.addDeserializer(TimeWindow.class, new TimeWindowDeserializer());
    }

    /**
     * This is a Jackson serializer for {@code TimeWindow} objects.
     */
    public static class TimeWindowSerializer extends JsonSerializer<TimeWindow>
    {
        @Override
        public void serialize(final TimeWindow window, final JsonGenerator jsonGenerator,
                              final SerializerProvider serializerProvider) throws IOException
        {
            String windowString = String.format("%02d:%02d-%02d:%02d",
                                                window.getEarliestMoment().getHour(),
                                                window.getEarliestMoment().getMinute(),
                                                window.getLatestMoment().getHour(),
                                                window.getLatestMoment().getMinute());

            jsonGenerator.writeString(windowString);
        }
    }

    /**
     * This is a Jackson deserializer for {@code TimeWindow} objects.
     */
    public static class TimeWindowDeserializer extends StdScalarDeserializer<TimeWindow>
    {
        private final Pattern windowPattern = Pattern.compile("(\\d{1,2}):(\\d{2})\\-(\\d{1,2}):(\\d{2})");

        /**
         * Create a new deserializer for {@code TimeWindow} objects.
         */
        TimeWindowDeserializer()
        {
            super(TimeWindow.class);
        }

        @Override
        public TimeWindow deserialize(final JsonParser jsonParser,
                                final DeserializationContext deserializationContext) throws IOException
        {
            Matcher windowMatcher = this.windowPattern.matcher(jsonParser.getValueAsString());
            if (windowMatcher.matches())
            {
                int startHour = Integer.parseInt(windowMatcher.group(1));
                int startMin = Integer.parseInt(windowMatcher.group(2));
                int endHour = Integer.parseInt(windowMatcher.group(3));
                int endMin = Integer.parseInt(windowMatcher.group(4));

                return new TimeWindow(startHour, startMin, endHour, endMin);
            }
            else throw deserializationContext.mappingException(TimeWindow.class);
        }
    }
}
