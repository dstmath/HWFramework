package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XBoolean;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class FuncLang extends FunctionOneArg {
    static final long serialVersionUID = -7868705139354872185L;

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        boolean z;
        int attributeNode;
        int length;
        String str = this.m_arg0.execute(xPathContext).str();
        int currentNode = xPathContext.getCurrentNode();
        DTM dtm = xPathContext.getDTM(currentNode);
        while (true) {
            z = false;
            if (-1 == currentNode) {
                break;
            } else if (1 != dtm.getNodeType(currentNode) || -1 == (attributeNode = dtm.getAttributeNode(currentNode, "http://www.w3.org/XML/1998/namespace", "lang"))) {
                currentNode = dtm.getParent(currentNode);
            } else {
                String nodeValue = dtm.getNodeValue(attributeNode);
                if (nodeValue.toLowerCase().startsWith(str.toLowerCase()) && (nodeValue.length() == (length = str.length()) || nodeValue.charAt(length) == '-')) {
                    z = true;
                }
            }
        }
        return z ? XBoolean.S_TRUE : XBoolean.S_FALSE;
    }
}
