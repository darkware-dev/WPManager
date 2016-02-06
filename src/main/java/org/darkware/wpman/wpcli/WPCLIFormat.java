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

/**
 * This enumeration contains a full set of the recognized output formats for the
 * core WP-CLI commands. This is used by the {@link WPCLIFormatOption} option
 * type to declare output format.
 *
 * @author jeff
 * @since 2016-01-23
 */
public enum WPCLIFormat
{
    /** The default format. This may vary by command. */
    DEFAULT,
    /** A tabular format including ASCII-based table borders. */
    TABLE,
    /** Quoted and escaped CSV format, with included header line. */
    CSV,
    /** Common JSON format. Lists are presented as arrays of objects. */
    JSON;
}
