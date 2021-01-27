package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.com.sun.org.apache.xpath.internal.objects.XString;
import ohos.javax.xml.transform.TransformerException;

public class FuncDoclocation extends FunctionDef1Arg {
    static final long serialVersionUID = 7469213946343568769L;

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0024  */
    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        String str;
        int arg0AsNode = getArg0AsNode(xPathContext);
        if (-1 != arg0AsNode) {
            DTM dtm = xPathContext.getDTM(arg0AsNode);
            if (11 == dtm.getNodeType(arg0AsNode)) {
                arg0AsNode = dtm.getFirstChild(arg0AsNode);
            }
            if (-1 != arg0AsNode) {
                str = dtm.getDocumentBaseURI();
                if (str == null) {
                    str = "";
                }
                return new XString(str);
            }
        }
        str = null;
        if (str == null) {
        }
        return new XString(str);
    }
}
