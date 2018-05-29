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
import java.util.stream.Collectors;
import org.essence.modeliohelper.core.BaseList;

/**
 *
 * @author joseluis
 */
public class MClass extends MNamespace {

//<editor-fold defaultstate="collapsed" desc="fields">
    private String fullNamespace;
    private ClassType classType;

    private MClass elementType;
    private int min, max;
    //private final List<MTemplateParameter> genericArguments = new ArrayList<>();

    private final List<MClass> inheritance = new ArrayList<>();

    private final List<MMethod> methods = new MemberList<>();
    private final List<MProperty> properties = new MemberList<>();
    private final List<MAssociationEnd> associationEnds = new MemberList<>();
    private final List<MField> fields = new MemberList<>();
//</editor-fold>

    public MClass(String id) {
        super(id);
    }

    public boolean isClass() {
        return classType == ClassType.CLASS;
    }

    public boolean isInterface() {
        return classType == ClassType.INTERFACE;
    }

    public boolean isEnum() {
        return classType == ClassType.ENUM;
    }

    public boolean isArray() {
        return isSet(Modifiers.ARRAY);
    }

    public boolean isPrimitive() {
        return classType == ClassType.PRIMITIVE;
    }

    public boolean isTemplateDefinition() {
        return false;
    }

    public boolean isTemplateInstanciation() {
        return false;
    }

    public boolean isTemplateParameter() {
        return false;
    }

    public ClassType getClassType() {
        return classType;
    }

    public void setClassType(ClassType value) {
        classType = value;
    }

    //public boolean isGeneric() {
    //    return (getGenericArguments().size() > 0);
    //}
    public void setFullNamespace(String value) {
        fullNamespace = value;
    }

    public MClass getElementType() {
        return elementType;
    }

    public void setElementType(MClass value) {
        elementType = value;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int value) {
        min = value;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int value) {
        max = value;
    }

    //public List<MTemplateParameter> getGenericArguments() {
    //    return genericArguments;
    //}
    public String getFullName() {
        StringBuilder buff = new StringBuilder();
        if (isArray()) {
            buff.append(getElementType().getFullName());
            buff.append("[]");
        } else {
            if (!Utils.isNullOrEmpty(getFullNamespace())) {
                buff.append(getFullNamespace());
                buff.append(".");
            }
            buff.append(getName());
            //if (isGeneric()) {
            //    buff.append("<");
            //    buff.append(getGenericArguments().stream().map(x -> x.getFullName()).reduce((x, y) -> x + ", " + y).orElse(""));
            //    buff.append(">");
            //}
        }
        return buff.toString();
    }

    public List<MClass> getInheritance() {
        return inheritance;
    }

    public MClass getExtends() {
        return getInheritance().stream()
                .filter(x -> x.isClass())
                .findFirst()
                .orElse(null);
    }

    public List<MClass> getImplements() {
        return getInheritance().stream()
                .filter(x -> x.isInterface())
                .collect(Collectors.toList());
    }

    public List<MMethod> getMethods() {
        return methods;
    }

    public List<MProperty> getProperties() {
        return properties;
    }

    public List<MAssociationEnd> getAssociationEnds() {
        return associationEnds;
    }

    public List<MField> getFields() {
        return fields;
    }

    public MMethod findOperation(String id) {
        return getMethods().stream()
                .filter(o -> Utils.equals(o.getId(), id))
                .findFirst()
                .orElse(null);
    }

    public MProperty findProperty(String id) {
        return getProperties().stream()
                .filter(o -> Utils.equals(o.getId(), id))
                .findFirst()
                .orElse(null);
    }

    public MAssociationEnd findAssociationEnd(String id) {
        return getAssociationEnds().stream()
                .filter(o -> Utils.equals(o.getId(), id))
                .findFirst()
                .orElse(null);
    }

    public MField findField(String id) {
        return getFields().stream()
                .filter(o -> Utils.equals(o.getId(), id))
                .findFirst()
                .orElse(null);
    }

//<editor-fold defaultstate="collapsed" desc="MNamespace">
    @Override
    public String getFullNamespace() {
        if (fullNamespace != null) {
            return fullNamespace;
        }
        return super.getFullNamespace();
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="inner classes">
    private class MemberList<T extends MMember> extends BaseList<T> {

        @Override
        public void onSet(int index, T oldItem, T newItem) {
            oldItem.setDeclaringClass(null);
            newItem.setDeclaringClass(MClass.this);
        }

        @Override
        public void onAdd(int index, T newItem) {
            newItem.setDeclaringClass(MClass.this);
        }

        @Override
        public void onRemove(int index, T oldItem) {
            oldItem.setDeclaringClass(null);
        }
    }
//</editor-fold>
}
