package org.apache.xpath.functions;

import java.util.StringTokenizer;
import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.utils.StringVector;
import org.apache.xpath.NodeSetDTM;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.res.XPATHErrorResources;

public class FuncId extends FunctionOneArg {
    static final long serialVersionUID = 8930573966143567310L;

    private StringVector getNodesByID(XPathContext xctxt, int docContext, String refval, StringVector usedrefs, NodeSetDTM nodeSet, boolean mayBeMore) {
        if (refval != null) {
            StringTokenizer tokenizer = new StringTokenizer(refval);
            boolean hasMore = tokenizer.hasMoreTokens();
            DTM dtm = xctxt.getDTM(docContext);
            while (hasMore) {
                String ref = tokenizer.nextToken();
                hasMore = tokenizer.hasMoreTokens();
                if (usedrefs == null || !usedrefs.contains(ref)) {
                    int node = dtm.getElementById(ref);
                    if (-1 != node) {
                        nodeSet.addNodeInDocOrder(node, xctxt);
                    }
                    if (ref != null && (hasMore || mayBeMore)) {
                        if (usedrefs == null) {
                            usedrefs = new StringVector();
                        }
                        usedrefs.addElement(ref);
                    }
                }
            }
        }
        return usedrefs;
    }

    public XObject execute(XPathContext xctxt) throws TransformerException {
        XPathContext xPathContext = xctxt;
        int docContext = xPathContext.getDTM(xctxt.getCurrentNode()).getDocument();
        if (-1 == docContext) {
            error(xPathContext, XPATHErrorResources.ER_CONTEXT_HAS_NO_OWNERDOC, null);
        }
        XObject arg = this.m_arg0.execute(xPathContext);
        int argType = arg.getType();
        XNodeSet nodes = new XNodeSet(xctxt.getDTMManager());
        NodeSetDTM nodeSet = nodes.mutableNodeset();
        if (4 == argType) {
            DTMIterator ni = arg.iter();
            int pos = ni.nextNode();
            StringVector usedrefs = null;
            while (true) {
                int pos2 = pos;
                if (-1 == pos2) {
                    break;
                }
                DTM ndtm = ni.getDTM(pos2);
                String refval = ndtm.getStringValue(pos2).toString();
                int pos3 = ni.nextNode();
                DTM dtm = ndtm;
                usedrefs = getNodesByID(xPathContext, docContext, refval, usedrefs, nodeSet, -1 != pos3);
                pos = pos3;
                ni = ni;
            }
        } else if (-1 == argType) {
            return nodes;
        } else {
            getNodesByID(xPathContext, docContext, arg.str(), null, nodeSet, false);
        }
        return nodes;
    }
}
