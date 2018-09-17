package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTM;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;

public class FuncUnparsedEntityURI extends FunctionOneArg {
    static final long serialVersionUID = 845309759097448178L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        String name = this.m_arg0.execute(xctxt).str();
        DTM dtm = xctxt.getDTM(xctxt.getCurrentNode());
        int doc = dtm.getDocument();
        return new XString(dtm.getUnparsedEntityURI(name));
    }
}
