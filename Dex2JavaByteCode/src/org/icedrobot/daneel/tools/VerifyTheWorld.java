package org.icedrobot.daneel.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.icedrobot.daneel.dex.DexClassVisitor;
import org.icedrobot.daneel.dex.DexFile;
import org.icedrobot.daneel.dex.DexFileVisitor;
import org.icedrobot.daneel.dex.DexReader;
import org.icedrobot.daneel.loader.Verifier;
import org.icedrobot.daneel.rewriter.DexRewriter;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

public class VerifyTheWorld {
    // java org.icedrobot.daneel.loader.VerifyTheWorld myfile.dex
    public static void main(String[] args) throws IOException { 
        final PrintWriter printWriter = new PrintWriter(System.err);
        final ClassLoader classloader = VerifyTheWorld.class.getClassLoader();
        
        final DexFile dexFile = DexFile.parse(new File(args[0]));
        
        dexFile.accept(new DexFileVisitor() {
            @Override
            public DexClassVisitor visitClass(final String name) {
                final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                
                return new DexRewriter(writer) {
                    @Override
                    public void visitEnd() {
                        super.visitEnd();
                        
                        byte[] byteArray = writer.toByteArray();
                        final String className = Type.getType(name).getInternalName().replace('/', '.');
                        Verifier.verify(classloader, dexFile, className, byteArray, printWriter);
                    }
                };
            }
            
            @Override
            public void visitEnd() {
                // do nothing
            }
        }, DexReader.SKIP_NONE);
    }
}
