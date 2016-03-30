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
import org.darkware.wpman.data.Version;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author jeff
 * @since 2016-03-28
 */
public class VersionModule extends SimpleModule
{
    public VersionModule()
    {
        super();

        this.addSerializer(Version.class, new VersionSerializer());
        this.addDeserializer(Version.class, new VersionDeserializer());
    }

    public static class VersionSerializer extends JsonSerializer<Version>
    {
        @Override
        public void serialize(final Version version, final JsonGenerator jsonGenerator,
                              final SerializerProvider serializerProvider) throws IOException, JsonProcessingException
        {
            jsonGenerator.writeString(version.toString());
        }
    }

    public class VersionDeserializer extends StdScalarDeserializer<Version>
    {
        public VersionDeserializer()
        {
            super(Path.class);
        }

        @Override
        public Version deserialize(final JsonParser jsonParser,
                                final DeserializationContext deserializationContext) throws IOException, JsonProcessingException
        {
            return new Version(jsonParser.getValueAsString());
        }
    }
}
