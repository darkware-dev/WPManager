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

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author jeff
 * @since 2016-01-23
 */
public class CSVTests
{
    @Test
    public void parseSimple()
    {
        String csv = "Apple,Banana,Citron";

        List<List<String>> csvParts = CSV.parse(csv);

        assertEquals("Apple", csvParts.get(0).get(0));
        assertEquals("Banana", csvParts.get(0).get(1));
        assertEquals("Citron", csvParts.get(0).get(2));
    }

    @Test
    public void parseQuoted()
    {
        String csv = "Apple,Banana,Citron,\"Durian Fruit\"";

        List<List<String>> csvParts = CSV.parse(csv);

        assertEquals("Apple", csvParts.get(0).get(0));
        assertEquals("Banana", csvParts.get(0).get(1));
        assertEquals("Citron", csvParts.get(0).get(2));
        assertEquals("Durian Fruit", csvParts.get(0).get(3));
    }

    @Test
    public void parseQuoteception()
    {
        String csv = "Lincoln,Einstein,\"Jake \\\"The Snake\\\" Roberts\"";

        List<List<String>> csvParts = CSV.parse(csv);

        assertEquals("Lincoln", csvParts.get(0).get(0));
        assertEquals("Einstein", csvParts.get(0).get(1));
        assertEquals("Jake \"The Snake\" Roberts", csvParts.get(0).get(2));
    }

}
