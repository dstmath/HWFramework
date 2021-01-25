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
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

final class ParentLocationPath extends RelativeLocationPath {
    private boolean _axisMismatch = false;
    private boolean _orderNodes = false;
    private final RelativeLocationPath _path;
    private Expression _step;
    private Type stype;

    public ParentLocationPath(RelativeLocationPath relativeLocationPath, Expression expression) {
        this._path = relativeLocationPath;
        this._step = expression;
        this._path.setParent(this);
        this._step.setParent(this);
        if (this._step instanceof Step) {
            this._axisMismatch = checkAxisMismatch();
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.RelativeLocationPath
    public void setAxis(int i) {
        this._path.setAxis(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.RelativeLocationPath
    public int getAxis() {
        return this._path.getAxis();
    }

    public RelativeLocationPath getPath() {
        return this._path;
    }

    public Expression getStep() {
        return this._step;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void setParser(Parser parser) {
        super.setParser(parser);
        this._step.setParser(parser);
        this._path.setParser(parser);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        return "ParentLocationPath(" + this._path + ", " + this._step + ')';
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        this.stype = this._step.typeCheck(symbolTable);
        this._path.typeCheck(symbolTable);
        if (this._axisMismatch) {
            enableNodeOrdering();
        }
        Type type = Type.NodeSet;
        this._type = type;
        return type;
    }

    public void enableNodeOrdering() {
        SyntaxTreeNode parent = getParent();
        if (parent instanceof ParentLocationPath) {
            ((ParentLocationPath) parent).enableNodeOrdering();
        } else {
            this._orderNodes = true;
        }
    }

    public boolean checkAxisMismatch() {
        int axis = this._path.getAxis();
        int axis2 = ((Step) this._step).getAxis();
        if (((axis == 0 || axis == 1) && (axis2 == 3 || axis2 == 4 || axis2 == 5 || axis2 == 10 || axis2 == 11 || axis2 == 12)) || ((axis == 3 && axis2 == 0) || axis2 == 1 || axis2 == 10 || axis2 == 11 || axis == 4 || axis == 5 || (((axis == 6 || axis == 7) && (axis2 == 6 || axis2 == 10 || axis2 == 11 || axis2 == 12)) || ((axis == 11 || axis == 12) && (axis2 == 4 || axis2 == 5 || axis2 == 6 || axis2 == 7 || axis2 == 10 || axis2 == 11 || axis2 == 12))))) {
            return true;
        }
        if (axis2 != 6 || axis != 3) {
            return false;
        }
        RelativeLocationPath relativeLocationPath = this._path;
        if (!(relativeLocationPath instanceof Step) || ((Step) relativeLocationPath).getNodeType() != 2) {
            return false;
        }
        return true;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        this._path.translate(classGenerator, methodGenerator);
        translateStep(classGenerator, methodGenerator);
    }

    public void translateStep(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        LocalVariableGen addLocalVariable = methodGenerator.addLocalVariable("parent_location_path_tmp1", Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), null, null);
        addLocalVariable.setStart(instructionList.append(new ASTORE(addLocalVariable.getIndex())));
        this._step.translate(classGenerator, methodGenerator);
        LocalVariableGen addLocalVariable2 = methodGenerator.addLocalVariable("parent_location_path_tmp2", Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), null, null);
        addLocalVariable2.setStart(instructionList.append(new ASTORE(addLocalVariable2.getIndex())));
        int addMethodref = constantPool.addMethodref(Constants.STEP_ITERATOR_CLASS, Constants.CONSTRUCTOR_NAME, "(Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;)V");
        instructionList.append(new NEW(constantPool.addClass(Constants.STEP_ITERATOR_CLASS)));
        instructionList.append(DUP);
        addLocalVariable.setEnd(instructionList.append(new ALOAD(addLocalVariable.getIndex())));
        addLocalVariable2.setEnd(instructionList.append(new ALOAD(addLocalVariable2.getIndex())));
        instructionList.append(new INVOKESPECIAL(addMethodref));
        Expression expression = this._step;
        if (expression instanceof ParentLocationPath) {
            expression = ((ParentLocationPath) expression).getStep();
        }
        RelativeLocationPath relativeLocationPath = this._path;
        if ((relativeLocationPath instanceof Step) && (expression instanceof Step)) {
            int axis = ((Step) relativeLocationPath).getAxis();
            int axis2 = ((Step) expression).getAxis();
            if ((axis == 5 && axis2 == 3) || (axis == 11 && axis2 == 10)) {
                instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.NODE_ITERATOR_BASE, "includeSelf", "()Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;")));
            }
        }
        if (this._orderNodes) {
            int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.ORDER_ITERATOR, Constants.ORDER_ITERATOR_SIG);
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(SWAP);
            instructionList.append(methodGenerator.loadContextNode());
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 3));
        }
    }
}
