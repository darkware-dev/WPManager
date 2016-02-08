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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author jeff
 * @since 2016-02-07
 */
public class ObjectFactory
{
    /**
     * Create a new instance of an object from a {@code String}.
     *
     * @param value The string value to convert.
     * @param valueClass The class of object to create.
     * @param <T> The type to return.
     * @return An object of class {@code T}.
     * @throws BuildException If any errors are encountered while creating the object.
     */
    public static <T> T fromString(final String value, Class<T> valueClass) throws BuildException
    {
        // Find a constructor
        try
        {
            Constructor<T> constructor = valueClass.getConstructor(String.class);
            return constructor.newInstance(value);
        }
        catch (NoSuchMethodException e)
        {
            throw new BuildException("No simple string constructor for class: " + valueClass, e);
        }
        catch (IllegalAccessException e)
        {
            throw new BuildException("Access to the constructor is forbidden: " + valueClass, e);
        }
        catch (InstantiationException e)
        {
            throw new BuildException("The class doesn't support instantiation: " + valueClass, e);
        }
        catch (InvocationTargetException e)
        {
            throw new BuildException("An exception was thrown during construction: " + valueClass, e);
        }
    }

    /**
     * A {@code BuildException} is a wrapper exception which encapsulates all exceptions which
     * might occur during construction.
     */
    public static class BuildException extends RuntimeException
    {
        /**
         * Creates a new {@code BuildException}.
         *
         * @param message A description of the error encountered
         * @param cause The exception which caused the error.
         */
        public BuildException(final String message, final Throwable cause)
        {
            super(message, cause);
        }
    }
}
