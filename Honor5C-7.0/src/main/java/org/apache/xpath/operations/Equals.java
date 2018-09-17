package org.apache.xpath.operations;

import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XBoolean;
import org.apache.xpath.objects.XObject;

public class Equals extends Operation {
    static final long serialVersionUID = -2658315633903426134L;

    public XObject operate(XObject left, XObject right) throws TransformerException {
        return left.equals(right) ? XBoolean.S_TRUE : XBoolean.S_FALSE;
    }

    public boolean bool(XPathContext xctxt) throws TransformerException {
        XObject left = this.m_left.execute(xctxt, true);
        XObject right = this.m_right.execute(xctxt, true);
        boolean result = left.equals(right);
        left.detach();
        right.detach();
        return result;
    }
}
