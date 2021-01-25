package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.com.sun.org.apache.xpath.internal.objects.XString;
import ohos.javax.xml.transform.TransformerException;

public class FuncNamespace extends FunctionDef1Arg {
    static final long serialVersionUID = -4695674566722321237L;

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        String str;
        int arg0AsNode = getArg0AsNode(xPathContext);
        if (arg0AsNode == -1) {
            return XString.EMPTYSTRING;
        }
        DTM dtm = xPathContext.getDTM(arg0AsNode);
        short nodeType = dtm.getNodeType(arg0AsNode);
        if (nodeType == 1) {
            str = dtm.getNamespaceURI(arg0AsNode);
        } else if (nodeType != 2) {
            return XString.EMPTYSTRING;
        } else {
            String nodeName = dtm.getNodeName(arg0AsNode);
            if (nodeName.startsWith("xmlns:") || nodeName.equals("xmlns")) {
                return XString.EMPTYSTRING;
            }
            str = dtm.getNamespaceURI(arg0AsNode);
        }
        if (str == null) {
            return XString.EMPTYSTRING;
        }
        return new XString(str);
    }
}
