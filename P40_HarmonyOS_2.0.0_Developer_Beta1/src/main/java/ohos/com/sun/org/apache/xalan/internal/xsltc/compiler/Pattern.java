package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

public abstract class Pattern extends Expression {
    public abstract double getPriority();

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public abstract void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator);

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public abstract Type typeCheck(SymbolTable symbolTable) throws TypeCheckError;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public /* bridge */ /* synthetic */ void backPatchFalseList(InstructionHandle instructionHandle) {
        super.backPatchFalseList(instructionHandle);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public /* bridge */ /* synthetic */ void backPatchTrueList(InstructionHandle instructionHandle) {
        super.backPatchTrueList(instructionHandle);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public /* bridge */ /* synthetic */ void desynthesize(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        super.desynthesize(classGenerator, methodGenerator);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public /* bridge */ /* synthetic */ Object evaluateAtCompileTime() {
        return super.evaluateAtCompileTime();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public /* bridge */ /* synthetic */ FlowList getFalseList() {
        return super.getFalseList();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public /* bridge */ /* synthetic */ FlowList getTrueList() {
        return super.getTrueList();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public /* bridge */ /* synthetic */ Type getType() {
        return super.getType();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public /* bridge */ /* synthetic */ boolean hasLastCall() {
        return super.hasLastCall();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public /* bridge */ /* synthetic */ boolean hasPositionCall() {
        return super.hasPositionCall();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public /* bridge */ /* synthetic */ MethodType lookupPrimop(SymbolTable symbolTable, String str, MethodType methodType) {
        return super.lookupPrimop(symbolTable, str, methodType);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public /* bridge */ /* synthetic */ void startIterator(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        super.startIterator(classGenerator, methodGenerator);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public /* bridge */ /* synthetic */ void synthesize(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        super.synthesize(classGenerator, methodGenerator);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public /* bridge */ /* synthetic */ void translateDesynthesized(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        super.translateDesynthesized(classGenerator, methodGenerator);
    }
}
