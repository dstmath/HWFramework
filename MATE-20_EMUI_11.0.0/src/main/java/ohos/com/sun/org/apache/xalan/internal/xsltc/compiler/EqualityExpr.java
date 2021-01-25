package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.IFEQ;
import ohos.com.sun.org.apache.bcel.internal.generic.IFNE;
import ohos.com.sun.org.apache.bcel.internal.generic.IF_ICMPEQ;
import ohos.com.sun.org.apache.bcel.internal.generic.IF_ICMPNE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.BooleanType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.IntType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeSetType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NumberType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.RealType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ReferenceType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ResultTreeType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.Operators;

/* access modifiers changed from: package-private */
public final class EqualityExpr extends Expression {
    private Expression _left;
    private final int _op;
    private Expression _right;

    public EqualityExpr(int i, Expression expression, Expression expression2) {
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
    public String toString() {
        return Operators.getOpNames(this._op) + '(' + this._left + ", " + this._right + ')';
    }

    public Expression getLeft() {
        return this._left;
    }

    public Expression getRight() {
        return this._right;
    }

    public boolean getOp() {
        return this._op != 1;
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
        if (!this._left.hasLastCall() && !this._right.hasLastCall()) {
            return false;
        }
        return true;
    }

    private void swapArguments() {
        Expression expression = this._left;
        this._left = this._right;
        this._right = expression;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        Type typeCheck = this._left.typeCheck(symbolTable);
        Type typeCheck2 = this._right.typeCheck(symbolTable);
        if (!typeCheck.isSimple() || !typeCheck2.isSimple()) {
            if (typeCheck instanceof ReferenceType) {
                this._right = new CastExpr(this._right, Type.Reference);
            } else if (typeCheck2 instanceof ReferenceType) {
                this._left = new CastExpr(this._left, Type.Reference);
            } else {
                boolean z = typeCheck instanceof NodeType;
                if (z && typeCheck2 == Type.String) {
                    this._left = new CastExpr(this._left, Type.String);
                } else if (typeCheck == Type.String && (typeCheck2 instanceof NodeType)) {
                    this._right = new CastExpr(this._right, Type.String);
                } else if (z && (typeCheck2 instanceof NodeType)) {
                    this._left = new CastExpr(this._left, Type.String);
                    this._right = new CastExpr(this._right, Type.String);
                } else if (!z || !(typeCheck2 instanceof NodeSetType)) {
                    if (!(typeCheck instanceof NodeSetType) || !(typeCheck2 instanceof NodeType)) {
                        if (z) {
                            this._left = new CastExpr(this._left, Type.NodeSet);
                        }
                        if (typeCheck2 instanceof NodeType) {
                            this._right = new CastExpr(this._right, Type.NodeSet);
                        }
                        if (typeCheck.isSimple() || ((typeCheck instanceof ResultTreeType) && (typeCheck2 instanceof NodeSetType))) {
                            swapArguments();
                        }
                        if (this._right.getType() instanceof IntType) {
                            this._right = new CastExpr(this._right, Type.Real);
                        }
                    } else {
                        swapArguments();
                    }
                }
            }
        } else if (typeCheck != typeCheck2) {
            if (typeCheck instanceof BooleanType) {
                this._right = new CastExpr(this._right, Type.Boolean);
            } else if (typeCheck2 instanceof BooleanType) {
                this._left = new CastExpr(this._left, Type.Boolean);
            } else if ((typeCheck instanceof NumberType) || (typeCheck2 instanceof NumberType)) {
                this._left = new CastExpr(this._left, Type.Real);
                this._right = new CastExpr(this._right, Type.Real);
            } else {
                this._left = new CastExpr(this._left, Type.String);
                this._right = new CastExpr(this._right, Type.String);
            }
        }
        Type type = Type.Boolean;
        this._type = type;
        return type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public void translateDesynthesized(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        BranchInstruction branchInstruction;
        BranchInstruction branchInstruction2;
        BranchInstruction branchInstruction3;
        Type type = this._left.getType();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (type instanceof BooleanType) {
            this._left.translate(classGenerator, methodGenerator);
            this._right.translate(classGenerator, methodGenerator);
            FlowList flowList = this._falseList;
            if (this._op == 0) {
                branchInstruction3 = new IF_ICMPNE(null);
            } else {
                branchInstruction3 = new IF_ICMPEQ(null);
            }
            flowList.add(instructionList.append(branchInstruction3));
        } else if (type instanceof NumberType) {
            this._left.translate(classGenerator, methodGenerator);
            this._right.translate(classGenerator, methodGenerator);
            if (type instanceof RealType) {
                instructionList.append(DCMPG);
                FlowList flowList2 = this._falseList;
                if (this._op == 0) {
                    branchInstruction2 = new IFNE(null);
                } else {
                    branchInstruction2 = new IFEQ(null);
                }
                flowList2.add(instructionList.append(branchInstruction2));
                return;
            }
            FlowList flowList3 = this._falseList;
            if (this._op == 0) {
                branchInstruction = new IF_ICMPNE(null);
            } else {
                branchInstruction = new IF_ICMPEQ(null);
            }
            flowList3.add(instructionList.append(branchInstruction));
        } else {
            translate(classGenerator, methodGenerator);
            desynthesize(classGenerator, methodGenerator);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        BranchInstruction branchInstruction;
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        Type type = this._left.getType();
        Type type2 = this._right.getType();
        if ((type instanceof BooleanType) || (type instanceof NumberType)) {
            translateDesynthesized(classGenerator, methodGenerator);
            synthesize(classGenerator, methodGenerator);
        } else if (type instanceof StringType) {
            int addMethodref = constantPool.addMethodref("java.lang.String", "equals", "(Ljava/lang/Object;)Z");
            this._left.translate(classGenerator, methodGenerator);
            this._right.translate(classGenerator, methodGenerator);
            instructionList.append(new INVOKEVIRTUAL(addMethodref));
            if (this._op == 1) {
                instructionList.append(ICONST_1);
                instructionList.append(IXOR);
            }
        } else if (!(type instanceof ResultTreeType)) {
            boolean z = type instanceof NodeSetType;
            if (z && (type2 instanceof BooleanType)) {
                this._left.translate(classGenerator, methodGenerator);
                this._left.startIterator(classGenerator, methodGenerator);
                Type.NodeSet.translateTo(classGenerator, methodGenerator, Type.Boolean);
                this._right.translate(classGenerator, methodGenerator);
                instructionList.append(IXOR);
                if (this._op == 0) {
                    instructionList.append(ICONST_1);
                    instructionList.append(IXOR);
                }
            } else if (!z || !(type2 instanceof StringType)) {
                this._left.translate(classGenerator, methodGenerator);
                this._left.startIterator(classGenerator, methodGenerator);
                this._right.translate(classGenerator, methodGenerator);
                this._right.startIterator(classGenerator, methodGenerator);
                if (type2 instanceof ResultTreeType) {
                    type2.translateTo(classGenerator, methodGenerator, Type.String);
                    type2 = Type.String;
                }
                instructionList.append(new PUSH(constantPool, this._op));
                instructionList.append(methodGenerator.loadDOM());
                instructionList.append(new INVOKESTATIC(constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "compare", "(" + type.toSignature() + type2.toSignature() + "I" + Constants.DOM_INTF_SIG + ")Z")));
            } else {
                this._left.translate(classGenerator, methodGenerator);
                this._left.startIterator(classGenerator, methodGenerator);
                this._right.translate(classGenerator, methodGenerator);
                instructionList.append(new PUSH(constantPool, this._op));
                instructionList.append(methodGenerator.loadDOM());
                instructionList.append(new INVOKESTATIC(constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "compare", "(" + type.toSignature() + type2.toSignature() + "I" + Constants.DOM_INTF_SIG + ")Z")));
            }
        } else if (type2 instanceof BooleanType) {
            this._right.translate(classGenerator, methodGenerator);
            if (this._op == 1) {
                instructionList.append(ICONST_1);
                instructionList.append(IXOR);
            }
        } else if (type2 instanceof RealType) {
            this._left.translate(classGenerator, methodGenerator);
            type.translateTo(classGenerator, methodGenerator, Type.Real);
            this._right.translate(classGenerator, methodGenerator);
            instructionList.append(DCMPG);
            if (this._op == 0) {
                branchInstruction = new IFNE(null);
            } else {
                branchInstruction = new IFEQ(null);
            }
            BranchHandle append = instructionList.append(branchInstruction);
            instructionList.append(ICONST_1);
            BranchHandle append2 = instructionList.append((BranchInstruction) new GOTO(null));
            append.setTarget(instructionList.append(ICONST_0));
            append2.setTarget(instructionList.append(NOP));
        } else {
            this._left.translate(classGenerator, methodGenerator);
            type.translateTo(classGenerator, methodGenerator, Type.String);
            this._right.translate(classGenerator, methodGenerator);
            if (type2 instanceof ResultTreeType) {
                type2.translateTo(classGenerator, methodGenerator, Type.String);
            }
            instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref("java.lang.String", "equals", "(Ljava/lang/Object;)Z")));
            if (this._op == 1) {
                instructionList.append(ICONST_1);
                instructionList.append(IXOR);
            }
        }
    }
}
