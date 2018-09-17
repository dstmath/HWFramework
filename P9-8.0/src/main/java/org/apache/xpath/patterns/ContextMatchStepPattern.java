package org.apache.xpath.patterns;

import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMAxisTraverser;
import org.apache.xpath.XPathContext;
import org.apache.xpath.axes.WalkerFactory;
import org.apache.xpath.objects.XObject;

public class ContextMatchStepPattern extends StepPattern {
    static final long serialVersionUID = -1888092779313211942L;

    public ContextMatchStepPattern(int axis, int paxis) {
        super(-1, axis, paxis);
    }

    public XObject execute(XPathContext xctxt) throws TransformerException {
        if (xctxt.getIteratorRoot() == xctxt.getCurrentNode()) {
            return getStaticScore();
        }
        return SCORE_NONE;
    }

    public XObject executeRelativePathPattern(XPathContext xctxt, StepPattern prevStep) throws TransformerException {
        XObject score = NodeTest.SCORE_NONE;
        int context = xctxt.getCurrentNode();
        DTM dtm = xctxt.getDTM(context);
        if (dtm != null) {
            int predContext = xctxt.getCurrentNode();
            int axis = this.m_axis;
            boolean needToTraverseAttrs = WalkerFactory.isDownwardAxisOfMany(axis);
            boolean iterRootIsAttr = dtm.getNodeType(xctxt.getIteratorRoot()) == (short) 2;
            if (11 == axis && iterRootIsAttr) {
                axis = 15;
            }
            DTMAxisTraverser traverser = dtm.getAxisTraverser(axis);
            int relative = traverser.first(context);
            while (-1 != relative) {
                try {
                    xctxt.pushCurrentNode(relative);
                    score = execute(xctxt);
                    if (score != NodeTest.SCORE_NONE) {
                        if (executePredicates(xctxt, dtm, context)) {
                            xctxt.popCurrentNode();
                            return score;
                        }
                        score = NodeTest.SCORE_NONE;
                    }
                    if (needToTraverseAttrs && iterRootIsAttr && (short) 1 == dtm.getNodeType(relative)) {
                        int xaxis = 2;
                        for (int i = 0; i < 2; i++) {
                            DTMAxisTraverser atraverser = dtm.getAxisTraverser(xaxis);
                            int arelative = atraverser.first(relative);
                            while (-1 != arelative) {
                                xctxt.pushCurrentNode(arelative);
                                score = execute(xctxt);
                                if (score == NodeTest.SCORE_NONE || score == NodeTest.SCORE_NONE) {
                                    xctxt.popCurrentNode();
                                    arelative = atraverser.next(relative, arelative);
                                } else {
                                    xctxt.popCurrentNode();
                                    return score;
                                }
                            }
                            xaxis = 9;
                        }
                        continue;
                    }
                    xctxt.popCurrentNode();
                    relative = traverser.next(context, relative);
                } catch (Throwable th) {
                } finally {
                    xctxt.popCurrentNode();
                }
            }
        }
        return score;
    }
}
