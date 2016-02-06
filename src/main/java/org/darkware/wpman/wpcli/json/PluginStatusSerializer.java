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

package org.darkware.wpman.wpcli.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.darkware.wpman.data.WPPluginStatus;

import java.lang.reflect.Type;

/**
 * @author jeff
 * @since 2016-01-23
 */
public class PluginStatusSerializer implements JsonSerializer<WPPluginStatus>, JsonDeserializer<WPPluginStatus>
{
    @Override
    public JsonElement serialize(WPPluginStatus src, Type srcType, JsonSerializationContext context)
    {
        return new JsonPrimitive(src.toString().toLowerCase());
    }

    @Override
    public WPPluginStatus deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
    {
        String versionString = json.getAsString();

        if ("active".equals(versionString)) return WPPluginStatus.ACTIVE;
        if ("active-network".equals(versionString)) return WPPluginStatus.NETWORK_ACTIVE;

        return WPPluginStatus.INACTIVE;
    }
}
