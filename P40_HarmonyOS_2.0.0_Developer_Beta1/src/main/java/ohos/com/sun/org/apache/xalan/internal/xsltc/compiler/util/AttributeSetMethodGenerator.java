package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;

public final class AttributeSetMethodGenerator extends MethodGenerator {
    protected static final int CURRENT_INDEX = 4;
    private static final int PARAM_START_INDEX = 5;
    private static final String[] argNames = new String[4];
    private static final Type[] argTypes = new Type[4];

    static {
        argTypes[0] = Util.getJCRefType(Constants.DOM_INTF_SIG);
        argTypes[1] = Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;");
        argTypes[2] = Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;");
        argTypes[3] = Type.INT;
        String[] strArr = argNames;
        strArr[0] = Constants.DOCUMENT_PNAME;
        strArr[1] = Constants.ITERATOR_PNAME;
        strArr[2] = Constants.TRANSLET_OUTPUT_PNAME;
        strArr[3] = "node";
    }

    public AttributeSetMethodGenerator(String str, ClassGenerator classGenerator) {
        super(2, Type.VOID, argTypes, argNames, str, classGenerator.getClassName(), new InstructionList(), classGenerator.getConstantPool());
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
