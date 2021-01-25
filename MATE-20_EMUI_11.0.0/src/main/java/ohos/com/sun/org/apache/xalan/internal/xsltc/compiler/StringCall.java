package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

final class StringCall extends FunctionCall {
    public StringCall(QName qName, Vector vector) {
        super(qName, vector);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        int argumentCount = argumentCount();
        if (argumentCount <= 1) {
            if (argumentCount > 0) {
                argument().typeCheck(symbolTable);
            }
            Type type = Type.String;
            this._type = type;
            return type;
        }
        throw new TypeCheckError(new ErrorMsg(ErrorMsg.ILLEGAL_ARG_ERR, (SyntaxTreeNode) this));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        Type type;
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (argumentCount() == 0) {
            instructionList.append(methodGenerator.loadContextNode());
            type = Type.Node;
        } else {
            Expression argument = argument();
            argument.translate(classGenerator, methodGenerator);
            argument.startIterator(classGenerator, methodGenerator);
            type = argument.getType();
        }
        if (!type.identicalTo(Type.String)) {
            type.translateTo(classGenerator, methodGenerator, Type.String);
        }
    }
}
