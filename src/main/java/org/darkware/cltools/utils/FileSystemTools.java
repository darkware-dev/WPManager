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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This is a collection of utility methods for performing common actions against the
 * local filesystem.
 *
 * @author jeff
 * @since 2016-02-05
 */
public class FileSystemTools
{
    /**
     * Checks the given {@link Path} against the given tests.
     * <p>The available tests are:</p>
     * <table>
     *   <tr><th>d</th><td>Test if the path is a directory.</td></tr>
     *   <tr><th>e</th><td>Test if the path exists.</td></tr>
     *   <tr><th>f</th><td>Test if the path is a regular file.</td></tr>
     *   <tr><th>r</th><td>Test if the path is readable.</td></tr>
     *   <tr><th>s</th><td>Test if the path is a non-zero-length file.</td></tr>
     *   <tr><th>w</th><td>Test if the path is writable.</td></tr>
     *   <tr><th>x</th><td>Test if the path is executable or usable.</td></tr>
     *   <tr><th>z</th><td>Test if the path is a zero length file.</td></tr>
     * </table>
     *
     * @param path The {@code Path} to check.
     * @param tests The set of tests to run.
     * @return {@code false} if any test fails, otherwise {@code true}.
     * @see #testPath(Path, char)
     */
    public static boolean check(final Path path, final CharSequence tests)
    {
        for (int i = 0; i < tests.length(); i++)
        {
            FileSystemTestFailure failure = FileSystemTools.testPath(path, tests.charAt(i));
            if (failure != null) return false;
        }

        return true;
    }

    /**
     * Checks the given {@link Path} against the given tests. Unlike
     * {@link #check(Path, CharSequence)}, a test failure will throw an
     * {@link FileSystemTestFailure}.
     *
     * @param path The {@code Path} to check.
     * @param tests The set of tests to run.
     * @see #testPath(Path, char)
     * @throws FileSystemTestFailure If any of the tests fail.
     */
    public static void require(final Path path, final CharSequence tests)
    {
        for (int i = 0; i < tests.length(); i++)
        {
            FileSystemTestFailure failure = FileSystemTools.testPath(path, tests.charAt(i));
            if (failure != null) throw failure;
        }
    }

    /**
     * Execute a single filesystem test against the given path.
     *
     * @param path The {@code Path} to test
     * @param test The character code of the test to perform.
     * @return {@code null} if the test passes, or a {@code FileSystemTestFailure} if it fails.
     */
    protected static FileSystemTestFailure testPath(final Path path, char test)
    {
        switch (test)
        {
            case 'e': return (Files.exists(path)) ? null : new FileSystemTestFailure(path, test, "The path does not exist");
            case 'f': return (Files.isRegularFile(path)) ? null : new FileSystemTestFailure(path, test, "The path is not a file");
            case 'd': return (Files.isDirectory(path)) ? null : new FileSystemTestFailure(path, test, "The path is not a directory");
            case 'z':
                try
                {
                    if (!Files.isRegularFile(path)) return new FileSystemTestFailure(path, test, "The path is not a file");
                    return (Files.size(path) < 1) ? null : new FileSystemTestFailure(path, test, "The path is not zero length");
                }
                catch (IOException ioe)
                {
                    return new FileSystemTestFailure(path, test, "The path could not be read");
                }
            case 's':
                try
                {
                    if (!Files.isRegularFile(path)) return new FileSystemTestFailure(path, test, "The path is not a file");
                    return (Files.size(path) > 0) ? null : new FileSystemTestFailure(path, test, "The path is a zero length file");
                }
                catch (IOException ioe)
                {
                    return new FileSystemTestFailure(path, test, "The path could not be read");
                }
            case 'r': return (Files.isReadable(path)) ? null : new FileSystemTestFailure(path, test, "The path is not readable");
            case 'w': return (Files.isWritable(path)) ? null : new FileSystemTestFailure(path, test, "The path is not writable");
            case 'x': return (Files.isExecutable(path)) ? null : new FileSystemTestFailure(path, test, "The path is not executable");
            default:
                throw new IllegalArgumentException("Unrecognized path test: " + test);
        }
    }

    /**
     * This is an exception which indicates a given {@link Path} failed some requested test.
     */
    public static class FileSystemTestFailure extends IllegalStateException
    {
        private final Path path;
        private final char test;

        /**
         * Create a new {@code FileSystemTestFailure}.
         *
         * @param path The path that failed the test.
         * @param test The test id code.
         * @param description A description of the test failure.
         */
        public FileSystemTestFailure(final Path path, final char test, final String description)
        {
            super(description + ": " + path);

            this.path = path;
            this.test = test;
        }

        /**
         * Fetch the path that failed the test.
         *
         * @return A {@code Path} object.
         */
        public Path getPath()
        {
            return path;
        }

        /**
         * The test id code of the test that failed.
         *
         * @return A {@code char} indicating the failed test.
         */
        public char getTest()
        {
            return test;
        }
    }
}
