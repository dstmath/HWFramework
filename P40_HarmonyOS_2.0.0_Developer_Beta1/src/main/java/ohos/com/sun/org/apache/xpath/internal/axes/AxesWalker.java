package ohos.com.sun.org.apache.xpath.internal.axes;

import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.com.sun.org.apache.xpath.internal.compiler.Compiler;
import ohos.javax.xml.transform.TransformerException;

public class AxesWalker extends PredicatedNodeTest implements Cloneable, PathComponent, ExpressionOwner {
    static final long serialVersionUID = -2966031951306601247L;
    protected int m_axis = -1;
    private transient int m_currentNode = -1;
    private DTM m_dtm;
    transient boolean m_isFresh;
    protected AxesWalker m_nextWalker;
    AxesWalker m_prevWalker;
    transient int m_root = -1;
    protected DTMAxisTraverser m_traverser;

    private int returnNextNode(int i) {
        return i;
    }

    public boolean isDocOrdered() {
        return true;
    }

    public AxesWalker(LocPathIterator locPathIterator, int i) {
        super(locPathIterator);
        this.m_axis = i;
    }

    public final WalkingIterator wi() {
        return (WalkingIterator) this.m_lpi;
    }

    public void init(Compiler compiler, int i, int i2) throws TransformerException {
        initPredicateInfo(compiler, i);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        return (AxesWalker) super.clone();
    }

    /* access modifiers changed from: package-private */
    public AxesWalker cloneDeep(WalkingIterator walkingIterator, Vector vector) throws CloneNotSupportedException {
        AxesWalker findClone = findClone(this, vector);
        if (findClone != null) {
            return findClone;
        }
        AxesWalker axesWalker = (AxesWalker) clone();
        axesWalker.setLocPathIterator(walkingIterator);
        if (vector != null) {
            vector.addElement(this);
            vector.addElement(axesWalker);
        }
        if (wi().m_lastUsedWalker == this) {
            walkingIterator.m_lastUsedWalker = axesWalker;
        }
        AxesWalker axesWalker2 = this.m_nextWalker;
        if (axesWalker2 != null) {
            axesWalker.m_nextWalker = axesWalker2.cloneDeep(walkingIterator, vector);
        }
        if (vector != null) {
            AxesWalker axesWalker3 = this.m_prevWalker;
            if (axesWalker3 != null) {
                axesWalker.m_prevWalker = axesWalker3.cloneDeep(walkingIterator, vector);
            }
        } else if (this.m_nextWalker != null) {
            axesWalker.m_nextWalker.m_prevWalker = axesWalker;
        }
        return axesWalker;
    }

    static AxesWalker findClone(AxesWalker axesWalker, Vector vector) {
        if (vector == null) {
            return null;
        }
        int size = vector.size();
        for (int i = 0; i < size; i += 2) {
            if (axesWalker == vector.elementAt(i)) {
                return (AxesWalker) vector.elementAt(i + 1);
            }
        }
        return null;
    }

    public void detach() {
        this.m_currentNode = -1;
        this.m_dtm = null;
        this.m_traverser = null;
        this.m_isFresh = true;
        this.m_root = -1;
    }

    public int getRoot() {
        return this.m_root;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PathComponent
    public int getAnalysisBits() {
        return WalkerFactory.getAnalysisBitFromAxes(getAxis());
    }

    public void setRoot(int i) {
        this.m_dtm = wi().getXPathContext().getDTM(i);
        this.m_traverser = this.m_dtm.getAxisTraverser(this.m_axis);
        this.m_isFresh = true;
        this.m_foundLast = false;
        this.m_root = i;
        this.m_currentNode = i;
        if (-1 != i) {
            resetProximityPositions();
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_SETTING_WALKER_ROOT_TO_NULL", null));
    }

    public final int getCurrentNode() {
        return this.m_currentNode;
    }

    public void setNextWalker(AxesWalker axesWalker) {
        this.m_nextWalker = axesWalker;
    }

    public AxesWalker getNextWalker() {
        return this.m_nextWalker;
    }

    public void setPrevWalker(AxesWalker axesWalker) {
        this.m_prevWalker = axesWalker;
    }

    public AxesWalker getPrevWalker() {
        return this.m_prevWalker;
    }

    /* access modifiers changed from: protected */
    public int getNextNode() {
        if (this.m_foundLast) {
            return -1;
        }
        if (this.m_isFresh) {
            this.m_currentNode = this.m_traverser.first(this.m_root);
            this.m_isFresh = false;
        } else {
            int i = this.m_currentNode;
            if (-1 != i) {
                this.m_currentNode = this.m_traverser.next(this.m_root, i);
            }
        }
        if (-1 == this.m_currentNode) {
            this.m_foundLast = true;
        }
        return this.m_currentNode;
    }

    public int nextNode() {
        AxesWalker lastUsedWalker = wi().getLastUsedWalker();
        int i = -1;
        while (true) {
            if (lastUsedWalker == null) {
                break;
            }
            i = lastUsedWalker.getNextNode();
            if (-1 == i) {
                lastUsedWalker = lastUsedWalker.m_prevWalker;
            } else if (lastUsedWalker.acceptNode(i) == 1) {
                AxesWalker axesWalker = lastUsedWalker.m_nextWalker;
                if (axesWalker == null) {
                    wi().setLastUsedWalker(lastUsedWalker);
                    break;
                }
                axesWalker.setRoot(i);
                axesWalker.m_prevWalker = lastUsedWalker;
                lastUsedWalker = axesWalker;
            } else {
                continue;
            }
        }
        return i;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, ohos.com.sun.org.apache.xpath.internal.axes.SubContextList
    public int getLastPos(XPathContext xPathContext) {
        int proximityPosition = getProximityPosition();
        try {
            AxesWalker axesWalker = (AxesWalker) clone();
            axesWalker.setPredicateCount(this.m_predicateIndex);
            axesWalker.setNextWalker(null);
            axesWalker.setPrevWalker(null);
            WalkingIterator wi = wi();
            axesWalker = wi.getLastUsedWalker();
            try {
                while (-1 != axesWalker.nextNode()) {
                    proximityPosition++;
                }
                wi.setLastUsedWalker(axesWalker);
                return proximityPosition;
            } finally {
                wi.setLastUsedWalker(axesWalker);
            }
        } catch (CloneNotSupportedException unused) {
            return -1;
        }
    }

    public void setDefaultDTM(DTM dtm) {
        this.m_dtm = dtm;
    }

    public DTM getDTM(int i) {
        return wi().getXPathContext().getDTM(i);
    }

    public int getAxis() {
        return this.m_axis;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.XPathVisitable
    public void callVisitors(ExpressionOwner expressionOwner, XPathVisitor xPathVisitor) {
        if (xPathVisitor.visitStep(expressionOwner, this)) {
            callPredicateVisitors(xPathVisitor);
            AxesWalker axesWalker = this.m_nextWalker;
            if (axesWalker != null) {
                axesWalker.callVisitors(this, xPathVisitor);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
    public Expression getExpression() {
        return this.m_nextWalker;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
    public void setExpression(Expression expression) {
        expression.exprSetParent(this);
        this.m_nextWalker = (AxesWalker) expression;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        if (super.deepEquals(expression) && this.m_axis == ((AxesWalker) expression).m_axis) {
            return true;
        }
        return false;
    }
}
