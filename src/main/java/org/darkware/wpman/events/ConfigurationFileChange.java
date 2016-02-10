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

package org.darkware.wpman.events;

import java.nio.file.Path;

/**
 * A {@link WPEvent} sent when a configuration file has been modified.
 *
 * @author jeff
 * @since 2016-02-09
 */
public class ConfigurationFileChange
{
    private final Path path;

    /**
     * Create a new configuration file change event.
     *
     * @param path A path to the file that changed, relative to the configuration root.
     */
    public ConfigurationFileChange(final Path path)
    {
        this.path = path;
    }
}
