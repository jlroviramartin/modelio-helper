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

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.essence.modeliohelper.core.EventMulticaster;
import org.essence.modeliohelper.model.event.FoundClassEvent;
import org.essence.modeliohelper.model.event.FoundClassListener;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.metamodel.uml.infrastructure.ModelTree;
import org.modelio.metamodel.uml.infrastructure.Stereotype;
import org.modelio.metamodel.uml.infrastructure.Usage;
import org.modelio.metamodel.uml.statik.AggregationKind;
import org.modelio.metamodel.uml.statik.AssociationEnd;
import org.modelio.metamodel.uml.statik.Attribute;
import org.modelio.metamodel.uml.statik.Classifier;
import org.modelio.metamodel.uml.statik.Enumeration;
import org.modelio.metamodel.uml.statik.EnumerationLiteral;
import org.modelio.metamodel.uml.statik.Interface;
import org.modelio.metamodel.uml.statik.KindOfAccess;
import org.modelio.metamodel.uml.statik.NameSpace;
import org.modelio.metamodel.uml.statik.Operation;
import org.modelio.metamodel.uml.statik.Parameter;
import org.modelio.metamodel.uml.statik.PassingMode;
import org.modelio.metamodel.uml.statik.TemplateBinding;
import org.modelio.metamodel.uml.statik.TemplateParameter;
import org.modelio.metamodel.uml.statik.TemplateParameterSubstitution;
import org.modelio.metamodel.uml.statik.VisibilityMode;

/**
 * This class build a model.
 *
 * @author joseluis
 */
public class ModelBuilder {

//<editor-fold defaultstate="collapsed" desc="fields">
    private static final Logger LOGGER = Logger.getLogger(ModelBuilder.class.getName());
    private final Map<String, MClass> cacheClass = new HashMap<>();
    private static final MClass VOID_CLASS = new MClass(null);

    private final MNamespace root = new MNamespace(null);
    private final EventMulticaster<FoundClassListener> foundClassListeners = new EventMulticaster<>(FoundClassListener.class);

    static {
        VOID_CLASS.setName("void");
    }

    private final Stack<Function<Classifier, MClass>> classResolver = new Stack<>();

    {
        pushResolver(cl -> {
            // To prevent infinite recursive calls.
            if (cl instanceof TemplateParameter) {
                throw new Error(String.format("'TemplateParameter' cannot be resolved: %1$s",
                                              ((TemplateParameter) cl).getName()));
            }
            return this.buildClass(cl);
        });
    }
//</editor-fold>

    public ModelBuilder() {
    }

    public void processClasses(Consumer<MClass> classFounded) {
        ArrayDeque<MNamespace> stack = new ArrayDeque<>();
        stack.addLast(getRoot());
        while (!stack.isEmpty()) {
            MNamespace ns = stack.removeLast();
            if (ns instanceof MClass) {
                classFounded.accept((MClass) ns);
            }

            ns.getOwnedNamespaces().forEach(x -> stack.addLast(x));
        }
    }

    public MNamespace getRoot() {
        return root;
    }

    public void addMapping(String nameToMap, MClass mclass) {
        cacheClass.put(nameToMap, mclass);
    }

    public void addMapping(String nameToMap, String name, String fullNamespace, ClassType type, int modifiers) {
        MClass mclass = new MClass(null);
        mclass.setName(name);
        mclass.setFullNamespace(fullNamespace);
        mclass.setClassType(type);
        mclass.set(modifiers);
        addMapping(nameToMap, mclass);
    }

    /**
     * This method adds mapping from UML standard types to Java types.
     */
    public void addUmlMapping() {
        MClass integerClass = new MClass(null);
        integerClass.setName("int");
        integerClass.setClassType(ClassType.PRIMITIVE);
        addMapping("UML Types.integer", integerClass);

        MClass stringClass = new MClass(null);
        stringClass.setName("String");
        stringClass.setFullNamespace("java.lang");
        stringClass.setClassType(ClassType.CLASS);
        stringClass.set(Modifiers.FINAL);
        addMapping("UML Types.string", stringClass);
    }

    public MNamespace build(NameSpace element) {
        MNamespace mentity = buildVirtual(element);
        root.getOwnedNamespaces().add(mentity);
        return mentity;
    }

    /**
     * This method builds a {@code MNamespace} from the {@code NameSpace} {@code ns}.
     *
     * @param nameSpace NameSpace.
     * @return Model namespace.
     */
    public MNamespace buildNamespace(NameSpace nameSpace) {
        MNamespace mnamespace = new MNamespace(nameSpace.getUuid());
        mnamespace.setName(nameSpace.getName());

        // Inner classes
        mnamespace.getOwnedNamespaces().addAll(
                nameSpace.getOwnedElement().stream()
                        .filter(x -> NameSpace.class.isInstance(x))
                        .map(x -> buildVirtual((NameSpace) x))
                        .collect(Collectors.toList()));

        // Dependencies
        buildDependencies(nameSpace);

        return mnamespace;
    }

    public MClass buildClassWithMultiplicity(Classifier classifier, int min, int max) {
        MClass mclass = resolve(classifier);

        if (min == 0 && max == 1 && mclass.isPrimitive()) {
        } else if (max > 1) {
            MClass marray = new MClass("");
            marray.set(Modifiers.ARRAY);
            marray.setElementType(mclass);
            marray.setMin(min);
            marray.setMax(max);

            mclass = marray;
        }
        return mclass;
    }

    /**
     * This method builds a {@code MClass} from the {@code Classifier} {@code cl}.
     *
     * @param classifier Classifier.
     * @return Model class.
     */
    public MClass buildClass(Classifier classifier) {
        // TemplateParameter
        if (classifier instanceof TemplateParameter) {
            // It is supposed to be called from: pushResolverForClass or pushResolverForMethod.
            return (MTemplateParameter) resolve(classifier);
        }

        // DataType
        // Class
        // Interface
        String fullName = getFullName(classifier);
        MClass mclass = cacheClass.get(fullName);
        if (mclass == null) {
            boolean isTemplateDef = false;

            if (!classifier.getTemplate().isEmpty()) {
                isTemplateDef = true;
                mclass = new MTemplateDefinition(classifier.getUuid());
            } else {
                mclass = new MClass(classifier.getUuid());
            }
            mclass.setId(classifier.getUuid());

            addMapping(fullName, mclass); // Prevents infinite recursiveness.

            final MClass mclassFinal = mclass;

            mclass.setName(classifier.getName());

            mclass.set(calculateModifiers(classifier.getVisibility()));
            if (classifier instanceof Enumeration) {
                mclass.setClassType(ClassType.ENUM);
            } else if (classifier instanceof Interface) {
                mclass.setClassType(ClassType.INTERFACE);
            } else if (classifier instanceof org.modelio.metamodel.uml.statik.Class) {
                if (classifier.isIsAbstract()) {
                    mclass.set(Modifiers.ABSTRACT);
                }
                if (!classifier.isModifiable()) {
                    mclass.set(Modifiers.FINAL);
                }
                mclass.setClassType(ClassType.CLASS);
            }

            // Stereotypes
            mclass.getStereotypes().addAll(buildStereotypes(classifier));

            // Template definition
            if (isTemplateDef) {
                classifier.getTemplate().stream()
                        .map(x -> buildTemplateParameter(x, mclassFinal))
                        .forEach(((MTemplateDefinition) mclass).getTemplateArguments()::add);

                pushResolverForClass((MTemplateDefinition) mclass, classifier);
            }

            // Using new resolver if it proceeds.
            {
                // Bindings for generalization/realization.
                List<TemplateBinding> bindings = classifier.getTemplateInstanciation();

                // Generalization
                classifier.getParent().stream()
                        .map(x -> x.getSuperType())
                        .filter(x -> x instanceof Classifier)
                        .map(x -> buildClassInheritance((Classifier) x, bindings))
                        .forEach(mclass.getInheritance()::add);

                // Realization
                classifier.getRealized().stream()
                        .map(x -> x.getImplemented())
                        .filter(x -> x instanceof Classifier)
                        .map(x -> buildClassInheritance((Classifier) x, bindings))
                        .forEach(mclass.getInheritance()::add);

                // Documentation
                mclass.setDocumentation(getDocumentation(classifier));

                // Methods
                classifier.getOwnedOperation().stream()
                        .map(this::buildOperation)
                        .forEach(mclass.getMethods()::add);

                // Properties
                classifier.getOwnedAttribute().stream()
                        .map(this::buildProperty)
                        .forEach(mclass.getProperties()::add);

                // Association ends
                classifier.getOwnedEnd().stream()
                        .filter(end -> end.isNavigable())
                        .map(this::buildEnd)
                        .forEach(mclass.getAssociationEnds()::add);

                // Enumeration literals
                if (classifier instanceof Enumeration) {
                    ((Enumeration) classifier).getValue().stream()
                            .map(this::buildEnumValue)
                            .forEach(mclass.getProperties()::add);
                }

                // Dependencies
                buildDependencies(classifier);

                // Inner classes
                classifier.getOwnedElement().stream()
                        .filter(x -> NameSpace.class.isInstance(x))
                        .map(x -> buildVirtual((NameSpace) x))
                        .forEach(mclass.getOwnedNamespaces()::add);
            }

            if (isTemplateDef) {
                popResolver();
            }

            foundClassListeners.getMulticaster().found(new FoundClassEvent(this, mclass));
        }
        return mclass;
    }

    private MClass buildClassInheritance(Classifier cl, List<TemplateBinding> bindings) {
        MClass mclass = resolve(cl);

        if (cl.getTemplate().isEmpty()) {
            return mclass;
        }

        // It is a template definition
        MTemplateDefinition templateDefinition = (MTemplateDefinition) mclass;

        // It finds the binding
        TemplateBinding binding = bindings.stream()
                .filter(x -> Utils.equals(x.getInstanciatedTemplate(), cl))
                .findFirst()
                .orElse(null);

        if (binding == null) {
            throw new Error(String.format("Cannot find Binding for %1$s",
                                          cl.getName()));
        }

        Map<String, MClass> arguments = new HashMap<>();
        for (TemplateParameterSubstitution substitution : binding.getParameterSubstitution()) {

            TemplateParameter formalParameter = substitution.getFormalParameter();
            Classifier actual = Utils.as(Classifier.class, substitution.getActual());

            if (actual == null) {
                throw new Error(String.format("'Actual' is null or is not a classifier: %1$s",
                                              substitution.getActual()));
            }
            arguments.put(formalParameter.getName(), resolve(actual));
        }

        MTemplateInstanciation templateInstanciation = templateDefinition.instantiate(arguments);
        return templateInstanciation;
    }

    /**
     * This method builds a {@code MMethod} from the {@code Operation} {@code op}.
     *
     * @param operation Operation.
     * @return Model method.
     */
    public MMethod buildOperation(Operation operation) {
        MMethod mmethod = new MMethod(operation.getUuid());

        mmethod.setName(operation.getName());
        mmethod.set(calculateModifiers(operation.getVisibility()));
        if (operation.isIsAbstract()) {
            mmethod.set(Modifiers.ABSTRACT);
        }
        if (!operation.isModifiable()) {
            mmethod.set(Modifiers.FINAL);
        }
        if (operation.isIsClass()) {
            mmethod.set(Modifiers.STATIC);
        }

        // Constructor
        Stereotype create = operation.getExtension().stream()
                .filter(x -> Utils.equals(x.getName(), "create"))
                .findFirst()
                .orElse(null);
        mmethod.setConstructor(create != null);

        // Destructor
        Stereotype destroy = operation.getExtension().stream()
                .filter(x -> Utils.equals(x.getName(), "destroy"))
                .findFirst()
                .orElse(null);
        mmethod.setDestructor(destroy != null);

        mmethod.getStereotypes().addAll(buildStereotypes(operation));

        // Templates
        operation.getTemplate().stream()
                .map(x -> buildTemplateParameter(x, mmethod))
                .forEach(mmethod.getTemplateArguments()::add);

        // Documentation
        mmethod.setDocumentation(getDocumentation(operation));

        // Dependencies
        buildDependencies(operation);

        // Template parameters must be included in the 'classResolver'.
        pushResolverForMethod(mmethod, operation);

        // Using new resolver.
        {
            MParameter _return;
            if (operation.getReturn() != null) {
                _return = buildParameter(operation.getReturn());

                // Documentation
                String aux = getDocumentation(operation.getReturn());
                if (aux != null) {
                    mmethod.addDocumentation(String.format("@return %1$s",
                                                           aux));
                }
            } else {
                _return = new MParameter();
                _return.setType(VOID_CLASS);
            }
            mmethod.setReturn(_return);

            mmethod.getParameters().addAll(
                    operation.getIO().stream()
                            .map(this::buildParameter)
                            .collect(Collectors.toList()));

            // Documentation
            operation.getIO().forEach(
                    par -> {
                        String aux = getDocumentation(par);
                        if (aux != null) {
                            mmethod.addDocumentation(String.format("@param %1$s %2$s",
                                                                   par.getName(),
                                                                   aux));
                        }
                    });
        }

        popResolver();

        return mmethod;
    }

    /**
     * This method builds a {@code MProperty} from the {@code Attribute} {@code attribute}.
     *
     * @param attribute Attribute.
     * @return Model property.
     */
    public MProperty buildProperty(Attribute attribute) {
        MProperty mproperty = new MProperty(attribute.getUuid());

        int min = parseMultiplicity(attribute.getMultiplicityMin());
        int max = parseMultiplicity(attribute.getMultiplicityMax());
        boolean ordered = attribute.isIsOrdered();

        mproperty.setName(attribute.getName());
        mproperty.setType(buildClassWithMultiplicity(attribute.getType(), min, max));
        mproperty.set(calculateModifiers(attribute.getVisibility()));
        if (attribute.isIsAbstract()) {
            mproperty.set(Modifiers.ABSTRACT);
        }
        if (!attribute.isModifiable() || attribute.getChangeable() == KindOfAccess.READ) {
            mproperty.set(Modifiers.FINAL);
        }
        if (attribute.isTargetIsClass()) {
            mproperty.set(Modifiers.STATIC);
        }

        // Stereotypes
        buildStereotypes(attribute).stream().forEach(mproperty.getStereotypes()::add);

        // Documentation
        mproperty.setDocumentation(getDocumentation(attribute));

        // Dependencies
        buildDependencies(attribute);

        String value = attribute.getValue();

        boolean unique = attribute.isIsUnique();
        boolean derived = attribute.isIsDerived();

        return mproperty;
    }

    /**
     * This method builds a {@code MAssociationEnd} from the {@code AssociationEnd} {@code end}.
     *
     * @param end Association end.
     * @return Model association end.
     */
    public MAssociationEnd buildEnd(AssociationEnd end) {
        if (!end.isNavigable()) {
            return null;
        }

        MAssociationEnd mend = new MAssociationEnd(end.getUuid());

        int min = parseMultiplicity(end.getMultiplicityMin());
        int max = parseMultiplicity(end.getMultiplicityMax());
        boolean ordered = end.isIsOrdered();

        mend.setName(end.getName());
        mend.setType(buildClassWithMultiplicity(end.getTarget(), min, max));
        mend.set(calculateModifiers(end.getVisibility()));
        if (end.isIsAbstract()) {
            mend.set(Modifiers.ABSTRACT);
        }
        if (!end.isModifiable() || end.getChangeable() == KindOfAccess.READ) {
            mend.set(Modifiers.FINAL);
        }
        if (end.isIsClass()) {
            mend.set(Modifiers.STATIC);
        }

        // Stereotypes
        mend.getStereotypes().addAll(buildStereotypes(end));

        // Documentation
        mend.setDocumentation(getDocumentation(end));

        AggregationKind aggregation = end.getAggregation();
        switch (aggregation) {
            case KINDISASSOCIATION:
                mend.setAssociationEndType(AssociationEndType.ASSOCIATION);
                break;
            case KINDISAGGREGATION:
                mend.setAssociationEndType(AssociationEndType.AGGREGATION);
                break;
            case KINDISCOMPOSITION:
                mend.setAssociationEndType(AssociationEndType.COMPOSITION);
                break;
        }

        // Dependencies
        buildDependencies(end);

        boolean unique = end.isIsUnique();
        boolean derived = end.isIsDerived();

        return mend;
    }

    public MEnumValue buildEnumValue(EnumerationLiteral enumerationLiteral) {
        MEnumValue menumValue = new MEnumValue(enumerationLiteral.getUuid());

        menumValue.setName(enumerationLiteral.getName());
        menumValue.setType(resolve(enumerationLiteral.getValuated()));

        // Stereotypes
        buildStereotypes(enumerationLiteral).stream().forEach(menumValue.getStereotypes()::add);

        // Documentation
        menumValue.setDocumentation(getDocumentation(enumerationLiteral));

        // Dependencies
        buildDependencies(enumerationLiteral);

        return menumValue;
    }

    public void buildDependencies(ModelElement modelElement) {
        MEntity msource = resolveModelElement(modelElement);
        //me.getImpactImpacted()
        modelElement.getDependsOnDependency().stream()
                .forEach(d -> {
                    MDependency dependency = new MDependency(d.getUuid());
                    dependency.setDependsOn(resolveModelElement(d.getDependsOn()));
                    msource.getDependencies().add(dependency);

                    if (d instanceof Usage) {
                        MStereotype mstereotype = new MStereotype();

                        mstereotype.setName("Usage");
                        mstereotype.setModule("Standard");
                        mstereotype.setProfile("Standard");
                        mstereotype.setHidden(false);

                        dependency.getStereotypes().add(mstereotype);
                    }

                    // Stereotypes
                    dependency.getStereotypes().addAll(buildStereotypes(d));
                });
    }

    /**
     * This method builds a {@code MParameter} from the {@code Parameter} {@code parameter}.
     *
     * @param parameter Parameter.
     * @return Model parameter.
     */
    public MParameter buildParameter(Parameter parameter) {
        MParameter mparameter = new MParameter();

        int min = parseMultiplicity(parameter.getMultiplicityMin());
        int max = parseMultiplicity(parameter.getMultiplicityMax());
        boolean ordered = parameter.isIsOrdered();

        mparameter.setName(parameter.getName());
        mparameter.setType(buildClassWithMultiplicity(parameter.getType(), min, max));

        // Stereotypes
        buildStereotypes(parameter).stream().forEach(mparameter.getStereotypes()::add);

        // Documentation
        mparameter.setDocumentation(getDocumentation(parameter));

        // Dependencies
        buildDependencies(parameter);

        PassingMode passingMode = parameter.getParameterPassing();
        String defaultValue = parameter.getDefaultValue();

        boolean unique = parameter.isIsUnique();

        boolean isException = parameter.isIsException();

        return mparameter;
    }

    public void addFoundClassListener(FoundClassListener listener) {
        foundClassListeners.addListener(listener);
    }

    public void removeFoundClassListener(FoundClassListener listener) {
        foundClassListeners.removeListener(listener);
    }

//<editor-fold defaultstate="collapsed" desc="private">
    private MTemplateParameter buildTemplateParameter(TemplateParameter tp, MEntity parametrized) {
        MTemplateParameter mtemplateParameter = new MTemplateParameter(tp.getUuid());
        mtemplateParameter.setName(tp.getName());
        mtemplateParameter.setParametrized(parametrized);
        return mtemplateParameter;
    }

    /**
     * This method builds a {@code MClass} or {@code MNamespace} from the
     * {@code org.modelio.metamodel.uml.statik.Class}, {@code Interface}, {@code Enumeration} or {@code NameSpace}.
     */
    private MNamespace buildVirtual(NameSpace nameSpace) {
        if (nameSpace instanceof org.modelio.metamodel.uml.statik.Class) {
            return resolve((org.modelio.metamodel.uml.statik.Class) nameSpace);
        } else if (nameSpace instanceof Interface) {
            return resolve((Interface) nameSpace);
        } else if (nameSpace instanceof Enumeration) {
            return resolve((Enumeration) nameSpace);
        }

        MNamespace mnamespace = buildNamespace((NameSpace) nameSpace);
        return mnamespace;
    }

    private List<MStereotype> buildStereotypes(ModelElement modelElement) {
        return modelElement.getExtension()
                .stream()
                .map(st -> {
                    MStereotype mstereotype = new MStereotype();

                    mstereotype.setName(getSafeName(st));
                    mstereotype.setModule(getSafeName(st.getModule()));
                    mstereotype.setProfile(getSafeName(st.getOwner()));
                    mstereotype.setHidden(st.isIsHidden());

                    st.getDefinedTagType()
                            .stream()
                            .forEach(tagType -> {
                                String moduleName = getSafeName(tagType.getModule());
                                String tagName = getSafeName(tagType);
                                String[] tagValues = modelElement.getTagValues(moduleName, tagName).stream().toArray(sz -> new String[sz]);

                                mstereotype.getTaggedValues().put(tagName, tagValues);
                            });

                    return mstereotype;
                })
                .collect(Collectors.toList());
    }

    private int calculateModifiers(VisibilityMode vis) {
        int modifiers = 0;
        switch (vis) {
            case PUBLIC:
                modifiers |= Modifiers.PUBLIC;
            case PROTECTED:
                modifiers |= Modifiers.PROTECTED;
            case PRIVATE:
                modifiers |= Modifiers.PRIVATE;
            default:
                modifiers |= Modifiers.PACKAGE;
        }
        return modifiers;
    }

    /**
     * This method pushes a new classifier resolver based on the pair class {@code mclass} and classifier {@code cl}.
     */
    private void pushResolverForClass(MTemplateDefinition mclass, Classifier classifier) {
        // Template parameters must be included in the 'classResolver'.
        Function<Classifier, MClass> prev = peekResolver();
        pushResolver(x -> {

            // It tests if the owner of the template parameter is the class.
            TemplateParameter templateParameter = Utils.as(TemplateParameter.class, x);
            if ((templateParameter != null) && Utils.equals(templateParameter.getParameterized(), classifier)) {

                MTemplateParameter mtemplateParameter = mclass.getTemplateArgument(templateParameter.getName());
                if (mtemplateParameter == null) {
                    throw new Error(String.format("The template parameter '%1$s' can not be found in the classifier '%2$s'",
                                                  templateParameter.getName(),
                                                  getFullName(classifier)));
                }
                return mtemplateParameter;
            }
            return prev.apply(x);
        });
    }

    /**
     * This method pushes a new classifier resolver based on the pair method {@code mmethod} and operation {@code op}.
     */
    private void pushResolverForMethod(MMethod mmethod, Operation operation) {
        Function<Classifier, MClass> prev = peekResolver();
        pushResolver(x -> {

            // It tests if the owner of the template parameter is the operation.
            TemplateParameter templateParameter = Utils.as(TemplateParameter.class, x);
            if ((templateParameter != null) && Utils.equals(templateParameter.getParameterizedOperation(), operation)) {

                MTemplateParameter mtemplateParameter = mmethod.getTemplateArgument(templateParameter.getName());
                if (mtemplateParameter == null) {
                    throw new Error(String.format("The template parameter '%1$s' can not be found in the operation '%2$s' in '%3$s'",
                                                  templateParameter.getName(),
                                                  operation.getName(),
                                                  getFullName(operation.getOwner())));
                }
                return mtemplateParameter;
            }
            return prev.apply(x);
        });
    }

    private void pushResolver(Function<Classifier, MClass> func) {
        classResolver.push(func);
    }

    private Function<Classifier, MClass> peekResolver() {
        return classResolver.peek();
    }

    private void popResolver() {
        classResolver.pop();
    }

    private MClass resolve(Classifier classifier) {
        Function<Classifier, MClass> func = classResolver.peek();
        return func.apply(classifier);
    }

    private MEntity resolveModelElement(ModelElement modelElement) {
        if (modelElement instanceof Classifier) {
            return resolve((Classifier) modelElement);
        } else if (modelElement instanceof Operation) {
            Operation operation = (Operation) modelElement;
            Classifier owner = operation.getOwner();
            if (owner != null) {
                MClass cowner = resolve(owner);
                return cowner.findOperation(operation.getUuid());
            }
        } else if (modelElement instanceof Attribute) {
            Attribute attribute = (Attribute) modelElement;
            Classifier owner = attribute.getOwner();
            if (owner != null) {
                MClass cowner = resolve(owner);
                return cowner.findProperty(attribute.getUuid());
            }
        } else if (modelElement instanceof AssociationEnd) {
            AssociationEnd associationEnd = (AssociationEnd) modelElement;
            Classifier owner = associationEnd.getOwner();
            if (owner != null) {
                MClass cowner = resolve(owner);
                return cowner.findAssociationEnd(associationEnd.getUuid());
            }
        } else if (modelElement instanceof EnumerationLiteral) {
            EnumerationLiteral enumerationLiteral = (EnumerationLiteral) modelElement;
            Classifier owner = enumerationLiteral.getValuated();
            if (owner != null) {
                MClass cowner = resolve(owner);
                return cowner.findProperty(enumerationLiteral.getUuid());
            }
        }
        return null;
    }

    private static String getFullName(ModelTree modelTree) {
        ModelTree owner = modelTree.getOwner();
        if (!(owner instanceof NameSpace)) {
            return getSafeName(modelTree);
        }
        return getFullName(owner) + "." + getSafeName(modelTree);
    }

    private static String getSafeName(ModelElement modelElement) {
        if (modelElement == null) {
            return "";
        }
        return modelElement.getName();
    }

    private static String getDocumentation(ModelElement modelElement) {
        return modelElement.getDescriptor()
                .stream()
                .map(x -> x.getContent())
                .reduce((x, y) -> x + System.lineSeparator() + y)
                .orElse(null);
    }

    private static int parseMultiplicity(String multi) {
        if ("*".equals(multi)) {
            return Integer.MAX_VALUE;
        }
        try {
            return Integer.parseUnsignedInt(multi);
        } catch (NumberFormatException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return 0;
        }
    }
//</editor-fold>
}
