package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.global.icu.impl.locale.LanguageTag;

final class BinOpExpr extends Expression {
    public static final int DIV = 3;
    public static final int MINUS = 1;
    public static final int MOD = 4;
    private static final String[] Ops = {"+", LanguageTag.SEP, "*", PsuedoNames.PSEUDONAME_ROOT, "%"};
    public static final int PLUS = 0;
    public static final int TIMES = 2;
    private Expression _left;
    private int _op;
    private Expression _right;

    public BinOpExpr(int i, Expression expression, Expression expression2) {
        this._op = i;
        this._left = expression;
        expression.setParent(this);
        this._right = expression2;
        expression2.setParent(this);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public boolean hasPositionCall() {
        if (!this._left.hasPositionCall() && !this._right.hasPositionCall()) {
            return false;
        }
        return true;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public boolean hasLastCall() {
        return this._left.hasLastCall() || this._right.hasLastCall();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void setParser(Parser parser) {
        super.setParser(parser);
        this._left.setParser(parser);
        this._right.setParser(parser);
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
        InstructionList instructionList = methodGenerator.getInstructionList();
        this._left.translate(classGenerator, methodGenerator);
        this._right.translate(classGenerator, methodGenerator);
        int i = this._op;
        if (i == 0) {
            instructionList.append(this._type.ADD());
        } else if (i == 1) {
            instructionList.append(this._type.SUB());
        } else if (i == 2) {
            instructionList.append(this._type.MUL());
        } else if (i == 3) {
            instructionList.append(this._type.DIV());
        } else if (i != 4) {
            getParser().reportError(3, new ErrorMsg(ErrorMsg.ILLEGAL_BINARY_OP_ERR, (SyntaxTreeNode) this));
        } else {
            instructionList.append(this._type.REM());
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        return Ops[this._op] + '(' + this._left + ", " + this._right + ')';
    }
}
