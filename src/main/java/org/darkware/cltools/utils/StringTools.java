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

import java.util.Arrays;
import java.util.Collection;

/**
 * @author jsharpe
 * @since 2015-02-05
 */
public class StringTools
{
    public static final char[] CRLF = new char[] { 0x0D, 0x0A };

    public static CharSequence join(String glue, Collection<? extends Object> objects)
    {
        StringBuilder str = new StringBuilder();

        for (Object obj : objects)
        {
            if (str.length() > 0) str.append(glue);

            if (obj instanceof CharSequence) str.append(obj);
            else str.append(obj.toString());
        }

        return str;
    }

    public static CharSequence join(String glue, Object ... objects)
    {
        return StringTools.join(glue, Arrays.asList(objects));
    }

    public static String rechunk(String src, int maxWidth)
    {
        return StringTools.rechunk(src, maxWidth, StringTools.CRLF);
    }

    public static String rechunk(String src, int maxWidth, char[] lineDelimiter)
    {
        StringBuilder rechunked = new StringBuilder();

        int currentWidth = 0;

        for(int srcCursor = 0; srcCursor < src.length(); srcCursor++)
        {
            char c = src.charAt(srcCursor);

            // Ignore line delimiters
            if (c == '\n' || c == '\r') continue;

            // Inject a line delimiter if we're at the max width
            if (currentWidth >= maxWidth)
            {
                rechunked.append(lineDelimiter);
                currentWidth = 0;
            }

            // Copy the current character
            rechunked.append(c);
            currentWidth++;
        }

        return rechunked.toString();
    }

    /**
     * Constructs a string composed of the given pattern, repeated until it is the desired length. Note that the
     * length is measured in number of characters, not number of times the pattern is repeated. Also, there is no
     * guarantee that the result will contain only full repeats of the pattern. If the pattern consists of three
     * characters and the length is ten, then the final result will consist of three repeats of the pattern followed by
     * the first character of the pattern, for an end result that is exactly ten characters long.
     * @param pattern The pattern to repeat.
     * @param length The length of the final result.
     * @return An {@link CharSequence} containing the pattern repeated until it is exactly the required length.
     */
    public static CharSequence repeat(String pattern, int length)
    {
        StringBuilder result = new StringBuilder();

        int remaining = length;
        while(remaining > 0)
        {
            if (remaining < pattern.length()) result.append(pattern.substring(0, remaining));
            else result.append(pattern);

            // Update the remaining length
            remaining = length - result.length();
        }

        return result;
    }

    /**
     * Constructs a string composed of the given character repeated to fill out a desired length.
     * @param c The character to repeat
     * @param length The desired length
     * @return A {@link CharSequence} of the requested length
     */
    public static CharSequence repeat(char c, int length)
    {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < length; i++) result.append(c);

        return result;
    }

    /**
     * Insert a repeating pattern of characters before a given string.
     *
     * @param str The supplied "content" string to pad.
     * @param width The final total width of the string.
     * @param padding The string pattern to copy before the content string.
     * @return A {@code String} of length {@code width} characters, containing zero or more copies of the padding string
     * followed by the content string.
     */
    public static String padBefore(String str, int width, String padding)
    {
        // Short circuit if there is no padding needed.
        if (str.length() >= width) return str;

        int padLength = width - str.length();

        return StringTools.repeat(padding, padLength) + str;
    }

    public static String padAfter(String str, int width, String padding)
    {
        // Short circuit if there is no padding needed.
        if (str.length() >= width) return str;

        int padLength = width - str.length();

        return str + StringTools.repeat(padding, padLength);
    }

    public static String padBefore(String str, int width, char padding)
    {
        // Short circuit if there is no padding needed.
        if (str.length() >= width) return str;

        int padLength = width - str.length();

        return StringTools.repeat(padding, padLength) + str;
    }

    public static String padAfter(String str, int width, char padding)
    {
        // Short circuit if there is no padding needed.
        if (str.length() >= width) return str;

        int padLength = width - str.length();

        return str + StringTools.repeat(padding, padLength);
    }
}
