package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.ArrayList;
import ohos.com.sun.org.apache.bcel.internal.classfile.Field;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.CHECKCAST;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GETFIELD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.bcel.internal.generic.PUTFIELD;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.BooleanType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.FilterGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.IntType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NumberType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ReferenceType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ResultTreeType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TestGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;

/* access modifiers changed from: package-private */
public final class Predicate extends Expression implements Closure {
    private boolean _canOptimize = true;
    private String _className = null;
    private ArrayList _closureVars = null;
    private Expression _exp = null;
    private boolean _nthDescendant = false;
    private boolean _nthPositionFilter = false;
    private Closure _parentClosure = null;
    int _ptype = -1;
    private Step _step = null;
    private Expression _value = null;

    public Predicate(Expression expression) {
        this._exp = expression;
        this._exp.setParent(this);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void setParser(Parser parser) {
        super.setParser(parser);
        this._exp.setParser(parser);
    }

    public boolean isNthPositionFilter() {
        return this._nthPositionFilter;
    }

    public boolean isNthDescendant() {
        return this._nthDescendant;
    }

    public void dontOptimize() {
        this._canOptimize = false;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public boolean hasPositionCall() {
        return this._exp.hasPositionCall();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public boolean hasLastCall() {
        return this._exp.hasLastCall();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Closure
    public boolean inInnerClass() {
        return this._className != null;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Closure
    public Closure getParentClosure() {
        if (this._parentClosure == null) {
            SyntaxTreeNode parent = getParent();
            while (true) {
                if (!(parent instanceof Closure)) {
                    if (parent instanceof TopLevelElement) {
                        break;
                    }
                    parent = parent.getParent();
                    if (parent == null) {
                        break;
                    }
                } else {
                    this._parentClosure = (Closure) parent;
                    break;
                }
            }
        }
        return this._parentClosure;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Closure
    public String getInnerClassName() {
        return this._className;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Closure
    public void addVariable(VariableRefBase variableRefBase) {
        if (this._closureVars == null) {
            this._closureVars = new ArrayList();
        }
        if (!this._closureVars.contains(variableRefBase)) {
            this._closureVars.add(variableRefBase);
            Closure parentClosure = getParentClosure();
            if (parentClosure != null) {
                parentClosure.addVariable(variableRefBase);
            }
        }
    }

    public int getPosType() {
        if (this._ptype == -1) {
            SyntaxTreeNode parent = getParent();
            if (parent instanceof StepPattern) {
                this._ptype = ((StepPattern) parent).getNodeType();
            } else if (parent instanceof AbsoluteLocationPath) {
                Expression path = ((AbsoluteLocationPath) parent).getPath();
                if (path instanceof Step) {
                    this._ptype = ((Step) path).getNodeType();
                }
            } else if (parent instanceof VariableRefBase) {
                Expression expression = ((VariableRefBase) parent).getVariable().getExpression();
                if (expression instanceof Step) {
                    this._ptype = ((Step) expression).getNodeType();
                }
            } else if (parent instanceof Step) {
                this._ptype = ((Step) parent).getNodeType();
            }
        }
        return this._ptype;
    }

    public boolean parentIsPattern() {
        return getParent() instanceof Pattern;
    }

    public Expression getExpr() {
        return this._exp;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        return "pred(" + this._exp + ')';
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        Type typeCheck = this._exp.typeCheck(symbolTable);
        if (typeCheck instanceof ReferenceType) {
            Expression expression = this._exp;
            Type type = Type.Real;
            this._exp = new CastExpr(expression, type);
            typeCheck = type;
        }
        if (typeCheck instanceof ResultTreeType) {
            this._exp = new CastExpr(this._exp, Type.Boolean);
            this._exp = new CastExpr(this._exp, Type.Real);
            typeCheck = this._exp.typeCheck(symbolTable);
        }
        if (typeCheck instanceof NumberType) {
            if (!(typeCheck instanceof IntType)) {
                this._exp = new CastExpr(this._exp, Type.Int);
            }
            boolean z = false;
            if (this._canOptimize) {
                this._nthPositionFilter = !this._exp.hasLastCall() && !this._exp.hasPositionCall();
                if (this._nthPositionFilter) {
                    SyntaxTreeNode parent = getParent();
                    if ((parent instanceof Step) && (parent.getParent() instanceof AbsoluteLocationPath)) {
                        z = true;
                    }
                    this._nthDescendant = z;
                    Type type2 = Type.NodeSet;
                    this._type = type2;
                    return type2;
                }
            }
            this._nthDescendant = false;
            this._nthPositionFilter = false;
            PositionCall positionCall = new PositionCall(getParser().getQNameIgnoreDefaultNs(Keywords.FUNC_POSITION_STRING));
            positionCall.setParser(getParser());
            positionCall.setParent(this);
            this._exp = new EqualityExpr(0, positionCall, this._exp);
            if (this._exp.typeCheck(symbolTable) != Type.Boolean) {
                this._exp = new CastExpr(this._exp, Type.Boolean);
            }
            Type type3 = Type.Boolean;
            this._type = type3;
            return type3;
        }
        if (!(typeCheck instanceof BooleanType)) {
            this._exp = new CastExpr(this._exp, Type.Boolean);
        }
        Type type4 = Type.Boolean;
        this._type = type4;
        return type4;
    }

    private void compileFilter(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        this._className = getXSLTC().getHelperClassName();
        FilterGenerator filterGenerator = new FilterGenerator(this._className, Constants.OBJECT_CLASS, toString(), 33, new String[]{Constants.CURRENT_NODE_LIST_FILTER}, classGenerator.getStylesheet());
        ConstantPoolGen constantPool = filterGenerator.getConstantPool();
        ArrayList arrayList = this._closureVars;
        int size = arrayList == null ? 0 : arrayList.size();
        for (int i = 0; i < size; i++) {
            VariableBase variable = ((VariableRefBase) this._closureVars.get(i)).getVariable();
            filterGenerator.addField(new Field(1, constantPool.addUtf8(variable.getEscapedName()), constantPool.addUtf8(variable.getType().toSignature()), null, constantPool.getConstantPool()));
        }
        InstructionList instructionList = new InstructionList();
        TestGenerator testGenerator = new TestGenerator(17, ohos.com.sun.org.apache.bcel.internal.generic.Type.BOOLEAN, new ohos.com.sun.org.apache.bcel.internal.generic.Type[]{ohos.com.sun.org.apache.bcel.internal.generic.Type.INT, ohos.com.sun.org.apache.bcel.internal.generic.Type.INT, ohos.com.sun.org.apache.bcel.internal.generic.Type.INT, ohos.com.sun.org.apache.bcel.internal.generic.Type.INT, Util.getJCRefType("Lohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;"), Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;")}, new String[]{"node", Keywords.FUNC_POSITION_STRING, Keywords.FUNC_LAST_STRING, Keywords.FUNC_CURRENT_STRING, "translet", Constants.ITERATOR_PNAME}, Constants.ATTRNAME_TEST, this._className, instructionList, constantPool);
        LocalVariableGen addLocalVariable = testGenerator.addLocalVariable(Constants.DOCUMENT_PNAME, Util.getJCRefType(Constants.DOM_INTF_SIG), null, null);
        String className = classGenerator.getClassName();
        instructionList.append(filterGenerator.loadTranslet());
        instructionList.append(new CHECKCAST(constantPool.addClass(className)));
        instructionList.append(new GETFIELD(constantPool.addFieldref(className, Constants.DOM_FIELD, Constants.DOM_INTF_SIG)));
        addLocalVariable.setStart(instructionList.append(new ASTORE(addLocalVariable.getIndex())));
        testGenerator.setDomIndex(addLocalVariable.getIndex());
        this._exp.translate(filterGenerator, testGenerator);
        instructionList.append(IRETURN);
        filterGenerator.addEmptyConstructor(1);
        filterGenerator.addMethod(testGenerator);
        getXSLTC().dumpClass(filterGenerator.getJavaClass());
    }

    public boolean isBooleanTest() {
        return this._exp instanceof BooleanExpr;
    }

    public boolean isNodeValueTest() {
        if (!this._canOptimize || getStep() == null || getCompareValue() == null) {
            return false;
        }
        return true;
    }

    public Step getStep() {
        Step step = this._step;
        if (step != null) {
            return step;
        }
        Expression expression = this._exp;
        if (expression == null) {
            return null;
        }
        if (expression instanceof EqualityExpr) {
            EqualityExpr equalityExpr = (EqualityExpr) expression;
            Expression left = equalityExpr.getLeft();
            Expression right = equalityExpr.getRight();
            if (left instanceof CastExpr) {
                left = ((CastExpr) left).getExpr();
            }
            if (left instanceof Step) {
                this._step = (Step) left;
            }
            if (right instanceof CastExpr) {
                right = ((CastExpr) right).getExpr();
            }
            if (right instanceof Step) {
                this._step = (Step) right;
            }
        }
        return this._step;
    }

    public Expression getCompareValue() {
        Expression expression = this._value;
        if (expression != null) {
            return expression;
        }
        Expression expression2 = this._exp;
        if (expression2 != null && (expression2 instanceof EqualityExpr)) {
            EqualityExpr equalityExpr = (EqualityExpr) expression2;
            Expression left = equalityExpr.getLeft();
            Expression right = equalityExpr.getRight();
            if (left instanceof LiteralExpr) {
                this._value = left;
                return this._value;
            } else if ((left instanceof VariableRefBase) && left.getType() == Type.String) {
                this._value = left;
                return this._value;
            } else if (right instanceof LiteralExpr) {
                this._value = right;
                return this._value;
            } else if ((right instanceof VariableRefBase) && right.getType() == Type.String) {
                this._value = right;
                return this._value;
            }
        }
        return null;
    }

    public void translateFilter(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        compileFilter(classGenerator, methodGenerator);
        instructionList.append(new NEW(constantPool.addClass(this._className)));
        instructionList.append(DUP);
        instructionList.append(new INVOKESPECIAL(constantPool.addMethodref(this._className, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "()V")));
        ArrayList arrayList = this._closureVars;
        int size = arrayList == null ? 0 : arrayList.size();
        for (int i = 0; i < size; i++) {
            VariableBase variable = ((VariableRefBase) this._closureVars.get(i)).getVariable();
            Type type = variable.getType();
            instructionList.append(DUP);
            Closure closure = this._parentClosure;
            while (closure != null && !closure.inInnerClass()) {
                closure = closure.getParentClosure();
            }
            if (closure != null) {
                instructionList.append(ALOAD_0);
                instructionList.append(new GETFIELD(constantPool.addFieldref(closure.getInnerClassName(), variable.getEscapedName(), type.toSignature())));
            } else {
                instructionList.append(variable.loadInstruction());
            }
            instructionList.append(new PUTFIELD(constantPool.addFieldref(this._className, variable.getEscapedName(), type.toSignature())));
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (this._nthPositionFilter || this._nthDescendant) {
            this._exp.translate(classGenerator, methodGenerator);
        } else if (!isNodeValueTest() || !(getParent() instanceof Step)) {
            translateFilter(classGenerator, methodGenerator);
        } else {
            this._value.translate(classGenerator, methodGenerator);
            instructionList.append(new CHECKCAST(constantPool.addClass("java.lang.String")));
            instructionList.append(new PUSH(constantPool, ((EqualityExpr) this._exp).getOp()));
        }
    }
}
