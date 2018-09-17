package org.apache.xpath.axes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.patterns.NodeTest;

public abstract class PredicatedNodeTest extends NodeTest implements SubContextList {
    static final boolean DEBUG_PREDICATECOUNTING = false;
    static final long serialVersionUID = -6193530757296377351L;
    protected transient boolean m_foundLast = false;
    protected LocPathIterator m_lpi;
    protected int m_predCount = -1;
    transient int m_predicateIndex = -1;
    private Expression[] m_predicates;
    protected transient int[] m_proximityPositions;

    class PredOwner implements ExpressionOwner {
        int m_index;

        PredOwner(int index) {
            this.m_index = index;
        }

        public Expression getExpression() {
            return PredicatedNodeTest.this.m_predicates[this.m_index];
        }

        public void setExpression(Expression exp) {
            exp.exprSetParent(PredicatedNodeTest.this);
            PredicatedNodeTest.this.m_predicates[this.m_index] = exp;
        }
    }

    public abstract int getLastPos(XPathContext xPathContext);

    PredicatedNodeTest(LocPathIterator locPathIterator) {
        this.m_lpi = locPathIterator;
    }

    PredicatedNodeTest() {
    }

    private void readObject(ObjectInputStream stream) throws IOException, TransformerException {
        try {
            stream.defaultReadObject();
            this.m_predicateIndex = -1;
            resetProximityPositions();
        } catch (ClassNotFoundException cnfe) {
            throw new TransformerException(cnfe);
        }
    }

    public Object clone() throws CloneNotSupportedException {
        PredicatedNodeTest clone = (PredicatedNodeTest) super.clone();
        if (this.m_proximityPositions != null && this.m_proximityPositions == clone.m_proximityPositions) {
            clone.m_proximityPositions = new int[this.m_proximityPositions.length];
            System.arraycopy(this.m_proximityPositions, 0, clone.m_proximityPositions, 0, this.m_proximityPositions.length);
        }
        if (clone.m_lpi == this) {
            clone.m_lpi = (LocPathIterator) clone;
        }
        return clone;
    }

    public int getPredicateCount() {
        if (-1 != this.m_predCount) {
            return this.m_predCount;
        }
        return this.m_predicates == null ? 0 : this.m_predicates.length;
    }

    public void setPredicateCount(int count) {
        if (count > 0) {
            Expression[] newPredicates = new Expression[count];
            for (int i = 0; i < count; i++) {
                newPredicates[i] = this.m_predicates[i];
            }
            this.m_predicates = newPredicates;
            return;
        }
        this.m_predicates = null;
    }

    protected void initPredicateInfo(Compiler compiler, int opPos) throws TransformerException {
        int pos = compiler.getFirstPredicateOpPos(opPos);
        if (pos > 0) {
            this.m_predicates = compiler.getCompiledPredicates(pos);
            if (this.m_predicates != null) {
                for (Expression exprSetParent : this.m_predicates) {
                    exprSetParent.exprSetParent(this);
                }
            }
        }
    }

    public Expression getPredicate(int index) {
        return this.m_predicates[index];
    }

    public int getProximityPosition() {
        return getProximityPosition(this.m_predicateIndex);
    }

    public int getProximityPosition(XPathContext xctxt) {
        return getProximityPosition();
    }

    protected int getProximityPosition(int predicateIndex) {
        return predicateIndex >= 0 ? this.m_proximityPositions[predicateIndex] : 0;
    }

    public void resetProximityPositions() {
        int nPredicates = getPredicateCount();
        if (nPredicates > 0) {
            if (this.m_proximityPositions == null) {
                this.m_proximityPositions = new int[nPredicates];
            }
            int i = 0;
            while (i < nPredicates) {
                try {
                    initProximityPosition(i);
                    i++;
                } catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
        }
    }

    public void initProximityPosition(int i) throws TransformerException {
        this.m_proximityPositions[i] = 0;
    }

    protected void countProximityPosition(int i) {
        int[] pp = this.m_proximityPositions;
        if (pp != null && i < pp.length) {
            pp[i] = pp[i] + 1;
        }
    }

    public boolean isReverseAxes() {
        return false;
    }

    public int getPredicateIndex() {
        return this.m_predicateIndex;
    }

    boolean executePredicates(int context, XPathContext xctxt) throws TransformerException {
        int nPredicates = getPredicateCount();
        if (nPredicates == 0) {
            return true;
        }
        PrefixResolver savedResolver = xctxt.getNamespaceContext();
        try {
            this.m_predicateIndex = 0;
            xctxt.pushSubContextList(this);
            xctxt.pushNamespaceContext(this.m_lpi.getPrefixResolver());
            xctxt.pushCurrentNode(context);
            int i = 0;
            while (i < nPredicates) {
                XObject pred = this.m_predicates[i].execute(xctxt);
                if (2 == pred.getType()) {
                    if (getProximityPosition(this.m_predicateIndex) != ((int) pred.num())) {
                        return false;
                    }
                    if (this.m_predicates[i].isStableNumber() && i == nPredicates - 1) {
                        this.m_foundLast = true;
                    }
                } else if (!pred.bool()) {
                    xctxt.popCurrentNode();
                    xctxt.popNamespaceContext();
                    xctxt.popSubContextList();
                    this.m_predicateIndex = -1;
                    return false;
                }
                int i2 = this.m_predicateIndex + 1;
                this.m_predicateIndex = i2;
                countProximityPosition(i2);
                i++;
            }
            xctxt.popCurrentNode();
            xctxt.popNamespaceContext();
            xctxt.popSubContextList();
            this.m_predicateIndex = -1;
            return true;
        } finally {
            xctxt.popCurrentNode();
            xctxt.popNamespaceContext();
            xctxt.popSubContextList();
            this.m_predicateIndex = -1;
        }
    }

    public void fixupVariables(Vector vars, int globalsSize) {
        super.fixupVariables(vars, globalsSize);
        int nPredicates = getPredicateCount();
        for (int i = 0; i < nPredicates; i++) {
            this.m_predicates[i].fixupVariables(vars, globalsSize);
        }
    }

    protected String nodeToString(int n) {
        if (-1 == n) {
            return "null";
        }
        return this.m_lpi.getXPathContext().getDTM(n).getNodeName(n) + "{" + (n + 1) + "}";
    }

    public short acceptNode(int n) {
        XPathContext xctxt = this.m_lpi.getXPathContext();
        try {
            xctxt.pushCurrentNode(n);
            if (execute(xctxt, n) != NodeTest.SCORE_NONE) {
                if (getPredicateCount() > 0) {
                    countProximityPosition(0);
                    if (!executePredicates(n, xctxt)) {
                        xctxt.popCurrentNode();
                        return (short) 3;
                    }
                }
                xctxt.popCurrentNode();
                return (short) 1;
            }
            xctxt.popCurrentNode();
            return (short) 3;
        } catch (TransformerException se) {
            throw new RuntimeException(se.getMessage());
        } catch (Throwable th) {
            xctxt.popCurrentNode();
        }
    }

    public LocPathIterator getLocPathIterator() {
        return this.m_lpi;
    }

    public void setLocPathIterator(LocPathIterator li) {
        this.m_lpi = li;
        if (this != li) {
            li.exprSetParent(this);
        }
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

    public void callPredicateVisitors(XPathVisitor visitor) {
        if (this.m_predicates != null) {
            int n = this.m_predicates.length;
            for (int i = 0; i < n; i++) {
                ExpressionOwner predOwner = new PredOwner(i);
                if (visitor.visitPredicate(predOwner, this.m_predicates[i])) {
                    this.m_predicates[i].callVisitors(predOwner, visitor);
                }
            }
        }
    }

    public boolean deepEquals(Expression expr) {
        if (!super.deepEquals(expr)) {
            return false;
        }
        PredicatedNodeTest pnt = (PredicatedNodeTest) expr;
        if (this.m_predicates != null) {
            int n = this.m_predicates.length;
            if (pnt.m_predicates == null || pnt.m_predicates.length != n) {
                return false;
            }
            for (int i = 0; i < n; i++) {
                if (!this.m_predicates[i].deepEquals(pnt.m_predicates[i])) {
                    return false;
                }
            }
        } else if (pnt.m_predicates != null) {
            return false;
        }
        return true;
    }
}
