package ohos.com.sun.org.apache.xpath.internal.compiler;

import ohos.com.sun.org.apache.xalan.internal.utils.ConfigurationError;
import ohos.com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xpath.internal.functions.Function;
import ohos.javax.xml.transform.TransformerException;

public class FuncLoader {
    private int m_funcID;
    private String m_funcName;

    public String getName() {
        return this.m_funcName;
    }

    public FuncLoader(String str, int i) {
        this.m_funcID = i;
        this.m_funcName = str;
    }

    /* access modifiers changed from: package-private */
    public Function getFunction() throws TransformerException {
        try {
            String str = this.m_funcName;
            if (str.indexOf(".") < 0) {
                str = "com.sun.org.apache.xpath.internal.functions." + str;
            }
            String substring = str.substring(0, str.lastIndexOf(46));
            if (!substring.equals("com.sun.org.apache.xalan.internal.templates")) {
                if (!substring.equals("com.sun.org.apache.xpath.internal.functions")) {
                    throw new TransformerException("Application can't install his own xpath function.");
                }
            }
            return (Function) ObjectFactory.newInstance(str, true);
        } catch (ConfigurationError e) {
            throw new TransformerException(e.getException());
        }
    }
}
