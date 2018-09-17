package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTM;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;

public class FuncDoclocation extends FunctionDef1Arg {
    static final long serialVersionUID = 7469213946343568769L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        int whereNode = getArg0AsNode(xctxt);
        String fileLocation = null;
        if (-1 != whereNode) {
            DTM dtm = xctxt.getDTM(whereNode);
            if ((short) 11 == dtm.getNodeType(whereNode)) {
                whereNode = dtm.getFirstChild(whereNode);
            }
            if (-1 != whereNode) {
                fileLocation = dtm.getDocumentBaseURI();
            }
        }
        if (fileLocation == null) {
            fileLocation = "";
        }
        return new XString(fileLocation);
    }
}
