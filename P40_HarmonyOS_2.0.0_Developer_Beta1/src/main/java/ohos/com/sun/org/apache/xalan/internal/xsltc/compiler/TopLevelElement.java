package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

class TopLevelElement extends SyntaxTreeNode {
    protected Vector _dependencies = null;

    TopLevelElement() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        return typeCheckContents(symbolTable);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        getParser().reportError(2, new ErrorMsg(ErrorMsg.NOT_IMPLEMENTED_ERR, (Object) getClass(), (SyntaxTreeNode) this));
    }

    public InstructionList compile(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        InstructionList instructionList2 = new InstructionList();
        methodGenerator.setInstructionList(instructionList2);
        translate(classGenerator, methodGenerator);
        methodGenerator.setInstructionList(instructionList);
        return instructionList2;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        Util.println("TopLevelElement");
        displayContents(i + 4);
    }

    public void addDependency(TopLevelElement topLevelElement) {
        if (this._dependencies == null) {
            this._dependencies = new Vector();
        }
        if (!this._dependencies.contains(topLevelElement)) {
            this._dependencies.addElement(topLevelElement);
        }
    }

    public Vector getDependencies() {
        return this._dependencies;
    }
}
