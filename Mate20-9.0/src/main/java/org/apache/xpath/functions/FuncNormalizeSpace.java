package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class FuncNormalizeSpace extends FunctionDef1Arg {
    static final long serialVersionUID = -3377956872032190880L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        return (XString) getArg0AsString(xctxt).fixWhiteSpace(true, true, false);
    }

    public void executeCharsToContentHandler(XPathContext xctxt, ContentHandler handler) throws TransformerException, SAXException {
        if (Arg0IsNodesetExpr()) {
            int node = getArg0AsNode(xctxt);
            if (-1 != node) {
                xctxt.getDTM(node).dispatchCharactersEvents(node, handler, true);
                return;
            }
            return;
        }
        execute(xctxt).dispatchCharactersEvents(handler);
    }
}
