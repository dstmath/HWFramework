package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;

final class NamespaceUriCall extends NameBase {
    public NamespaceUriCall(QName qName) {
        super(qName);
    }

    public NamespaceUriCall(QName qName, Vector vector) {
        super(qName, vector);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.NameBase, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getNamespaceName", "(I)Ljava/lang/String;");
        super.translate(classGenerator, methodGenerator);
        instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
    }
}
