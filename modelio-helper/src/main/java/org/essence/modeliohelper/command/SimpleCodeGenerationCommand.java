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
package org.essence.modeliohelper.command;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.essence.modeliohelper.model.ModelBuilder;
import org.essence.modeliohelper.model.ModelPrinter;
import org.essence.modeliohelper.model.pebble.PebbleModelProcessor;
import org.modelio.api.module.IModule;
import org.modelio.api.module.command.DefaultModuleCommandHandler;
import org.modelio.api.module.context.log.ILogService;
import org.modelio.api.module.context.project.IProjectStructure;
import org.modelio.metamodel.uml.statik.NameSpace;
import org.modelio.vcore.smkernel.mapi.MObject;

/**
 * Implementation of the IModuleContextualCommand interface.
 * <br>The module contextual commands are displayed in the contextual menu and in the specific toolbar of each module
 * property page.
 * <br>The developer may inherit the DefaultModuleContextualCommand class which contains a default standard contextual
 * command implementation.
 *
 */
public class SimpleCodeGenerationCommand extends DefaultModuleCommandHandler {

    private static final Logger LOGGER = Logger.getLogger(SimpleCodeGenerationCommand.class.getName());
    private static final String TEMPLATE = "pebble/simple/Default.pebble";

    /**
     * Constructor.
     */
    public SimpleCodeGenerationCommand() {
    }

    /**
     * This method accepts only one item selected of type {@code NameSpace}
     */
    @Override
    public boolean accept(List<MObject> selectedElements, IModule module) {
        return selectedElements.size() == 1 && selectedElements.get(0) instanceof NameSpace;
    }

    /**
     * This method generates the code.
     */
    @Override
    public void actionPerformed(List<MObject> selectedElements, IModule module) {
        ILogService logService = module.getModuleContext().getLogService();
        IProjectStructure projectStructure = module.getModuleContext().getProjectStructure();

        //ScriptEngine scriptEngine = module.getModuleContext().getJythonEngine();
        //IModelingSession session = module.getModuleContext().getModelingSession();
        //IModuleUserConfiguration configuration = module.getModuleContext().getConfiguration();
        //List<MObject> root = session.getModel().getModelRoots();
        logService.info(String.format("%s: actionPerformed(...)", getClass().getSimpleName()));

        //ClassLoader classLoader = getClass().getClassLoader();
        //String fullPath = classLoader.getResource(fileName).getFile();
        NameSpace nameSpace = (NameSpace) selectedElements.get(0);
        Path projectPath = projectStructure.getPath();

        Path outputPath = projectPath.resolve("output");

        // Debug information is printed.
        Path infoPath = outputPath.resolve("Info.txt");
        try (HPrinter writer = new HPrinter(infoPath.toFile(), "UTF-8")) {
            ModelPrinter.print(nameSpace, writer);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        // The model is built.
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.addUmlMapping();
        modelBuilder.build(nameSpace);

        // Templates are used to build the classes.
        try {
            logService.info(String.format("%s: Processing", getClass().getSimpleName()));

            PebbleModelProcessor modelProcessor = new PebbleModelProcessor(outputPath, TEMPLATE);
            modelBuilder.processClasses(x -> modelProcessor.processClass(x));
        } catch (Exception ex) {
            logService.error(ex);
            LOGGER.log(Level.SEVERE, null, ex);
        }

        logService.info(String.format("%s: Code generated %s", getClass().getSimpleName(), projectPath));

        MessageDialog.openInformation(null, "Finished", projectPath.toString());
    }
}
