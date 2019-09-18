package org.apache.xpath.patterns;

import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMAxisTraverser;
import org.apache.xpath.XPathContext;
import org.apache.xpath.axes.WalkerFactory;
import org.apache.xpath.objects.XNumber;
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

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v11, resolved type: short} */
    /* JADX WARNING: type inference failed for: r11v4 */
    /* JADX WARNING: type inference failed for: r11v10 */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00b4, code lost:
        r15 = 9;
        r14 = r14 + 1;
        r0 = -1;
        r10 = 2;
        r11 = 1;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    public XObject executeRelativePathPattern(XPathContext xctxt, StepPattern prevStep) throws TransformerException {
        XObject score;
        XPathContext xPathContext = xctxt;
        XObject xObject = NodeTest.SCORE_NONE;
        int context = xctxt.getCurrentNode();
        DTM dtm = xPathContext.getDTM(context);
        if (dtm != null) {
            int currentNode = xctxt.getCurrentNode();
            int axis = this.m_axis;
            boolean needToTraverseAttrs = WalkerFactory.isDownwardAxisOfMany(axis);
            int i = 2;
            short s = 1;
            boolean iterRootIsAttr = dtm.getNodeType(xctxt.getIteratorRoot()) == 2;
            if (11 == axis && iterRootIsAttr) {
                axis = 15;
            }
            DTMAxisTraverser traverser = dtm.getAxisTraverser(axis);
            int relative = traverser.first(context);
            score = xObject;
            while (true) {
                int i2 = -1;
                if (-1 == relative) {
                    break;
                }
                try {
                    xPathContext.pushCurrentNode(relative);
                    score = execute(xctxt);
                    if (score != NodeTest.SCORE_NONE) {
                        if (executePredicates(xPathContext, dtm, context)) {
                            xctxt.popCurrentNode();
                            return score;
                        }
                        score = NodeTest.SCORE_NONE;
                    }
                    if (needToTraverseAttrs && iterRootIsAttr && s == dtm.getNodeType(relative)) {
                        int xaxis = 2;
                        XObject score2 = score;
                        int i3 = 0;
                        while (i3 < i) {
                            try {
                                DTMAxisTraverser atraverser = dtm.getAxisTraverser(xaxis);
                                int arelative = atraverser.first(relative);
                                short s2 = s;
                                while (true) {
                                    int arelative2 = arelative;
                                    if (i2 == arelative2) {
                                        break;
                                    }
                                    try {
                                        XObject score3 = s2;
                                        xPathContext.pushCurrentNode(arelative2);
                                        XObject score4 = execute(xctxt);
                                        try {
                                            score3 = s2;
                                            score3 = score4;
                                            if (score3 != NodeTest.SCORE_NONE) {
                                                try {
                                                    if (score3 != NodeTest.SCORE_NONE) {
                                                        xctxt.popCurrentNode();
                                                        xctxt.popCurrentNode();
                                                        return score3;
                                                    }
                                                } catch (Throwable th) {
                                                    th = th;
                                                    xctxt.popCurrentNode();
                                                    throw th;
                                                }
                                            }
                                            xctxt.popCurrentNode();
                                            arelative = atraverser.next(relative, arelative2);
                                            score2 = score3;
                                            i2 = -1;
                                            s2 = 1;
                                        } catch (Throwable th2) {
                                            th = th2;
                                            XNumber xNumber = score3;
                                            xctxt.popCurrentNode();
                                            throw th;
                                        }
                                    } catch (Throwable th3) {
                                        th = th3;
                                        XObject xObject2 = score2;
                                        xctxt.popCurrentNode();
                                        throw th;
                                    }
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                XObject xObject3 = score2;
                                xctxt.popCurrentNode();
                                throw th;
                            }
                        }
                        score = score2;
                    }
                    xctxt.popCurrentNode();
                    relative = traverser.next(context, relative);
                    i = 2;
                    s = 1;
                } catch (Throwable th5) {
                    th = th5;
                    xctxt.popCurrentNode();
                    throw th;
                }
            }
        } else {
            score = xObject;
        }
        return score;
    }
}
