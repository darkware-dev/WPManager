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

package org.darkware.wpman.data;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author jeff
 * @since 2016-01-23
 */
public class VersionTests
{
    @Test
    public void testCompare_reflexive()
    {
        Version a = new Version("1.2.3");
        Version b = new Version("1.2.3");

        assertEquals(0, a.compareTo(b));
        assertEquals(0, b.compareTo(a));
    }

    @Test
    public void testCompare_negative()
    {
        Version a = new Version("1.2.3");
        Version b = new Version("1.2.5");

        assertEquals(-1, a.compareTo(b));
        assertEquals(1, b.compareTo(a));
        assertNotEquals(a, b);
    }

    @Test
    public void testCompare_negative_differentElementCount()
    {
        Version a = new Version("1.2");
        Version b = new Version("1.2.5");

        assertNotEquals(a, b);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
    }

    @Test
    public void testCompare_twoelements()
    {
        Version a = new Version("4.5");
        Version b = new Version("4.5");

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
    }

    @Test
    public void testCompare_majorChange()
    {
        Version a = new Version("1.2.3");
        Version b = new Version("2.1.3");

        assertEquals(-1, a.compareTo(b));
        assertEquals(1, b.compareTo(a));

        List<Version> versions = new ArrayList<>();
        versions.add(a);
        versions.add(b);
        Collections.sort(versions);

        assertEquals(a, versions.get(0));
        assertEquals(b, versions.get(1));
    }

    @Test
    public void testCompare_minorChange()
    {
        Version a = new Version("2.1.3");
        Version b = new Version("2.2.3");

        assertEquals(-1, a.compareTo(b));
        assertEquals(1, b.compareTo(a));

        List<Version> versions = new ArrayList<>();
        versions.add(a);
        versions.add(b);
        Collections.sort(versions);

        assertEquals(a, versions.get(0));
        assertEquals(b, versions.get(1));
    }

    @Test
    public void testCompare_newLevel()
    {
        Version a = new Version("2.3");
        Version b = new Version("2.3.1");

        assertEquals(-1, a.compareTo(b));
        assertEquals(1, b.compareTo(a));

        List<Version> versions = new ArrayList<>();
        versions.add(a);
        versions.add(b);
        Collections.sort(versions);

        assertEquals(a, versions.get(0));
        assertEquals(b, versions.get(1));
    }

    @Test
    public void testCompare_newLevel_extra()
    {
        Version a = new Version("2.3.4.8.1");
        Version b = new Version("2.3.4");

        assertEquals(1, a.compareTo(b));
        assertEquals(-1, b.compareTo(a));

        List<Version> versions = new ArrayList<>();
        versions.add(a);
        versions.add(b);
        Collections.sort(versions);

        assertEquals(b, versions.get(0));
        assertEquals(a, versions.get(1));
    }


}
