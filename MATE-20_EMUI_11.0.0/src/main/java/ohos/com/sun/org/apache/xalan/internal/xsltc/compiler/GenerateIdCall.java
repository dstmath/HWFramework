package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;

final class GenerateIdCall extends FunctionCall {
    public GenerateIdCall(QName qName, Vector vector) {
        super(qName, vector);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (argumentCount() == 0) {
            instructionList.append(methodGenerator.loadContextNode());
        } else {
            argument().translate(classGenerator, methodGenerator);
        }
        instructionList.append(new INVOKESTATIC(classGenerator.getConstantPool().addMethodref(Constants.BASIS_LIBRARY_CLASS, "generate_idF", "(I)Ljava/lang/String;")));
    }
}
