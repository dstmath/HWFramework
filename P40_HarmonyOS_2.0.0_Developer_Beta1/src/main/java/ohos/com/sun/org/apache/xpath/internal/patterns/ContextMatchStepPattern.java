package ohos.com.sun.org.apache.xpath.internal.patterns;

import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.axes.WalkerFactory;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class ContextMatchStepPattern extends StepPattern {
    static final long serialVersionUID = -1888092779313211942L;

    public ContextMatchStepPattern(int i, int i2) {
        super(-1, i, i2);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.patterns.StepPattern, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        if (xPathContext.getIteratorRoot() == xPathContext.getCurrentNode()) {
            return getStaticScore();
        }
        return SCORE_NONE;
    }

    public XObject executeRelativePathPattern(XPathContext xPathContext, StepPattern stepPattern) throws TransformerException {
        XObject xObject = NodeTest.SCORE_NONE;
        int currentNode = xPathContext.getCurrentNode();
        DTM dtm = xPathContext.getDTM(currentNode);
        if (dtm != null) {
            xPathContext.getCurrentNode();
            int i = this.m_axis;
            boolean isDownwardAxisOfMany = WalkerFactory.isDownwardAxisOfMany(i);
            boolean z = dtm.getNodeType(xPathContext.getIteratorRoot()) == 2;
            if (11 == i && z) {
                i = 15;
            }
            DTMAxisTraverser axisTraverser = dtm.getAxisTraverser(i);
            for (int first = axisTraverser.first(currentNode); -1 != first; first = axisTraverser.next(currentNode, first)) {
                try {
                    xPathContext.pushCurrentNode(first);
                    xObject = execute(xPathContext);
                    if (xObject != NodeTest.SCORE_NONE) {
                        if (executePredicates(xPathContext, dtm, currentNode)) {
                            xPathContext.popCurrentNode();
                            return xObject;
                        }
                        xObject = NodeTest.SCORE_NONE;
                    }
                    if (isDownwardAxisOfMany && z && 1 == dtm.getNodeType(first)) {
                        XObject xObject2 = xObject;
                        int i2 = 2;
                        for (int i3 = 0; i3 < 2; i3++) {
                            DTMAxisTraverser axisTraverser2 = dtm.getAxisTraverser(i2);
                            for (int first2 = axisTraverser2.first(first); -1 != first2; first2 = axisTraverser2.next(first, first2)) {
                                try {
                                    xPathContext.pushCurrentNode(first2);
                                    xObject2 = execute(xPathContext);
                                    if (xObject2 == NodeTest.SCORE_NONE || xObject2 == NodeTest.SCORE_NONE) {
                                        xPathContext.popCurrentNode();
                                    } else {
                                        xPathContext.popCurrentNode();
                                        return xObject2;
                                    }
                                } finally {
                                    xPathContext.popCurrentNode();
                                }
                            }
                            i2 = 9;
                        }
                        xObject = xObject2;
                    }
                    xPathContext.popCurrentNode();
                } finally {
                    xPathContext.popCurrentNode();
                }
            }
        }
        return xObject;
    }
}
