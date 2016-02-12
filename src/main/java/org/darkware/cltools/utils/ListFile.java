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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * A {@code ListFile} is a convenience facade around some standard file reading and
 * parsing routines that extract a list of strings from a file. The parsing routines
 * include code that trims whitespace and comments, translates escaped characters,
 * and ignores any empty lines after all trimming.
 *
 * @author jeff
 * @since 2016-02-11
 */
public class ListFile implements Iterable<String>
{
    /**
     * The set of default comment tokens to strip from lines.
     */
    protected static final String[] standardCommentTokens = { "#", ";", "//" };

    /**
     * Attempt to match a comment in the given line, within the stated bounds. This is
     * a reusable function which is called by {@link #stripLine(String)} to find comment
     * fragments to remove from the line data.
     *
     * @param line The raw line to search.
     * @param start The lowest index to search from.
     * @param end The highest index to search to.
     * @param commentToken The comment string initiator to search for.
     * @return The index of the start of the detected comment, or {@code -1} if no comment
     * was found.
     */
    protected static int matchComment(final String line, final int start, final int end, String commentToken)
    {
        int commentLook = start;
        while (commentLook < end)
        {
            int commentStart = line.indexOf(commentToken, commentLook);
            if (commentStart == -1) break;

            if (commentStart == start || line.charAt(commentStart-1) != '\\')
            {
                return commentStart;
            }
            else commentLook = commentStart+1;
        }

        return -1;
    }

    private final Path file;
    private final AtomicBoolean loaded;
    private boolean failed;
    private final List<String> lines;
    private final Set<String> commentTokens;

    /**
     * Create a new list file reader for the file at the given path.
     *
     * @param file The path of the file to read.
     */
    public ListFile(final Path file)
    {
        super();

        this.file = file;
        this.loaded = new AtomicBoolean(false);
        this.failed = false;
        this.lines = new ArrayList<>();
        this.commentTokens = new HashSet<>();
        this.setCommentTokens(ListFile.standardCommentTokens);
    }

    /**
     * Create a new list file reader for the file at the given path.
     *
     * @param filePath The path of the file to read.
     */
    public ListFile(final String filePath)
    {
        this(Paths.get(filePath));
    }

    /**
     * Set the list of comment initiators which will be stripped from lines. Any previously
     * added initiators will be removed and replaced with this list. Duplicate contents are
     * ignored.
     *
     * <p>Note that this is not retroactive. It must be set before the file data is read.</p>
     *
     * @param commentTokens The tokens to set.
     */
    public void setCommentTokens(final String ... commentTokens)
    {
        synchronized (this.loaded)
        {
            this.commentTokens.clear();
            this.addCommentTokens(commentTokens);
        }
    }

    /**
     * Adds new comment initiators which will be stripped from lines. Duplicate tokens are
     * ignored.
     *
     * <p>Note that this is not retroactive. It must be set before the file data is read.</p>
     *
     * @param commentTokens The tokens to set.
     */
    public void addCommentTokens(final String ... commentTokens)
    {
        synchronized (this.loaded)
        {
            this.commentTokens.clear();
            Arrays.asList(commentTokens).stream().forEach(this.commentTokens::add);
        }
    }

    /**
     * Read the data from the file and store it internally for later retrieval. This method
     * is implicitly invoked by any other method which fetches data, however, if you wish to
     * control when the work of parsing the file is performed, you may invoke it manually.
     * Multiple invocations don't read the data multiple times. If the method is called after
     * the data has been previously read, the method returns immediately with no effect.
     */
    public void read()
    {
        synchronized (this.loaded)
        {
            if (this.loaded.compareAndSet(false, true))
            {
                try (BufferedReader reader = new BufferedReader(Channels.newReader(FileChannel.open(this.file, StandardOpenOption.READ), "utf-8")))
                {
                    // Clear the lines, just in case
                    this.lines.clear();

                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        String data = this.stripLine(line);
                        if (data != null) this.lines.add(data);
                    }
                }
                catch (IOException e)
                {
                    // We failed. Sad. But still, if we fail there's no point in trying again.
                    this.failed = true;

                    // Also clear the contents. A failure code is better than partial success
                    this.lines.clear();
                }
            }
        }
    }

    /**
     * Strips non-data from a single line of data from the file. This will remove:
     * <ul>
     *   <li>Whitespace at the start of the line.</li>
     *   <li>Whitespace at the end of the line.</li>
     *   <li>Any text that follows one of the registered comment initiators, when that
     *       initiator is not preceded by a backslash.</li>
     * </ul>
     * <p>Following the stripping of non-data fragments, any escaped characters are
     * translated back to native representations. This includes the removal of escaping
     * for comment initiators as well as the standard {@code \t}, {@code \r}, and
     * {@code \n} escaped tokens.</p>
     *
     * @param line The line to strip.
     * @return The stripped data from the line or {@code null} if stripping resulted in
     * a zero-length data fragment.
     */
    protected String stripLine(final String line)
    {
        int start = 0;
        int end = line.length();

        // Short circuit for empty lines
        if (end == 0) return null;

        // Strip leading whitespace
        while (start < end && Character.isWhitespace(line.charAt(start))) start++;

        // Short circuit for whitespace lines
        if (start == end) return null;

        // Trim comments
        for (String commentToken : this.commentTokens)
        {
            int commentStart = ListFile.matchComment(line, start, end, commentToken);
            if (commentStart > -1) end = commentStart;
        }

        // Strip trailing whitespace
        while (start < end && Character.isWhitespace(line.charAt(end - 1))) end--;

        // Short circuit for empty lines
        if (start >= end) return null;

        StringBuilder translated = new StringBuilder((end-start)+1);
        for (int i = start; i < end; i++)
        {
            char c = line.charAt(i);
            if (c == '\\')
            {
                char c2 = line.charAt(++i);
                switch (c2)
                {
                    case 'n': translated.append('\n'); break;
                    case 't': translated.append('\t'); break;
                    case 'r': translated.append('\r'); break;
                    default: translated.append(c2);
                }
            }
            else translated.append(c);
        }

        return translated.toString();
    }

    /**
     * Checks to see if the file parsing failed. This method will cause the file to be
     * read if it has not been read yet.
     *
     * @return {@code true} if the file parsing failed for any reason, {@code false} if
     * it succeeded.
     */
    public boolean failed()
    {
        this.read();
        return this.failed;
    }

    @Override
    public Iterator<String> iterator()
    {
        this.read();
        return lines.iterator();
    }

    /**
     * Fetch the complete list of lines. The list cannot be modified.
     *
     * @return The lines from the file as a {@code List}.
     */
    public List<String> getLines()
    {
        this.read();
        return Collections.unmodifiableList(this.lines);
    }

    /**
     * Fetch the count of lines parsed from the file.
     *
     * @return The line count, as a non-negative integer.
     */
    public int lineCount()
    {
        this.read();
        return this.lines.size();
    }

    /**
     * Fetch the content of the given line number. Note that this is the line number of
     * the parsed content. Empty lines or comment lines are ignored and not calculated in
     * the count. Consult {@link #lineCount()} to get the parsed line count.
     *
     * @param lineNumber The line number to fetch.
     * @return The parsed content of the line number.
     * @throws IllegalArgumentException If the line number is negative.
     * @throws IllegalArgumentException If the line number is greater than or equal to the line count.
     */
    public String getLine(final int lineNumber)
    {
        if (lineNumber < 0) throw new IllegalArgumentException("Line numbers cannot be negative");
        if (lineNumber >= this.lines.size()) throw new IllegalArgumentException("Requested line number not available");

        return this.lines.get(lineNumber);
    }

    /**
     * Fetch a {@link Stream} for the file contents. Each valid line is an entry in the stream.
     *
     * @return A {@code Stream} associated with the lines from the file.
     */
    public Stream<String> stream()
    {
        this.read();
        return this.lines.stream();
    }
}
