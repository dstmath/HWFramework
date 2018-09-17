package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xalan.templates.Constants;
import org.apache.xml.dtm.DTM;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;

public class FuncNamespace extends FunctionDef1Arg {
    static final long serialVersionUID = -4695674566722321237L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        int context = getArg0AsNode(xctxt);
        if (context == -1) {
            return XString.EMPTYSTRING;
        }
        String s;
        XObject xObject;
        DTM dtm = xctxt.getDTM(context);
        int t = dtm.getNodeType(context);
        if (t == 1) {
            s = dtm.getNamespaceURI(context);
        } else if (t != 2) {
            return XString.EMPTYSTRING;
        } else {
            s = dtm.getNodeName(context);
            if (s.startsWith(Constants.ATTRNAME_XMLNS) || s.equals("xmlns")) {
                return XString.EMPTYSTRING;
            }
            s = dtm.getNamespaceURI(context);
        }
        if (s == null) {
            xObject = XString.EMPTYSTRING;
        } else {
            xObject = new XString(s);
        }
        return xObject;
    }
}
