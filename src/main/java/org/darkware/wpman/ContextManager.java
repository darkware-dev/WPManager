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

package org.darkware.wpman;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jeff
 * @since 2016-01-31
 */
public class ContextManager
{
    private static final ContextManagerThreadLocal localContext = new ContextManagerThreadLocal();

    public static ContextManager local()
    {
        return ContextManager.localContext.get();
    }

    public static void attach(final ContextManager contextManager)
    {
        ContextManager.localContext.set(contextManager);
    }

    private final Map<Type, Object> instances;

    public ContextManager()
    {
        super();

        this.instances = new ConcurrentHashMap<>();
    }

    public <T> T getContextualInstance(T example)
    {
        return this.getContextualInstance(TypeToken.of((Class<? extends T>)example.getClass()));
    }


    public <T> T getContextualInstance(Class<T> instanceClass)
    {
        return this.getContextualInstance(TypeToken.of(instanceClass));
    }

    public <T> T getContextualInstance(TypeToken<T> typeToken)
    {
        try
        {
            final T instance = (T)this.instances.get(typeToken.getType());

            if (instance == null)
            {
                throw new ContextManagerException("No instance registered for type " + typeToken);
            }
            else return instance;
        }
        catch (ClassCastException cce)
        {
            throw new ContextManagerException("Stored instance for type " + typeToken + " did not match declared type.", cce);
        }
    }

    public <T> void registerInstance(T instance)
    {
        final Type instanceType = TypeToken.of(instance.getClass()).getType();
        this.instances.put(instanceType, instance);
        WPManager.log.debug("Registered instance for: {}@{}", instanceType, Thread.currentThread().getId());
    }

    private final static class ContextManagerException extends RuntimeException
    {
        public ContextManagerException(final String message)
        {
            super(message);
        }

        public ContextManagerException(final String message, final Throwable cause)
        {
            super(message, cause);
        }
    }

    private static final class ContextManagerThreadLocal extends InheritableThreadLocal<ContextManager>
    {
        @Override
        protected ContextManager initialValue()
        {
            return new ContextManager();
        }
    }
}
