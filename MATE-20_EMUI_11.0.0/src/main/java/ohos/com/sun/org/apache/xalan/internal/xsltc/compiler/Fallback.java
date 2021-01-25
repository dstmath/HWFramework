package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

final class Fallback extends Instruction {
    private boolean _active = false;

    public String toString() {
        return Constants.ELEMNAME_FALLBACK_STRING;
    }

    Fallback() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        if (this._active) {
            return typeCheckContents(symbolTable);
        }
        return Type.Void;
    }

    public void activate() {
        this._active = true;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        if (this._active) {
            parseChildren(parser);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        classGenerator.getConstantPool();
        methodGenerator.getInstructionList();
        if (this._active) {
            translateContents(classGenerator, methodGenerator);
        }
    }
}
