package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;

public class FuncRound extends FunctionOneArg {
    static final long serialVersionUID = -7970583902573826611L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        double val = this.m_arg0.execute(xctxt).num();
        if (val >= -0.5d && val < XPath.MATCH_SCORE_QNAME) {
            return new XNumber(-0.0d);
        }
        if (val == XPath.MATCH_SCORE_QNAME) {
            return new XNumber(val);
        }
        return new XNumber(Math.floor(0.5d + val));
    }
}
