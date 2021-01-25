package ohos.com.sun.org.apache.xpath.internal.operations;

import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XNumber;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class Number extends UnaryOperation {
    static final long serialVersionUID = 7196954482871619765L;

    @Override // ohos.com.sun.org.apache.xpath.internal.operations.UnaryOperation
    public XObject operate(XObject xObject) throws TransformerException {
        if (2 == xObject.getType()) {
            return xObject;
        }
        return new XNumber(xObject.num());
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public double num(XPathContext xPathContext) throws TransformerException {
        return this.m_right.num(xPathContext);
    }
}
