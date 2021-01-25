package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Stylesheet;

public final class NodeSortRecordFactGenerator extends ClassGenerator {
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator
    public boolean isExternal() {
        return true;
    }

    public NodeSortRecordFactGenerator(String str, String str2, String str3, int i, String[] strArr, Stylesheet stylesheet) {
        super(str, str2, str3, i, strArr, stylesheet);
    }
}
