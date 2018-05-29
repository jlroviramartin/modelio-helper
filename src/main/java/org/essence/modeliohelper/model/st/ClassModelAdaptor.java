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
package org.essence.modeliohelper.model.st;

import org.essence.modeliohelper.model.MAssociationEnd;
import org.essence.modeliohelper.model.MClass;
import org.essence.modeliohelper.model.MProperty;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

/**
 *
 * @author joseluis
 */
public class ClassModelAdaptor extends ObjectModelAdaptor {

    private static final String TEMPLATE_FOR_FIELD = "templateForField";
    private static final String TEMPLATE_FOR_ACCESSORS = "templateForAccessors";

    @Override
    public synchronized Object getProperty(Interpreter interp, ST self, Object o, Object property, String propertyName)
            throws STNoSuchPropertyException {
        if (o instanceof MClass) {

        } else if (o instanceof MProperty) {
            MProperty mproperty = (MProperty) o;

            if (TEMPLATE_FOR_FIELD.equals(property)) {
                return "Class_PropertyField";
            } else if (TEMPLATE_FOR_ACCESSORS.equals(property)) {
                return "Class_PropertyAccessors";
            }
        } else if (o instanceof MAssociationEnd) {
            MAssociationEnd mend = (MAssociationEnd) o;

            if (TEMPLATE_FOR_FIELD.equals(property)) {
                return "Class_PropertyField";
            } else if (TEMPLATE_FOR_ACCESSORS.equals(property)) {
                return "Class_PropertyAccessors";
            }
        } else if (o instanceof MProperty) {
            MProperty mproperty = (MProperty) o;

            if (TEMPLATE_FOR_FIELD.equals(property)) {
                return "Class_PropertyField";
            } else if (TEMPLATE_FOR_ACCESSORS.equals(property)) {
                return "Class_PropertyAccessors";
            }
        }

        return super.getProperty(interp, self, o, property, propertyName);
    }
}
