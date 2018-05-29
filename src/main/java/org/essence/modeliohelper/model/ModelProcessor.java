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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.essence.modeliohelper.command.HPrinter;
import org.essence.modeliohelper.core.Astyle;

/**
 *
 * @author joseluis
 */
public abstract class ModelProcessor {

//<editor-fold defaultstate="collapsed" desc="fields">
    private static final Logger LOGGER = Logger.getLogger(ModelProcessor.class.getName());

    private final List<Path> templatePaths = new ArrayList<>();
    private final Path outputPath;
//</editor-fold>

    public ModelProcessor(Path outputPath) {
        this.outputPath = outputPath;

        try {
            Files.createDirectories(outputPath);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public void addTemplatePath(Path path) {
        templatePaths.add(path);
    }

    public void processClass(MClass mclass) {
        Path code = prepareFile(outputPath, mclass);
        String text = templateClass(mclass);
        File file = code.toFile();
        printTo(file, text);

        if (Astyle.INSTANCE != null) {
            try {
                Astyle.INSTANCE.javaBeautify(file);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

    protected List<Path> getTemplatePaths() {
        return templatePaths;
    }

    /**
     * This method prints {@code mclass} as a class using templates.
     *
     * @param mclass Classifier.
     * @return Text.
     */
    protected abstract String templateClass(MClass mclass);

//<editor-fold defaultstate="collapsed" desc="private">
    private Path prepareFile(Path outputPath, MClass mclass) {
        String sub = mclass.getFullNamespace().replace('.', File.separatorChar);
        Path basePath = outputPath.resolve(sub);

        try {
            Files.createDirectories(basePath);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        Path code = basePath.resolve(mclass.getName() + ".java");
        return code;

    }

    /**
     * This method writes the {@code text} into the {@code file}.
     *
     * @param file File where to write.
     * @param text Text to write.
     */
    private void printTo(File file, String text) {
        try (HPrinter writer = new HPrinter(file)) {
            writer.println(text);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
//</editor-fold>
}
