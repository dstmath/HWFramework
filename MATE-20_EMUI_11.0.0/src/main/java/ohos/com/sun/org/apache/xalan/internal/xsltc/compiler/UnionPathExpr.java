package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xml.internal.dtm.Axis;

/* access modifiers changed from: package-private */
public final class UnionPathExpr extends Expression {
    private Expression[] _components;
    private final Expression _pathExpr;
    private final Expression _rest;
    private boolean _reverse = false;

    public UnionPathExpr(Expression expression, Expression expression2) {
        this._pathExpr = expression;
        this._rest = expression2;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void setParser(Parser parser) {
        super.setParser(parser);
        Vector vector = new Vector();
        flatten(vector);
        int size = vector.size();
        this._components = (Expression[]) vector.toArray(new Expression[size]);
        for (int i = 0; i < size; i++) {
            this._components[i].setParser(parser);
            this._components[i].setParent(this);
            Expression[] expressionArr = this._components;
            if (expressionArr[i] instanceof Step) {
                Step step = (Step) expressionArr[i];
                int axis = step.getAxis();
                int nodeType = step.getNodeType();
                if (axis == 2 || nodeType == 2) {
                    Expression[] expressionArr2 = this._components;
                    expressionArr2[i] = expressionArr2[0];
                    expressionArr2[0] = step;
                }
                if (Axis.isReverse(axis)) {
                    this._reverse = true;
                }
            }
        }
        if (getParent() instanceof Expression) {
            this._reverse = false;
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        int length = this._components.length;
        for (int i = 0; i < length; i++) {
            if (this._components[i].typeCheck(symbolTable) != Type.NodeSet) {
                Expression[] expressionArr = this._components;
                expressionArr[i] = new CastExpr(expressionArr[i], Type.NodeSet);
            }
        }
        Type type = Type.NodeSet;
        this._type = type;
        return type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        return "union(" + this._pathExpr + ", " + this._rest + ')';
    }

    private void flatten(Vector vector) {
        vector.addElement(this._pathExpr);
        Expression expression = this._rest;
        if (expression == null) {
            return;
        }
        if (expression instanceof UnionPathExpr) {
            ((UnionPathExpr) expression).flatten(vector);
        } else {
            vector.addElement(expression);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int addMethodref = constantPool.addMethodref(Constants.UNION_ITERATOR_CLASS, Constants.CONSTRUCTOR_NAME, "(Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;)V");
        int addMethodref2 = constantPool.addMethodref(Constants.UNION_ITERATOR_CLASS, Constants.ADD_ITERATOR, Constants.ADD_ITERATOR_SIG);
        instructionList.append(new NEW(constantPool.addClass(Constants.UNION_ITERATOR_CLASS)));
        instructionList.append(DUP);
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(new INVOKESPECIAL(addMethodref));
        int length = this._components.length;
        for (int i = 0; i < length; i++) {
            this._components[i].translate(classGenerator, methodGenerator);
            instructionList.append(new INVOKEVIRTUAL(addMethodref2));
        }
        if (this._reverse) {
            int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.ORDER_ITERATOR, Constants.ORDER_ITERATOR_SIG);
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(SWAP);
            instructionList.append(methodGenerator.loadContextNode());
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 3));
        }
    }
}
