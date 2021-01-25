package ohos.com.sun.org.apache.xpath.internal.operations;

import ohos.com.sun.org.apache.xpath.internal.objects.XNumber;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class Quo extends Operation {
    static final long serialVersionUID = 693765299196169905L;

    @Override // ohos.com.sun.org.apache.xpath.internal.operations.Operation
    public XObject operate(XObject xObject, XObject xObject2) throws TransformerException {
        return new XNumber((double) ((int) (xObject.num() / xObject2.num())));
    }
}
