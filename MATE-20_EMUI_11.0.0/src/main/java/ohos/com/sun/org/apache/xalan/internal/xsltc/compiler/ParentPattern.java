package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.ISTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

/* access modifiers changed from: package-private */
public final class ParentPattern extends RelativePathPattern {
    private final Pattern _left;
    private final RelativePathPattern _right;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern
    public boolean isWildcard() {
        return false;
    }

    public ParentPattern(Pattern pattern, RelativePathPattern relativePathPattern) {
        this._left = pattern;
        pattern.setParent(this);
        this._right = relativePathPattern;
        relativePathPattern.setParent(this);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void setParser(Parser parser) {
        super.setParser(parser);
        this._left.setParser(parser);
        this._right.setParser(parser);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern
    public StepPattern getKernelPattern() {
        return this._right.getKernelPattern();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern
    public void reduceKernelPattern() {
        this._right.reduceKernelPattern();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        this._left.typeCheck(symbolTable);
        return this._right.typeCheck(symbolTable);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        LocalVariableGen addLocalVariable2 = methodGenerator.addLocalVariable2("ppt", Util.getJCRefType("I"), null);
        ILOAD iload = new ILOAD(addLocalVariable2.getIndex());
        ISTORE istore = new ISTORE(addLocalVariable2.getIndex());
        if (this._right.isWildcard()) {
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(SWAP);
        } else {
            RelativePathPattern relativePathPattern = this._right;
            if (relativePathPattern instanceof StepPattern) {
                instructionList.append(DUP);
                addLocalVariable2.setStart(instructionList.append(istore));
                this._right.translate(classGenerator, methodGenerator);
                instructionList.append(methodGenerator.loadDOM());
                addLocalVariable2.setEnd(instructionList.append(iload));
            } else {
                relativePathPattern.translate(classGenerator, methodGenerator);
                if (this._right instanceof AncestorPattern) {
                    instructionList.append(methodGenerator.loadDOM());
                    instructionList.append(SWAP);
                }
            }
        }
        instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.GET_PARENT, Constants.GET_PARENT_SIG), 2));
        SyntaxTreeNode parent = getParent();
        if (parent == null || (parent instanceof Instruction) || (parent instanceof TopLevelElement)) {
            this._left.translate(classGenerator, methodGenerator);
        } else {
            instructionList.append(DUP);
            InstructionHandle append = instructionList.append(istore);
            if (addLocalVariable2.getStart() == null) {
                addLocalVariable2.setStart(append);
            }
            this._left.translate(classGenerator, methodGenerator);
            instructionList.append(methodGenerator.loadDOM());
            addLocalVariable2.setEnd(instructionList.append(iload));
        }
        methodGenerator.removeLocalVariable(addLocalVariable2);
        RelativePathPattern relativePathPattern2 = this._right;
        if (relativePathPattern2 instanceof AncestorPattern) {
            this._left.backPatchFalseList(((AncestorPattern) relativePathPattern2).getLoopHandle());
        }
        this._trueList.append(this._right._trueList.append(this._left._trueList));
        this._falseList.append(this._right._falseList.append(this._left._falseList));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        return "Parent(" + this._left + ", " + this._right + ')';
    }
}
