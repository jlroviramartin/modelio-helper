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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author joseluis
 */
public class BaseList<T> extends AbstractList<T> {

    private final List<T> inner;

    public BaseList() {
        this(new ArrayList<T>());
    }

    public BaseList(List<T> list) {
        inner = list;
    }

    @Override
    public T get(int index) {
        return inner.get(index);
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public T set(int index, T item) {
        T oldItem = inner.set(index, item);
        onSet(index, oldItem, item);
        return oldItem;
    }

    @Override
    public void add(int index, T item) {
        inner.add(index, item);
        onAdd(index, item);
    }

    @Override
    public T remove(int index) {
        T oldItem = inner.remove(index);
        onRemove(index, oldItem);
        return oldItem;
    }

    protected void onSet(int index, T oldItem, T newItem) {
    }

    protected void onAdd(int index, T newItem) {
    }

    protected void onRemove(int index, T oldItem) {
    }
}
