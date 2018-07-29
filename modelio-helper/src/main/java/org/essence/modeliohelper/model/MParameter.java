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
 * This class represents a parameter from a {@code MMethod}.
 *
 * @author joseluis
 */
public class MParameter {

//<editor-fold defaultstate="collapsed" desc="fields">
    private String name;
    private MClass type;
    private final List<MStereotype> stereotypes = new ArrayList<>();
    private String documentation;
//</editor-fold>

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public MClass getType() {
        return type;
    }

    public void setType(MClass value) {
        type = value;
    }

    public List<MStereotype> getStereotypes() {
        return stereotypes;
    }

    public String getDocumentation() {
        return documentation;
    }

    public String[] getDocumentationLines() {
        return getDocumentation().split("\\r?\\n");
    }

    public void setDocumentation(String value) {
        documentation = value;
    }
}
