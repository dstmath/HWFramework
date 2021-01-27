package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.CHECKCAST;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ObjectType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

final class CastCall extends FunctionCall {
    private String _className;
    private Expression _right;

    public CastCall(QName qName, Vector vector) {
        super(qName, vector);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        if (argumentCount() == 2) {
            Expression argument = argument(0);
            if (argument instanceof LiteralExpr) {
                this._className = ((LiteralExpr) argument).getValue();
                this._type = Type.newObjectType(this._className);
                this._right = argument(1);
                Type typeCheck = this._right.typeCheck(symbolTable);
                if (typeCheck == Type.Reference || (typeCheck instanceof ObjectType)) {
                    return this._type;
                }
                throw new TypeCheckError(new ErrorMsg("DATA_CONVERSION_ERR", typeCheck, this._type, this));
            }
            throw new TypeCheckError(new ErrorMsg(ErrorMsg.NEED_LITERAL_ERR, (Object) getName(), (SyntaxTreeNode) this));
        }
        throw new TypeCheckError(new ErrorMsg(ErrorMsg.ILLEGAL_ARG_ERR, (Object) getName(), (SyntaxTreeNode) this));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        this._right.translate(classGenerator, methodGenerator);
        instructionList.append(new CHECKCAST(constantPool.addClass(this._className)));
    }
}
