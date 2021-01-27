package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.com.sun.org.apache.xpath.internal.objects.XString;
import ohos.javax.xml.transform.TransformerException;

public class FuncQname extends FunctionDef1Arg {
    static final long serialVersionUID = -1532307875532617380L;

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        int arg0AsNode = getArg0AsNode(xPathContext);
        if (-1 == arg0AsNode) {
            return XString.EMPTYSTRING;
        }
        String nodeNameX = xPathContext.getDTM(arg0AsNode).getNodeNameX(arg0AsNode);
        return nodeNameX == null ? XString.EMPTYSTRING : new XString(nodeNameX);
    }
}
