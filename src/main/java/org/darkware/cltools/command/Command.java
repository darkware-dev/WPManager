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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jeff
 * @since 2015-11-02
 */
public class Command
{
    private static final Logger log = LoggerFactory.getLogger("Command");

    private final ProcessBuilder processBuilder;
    private final List<String> argv;

    private Process process;
    private int rval;

    private ProcessReader outputReader;

    public Command(ProcessBuilder processBuilder, String executable, Object ... args)
    {
        super();

        this.processBuilder = processBuilder;
        this.argv = new ArrayList<>();
        this.outputReader = new RawProcessReader();

        this.setExecutable(executable);
        this.addArguments(args);
    }

    public Command(ProcessBuilder processBuilder, Path executable, Object ... args)
    {
        this(processBuilder, executable.toAbsolutePath().toString(), args);
    }

    public void setExecutable(Path executable)
    {
        this.setExecutable(executable.toAbsolutePath().toString());
    }

    public void setExecutable(String executable)
    {
        if (this.argv.size() < 1) this.argv.add(executable);
        else this.argv.set(0, executable);
    }

    public Command addArgument(Object arg)
    {
        if (arg instanceof String) this.argv.add((String)arg);
        else if (arg instanceof Path) this.argv.add(((Path)arg).toAbsolutePath().toString());
        else this.argv.add(arg.toString());

        return this;
    }

    public Command addArguments(Object ... args)
    {
        for (Object arg : args)
        {
            this.addArgument(arg);
        }

        return this;
    }

    public Command addArgumentList(List<? extends Object> argList)
    {
        argList.stream().forEach(this::addArgument);

        return this;
    }

    protected void render()
    {
        // By default, we use live rendering.
    }

    public Process start() throws IOException
    {
        this.processBuilder.command(this.argv);

        Command.log.debug("Execute: {}", this);
        this.process = this.processBuilder.start();
        this.outputReader.readFrom(this.process);
        this.outputReader.start();

        return this.process;
    }

    public OutputStream getOutputStream()
    {
        return this.process.getOutputStream();
    }

    public int waitForCompletion()
    {
        try
        {
            synchronized (this.outputReader)
            {
                this.rval = this.process.waitFor();

                this.outputReader.wait();
                return rval;
            }
        }
        catch (InterruptedException e)
        {
            //TODO: Toss a better exception
            throw new RuntimeException("Interrupted while waiting for process.");
        }
    }

    public void attachOutputReader(ProcessReader reader)
    {
        this.outputReader = reader;
    }

    @Override
    public String toString()
    {
        return "CMD:{ " + StringTools.join(", ", this.argv) + " }";
    }

    public String quotedString()
    {
        StringBuilder command = new StringBuilder();

        for (String arg : this.argv)
        {
            if (command.length() > 0) command.append(' ');
            if (arg.contains(" "))
            {
                command.append("\"");
                command.append(arg);
                command.append("\"");
            }
            else command.append(arg);
        }

        return command.toString();
    }
}
