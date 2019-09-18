package org.apache.xpath.operations;

import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;

public class Neg extends UnaryOperation {
    static final long serialVersionUID = -6280607702375702291L;

    public XObject operate(XObject right) throws TransformerException {
        return new XNumber(-right.num());
    }

    public double num(XPathContext xctxt) throws TransformerException {
        return -this.m_right.num(xctxt);
    }
}
