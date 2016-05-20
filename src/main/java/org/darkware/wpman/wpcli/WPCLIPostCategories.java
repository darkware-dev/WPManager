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

import org.darkware.wpman.data.WPTerm;

import java.util.HashSet;
import java.util.Set;

/**
 * This is a specialized {@link WPCLIOption} for collecting categories while creating posts.
 *
 * @author jeff
 * @since 2016-05-18
 */
public class WPCLIPostCategories extends WPCLIBasicOption
{
    private final Set<WPTerm> categories;

    /**
     * Create a new set of post categories. Initially, the set is empty.
     */
    public WPCLIPostCategories()
    {
        super("post_category");

        this.categories = new HashSet<>();
    }

    /**
     * Create a new set of post categories using the supplied terms.
     *
     * @param categories A list of {@link WPTerm}s representing post categories to add.
     */
    public WPCLIPostCategories(final WPTerm ... categories)
    {
        this();

        for (WPTerm term : categories) this.addCategory(term);
    }

    /**
     * Add a category to the set. Duplicate categories are ignored.
     *
     * @param category The category to add.
     */
    public void addCategory(final WPTerm category)
    {
        this.categories.add(category);
    }

    @Override
    protected CharSequence renderValue()
    {
        if (this.categories.size() < 1) return null;
        StringBuilder value = new StringBuilder();
        for (WPTerm category : this.categories)
        {
            if (value.length() > 0) value.append(',');
            value.append(String.valueOf(category.getId()));
        }

        return value;
    }
}
