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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Print text using an indentation.
 *
 * @author joseluis
 */
public class IndentPrintWriter extends PrintWriter {

    public IndentPrintWriter(Writer out) {
        super(out);
    }

    public IndentPrintWriter(Writer out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public IndentPrintWriter(OutputStream out) {
        super(out);
    }

    public IndentPrintWriter(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public IndentPrintWriter(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public IndentPrintWriter(String fileName, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    public IndentPrintWriter(File file) throws FileNotFoundException {
        super(file);
    }

    public IndentPrintWriter(File file, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    public void indent() {
        this.indentLevel++;
        this.updateIndent();
    }

    public void unindent() {
        this.indentLevel--;
        this.updateIndent();
    }

    public String getIndentText() {
        return this.indentText;
    }

    public void setIndentText(String text) {
        this.indentText = text;
    }

    public void indent(ZeroAction action) {
        this.indent();
        action.exec();
        this.unindent();
    }

    @FunctionalInterface
    public interface ZeroAction {

        void exec();
    }

//<editor-fold defaultstate="collapsed" desc="private">
    private void tryWriteIndent() {
        if (this.addIndent) {
            this.addIndent = false;

            super.write(this.indentBuffer, 0, this.indentBuffer.length);
        }
    }

    private void updateIndent() {
        String currentIndentText = "";
        for (int i = 0; i < this.indentLevel; i++) {
            currentIndentText += this.indentText;
        }
        this.indentBuffer = new char[currentIndentText.length()];
        currentIndentText.getChars(0, currentIndentText.length(), this.indentBuffer, 0);
    }

    private boolean addIndent = true;
    private int indentLevel;
    private String indentText = "  ";
    private char[] indentBuffer = new char[0];
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Writer">
    @Override
    public void write(int c) {
        this.tryWriteIndent();
        super.write(c);
    }

    @Override
    public void write(char cbuf[], int off, int len) {
        this.tryWriteIndent();
        super.write(cbuf, off, len);
    }

    @Override
    public void write(String s, int off, int len) {
        this.tryWriteIndent();
        super.write(s, off, len);
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="PrintWriter">
    @Override
    public void println() {
        super.println();
        this.addIndent = true;
    }
//</editor-fold>
}
