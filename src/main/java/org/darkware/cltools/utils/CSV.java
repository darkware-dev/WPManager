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

package org.darkware.cltools.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jeff
 * @since 2016-01-23
 */
public class CSV
{
    private final static String BRACKETS = "\"'";

    public static char escapedChar(final char escapeToken)
    {
        if (escapeToken == 'n') return '\n';
        if (escapeToken == 'r') return '\r';

        return escapeToken;
    }

    public static List<List<String>> parse(final String data)
    {
        List<List<String>> lines = new ArrayList<>();

        for (String line : data.split("\n"))
        {
            final String fixed = line.trim();
            List<String> fields = new ArrayList<>();

            StringBuilder current = new StringBuilder();
            Character bracket = null;
            for (int i = 0; i < fixed.length(); i++)
            {
                char c = fixed.charAt(i);

                // If we're bracketed, keep reading until we hit the end bracket
                if (bracket != null)
                {
                    if (bracket.equals(c))
                    {
                        bracket = null;
                        continue;
                    }
                }

                // Check for a terminator
                if (bracket == null && c == ',')
                {
                    fields.add(current.toString());
                    current = new StringBuilder();
                    continue;
                }

                // Detect escaping
                else if (c == '\\')
                {
                    current.append(CSV.escapedChar(fixed.charAt(++i)));
                }

                // Detect bracketing
                else if (CSV.BRACKETS.indexOf(c) >= 0)
                {
                    bracket = c;
                }

                // Otherwise accept the character
                else current.append(c);
            }

            if (current.length() > 0) fields.add(current.toString());

            if (fields.size() > 0) lines.add(fields);
        }

        return lines;
    }
}
