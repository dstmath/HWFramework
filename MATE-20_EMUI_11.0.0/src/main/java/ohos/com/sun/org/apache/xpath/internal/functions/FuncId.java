package ohos.com.sun.org.apache.xpath.internal.functions;

import java.util.StringTokenizer;
import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xml.internal.utils.StringVector;
import ohos.com.sun.org.apache.xpath.internal.NodeSetDTM;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XNodeSet;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class FuncId extends FunctionOneArg {
    static final long serialVersionUID = 8930573966143567310L;

    private StringVector getNodesByID(XPathContext xPathContext, int i, String str, StringVector stringVector, NodeSetDTM nodeSetDTM, boolean z) {
        if (str != null) {
            StringTokenizer stringTokenizer = new StringTokenizer(str);
            boolean hasMoreTokens = stringTokenizer.hasMoreTokens();
            DTM dtm = xPathContext.getDTM(i);
            while (hasMoreTokens) {
                String nextToken = stringTokenizer.nextToken();
                boolean hasMoreTokens2 = stringTokenizer.hasMoreTokens();
                if (stringVector == null || !stringVector.contains(nextToken)) {
                    int elementById = dtm.getElementById(nextToken);
                    if (-1 != elementById) {
                        nodeSetDTM.addNodeInDocOrder(elementById, xPathContext);
                    }
                    if (nextToken != null && (hasMoreTokens2 || z)) {
                        if (stringVector == null) {
                            stringVector = new StringVector();
                        }
                        stringVector.addElement(nextToken);
                    }
                }
                hasMoreTokens = hasMoreTokens2;
            }
        }
        return stringVector;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        int document = xPathContext.getDTM(xPathContext.getCurrentNode()).getDocument();
        if (-1 == document) {
            error(xPathContext, "ER_CONTEXT_HAS_NO_OWNERDOC", null);
        }
        XObject execute = this.m_arg0.execute(xPathContext);
        int type = execute.getType();
        XNodeSet xNodeSet = new XNodeSet(xPathContext.getDTMManager());
        NodeSetDTM mutableNodeset = xNodeSet.mutableNodeset();
        if (4 == type) {
            DTMIterator iter = execute.iter();
            int nextNode = iter.nextNode();
            StringVector stringVector = null;
            while (-1 != nextNode) {
                String xMLString = iter.getDTM(nextNode).getStringValue(nextNode).toString();
                int nextNode2 = iter.nextNode();
                stringVector = getNodesByID(xPathContext, document, xMLString, stringVector, mutableNodeset, -1 != nextNode2);
                nextNode = nextNode2;
            }
        } else if (-1 == type) {
            return xNodeSet;
        } else {
            getNodesByID(xPathContext, document, execute.str(), null, mutableNodeset, false);
        }
        return xNodeSet;
    }
}
