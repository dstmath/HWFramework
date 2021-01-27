package ohos.com.sun.org.apache.bcel.internal.util;

import java.io.Serializable;
import java.util.ArrayList;
import ohos.com.sun.org.apache.bcel.internal.classfile.JavaClass;

public class ClassVector implements Serializable {
    protected ArrayList vec = new ArrayList();

    public void addElement(JavaClass javaClass) {
        this.vec.add(javaClass);
    }

    public JavaClass elementAt(int i) {
        return (JavaClass) this.vec.get(i);
    }

    public void removeElementAt(int i) {
        this.vec.remove(i);
    }

    public JavaClass[] toArray() {
        JavaClass[] javaClassArr = new JavaClass[this.vec.size()];
        this.vec.toArray(javaClassArr);
        return javaClassArr;
    }
}
