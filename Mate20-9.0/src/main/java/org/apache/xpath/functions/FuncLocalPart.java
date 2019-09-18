package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;

public class FuncLocalPart extends FunctionDef1Arg {
    static final long serialVersionUID = 7591798770325814746L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        int context = getArg0AsNode(xctxt);
        if (-1 == context) {
            return XString.EMPTYSTRING;
        }
        String s = context != -1 ? xctxt.getDTM(context).getLocalName(context) : "";
        if (s.startsWith("#") || s.equals("xmlns")) {
            return XString.EMPTYSTRING;
        }
        return new XString(s);
    }
}
