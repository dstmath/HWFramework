package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeSetType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ReferenceType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

final class FilterParentPath extends Expression {
    private Expression _filterExpr;
    private boolean _hasDescendantAxis = false;
    private Expression _path;

    public FilterParentPath(Expression expression, Expression expression2) {
        this._path = expression2;
        expression2.setParent(this);
        this._filterExpr = expression;
        expression.setParent(this);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void setParser(Parser parser) {
        super.setParser(parser);
        this._filterExpr.setParser(parser);
        this._path.setParser(parser);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        return "FilterParentPath(" + this._filterExpr + ", " + this._path + ')';
    }

    public void setDescendantAxis() {
        this._hasDescendantAxis = true;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        Type typeCheck = this._filterExpr.typeCheck(symbolTable);
        if (!(typeCheck instanceof NodeSetType)) {
            if (typeCheck instanceof ReferenceType) {
                this._filterExpr = new CastExpr(this._filterExpr, Type.NodeSet);
            } else if (typeCheck instanceof NodeType) {
                this._filterExpr = new CastExpr(this._filterExpr, Type.NodeSet);
            } else {
                throw new TypeCheckError(this);
            }
        }
        if (!(this._path.typeCheck(symbolTable) instanceof NodeSetType)) {
            this._path = new CastExpr(this._path, Type.NodeSet);
        }
        Type type = Type.NodeSet;
        this._type = type;
        return type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int addMethodref = constantPool.addMethodref(Constants.STEP_ITERATOR_CLASS, Constants.CONSTRUCTOR_NAME, "(Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;)V");
        this._filterExpr.translate(classGenerator, methodGenerator);
        LocalVariableGen addLocalVariable = methodGenerator.addLocalVariable("filter_parent_path_tmp1", Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), null, null);
        addLocalVariable.setStart(instructionList.append(new ASTORE(addLocalVariable.getIndex())));
        this._path.translate(classGenerator, methodGenerator);
        LocalVariableGen addLocalVariable2 = methodGenerator.addLocalVariable("filter_parent_path_tmp2", Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), null, null);
        addLocalVariable2.setStart(instructionList.append(new ASTORE(addLocalVariable2.getIndex())));
        instructionList.append(new NEW(constantPool.addClass(Constants.STEP_ITERATOR_CLASS)));
        instructionList.append(DUP);
        addLocalVariable.setEnd(instructionList.append(new ALOAD(addLocalVariable.getIndex())));
        addLocalVariable2.setEnd(instructionList.append(new ALOAD(addLocalVariable2.getIndex())));
        instructionList.append(new INVOKESPECIAL(addMethodref));
        if (this._hasDescendantAxis) {
            instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.NODE_ITERATOR_BASE, "includeSelf", "()Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;")));
        }
        SyntaxTreeNode parent = getParent();
        if (!((parent instanceof RelativeLocationPath) || (parent instanceof FilterParentPath) || (parent instanceof KeyCall) || (parent instanceof CurrentCall) || (parent instanceof DocumentCall))) {
            int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.ORDER_ITERATOR, Constants.ORDER_ITERATOR_SIG);
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(SWAP);
            instructionList.append(methodGenerator.loadContextNode());
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 3));
        }
    }
}
