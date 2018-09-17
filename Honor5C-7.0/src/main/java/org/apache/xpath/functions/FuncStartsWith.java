package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XBoolean;
import org.apache.xpath.objects.XObject;

public class FuncStartsWith extends Function2Args {
    static final long serialVersionUID = 2194585774699567928L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        return this.m_arg0.execute(xctxt).xstr().startsWith(this.m_arg1.execute(xctxt).xstr()) ? XBoolean.S_TRUE : XBoolean.S_FALSE;
    }
}
