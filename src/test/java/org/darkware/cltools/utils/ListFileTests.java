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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author jeff
 * @since 2016-02-11
 */
public class ListFileTests
{
    private ListFile lf = new ListFile("foo");

    @Test
    public void testLineStrip_unneeded()
    {
        assertEquals("Banana", lf.stripLine("Banana"));
    }

    @Test
    public void testLineStrip_trailingWhitespace()
    {
        assertEquals("Banana", lf.stripLine("Banana "));
    }

    @Test
    public void testLineStrip_leadingWhitespace()
    {
        assertEquals("Apple", lf.stripLine("  Apple"));
    }

    @Test
    public void testLineStrip_multipleWhitespace()
    {
        assertEquals("Cara Cara Orange", lf.stripLine("  Cara Cara Orange  \n"));
    }

    @Test
    public void testLineStrip_tabs()
    {
        assertEquals("Figs", lf.stripLine("\tFigs \n"));
    }

    @Test
    public void testLineStrip_hashComment()
    {
        assertEquals("Guava", lf.stripLine("Guava # Testing"));
    }

    @Test
    public void testLineStrip_falseHashComment()
    {
        assertEquals("Guava # Testing", lf.stripLine("Guava \\# Testing"));
    }

    @Test
    public void testLineStrip_doubleShashComment()
    {
        assertEquals("Guava", lf.stripLine("Guava // Testing"));
    }

    @Test
    public void testLineStrip_falseDoubleSlashComment()
    {
        assertEquals("Guava // Testing", lf.stripLine("Guava \\// Testing"));
    }

    @Test
    public void testLineStrip_emptyLine()
    {
        assertNull(lf.stripLine(""));
    }

    @Test
    public void testLineStrip_whitespaceLine()
    {
        assertNull(lf.stripLine("   \n"));
    }

    @Test
    public void testLineStrip_commentLine()
    {
        assertNull(lf.stripLine("# Comment Line\n"));
    }

    @Test
    public void testLineStrip_complexCommentLine()
    {
        assertNull(lf.stripLine(" # Comment Line\n  // What the... "));
    }
}
