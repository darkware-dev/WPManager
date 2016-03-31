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
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author jeff
 * @since 2016-03-28
 */
public class PathModule extends SimpleModule
{
    public PathModule()
    {
        super();

        this.addSerializer(Path.class, new PathSerializer());
        this.addDeserializer(Path.class, new PathDeserializer());
    }

    public static class PathSerializer extends JsonSerializer<Path>
    {
        @Override
        public void serialize(final Path path, final JsonGenerator jsonGenerator,
                              final SerializerProvider serializerProvider) throws IOException, JsonProcessingException
        {
            jsonGenerator.writeString(path.toString());
        }
    }

    public static class PathDeserializer extends StdScalarDeserializer<Path>
    {
        public PathDeserializer()
        {
            super(Path.class);
        }

        @Override
        public Path deserialize(final JsonParser jsonParser,
                                final DeserializationContext deserializationContext) throws IOException, JsonProcessingException
        {
            return Paths.get(jsonParser.getValueAsString());
        }
    }
}
