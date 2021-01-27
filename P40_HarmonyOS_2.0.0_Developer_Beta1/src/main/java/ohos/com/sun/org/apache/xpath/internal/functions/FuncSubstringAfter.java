package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.com.sun.org.apache.xpath.internal.objects.XString;
import ohos.javax.xml.transform.TransformerException;

public class FuncSubstringAfter extends Function2Args {
    static final long serialVersionUID = -8119731889862512194L;

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        XMLString xstr = this.m_arg0.execute(xPathContext).xstr();
        XMLString xstr2 = this.m_arg1.execute(xPathContext).xstr();
        int indexOf = xstr.indexOf(xstr2);
        if (-1 == indexOf) {
            return XString.EMPTYSTRING;
        }
        return (XString) xstr.substring(indexOf + xstr2.length());
    }
}
