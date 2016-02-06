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

package org.darkware.wpman.wpcli;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.darkware.cltools.command.Command;
import org.darkware.cltools.command.LineProcessReader;
import org.darkware.cltools.command.ProcessReader;
import org.darkware.cltools.command.StringProcessReader;
import org.darkware.cltools.utils.CSV;
import org.darkware.cltools.utils.FileSystemTools;
import org.darkware.wpman.data.Version;
import org.darkware.wpman.data.WPSite;
import org.darkware.wpman.wpcli.json.DateTimeSerializer;
import org.darkware.wpman.wpcli.json.VersionSerializer;
import org.joda.time.DateTime;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a wrapper class that abstracts the execution of the somewhat famous
 * WP-CLI command line tool for interacting directly with WordPress. As it
 * operates by directly loading the WordPress code, it cannot be loaded directly
 * into any language except PHP. Here, we use standard command execution via the
 * internal cltools library to run the commands.
 *
 * <p>This wrapper is designed to apply some standard optimization to the command
 * execution. In all cases, this can be modified later before the command is
 * actually run.</p>
 *
 * @author jeff
 * @since 2016-01-22
 */
public class WPCLI
{
    private static Path toolPath = Paths.get("/opt/wpcli/wp");
    public static void setPath(final Path toolPath)
    {
        FileSystemTools.require(toolPath, "efrx");
        WPCLI.toolPath = toolPath;
    }

    private static final Gson jsonAdapter = WPCLI.createJsonAdapter();
    private static Gson createJsonAdapter()
    {
        GsonBuilder jsonBuilder = new GsonBuilder();

        jsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeSerializer());
        jsonBuilder.registerTypeAdapter(Version.class, new VersionSerializer());

        return jsonBuilder.create();
    }

    private final Command cmd;

    private final String group;
    private final String command;
    private final String[] commandArgs;
    private final List<String> args;
    private final Map<String,WPCLIBasicOption> options;

    public WPCLI(final String group, final String command, final String ... commandArgs)
    {
        super();

        this.cmd = new Command(new ProcessBuilder(), toolPath);

        this.group = group;
        this.command = command;
        this.commandArgs = commandArgs;
        this.args = new ArrayList<>();
        this.options = new HashMap<>();
    }

    public void setOption(WPCLIBasicOption option)
    {
        this.options.put(option.getName(), option);
    }

    public void removeOption(final String name)
    {
        this.options.remove(name);
    }

    public void setSite(final WPSite site)
    {
        this.setOption(new WPCLIOption<>("url", site.getDomain()));
    }

    public void restrictList(final String fieldName, final Object value)
    {
        this.setOption(new WPCLIOption<>(fieldName, value.toString()));
    }

    public void setFormat(final WPCLIFormat format)
    {
        if (format.equals(WPCLIFormat.DEFAULT)) this.removeOption("format");
        else this.setOption(new WPCLIFormatOption(format));
    }

    public void loadThemes(final boolean enabled)
    {
        if (enabled) this.removeOption("skip-themes");
        else this.setOption(new WPCLIFlag("skip-themes"));
    }

    public void loadPlugins(final boolean enabled)
    {
        if (enabled) this.removeOption("skip-plugins");
        else this.setOption(new WPCLIFlag("skip-plugins"));
    }

    protected void render()
    {
        this.cmd.addArguments(group);
        this.cmd.addArguments(command);
        this.cmd.addArguments(commandArgs);
        if (this.args.size() > 0) this.cmd.addArguments(this.args);
        for (WPCLIBasicOption opt : this.options.values())
        {
            if (opt.isEnabled()) this.cmd.addArguments(opt.render());
        }
    }

    public List<String> readLines() throws WPCLIError
    {
        LineProcessReader lineReader = new LineProcessReader();

        try
        {
            this.runCommand(lineReader);

            return lineReader.getLines();
        }
        catch (IOException e)
        {
            throw new WPCLIError(this, "Error while reading command output.", e);
        }
    }

    public String readValue() throws WPCLIError
    {
        StringProcessReader stringReader = new StringProcessReader();

        try
        {
            this.runCommand(stringReader);

            return stringReader.getData().trim();
        }
        catch (IOException e)
        {
            throw new WPCLIError(this, "Error while reading command output.", e);
        }
    }


    public List<List<String>> readCSV() throws WPCLIError
    {
        // Force the format to CSV
        this.setOption(new WPCLIFormatOption(WPCLIFormat.CSV));

        return CSV.parse(this.execute());
    }

    public <T> T readJSON(Class<T> dstType) throws WPCLIError
    {
        return this.readJSON(TypeToken.get(dstType));
    }

    public <T> T readJSON(TypeToken<T> dstType) throws WPCLIError
    {
        String data = "";
        try
        {
            this.setFormat(WPCLIFormat.JSON);
            data = this.execute();

            return WPCLI.jsonAdapter.fromJson(data, dstType.getType());
        }
        catch (JsonSyntaxException e)
        {
            throw new WPCLIError(this, "Syntax error in JSON response: " + this + " - Response was:\n" + data);
        }
        catch (IllegalStateException e)
        {
            throw new WPCLIError(this, "Error parsing JSON response: " + data);
        }
    }

    public String execute() throws WPCLIError
    {
        StringProcessReader stringReader = new StringProcessReader();

        try
        {
            this.runCommand(stringReader);

            return stringReader.getData();
        }
        catch (IOException e)
        {
            throw new WPCLIError(this, "Error while reading command output.", e);
        }
    }

    protected void runCommand(ProcessReader reader) throws IOException, WPCLIError
    {
        this.render();
        this.cmd.attachOutputReader(reader);
        this.cmd.start();
        int result = this.cmd.waitForCompletion();

        if (result != 0)
        {
            String errorMessage = reader.getStringData();

            /* Check if we tossed an error code, but still succeeded */
            if (errorMessage.contains("\nSucess: ")) return;

            throw new WPCLIError(this, "Result=" + result + ": " + errorMessage);
        }
    }

    @Override
    public String toString()
    {
        return "WPCLI:" + this.cmd.quotedString();
    }
}
