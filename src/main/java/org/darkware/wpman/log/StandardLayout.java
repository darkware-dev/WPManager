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

package org.darkware.wpman.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author jeff
 * @since 2016-01-22
 */
public class StandardLayout extends LayoutBase<ILoggingEvent>
{
    private final DateTimeFormatter timeFormat;
    private final ZoneOffset tzOffset;

    public StandardLayout()
    {
        super();

        this.timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-DD HH:mm:ss.SSS");
        this.tzOffset = OffsetDateTime.now().getOffset();
    }

    public String doLayout(ILoggingEvent event) {
        StringBuffer sbuf = new StringBuffer(128);

        sbuf.append(this.timeFormat.format(LocalDateTime.ofEpochSecond(event.getTimeStamp(), 0, this.tzOffset)));
        sbuf.append(" [");
        sbuf.append(event.getLevel());
        sbuf.append("] ");
        sbuf.append(event.getLoggerName());
        sbuf.append(" - ");
        sbuf.append(event.getFormattedMessage());
        sbuf.append(CoreConstants.LINE_SEPARATOR);
        return sbuf.toString();
    }
}
