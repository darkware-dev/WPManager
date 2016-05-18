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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.darkware.wpman.data.WPUpdatableComponent;

import java.io.IOException;

/**
 * This is a minimalistic serializer for {@link WPUpdatableComponent} types. Instead of serializing the entire
 * type, it simply serializes the ID and name.
 *
 * @author jeff
 * @since 2016-03-31
 */
public class MinimalUpdatableSerializer extends JsonSerializer<WPUpdatableComponent>
{
    @Override
    public void serialize(final WPUpdatableComponent wpBlog, final JsonGenerator jsonGenerator,
                          final SerializerProvider serializerProvider) throws IOException, JsonProcessingException
    {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("id", wpBlog.getId());
        jsonGenerator.writeStringField("name", wpBlog.getName());
        jsonGenerator.writeEndObject();
    }
}
