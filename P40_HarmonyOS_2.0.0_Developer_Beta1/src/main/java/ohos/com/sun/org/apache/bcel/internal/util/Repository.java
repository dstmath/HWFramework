package ohos.com.sun.org.apache.bcel.internal.util;

import java.io.Serializable;
import ohos.com.sun.org.apache.bcel.internal.classfile.JavaClass;

public interface Repository extends Serializable {
    void clear();

    JavaClass findClass(String str);

    JavaClass loadClass(Class cls) throws ClassNotFoundException;

    JavaClass loadClass(String str) throws ClassNotFoundException;

    void removeClass(JavaClass javaClass);

    void storeClass(JavaClass javaClass);
}
