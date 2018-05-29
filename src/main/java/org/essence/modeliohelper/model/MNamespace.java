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

import java.util.List;
import org.essence.modeliohelper.core.BaseList;

/**
 *
 * @author joseluis
 */
public class MNamespace extends MEntity {

//<editor-fold defaultstate="collapsed" desc="fields">
    private MNamespace namespace;
    private final List<MNamespace> ownedNamespaces = new OwnedNamespacesList<>();
//</editor-fold>

    public MNamespace(String id) {
        super(id);
    }

    public boolean isRoot() {
        return Utils.isNullOrEmpty(getName());
    }

    public String getFullNamespace() {
        String fname = "";
        MNamespace parent = getNamespace();
        while (parent != null && !parent.isRoot()) {
            if (!fname.isEmpty()) {
                fname = parent.getName() + "." + fname;
            } else {
                fname = parent.getName();
            }
            parent = parent.getNamespace();
        }
        return fname;
    }

    public MNamespace getNamespace() {
        return namespace;
    }

    public void setNamespace(MNamespace value) {
        namespace = value;
    }

    public List<MNamespace> getOwnedNamespaces() {
        return ownedNamespaces;
    }

//<editor-fold defaultstate="collapsed" desc="inner classes">
    private class OwnedNamespacesList<T extends MNamespace> extends BaseList<T> {

        @Override
        public void onSet(int index, T oldItem, T newItem) {
            oldItem.setNamespace(null);
            newItem.setNamespace(MNamespace.this);
        }

        @Override
        public void onAdd(int index, T newItem) {
            newItem.setNamespace(MNamespace.this);
        }

        @Override
        public void onRemove(int index, T oldItem) {
            oldItem.setNamespace(null);
        }
    }
//</editor-fold>
}
