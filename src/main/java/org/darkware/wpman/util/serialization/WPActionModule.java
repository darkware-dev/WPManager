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
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.darkware.wpman.actions.WPAction;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author jeff
 * @since 2016-03-31
 */
public class WPActionModule extends SimpleModule
{
    public WPActionModule()
    {
        super();

        this.addSerializer(WPAction.class, new WPActonSerializer());
    }

    public static class WPActonSerializer extends JsonSerializer<WPAction>
    {
        @Override
        public void serialize(final WPAction wpAction, final JsonGenerator jsonGenerator,
                              final SerializerProvider serializerProvider) throws IOException, JsonProcessingException
        {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("state", wpAction.getState().toString());
            jsonGenerator.writeStringField("description", wpAction.getDescription());
            Future future = wpAction.getFuture();
            if (future != null && future instanceof ScheduledFuture)
            {
                Long delay = ((ScheduledFuture)future).getDelay(TimeUnit.SECONDS);
                jsonGenerator.writeObjectField("executionTime", DateTime.now().plusSeconds(delay.intValue()));
            }
            jsonGenerator.writeObjectField("creationTime", wpAction.getCreationTime());
            jsonGenerator.writeObjectField("startTime", wpAction.getStartTime());
            jsonGenerator.writeObjectField("completionTime", wpAction.getCompletionTime());
            jsonGenerator.writeEndObject();
        }
    }
}
