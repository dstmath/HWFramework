package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ISTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;

public final class MatchGenerator extends MethodGenerator {
    private static int CURRENT_INDEX = 1;
    private Instruction _aloadDom;
    private final Instruction _iloadCurrent = new ILOAD(CURRENT_INDEX);
    private final Instruction _istoreCurrent = new ISTORE(CURRENT_INDEX);
    private int _iteratorIndex = -1;

    public int getHandlerIndex() {
        return -1;
    }

    public MatchGenerator(int i, Type type, Type[] typeArr, String[] strArr, String str, String str2, InstructionList instructionList, ConstantPoolGen constantPoolGen) {
        super(i, type, typeArr, strArr, str, str2, instructionList, constantPoolGen);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator
    public Instruction loadCurrentNode() {
        return this._iloadCurrent;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator
    public Instruction storeCurrentNode() {
        return this._istoreCurrent;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator
    public Instruction loadDOM() {
        return this._aloadDom;
    }

    public void setDomIndex(int i) {
        this._aloadDom = new ALOAD(i);
    }

    public int getIteratorIndex() {
        return this._iteratorIndex;
    }

    public void setIteratorIndex(int i) {
        this._iteratorIndex = i;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator
    public int getLocalIndex(String str) {
        if (str.equals(Keywords.FUNC_CURRENT_STRING)) {
            return CURRENT_INDEX;
        }
        return super.getLocalIndex(str);
    }
}
