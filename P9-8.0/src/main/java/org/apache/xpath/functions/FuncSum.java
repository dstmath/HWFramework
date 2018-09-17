package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.utils.XMLString;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;

public class FuncSum extends FunctionOneArg {
    static final long serialVersionUID = -2719049259574677519L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        DTMIterator nodes = this.m_arg0.asIterator(xctxt, xctxt.getCurrentNode());
        double sum = XPath.MATCH_SCORE_QNAME;
        while (true) {
            int pos = nodes.nextNode();
            if (-1 != pos) {
                XMLString s = nodes.getDTM(pos).getStringValue(pos);
                if (s != null) {
                    sum += s.toDouble();
                }
            } else {
                nodes.detach();
                return new XNumber(sum);
            }
        }
    }
}
