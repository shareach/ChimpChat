/*
 * Daneel - Dalvik to Java bytecode compiler
 * Copyright (C) 2011  IcedRobot team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * This file is subject to the "Classpath" exception:
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under terms
 * of your choice, provided that you also meet, for each linked independent
 * module, the terms and conditions of the license of that module.  An
 * independent module is a module which is not derived from or based on
 * this library.  If you modify this library, you may extend this exception
 * to your version of the library, but you are not obligated to do so.  If
 * you do not wish to do so, delete this exception statement from your
 * version.
 */
package org.icedrobot.daneel.dex;

import java.nio.ByteBuffer;

/**
 * A parser class capable of parsing {@code field_id_item} structures as part of
 * DEX files. Keep package-private to hide internal API.
 */
class FieldId {

    /**
     * Parses a {@code field_id_item} structure in a DEX file at the buffer's
     * current position.
     * 
     * @param buffer The byte buffer to read from.
     * @param dex The DEX file currently being parsed.
     * @return An object representing the parsed data.
     */
    static FieldId parse(ByteBuffer buffer, DexFile dex) {
        return new FieldId(buffer, dex);
    }

    private final String className;

    private final String typeDescriptor;

    private final String name;

    private FieldId(ByteBuffer buffer, DexFile dex) {
        className = dex.getTypeDescriptor(buffer.getShort());
        typeDescriptor = dex.getTypeDescriptor(buffer.getShort());
        name = dex.getString(buffer.getInt());
    }

    public String getClassName() {
        return className;
    }

    public String getTypeDescriptor() {
        return typeDescriptor;
    }

    public String getName() {
        return name;
    }
}
