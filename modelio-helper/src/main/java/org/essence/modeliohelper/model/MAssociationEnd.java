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
 * This class represents an association end from a {@code MClass}.
 *
 * @author joseluis
 */
public class MAssociationEnd extends MProperty {

//<editor-fold defaultstate="collapsed" desc="fields">
    private MAssociationEnd otherEnd;
    private AssociationEndType associationEndType;
//</editor-fold>

    public MAssociationEnd(String id) {
        super(id);
    }

    public boolean isAssociationEnd() {
        return true;
    }

    public MAssociationEnd getOtherEnd() {
        return otherEnd;
    }

    public void setOtherEnd(MAssociationEnd value) {
        otherEnd = value;
    }

    public AssociationEndType getAssociationEndType() {
        return associationEndType;
    }

    public void setAssociationEndType(AssociationEndType value) {
        associationEndType = value;
    }
}
