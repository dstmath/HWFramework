package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.bundle.ProfileConstants;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.IFLT;
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

final class AncestorPattern extends RelativePathPattern {
    private final Pattern _left;
    private InstructionHandle _loop;
    private final RelativePathPattern _right;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern
    public boolean isWildcard() {
        return false;
    }

    public AncestorPattern(RelativePathPattern relativePathPattern) {
        this(null, relativePathPattern);
    }

    public AncestorPattern(Pattern pattern, RelativePathPattern relativePathPattern) {
        this._left = pattern;
        this._right = relativePathPattern;
        relativePathPattern.setParent(this);
        if (pattern != null) {
            pattern.setParent(this);
        }
    }

    public InstructionHandle getLoopHandle() {
        return this._loop;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void setParser(Parser parser) {
        super.setParser(parser);
        Pattern pattern = this._left;
        if (pattern != null) {
            pattern.setParser(parser);
        }
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
        Pattern pattern = this._left;
        if (pattern != null) {
            pattern.typeCheck(symbolTable);
        }
        return this._right.typeCheck(symbolTable);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        LocalVariableGen addLocalVariable2 = methodGenerator.addLocalVariable2(ProfileConstants.APP, Util.getJCRefType("I"), instructionList.getEnd());
        ILOAD iload = new ILOAD(addLocalVariable2.getIndex());
        ISTORE istore = new ISTORE(addLocalVariable2.getIndex());
        RelativePathPattern relativePathPattern = this._right;
        if (relativePathPattern instanceof StepPattern) {
            instructionList.append(DUP);
            instructionList.append(istore);
            this._right.translate(classGenerator, methodGenerator);
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(iload);
        } else {
            relativePathPattern.translate(classGenerator, methodGenerator);
            if (this._right instanceof AncestorPattern) {
                instructionList.append(methodGenerator.loadDOM());
                instructionList.append(SWAP);
            }
        }
        if (this._left != null) {
            InstructionHandle append = instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.GET_PARENT, Constants.GET_PARENT_SIG), 2));
            instructionList.append(DUP);
            instructionList.append(istore);
            this._falseList.add(instructionList.append((BranchInstruction) new IFLT(null)));
            instructionList.append(iload);
            this._left.translate(classGenerator, methodGenerator);
            SyntaxTreeNode parent = getParent();
            if (parent != null && !(parent instanceof Instruction) && !(parent instanceof TopLevelElement)) {
                instructionList.append(iload);
            }
            BranchHandle append2 = instructionList.append((BranchInstruction) new GOTO(null));
            this._loop = instructionList.append(methodGenerator.loadDOM());
            instructionList.append(iload);
            addLocalVariable2.setEnd(this._loop);
            instructionList.append((BranchInstruction) new GOTO(append));
            append2.setTarget(instructionList.append(NOP));
            this._left.backPatchFalseList(this._loop);
            this._trueList.append(this._left._trueList);
        } else {
            instructionList.append(POP2);
        }
        RelativePathPattern relativePathPattern2 = this._right;
        if (relativePathPattern2 instanceof AncestorPattern) {
            this._falseList.backPatch(((AncestorPattern) relativePathPattern2).getLoopHandle());
        }
        this._trueList.append(this._right._trueList);
        this._falseList.append(this._right._falseList);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        return "AncestorPattern(" + this._left + ", " + this._right + ')';
    }
}
