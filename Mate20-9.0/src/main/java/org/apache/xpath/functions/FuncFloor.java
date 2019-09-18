package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;

public class FuncFloor extends FunctionOneArg {
    static final long serialVersionUID = 2326752233236309265L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        return new XNumber(Math.floor(this.m_arg0.execute(xctxt).num()));
    }
}
