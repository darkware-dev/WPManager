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

package org.darkware.wpman.util;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertTrue;

/**
 * @author jeff
 * @since 2016-04-18
 */
public class TimeWindowTests
{
    @Test
    public void testNext_simple_exhaustive()
    {
        LocalDateTime now = LocalDateTime.now();

        for (int h = 0; h < 24; h++)
            for (int m = 0; m < 60; m++)
            {
                LocalDateTime time = TimeWindow.nextTime(h, m);
                if (!time.isAfter(now))
                {
                    System.out.printf("Failed: now vs. %d:%02d (%s)\n", h, m, time);
                }
                assertTrue(time.isAfter(now));
            }
    }

    @Test
    public void testNext_base_exhaustive()
    {
        LocalDateTime base = LocalDateTime.now().plus(1, ChronoUnit.HOURS).plus(34, ChronoUnit.MINUTES);

        for (int h = 0; h < 24; h++)
            for (int m = 0; m < 60; m++)
            {
                LocalDateTime time = TimeWindow.nextTime(base, h, m);
                if (!time.isAfter(base))
                {
                    System.out.printf("Failed: now vs. %d:%02d (%s)\n", h, m, time);
                }
                assertTrue(time.isAfter(base));
            }
    }

    @Test
    public void testRandomMoment_exhaustive()
    {
        for (int h = 0; h < 24; h++)
            for (int m = 0; m < 60; m++)
            {
                LocalDateTime start = TimeWindow.nextTime(h, m);
                LocalDateTime stop = start.plus(22 + h + m, ChronoUnit.MINUTES);
                TimeWindow window = new TimeWindow(start, stop);

                LocalDateTime rand = window.getRandomMoment();
                assertTrue(rand.equals(start) || rand.isAfter(start));
                assertTrue(rand.equals(stop) || rand.isBefore(stop));
            }
    }

}
