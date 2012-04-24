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

package org.icedrobot.daneel.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.icedrobot.daneel.dex.DexAnnotationVisitor;
import org.icedrobot.daneel.dex.DexClassVisitor;
import org.icedrobot.daneel.dex.DexFieldVisitor;
import org.icedrobot.daneel.dex.DexFile;
import org.icedrobot.daneel.dex.DexFileVisitor;
import org.icedrobot.daneel.dex.DexMethodVisitor;
import org.icedrobot.daneel.dex.Label;
import org.icedrobot.daneel.dex.Opcode;
import org.icedrobot.daneel.loader.ApkFile;
import org.objectweb.asm.Type;

/**
 * Simple tool which dumps the content of a DEX file. Mainly used to test the
 * parser functionality by hand.
 */
public class DexDumper {

    /**
     * Dumps all information contained in the given file (either a DEX or an APK
     * file) to the given output stream in human-readable format.
     * 
     * @param fileName The name of the file to be dumped.
     * @param out The output stream to print to.
     * @throws IOException In case of an error while accessing the file.
     */
    public void execute(String fileName, PrintStream out) throws IOException {
        DexFile dex;
        long m1 = System.currentTimeMillis();
        if (fileName.endsWith(".apk")) {
            ApkFile apk = new ApkFile(fileName);
            dex = apk.getDexFile();
        } else {
            dex = DexFile.parse(new File(fileName));
        }
        long m2 = System.currentTimeMillis();
        System.err.printf("Construction took %4dms.\n", m2 - m1);
        dex.accept(new FileDumper(out), 0);
        long m3 = System.currentTimeMillis();
        System.err.printf("Dumping took      %4dms.\n", m3 - m1);
    }

    /**
     * The dumper class taking care of printing file information.
     */
    private static class FileDumper implements DexFileVisitor {
        private final PrintStream out;

        public FileDumper(PrintStream out) {
            this.out = out;
        }

        @Override
        public DexClassVisitor visitClass(String name) {
            out.printf("class \"%s\":\n", convertDescToHuman(name));
            return new ClassDumper(out);
        }

        @Override
        public void visitEnd() {
            // Nothing to do here.
        }
    };

    /**
     * The dumper class taking care of printing class information.
     */
    private static class ClassDumper implements DexClassVisitor {
        private final PrintStream out;

        public ClassDumper(PrintStream out) {
            this.out = out;
        }

        @Override
        public void visit(int access, String name, String supername,
                String[] interfaces) {
            out.printf("  access: 0x%08x\n", access);
            out.printf("  name: %s\n", name);
            out.printf("  super: %s\n", supername);
            out.printf("  interfaces: %s\n", Arrays.toString(interfaces));
        }

        @Override
        public void visitEnd() {
            out.printf("end of class.\n\n");
        }

        @Override
        public DexAnnotationVisitor visitAnnotation(int visibility, String type) {
            out.printf("  annotation-on-class:\n");
            out.printf("    visibility: 0x%02x\n", visibility);
            out.printf("    type: %s\n", type);
            return new AnnotationDumper(out, "  ");
        }

        @Override
        public DexFieldVisitor visitField(int access, String name, String type,
                Object value) {
            out.printf("  field \"%s\":\n", name);
            out.printf("    access: 0x%08x\n", access);
            out.printf("    type: %s\n", type);
            out.printf("    value: %s\n", value);
            return new FieldDumper(out);
        }

        @Override
        public DexMethodVisitor visitMethod(int access, String name,
                String shorty, String returnType, String[] parameterTypes) {
            out.printf("  method \"%s\":\n", name);
            out.printf("    access: 0x%08x\n", access);
            out.printf("    shorty: %s\n", shorty);
            out.printf("    return-type: %s\n", returnType);
            out.printf("    parameter-types: %s\n", Arrays
                    .toString(parameterTypes));
            return new MethodDumper(out);
        }

        @Override
        public void visitSource(String source) {
            out.printf("  source: %s\n", source);
        }
    };

    /**
     * The dumper class taking care of printing field information.
     */
    private static class FieldDumper implements DexFieldVisitor {
        private final PrintStream out;

        public FieldDumper(PrintStream out) {
            this.out = out;
        }

        @Override
        public DexAnnotationVisitor visitAnnotation(int visibility, String type) {
            out.printf("    annotation-on-field:\n");
            out.printf("      visibility: 0x%02x\n", visibility);
            out.printf("      type: %s\n", type);
            return new AnnotationDumper(out, "    ");
        }

        @Override
        public void visitEnd() {
            // Nothing to do here.
        }
    };

    /**
     * The dumper class taking care of printing method information.
     */
    private static class MethodDumper implements DexMethodVisitor {
        private final PrintStream out;

        public MethodDumper(PrintStream out) {
            this.out = out;
        }

        @Override
        public DexAnnotationVisitor visitAnnotation(int visibility, String type) {
            out.printf("    annotation-on-method:\n");
            out.printf("      visibility: 0x%02x\n", visibility);
            out.printf("      type: %s\n", type);
            return new AnnotationDumper(out, "    ");
        }

        @Override
        public DexAnnotationVisitor visitParameterAnnotation(int parameter,
                int visibility, String type) {
            throw new RuntimeException("Implement me!");
            // return new DumpDexAnnotationVisitor()
        }

        @Override
        public void visitCode(int registers, int ins, int outs) {
            out.printf("    code (r=%d, i=%d, o=%d):\n", registers, ins, outs);
        }

        @Override
        public void visitEnd() {
            out.printf("  end of method.\n");
        }

        @Override
        public void visitInstr(Opcode opcode) {
            out.printf("      %s\n", opcode.name());
        }

        @Override
        public void visitInstrArray(Opcode opcode, int vsrcOrDest, int varray,
                int vindex) {
            out.printf("      %s v%d v%d v%d\n", opcode.name(), vsrcOrDest,
                    varray, vindex);
        }

        @Override
        public void visitInstrBinOp(Opcode opcode, int vdest, int vsrc,
                int vsrc2) {
            out.printf("      %s v%d v%d v%d\n", opcode.name(), vdest, vsrc,
                    vsrc2);
        }

        @Override
        public void visitInstrBinOpAndLiteral(Opcode opcode, int vdest,
                int vsrc, int value) {
            out.printf("      %s v%d v%d #%d(0x%08x)\n", opcode.name(), vdest,
                    vsrc, value, value);
        }

        @Override
        public void visitInstrClass(Opcode opcode, int vdest, String type) {
            String classRef = convertDescToHuman(type);
            out.printf("      %s v%d [%s]\n", opcode.name(), vdest, classRef);
        }

        @Override
        public void visitInstrConstString(Opcode opcode, int vdest, String value) {
            out.printf("      %s v%d \"%s\"\n", opcode.name(), vdest, value);
        }

        @Override
        public void visitInstrConstU32(Opcode opcode, int vdest, int value) {
            out.printf("      %s v%d #%d(0x%08x)\n", opcode.name(), vdest,
                    value, value);
        }

        @Override
        public void visitInstrConstU64(Opcode opcode, int vdest, long value) {
            out.printf("      %s v%d #%d(0x%016x)\n", opcode.name(), vdest,
                    value, value);
        }

        @Override
        public void visitInstrField(Opcode opcode, int vsrcOrDest, int vref,
                String owner, String name, String desc) {
            String fieldRef = convertDescToHuman(desc) + ' '
                    + convertDescToHuman(owner) + '#' + name;
            out.printf("      %s v%d v%d [%s]\n", opcode.name(), vsrcOrDest,
                    vref, fieldRef);
        }

        @Override
        public void visitInstrFillArrayData(Opcode opcode, int vsrc,
                int elementWidth, int elementNumber, ByteBuffer data) {
            out.printf("      %s v%d %s\n", opcode.name(), vsrc, data);
        }

        @Override
        public void visitInstrFilledNewArray(Opcode opcode, int num, int va,
                int vpacked, String type) {
            out.printf("      %s v%d+%d, %s\n", opcode.name(), va, num, type);
        }

        @Override
        public void visitInstrGoto(Opcode opcode, Label label) {
            out.printf("      %s %s\n", opcode.name(), label);
        }

        @Override
        public void visitInstrIfTest(Opcode opcode, int vsrc1, int vsrc2,
                Label label) {
            out.printf("      %s v%d v%d %s\n", opcode.name(), vsrc1, vsrc2,
                    label);
        }

        @Override
        public void visitInstrIfTestZ(Opcode opcode, int vsrc, Label label) {
            out.printf("      %s v%d %s\n", opcode.name(), vsrc, label);
        }

        @Override
        public void visitInstrInstanceof(Opcode opcode, int vdest, int vsrc,
                String type) {
            out.printf("      %s v%d v%d %s\n", opcode.name(), vdest, vsrc,
                    type);
        }

        @Override
        public void visitInstrMethod(Opcode opcode, int num, int va,
                int vpacked, String owner, String name, String desc) {
            String methodRef = convertDescToHuman(owner) + "#" + name + desc;
            out.printf("      %s {%x%04x}+%d [%s]\n", opcode.name(), va,
                    vpacked, num, methodRef);
        }

        @Override
        public void visitInstrNewArray(Opcode opcode, int vdest, int vsize,
                String type) {
            out.printf("      %s v%d v%d %s\n", opcode.name(), vdest, vsize,
                    type);
        }

        @Override
        public void visitInstrOp(Opcode opcode, int srcOrDst) {
            out.printf("      %s v%d\n", opcode.name(), srcOrDst);
        }

        @Override
        public void visitInstrPackedSwitch(Opcode opcode, int vsrc,
                int firstKey, Label[] targets) {
            out.printf("      %s v%d %s\n", opcode.name(), vsrc, Arrays
                    .toString(targets));
        }

        @Override
        public void visitInstrSparseSwitch(Opcode opcode, int vsrc, int[] keys,
                Label[] targets) {
            out.printf("      %s v%d %s\n", opcode.name(), vsrc, Arrays
                    .toString(targets));
        }

        @Override
        public void visitInstrUnaryOp(Opcode opcode, int vdest, int vsrc) {
            out.printf("      %s v%d v%d\n", opcode.name(), vdest, vsrc);
        }

        @Override
        public void visitLabel(Label label) {
            out.printf("      --- %s:\n", label);
        }

        @Override
        public void visitTryCatch(Label start, Label end, Label handler,
                String type) {
            out.printf("    try-catch: %s-%s -> %s, %s\n", start, end, handler,
                    type);
        }

        @Override
        public void visitLineNumber(String source, int line, Label start) {
            out.printf("    line-number: %s -> %s:%d\n", start, source, line);
        }

        @Override
        public void visitLocalVariable(String name, String desc, Label start,
                Label end, int reg) {
            String varRef = convertDescToHuman(desc) + ' ' + name;
            out.printf("    local-var: %s-%s, v%d -> [%s]\n", start, end, reg,
                    varRef);
        }
    };

    /**
     * The dumper class taking care of printing annotation information.
     */
    private static class AnnotationDumper implements DexAnnotationVisitor {
        private final PrintStream out;
        private final String indent;

        public AnnotationDumper(PrintStream out, String indent) {
            this.out = out;
            this.indent = indent;
        }

        @Override
        public void visitPrimitive(String name, Object value) {
            out.printf("%s  primitive-value: %s = %s\n", indent, name, value);
        }

        @Override
        public DexAnnotationVisitor visitAnnotation(String name, String type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DexAnnotationVisitor visitArray(String name, int size) {
            out.printf("%s  array-value: %s = array[%d]\n", indent, name, size);
            return null;
        }

        @Override
        public void visitEnum(String name, String enumOwner, String enumName) {
            out.printf("%s  enum-value: %s = %s %s\n", indent, name, enumOwner,
                    enumName);
        }

        @Override
        public void visitField(String name, String fieldOwner,
                String fieldName, String fieldDesc) {
            out.printf("%s  field-value: %s = %s %s %s\n", indent, name,
                    fieldOwner, fieldName, fieldDesc);
        }

        @Override
        public void visitMethod(String name, String methodOwner,
                String methodName, String methodDesc) {
            out.printf("%s  method-value: %s = %s %s %s\n", indent, name,
                    methodOwner, methodName, methodDesc);
        }

        @Override
        public void visitType(String name, String typeDesc) {
            out.printf("%s  type-value: %s = %s\n", indent, name, typeDesc);
        }

        @Override
        public void visitEnd() {
            out.printf("%send of annotation.\n", indent);
        }
    };

    /**
     * Converts a type descriptor into a human-readable representation.
     * 
     * @param desc The given type descriptor.
     * @return The human-readable representation.
     */
    static String convertDescToHuman(String desc) {
        return Type.getType(desc).getClassName();
    }

    public static void main(String[] args) throws Exception {
        DexDumper dumper = new DexDumper();
        dumper.execute(args[0], System.out);
    }
}
