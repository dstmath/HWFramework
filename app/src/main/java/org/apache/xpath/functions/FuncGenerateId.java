package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;

public class FuncGenerateId extends FunctionDef1Arg {
    static final long serialVersionUID = 973544842091724273L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        int which = getArg0AsNode(xctxt);
        if (-1 != which) {
            return new XString("N" + Integer.toHexString(which).toUpperCase());
        }
        return XString.EMPTYSTRING;
    }
}
