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

import com.google.common.reflect.TypeToken;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.darkware.cltools.command.Command;
import org.darkware.cltools.command.LineProcessReader;
import org.darkware.cltools.command.ProcessReader;
import org.darkware.cltools.command.StringProcessReader;
import org.darkware.cltools.utils.CSV;
import org.darkware.wpman.WPManager;
import org.darkware.wpman.data.Version;
import org.darkware.wpman.data.WPBlog;
import org.darkware.wpman.util.JSONHelper;
import org.darkware.wpman.util.JsonFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    /** A shared log facility for reporting messages about the WP-CLI wrapper system */
    protected static final Logger log = LoggerFactory.getLogger("WP-CLI");

    private static Path toolPath = Paths.get("/opt/wpcli/wp");
    public static void setPath(final Path toolPath)
    {
        WPCLI.toolPath = toolPath;
    }

    /**
     * Check to see if an updated version of WP-CLI is available.
     *
     * @return The new version, or null if no update is available.
     */
    public static Version checkForUpdate()
    {
        WPCLI updateCheck = new WPCLI("cli", "check-update");
        updateCheck.setOption(new WPCLIFlag("allow-root"));
        updateCheck.setOption(new WPCLIFlag("no-color"));

        WPCLIFieldsOption fields = new WPCLIFieldsOption();
        fields.add("version");
        updateCheck.setOption(fields);

        List<String> lines = updateCheck.readLines();

        if (lines.size() == 2) return new Version(lines.get(1));
        return null;
    }

    /**
     * Update the local WP-CLI tool to the most recent version.
     */
    public static void update()
    {
        try
        {
            WPManager.log.info("Downloading new version of WP-CLI.");

            CloseableHttpClient httpclient = HttpClients.createDefault();

            URI pharURI = new URIBuilder().setScheme("http")
                                          .setHost("raw.githubusercontent.com")
                                          .setPath("/wp-cli/builds/gh-pages/phar/wp-cli.phar").build();

            WPManager.log.info("Downloading from: {}", pharURI);
            HttpGet downloadRequest = new HttpGet(pharURI);

            CloseableHttpResponse response = httpclient.execute(downloadRequest);

            WPManager.log.info("Download response: {}", response.getStatusLine());
            WPManager.log.info("Download content type: {}", response.getFirstHeader("Content-Type").getValue());

            FileChannel wpcliFile = FileChannel.open(WPCLI.toolPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

            response.getEntity().writeTo(Channels.newOutputStream(wpcliFile));
            wpcliFile.close();

            Set<PosixFilePermission> wpcliPerms = new HashSet<>();
            wpcliPerms.add(PosixFilePermission.OWNER_READ);
            wpcliPerms.add(PosixFilePermission.OWNER_WRITE);
            wpcliPerms.add(PosixFilePermission.OWNER_EXECUTE);
            wpcliPerms.add(PosixFilePermission.GROUP_READ);
            wpcliPerms.add(PosixFilePermission.GROUP_EXECUTE);

            Files.setPosixFilePermissions(WPCLI.toolPath, wpcliPerms);
        }
        catch (URISyntaxException e)
        {
            WPManager.log.error("Failure building URL for WPCLI download.", e);
            System.exit(1);
        }
        catch (IOException e)
        {
            WPManager.log.error("Error while downloading WPCLI client.", e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private final Command cmd;

    private final String group;
    private final String command;
    private final List<String> commandArgs;
    private final List<String> args;
    private final Map<String,WPCLIBasicOption> options;
    private final ByteArrayOutputStream inputData;
    private final PrintWriter input;

    public WPCLI(final String group, final String command, final String ... commandArgs)
    {
        super();

        this.cmd = new Command(new ProcessBuilder(), toolPath);

        this.group = group;
        this.command = command;
        this.commandArgs = new ArrayList<>();
        for (String arg : commandArgs) this.addArgument(arg);
        this.args = new ArrayList<>();
        this.options = new HashMap<>();

        this.inputData = new ByteArrayOutputStream();
        this.input = new PrintWriter(this.inputData);
    }

    /**
     * Add another argument to the command. This is explicitly placed after the group and
     * command strings, and ordered in the same order that it was added.
     *
     * @param argument The argument to add.
     */
    public void addArgument(final String argument)
    {
        this.commandArgs.add(argument);
    }

    public void setOption(WPCLIBasicOption option)
    {
        this.options.put(option.getName(), option);
    }

    public void removeOption(final String name)
    {
        this.options.remove(name);
    }

    public void setBlog(final WPBlog blog)
    {
        this.setOption(new WPCLIOption<>("url", blog.getDomain()));
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

    public PrintWriter getStdin()
    {
        return this.input;
    }

    protected void render()
    {
        this.cmd.addArgument(group);
        this.cmd.addArgument(command);
        this.cmd.addArgumentList(commandArgs);
        if (this.args.size() > 0) this.cmd.addArguments(this.args);
        this.options.values()
                    .stream()
                    .filter(opt -> opt.isEnabled())
                    .forEach(opt -> this.cmd.addArguments(opt.render()));
    }

    public List<String> readLines() throws WPCLIError
    {
        LineProcessReader lineReader = new LineProcessReader();

        try
        {
            this.runCommand(lineReader, this.inputData.toByteArray());

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
            this.runCommand(stringReader, this.inputData.toByteArray());

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
        return this.readJSON(TypeToken.of(dstType));
    }

    public <T> T readJSON(TypeToken<T> dstType) throws WPCLIError
    {
        return this.readJSON(dstType.getType());
    }

    public <T> T readJSON(Type type) throws WPCLIError
    {
        String data = "";
        try
        {
            this.setFormat(WPCLIFormat.JSON);
            data = this.execute();

            return JSONHelper.fromJSON(data, type);
        }
        catch (JsonFormatException e)
        {
            WPCLI.log.error("Error in JSON parsing.", e);
            System.exit(1);
            throw new WPCLIError(this, "Syntax error in JSON response: " + this + " - Response was:\n" + data);
        }
        catch (IllegalStateException e)
        {
            throw new WPCLIError(this, "Error parsing JSON response: " + data);
        }
    }

    /**
     * Executes the command, ignoring any messages or data and only checking for success
     * or failure response codes.
     *
     * @return {@code true} if the command returned no error, {@code false} if any error
     * is returned.
     */
    public boolean checkSuccess()
    {
        try
        {
            this.execute();
            return true;
        }
        catch (WPCLIError e)
        {
            WPCLI.log.debug("Command returned error: {}", e.getLocalizedMessage());
            return false;
        }
        catch (Throwable t)
        {
            WPCLI.log.warn("Saw unexpected exception while running a command for success/failure: {}: {}", t.getClass().getName(), t.getLocalizedMessage());
            return false;
        }
    }

    public String execute() throws WPCLIError
    {
        StringProcessReader stringReader = new StringProcessReader();

        try
        {
            this.input.flush();
            this.runCommand(stringReader, this.inputData.toByteArray());

            return stringReader.getData();
        }
        catch (IOException e)
        {
            throw new WPCLIError(this, "Error while reading command output.", e);
        }
    }

    protected void runCommand(ProcessReader reader, final byte[] input) throws IOException, WPCLIError
    {
        this.render();
        this.cmd.attachOutputReader(reader);
        this.cmd.start();

        // Write the input, if it exists
        if (input != null && input.length > 0)
        {
            this.cmd.getOutputStream().write(input);
            this.cmd.getOutputStream().flush();
            this.cmd.getOutputStream().close();
        }

        int result = this.cmd.waitForCompletion();

        if (result != 0)
        {
            String errorMessage = reader.getStringData();

            /* Check if we tossed an error code, but still succeeded */
            if (errorMessage.contains("\nSucess: ")) return;

            WPManager.log.error("Error running command: " + this.cmd.quotedString());
            throw new WPCLIError(this, "Result=" + result + ": " + errorMessage);
        }
    }

    @Override
    public String toString()
    {
        return "WPCLI:" + this.cmd.quotedString();
    }
}
