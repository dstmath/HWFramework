package ohos.com.sun.org.apache.bcel.internal.util;

import java.io.Serializable;
import java.util.Stack;
import ohos.com.sun.org.apache.bcel.internal.classfile.JavaClass;

public class ClassStack implements Serializable {
    private Stack stack = new Stack();

    public void push(JavaClass javaClass) {
        this.stack.push(javaClass);
    }

    public JavaClass pop() {
        return (JavaClass) this.stack.pop();
    }

    public JavaClass top() {
        return (JavaClass) this.stack.peek();
    }

    public boolean empty() {
        return this.stack.empty();
    }
}
