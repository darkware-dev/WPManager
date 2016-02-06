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

import com.google.common.base.Objects;

/**
 * A customized abstraction of WordPress's official versioning scheme. This should be
 * suitable for storing and comparing versions of core releases, plugins, and themes.
 *
 * <p>This class performs a decomposition of the version to support logical comparison
 * between versions.
 * </p>
 *
 * @author jeff
 * @since 2016-01-23
 */
public class Version implements Comparable<Version>
{
    private String stringValue;
    private int[] parts;

    /**
     * Converts the given version string into a {@link Comparable} object.
     *
     * @param versionString The version, as a {@code String}.
     */
    public Version(String versionString)
    {
        super();

        this.stringValue = versionString;

        String [] strParts = versionString.split("\\.");
        this.parts = new int[strParts.length];
        for (int i = 0; i < strParts.length; i++)
        {
            this.parts[i] = Integer.parseInt(strParts[i]);
        }
    }

    /**
     * Checks to see if the passed {@code Version} is more recent than this {@code Version}.
     *
     * @param that The {@code Version} to compare.
     * @return {@code true} if the compared version is greater, {@code false} if it is less
     * than or equal to this version.
     */
    public boolean greaterThan(Version that)
    {
        return this.compareTo(that) > 0;
    }

    /**
     * Checks to see if the passed {@code Version} is older than this {@code Version}.
     *
     * @param that The {@code Version} to compare.
     * @return {@code true} if the compared version is less, {@code false} if it is greater
     * than or equal to this version.
     */
    public boolean lessThan(Version that)
    {
        return this.compareTo(that) < 0;
    }

    @Override
    public int compareTo(final Version that)
    {
        for (int i = 0; i < this.parts.length; i++)
        {
            if (that.parts.length <= i) return 1;
            if (this.parts[i] < that.parts[i]) return -1;
            if (this.parts[i] > that.parts[i]) return 1;
        }

        // Check if that still has more fields
        if (that.parts.length > this.parts.length) return -1;

        return 0;
    }

    @Override
    public String toString()
    {
        return this.stringValue;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;
        final Version version = (Version) o;
        return Objects.equal(parts, version.parts);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(parts);
    }
}
