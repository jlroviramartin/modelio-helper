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

import java.util.List;
import org.essence.modeliohelper.command.HPrinter;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.metamodel.uml.infrastructure.ModelTree;
import org.modelio.metamodel.uml.infrastructure.Stereotype;
import org.modelio.metamodel.uml.infrastructure.UmlModelElement;
import org.modelio.metamodel.uml.statik.Classifier;
import org.modelio.metamodel.uml.statik.Interface;
import org.modelio.metamodel.uml.statik.NameSpace;
import org.modelio.metamodel.uml.statik.TemplateParameter;
import org.modelio.vcore.smkernel.mapi.MObject;

/**
 *
 * @author joseluis
 */
public class ModelPrinter {

    public static void print(MObject element, HPrinter writer) {
        if (element instanceof org.modelio.metamodel.uml.statik.Class) {
            ModelPrinter.printClass((org.modelio.metamodel.uml.statik.Class) element, writer);
        } else if (element instanceof Interface) {
            ModelPrinter.printClass((Interface) element, writer);
        } else if (element instanceof NameSpace) {
            NameSpace ns = (NameSpace) element;

            writer.printGroup(
                    ns.getName(),
                    () -> {
                        ns.getOwnedElement().stream().forEach(x -> print(x, writer));
                    });
        } else if (element instanceof ModelTree) {
            ModelTree mt = (ModelTree) element;

            writer.printGroup(
                    mt.getName(),
                    () -> {
                        mt.getOwnedElement().stream().forEach(x -> print(x, writer));
                    });
        }
    }

    public static void printClass(Classifier cl, HPrinter writer) {
        writer.printGroup(
                cl.getName(),
                () -> {
                    if (cl instanceof org.modelio.metamodel.uml.statik.Class) {
                        writer.printProperty("Type", "Class");
                    } else if (cl instanceof Interface) {
                        writer.printProperty("Type", "interface");
                    }

                    printNotes(cl, writer);
                    printStereotypes(cl, writer);
                    printConstraints(cl, writer);

                    writer.printProperty("Uuid", cl.getUuid());
                    writer.printProperty("Namespace", getFullNamespace(cl));

                    writer.printProperty("Visibility", cl.getVisibility());
                    writer.printProperty("Abstract", cl.isIsAbstract());
                    writer.printProperty("Modifiable", cl.isModifiable());

                    // Between a class and the classes that are implemented by the class
                    // or between an interface and the interfaces that are implemented by the interface.
                    writer.printGroup(
                            "Parents",
                            cl.getParent(),
                            gen -> "Parent",
                            gen -> {
                                writer.printProperty("Uuid", gen.getUuid());
                                writer.printProperty("Discriminator", gen.getDiscriminator());
                                writer.printProperty("SuperType", getFullNamespace(gen.getSuperType()));
                                writer.printProperty("SubType", getFullNamespace(gen.getSubType()));
                            });

                    // Between a class and the interfaces that are implemented by the class.
                    writer.printGroup(
                            "Realizations",
                            cl.getRealized(),
                            ireal -> "Realization",
                            ireal -> {
                                writer.printProperty("Uuid", ireal.getUuid());
                                writer.printProperty("Implemented", getSafeName(ireal.getImplemented()));
                                writer.printProperty("Implementer", getSafeName(ireal.getImplementer()));
                            });

                    writer.printGroup(
                            "Templates",
                            cl.getTemplate(),
                            tpar -> "TemplateParameter",
                            tpar -> {
                                printTemplateParameter(tpar, writer);
                            });

                    writer.printGroup(
                            "TemplateInstanciations",
                            cl.getTemplateInstanciation(),
                            tbin -> "TemplateBinding",
                            tbin -> {
                                writer.printProperty("Uuid", tbin.getUuid());
                                writer.printProperty("InstanciatedTemplate", getFullNamespace(tbin.getInstanciatedTemplate()));
                                writer.printProperty("BoundElement", getFullNamespace(tbin.getBoundElement()));
                                writer.printProperty("InstanciatedTemplateOperation", getSafeName(tbin.getInstanciatedTemplateOperation()));
                                writer.printProperty("BoundOperation", getSafeName(tbin.getBoundOperation()));

                                writer.printGroup(
                                        "ParameterSubstitutions",
                                        tbin.getParameterSubstitution(),
                                        psus -> "ParameterSubstitution",
                                        psus -> {
                                            writer.printProperty("Uuid", psus.getUuid());
                                            writer.printProperty("Owner", getSafeName(psus.getOwner()));
                                            writer.printProperty("Value", psus.getValue());
                                            writer.printProperty("Actual", getSafeName(psus.getActual()));
                                            writer.printProperty("Actual.class", psus.getActual().getClass());

                                            // FormalParameter
                                            writer.printGroup(
                                                    "FormalParameter",
                                                    () -> printTemplateParameter(psus.getFormalParameter(), writer));
                                        });
                            });

                    // Between a class and the classes that implements the class.
                    // It is (+ -) the inverse of Parents. It is contained in the Parent.
                    writer.printGroup(
                            "Specializations",
                            cl.getSpecialization(),
                            gen -> "Specialization",
                            gen -> {
                                writer.printProperty("Uuid", gen.getUuid());
                                writer.printProperty("Discriminator", gen.getDiscriminator());
                                writer.printProperty("SuperType", getFullNamespace(gen.getSuperType()));
                                writer.printProperty("SubType", getFullNamespace(gen.getSubType()));
                            });

                    // Between an interface and the interfaces that implements the interface.
                    // It is (+ -) the inverse of Parents. It is contained in the Parent.
                    if (cl instanceof Interface) {
                        writer.printGroup(
                                "ImplementedLinks",
                                ((Interface) cl).getImplementedLink(),
                                ireal -> "ImplementedLink",
                                ireal -> {
                                    writer.printProperty("Uuid", ireal.getUuid());
                                    writer.printProperty("Implemented", getFullNamespace(ireal.getImplemented()));
                                    writer.printProperty("Implementer", getFullNamespace(ireal.getImplementer()));
                                });
                    }

                    writer.printGroup(
                            "Operations",
                            cl.getOwnedOperation(),
                            op -> op.getName(),
                            op -> {
                                printNotes(op, writer);
                                printStereotypes(op, writer);
                                printConstraints(op, writer);

                                writer.printProperty("Uuid", op.getUuid());

                                if (op.getReturn() != null) {
                                    writer.printGroup(
                                            "return",
                                            () -> {
                                                printNotes(op.getReturn(), writer);
                                                printStereotypes(op.getReturn(), writer);
                                                printConstraints(op.getReturn(), writer);

                                                writer.printProperty("Uuid", op.getReturn().getUuid());
                                                writer.printProperty("Type", getFullNamespace(op.getReturn().getType()));
                                                writer.printProperty("Min", op.getReturn().getMultiplicityMin());
                                                writer.printProperty("Max", op.getReturn().getMultiplicityMax());
                                            });
                                }

                                writer.printGroup(
                                        "parameters",
                                        op.getIO(),
                                        parameter -> parameter.getName(),
                                        parameter -> {
                                            printNotes(parameter, writer);
                                            printStereotypes(parameter, writer);
                                            printConstraints(parameter, writer);

                                            writer.printProperty("Uuid", parameter.getUuid());
                                            writer.printProperty("Type", getFullNamespace(parameter.getType()));
                                            writer.printProperty("Min", parameter.getMultiplicityMin());
                                            writer.printProperty("Max", parameter.getMultiplicityMax());
                                        });
                            });

                    writer.printGroup(
                            "Attributes",
                            cl.getOwnedAttribute(),
                            attribute -> attribute.getName(),
                            attribute -> {
                                printNotes(attribute, writer);
                                printStereotypes(attribute, writer);
                                printConstraints(attribute, writer);

                                writer.printProperty("Uuid", attribute.getUuid());
                                writer.printProperty("Type", getFullNamespace(attribute.getType()));
                                writer.printProperty("Visibility", attribute.getVisibility());
                            });

                    writer.printGroup(
                            "AssociationEnds",
                            cl.getOwnedEnd(),
                            end -> end.getName(),
                            end -> {
                                printNotes(end, writer);
                                printStereotypes(end, writer);
                                printConstraints(end, writer);

                                writer.printProperty("Uuid", end.getUuid());
                                writer.printProperty("Visibility", end.getVisibility());
                                writer.printProperty("Navigable", end.isNavigable());
                                writer.printProperty("IsChangeable", end.isIsChangeable());
                                writer.printProperty("Changeable", end.getChangeable());
                                writer.printProperty("Ordered", end.isIsOrdered());
                                writer.printProperty("Abstract", end.isIsAbstract());
                                writer.printProperty("Aggregation", end.getAggregation());
                                writer.printProperty("Source", getFullNamespace(end.getSource()));
                                writer.printProperty("Target", getFullNamespace(end.getTarget()));
                            });

                });
    }

    private static void printTemplateParameter(TemplateParameter tpar, HPrinter writer) {
        writer.printProperty("Uuid", tpar.getUuid());
        writer.printProperty("Namespace", getFullNamespace(tpar));

        writer.printProperty("IsValueParameter", tpar.isIsValueParameter());
        writer.printProperty("DefaultValue", tpar.getDefaultValue());

        writer.printProperty("Type", getSafeName(tpar.getType()));
        writer.printProperty("DefaultType", getSafeName(tpar.getDefaultType()));
        writer.printProperty("OwnedParameterElement", getSafeName(tpar.getOwnedParameterElement()));

        writer.printProperty("Parameterized", getFullNamespace(tpar.getParameterized()));
        writer.printProperty("ParameterizedOperation", getSafeName(tpar.getParameterizedOperation()));
    }

    private static void printStereotypes(ModelElement me, HPrinter writer) {
        writer.printGroup(
                "Stereotypes",
                me.getExtension(),
                st -> st.getName(),
                st -> {
                    printTaggedValues(me, st, writer);
                });
    }

    private static void printTaggedValues(ModelElement me, Stereotype st, HPrinter writer) {
        writer.printGroup(
                "Stereotypes",
                st.getDefinedTagType(),
                tag -> tag.getName(),
                tag -> {
                    String moduleName = getSafeName(tag.getModule());
                    String tagName = tag.getName();
                    List<String> tagValues = me.getTagValues(moduleName, tagName); // ???
                    if (tagValues != null) {
                        writer.printProperty("Value",
                                             me.getTagValues(moduleName, tagName)
                                                     .stream()
                                                     .reduce((x, y) -> x + ", " + y)
                                                     .orElse(""));
                    }
                });
    }

    private static void printNotes(ModelElement me, HPrinter writer) {
        writer.printGroup(
                "Notes",
                me.getDescriptor(),
                n -> "Note",
                n -> {
                    printStereotypes(n, writer);
                    if (n.getModel() != null) {
                        writer.printProperty("Model", n.getModel());
                        writer.printProperty("Model.LabelKey", n.getModel().getLabelKey());
                        writer.printProperty("Model.MimeType", n.getModel().getMimeType());
                        writer.printProperty("Model.IsHidden", n.getModel().isIsHidden());
                        if (n.getModel().getOwnerStereotype() != null) {
                            printStereotypes(n.getModel().getOwnerStereotype(), writer);
                        }
                    }
                    writer.printProperty("Subject", n.getSubject() != null ? n.getSubject().getName() : "");
                    writer.printProperty("MimeType", n.getMimeType());
                    writer.printProperty("Content", n.getContent());
                });
    }

    private static void printConstraints(UmlModelElement me, HPrinter writer) {
        writer.printGroup(
                "Constraints",
                me.getConstraintDefinition(),
                n -> "Constraint",
                n -> {
                    printStereotypes(n, writer);
                    writer.printProperty("BaseClass", n.getBaseClass());
                    writer.printProperty("Body", n.getBody());
                    writer.printProperty("Language", n.getLanguage());
                });
    }

    /**
     * This method gets the full namespace of {@code me}.
     *
     * @param me Model element.
     * @return Full namespace.
     */
    private static String getFullNamespace(NameSpace me) {
        ModelTree owner = me.getOwner();
        String str = "";
        if (owner instanceof NameSpace) {
            str = getFullNamespace((NameSpace) owner) + ".";
        }
        return str + getSafeName(me);
    }

    public static String getSafeName(ModelElement me) {
        if (me != null) {
            return me.getName();
        }
        return "<null>";
    }
}
