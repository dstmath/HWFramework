package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.IF_ICMPNE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.SIPUSH;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.BooleanType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MultiHashtable;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ResultTreeType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

/* access modifiers changed from: package-private */
public final class CastExpr extends Expression {
    private static final MultiHashtable<Type, Type> InternalTypeMap = new MultiHashtable<>();
    private final Expression _left;
    private boolean _typeTest = false;

    static {
        InternalTypeMap.put(Type.Boolean, Type.Boolean);
        InternalTypeMap.put(Type.Boolean, Type.Real);
        InternalTypeMap.put(Type.Boolean, Type.String);
        InternalTypeMap.put(Type.Boolean, Type.Reference);
        InternalTypeMap.put(Type.Boolean, Type.Object);
        InternalTypeMap.put(Type.Real, Type.Real);
        InternalTypeMap.put(Type.Real, Type.Int);
        InternalTypeMap.put(Type.Real, Type.Boolean);
        InternalTypeMap.put(Type.Real, Type.String);
        InternalTypeMap.put(Type.Real, Type.Reference);
        InternalTypeMap.put(Type.Real, Type.Object);
        InternalTypeMap.put(Type.Int, Type.Int);
        InternalTypeMap.put(Type.Int, Type.Real);
        InternalTypeMap.put(Type.Int, Type.Boolean);
        InternalTypeMap.put(Type.Int, Type.String);
        InternalTypeMap.put(Type.Int, Type.Reference);
        InternalTypeMap.put(Type.Int, Type.Object);
        InternalTypeMap.put(Type.String, Type.String);
        InternalTypeMap.put(Type.String, Type.Boolean);
        InternalTypeMap.put(Type.String, Type.Real);
        InternalTypeMap.put(Type.String, Type.Reference);
        InternalTypeMap.put(Type.String, Type.Object);
        InternalTypeMap.put(Type.NodeSet, Type.NodeSet);
        InternalTypeMap.put(Type.NodeSet, Type.Boolean);
        InternalTypeMap.put(Type.NodeSet, Type.Real);
        InternalTypeMap.put(Type.NodeSet, Type.String);
        InternalTypeMap.put(Type.NodeSet, Type.Node);
        InternalTypeMap.put(Type.NodeSet, Type.Reference);
        InternalTypeMap.put(Type.NodeSet, Type.Object);
        InternalTypeMap.put(Type.Node, Type.Node);
        InternalTypeMap.put(Type.Node, Type.Boolean);
        InternalTypeMap.put(Type.Node, Type.Real);
        InternalTypeMap.put(Type.Node, Type.String);
        InternalTypeMap.put(Type.Node, Type.NodeSet);
        InternalTypeMap.put(Type.Node, Type.Reference);
        InternalTypeMap.put(Type.Node, Type.Object);
        InternalTypeMap.put(Type.ResultTree, Type.ResultTree);
        InternalTypeMap.put(Type.ResultTree, Type.Boolean);
        InternalTypeMap.put(Type.ResultTree, Type.Real);
        InternalTypeMap.put(Type.ResultTree, Type.String);
        InternalTypeMap.put(Type.ResultTree, Type.NodeSet);
        InternalTypeMap.put(Type.ResultTree, Type.Reference);
        InternalTypeMap.put(Type.ResultTree, Type.Object);
        InternalTypeMap.put(Type.Reference, Type.Reference);
        InternalTypeMap.put(Type.Reference, Type.Boolean);
        InternalTypeMap.put(Type.Reference, Type.Int);
        InternalTypeMap.put(Type.Reference, Type.Real);
        InternalTypeMap.put(Type.Reference, Type.String);
        InternalTypeMap.put(Type.Reference, Type.Node);
        InternalTypeMap.put(Type.Reference, Type.NodeSet);
        InternalTypeMap.put(Type.Reference, Type.ResultTree);
        InternalTypeMap.put(Type.Reference, Type.Object);
        InternalTypeMap.put(Type.Object, Type.String);
        InternalTypeMap.put(Type.Void, Type.String);
        InternalTypeMap.makeUnmodifiable();
    }

    public CastExpr(Expression expression, Type type) throws TypeCheckError {
        this._left = expression;
        this._type = type;
        if ((this._left instanceof Step) && this._type == Type.Boolean) {
            Step step = (Step) this._left;
            if (step.getAxis() == 13 && step.getNodeType() != -1) {
                this._typeTest = true;
            }
        }
        setParser(expression.getParser());
        setParent(expression.getParent());
        expression.setParent(this);
        typeCheck(expression.getParser().getSymbolTable());
    }

    public Expression getExpr() {
        return this._left;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public boolean hasPositionCall() {
        return this._left.hasPositionCall();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public boolean hasLastCall() {
        return this._left.hasLastCall();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        return "cast(" + this._left + ", " + this._type + ")";
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        Type type = this._left.getType();
        if (type == null) {
            type = this._left.typeCheck(symbolTable);
        }
        if (type instanceof NodeType) {
            type = Type.Node;
        } else if (type instanceof ResultTreeType) {
            type = Type.ResultTree;
        }
        if (InternalTypeMap.maps(type, this._type) != null) {
            return this._type;
        }
        throw new TypeCheckError(new ErrorMsg("DATA_CONVERSION_ERR", type.toString(), this._type.toString()));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public void translateDesynthesized(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        Type type = this._left.getType();
        if (this._typeTest) {
            ConstantPoolGen constantPool = classGenerator.getConstantPool();
            InstructionList instructionList = methodGenerator.getInstructionList();
            int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getExpandedTypeID", Constants.GET_PARENT_SIG);
            instructionList.append(new SIPUSH((short) ((Step) this._left).getNodeType()));
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(methodGenerator.loadContextNode());
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
            this._falseList.add(instructionList.append((BranchInstruction) new IF_ICMPNE(null)));
            return;
        }
        this._left.translate(classGenerator, methodGenerator);
        if (this._type != type) {
            this._left.startIterator(classGenerator, methodGenerator);
            if (this._type instanceof BooleanType) {
                FlowList translateToDesynthesized = type.translateToDesynthesized(classGenerator, methodGenerator, this._type);
                if (translateToDesynthesized != null) {
                    this._falseList.append(translateToDesynthesized);
                    return;
                }
                return;
            }
            type.translateTo(classGenerator, methodGenerator, this._type);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        Type type = this._left.getType();
        this._left.translate(classGenerator, methodGenerator);
        if (!this._type.identicalTo(type)) {
            this._left.startIterator(classGenerator, methodGenerator);
            type.translateTo(classGenerator, methodGenerator, this._type);
        }
    }
}
