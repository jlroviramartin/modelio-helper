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
 * This class represents a method from a {@code MClass}.
 *
 * @author joseluis
 */
public class MMethod extends MMember {

//<editor-fold defaultstate="collapsed" desc="fields">
    private boolean constructor;
    private boolean destructor;
    private final List<MTemplateParameter> templateArguments = new ArrayList<>();
    private MParameter _return;
    private final ArrayList<MParameter> parameters = new ArrayList<>();
//</editor-fold>

    public MMethod(String id) {
        super(id);
    }

    @Override
    public boolean isMethod() {
        return true;
    }

    public boolean isConstructor() {
        return constructor;
    }

    public void setConstructor(boolean value) {
        constructor = value;
    }

    public boolean isDestructor() {
        return destructor;
    }

    public void setDestructor(boolean value) {
        destructor = value;
    }

    public List<MTemplateParameter> getTemplateArguments() {
        return templateArguments;
    }

    public MTemplateParameter getTemplateArgument(String name) {
        return getTemplateArguments()
                .stream()
                .filter(x -> Utils.equals(x.getName(), name))
                .findFirst()
                .orElse(null);
    }

    public MParameter getReturn() {
        return _return;
    }

    public void setReturn(MParameter value) {
        _return = value;
    }

    public List<MParameter> getParameters() {
        return parameters;
    }
}
