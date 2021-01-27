package ohos.com.sun.org.apache.xpath.internal.operations;

import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.com.sun.org.apache.xpath.internal.objects.XString;
import ohos.javax.xml.transform.TransformerException;

public class String extends UnaryOperation {
    static final long serialVersionUID = 2973374377453022888L;

    @Override // ohos.com.sun.org.apache.xpath.internal.operations.UnaryOperation
    public XObject operate(XObject xObject) throws TransformerException {
        return (XString) xObject.xstr();
    }
}
