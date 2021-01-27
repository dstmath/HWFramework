package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.com.sun.org.apache.xpath.internal.objects.XString;
import ohos.javax.xml.transform.TransformerException;

public class FuncUnparsedEntityURI extends FunctionOneArg {
    static final long serialVersionUID = 845309759097448178L;

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        String str = this.m_arg0.execute(xPathContext).str();
        DTM dtm = xPathContext.getDTM(xPathContext.getCurrentNode());
        dtm.getDocument();
        return new XString(dtm.getUnparsedEntityURI(str));
    }
}
