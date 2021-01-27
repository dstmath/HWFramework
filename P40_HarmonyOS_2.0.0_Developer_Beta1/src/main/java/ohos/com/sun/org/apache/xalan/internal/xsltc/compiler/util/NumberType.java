package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

public abstract class NumberType extends Type {
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public boolean isNumber() {
        return true;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public boolean isSimple() {
        return true;
    }
}
