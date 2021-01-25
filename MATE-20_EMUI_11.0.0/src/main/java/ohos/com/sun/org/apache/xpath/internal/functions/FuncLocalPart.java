package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.com.sun.org.apache.xpath.internal.objects.XString;
import ohos.javax.xml.transform.TransformerException;

public class FuncLocalPart extends FunctionDef1Arg {
    static final long serialVersionUID = 7591798770325814746L;

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        int arg0AsNode = getArg0AsNode(xPathContext);
        if (-1 == arg0AsNode) {
            return XString.EMPTYSTRING;
        }
        String localName = arg0AsNode != -1 ? xPathContext.getDTM(arg0AsNode).getLocalName(arg0AsNode) : "";
        if (localName.startsWith("#") || localName.equals("xmlns")) {
            return XString.EMPTYSTRING;
        }
        return new XString(localName);
    }
}
