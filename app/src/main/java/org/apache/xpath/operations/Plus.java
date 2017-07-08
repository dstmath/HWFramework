package org.apache.xpath.operations;

import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;

public class Plus extends Operation {
    static final long serialVersionUID = -4492072861616504256L;

    public XObject operate(XObject left, XObject right) throws TransformerException {
        return new XNumber(left.num() + right.num());
    }

    public double num(XPathContext xctxt) throws TransformerException {
        return this.m_right.num(xctxt) + this.m_left.num(xctxt);
    }
}
