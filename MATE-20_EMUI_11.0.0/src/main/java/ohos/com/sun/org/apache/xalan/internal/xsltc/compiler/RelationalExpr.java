package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.BooleanType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.IntType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeSetType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.RealType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ReferenceType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ResultTreeType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.Operators;

final class RelationalExpr extends Expression {
    private Expression _left;
    private int _op;
    private Expression _right;

    public RelationalExpr(int i, Expression expression, Expression expression2) {
        this._op = i;
        this._left = expression;
        expression.setParent(this);
        this._right = expression2;
        expression2.setParent(this);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void setParser(Parser parser) {
        super.setParser(parser);
        this._left.setParser(parser);
        this._right.setParser(parser);
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

    public boolean hasReferenceArgs() {
        return (this._left.getType() instanceof ReferenceType) || (this._right.getType() instanceof ReferenceType);
    }

    public boolean hasNodeArgs() {
        return (this._left.getType() instanceof NodeType) || (this._right.getType() instanceof NodeType);
    }

    public boolean hasNodeSetArgs() {
        return (this._left.getType() instanceof NodeSetType) || (this._right.getType() instanceof NodeSetType);
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0062  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0064  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x006b  */
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        Type type;
        Type typeCheck = this._left.typeCheck(symbolTable);
        Type typeCheck2 = this._right.typeCheck(symbolTable);
        if ((typeCheck instanceof ResultTreeType) && (typeCheck2 instanceof ResultTreeType)) {
            this._right = new CastExpr(this._right, Type.Real);
            this._left = new CastExpr(this._left, Type.Real);
            Type type2 = Type.Boolean;
            this._type = type2;
            return type2;
        } else if (hasReferenceArgs()) {
            boolean z = typeCheck instanceof ReferenceType;
            Type type3 = null;
            if (z) {
                Expression expression = this._left;
                if (expression instanceof VariableRefBase) {
                    type = ((VariableRefBase) expression).getVariable().getType();
                    if (typeCheck2 instanceof ReferenceType) {
                        Expression expression2 = this._right;
                        if (expression2 instanceof VariableRefBase) {
                            type3 = ((VariableRefBase) expression2).getVariable().getType();
                        }
                    }
                    if (type != null) {
                        type = type3;
                    } else if (type3 != null) {
                        type = Type.Real;
                    }
                    if (type == null) {
                        type = Type.Real;
                    }
                    this._right = new CastExpr(this._right, type);
                    this._left = new CastExpr(this._left, type);
                    Type type4 = Type.Boolean;
                    this._type = type4;
                    return type4;
                }
            }
            type = null;
            if (typeCheck2 instanceof ReferenceType) {
            }
            if (type != null) {
            }
            if (type == null) {
            }
            this._right = new CastExpr(this._right, type);
            this._left = new CastExpr(this._left, type);
            Type type42 = Type.Boolean;
            this._type = type42;
            return type42;
        } else if (hasNodeSetArgs()) {
            if (typeCheck2 instanceof NodeSetType) {
                Expression expression3 = this._right;
                this._right = this._left;
                this._left = expression3;
                int i = this._op;
                int i2 = 4;
                if (i == 2) {
                    i2 = 3;
                } else if (i == 3) {
                    i2 = 2;
                } else if (i == 4) {
                    i2 = 5;
                }
                this._op = i2;
                typeCheck2 = this._right.getType();
            }
            if (typeCheck2 instanceof NodeType) {
                this._right = new CastExpr(this._right, Type.NodeSet);
            }
            if (typeCheck2 instanceof IntType) {
                this._right = new CastExpr(this._right, Type.Real);
            }
            if (typeCheck2 instanceof ResultTreeType) {
                this._right = new CastExpr(this._right, Type.String);
            }
            Type type5 = Type.Boolean;
            this._type = type5;
            return type5;
        } else {
            if (hasNodeArgs()) {
                if (typeCheck instanceof BooleanType) {
                    this._right = new CastExpr(this._right, Type.Boolean);
                    typeCheck2 = Type.Boolean;
                }
                if (typeCheck2 instanceof BooleanType) {
                    this._left = new CastExpr(this._left, Type.Boolean);
                    typeCheck = Type.Boolean;
                }
            }
            MethodType lookupPrimop = lookupPrimop(symbolTable, Operators.getOpNames(this._op), new MethodType(Type.Void, typeCheck, typeCheck2));
            if (lookupPrimop != null) {
                Type type6 = (Type) lookupPrimop.argsType().elementAt(0);
                if (!type6.identicalTo(typeCheck)) {
                    this._left = new CastExpr(this._left, type6);
                }
                if (!((Type) lookupPrimop.argsType().elementAt(1)).identicalTo(typeCheck2)) {
                    this._right = new CastExpr(this._right, type6);
                }
                Type resultType = lookupPrimop.resultType();
                this._type = resultType;
                return resultType;
            }
            throw new TypeCheckError(this);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        if (hasNodeSetArgs() || hasReferenceArgs()) {
            ConstantPoolGen constantPool = classGenerator.getConstantPool();
            InstructionList instructionList = methodGenerator.getInstructionList();
            this._left.translate(classGenerator, methodGenerator);
            this._left.startIterator(classGenerator, methodGenerator);
            this._right.translate(classGenerator, methodGenerator);
            this._right.startIterator(classGenerator, methodGenerator);
            instructionList.append(new PUSH(constantPool, this._op));
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(new INVOKESTATIC(constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "compare", "(" + this._left.getType().toSignature() + this._right.getType().toSignature() + "I" + Constants.DOM_INTF_SIG + ")Z")));
            return;
        }
        translateDesynthesized(classGenerator, methodGenerator);
        synthesize(classGenerator, methodGenerator);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public void translateDesynthesized(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        if (hasNodeSetArgs() || hasReferenceArgs()) {
            translate(classGenerator, methodGenerator);
            desynthesize(classGenerator, methodGenerator);
            return;
        }
        BranchInstruction branchInstruction = null;
        InstructionList instructionList = methodGenerator.getInstructionList();
        this._left.translate(classGenerator, methodGenerator);
        this._right.translate(classGenerator, methodGenerator);
        Type type = this._left.getType();
        boolean z = true;
        boolean z2 = false;
        if (type instanceof RealType) {
            int i = this._op;
            if (i == 3 || i == 5) {
                z2 = true;
            }
            instructionList.append(type.CMP(z2));
            type = Type.Int;
        } else {
            z = false;
        }
        int i2 = this._op;
        if (i2 == 2) {
            branchInstruction = type.LE(z);
        } else if (i2 == 3) {
            branchInstruction = type.GE(z);
        } else if (i2 == 4) {
            branchInstruction = type.LT(z);
        } else if (i2 != 5) {
            getParser().reportError(2, new ErrorMsg(ErrorMsg.ILLEGAL_RELAT_OP_ERR, (SyntaxTreeNode) this));
        } else {
            branchInstruction = type.GT(z);
        }
        this._falseList.add(instructionList.append(branchInstruction));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        return Operators.getOpNames(this._op) + '(' + this._left + ", " + this._right + ')';
    }
}
