package org.apache.xpath.patterns;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.Axis;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMAxisTraverser;
import org.apache.xml.dtm.DTMFilter;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.axes.SubContextList;
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
            case -1:
                this.m_targetString = "*";
                return;
            case 1:
                if ("*" == this.m_name) {
                    this.m_targetString = "*";
                    return;
                } else {
                    this.m_targetString = this.m_name;
                    return;
                }
            case 4:
            case 8:
            case 12:
                this.m_targetString = PsuedoNames.PSEUDONAME_TEXT;
                return;
            case 128:
                this.m_targetString = PsuedoNames.PSEUDONAME_COMMENT;
                return;
            case DTMFilter.SHOW_DOCUMENT /*256*/:
            case 1280:
                this.m_targetString = PsuedoNames.PSEUDONAME_ROOT;
                return;
            default:
                this.m_targetString = "*";
                return;
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
        return false;
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
        if (this.m_whatToShow != 65536) {
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

    private final boolean checkProximityPosition(XPathContext xctxt, int predPos, DTM dtm, int context, int pos) {
        boolean z = true;
        try {
            DTMAxisTraverser traverser = dtm.getAxisTraverser(12);
            int child = traverser.first(context);
            while (-1 != child) {
                try {
                    xctxt.pushCurrentNode(child);
                    if (NodeTest.SCORE_NONE != super.execute(xctxt, child)) {
                        boolean pass = true;
                        try {
                            xctxt.pushSubContextList(this);
                            int i = 0;
                            while (i < predPos) {
                                xctxt.pushPredicatePos(i);
                                XObject pred;
                                try {
                                    pred = this.m_predicates[i].execute(xctxt);
                                    if (2 == pred.getType()) {
                                        throw new Error("Why: Should never have been called");
                                    } else if (!pred.boolWithSideEffects()) {
                                        pass = false;
                                        pred.detach();
                                        xctxt.popPredicatePos();
                                        break;
                                    } else {
                                        pred.detach();
                                        xctxt.popPredicatePos();
                                        i++;
                                    }
                                } catch (Throwable th) {
                                    xctxt.popPredicatePos();
                                    throw th;
                                }
                            }
                            xctxt.popSubContextList();
                            if (pass) {
                                pos--;
                            }
                            if (pos < 1) {
                                xctxt.popCurrentNode();
                                return false;
                            }
                        } catch (Throwable th2) {
                            xctxt.popSubContextList();
                            throw th2;
                        }
                    }
                    xctxt.popCurrentNode();
                    child = traverser.next(context, child);
                } catch (Throwable th22) {
                    xctxt.popCurrentNode();
                    throw th22;
                }
            }
            if (pos != 1) {
                z = false;
            }
            return z;
        } catch (TransformerException se) {
            throw new RuntimeException(se.getMessage());
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
                try {
                    xctxt.pushCurrentNode(child);
                    if (NodeTest.SCORE_NONE != super.execute(xctxt, child)) {
                        boolean pass = true;
                        xctxt.pushSubContextList(this);
                        int i = 0;
                        while (i < predPos) {
                            xctxt.pushPredicatePos(i);
                            XObject pred;
                            try {
                                pred = this.m_predicates[i].execute(xctxt);
                                if (2 != pred.getType()) {
                                    if (!pred.boolWithSideEffects()) {
                                        pass = false;
                                        pred.detach();
                                        xctxt.popPredicatePos();
                                        break;
                                    }
                                } else if (pos + 1 != ((int) pred.numWithSideEffects())) {
                                    pass = false;
                                    pred.detach();
                                    xctxt.popPredicatePos();
                                    break;
                                }
                                pred.detach();
                                xctxt.popPredicatePos();
                                i++;
                            } catch (Throwable th) {
                                xctxt.popPredicatePos();
                            }
                        }
                        xctxt.popSubContextList();
                        if (pass) {
                            pos++;
                        }
                        if (!findLast && child == context) {
                            xctxt.popCurrentNode();
                            return pos;
                        }
                    }
                    xctxt.popCurrentNode();
                    child = traverser.next(parent, child);
                } catch (Throwable th2) {
                    xctxt.popCurrentNode();
                }
            }
            return pos;
        } catch (TransformerException se) {
            throw new RuntimeException(se.getMessage());
        }
    }

    public int getProximityPosition(XPathContext xctxt) {
        return getProximityPosition(xctxt, xctxt.getPredicatePos(), false);
    }

    public int getLastPos(XPathContext xctxt) {
        return getProximityPosition(xctxt, xctxt.getPredicatePos(), true);
    }

    protected final XObject executeRelativePathPattern(XPathContext xctxt, DTM dtm, int currentNode) throws TransformerException {
        XObject score = NodeTest.SCORE_NONE;
        int context = currentNode;
        DTMAxisTraverser traverser = dtm.getAxisTraverser(this.m_axis);
        int relative = traverser.first(currentNode);
        while (-1 != relative) {
            try {
                xctxt.pushCurrentNode(relative);
                score = execute(xctxt);
                if (score != NodeTest.SCORE_NONE) {
                    xctxt.popCurrentNode();
                    break;
                }
                xctxt.popCurrentNode();
                relative = traverser.next(currentNode, relative);
            } catch (Throwable th) {
                xctxt.popCurrentNode();
                throw th;
            }
        }
        return score;
    }

    protected final boolean executePredicates(XPathContext xctxt, DTM dtm, int currentNode) throws TransformerException {
        boolean result = true;
        boolean positionAlreadySeen = false;
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
                            result = false;
                            pred.detach();
                            xctxt.popPredicatePos();
                            break;
                        }
                    }
                    int pos = (int) pred.num();
                    if (!positionAlreadySeen) {
                        positionAlreadySeen = true;
                        if (!checkProximityPosition(xctxt, i, dtm, currentNode, pos)) {
                            result = false;
                            pred.detach();
                            xctxt.popPredicatePos();
                            break;
                        }
                    }
                    if (pos == 1) {
                        result = true;
                    } else {
                        result = false;
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
            } else if (65536 == pat.m_whatToShow) {
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
            } else if (128 == pat.m_whatToShow) {
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
            return false;
        }
        StepPattern sp = (StepPattern) expr;
        if (this.m_predicates != null) {
            int n = this.m_predicates.length;
            if (sp.m_predicates == null || sp.m_predicates.length != n) {
                return false;
            }
            for (int i = 0; i < n; i++) {
                if (!this.m_predicates[i].deepEquals(sp.m_predicates[i])) {
                    return false;
                }
            }
        } else if (sp.m_predicates != null) {
            return false;
        }
        if (this.m_relativePathPattern != null) {
            if (!this.m_relativePathPattern.deepEquals(sp.m_relativePathPattern)) {
                return false;
            }
        } else if (sp.m_relativePathPattern != null) {
            return false;
        }
        return true;
    }
}
