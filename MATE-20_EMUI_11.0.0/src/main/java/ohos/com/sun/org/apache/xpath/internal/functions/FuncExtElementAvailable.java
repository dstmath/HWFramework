package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xpath.internal.ExtensionsProvider;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XBoolean;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class FuncExtElementAvailable extends FunctionOneArg {
    static final long serialVersionUID = -472533699257968546L;

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        String str;
        String str2 = this.m_arg0.execute(xPathContext).str();
        int indexOf = str2.indexOf(58);
        if (indexOf < 0) {
            str = "http://www.w3.org/1999/XSL/Transform";
        } else {
            str = xPathContext.getNamespaceContext().getNamespaceForPrefix(str2.substring(0, indexOf));
            if (str == null) {
                return XBoolean.S_FALSE;
            }
            str2 = str2.substring(indexOf + 1);
        }
        if (str.equals("http://www.w3.org/1999/XSL/Transform") || str.equals("http://xml.apache.org/xalan")) {
            return XBoolean.S_FALSE;
        }
        if (((ExtensionsProvider) xPathContext.getOwnerObject()).elementAvailable(str, str2)) {
            return XBoolean.S_TRUE;
        }
        return XBoolean.S_FALSE;
    }
}
