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
package org.essence.modeliohelper.model;

/**
 *
 * @author joseluis
 */
public class Utils {

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNullOrWhiteSpace(String str) {
        return str == null || str.chars().allMatch(x -> Character.isWhitespace((char) x));
    }

    /**
     * This method is similar to the C# {@code as} operator.
     *
     * @param <T> Type to cast
     * @param type Type to cast.
     * @param o Object to cast.
     * @return Value.
     */
    public static <T> T as(Class<T> type, Object o) {
        if (type.isInstance(o)) {
            return (T) o;
        }
        return null;
    }

    public static boolean equals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        return o1.equals(o2);
    }
}
