package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.CompareGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TestGenerator;

final class LastCall extends FunctionCall {
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public boolean hasLastCall() {
        return true;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public boolean hasPositionCall() {
        return true;
    }

    public LastCall(QName qName) {
        super(qName);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (methodGenerator instanceof CompareGenerator) {
            instructionList.append(((CompareGenerator) methodGenerator).loadLastNode());
        } else if (methodGenerator instanceof TestGenerator) {
            instructionList.append(new ILOAD(3));
        } else {
            int addInterfaceMethodref = classGenerator.getConstantPool().addInterfaceMethodref(Constants.NODE_ITERATOR, "getLast", "()I");
            instructionList.append(methodGenerator.loadIterator());
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 1));
        }
    }
}
