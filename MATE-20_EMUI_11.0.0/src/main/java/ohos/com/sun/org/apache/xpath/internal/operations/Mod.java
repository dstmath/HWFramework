package ohos.com.sun.org.apache.xpath.internal.operations;

import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XNumber;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class Mod extends Operation {
    static final long serialVersionUID = 5009471154238918201L;

    @Override // ohos.com.sun.org.apache.xpath.internal.operations.Operation
    public XObject operate(XObject xObject, XObject xObject2) throws TransformerException {
        return new XNumber(xObject.num() % xObject2.num());
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public double num(XPathContext xPathContext) throws TransformerException {
        return this.m_left.num(xPathContext) % this.m_right.num(xPathContext);
    }
}
