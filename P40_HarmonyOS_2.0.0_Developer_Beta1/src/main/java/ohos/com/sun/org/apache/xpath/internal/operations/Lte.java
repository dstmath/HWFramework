package ohos.com.sun.org.apache.xpath.internal.operations;

import ohos.com.sun.org.apache.xpath.internal.objects.XBoolean;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class Lte extends Operation {
    static final long serialVersionUID = 6945650810527140228L;

    @Override // ohos.com.sun.org.apache.xpath.internal.operations.Operation
    public XObject operate(XObject xObject, XObject xObject2) throws TransformerException {
        return xObject.lessThanOrEqual(xObject2) ? XBoolean.S_TRUE : XBoolean.S_FALSE;
    }
}
