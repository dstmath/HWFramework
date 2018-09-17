package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;

public class FuncStringLength extends FunctionDef1Arg {
    static final long serialVersionUID = -159616417996519839L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        return new XNumber((double) getArg0AsString(xctxt).length());
    }
}
