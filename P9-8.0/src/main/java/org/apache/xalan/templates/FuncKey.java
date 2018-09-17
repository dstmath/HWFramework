package org.apache.xalan.templates;

import java.util.Hashtable;
import javax.xml.transform.TransformerException;
import org.apache.xalan.transformer.KeyManager;
import org.apache.xalan.transformer.TransformerImpl;
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
        TransformerImpl transformer = (TransformerImpl) xctxt.getOwnerObject();
        int context = xctxt.getCurrentNode();
        int docContext = xctxt.getDTM(context).getDocumentRoot(context);
        QName keyname = new QName(getArg0().execute(xctxt).str(), xctxt.getNamespaceContext());
        XObject arg = getArg1().execute(xctxt);
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
            Hashtable usedrefs = null;
            DTMIterator ni = arg.iter();
            UnionPathIterator upi = new UnionPathIterator();
            upi.exprSetParent(this);
            while (true) {
                int pos = ni.nextNode();
                if (-1 != pos) {
                    XMLString ref = xctxt.getDTM(pos).getStringValue(pos);
                    if (ref != null) {
                        if (usedrefs == null) {
                            usedrefs = new Hashtable();
                        }
                        if (usedrefs.get(ref) == null) {
                            usedrefs.put(ref, ISTRUE);
                            XNodeSet nl = kmgr.getNodeSetDTMByKey(xctxt, docContext, keyname, ref, xctxt.getNamespaceContext());
                            nl.setRoot(xctxt.getCurrentNode(), xctxt);
                            upi.addIterator(nl);
                        }
                    }
                } else {
                    upi.setRoot(xctxt.getCurrentNode(), xctxt);
                    return new XNodeSet((DTMIterator) upi);
                }
            }
        }
        XNodeSet nodes = kmgr.getNodeSetDTMByKey(xctxt, docContext, keyname, arg.xstr(), xctxt.getNamespaceContext());
        nodes.setRoot(xctxt.getCurrentNode(), xctxt);
        return nodes;
    }
}
