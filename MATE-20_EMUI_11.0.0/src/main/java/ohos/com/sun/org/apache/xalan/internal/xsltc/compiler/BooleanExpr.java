package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

final class BooleanExpr extends Expression {
    private boolean _value;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public boolean contextDependent() {
        return false;
    }

    public BooleanExpr(boolean z) {
        this._value = z;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        this._type = Type.Boolean;
        return this._type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        return this._value ? "true()" : "false()";
    }

    public boolean getValue() {
        return this._value;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        methodGenerator.getInstructionList().append(new PUSH(classGenerator.getConstantPool(), this._value));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public void translateDesynthesized(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (this._value) {
            instructionList.append(NOP);
        } else {
            this._falseList.add(instructionList.append((BranchInstruction) new GOTO(null)));
        }
    }
}
