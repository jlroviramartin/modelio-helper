/* 
 * Copyright (C) 2018 joseluis.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.essence.modeliohelper.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A proxy-based event multicaster for any listener-interface. Uses automatically generated proxy-class to receive calls
 * which will be forwarded to all registered listeners.
 *
 * @author Juha Komulainen
 */
public final class EventMulticaster<T> {

    /**
     * Thread-safe list of registered listeners
     */
    private final List<T> listeners = new CopyOnWriteArrayList<>();

    /**
     * Proxy for multicasting the events
     */
    private final T multicaster;

    /**
     * Constructs a multicaster for given listener-interface.
     *
     * @param type of the listener-interface
     */
    @SuppressWarnings("unchecked")
    public EventMulticaster(Class<T> type) {
        this.multicaster = (T) Proxy.newProxyInstance(type.getClassLoader(),
                                                      new Class[]{type},
                                                      new MyInvocationHandler());
    }

    /**
     * Registers a new listener for receiving the events.
     *
     * @param listener Listener to add.
     */
    public void addListener(T listener) {
        if (listener == null) {
            throw new NullPointerException("null listener");
        }

        this.listeners.add(listener);
    }

    /**
     * Removes the listener.
     *
     * @param listener Listener to remove.
     */
    public void removeListener(T listener) {
        this.listeners.remove(listener);
    }

    /**
     * Returns the multicaster. All methods called on the multicaster will end up being called on all registered
     * listeners (unless one of them throws an exception, in which case the exception is thrown and processing stops).
     *
     * @return Multicaster.
     */
    public T getMulticaster() {
        return this.multicaster;
    }

    /**
     * Return if the internal list had listeners.
     *
     * @return If empty.
     */
    public boolean isEmpty() {
        return this.listeners.isEmpty();
    }

    /**
     * The invocation-handler which will receive the calls made to proxy and will forward them to the listeners.
     */
    private class MyInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            try {
                for (T listener : EventMulticaster.this.listeners) {
                    method.invoke(listener, args);
                }
                return null;
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

    }
}
