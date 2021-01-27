package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Stylesheet;

public final class NodeCounterGenerator extends ClassGenerator {
    private Instruction _aloadTranslet;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator
    public boolean isExternal() {
        return true;
    }

    public NodeCounterGenerator(String str, String str2, String str3, int i, String[] strArr, Stylesheet stylesheet) {
        super(str, str2, str3, i, strArr, stylesheet);
    }

    public void setTransletIndex(int i) {
        this._aloadTranslet = new ALOAD(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator
    public Instruction loadTranslet() {
        return this._aloadTranslet;
    }
}
