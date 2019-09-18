package org.apache.xalan.templates;

import java.util.Hashtable;
import javax.xml.transform.TransformerException;
import org.apache.xalan.transformer.KeyManager;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.XMLString;
import org.apache.xpath.XPathContext;
import org.apache.xpath.axes.UnionPathIterator;
import org.apache.xpath.functions.Function2Args;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XObject;

public class FuncKey extends Function2Args {
    private static Boolean ISTRUE = new Boolean(true);
    static final long serialVersionUID = 9089293100115347340L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        DTM dtm;
        Hashtable usedrefs;
        XPathContext xPathContext = xctxt;
        TransformerImpl transformer = (TransformerImpl) xctxt.getOwnerObject();
        int context = xctxt.getCurrentNode();
        int docContext = xPathContext.getDTM(context).getDocumentRoot(context);
        QName keyname = new QName(getArg0().execute(xPathContext).str(), xctxt.getNamespaceContext());
        XObject arg = getArg1().execute(xPathContext);
        boolean argIsNodeSetDTM = 4 == arg.getType();
        KeyManager kmgr = transformer.getKeyManager();
        if (argIsNodeSetDTM) {
            XNodeSet ns = (XNodeSet) arg;
            ns.setShouldCacheNodes(true);
            if (ns.getLength() <= 1) {
                argIsNodeSetDTM = false;
            }
        }
        if (argIsNodeSetDTM) {
            Hashtable usedrefs2 = null;
            DTMIterator ni = arg.iter();
            UnionPathIterator upi = new UnionPathIterator();
            upi.exprSetParent(this);
            while (true) {
                int nextNode = ni.nextNode();
                int pos = nextNode;
                if (-1 != nextNode) {
                    DTM dtm2 = xPathContext.getDTM(pos);
                    XMLString ref = dtm2.getStringValue(pos);
                    if (ref == null) {
                        DTM dtm3 = dtm2;
                    } else {
                        if (usedrefs2 == null) {
                            usedrefs2 = new Hashtable();
                        }
                        if (usedrefs2.get(ref) != null) {
                            usedrefs = usedrefs2;
                            dtm = dtm2;
                        } else {
                            usedrefs2.put(ref, ISTRUE);
                            usedrefs = usedrefs2;
                            dtm = dtm2;
                            int i = pos;
                            XNodeSet nl = kmgr.getNodeSetDTMByKey(xPathContext, docContext, keyname, ref, xctxt.getNamespaceContext());
                            nl.setRoot(xctxt.getCurrentNode(), xPathContext);
                            upi.addIterator(nl);
                        }
                        usedrefs2 = usedrefs;
                        DTM dtm4 = dtm;
                    }
                } else {
                    int i2 = pos;
                    upi.setRoot(xctxt.getCurrentNode(), xPathContext);
                    XObject xObject = arg;
                    return new XNodeSet((DTMIterator) upi);
                }
            }
        } else {
            XObject xObject2 = arg;
            XNodeSet nodes = kmgr.getNodeSetDTMByKey(xPathContext, docContext, keyname, arg.xstr(), xctxt.getNamespaceContext());
            nodes.setRoot(xctxt.getCurrentNode(), xPathContext);
            return nodes;
        }
    }
}
