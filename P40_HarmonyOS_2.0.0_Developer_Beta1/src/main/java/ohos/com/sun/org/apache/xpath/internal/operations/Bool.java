package ohos.com.sun.org.apache.xpath.internal.operations;

import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XBoolean;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class Bool extends UnaryOperation {
    static final long serialVersionUID = 44705375321914635L;

    @Override // ohos.com.sun.org.apache.xpath.internal.operations.UnaryOperation
    public XObject operate(XObject xObject) throws TransformerException {
        if (1 == xObject.getType()) {
            return xObject;
        }
        return xObject.bool() ? XBoolean.S_TRUE : XBoolean.S_FALSE;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean bool(XPathContext xPathContext) throws TransformerException {
        return this.m_right.bool(xPathContext);
    }
}
