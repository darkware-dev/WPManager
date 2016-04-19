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

import org.apache.commons.lang3.RandomUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;

/**
 * A {@code TimeWindow} is a helper object for generating {@link LocalDateTime} or {@link Duration} objects which
 * fall within a given span of time.
 *
 * @author jeff
 * @since 2016-04-11
 */
public class TimeWindow
{
    /**
     * Calculates the next time when a given hour and minute occur, based from the current time.
     *
     * @param hour The hour to search for.
     * @param minute The minute to search for.
     * @return A {@code DateTime} corresponding to the hour and minute declared which is explicitly after
     * the current time.
     */
    public static LocalDateTime nextTime(final int hour, final int minute)
    {
        return TimeWindow.nextTime(LocalDateTime.now(), hour, minute);
    }

    /**
     * Calculates the next time when a given hour and minute occur, based from the given start time.
     *
     * @param after The time to start searching from.
     * @param hour The hour to search for.
     * @param minute The minute to search for.
     * @return A {@code DateTime} corresponding to the hour and minute declared which is explicitly after
     * the start time.
     */
    public static LocalDateTime nextTime(final LocalDateTime after, final int hour, final int minute)
    {
        LocalTime time = LocalTime.of(hour, minute);
        LocalDate afterDate = after.toLocalDate();
        if (!time.isAfter(after.toLocalTime())) afterDate = afterDate.plus(1, ChronoUnit.DAYS);
        return time.atDate(afterDate);
    }

    private LocalDateTime earliest;
    private LocalDateTime latest;

    /**
     * Creates a new time window with the declared start and stop times.
     *
     * @param earliest The earliest allowed time in the window.
     * @param latest The latest allowed time in the window.
     */
    public TimeWindow(final LocalDateTime earliest, final LocalDateTime latest)
    {
        super();

        this.earliest = earliest;
        this.latest = latest;
    }

    /**
     * Creates a new time window with the declared start and a given duration.
     *
     * @param earliest The earliest allowed time in the window.
     * @param duration The length of the window.
     */
    public TimeWindow(final LocalDateTime earliest, final TemporalAmount duration)
    {
        this(earliest, earliest.plus(duration));
    }

    /**
     * Creates a new time window the with declared start and stop times. This particular method uses an
     * admittedly bizarre combination of parameters which marginally improves the performance of the form
     * that takes two hour (or hour-minute) specifications.
     *
     * @param earliest The earliest allowed time in the window.
     * @param latestHour The latest allowed time in the window, as the next hour after the start.
     * @param latestMinute The minutes past the latest hour allowed.
     */
    protected TimeWindow(final LocalDateTime earliest, final int latestHour, final int latestMinute)
    {
        this(earliest, TimeWindow.nextTime(earliest, latestHour, latestMinute));
    }

    /**
     * Creates a new time window based on the declared hour and minute pairs. The time specifications are
     * interpreted as starting on the first time after the present matching the hour and minute, and ending at
     * the first time after the start which matches the ending hour and minute.
     *
     * @param earliestHour The hour of the start time for the window.
     * @param earliestMinute The minutes past the hour for the start time of the window.
     * @param latestHour The hour of the end time for the window.
     * @param latestMinute The minutes past the hour for the end time of the window.
     */
    public TimeWindow(final int earliestHour, final int earliestMinute, final int latestHour, final int latestMinute)
    {
        this(TimeWindow.nextTime(earliestHour, earliestMinute), latestHour, latestMinute);
    }

    /**
     * Creates a new time window based on the declared hours. This is equivalent to calling
     * {@link #TimeWindow(int, int, int, int)} with the minutes set to 0.
     *
     * @param earliestHour The hour of the start time for the window.
     * @param latestHour The hour of the end time for the window.
     * @see #TimeWindow(int, int, int, int)
     */
    public TimeWindow(final int earliestHour, final int latestHour)
    {
        this(earliestHour, 0, latestHour, 0);
    }

    /**
     * Fetch the earliest moment in this time window.
     *
     * @return A {@code DateTime} representing the earliest possible time in this window.
     */
    public LocalDateTime getEarliestMoment()
    {
        return this.earliest;
    }

    /**
     * Fetch the latest moment in this time window.
     *
     * @return A {@code DateTime} representing the latest possible time in this window.
     */
    public LocalDateTime getLatestMoment()
    {
        return this.latest;
    }

    /**
     * Fetch a random moment falling somewhere inside this time window.
     *
     * @return A {@code DateTime} representing a random moment which is explicitly between the earliest
     * and latest moments in the window.
     */
    public LocalDateTime getRandomMoment()
    {
        long durationSeconds = this.earliest.until(this.latest, ChronoUnit.SECONDS);
        return this.earliest.plus(RandomUtils.nextLong(0, durationSeconds), ChronoUnit.SECONDS);
    }

    /**
     * Fetch a time offset to a random moment within the time window.
     *
     * @return A number of seconds to a moment that is within the time window.
     */
    public Duration getRandomOffset()
    {
        return Duration.between(LocalDateTime.now(), this.getRandomMoment());
    }
}
