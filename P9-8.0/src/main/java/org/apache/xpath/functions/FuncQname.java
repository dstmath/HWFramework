package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;

public class FuncQname extends FunctionDef1Arg {
    static final long serialVersionUID = -1532307875532617380L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        int context = getArg0AsNode(xctxt);
        if (-1 == context) {
            return XString.EMPTYSTRING;
        }
        String qname = xctxt.getDTM(context).getNodeNameX(context);
        return qname == null ? XString.EMPTYSTRING : new XString(qname);
    }
}
