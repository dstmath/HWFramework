package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import java.util.Stack;

public final class StringStack extends Stack {
    static final long serialVersionUID = -1506910875640317898L;

    public String peekString() {
        return (String) super.peek();
    }

    public String popString() {
        return (String) super.pop();
    }

    public String pushString(String str) {
        return (String) super.push(str);
    }
}
