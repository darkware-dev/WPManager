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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author jeff
 * @since 2015-11-02
 */
public class RawProcessReader extends ProcessReader
{
    private final ByteArrayOutputStream output;

    public RawProcessReader()
    {
        super();

        this.output = new ByteArrayOutputStream();
    }

    public byte[] getData()
    {
        return this.output.toByteArray();
    }

    @Override
    public String getStringData()
    {
        return Charset.forName("UTF-8").decode(ByteBuffer.wrap(this.getData())).toString();
    }

    @Override
    protected void read(final Process process) throws IOException
    {
        byte[] buffer = new byte[8192];

        InputStream cmdOutput = process.getInputStream();
        int len;
        while((len = cmdOutput.read(buffer)) >= 0)
        {
            if (len > 0) this.output.write(buffer, 0, len);
        }
    }
}
