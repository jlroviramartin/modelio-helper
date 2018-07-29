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
import java.util.Map;

/**
 * Generic type definition, from which other generic types can be constructed.
 *
 * @author joseluis
 */
public class MTemplateDefinition extends MClass {

//<editor-fold defaultstate="collapsed" desc="fields">
    private final List<MTemplateParameter> templateArguments = new ArrayList<>();
//</editor-fold>

    public MTemplateDefinition(String id) {
        super(id);
    }

    @Override
    public boolean isTemplateDefinition() {
        return true;
    }

    public List<MTemplateParameter> getTemplateArguments() {
        return templateArguments;
    }

    public MTemplateParameter getTemplateArgument(String name) {
        return templateArguments.stream()
                .filter(x -> Utils.equals(x.getName(), name))
                .findFirst()
                .orElse(null);
    }

    public MTemplateInstanciation instantiate(Map<String, MClass> arguments) {
        MTemplateInstanciation templateInstanciation = new MTemplateInstanciation("");
        templateInstanciation.setName(getName());
        templateInstanciation.setNamespace(getNamespace());
        templateInstanciation.setClassType(getClassType());
        templateInstanciation.setGenericTypeDefinition(this);

        for (MTemplateParameter templateArgument : getTemplateArguments()) {
            MClass arg = arguments.get(templateArgument.getName());
            templateInstanciation.getGenericTypeArguments().add(arg);
        }
        return templateInstanciation;
    }

    @Override
    public String getFullName() {
        StringBuilder buff = new StringBuilder(super.getFullName());
        buff.append("<");
        buff.append(getTemplateArguments().stream()
                .map(x -> x.getFullName())
                .reduce((x, y) -> x + ", " + y)
                .orElse(""));
        buff.append(">");
        return buff.toString();
    }
}
