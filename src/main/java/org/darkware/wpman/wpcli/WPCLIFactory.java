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

import org.darkware.wpman.WPManagerConfiguration;

import java.nio.file.Path;

/**
 * A {@code WPCLIBuilder} is a factory class for bootstrapping the creation of {@link WPCLI}
 * objects. It supports setting up default and global options which will be applied to all
 * created instances.
 *
 * <p>Additionally, the factory supports reading supplementary command modules and
 * automatically loading them if the designated group is requested.</p>
 *
 * @author jeff
 * @since 2016-01-22
 */
public class WPCLIFactory
{
    private final WPManagerConfiguration config;

    private Path wordpressDir;
    private String defaultUrl;

    /**
     * Create a new factory object based on the given configuration. This will pre-load some
     * values from the configuration, and attempt to populate the command auto-loading tables.
     * Any changes to the configuration will trigger a reinitialization of the factory.
     *
     * @param config The configuration to attach the factory to.
     */
    public WPCLIFactory(final WPManagerConfiguration config)
    {
        super();

        this.config = config;

        this.wordpressDir = this.config.getWordpress().getBasePath();
        this.defaultUrl = this.config.getWordpress().getDefaultHost();
    }

    /**
     * Builds a {@link WPCLI} based on the current configuration and factory settings.
     *
     * @param group The command group to invoke. If the group has a mapped auto-load module,
     * this will trigger the addition of appropriate options to load the module.
     * @param command The command to execute.
     * @param arguments Any additional non-option arguments to the command.
     * @return A newly initialized {@code WPCLI} object.
     */
    public WPCLI build(final String group, final String command, final String ... arguments)
    {
        final WPCLI wpcli = new WPCLI(group, command, arguments);

        wpcli.setOption(new WPCLIFlag("allow-root"));
        wpcli.setOption(new WPCLIFlag("no-color"));
        if (this.wordpressDir != null) wpcli.setOption(new WPCLIOption<>("path", this.wordpressDir));
        if (this.defaultUrl != null) wpcli.setOption(new WPCLIOption<>("url", this.defaultUrl));

        return wpcli;
    }
}
