package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

final class SimpleAttributeValue extends AttributeValue {
    private String _value;

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public boolean contextDependent() {
        return false;
    }

    public SimpleAttributeValue(String str) {
        this._value = str;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        Type type = Type.String;
        this._type = type;
        return type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        return this._value;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        methodGenerator.getInstructionList().append(new PUSH(classGenerator.getConstantPool(), this._value));
    }
}
