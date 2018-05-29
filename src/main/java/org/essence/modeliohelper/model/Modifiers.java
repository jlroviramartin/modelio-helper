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

public class Modifiers {

    public static final int PUBLIC = 1;
    public static final int PROTECTED = 2;
    public static final int PRIVATE = 4;
    public static final int PACKAGE = 8;

    public static final int STATIC = 16;

    public static final int ABSTRACT = 32;
    public static final int FINAL = 64;

    public static final int ARRAY = 1024;

    public static boolean isSet(int modifiers, int mask) {
        return (modifiers & mask) == mask;
    }
}
