package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.com.sun.org.apache.xpath.internal.objects.XString;
import ohos.javax.xml.transform.TransformerException;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.SAXException;

public class FuncNormalizeSpace extends FunctionDef1Arg {
    static final long serialVersionUID = -3377956872032190880L;

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        return (XString) getArg0AsString(xPathContext).fixWhiteSpace(true, true, false);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public void executeCharsToContentHandler(XPathContext xPathContext, ContentHandler contentHandler) throws TransformerException, SAXException {
        if (Arg0IsNodesetExpr()) {
            int arg0AsNode = getArg0AsNode(xPathContext);
            if (-1 != arg0AsNode) {
                xPathContext.getDTM(arg0AsNode).dispatchCharactersEvents(arg0AsNode, contentHandler, true);
                return;
            }
            return;
        }
        execute(xPathContext).dispatchCharactersEvents(contentHandler);
    }
}
