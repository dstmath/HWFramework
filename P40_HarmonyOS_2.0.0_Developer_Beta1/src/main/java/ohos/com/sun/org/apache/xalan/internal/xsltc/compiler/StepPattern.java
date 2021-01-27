package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.classfile.Field;
import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GETFIELD;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO_W;
import ohos.com.sun.org.apache.bcel.internal.generic.IFLT;
import ohos.com.sun.org.apache.bcel.internal.generic.IFNE;
import ohos.com.sun.org.apache.bcel.internal.generic.IFNONNULL;
import ohos.com.sun.org.apache.bcel.internal.generic.IF_ICMPEQ;
import ohos.com.sun.org.apache.bcel.internal.generic.IF_ICMPLT;
import ohos.com.sun.org.apache.bcel.internal.generic.IF_ICMPNE;
import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.ISTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.bcel.internal.generic.PUTFIELD;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xml.internal.dtm.Axis;
import ohos.com.sun.org.apache.xpath.internal.XPath;

/* access modifiers changed from: package-private */
public class StepPattern extends RelativePathPattern {
    private static final int GENERAL_CONTEXT = 2;
    private static final int NO_CONTEXT = 0;
    private static final int SIMPLE_CONTEXT = 1;
    protected final int _axis;
    private int _contextCase;
    private boolean _isEpsilon = false;
    protected final int _nodeType;
    protected Vector _predicates;
    private double _priority = Double.MAX_VALUE;
    private Step _step = null;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern
    public StepPattern getKernelPattern() {
        return this;
    }

    public StepPattern(int i, int i2, Vector vector) {
        this._axis = i;
        this._nodeType = i2;
        this._predicates = vector;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void setParser(Parser parser) {
        super.setParser(parser);
        Vector vector = this._predicates;
        if (vector != null) {
            int size = vector.size();
            for (int i = 0; i < size; i++) {
                Predicate predicate = (Predicate) this._predicates.elementAt(i);
                predicate.setParser(parser);
                predicate.setParent(this);
            }
        }
    }

    public int getNodeType() {
        return this._nodeType;
    }

    public void setPriority(double d) {
        this._priority = d;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern
    public boolean isWildcard() {
        return this._isEpsilon && !hasPredicates();
    }

    public StepPattern setPredicates(Vector vector) {
        this._predicates = vector;
        return this;
    }

    /* access modifiers changed from: protected */
    public boolean hasPredicates() {
        Vector vector = this._predicates;
        return vector != null && vector.size() > 0;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern
    public double getDefaultPriority() {
        double d = this._priority;
        if (d != Double.MAX_VALUE) {
            return d;
        }
        if (hasPredicates()) {
            return 0.5d;
        }
        int i = this._nodeType;
        if (i == -1) {
            return -0.5d;
        }
        if (i == 0) {
            return XPath.MATCH_SCORE_QNAME;
        }
        if (i >= 14) {
            return XPath.MATCH_SCORE_QNAME;
        }
        return -0.5d;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern
    public int getAxis() {
        return this._axis;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern
    public void reduceKernelPattern() {
        this._isEpsilon = true;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        String str;
        StringBuffer stringBuffer = new StringBuffer("stepPattern(\"");
        stringBuffer.append(Axis.getNames(this._axis));
        stringBuffer.append("\", ");
        if (this._isEpsilon) {
            str = "epsilon{" + Integer.toString(this._nodeType) + "}";
        } else {
            str = Integer.toString(this._nodeType);
        }
        stringBuffer.append(str);
        if (this._predicates != null) {
            stringBuffer.append(", ");
            stringBuffer.append(this._predicates.toString());
        }
        stringBuffer.append(')');
        return stringBuffer.toString();
    }

    private int analyzeCases() {
        int size = this._predicates.size();
        boolean z = true;
        for (int i = 0; i < size && z; i++) {
            Predicate predicate = (Predicate) this._predicates.elementAt(i);
            if (predicate.isNthPositionFilter() || predicate.hasPositionCall() || predicate.hasLastCall()) {
                z = false;
            }
        }
        if (z) {
            return 0;
        }
        if (size == 1) {
            return 1;
        }
        return 2;
    }

    private String getNextFieldName() {
        return "__step_pattern_iter_" + getXSLTC().nextStepPatternSerial();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        Step step;
        if (hasPredicates()) {
            int size = this._predicates.size();
            for (int i = 0; i < size; i++) {
                ((Predicate) this._predicates.elementAt(i)).typeCheck(symbolTable);
            }
            this._contextCase = analyzeCases();
            int i2 = this._contextCase;
            Step step2 = null;
            if (i2 == 1) {
                if (((Predicate) this._predicates.elementAt(0)).isNthPositionFilter()) {
                    this._contextCase = 2;
                    step = new Step(this._axis, this._nodeType, this._predicates);
                } else {
                    step = new Step(this._axis, this._nodeType, null);
                }
                step2 = step;
            } else if (i2 == 2) {
                int size2 = this._predicates.size();
                for (int i3 = 0; i3 < size2; i3++) {
                    ((Predicate) this._predicates.elementAt(i3)).dontOptimize();
                }
                step2 = new Step(this._axis, this._nodeType, this._predicates);
            }
            if (step2 != null) {
                step2.setParser(getParser());
                step2.typeCheck(symbolTable);
                this._step = step2;
            }
        }
        return this._axis == 3 ? Type.Element : Type.Attribute;
    }

    private void translateKernel(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int i = this._nodeType;
        if (i == 1) {
            int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "isElement", "(I)Z");
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(SWAP);
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
            BranchHandle append = instructionList.append((BranchInstruction) new IFNE(null));
            this._falseList.add(instructionList.append((BranchInstruction) new GOTO_W(null)));
            append.setTarget(instructionList.append(NOP));
        } else if (i == 2) {
            int addInterfaceMethodref2 = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "isAttribute", "(I)Z");
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(SWAP);
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref2, 2));
            BranchHandle append2 = instructionList.append((BranchInstruction) new IFNE(null));
            this._falseList.add(instructionList.append((BranchInstruction) new GOTO_W(null)));
            append2.setTarget(instructionList.append(NOP));
        } else {
            int addInterfaceMethodref3 = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getExpandedTypeID", Constants.GET_PARENT_SIG);
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(SWAP);
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref3, 2));
            instructionList.append(new PUSH(constantPool, this._nodeType));
            BranchHandle append3 = instructionList.append((BranchInstruction) new IF_ICMPEQ(null));
            this._falseList.add(instructionList.append((BranchInstruction) new GOTO_W(null)));
            append3.setTarget(instructionList.append(NOP));
        }
    }

    private void translateNoContext(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(methodGenerator.loadCurrentNode());
        instructionList.append(SWAP);
        instructionList.append(methodGenerator.storeCurrentNode());
        if (!this._isEpsilon) {
            instructionList.append(methodGenerator.loadCurrentNode());
            translateKernel(classGenerator, methodGenerator);
        }
        int size = this._predicates.size();
        for (int i = 0; i < size; i++) {
            Expression expr = ((Predicate) this._predicates.elementAt(i)).getExpr();
            expr.translateDesynthesized(classGenerator, methodGenerator);
            this._trueList.append(expr._trueList);
            this._falseList.append(expr._falseList);
        }
        backPatchTrueList(instructionList.append(methodGenerator.storeCurrentNode()));
        BranchHandle append = instructionList.append((BranchInstruction) new GOTO(null));
        backPatchFalseList(instructionList.append(methodGenerator.storeCurrentNode()));
        this._falseList.add(instructionList.append((BranchInstruction) new GOTO(null)));
        append.setTarget(instructionList.append(NOP));
    }

    private void translateSimpleContext(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        LocalVariableGen addLocalVariable = methodGenerator.addLocalVariable("step_pattern_tmp1", Util.getJCRefType("I"), null, null);
        addLocalVariable.setStart(instructionList.append(new ISTORE(addLocalVariable.getIndex())));
        if (!this._isEpsilon) {
            instructionList.append(new ILOAD(addLocalVariable.getIndex()));
            translateKernel(classGenerator, methodGenerator);
        }
        instructionList.append(methodGenerator.loadCurrentNode());
        instructionList.append(methodGenerator.loadIterator());
        int addMethodref = constantPool.addMethodref(Constants.MATCHING_ITERATOR, Constants.CONSTRUCTOR_NAME, "(ILohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;)V");
        this._step.translate(classGenerator, methodGenerator);
        LocalVariableGen addLocalVariable2 = methodGenerator.addLocalVariable("step_pattern_tmp2", Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), null, null);
        addLocalVariable2.setStart(instructionList.append(new ASTORE(addLocalVariable2.getIndex())));
        instructionList.append(new NEW(constantPool.addClass(Constants.MATCHING_ITERATOR)));
        instructionList.append(DUP);
        instructionList.append(new ILOAD(addLocalVariable.getIndex()));
        addLocalVariable2.setEnd(instructionList.append(new ALOAD(addLocalVariable2.getIndex())));
        instructionList.append(new INVOKESPECIAL(addMethodref));
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(new ILOAD(addLocalVariable.getIndex()));
        instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.GET_PARENT, Constants.GET_PARENT_SIG), 2));
        instructionList.append(methodGenerator.setStartNode());
        instructionList.append(methodGenerator.storeIterator());
        addLocalVariable.setEnd(instructionList.append(new ILOAD(addLocalVariable.getIndex())));
        instructionList.append(methodGenerator.storeCurrentNode());
        Expression expr = ((Predicate) this._predicates.elementAt(0)).getExpr();
        expr.translateDesynthesized(classGenerator, methodGenerator);
        InstructionHandle append = instructionList.append(methodGenerator.storeIterator());
        instructionList.append(methodGenerator.storeCurrentNode());
        expr.backPatchTrueList(append);
        BranchHandle append2 = instructionList.append((BranchInstruction) new GOTO(null));
        InstructionHandle append3 = instructionList.append(methodGenerator.storeIterator());
        instructionList.append(methodGenerator.storeCurrentNode());
        expr.backPatchFalseList(append3);
        this._falseList.add(instructionList.append((BranchInstruction) new GOTO(null)));
        append2.setTarget(instructionList.append(NOP));
    }

    private void translateGeneralContext(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        BranchHandle branchHandle;
        int i;
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        String nextFieldName = getNextFieldName();
        LocalVariableGen addLocalVariable = methodGenerator.addLocalVariable("step_pattern_tmp1", Util.getJCRefType("I"), null, null);
        addLocalVariable.setStart(instructionList.append(new ISTORE(addLocalVariable.getIndex())));
        LocalVariableGen addLocalVariable2 = methodGenerator.addLocalVariable("step_pattern_tmp2", Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), null, null);
        if (!classGenerator.isExternal()) {
            classGenerator.addField(new Field(2, constantPool.addUtf8(nextFieldName), constantPool.addUtf8("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), null, constantPool.getConstantPool()));
            i = constantPool.addFieldref(classGenerator.getClassName(), nextFieldName, "Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;");
            instructionList.append(classGenerator.loadTranslet());
            instructionList.append(new GETFIELD(i));
            instructionList.append(DUP);
            addLocalVariable2.setStart(instructionList.append(new ASTORE(addLocalVariable2.getIndex())));
            branchHandle = instructionList.append((BranchInstruction) new IFNONNULL(null));
            instructionList.append(classGenerator.loadTranslet());
        } else {
            i = 0;
            branchHandle = null;
        }
        this._step.translate(classGenerator, methodGenerator);
        InstructionHandle append = instructionList.append(new ASTORE(addLocalVariable2.getIndex()));
        if (!classGenerator.isExternal()) {
            instructionList.append(new ALOAD(addLocalVariable2.getIndex()));
            instructionList.append(new PUTFIELD(i));
            branchHandle.setTarget(instructionList.append(NOP));
        } else {
            addLocalVariable2.setStart(append);
        }
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(new ILOAD(addLocalVariable.getIndex()));
        instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.GET_PARENT, Constants.GET_PARENT_SIG), 2));
        instructionList.append(new ALOAD(addLocalVariable2.getIndex()));
        instructionList.append(SWAP);
        instructionList.append(methodGenerator.setStartNode());
        LocalVariableGen addLocalVariable3 = methodGenerator.addLocalVariable("step_pattern_tmp3", Util.getJCRefType("I"), null, null);
        BranchHandle append2 = instructionList.append((BranchInstruction) new GOTO(null));
        InstructionHandle append3 = instructionList.append(new ALOAD(addLocalVariable2.getIndex()));
        addLocalVariable3.setStart(append3);
        InstructionHandle append4 = instructionList.append(methodGenerator.nextNode());
        instructionList.append(DUP);
        instructionList.append(new ISTORE(addLocalVariable3.getIndex()));
        this._falseList.add(instructionList.append((BranchInstruction) new IFLT(null)));
        instructionList.append(new ILOAD(addLocalVariable3.getIndex()));
        instructionList.append(new ILOAD(addLocalVariable.getIndex()));
        addLocalVariable2.setEnd(instructionList.append((BranchInstruction) new IF_ICMPLT(append3)));
        addLocalVariable3.setEnd(instructionList.append(new ILOAD(addLocalVariable3.getIndex())));
        addLocalVariable.setEnd(instructionList.append(new ILOAD(addLocalVariable.getIndex())));
        this._falseList.add(instructionList.append((BranchInstruction) new IF_ICMPNE(null)));
        append2.setTarget(append4);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (hasPredicates()) {
            int i = this._contextCase;
            if (i == 0) {
                translateNoContext(classGenerator, methodGenerator);
            } else if (i != 1) {
                translateGeneralContext(classGenerator, methodGenerator);
            } else {
                translateSimpleContext(classGenerator, methodGenerator);
            }
        } else if (isWildcard()) {
            instructionList.append(POP);
        } else {
            translateKernel(classGenerator, methodGenerator);
        }
    }
}
