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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.modelio.api.modelio.Modelio;
import org.modelio.api.module.context.log.ILogService;

/**
 *
 * @author joseluis
 */
public class Helper {

//<editor-fold defaultstate="collapsed" desc="fields">
    private static final Logger LOGGER = Logger.getLogger(Helper.class.getName());
    private static final String LOG_FILE_PATH = "C:\\Temp\\ModelioHelper.log";
    private static final String LOG_FILE2_PATH = "C:\\Temp\\ModelioHelper.hard.log";
    private static FileHandler fileTxt;
//</editor-fold>

    public static void hardLog(String txt) {
        try (FileOutputStream stream = new FileOutputStream(LOG_FILE2_PATH, true);
             PrintWriter writer = new PrintWriter(stream)) {
            writer.println(txt);
        } catch (IOException ex) {
        }
    }

    public static void modelioLog(String txt) {
        ILogService logService = Modelio.getInstance().getLogService();
        logService.info(txt);
    }

    public static void setupLogger() throws IOException {
        Logger logger = Logger.getLogger("org.essence.modeliohelper");
        if (fileTxt == null) {
            fileTxt = new FileHandler(LOG_FILE_PATH, true);
            fileTxt.setLevel(Level.ALL);
            fileTxt.setFormatter(new SimpleFormatter());
            logger.addHandler(fileTxt);
        }

        LOGGER.info("doing setupLogger");
    }

    public static void disposeLogger() throws IOException {
        Logger logger = Logger.getLogger("org.essence.modeliohelper");

        LOGGER.info("doing disposeLogger");

        if (fileTxt != null) {
            logger.removeHandler(fileTxt);
            fileTxt.close();
            fileTxt = null;
        }
    }
}
