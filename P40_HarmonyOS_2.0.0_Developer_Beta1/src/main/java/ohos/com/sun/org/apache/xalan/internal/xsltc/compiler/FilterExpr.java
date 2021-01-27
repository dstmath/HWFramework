package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.ISTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeSetType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ReferenceType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

class FilterExpr extends Expression {
    private final Vector _predicates;
    private Expression _primary;

    public FilterExpr(Expression expression, Vector vector) {
        this._primary = expression;
        this._predicates = vector;
        expression.setParent(this);
    }

    /* access modifiers changed from: protected */
    public Expression getExpr() {
        Expression expression = this._primary;
        return expression instanceof CastExpr ? ((CastExpr) expression).getExpr() : expression;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void setParser(Parser parser) {
        super.setParser(parser);
        this._primary.setParser(parser);
        Vector vector = this._predicates;
        if (vector != null) {
            int size = vector.size();
            for (int i = 0; i < size; i++) {
                Expression expression = (Expression) this._predicates.elementAt(i);
                expression.setParser(parser);
                expression.setParent(this);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        return "filter-expr(" + this._primary + ", " + this._predicates + ")";
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        Type typeCheck = this._primary.typeCheck(symbolTable);
        Expression expression = this._primary;
        boolean z = expression instanceof KeyCall;
        if (!(typeCheck instanceof NodeSetType)) {
            if (typeCheck instanceof ReferenceType) {
                this._primary = new CastExpr(expression, Type.NodeSet);
            } else {
                throw new TypeCheckError(this);
            }
        }
        int size = this._predicates.size();
        for (int i = 0; i < size; i++) {
            Predicate predicate = (Predicate) this._predicates.elementAt(i);
            if (!z) {
                predicate.dontOptimize();
            }
            predicate.typeCheck(symbolTable);
        }
        Type type = Type.NodeSet;
        this._type = type;
        return type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        Vector vector = this._predicates;
        translateFilterExpr(classGenerator, methodGenerator, vector == null ? -1 : vector.size() - 1);
    }

    private void translateFilterExpr(ClassGenerator classGenerator, MethodGenerator methodGenerator, int i) {
        if (i >= 0) {
            translatePredicates(classGenerator, methodGenerator, i);
        } else {
            this._primary.translate(classGenerator, methodGenerator);
        }
    }

    public void translatePredicates(ClassGenerator classGenerator, MethodGenerator methodGenerator, int i) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (i < 0) {
            translateFilterExpr(classGenerator, methodGenerator, i);
            return;
        }
        int i2 = i - 1;
        Predicate predicate = (Predicate) this._predicates.get(i);
        translatePredicates(classGenerator, methodGenerator, i2);
        if (predicate.isNthPositionFilter()) {
            int addMethodref = constantPool.addMethodref(Constants.NTH_ITERATOR_CLASS, Constants.CONSTRUCTOR_NAME, "(Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;I)V");
            LocalVariableGen addLocalVariable = methodGenerator.addLocalVariable("filter_expr_tmp1", Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), null, null);
            addLocalVariable.setStart(instructionList.append(new ASTORE(addLocalVariable.getIndex())));
            predicate.translate(classGenerator, methodGenerator);
            LocalVariableGen addLocalVariable2 = methodGenerator.addLocalVariable("filter_expr_tmp2", Util.getJCRefType("I"), null, null);
            addLocalVariable2.setStart(instructionList.append(new ISTORE(addLocalVariable2.getIndex())));
            instructionList.append(new NEW(constantPool.addClass(Constants.NTH_ITERATOR_CLASS)));
            instructionList.append(DUP);
            addLocalVariable.setEnd(instructionList.append(new ALOAD(addLocalVariable.getIndex())));
            addLocalVariable2.setEnd(instructionList.append(new ILOAD(addLocalVariable2.getIndex())));
            instructionList.append(new INVOKESPECIAL(addMethodref));
            return;
        }
        int addMethodref2 = constantPool.addMethodref(Constants.CURRENT_NODE_LIST_ITERATOR, Constants.CONSTRUCTOR_NAME, "(Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;ZLohos.com.sun.org.apache.xalan.internal.xsltc.dom.CurrentNodeListFilter;ILohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;)V");
        LocalVariableGen addLocalVariable3 = methodGenerator.addLocalVariable("filter_expr_tmp1", Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), null, null);
        addLocalVariable3.setStart(instructionList.append(new ASTORE(addLocalVariable3.getIndex())));
        predicate.translate(classGenerator, methodGenerator);
        LocalVariableGen addLocalVariable4 = methodGenerator.addLocalVariable("filter_expr_tmp2", Util.getJCRefType(Constants.CURRENT_NODE_LIST_FILTER_SIG), null, null);
        addLocalVariable4.setStart(instructionList.append(new ASTORE(addLocalVariable4.getIndex())));
        instructionList.append(new NEW(constantPool.addClass(Constants.CURRENT_NODE_LIST_ITERATOR)));
        instructionList.append(DUP);
        addLocalVariable3.setEnd(instructionList.append(new ALOAD(addLocalVariable3.getIndex())));
        instructionList.append(ICONST_1);
        addLocalVariable4.setEnd(instructionList.append(new ALOAD(addLocalVariable4.getIndex())));
        instructionList.append(methodGenerator.loadCurrentNode());
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new INVOKESPECIAL(addMethodref2));
    }
}
