package org.apache.xpath.patterns;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.Axis;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMAxisTraverser;
import org.apache.xml.dtm.DTMFilter;
import org.apache.xml.utils.XMLChar;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.axes.SubContextList;
import org.apache.xpath.axes.WalkerFactory;
import org.apache.xpath.compiler.OpCodes;
import org.apache.xpath.compiler.PsuedoNames;
import org.apache.xpath.objects.XObject;

public class StepPattern extends NodeTest implements SubContextList, ExpressionOwner {
    private static final boolean DEBUG_MATCHES = false;
    static final long serialVersionUID = 9071668960168152644L;
    protected int m_axis;
    Expression[] m_predicates;
    StepPattern m_relativePathPattern;
    String m_targetString;

    class PredOwner implements ExpressionOwner {
        int m_index;

        PredOwner(int index) {
            this.m_index = index;
        }

        public Expression getExpression() {
            return StepPattern.this.m_predicates[this.m_index];
        }

        public void setExpression(Expression exp) {
            exp.exprSetParent(StepPattern.this);
            StepPattern.this.m_predicates[this.m_index] = exp;
        }
    }

    private final boolean checkProximityPosition(org.apache.xpath.XPathContext r11, int r12, org.apache.xml.dtm.DTM r13, int r14, int r15) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:68:0x004b in {22, 26, 30, 32, 34, 36, 38, 41, 53, 56, 61, 64, 66, 67, 69, 70, 71, 72, 73, 74, 75, 76, 78, 79, 80, 81, 82, 83, 84, 85, 87, 88, 89, 91, 92} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r10 = this;
        r7 = 0;
        r6 = 1;
        r8 = 12;
        r5 = r13.getAxisTraverser(r8);	 Catch:{ TransformerException -> 0x0050 }
        r0 = r5.first(r14);	 Catch:{ TransformerException -> 0x0050 }
    L_0x000c:
        r8 = -1;
        if (r8 == r0) goto L_0x0086;
    L_0x000f:
        r11.pushCurrentNode(r0);	 Catch:{ all -> 0x004b }
        r8 = org.apache.xpath.patterns.NodeTest.SCORE_NONE;	 Catch:{ all -> 0x004b }
        r9 = super.execute(r11, r0);	 Catch:{ all -> 0x004b }
        if (r8 == r9) goto L_0x007e;
    L_0x001a:
        r2 = 1;
        r11.pushSubContextList(r10);	 Catch:{ all -> 0x0046 }
        r1 = 0;	 Catch:{ all -> 0x0046 }
    L_0x001f:
        if (r1 >= r12) goto L_0x0068;	 Catch:{ all -> 0x0046 }
    L_0x0021:
        r11.pushPredicatePos(r1);	 Catch:{ all -> 0x0046 }
        r8 = r10.m_predicates;	 Catch:{ all -> 0x003c, all -> 0x0041 }
        r8 = r8[r1];	 Catch:{ all -> 0x003c, all -> 0x0041 }
        r3 = r8.execute(r11);	 Catch:{ all -> 0x003c, all -> 0x0041 }
        r8 = r3.getType();	 Catch:{ all -> 0x003c, all -> 0x0041 }
        r9 = 2;	 Catch:{ all -> 0x003c, all -> 0x0041 }
        if (r9 != r8) goto L_0x005b;	 Catch:{ all -> 0x003c, all -> 0x0041 }
    L_0x0033:
        r6 = new java.lang.Error;	 Catch:{ all -> 0x003c, all -> 0x0041 }
        r7 = "Why: Should never have been called";	 Catch:{ all -> 0x003c, all -> 0x0041 }
        r6.<init>(r7);	 Catch:{ all -> 0x003c, all -> 0x0041 }
        throw r6;	 Catch:{ all -> 0x003c, all -> 0x0041 }
    L_0x003c:
        r6 = move-exception;
        r3.detach();	 Catch:{ all -> 0x003c, all -> 0x0041 }
        throw r6;	 Catch:{ all -> 0x003c, all -> 0x0041 }
    L_0x0041:
        r6 = move-exception;
        r11.popPredicatePos();	 Catch:{ all -> 0x0046 }
        throw r6;	 Catch:{ all -> 0x0046 }
    L_0x0046:
        r6 = move-exception;
        r11.popSubContextList();	 Catch:{ all -> 0x004b }
        throw r6;	 Catch:{ all -> 0x004b }
    L_0x004b:
        r6 = move-exception;
        r11.popCurrentNode();	 Catch:{ TransformerException -> 0x0050 }
        throw r6;	 Catch:{ TransformerException -> 0x0050 }
    L_0x0050:
        r4 = move-exception;
        r6 = new java.lang.RuntimeException;
        r7 = r4.getMessage();
        r6.<init>(r7);
        throw r6;
    L_0x005b:
        r8 = r3.boolWithSideEffects();	 Catch:{ all -> 0x003c, all -> 0x0041 }
        if (r8 != 0) goto L_0x0075;
    L_0x0061:
        r2 = 0;
        r3.detach();	 Catch:{ all -> 0x003c, all -> 0x0041 }
        r11.popPredicatePos();	 Catch:{ all -> 0x0046 }
    L_0x0068:
        r11.popSubContextList();
        if (r2 == 0) goto L_0x006f;
    L_0x006d:
        r15 = r15 + -1;
    L_0x006f:
        if (r15 >= r6) goto L_0x007e;
    L_0x0071:
        r11.popCurrentNode();
        return r7;
    L_0x0075:
        r3.detach();	 Catch:{ all -> 0x003c, all -> 0x0041 }
        r11.popPredicatePos();	 Catch:{ all -> 0x0046 }
        r1 = r1 + 1;
        goto L_0x001f;
    L_0x007e:
        r11.popCurrentNode();	 Catch:{ TransformerException -> 0x0050 }
        r0 = r5.next(r14, r0);	 Catch:{ TransformerException -> 0x0050 }
        goto L_0x000c;
    L_0x0086:
        if (r15 != r6) goto L_0x0089;
    L_0x0088:
        return r6;
    L_0x0089:
        r6 = r7;
        goto L_0x0088;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xpath.patterns.StepPattern.checkProximityPosition(org.apache.xpath.XPathContext, int, org.apache.xml.dtm.DTM, int, int):boolean");
    }

    protected final org.apache.xpath.objects.XObject executeRelativePathPattern(org.apache.xpath.XPathContext r6, org.apache.xml.dtm.DTM r7, int r8) throws javax.xml.transform.TransformerException {
        /* JADX: method processing error */
/*
        Error: java.lang.NullPointerException
	at jadx.core.dex.visitors.ssa.SSATransform.placePhi(SSATransform.java:82)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:50)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r5 = this;
        r2 = org.apache.xpath.patterns.NodeTest.SCORE_NONE;
        r0 = r8;
        r4 = r5.m_axis;
        r3 = r7.getAxisTraverser(r4);
        r1 = r3.first(r8);
    L_0x000d:
        r4 = -1;
        if (r4 == r1) goto L_0x001e;
    L_0x0010:
        r6.pushCurrentNode(r1);	 Catch:{ all -> 0x0027 }
        r2 = r5.execute(r6);	 Catch:{ all -> 0x0027 }
        r4 = org.apache.xpath.patterns.NodeTest.SCORE_NONE;	 Catch:{ all -> 0x0027 }
        if (r2 == r4) goto L_0x001f;
    L_0x001b:
        r6.popCurrentNode();
    L_0x001e:
        return r2;
    L_0x001f:
        r6.popCurrentNode();
        r1 = r3.next(r8, r1);
        goto L_0x000d;
    L_0x0027:
        r4 = move-exception;
        r6.popCurrentNode();
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xpath.patterns.StepPattern.executeRelativePathPattern(org.apache.xpath.XPathContext, org.apache.xml.dtm.DTM, int):org.apache.xpath.objects.XObject");
    }

    public StepPattern(int whatToShow, String namespace, String name, int axis, int axisForPredicate) {
        super(whatToShow, namespace, name);
        this.m_axis = axis;
    }

    public StepPattern(int whatToShow, int axis, int axisForPredicate) {
        super(whatToShow);
        this.m_axis = axis;
    }

    public void calcTargetString() {
        switch (getWhatToShow()) {
            case OpCodes.ENDOP /*-1*/:
                this.m_targetString = PsuedoNames.PSEUDONAME_OTHER;
            case OpCodes.OP_XPATH /*1*/:
                if (PsuedoNames.PSEUDONAME_OTHER == this.m_name) {
                    this.m_targetString = PsuedoNames.PSEUDONAME_OTHER;
                } else {
                    this.m_targetString = this.m_name;
                }
            case OpCodes.OP_NOTEQUALS /*4*/:
            case OpCodes.OP_GTE /*8*/:
            case OpCodes.OP_MULT /*12*/:
                this.m_targetString = PsuedoNames.PSEUDONAME_TEXT;
            case XMLChar.MASK_NCNAME /*128*/:
                this.m_targetString = PsuedoNames.PSEUDONAME_COMMENT;
            case DTMFilter.SHOW_DOCUMENT /*256*/:
            case 1280:
                this.m_targetString = PsuedoNames.PSEUDONAME_ROOT;
            default:
                this.m_targetString = PsuedoNames.PSEUDONAME_OTHER;
        }
    }

    public String getTargetString() {
        return this.m_targetString;
    }

    public void fixupVariables(Vector vars, int globalsSize) {
        super.fixupVariables(vars, globalsSize);
        if (this.m_predicates != null) {
            for (Expression fixupVariables : this.m_predicates) {
                fixupVariables.fixupVariables(vars, globalsSize);
            }
        }
        if (this.m_relativePathPattern != null) {
            this.m_relativePathPattern.fixupVariables(vars, globalsSize);
        }
    }

    public void setRelativePathPattern(StepPattern expr) {
        this.m_relativePathPattern = expr;
        expr.exprSetParent(this);
        calcScore();
    }

    public StepPattern getRelativePathPattern() {
        return this.m_relativePathPattern;
    }

    public Expression[] getPredicates() {
        return this.m_predicates;
    }

    public boolean canTraverseOutsideSubtree() {
        int n = getPredicateCount();
        for (int i = 0; i < n; i++) {
            if (getPredicate(i).canTraverseOutsideSubtree()) {
                return true;
            }
        }
        return DEBUG_MATCHES;
    }

    public Expression getPredicate(int i) {
        return this.m_predicates[i];
    }

    public final int getPredicateCount() {
        return this.m_predicates == null ? 0 : this.m_predicates.length;
    }

    public void setPredicates(Expression[] predicates) {
        this.m_predicates = predicates;
        if (predicates != null) {
            for (Expression exprSetParent : predicates) {
                exprSetParent.exprSetParent(this);
            }
        }
        calcScore();
    }

    public void calcScore() {
        if (getPredicateCount() > 0 || this.m_relativePathPattern != null) {
            this.m_score = SCORE_OTHER;
        } else {
            super.calcScore();
        }
        if (this.m_targetString == null) {
            calcTargetString();
        }
    }

    public XObject execute(XPathContext xctxt, int currentNode) throws TransformerException {
        DTM dtm = xctxt.getDTM(currentNode);
        if (dtm != null) {
            return execute(xctxt, currentNode, dtm, dtm.getExpandedTypeID(currentNode));
        }
        return NodeTest.SCORE_NONE;
    }

    public XObject execute(XPathContext xctxt) throws TransformerException {
        return execute(xctxt, xctxt.getCurrentNode());
    }

    public XObject execute(XPathContext xctxt, int currentNode, DTM dtm, int expType) throws TransformerException {
        if (this.m_whatToShow != WalkerFactory.BIT_CHILD) {
            XObject score = super.execute(xctxt, currentNode, dtm, expType);
            if (score == NodeTest.SCORE_NONE) {
                return NodeTest.SCORE_NONE;
            }
            if (getPredicateCount() != 0 && !executePredicates(xctxt, dtm, currentNode)) {
                return NodeTest.SCORE_NONE;
            }
            if (this.m_relativePathPattern != null) {
                return this.m_relativePathPattern.executeRelativePathPattern(xctxt, dtm, currentNode);
            }
            return score;
        } else if (this.m_relativePathPattern != null) {
            return this.m_relativePathPattern.execute(xctxt);
        } else {
            return NodeTest.SCORE_NONE;
        }
    }

    private final int getProximityPosition(XPathContext xctxt, int predPos, boolean findLast) {
        int pos = 0;
        int context = xctxt.getCurrentNode();
        DTM dtm = xctxt.getDTM(context);
        int parent = dtm.getParent(context);
        try {
            DTMAxisTraverser traverser = dtm.getAxisTraverser(3);
            int child = traverser.first(parent);
            while (-1 != child) {
                xctxt.pushCurrentNode(child);
                if (NodeTest.SCORE_NONE != super.execute(xctxt, child)) {
                    boolean pass = true;
                    xctxt.pushSubContextList(this);
                    for (int i = 0; i < predPos; i++) {
                        xctxt.pushPredicatePos(i);
                        XObject pred = this.m_predicates[i].execute(xctxt);
                        if (2 == pred.getType()) {
                            if (pos + 1 != ((int) pred.numWithSideEffects())) {
                                pass = DEBUG_MATCHES;
                                pred.detach();
                                xctxt.popPredicatePos();
                                break;
                            }
                        }
                        try {
                            if (!pred.boolWithSideEffects()) {
                                pass = DEBUG_MATCHES;
                                pred.detach();
                                xctxt.popPredicatePos();
                                break;
                            }
                        } catch (Throwable th) {
                            xctxt.popPredicatePos();
                        }
                        pred.detach();
                        xctxt.popPredicatePos();
                    }
                    try {
                        xctxt.popSubContextList();
                        if (pass) {
                            pos++;
                        }
                        if (!findLast && child == context) {
                            xctxt.popCurrentNode();
                            return pos;
                        }
                    } catch (Throwable th2) {
                        xctxt.popCurrentNode();
                    }
                }
                xctxt.popCurrentNode();
                child = traverser.next(parent, child);
            }
            return pos;
        } catch (TransformerException se) {
            throw new RuntimeException(se.getMessage());
        }
    }

    public int getProximityPosition(XPathContext xctxt) {
        return getProximityPosition(xctxt, xctxt.getPredicatePos(), DEBUG_MATCHES);
    }

    public int getLastPos(XPathContext xctxt) {
        return getProximityPosition(xctxt, xctxt.getPredicatePos(), true);
    }

    protected final boolean executePredicates(XPathContext xctxt, DTM dtm, int currentNode) throws TransformerException {
        boolean result = true;
        boolean positionAlreadySeen = DEBUG_MATCHES;
        int n = getPredicateCount();
        try {
            xctxt.pushSubContextList(this);
            int i = 0;
            while (i < n) {
                xctxt.pushPredicatePos(i);
                XObject pred = this.m_predicates[i].execute(xctxt);
                try {
                    if (2 != pred.getType()) {
                        if (!pred.boolWithSideEffects()) {
                            result = DEBUG_MATCHES;
                            pred.detach();
                            xctxt.popPredicatePos();
                            break;
                        }
                    }
                    int pos = (int) pred.num();
                    if (!positionAlreadySeen) {
                        positionAlreadySeen = true;
                        if (!checkProximityPosition(xctxt, i, dtm, currentNode, pos)) {
                            result = DEBUG_MATCHES;
                            pred.detach();
                            xctxt.popPredicatePos();
                            break;
                        }
                    }
                    if (pos == 1) {
                        result = true;
                    } else {
                        result = DEBUG_MATCHES;
                    }
                    pred.detach();
                    xctxt.popPredicatePos();
                    pred.detach();
                    xctxt.popPredicatePos();
                    i++;
                } catch (Throwable th) {
                    xctxt.popPredicatePos();
                }
            }
            xctxt.popSubContextList();
            return result;
        } catch (Throwable th2) {
            xctxt.popSubContextList();
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (StepPattern pat = this; pat != null; pat = pat.m_relativePathPattern) {
            if (pat != this) {
                buf.append(PsuedoNames.PSEUDONAME_ROOT);
            }
            buf.append(Axis.getNames(pat.m_axis));
            buf.append("::");
            if (20480 == pat.m_whatToShow) {
                buf.append("doc()");
            } else if (WalkerFactory.BIT_CHILD == pat.m_whatToShow) {
                buf.append("function()");
            } else if (-1 == pat.m_whatToShow) {
                buf.append("node()");
            } else if (4 == pat.m_whatToShow) {
                buf.append("text()");
            } else if (64 == pat.m_whatToShow) {
                buf.append("processing-instruction(");
                if (pat.m_name != null) {
                    buf.append(pat.m_name);
                }
                buf.append(")");
            } else if (XMLChar.MASK_NCNAME == pat.m_whatToShow) {
                buf.append("comment()");
            } else if (pat.m_name != null) {
                if (2 == pat.m_whatToShow) {
                    buf.append("@");
                }
                if (pat.m_namespace != null) {
                    buf.append("{");
                    buf.append(pat.m_namespace);
                    buf.append("}");
                }
                buf.append(pat.m_name);
            } else if (2 == pat.m_whatToShow) {
                buf.append("@");
            } else if (1280 == pat.m_whatToShow) {
                buf.append("doc-root()");
            } else {
                buf.append("?" + Integer.toHexString(pat.m_whatToShow));
            }
            if (pat.m_predicates != null) {
                for (Object append : pat.m_predicates) {
                    buf.append("[");
                    buf.append(append);
                    buf.append("]");
                }
            }
        }
        return buf.toString();
    }

    public double getMatchScore(XPathContext xctxt, int context) throws TransformerException {
        xctxt.pushCurrentNode(context);
        xctxt.pushCurrentExpressionNode(context);
        try {
            double num = execute(xctxt).num();
            return num;
        } finally {
            xctxt.popCurrentNode();
            xctxt.popCurrentExpressionNode();
        }
    }

    public void setAxis(int axis) {
        this.m_axis = axis;
    }

    public int getAxis() {
        return this.m_axis;
    }

    public void callVisitors(ExpressionOwner owner, XPathVisitor visitor) {
        if (visitor.visitMatchPattern(owner, this)) {
            callSubtreeVisitors(visitor);
        }
    }

    protected void callSubtreeVisitors(XPathVisitor visitor) {
        if (this.m_predicates != null) {
            int n = this.m_predicates.length;
            for (int i = 0; i < n; i++) {
                ExpressionOwner predOwner = new PredOwner(i);
                if (visitor.visitPredicate(predOwner, this.m_predicates[i])) {
                    this.m_predicates[i].callVisitors(predOwner, visitor);
                }
            }
        }
        if (this.m_relativePathPattern != null) {
            this.m_relativePathPattern.callVisitors(this, visitor);
        }
    }

    public Expression getExpression() {
        return this.m_relativePathPattern;
    }

    public void setExpression(Expression exp) {
        exp.exprSetParent(this);
        this.m_relativePathPattern = (StepPattern) exp;
    }

    public boolean deepEquals(Expression expr) {
        if (!super.deepEquals(expr)) {
            return DEBUG_MATCHES;
        }
        StepPattern sp = (StepPattern) expr;
        if (this.m_predicates != null) {
            int n = this.m_predicates.length;
            if (sp.m_predicates == null || sp.m_predicates.length != n) {
                return DEBUG_MATCHES;
            }
            for (int i = 0; i < n; i++) {
                if (!this.m_predicates[i].deepEquals(sp.m_predicates[i])) {
                    return DEBUG_MATCHES;
                }
            }
        } else if (sp.m_predicates != null) {
            return DEBUG_MATCHES;
        }
        if (this.m_relativePathPattern != null) {
            if (!this.m_relativePathPattern.deepEquals(sp.m_relativePathPattern)) {
                return DEBUG_MATCHES;
            }
        } else if (sp.m_relativePathPattern != null) {
            return DEBUG_MATCHES;
        }
        return true;
    }
}
