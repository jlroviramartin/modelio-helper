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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author joseluis
 */
public class MStereotype {

//<editor-fold defaultstate="collapsed" desc="fields">
    private String module;
    private String profile;
    private String name;
    private boolean hidden;
    private final Map<String, String[]> taggedValues = new HashMap<>();
//</editor-fold>

    public String getModule() {
        return module;
    }

    public void setModule(String value) {
        module = value;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String value) {
        profile = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public String getProfileName() {
        return String.format("%1$s.%2$s", getProfile(), getName());
    }

    public String getFullName() {
        return String.format("%1$s.%2$s.%3$s", getModule(), getProfile(), getName());
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean value) {
        hidden = value;
    }

    public Map<String, String[]> getTaggedValues() {
        return taggedValues;
    }
}
