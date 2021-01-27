package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.CompareGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TestGenerator;

/* access modifiers changed from: package-private */
public final class PositionCall extends FunctionCall {
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public boolean hasPositionCall() {
        return true;
    }

    public PositionCall(QName qName) {
        super(qName);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (methodGenerator instanceof CompareGenerator) {
            instructionList.append(((CompareGenerator) methodGenerator).loadCurrentNode());
        } else if (methodGenerator instanceof TestGenerator) {
            instructionList.append(new ILOAD(2));
        } else {
            int addInterfaceMethodref = classGenerator.getConstantPool().addInterfaceMethodref(Constants.NODE_ITERATOR, "getPosition", "()I");
            instructionList.append(methodGenerator.loadIterator());
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 1));
        }
    }
}
