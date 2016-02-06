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

package org.darkware.cltools.options;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * @author jeff
 * @since 2016-01-24
 */
public class SimpleOption<T> implements Option<T>
{
    private T value;
    private T defaultValue;

    public SimpleOption()
    {
        super();
    }

    @Override
    public void setDefault(final T value)
    {
        this.defaultValue = value;
    }

    @Override
    public void set(final T value)
    {
        synchronized (this)
        {
            this.value = value;
        }
    }

    @Override
    public void reset()
    {
        synchronized (this)
        {
            this.value = null;
        }
    }

    @Override
    public Type getType()
    {
        return new TypeToken<T>(){}.getType();
    }
}
