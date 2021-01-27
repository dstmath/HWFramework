package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

final class LogicalExpr extends Expression {
    public static final int AND = 1;
    public static final int OR = 0;
    private static final String[] Ops = {"or", "and"};
    private Expression _left;
    private final int _op;
    private Expression _right;

    public LogicalExpr(int i, Expression expression, Expression expression2) {
        this._op = i;
        this._left = expression;
        expression.setParent(this);
        this._right = expression2;
        expression2.setParent(this);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public boolean hasPositionCall() {
        return this._left.hasPositionCall() || this._right.hasPositionCall();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public boolean hasLastCall() {
        return this._left.hasLastCall() || this._right.hasLastCall();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public Object evaluateAtCompileTime() {
        Object evaluateAtCompileTime = this._left.evaluateAtCompileTime();
        Object evaluateAtCompileTime2 = this._right.evaluateAtCompileTime();
        if (evaluateAtCompileTime == null || evaluateAtCompileTime2 == null) {
            return null;
        }
        return this._op == 1 ? (evaluateAtCompileTime == Boolean.TRUE && evaluateAtCompileTime2 == Boolean.TRUE) ? Boolean.TRUE : Boolean.FALSE : (evaluateAtCompileTime == Boolean.TRUE || evaluateAtCompileTime2 == Boolean.TRUE) ? Boolean.TRUE : Boolean.FALSE;
    }

    public int getOp() {
        return this._op;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void setParser(Parser parser) {
        super.setParser(parser);
        this._left.setParser(parser);
        this._right.setParser(parser);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        return Ops[this._op] + '(' + this._left + ", " + this._right + ')';
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        Type typeCheck = this._left.typeCheck(symbolTable);
        Type typeCheck2 = this._right.typeCheck(symbolTable);
        MethodType lookupPrimop = lookupPrimop(symbolTable, Ops[this._op], new MethodType(Type.Void, typeCheck, typeCheck2));
        if (lookupPrimop != null) {
            Type type = (Type) lookupPrimop.argsType().elementAt(0);
            if (!type.identicalTo(typeCheck)) {
                this._left = new CastExpr(this._left, type);
            }
            if (!((Type) lookupPrimop.argsType().elementAt(1)).identicalTo(typeCheck2)) {
                this._right = new CastExpr(this._right, type);
            }
            Type resultType = lookupPrimop.resultType();
            this._type = resultType;
            return resultType;
        }
        throw new TypeCheckError(this);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        translateDesynthesized(classGenerator, methodGenerator);
        synthesize(classGenerator, methodGenerator);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public void translateDesynthesized(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        getParent();
        if (this._op == 1) {
            this._left.translateDesynthesized(classGenerator, methodGenerator);
            InstructionHandle append = instructionList.append(NOP);
            this._right.translateDesynthesized(classGenerator, methodGenerator);
            InstructionHandle append2 = instructionList.append(NOP);
            this._falseList.append(this._right._falseList.append(this._left._falseList));
            Expression expression = this._left;
            if (!(expression instanceof LogicalExpr) || ((LogicalExpr) expression).getOp() != 0) {
                Expression expression2 = this._left;
                if (expression2 instanceof NotCall) {
                    expression2.backPatchTrueList(append);
                } else {
                    this._trueList.append(this._left._trueList);
                }
            } else {
                this._left.backPatchTrueList(append);
            }
            Expression expression3 = this._right;
            if (!(expression3 instanceof LogicalExpr) || ((LogicalExpr) expression3).getOp() != 0) {
                Expression expression4 = this._right;
                if (expression4 instanceof NotCall) {
                    expression4.backPatchTrueList(append2);
                } else {
                    this._trueList.append(this._right._trueList);
                }
            } else {
                this._right.backPatchTrueList(append2);
            }
        } else {
            this._left.translateDesynthesized(classGenerator, methodGenerator);
            BranchHandle append3 = instructionList.append((BranchInstruction) new GOTO(null));
            this._right.translateDesynthesized(classGenerator, methodGenerator);
            this._left._trueList.backPatch(append3);
            this._left._falseList.backPatch(append3.getNext());
            this._falseList.append(this._right._falseList);
            this._trueList.add(append3).append(this._right._trueList);
        }
    }
}
