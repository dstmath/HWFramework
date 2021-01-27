package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

public interface Closure {
    void addVariable(VariableRefBase variableRefBase);

    String getInnerClassName();

    Closure getParentClosure();

    boolean inInnerClass();
}
