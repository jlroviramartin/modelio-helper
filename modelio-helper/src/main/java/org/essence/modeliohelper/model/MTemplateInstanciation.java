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

/**
 * Closed generic Type.
 *
 * @author joseluis
 */
public class MTemplateInstanciation extends MClass {

//<editor-fold defaultstate="collapsed" desc="fields">
    private MTemplateDefinition genericTypeDefinition;
    private final List<MClass> genericTypeArguments = new ArrayList<>();
//</editor-fold>

    public MTemplateInstanciation(String id) {
        super(id);
    }

    @Override
    public boolean isTemplateInstanciation() {
        return true;
    }

    public MTemplateDefinition getGenericTypeDefinition() {
        return genericTypeDefinition;
    }

    public void setGenericTypeDefinition(MTemplateDefinition value) {
        genericTypeDefinition = value;
    }

    public List<MClass> getGenericTypeArguments() {
        return genericTypeArguments;
    }

    @Override
    public String getFullName() {
        StringBuilder buff = new StringBuilder(super.getFullName());
        buff.append("<");
        buff.append(getGenericTypeArguments().stream()
                .map(x -> x.getFullName())
                .reduce((x, y) -> x + ", " + y)
                .orElse(""));
        buff.append(">");
        return buff.toString();
    }
}
