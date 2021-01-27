package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

final class KeyCall extends FunctionCall {
    private Expression _name;
    private QName _resolvedQName = null;
    private Expression _value;
    private Type _valueType;

    public KeyCall(QName qName, Vector vector) {
        super(qName, vector);
        int argumentCount = argumentCount();
        if (argumentCount == 1) {
            this._name = null;
            this._value = argument(0);
        } else if (argumentCount != 2) {
            this._value = null;
            this._name = null;
        } else {
            this._name = argument(0);
            this._value = argument(1);
        }
    }

    public void addParentDependency() {
        if (this._resolvedQName != null) {
            SyntaxTreeNode syntaxTreeNode = this;
            while (syntaxTreeNode != null && !(syntaxTreeNode instanceof TopLevelElement)) {
                syntaxTreeNode = syntaxTreeNode.getParent();
            }
            TopLevelElement topLevelElement = (TopLevelElement) syntaxTreeNode;
            if (topLevelElement != null) {
                topLevelElement.addDependency(getSymbolTable().getKey(this._resolvedQName));
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        Type typeCheck = super.typeCheck(symbolTable);
        Expression expression = this._name;
        if (expression != null) {
            Type typeCheck2 = expression.typeCheck(symbolTable);
            Expression expression2 = this._name;
            if (expression2 instanceof LiteralExpr) {
                this._resolvedQName = getParser().getQNameIgnoreDefaultNs(((LiteralExpr) expression2).getValue());
            } else if (!(typeCheck2 instanceof StringType)) {
                this._name = new CastExpr(expression2, Type.String);
            }
        }
        this._valueType = this._value.typeCheck(symbolTable);
        if (!(this._valueType == Type.NodeSet || this._valueType == Type.Reference || this._valueType == Type.String)) {
            this._value = new CastExpr(this._value, Type.String);
            this._valueType = this._value.typeCheck(symbolTable);
        }
        addParentDependency();
        return typeCheck;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int addMethodref = constantPool.addMethodref(Constants.TRANSLET_CLASS, "getKeyIndex", "(Ljava/lang/String;)Lohos.com.sun.org.apache.xalan.internal.xsltc.dom.KeyIndex;");
        int addMethodref2 = constantPool.addMethodref(Constants.KEY_INDEX_CLASS, "setDom", "(Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;I)V");
        int addMethodref3 = constantPool.addMethodref(Constants.KEY_INDEX_CLASS, "getKeyIndexIterator", "(" + this._valueType.toSignature() + "Z)" + Constants.KEY_INDEX_ITERATOR_SIG);
        instructionList.append(classGenerator.loadTranslet());
        Expression expression = this._name;
        if (expression == null) {
            instructionList.append(new PUSH(constantPool, "##id"));
        } else {
            QName qName = this._resolvedQName;
            if (qName != null) {
                instructionList.append(new PUSH(constantPool, qName.toString()));
            } else {
                expression.translate(classGenerator, methodGenerator);
            }
        }
        instructionList.append(new INVOKEVIRTUAL(addMethodref));
        instructionList.append(DUP);
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(methodGenerator.loadCurrentNode());
        instructionList.append(new INVOKEVIRTUAL(addMethodref2));
        this._value.translate(classGenerator, methodGenerator);
        instructionList.append(this._name != null ? ICONST_1 : ICONST_0);
        instructionList.append(new INVOKEVIRTUAL(addMethodref3));
    }
}
