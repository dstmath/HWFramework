package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ISTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;

public final class TestGenerator extends MethodGenerator {
    private static int CONTEXT_NODE_INDEX = 1;
    private static int CURRENT_NODE_INDEX = 4;
    private static int ITERATOR_INDEX = 6;
    private Instruction _aloadDom;
    private final Instruction _aloadIterator = new ALOAD(ITERATOR_INDEX);
    private final Instruction _astoreIterator = new ASTORE(ITERATOR_INDEX);
    private final Instruction _iloadContext = new ILOAD(CONTEXT_NODE_INDEX);
    private final Instruction _iloadCurrent = new ILOAD(CURRENT_NODE_INDEX);
    private final Instruction _istoreContext = new ILOAD(CONTEXT_NODE_INDEX);
    private final Instruction _istoreCurrent = new ISTORE(CURRENT_NODE_INDEX);

    public int getHandlerIndex() {
        return -1;
    }

    public TestGenerator(int i, Type type, Type[] typeArr, String[] strArr, String str, String str2, InstructionList instructionList, ConstantPoolGen constantPoolGen) {
        super(i, type, typeArr, strArr, str, str2, instructionList, constantPoolGen);
    }

    public int getIteratorIndex() {
        return ITERATOR_INDEX;
    }

    public void setDomIndex(int i) {
        this._aloadDom = new ALOAD(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator
    public Instruction loadDOM() {
        return this._aloadDom;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator
    public Instruction loadCurrentNode() {
        return this._iloadCurrent;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator
    public Instruction loadContextNode() {
        return this._iloadContext;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator
    public Instruction storeContextNode() {
        return this._istoreContext;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator
    public Instruction storeCurrentNode() {
        return this._istoreCurrent;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator
    public Instruction storeIterator() {
        return this._astoreIterator;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator
    public Instruction loadIterator() {
        return this._aloadIterator;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator
    public int getLocalIndex(String str) {
        if (str.equals(Keywords.FUNC_CURRENT_STRING)) {
            return CURRENT_NODE_INDEX;
        }
        return super.getLocalIndex(str);
    }
}
