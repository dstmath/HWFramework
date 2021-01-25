package ohos.com.sun.org.apache.xpath.internal.operations;

import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XBoolean;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class And extends Operation {
    static final long serialVersionUID = 392330077126534022L;

    @Override // ohos.com.sun.org.apache.xpath.internal.operations.Operation, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        if (this.m_left.execute(xPathContext).bool()) {
            return this.m_right.execute(xPathContext).bool() ? XBoolean.S_TRUE : XBoolean.S_FALSE;
        }
        return XBoolean.S_FALSE;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean bool(XPathContext xPathContext) throws TransformerException {
        return this.m_left.bool(xPathContext) && this.m_right.bool(xPathContext);
    }
}
