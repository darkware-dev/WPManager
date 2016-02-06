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

import org.darkware.cltools.utils.StringTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jeff
 * @since 2015-11-02
 */
public class LineProcessReader extends ProcessReader
{
    private final List<String> lines;

    public LineProcessReader()
    {
        super();

        this.lines = new ArrayList<>();
    }

    public List<String> getLines()
    {
        return Collections.unmodifiableList(this.lines);
    }

    @Override
    public String getStringData()
    {
        return StringTools.join("\n", this.getLines()).toString();
    }

    protected void read(Process process)
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        try
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                this.lines.add(line);
            }
        }
        catch (IOException e)
        {
            //TODO: Do something here...
        }
    }
}
