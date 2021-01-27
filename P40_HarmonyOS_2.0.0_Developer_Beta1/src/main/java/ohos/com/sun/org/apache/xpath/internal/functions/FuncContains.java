package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XBoolean;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class FuncContains extends Function2Args {
    static final long serialVersionUID = 5084753781887919723L;

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        String str = this.m_arg0.execute(xPathContext).str();
        String str2 = this.m_arg1.execute(xPathContext).str();
        if (str.length() == 0 && str2.length() == 0) {
            return XBoolean.S_TRUE;
        }
        return str.indexOf(str2) > -1 ? XBoolean.S_TRUE : XBoolean.S_FALSE;
    }
}
