package junit.runner;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

public abstract class ClassPathTestCollector implements TestCollector {
    static final int SUFFIX_LENGTH = 0;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: junit.runner.ClassPathTestCollector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: junit.runner.ClassPathTestCollector.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: junit.runner.ClassPathTestCollector.<clinit>():void");
    }

    public Enumeration collectTests() {
        return collectFilesInPath(System.getProperty("java.class.path")).elements();
    }

    public Hashtable collectFilesInPath(String classPath) {
        return collectFilesInRoots(splitClassPath(classPath));
    }

    Hashtable collectFilesInRoots(Vector roots) {
        Hashtable result = new Hashtable(100);
        Enumeration e = roots.elements();
        while (e.hasMoreElements()) {
            gatherFiles(new File((String) e.nextElement()), "", result);
        }
        return result;
    }

    void gatherFiles(File classRoot, String classFileName, Hashtable result) {
        File thisRoot = new File(classRoot, classFileName);
        if (thisRoot.isFile()) {
            if (isTestClass(classFileName)) {
                String className = classNameFromFile(classFileName);
                result.put(className, className);
            }
            return;
        }
        String[] contents = thisRoot.list();
        if (contents != null) {
            for (String str : contents) {
                gatherFiles(classRoot, classFileName + File.separatorChar + str, result);
            }
        }
    }

    Vector splitClassPath(String classPath) {
        Vector result = new Vector();
        StringTokenizer tokenizer = new StringTokenizer(classPath, System.getProperty("path.separator"));
        while (tokenizer.hasMoreTokens()) {
            result.addElement(tokenizer.nextToken());
        }
        return result;
    }

    protected boolean isTestClass(String classFileName) {
        if (!classFileName.endsWith(".class") || classFileName.indexOf(36) >= 0 || classFileName.indexOf("Test") <= 0) {
            return false;
        }
        return true;
    }

    protected String classNameFromFile(String classFileName) {
        String s2 = classFileName.substring(0, classFileName.length() - SUFFIX_LENGTH).replace(File.separatorChar, '.');
        if (s2.startsWith(".")) {
            return s2.substring(1);
        }
        return s2;
    }
}
