package org.icedrobot.daneel.loader;

/**
 * Created with IntelliJ IDEA.
 * User: cusgadmin
 * Date: 4/24/12
 * Time: 1:10 AM
 * To change this template use File | Settings | File Templates.
 */

/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2007 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */


import org.objectweb.asm.*;

/**
 * An empty implementation of the ASM visitor interfaces.
 *
 * @author Eric Bruneton
 */
public class EmptyMethodVisitor extends MethodVisitor
{
    private static EmptyMethodVisitor singleton = new EmptyMethodVisitor();
    public static EmptyMethodVisitor get(){ return singleton;}

    private EmptyMethodVisitor(){
        super(Opcodes.ASM4);
    }

    @Override
    public AnnotationVisitor visitAnnotation(
            final String desc,
            final boolean visible)
    {
        return EmptyAnnotationVisitor.get();
    }

    @Override
    public void visitAttribute(final Attribute attr) {
    }

    public void visitEnd() {
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return EmptyAnnotationVisitor.get();
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(
            final int parameter,
            final String desc,
            final boolean visible)
    {
        return EmptyAnnotationVisitor.get();
    }

    @Override
    public void visitCode() {
    }

    @Override
    public void visitFrame(
            final int type,
            final int nLocal,
            final Object[] local,
            final int nStack,
            final Object[] stack)
    {
    }

    @Override
    public void visitInsn(final int opcode) {
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand) {
    }

    @Override
    public void visitVarInsn(final int opcode, final int var) {
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
    }

    @Override
    public void visitFieldInsn(
            final int opcode,
            final String owner,
            final String name,
            final String desc)
    {
    }

    @Override
    public void visitMethodInsn(
            final int opcode,
            final String owner,
            final String name,
            final String desc)
    {
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
    }

    @Override
    public void visitLabel(final Label label) {
    }

    @Override
    public void visitLdcInsn(final Object cst) {
    }

    @Override
    public void visitIincInsn(final int var, final int increment) {
    }

    @Override
    public void visitTableSwitchInsn(
            final int min,
            final int max,
            final Label dflt,
            final Label[] labels)
    {
    }

    @Override
    public void visitLookupSwitchInsn(
            final Label dflt,
            final int[] keys,
            final Label[] labels)
    {
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
    }

    @Override
    public void visitTryCatchBlock(
            final Label start,
            final Label end,
            final Label handler,
            final String type)
    {
    }

    @Override
    public void visitLocalVariable(
            final String name,
            final String desc,
            final String signature,
            final Label start,
            final Label end,
            final int index)
    {
    }

    @Override
    public void visitLineNumber(final int line, final Label start) {
    }

    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
    }
}



class EmptyAnnotationVisitor extends AnnotationVisitor
{
    private static EmptyAnnotationVisitor singleton = new EmptyAnnotationVisitor();

    public static EmptyAnnotationVisitor get(){
        return singleton;
    }

    private EmptyAnnotationVisitor(){
        super(Opcodes.ASM4);
    }

    @Override
    public void visitEnd() {
    }


    @Override
    public void visit(final String name, final Object value) {
    }

    @Override
    public void visitEnum(
            final String name,
            final String desc,
            final String value)
    {
    }

    @Override
    public AnnotationVisitor visitAnnotation(
            final String name,
            final String desc)
    {
        return this;
    }

    @Override
    public AnnotationVisitor visitArray(final String name) {
        return this;
    }
}


class EmptyFieldVisitor extends FieldVisitor
{
    private static EmptyFieldVisitor singleton = new EmptyFieldVisitor();
    public static EmptyFieldVisitor get(){ return singleton; }

    private EmptyFieldVisitor(){
        super(Opcodes.ASM4);
    }

    @Override
    public AnnotationVisitor visitAnnotation(
            final String desc,
            final boolean visible)
    {
        return EmptyAnnotationVisitor.get();
    }

    @Override
    public void visitAttribute(final Attribute attr) {
    }

    @Override
    public void visitEnd() {
    }
}
