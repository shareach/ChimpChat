/**
 * Created with IntelliJ IDEA.
 * User: cusgadmin
 * Date: 4/23/12
 * Time: 4:22 PM
 * To change this template use File | Settings | File Templates.
 */

import org.icedrobot.daneel.dex.DexClassVisitor;
import org.icedrobot.daneel.dex.DexFile;
import org.icedrobot.daneel.dex.DexFileVisitor;
import org.icedrobot.daneel.dex.DexReader;
import org.icedrobot.daneel.rewriter.DexRewriter;
import org.objectweb.asm.ClassWriter;

import java.io.*;

public class Main {
    public static void main(String args[]){
        if(args.length != 1){
            throw new RuntimeException("Filename required");
        }
        DexFile dexFile = loadDexFile(args[0]);
        D2JFileConverter converter = new D2JFileConverter("/tmp/result24328234");
        dexFile.accept(converter, DexReader.SKIP_ANNOTATIONS);
        return;
    }

    public static DexFile loadDexFile(String filename){
        try{
            File file = new File(filename);
            return DexFile.parse(file);
        }
        catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Something is wrong with loading dex file");
        }
    }
}

//NOTE : to recompile class files to dex
// ~/android-sdks/platform-tools/dx --dex --output=output.dex --no-strict <CLASS DIRECTORY>
// above dx file is merely a shell script
// actual dx implementation is provided by as dx.jar

class D2JFileConverter implements DexFileVisitor{
    String resultDir;

    public D2JFileConverter(String resultDir){
        this.resultDir = resultDir;
        (new File(resultDir)).mkdirs();
    }

    @Override
    public DexClassVisitor visitClass(String name){
        return new D2JClassConverter(new ClassWriter(ClassWriter.COMPUTE_MAXS), resultDir);
    }

    public void visitEnd(){

    }
}

class D2JClassConverter extends DexRewriter{
    private ClassWriter cv;
    String resultDir;

    public D2JClassConverter(ClassWriter cv, String resultDir){
        super(cv);
        this.cv = cv;
        this.resultDir = resultDir;
    }

    public void visit(int access, String name, String supername, String[] interfaces){
        super.visit(access, name, supername, interfaces);

        byte[] byteCode = cv.toByteArray();
        saveClass(name, byteCode);
    }

    private void saveClass(String name, byte[] byteCode){
        int indexRightBeforeFileName = name.lastIndexOf("/");
        String subDir = name.substring(1, indexRightBeforeFileName - 1);
        String fileName = name.substring(indexRightBeforeFileName + 1);
        fileName = fileName.replaceFirst(";",".class");

        File dir = new File(resultDir, subDir);
        dir.mkdirs();

        File classFile = new File(dir, fileName);

        try{
            if(!classFile.exists()) classFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(classFile, false);
            fos.write(byteCode);
            fos.flush();
        }
        catch(IOException e){
            throw new RuntimeException("Cannot create class file!");
        }
    }
}
