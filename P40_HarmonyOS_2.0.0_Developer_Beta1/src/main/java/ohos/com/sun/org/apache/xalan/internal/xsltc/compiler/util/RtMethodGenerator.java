package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;

public final class RtMethodGenerator extends MethodGenerator {
    private static final int HANDLER_INDEX = 2;
    private final Instruction _aloadHandler = new ALOAD(2);
    private final Instruction _astoreHandler = new ASTORE(2);

    public int getIteratorIndex() {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator
    public int getLocalIndex(String str) {
        return -1;
    }

    public RtMethodGenerator(int i, Type type, Type[] typeArr, String[] strArr, String str, String str2, InstructionList instructionList, ConstantPoolGen constantPoolGen) {
        super(i, type, typeArr, strArr, str, str2, instructionList, constantPoolGen);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator
    public final Instruction storeHandler() {
        return this._astoreHandler;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator
    public final Instruction loadHandler() {
        return this._aloadHandler;
    }
}
