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
package org.essence.modeliohelper.impl;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import org.essence.modeliohelper.command.Helper;
import org.modelio.api.module.context.log.ILogService;
import org.modelio.api.module.lifecycle.DefaultModuleLifeCycleHandler;
import org.modelio.api.module.lifecycle.ModuleException;
import org.modelio.vbasic.version.Version;

/**
 * Implementation of the IModuleLifeCycleHandler interface.
 * <br>This default implementation may be inherited by the module developers in order to simplify the code writing of
 * the module life cycle handler .
 */
public class ModelioHelperLifeCycleHandler extends DefaultModuleLifeCycleHandler {

    private static final Logger LOGGER = Logger.getLogger(ModelioHelperLifeCycleHandler.class.getName());

    /**
     * Constructor.
     *
     * @param module the Module this life cycle handler is instanciated for.
     */
    public ModelioHelperLifeCycleHandler(ModelioHelperModule module) {
        super(module);
    }

    /**
     * @see org.modelio.api.module.DefaultModuleLifeCycleHandler#start()
     */
    @Override
    public boolean start() throws ModuleException {
        ILogService logService = module.getModuleContext().getLogService();

        logService.info("Start of " + module.getName() + " " + module.getVersion());

        try {
            Helper.setupLogger();
        } catch (IOException ex) {
            logService.error("Error while configuring the logger: " + ex);
        }

        return super.start();
    }

    /**
     * @see org.modelio.api.module.DefaultModuleLifeCycleHandler#stop()
     */
    @Override
    public void stop() throws ModuleException {
        ILogService logService = module.getModuleContext().getLogService();

        logService.info("Stop of " + module.getName() + " " + module.getVersion());

        try {
            Helper.disposeLogger();
        } catch (IOException ex) {
            logService.error("Error while configuring the logger: " + ex);
        }

        super.stop();
    }

    public static boolean install(String modelioPath, String mdaPath) throws ModuleException {
        return DefaultModuleLifeCycleHandler.install(modelioPath, mdaPath);
    }

    /**
     * @see org.modelio.api.module.DefaultModuleLifeCycleHandler#select()
     */
    @Override
    public boolean select() throws ModuleException {
        return super.select();
    }

    /**
     * @see org.modelio.api.module.DefaultModuleLifeCycleHandler#unselect()
     */
    @Override
    public void unselect() throws ModuleException {
        super.unselect();
    }

    /**
     * @see org.modelio.api.module.DefaultModuleLifeCycleHandler#upgrade(org.modelio.api.modelio.Version, java.util.Map)
     */
    @Override
    public void upgrade(Version oldVersion, Map<String, String> oldParameters) throws ModuleException {
        super.upgrade(oldVersion, oldParameters);
    }
}
