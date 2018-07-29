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

import org.essence.modeliohelper.model.st.MMethodModelAdaptor;
import org.essence.modeliohelper.model.st.MPropertyModelAdaptor;
import org.essence.modeliohelper.model.st.MClassModelAdaptor;
import java.nio.file.Path;
import org.essence.modeliohelper.model.MClass;
import org.essence.modeliohelper.model.MMethod;
import org.essence.modeliohelper.model.MProperty;
import org.essence.modeliohelper.model.ModelProcessor;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.STGroupString;

/**
 *
 * @author joseluis
 */
public class STModelProcessor extends ModelProcessor {

    public STModelProcessor(Path outputPath) {
        super(outputPath);
    }

//<editor-fold defaultstate="collapsed" desc="ModelProcessor">
    @Override
    public String templateClass(MClass mclass) {
        String textTemplate = "Default(entity) ::= <<$(entity.template)(entity)$>>";

        // If it is done static, the template is not loaded at runtime.
        STGroupString group = new STGroupString("<string>", textTemplate, '$', '$');

        for (Path path : getTemplatePaths()) {
            group.importTemplates(new STGroupFile(path.toString()));
        }

        group.registerRenderer(String.class, new StringRenderer());
        group.registerModelAdaptor(MClass.class, new MClassModelAdaptor());
        group.registerModelAdaptor(MProperty.class, new MPropertyModelAdaptor());
        group.registerModelAdaptor(MMethod.class, new MMethodModelAdaptor());

        ST instance = group.getInstanceOf("Default");
        instance.add("entity", mclass);

        String text = instance.render();
        return text;
    }
//</editor-fold>
}
