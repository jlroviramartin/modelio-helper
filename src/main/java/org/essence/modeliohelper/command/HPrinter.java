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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Hierarchical printer. Helper class for printing object in a structured (an hierarchical) way.
 *
 * @author joseluis
 */
public class HPrinter extends IndentPrintWriter {

    public HPrinter(Writer out) {
        super(out);
    }

    public HPrinter(Writer out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public HPrinter(OutputStream out) {
        super(out);
    }

    public HPrinter(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public HPrinter(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public HPrinter(String fileName, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    public HPrinter(File file) throws FileNotFoundException {
        super(file);
    }

    public HPrinter(File file, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    public HPrinter printProperty(String name, Object value) {
        this.println(name + " = " + value);
        return this;
    }

    public HPrinter printGroup(String name, IndentPrintWriter.ZeroAction action) {
        this.println(name);
        this.println("{");
        this.indent();
        action.exec();
        this.unindent();
        this.println("}");
        return this;
    }

    public HPrinter printGroup(IndentPrintWriter.ZeroAction action) {
        this.println("{");
        this.indent();
        action.exec();
        this.unindent();
        this.println("}");
        return this;
    }

    public <T> HPrinter printGroup(String name, Iterable<T> items, Function<T, String> header, Consumer<T> action) {
        if (items == null || !items.iterator().hasNext()) {
            return this;
        }

        this.println(name);
        this.println("{");
        this.indent();
        for (T t : items) {
            this.println(header.apply(t));
            this.println("{");
            this.indent();
            action.accept(t);
            this.unindent();
            this.println("}");
        }
        this.unindent();
        this.println("}");
        return this;
    }

    public <T> HPrinter printGroup(Iterable<T> items, Function<T, String> header, Consumer<T> action) {
        if (items == null || !items.iterator().hasNext()) {
            return this;
        }

        this.println("{");
        this.indent();
        for (T t : items) {
            this.println(header.apply(t));
            this.println("{");
            this.indent();
            action.accept(t);
            this.unindent();
            this.println("}");
        }
        this.unindent();
        this.println("}");
        return this;
    }
}
