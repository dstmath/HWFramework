package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;

/* access modifiers changed from: package-private */
public final class NotCall extends FunctionCall {
    public NotCall(QName qName, Vector vector) {
        super(qName, vector);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        argument().translate(classGenerator, methodGenerator);
        instructionList.append(ICONST_1);
        instructionList.append(IXOR);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public void translateDesynthesized(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        Expression argument = argument();
        argument.translateDesynthesized(classGenerator, methodGenerator);
        BranchHandle append = instructionList.append((BranchInstruction) new GOTO(null));
        this._trueList = argument._falseList;
        this._falseList = argument._trueList;
        this._falseList.add(append);
    }
}
