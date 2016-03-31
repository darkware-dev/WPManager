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

package org.darkware.wpman.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author jeff
 * @since 2016-03-30
 */
public class JsonHelperJackson extends JSONHelper
{
    private final ObjectMapper objectMapper;

    protected JsonHelperJackson(final ObjectMapper objectMapper)
    {
        super();

        this.objectMapper = objectMapper;
    }

    protected <T> T _fromJSON(final String json, final Type type)
    {
        try
        {
            JavaType objType = this.objectMapper.constructType(type);
            return this.objectMapper.readValue(json, objType);
        }
        catch (IOException e)
        {
            throw new JsonFormatException(e);
        }
    }

    protected <T> String _toJSON(final T object)
    {
        try
        {
            return this.objectMapper.writeValueAsString(object);
        }
        catch (JsonProcessingException e)
        {
            throw new JsonConversionException(e);
        }
    }
}
