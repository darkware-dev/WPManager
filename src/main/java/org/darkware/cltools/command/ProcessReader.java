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

/**
 * @author jeff
 * @since 2015-11-02
 */
public abstract class ProcessReader extends Thread
{
    private Process process;

    protected ProcessReader()
    {
        super();
    }

    /**
     * Register the given {@link Process} for output reading. The data from the process will be stored
     * or processed internally.
     *
     * @param process The {@link Process} to read from.
     */
    public final void readFrom(Process process)
    {
        this.process = process;
    }

    protected final Process getProcess()
    {
        return this.process;
    }

    /**
     * Start the thread which will perform the output reading.
     *
     * This overrides the {@link Thread#start()} method in order to properly initialize the internal
     * locking state in a predictably synchronous manner.
     */
    @Override
    public void start()
    {
        super.start();
    }

    @Override
    public void run()
    {
        try
        {
            this.read(this.process);
        }
        catch (IOException e)
        {
            //TODO: Do something
        }
        finally
        {
            synchronized (this)
            {
                this.notifyAll();
            }
        }
    }

    protected abstract void read(Process process) throws IOException;
    public abstract String getStringData();
}
