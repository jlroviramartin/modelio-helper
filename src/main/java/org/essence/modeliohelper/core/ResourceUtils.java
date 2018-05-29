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
package org.essence.modeliohelper.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 * https://stackoverflow.com/a/3923182 https://stackoverflow.com/a/600198
 * http://www.rgagnon.com/javadetails/java-0665.html
 *
 * @author joseluis
 */
public class ResourceUtils {

    private static final Logger LOGGER = Logger.getLogger(ResourceUtils.class.getName());

    public static void copyContent(String resource, File dest) throws Exception {
        final File jarFile = new File(ResourceUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath());

        final String resourcePath = resource.replace('.', '/');

        if (jarFile.isFile()) {
            try (JarFile jar = new JarFile(jarFile)) {
                LOGGER.log(Level.INFO, "Jar: " + jar);

                List<Exception> toThrow = new ArrayList<>();

                jar.stream()
                        .filter(jarEntry -> jarEntry.getName().startsWith(resourcePath + "/"))
                        .forEach(jarEntry -> {
                            String sub = jarEntry.getName().substring(resourcePath.length() + 1);

                            File targetFile = new File(dest, sub);
                            if (jarEntry.isDirectory()) {
                                targetFile.mkdirs();
                            } else {
                                if (targetFile.getParentFile() != null) {
                                    targetFile.getParentFile().mkdirs();
                                }

                                try (InputStream targetStream = jar.getInputStream(jarEntry)) {
                                    FileUtils.copyInputStreamToFile(targetStream, targetFile);
                                } catch (IOException ex) {
                                    LOGGER.log(Level.SEVERE, null, ex);

                                }
                            }
                        });
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        } else {
            final URL url = ResourceUtils.class.getResource("/" + resourcePath);
            LOGGER.log(Level.INFO, "url: " + url);

            if (url != null) {
                try {
                    Stack<File> stack = new Stack<>();
                    stack.push(new File(url.toURI()));
                    while (!stack.isEmpty()) {
                        File sourceFile = stack.pop();

                        String sub = url.toURI().relativize(sourceFile.toURI()).getPath();
                        File targetFile = new File(dest, sub);

                        if (sourceFile.isDirectory()) {
                            // It creates the directory.
                            targetFile.mkdir();

                            Arrays.stream(sourceFile.listFiles())
                                    .forEach(stack::push);
                        } else {
                            try (InputStream targetStream = new FileInputStream(sourceFile)) {

                                FileUtils.copyInputStreamToFile(targetStream, targetFile);
                            } catch (FileNotFoundException ex) {
                                LOGGER.log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                LOGGER.log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                } catch (URISyntaxException ex) {
                    // never happens
                }
            }
        }
    }
}
