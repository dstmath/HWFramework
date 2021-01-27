package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

final class NumberCall extends FunctionCall {
    public NumberCall(QName qName, Vector vector) {
        super(qName, vector);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        if (argumentCount() > 0) {
            argument().typeCheck(symbolTable);
        }
        Type type = Type.Real;
        this._type = type;
        return type;
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
        if (!type.identicalTo(Type.Real)) {
            type.translateTo(classGenerator, methodGenerator, Type.Real);
        }
    }
}
