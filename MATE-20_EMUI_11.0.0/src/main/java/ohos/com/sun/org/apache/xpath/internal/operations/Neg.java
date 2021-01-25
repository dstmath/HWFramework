package ohos.com.sun.org.apache.xpath.internal.operations;

import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XNumber;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class Neg extends UnaryOperation {
    static final long serialVersionUID = -6280607702375702291L;

    @Override // ohos.com.sun.org.apache.xpath.internal.operations.UnaryOperation
    public XObject operate(XObject xObject) throws TransformerException {
        return new XNumber(-xObject.num());
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public double num(XPathContext xPathContext) throws TransformerException {
        return -this.m_right.num(xPathContext);
    }
}
