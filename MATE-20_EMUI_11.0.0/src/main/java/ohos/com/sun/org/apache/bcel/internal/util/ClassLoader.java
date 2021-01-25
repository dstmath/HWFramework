package ohos.com.sun.org.apache.bcel.internal.util;

import java.io.ByteArrayInputStream;
import java.util.Hashtable;
import ohos.com.sun.org.apache.bcel.internal.classfile.ClassParser;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantClass;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantPool;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantUtf8;
import ohos.com.sun.org.apache.bcel.internal.classfile.JavaClass;
import ohos.com.sun.org.apache.bcel.internal.classfile.Utility;

public class ClassLoader extends ClassLoader {
    private Hashtable classes = new Hashtable();
    private ClassLoader deferTo = getSystemClassLoader();
    private String[] ignored_packages = {"java.", "javax.", "sun."};
    private Repository repository = SyntheticRepository.getInstance();

    /* access modifiers changed from: protected */
    public JavaClass modifyClass(JavaClass javaClass) {
        return javaClass;
    }

    public ClassLoader() {
    }

    public ClassLoader(ClassLoader classLoader) {
        this.deferTo = classLoader;
        this.repository = new ClassLoaderRepository(classLoader);
    }

    public ClassLoader(String[] strArr) {
        addIgnoredPkgs(strArr);
    }

    public ClassLoader(ClassLoader classLoader, String[] strArr) {
        this.deferTo = classLoader;
        this.repository = new ClassLoaderRepository(classLoader);
        addIgnoredPkgs(strArr);
    }

    private void addIgnoredPkgs(String[] strArr) {
        int length = strArr.length;
        String[] strArr2 = this.ignored_packages;
        String[] strArr3 = new String[(length + strArr2.length)];
        System.arraycopy(strArr2, 0, strArr3, 0, strArr2.length);
        System.arraycopy(strArr, 0, strArr3, this.ignored_packages.length, strArr.length);
        this.ignored_packages = strArr3;
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.ClassLoader
    public Class loadClass(String str, boolean z) throws ClassNotFoundException {
        JavaClass javaClass;
        Class<?> cls = (Class) this.classes.get(str);
        if (cls == null) {
            int i = 0;
            while (true) {
                String[] strArr = this.ignored_packages;
                if (i >= strArr.length) {
                    break;
                } else if (str.startsWith(strArr[i])) {
                    cls = this.deferTo.loadClass(str);
                    break;
                } else {
                    i++;
                }
            }
            if (cls == null) {
                if (str.indexOf("$$BCEL$$") >= 0) {
                    javaClass = createClass(str);
                } else {
                    JavaClass loadClass = this.repository.loadClass(str);
                    if (loadClass != null) {
                        javaClass = modifyClass(loadClass);
                    } else {
                        throw new ClassNotFoundException(str);
                    }
                }
                if (javaClass != null) {
                    byte[] bytes = javaClass.getBytes();
                    cls = defineClass(str, bytes, 0, bytes.length);
                } else {
                    cls = Class.forName(str);
                }
            }
            if (z) {
                resolveClass(cls);
            }
        }
        this.classes.put(str, cls);
        return cls;
    }

    /* access modifiers changed from: protected */
    public JavaClass createClass(String str) {
        try {
            JavaClass parse = new ClassParser(new ByteArrayInputStream(Utility.decode(str.substring(str.indexOf("$$BCEL$$") + 8), true)), "foo").parse();
            ConstantPool constantPool = parse.getConstantPool();
            ((ConstantUtf8) constantPool.getConstant(((ConstantClass) constantPool.getConstant(parse.getClassNameIndex(), (byte) 7)).getNameIndex(), (byte) 1)).setBytes(str.replace('.', '/'));
            return parse;
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }
}
