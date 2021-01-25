package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.com.sun.org.apache.xpath.internal.objects.XString;
import ohos.javax.xml.transform.TransformerException;

public class FuncTranslate extends Function3Args {
    static final long serialVersionUID = -1672834340026116482L;

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        String str = this.m_arg0.execute(xPathContext).str();
        String str2 = this.m_arg1.execute(xPathContext).str();
        String str3 = this.m_arg2.execute(xPathContext).str();
        int length = str.length();
        int length2 = str3.length();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < length; i++) {
            char charAt = str.charAt(i);
            int indexOf = str2.indexOf(charAt);
            if (indexOf < 0) {
                stringBuffer.append(charAt);
            } else if (indexOf < length2) {
                stringBuffer.append(str3.charAt(indexOf));
            }
        }
        return new XString(stringBuffer.toString());
    }
}
