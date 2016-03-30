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

package org.darkware.wpman;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.internal.NotNull;
import io.dropwizard.Configuration;
import org.darkware.wpman.config.WPCLIConfiguration;
import org.darkware.wpman.config.WordpressConfig;

import javax.validation.Valid;

/**
 * @author jeff
 * @since 2016-03-26
 */
public class WPManagerConfiguration extends Configuration
{
    @NotNull
    @Valid
    private WPCLIConfiguration wpcli = new WPCLIConfiguration();

    @NotNull
    @Valid
    private WordpressConfig wordpress = new WordpressConfig();

    /**
     * Fetch the configuration for WP-CLI.
     *
     * @return A {@code WPCLIConfiguration} object.
     */
    @JsonProperty("wpcli")
    public WPCLIConfiguration getWpcli()
    {
        return wpcli;
    }

    /**
     * Set the WP-CLI configuration.
     *
     * @param wpcli The {@code WPCLIConfiguration} to use.
     */
    @JsonProperty("wpcli")
    public void setWpcli(final WPCLIConfiguration wpcli)
    {
        this.wpcli = wpcli;
    }

    @JsonProperty("wordpress")
    public WordpressConfig getWordpress()
    {
        return wordpress;
    }

    @JsonProperty("wordpress")
    public void setWordpress(final WordpressConfig wordpress)
    {
        this.wordpress = wordpress;
    }
}