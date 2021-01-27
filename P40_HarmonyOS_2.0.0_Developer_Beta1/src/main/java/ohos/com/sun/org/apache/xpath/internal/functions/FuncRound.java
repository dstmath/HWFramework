package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XNumber;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class FuncRound extends FunctionOneArg {
    static final long serialVersionUID = -7970583902573826611L;

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        double num = this.m_arg0.execute(xPathContext).num();
        if (num >= -0.5d && num < XPath.MATCH_SCORE_QNAME) {
            return new XNumber(-0.0d);
        }
        if (num == XPath.MATCH_SCORE_QNAME) {
            return new XNumber(num);
        }
        return new XNumber(Math.floor(num + 0.5d));
    }
}
