package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO_W;
import ohos.com.sun.org.apache.bcel.internal.generic.IF_ICMPEQ;
import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.ISTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

final class AbsolutePathPattern extends LocationPathPattern {
    private final RelativePathPattern _left;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern
    public boolean isWildcard() {
        return false;
    }

    public AbsolutePathPattern(RelativePathPattern relativePathPattern) {
        this._left = relativePathPattern;
        if (relativePathPattern != null) {
            relativePathPattern.setParent(this);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void setParser(Parser parser) {
        super.setParser(parser);
        RelativePathPattern relativePathPattern = this._left;
        if (relativePathPattern != null) {
            relativePathPattern.setParser(parser);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        RelativePathPattern relativePathPattern = this._left;
        return relativePathPattern == null ? Type.Root : relativePathPattern.typeCheck(symbolTable);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern
    public StepPattern getKernelPattern() {
        RelativePathPattern relativePathPattern = this._left;
        if (relativePathPattern != null) {
            return relativePathPattern.getKernelPattern();
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern
    public void reduceKernelPattern() {
        this._left.reduceKernelPattern();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        RelativePathPattern relativePathPattern = this._left;
        if (relativePathPattern != null) {
            if (relativePathPattern instanceof StepPattern) {
                LocalVariableGen addLocalVariable2 = methodGenerator.addLocalVariable2("apptmp", Util.getJCRefType("I"), null);
                instructionList.append(DUP);
                addLocalVariable2.setStart(instructionList.append(new ISTORE(addLocalVariable2.getIndex())));
                this._left.translate(classGenerator, methodGenerator);
                instructionList.append(methodGenerator.loadDOM());
                addLocalVariable2.setEnd(instructionList.append(new ILOAD(addLocalVariable2.getIndex())));
                methodGenerator.removeLocalVariable(addLocalVariable2);
            } else {
                relativePathPattern.translate(classGenerator, methodGenerator);
            }
        }
        int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.GET_PARENT, Constants.GET_PARENT_SIG);
        int addInterfaceMethodref2 = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getExpandedTypeID", Constants.GET_PARENT_SIG);
        InstructionHandle append = instructionList.append(methodGenerator.loadDOM());
        instructionList.append(SWAP);
        instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
        if (this._left instanceof AncestorPattern) {
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(SWAP);
        }
        instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref2, 2));
        instructionList.append(new PUSH(constantPool, 9));
        BranchHandle append2 = instructionList.append((BranchInstruction) new IF_ICMPEQ(null));
        this._falseList.add(instructionList.append((BranchInstruction) new GOTO_W(null)));
        append2.setTarget(instructionList.append(NOP));
        RelativePathPattern relativePathPattern2 = this._left;
        if (relativePathPattern2 != null) {
            relativePathPattern2.backPatchTrueList(append);
            RelativePathPattern relativePathPattern3 = this._left;
            if (relativePathPattern3 instanceof AncestorPattern) {
                this._falseList.backPatch(((AncestorPattern) relativePathPattern3).getLoopHandle());
            }
            this._falseList.append(this._left._falseList);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("absolutePathPattern(");
        RelativePathPattern relativePathPattern = this._left;
        sb.append(relativePathPattern != null ? relativePathPattern.toString() : ")");
        return sb.toString();
    }
}
