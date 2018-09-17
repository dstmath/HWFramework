package org.apache.xpath.operations;

import javax.xml.transform.TransformerException;
import org.apache.xpath.objects.XBoolean;
import org.apache.xpath.objects.XObject;

public class NotEquals extends Operation {
    static final long serialVersionUID = -7869072863070586900L;

    public XObject operate(XObject left, XObject right) throws TransformerException {
        return left.notEquals(right) ? XBoolean.S_TRUE : XBoolean.S_FALSE;
    }
}
