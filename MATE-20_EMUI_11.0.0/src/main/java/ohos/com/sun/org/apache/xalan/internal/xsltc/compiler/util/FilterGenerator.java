package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Stylesheet;

public final class FilterGenerator extends ClassGenerator {
    private static int TRANSLET_INDEX = 5;
    private final Instruction _aloadTranslet = new ALOAD(TRANSLET_INDEX);

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator
    public boolean isExternal() {
        return true;
    }

    public FilterGenerator(String str, String str2, String str3, int i, String[] strArr, Stylesheet stylesheet) {
        super(str, str2, str3, i, strArr, stylesheet);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator
    public final Instruction loadTranslet() {
        return this._aloadTranslet;
    }
}
