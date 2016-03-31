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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * @author jeff
 * @since 2016-03-30
 */
public abstract class JSONHelper
{
    private static JSONHelper helper;

    public static void use(final Gson gson)
    {
        JSONHelper.helper = new JsonHelperGson(gson);
    }

    public static void use(final ObjectMapper mapper)
    {
        JSONHelper.helper = new JsonHelperJackson(mapper);
    }

    public static <T> T fromJSON(final String json, final Type type)
    {
        if (json.length() < 1) return null;
        return JSONHelper.helper._fromJSON(json, type);
    }

    public static <T> String toJSON(final T object)
    {
        return JSONHelper.helper._toJSON(object);
    }

    protected JSONHelper()
    {
        super();
    }

    protected abstract <T> T _fromJSON(final String json, final Type type);

    protected abstract  <T> String _toJSON(final T object);

}
