package ohos.com.sun.org.apache.xpath.internal.operations;

import ohos.com.sun.org.apache.xpath.internal.objects.XBoolean;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class Gte extends Operation {
    static final long serialVersionUID = 9142945909906680220L;

    @Override // ohos.com.sun.org.apache.xpath.internal.operations.Operation
    public XObject operate(XObject xObject, XObject xObject2) throws TransformerException {
        return xObject.greaterThanOrEqual(xObject2) ? XBoolean.S_TRUE : XBoolean.S_FALSE;
    }
}
