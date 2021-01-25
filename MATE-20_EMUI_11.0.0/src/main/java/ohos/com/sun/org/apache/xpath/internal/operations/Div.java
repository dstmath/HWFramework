package ohos.com.sun.org.apache.xpath.internal.operations;

import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XNumber;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class Div extends Operation {
    static final long serialVersionUID = 6220756595959798135L;

    @Override // ohos.com.sun.org.apache.xpath.internal.operations.Operation
    public XObject operate(XObject xObject, XObject xObject2) throws TransformerException {
        return new XNumber(xObject.num() / xObject2.num());
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public double num(XPathContext xPathContext) throws TransformerException {
        return this.m_left.num(xPathContext) / this.m_right.num(xPathContext);
    }
}
