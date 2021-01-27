package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

final class AlternativePattern extends Pattern {
    private final Pattern _left;
    private final Pattern _right;

    public AlternativePattern(Pattern pattern, Pattern pattern2) {
        this._left = pattern;
        this._right = pattern2;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void setParser(Parser parser) {
        super.setParser(parser);
        this._left.setParser(parser);
        this._right.setParser(parser);
    }

    public Pattern getLeft() {
        return this._left;
    }

    public Pattern getRight() {
        return this._right;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        this._left.typeCheck(symbolTable);
        this._right.typeCheck(symbolTable);
        return null;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern
    public double getPriority() {
        double priority = this._left.getPriority();
        double priority2 = this._right.getPriority();
        return priority < priority2 ? priority : priority2;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        return "alternative(" + this._left + ", " + this._right + ')';
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        this._left.translate(classGenerator, methodGenerator);
        BranchHandle append = instructionList.append((BranchInstruction) new GOTO(null));
        instructionList.append(methodGenerator.loadContextNode());
        this._right.translate(classGenerator, methodGenerator);
        this._left._trueList.backPatch(append);
        this._left._falseList.backPatch(append.getNext());
        this._trueList.append(this._right._trueList.add(append));
        this._falseList.append(this._right._falseList);
    }
}
