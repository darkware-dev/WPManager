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

package org.darkware.cltools.command;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

/**
 * @author jeff
 * @since 2015-11-02
 */
public class StringProcessReader extends ProcessReader
{
    private final StringBuilder output;

    public StringProcessReader()
    {
        super();

        this.output = new StringBuilder();
    }

    public String getData()
    {
        return this.output.toString(); 
    }

    @Override
    public String getStringData()
    {
        return this.getData();
    }

    @Override
    protected void read(final Process process) throws IOException
    {
        ByteBuffer buffer = ByteBuffer.allocate(8192);

        InputStream cmdOutput = process.getInputStream();

        ReadableByteChannel channel = Channels.newChannel(cmdOutput);

        int len;
        while((len = channel.read(buffer)) >= 0)
        {
            buffer.flip();
            if (len > 0) this.output.append(Charset.forName("UTF-8").decode(buffer));
            buffer.rewind();
        }
    }
}
