package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;

public final class NamedMethodGenerator extends MethodGenerator {
    protected static final int CURRENT_INDEX = 4;
    private static final int PARAM_START_INDEX = 5;

    public NamedMethodGenerator(int i, Type type, Type[] typeArr, String[] strArr, String str, String str2, InstructionList instructionList, ConstantPoolGen constantPoolGen) {
        super(i, type, typeArr, strArr, str, str2, instructionList, constantPoolGen);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator
    public int getLocalIndex(String str) {
        if (str.equals(Keywords.FUNC_CURRENT_STRING)) {
            return 4;
        }
        return super.getLocalIndex(str);
    }

    public Instruction loadParameter(int i) {
        return new ALOAD(i + 5);
    }

    public Instruction storeParameter(int i) {
        return new ASTORE(i + 5);
    }
}
