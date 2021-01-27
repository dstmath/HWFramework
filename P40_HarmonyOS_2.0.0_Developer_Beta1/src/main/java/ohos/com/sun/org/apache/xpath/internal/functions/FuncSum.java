package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XNumber;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class FuncSum extends FunctionOneArg {
    static final long serialVersionUID = -2719049259574677519L;

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        DTMIterator asIterator = this.m_arg0.asIterator(xPathContext, xPathContext.getCurrentNode());
        double d = XPath.MATCH_SCORE_QNAME;
        while (true) {
            int nextNode = asIterator.nextNode();
            if (-1 != nextNode) {
                XMLString stringValue = asIterator.getDTM(nextNode).getStringValue(nextNode);
                if (stringValue != null) {
                    d += stringValue.toDouble();
                }
            } else {
                asIterator.detach();
                return new XNumber(d);
            }
        }
    }
}
