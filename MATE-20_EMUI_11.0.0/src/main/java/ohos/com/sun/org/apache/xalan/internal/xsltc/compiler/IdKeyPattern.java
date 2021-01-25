package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.IFNE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

/* access modifiers changed from: package-private */
public abstract class IdKeyPattern extends LocationPathPattern {
    private String _index = null;
    protected RelativePathPattern _left = null;
    private String _value = null;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern
    public StepPattern getKernelPattern() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern
    public boolean isWildcard() {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern
    public void reduceKernelPattern() {
    }

    public IdKeyPattern(String str, String str2) {
        this._index = str;
        this._value = str2;
    }

    public String getIndexName() {
        return this._index;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        return Type.NodeSet;
    }

    public void setLeft(RelativePathPattern relativePathPattern) {
        this._left = relativePathPattern;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        return "id/keyPattern(" + this._index + ", " + this._value + ')';
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.LocationPathPattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int addMethodref = constantPool.addMethodref(Constants.TRANSLET_CLASS, "getKeyIndex", "(Ljava/lang/String;)Lohos.com.sun.org.apache.xalan.internal.xsltc.dom.KeyIndex;");
        int addMethodref2 = constantPool.addMethodref(Constants.KEY_INDEX_CLASS, "containsID", "(ILjava/lang/Object;)I");
        int addMethodref3 = constantPool.addMethodref(Constants.KEY_INDEX_CLASS, "containsKey", "(ILjava/lang/Object;)I");
        constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getNodeIdent", Constants.GET_PARENT_SIG);
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new PUSH(constantPool, this._index));
        instructionList.append(new INVOKEVIRTUAL(addMethodref));
        instructionList.append(SWAP);
        instructionList.append(new PUSH(constantPool, this._value));
        if (this instanceof IdPattern) {
            instructionList.append(new INVOKEVIRTUAL(addMethodref2));
        } else {
            instructionList.append(new INVOKEVIRTUAL(addMethodref3));
        }
        this._trueList.add(instructionList.append((BranchInstruction) new IFNE(null)));
        this._falseList.add(instructionList.append((BranchInstruction) new GOTO(null)));
    }
}
