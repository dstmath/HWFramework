package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

final class ConcatCall extends FunctionCall {
    public ConcatCall(QName qName, Vector vector) {
        super(qName, vector);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        for (int i = 0; i < argumentCount(); i++) {
            Expression argument = argument(i);
            if (!argument.typeCheck(symbolTable).identicalTo(Type.String)) {
                setArgument(i, new CastExpr(argument, Type.String));
            }
        }
        Type type = Type.String;
        this._type = type;
        return type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int argumentCount = argumentCount();
        if (argumentCount == 0) {
            instructionList.append(new PUSH(constantPool, ""));
        } else if (argumentCount != 1) {
            int addMethodref = constantPool.addMethodref(Constants.STRING_BUFFER_CLASS, Constants.CONSTRUCTOR_NAME, "()V");
            INVOKEVIRTUAL invokevirtual = new INVOKEVIRTUAL(constantPool.addMethodref(Constants.STRING_BUFFER_CLASS, "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;"));
            int addMethodref2 = constantPool.addMethodref(Constants.STRING_BUFFER_CLASS, "toString", "()Ljava/lang/String;");
            instructionList.append(new NEW(constantPool.addClass(Constants.STRING_BUFFER_CLASS)));
            instructionList.append(DUP);
            instructionList.append(new INVOKESPECIAL(addMethodref));
            for (int i = 0; i < argumentCount; i++) {
                argument(i).translate(classGenerator, methodGenerator);
                instructionList.append(invokevirtual);
            }
            instructionList.append(new INVOKEVIRTUAL(addMethodref2));
        } else {
            argument().translate(classGenerator, methodGenerator);
        }
    }
}
