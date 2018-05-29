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
 * Generic type parameter.
 *
 * @author joseluis
 */
public class MTemplateParameter extends MClass {

//<editor-fold defaultstate="collapsed" desc="field">
    private MEntity parametrized;
    private final List<MClass> genericParameterConstraints = new ArrayList<>();
//</editor-fold>

    public MTemplateParameter(String id) {
        super(id);
    }

    @Override
    public boolean isTemplateParameter() {
        return true;
    }

    public MEntity getParametrized() {
        return parametrized;
    }

    public void setParametrized(MEntity value) {
        parametrized = value;
    }

    public List<MClass> getGenericParameterConstraints() {
        return genericParameterConstraints;
    }
}
