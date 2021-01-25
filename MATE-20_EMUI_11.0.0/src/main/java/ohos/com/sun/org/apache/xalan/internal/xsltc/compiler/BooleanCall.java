package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

final class BooleanCall extends FunctionCall {
    private Expression _arg;

    public BooleanCall(QName qName, Vector vector) {
        super(qName, vector);
        this._arg = null;
        this._arg = argument(0);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        this._arg.typeCheck(symbolTable);
        Type type = Type.Boolean;
        this._type = type;
        return type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        this._arg.translate(classGenerator, methodGenerator);
        Type type = this._arg.getType();
        if (!type.identicalTo(Type.Boolean)) {
            this._arg.startIterator(classGenerator, methodGenerator);
            type.translateTo(classGenerator, methodGenerator, Type.Boolean);
        }
    }
}
