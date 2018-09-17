package org.apache.xpath.operations;

import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XBoolean;
import org.apache.xpath.objects.XObject;

public class Or extends Operation {
    static final long serialVersionUID = -644107191353853079L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        if (this.m_left.execute(xctxt).bool()) {
            return XBoolean.S_TRUE;
        }
        return this.m_right.execute(xctxt).bool() ? XBoolean.S_TRUE : XBoolean.S_FALSE;
    }

    public boolean bool(XPathContext xctxt) throws TransformerException {
        return !this.m_left.bool(xctxt) ? this.m_right.bool(xctxt) : true;
    }
}
