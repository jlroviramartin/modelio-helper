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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.essence.commons.MapBuilder;
import org.essence.modeliohelper.model.ModelPrinter;
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
public class CodeGenerationCommand extends DefaultModuleCommandHandler {

    private static final Logger LOGGER = Logger.getLogger(CodeGenerationCommand.class.getName());

    /**
     * Constructor.
     */
    public CodeGenerationCommand() {
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

        ScriptEngine scriptEngine = module.getModuleContext().getJythonEngine();

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
        //ModelBuilder modeBuilder = new ModelBuilder();
        //modeBuilder.build(nameSpace);
        Path scriptPath = projectPath.resolve("script");
        Path runPath = scriptPath.resolve("Run.py");

        if (!Files.exists(runPath, LinkOption.NOFOLLOW_LINKS)) {

            MessageDialog.openInformation(null, "Script not found", runPath.toString());
        } else {

            /*try {
                ResourceUtils.copyContent("app\\astyle", new File("C:\\Temp\\astile"));
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }*/

            Map<String, Object> args = new MapBuilder<String, Object>()
                    .put("outputPath", outputPath.toString())
                    .put("scriptPath", scriptPath.toString())
                    .put("__file__", runPath.toString())
                    //.put("model", modeBuilder.getRoot())
                    .put("selectedElements", selectedElements)
                    .build();

            try {
                jythonize2(runPath,
                           outputPath,
                           args,
                           scriptEngine,
                           logService);

                logService.info(String.format("%s: Code generated %s", getClass().getSimpleName(), projectPath));

                MessageDialog.openInformation(null, "Finished", projectPath.toString());
            } catch (Exception | Error ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                logService.error(ex);
            }
        }
    }

    private void jythonize(Path runPath,
                           Path outputPath,
                           Map<String, Object> args,
                           ILogService logService) {

        //PySystemState engineSys = new PySystemState();
        //engineSys.path.append(Py.newString("my/lib/directory"));
        //Py.setSystemState(engineSys);
        ScriptEngineManager manager = new ScriptEngineManager(CodeGenerationCommand.class.getClassLoader());
        ScriptEngine engine = manager.getEngineByName("python");
        jythonize2(runPath,
                   outputPath,
                   args,
                   engine,
                   logService);

        /*ILogService logService = module.getModuleContext().getLogService();

        try (FileWriter out = new FileWriter(outputPath.resolve("out.txt").toFile());
             FileWriter err = new FileWriter(outputPath.resolve("err.txt").toFile())) {
            PythonInterpreter interp = new PythonInterpreter(new PyObject());

            for (Map.Entry<String, Object> e : args.entrySet()) {
                interp.set(e.getKey(), e.getValue());
            }
            interp.setOut(out);
            interp.setErr(err);
            interp.execfile(runPath.toString());
        } catch (Exception ex) {
            logService.error(ex);
            LOGGER.log(Level.SEVERE, null, ex);
        }*/
    }

    private void jythonize2(Path runPath,
                            Path outputPath,
                            Map<String, Object> args,
                            ScriptEngine scriptEngine,
                            ILogService logService) {

        try (FileReader reader = new FileReader(runPath.toFile());
             FileWriter out = new FileWriter(outputPath.resolve("out.txt").toFile());
             FileWriter err = new FileWriter(outputPath.resolve("err.txt").toFile())) {

            logService.info(String.format("%s: Initializing", getClass().getSimpleName()));

            ScriptContext c = new SimpleScriptContext();
            c.setWriter(out);
            c.setErrorWriter(err);
            for (Map.Entry<String, Object> e : args.entrySet()) {
                c.getBindings(ScriptContext.ENGINE_SCOPE).put(e.getKey(), e.getValue());
            }

            logService.info(String.format("%s: Evaluating", getClass().getSimpleName()));

            scriptEngine.eval(reader, c);

            logService.info(String.format("%s: Evaluation finished", getClass().getSimpleName()));
        } catch (Exception ex) {
            logService.error(ex);
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
