package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XBoolean;
import org.apache.xpath.objects.XObject;

public class FuncNot extends FunctionOneArg {
    static final long serialVersionUID = 7299699961076329790L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        return this.m_arg0.execute(xctxt).bool() ? XBoolean.S_FALSE : XBoolean.S_TRUE;
    }
}
