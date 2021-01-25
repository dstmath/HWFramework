package ohos.com.sun.org.apache.bcel.internal.util;

import java.io.Serializable;
import java.util.LinkedList;
import ohos.com.sun.org.apache.bcel.internal.classfile.JavaClass;

public class ClassQueue implements Serializable {
    protected LinkedList vec = new LinkedList();

    public void enqueue(JavaClass javaClass) {
        this.vec.addLast(javaClass);
    }

    public JavaClass dequeue() {
        return (JavaClass) this.vec.removeFirst();
    }

    public boolean empty() {
        return this.vec.isEmpty();
    }

    @Override // java.lang.Object
    public String toString() {
        return this.vec.toString();
    }
}
