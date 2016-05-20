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

package org.darkware.wpman.wpcli;

import java.util.HashSet;
import java.util.Set;

/**
 * This is a specialized {@link WPCLIOption} for collecting tags while creating posts.
 *
 * @author jeff
 * @since 2016-05-18
 */
public class WPCLIPostTags extends WPCLIBasicOption
{
    private final Set<String> tags;

    /**
     * Create a new set of post tags. Initially, the set is empty.
     */
    public WPCLIPostTags()
    {
        super("tags_input");

        this.tags = new HashSet<>();
    }

    /**
     * Create a new set of post tags using the supplied tags.
     *
     * @param tags A list of {@code String}s representing post tags to add.
     */
    public WPCLIPostTags(final String ... tags)
    {
        this();

        for (String tag : tags) this.addTag(tag);
    }

    /**
     * Add a tag to the set. Duplicate tags are ignored.
     *
     * @param tag The tag to add.
     */
    public void addTag(final String tag)
    {
        this.tags.add(tag);
    }

    @Override
    protected CharSequence renderValue()
    {
        if (this.tags.size() < 1) return null;
        StringBuilder value = new StringBuilder();
        for (String field : this.tags)
        {
            if (value.length() > 0) value.append(',');
            value.append(field);
        }

        return value;
    }
}
