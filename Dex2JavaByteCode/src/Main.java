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
import org.icedrobot.daneel.loader.Verifier;
import org.icedrobot.daneel.rewriter.DexRewriter;
import org.objectweb.asm.ClassWriter;

import java.io.*;

public class Main {
    public static void main(String args[]){

        System.setProperty("daneel.verify", "true");

        //if(args.length != 1){
        //    throw new RuntimeException("Filename required");
        //}
        DexFile dexFile = loadDexFile("/tmp/classes.dex");
        D2JFileConverter converter = new D2JFileConverter("/tmp/result24328234", dexFile);
        dexFile.accept(converter, DexReader.SKIP_NONE);
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
    DexFile dexFile;

    public D2JFileConverter(String resultDir, DexFile dexFile){
        this.resultDir = resultDir;
        this.dexFile = dexFile;
        (new File(resultDir)).mkdirs();
    }

    @Override
    public DexClassVisitor visitClass(String name){
        return new D2JClassConverter(new ClassWriter(ClassWriter.COMPUTE_MAXS), resultDir, dexFile);
    }

    public void visitEnd(){

    }
}

class D2JClassConverter extends DexRewriter{
    private ClassWriter cv;
    String resultDir;
    DexFile dexFile;
    String className;

    public D2JClassConverter(ClassWriter cv, String resultDir, DexFile dexFile){
        super(cv);
        this.cv = cv;
        this.resultDir = resultDir;
        this.dexFile = dexFile;
    }

    @Override
    public void visit(int access, String name, String supername, String[] interfaces){
        this.className = name;
        super.visit(access, name, supername, interfaces);
    }

    @Override
    public void visitEnd(){
        byte[] byteCode = cv.toByteArray();
        saveClass(byteCode);
    }

    private void saveClass(byte[] byteCode){

        int indexRightBeforeFileName = className.lastIndexOf("/");
        String subDir = className.substring(1, indexRightBeforeFileName - 1);
        String fileName = className.substring(indexRightBeforeFileName + 1);
        fileName = fileName.replaceFirst(";",".class");

        String classFullName = (subDir + "/" + fileName).replaceAll("/",".");
        System.out.println(className);
        System.out.println(classFullName);
        //Verifier.verify(null, dexFile, classFullName, byteCode, new PrintWriter(System.err));

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
