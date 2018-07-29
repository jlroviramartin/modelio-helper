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
package org.essence.modeliohelper.model.pebble;

import java.nio.file.Path;
import org.essence.commons.MapBuilder;
import org.essence.modeliohelper.model.MClass;
import org.essence.modeliohelper.model.ModelProcessor;
import org.essence.pebble.TemplateEngine;

/**
 *
 * @author joseluis
 */
public class PebbleModelProcessor extends ModelProcessor {

//<editor-fold defaultstate="collapsed" desc="fields">
    private final String mainTemplate;
//</editor-fold>

    public PebbleModelProcessor(Path outputPath, String mainTemplate) {
        super(outputPath);
        this.mainTemplate = mainTemplate;
    }

//<editor-fold defaultstate="collapsed" desc="ModelProcessor">
    @Override
    public String templateClass(MClass mclass) {
        TemplateEngine engine = new TemplateEngine();
        TemplateEngine.Template t = engine.load(mainTemplate);

        String text = t.toString(new MapBuilder<String, Object>()
                .put("entity", mclass)
                .build());
        return text;
    }
//</editor-fold>
}
