package ohos.com.sun.org.apache.bcel.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import ohos.com.sun.org.apache.bcel.internal.classfile.ClassParser;
import ohos.com.sun.org.apache.bcel.internal.classfile.JavaClass;

public class ClassLoaderRepository implements Repository {
    private HashMap loadedClasses = new HashMap();
    private ClassLoader loader;

    public ClassLoaderRepository(ClassLoader classLoader) {
        this.loader = classLoader;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.util.Repository
    public void storeClass(JavaClass javaClass) {
        this.loadedClasses.put(javaClass.getClassName(), javaClass);
        javaClass.setRepository(this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.util.Repository
    public void removeClass(JavaClass javaClass) {
        this.loadedClasses.remove(javaClass.getClassName());
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.util.Repository
    public JavaClass findClass(String str) {
        if (this.loadedClasses.containsKey(str)) {
            return (JavaClass) this.loadedClasses.get(str);
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.util.Repository
    public JavaClass loadClass(String str) throws ClassNotFoundException {
        String replace = str.replace('.', '/');
        JavaClass findClass = findClass(str);
        if (findClass != null) {
            return findClass;
        }
        try {
            ClassLoader classLoader = this.loader;
            InputStream resourceAsStream = classLoader.getResourceAsStream(replace + ".class");
            if (resourceAsStream != null) {
                JavaClass parse = new ClassParser(resourceAsStream, str).parse();
                storeClass(parse);
                return parse;
            }
            throw new ClassNotFoundException(str + " not found.");
        } catch (IOException e) {
            throw new ClassNotFoundException(e.toString());
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.util.Repository
    public JavaClass loadClass(Class cls) throws ClassNotFoundException {
        return loadClass(cls.getName());
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.util.Repository
    public void clear() {
        this.loadedClasses.clear();
    }
}
