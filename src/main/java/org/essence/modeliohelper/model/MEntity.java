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

import java.util.ArrayList;
import java.util.List;
import org.essence.modeliohelper.core.BaseList;

/**
 * This class represents a model entity.
 *
 * @author joseluis
 */
public class MEntity {

//<editor-fold defaultstate="collapsed" desc="fields">
    private String id;
    private int modifiers;
    private String name;
    private final List<MStereotype> stereotypes = new ArrayList<>();
    private final List<MDependency> dependencies = new BaseList<MDependency>() {

        @Override
        public void onSet(int index, MDependency oldItem, MDependency newItem) {
            oldItem.setSource(null);
            newItem.setSource(MEntity.this);
        }

        @Override
        public void onAdd(int index, MDependency newItem) {
            newItem.setSource(MEntity.this);
        }

        @Override
        public void onRemove(int index, MDependency oldItem) {
            oldItem.setSource(null);
        }
    };
    private String documentation = "";
//</editor-fold>

    public MEntity(String id) {
        setId(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        id = value;
    }

    public int getModifiers() {
        return modifiers;
    }

    public void setModifiers(int value) {
        modifiers = value;
    }

    public void set(int mask) {
        modifiers |= mask;
    }

    public boolean isSet(int mask) {
        return Modifiers.isSet(modifiers, mask);
    }

    public boolean isPublic() {
        return isSet(Modifiers.PUBLIC);
    }

    public boolean isProtected() {
        return isSet(Modifiers.PROTECTED);
    }

    public boolean isPrivate() {
        return isSet(Modifiers.PRIVATE);
    }

    public boolean isPackage() {
        return isSet(Modifiers.PACKAGE);
    }

    public boolean isStatic() {
        return isSet(Modifiers.STATIC);
    }

    public boolean isAbstract() {
        return isSet(Modifiers.ABSTRACT);
    }

    public boolean isFinal() {
        return isSet(Modifiers.FINAL);
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public List<MStereotype> getStereotypes() {
        return stereotypes;
    }

    public List<MDependency> getDependencies() {
        return dependencies;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String value) {
        if (value == null) {
            value = "";
        }
        documentation = value;
    }

    public String[] getDocumentationLines() {
        return getDocumentation().split("\\r?\\n");
    }

    public void addDocumentation(String value) {
        if (Utils.isNullOrEmpty(value)) {
            return;
        }
        if (Utils.isNullOrEmpty(documentation)) {
            documentation = value;
        } else {
            documentation = documentation + System.lineSeparator() + value;
        }
    }
}
