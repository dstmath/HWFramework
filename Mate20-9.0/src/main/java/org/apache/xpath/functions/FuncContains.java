package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XBoolean;
import org.apache.xpath.objects.XObject;

public class FuncContains extends Function2Args {
    static final long serialVersionUID = 5084753781887919723L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        String s1 = this.m_arg0.execute(xctxt).str();
        String s2 = this.m_arg1.execute(xctxt).str();
        if (s1.length() == 0 && s2.length() == 0) {
            return XBoolean.S_TRUE;
        }
        return s1.indexOf(s2) > -1 ? XBoolean.S_TRUE : XBoolean.S_FALSE;
    }
}
