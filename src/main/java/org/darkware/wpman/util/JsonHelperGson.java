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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

/**
 * @author jeff
 * @since 2016-03-30
 */
public class JsonHelperGson extends JSONHelper
{
    private Gson gson;

    protected JsonHelperGson(final Gson gson)
    {
        super();

        this.gson = gson;
    }

    protected <T> T _fromJSON(final String json, final Type type)
    {
        try
        {
            return this.gson.fromJson(json, type);
        }
        catch (JsonSyntaxException e)
        {
            throw new JsonFormatException(e);
        }
    }

    protected <T> String _toJSON(final T object)
    {
        return this.gson.toJson(object);
    }
}
