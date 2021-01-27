package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;

final class ElementAvailableCall extends FunctionCall {
    public ElementAvailableCall(QName qName, Vector vector) {
        super(qName, vector);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        if (argument() instanceof LiteralExpr) {
            Type type = Type.Boolean;
            this._type = type;
            return type;
        }
        throw new TypeCheckError(new ErrorMsg(ErrorMsg.NEED_LITERAL_ERR, (Object) Keywords.FUNC_EXT_ELEM_AVAILABLE_STRING, (SyntaxTreeNode) this));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public Object evaluateAtCompileTime() {
        return getResult() ? Boolean.TRUE : Boolean.FALSE;
    }

    public boolean getResult() {
        try {
            LiteralExpr literalExpr = (LiteralExpr) argument();
            String value = literalExpr.getValue();
            int indexOf = value.indexOf(58);
            if (indexOf > 0) {
                value = value.substring(indexOf + 1);
            }
            return getParser().elementSupported(literalExpr.getNamespace(), value);
        } catch (ClassCastException unused) {
            return false;
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        methodGenerator.getInstructionList().append(new PUSH(classGenerator.getConstantPool(), getResult()));
    }
}
