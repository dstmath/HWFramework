package ohos.com.sun.org.apache.bcel.internal;

import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.classfile.JavaClass;
import ohos.com.sun.org.apache.bcel.internal.util.ClassPath;
import ohos.com.sun.org.apache.bcel.internal.util.SyntheticRepository;

public abstract class Repository {
    private static ohos.com.sun.org.apache.bcel.internal.util.Repository _repository = SyntheticRepository.getInstance();

    public static ohos.com.sun.org.apache.bcel.internal.util.Repository getRepository() {
        return _repository;
    }

    public static void setRepository(ohos.com.sun.org.apache.bcel.internal.util.Repository repository) {
        _repository = repository;
    }

    public static JavaClass lookupClass(String str) {
        try {
            JavaClass findClass = _repository.findClass(str);
            return findClass == null ? _repository.loadClass(str) : findClass;
        } catch (ClassNotFoundException unused) {
            return null;
        }
    }

    public static JavaClass lookupClass(Class cls) {
        try {
            return _repository.loadClass(cls);
        } catch (ClassNotFoundException unused) {
            return null;
        }
    }

    public static ClassPath.ClassFile lookupClassFile(String str) {
        try {
            return ClassPath.SYSTEM_CLASS_PATH.getClassFile(str);
        } catch (IOException unused) {
            return null;
        }
    }

    public static void clearCache() {
        _repository.clear();
    }

    public static JavaClass addClass(JavaClass javaClass) {
        JavaClass findClass = _repository.findClass(javaClass.getClassName());
        _repository.storeClass(javaClass);
        return findClass;
    }

    public static void removeClass(String str) {
        ohos.com.sun.org.apache.bcel.internal.util.Repository repository = _repository;
        repository.removeClass(repository.findClass(str));
    }

    public static void removeClass(JavaClass javaClass) {
        _repository.removeClass(javaClass);
    }

    public static JavaClass[] getSuperClasses(JavaClass javaClass) {
        return javaClass.getSuperClasses();
    }

    public static JavaClass[] getSuperClasses(String str) {
        JavaClass lookupClass = lookupClass(str);
        if (lookupClass == null) {
            return null;
        }
        return getSuperClasses(lookupClass);
    }

    public static JavaClass[] getInterfaces(JavaClass javaClass) {
        return javaClass.getAllInterfaces();
    }

    public static JavaClass[] getInterfaces(String str) {
        return getInterfaces(lookupClass(str));
    }

    public static boolean instanceOf(JavaClass javaClass, JavaClass javaClass2) {
        return javaClass.instanceOf(javaClass2);
    }

    public static boolean instanceOf(String str, String str2) {
        return instanceOf(lookupClass(str), lookupClass(str2));
    }

    public static boolean instanceOf(JavaClass javaClass, String str) {
        return instanceOf(javaClass, lookupClass(str));
    }

    public static boolean instanceOf(String str, JavaClass javaClass) {
        return instanceOf(lookupClass(str), javaClass);
    }

    public static boolean implementationOf(JavaClass javaClass, JavaClass javaClass2) {
        return javaClass.implementationOf(javaClass2);
    }

    public static boolean implementationOf(String str, String str2) {
        return implementationOf(lookupClass(str), lookupClass(str2));
    }

    public static boolean implementationOf(JavaClass javaClass, String str) {
        return implementationOf(javaClass, lookupClass(str));
    }

    public static boolean implementationOf(String str, JavaClass javaClass) {
        return implementationOf(lookupClass(str), javaClass);
    }
}
