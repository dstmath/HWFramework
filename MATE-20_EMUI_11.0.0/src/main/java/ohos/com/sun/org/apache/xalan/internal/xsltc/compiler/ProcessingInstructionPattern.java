package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.IFEQ;
import ohos.com.sun.org.apache.bcel.internal.generic.IF_ICMPEQ;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xpath.internal.XPath;

final class ProcessingInstructionPattern extends StepPattern {
    private String _name = null;
    private boolean _typeChecked = false;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.StepPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern
    public boolean isWildcard() {
        return false;
    }

    public ProcessingInstructionPattern(String str) {
        super(3, 7, null);
        this._name = str;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.StepPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern
    public double getDefaultPriority() {
        if (this._name != null) {
            return XPath.MATCH_SCORE_QNAME;
        }
        return -0.5d;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.StepPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        if (this._predicates == null) {
            return "processing-instruction(" + this._name + ")";
        }
        return "processing-instruction(" + this._name + ")" + this._predicates;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.StepPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern
    public void reduceKernelPattern() {
        this._typeChecked = true;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.StepPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        if (hasPredicates()) {
            int size = this._predicates.size();
            for (int i = 0; i < size; i++) {
                ((Predicate) this._predicates.elementAt(i)).typeCheck(symbolTable);
            }
        }
        return Type.NodeSet;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.StepPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getNodeName", "(I)Ljava/lang/String;");
        int addMethodref = constantPool.addMethodref("java.lang.String", "equals", "(Ljava/lang/Object;)Z");
        instructionList.append(methodGenerator.loadCurrentNode());
        instructionList.append(SWAP);
        instructionList.append(methodGenerator.storeCurrentNode());
        if (!this._typeChecked) {
            instructionList.append(methodGenerator.loadCurrentNode());
            int addInterfaceMethodref2 = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getExpandedTypeID", Constants.GET_PARENT_SIG);
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(methodGenerator.loadCurrentNode());
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref2, 2));
            instructionList.append(new PUSH(constantPool, 7));
            this._falseList.add(instructionList.append((BranchInstruction) new IF_ICMPEQ(null)));
        }
        instructionList.append(new PUSH(constantPool, this._name));
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(methodGenerator.loadCurrentNode());
        instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
        instructionList.append(new INVOKEVIRTUAL(addMethodref));
        this._falseList.add(instructionList.append((BranchInstruction) new IFEQ(null)));
        if (hasPredicates()) {
            int size = this._predicates.size();
            for (int i = 0; i < size; i++) {
                Expression expr = ((Predicate) this._predicates.elementAt(i)).getExpr();
                expr.translateDesynthesized(classGenerator, methodGenerator);
                this._trueList.append(expr._trueList);
                this._falseList.append(expr._falseList);
            }
        }
        backPatchTrueList(instructionList.append(methodGenerator.storeCurrentNode()));
        BranchHandle append = instructionList.append((BranchInstruction) new GOTO(null));
        backPatchFalseList(instructionList.append(methodGenerator.storeCurrentNode()));
        this._falseList.add(instructionList.append((BranchInstruction) new GOTO(null)));
        append.setTarget(instructionList.append(NOP));
    }
}
