package org.apache.xpath.axes;

import java.lang.annotation.RCUnownedRef;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMAxisTraverser;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.res.XPATHErrorResources;

public class AxesWalker extends PredicatedNodeTest implements Cloneable, PathComponent, ExpressionOwner {
    static final long serialVersionUID = -2966031951306601247L;
    protected int m_axis = -1;
    private transient int m_currentNode = -1;
    private DTM m_dtm;
    transient boolean m_isFresh;
    protected AxesWalker m_nextWalker;
    @RCUnownedRef
    AxesWalker m_prevWalker;
    transient int m_root = -1;
    protected DTMAxisTraverser m_traverser;

    public AxesWalker(LocPathIterator locPathIterator, int axis) {
        super(locPathIterator);
        this.m_axis = axis;
    }

    public final WalkingIterator wi() {
        return (WalkingIterator) this.m_lpi;
    }

    public void init(Compiler compiler, int opPos, int stepType) throws TransformerException {
        initPredicateInfo(compiler, opPos);
    }

    public Object clone() throws CloneNotSupportedException {
        return (AxesWalker) super.clone();
    }

    /* access modifiers changed from: package-private */
    public AxesWalker cloneDeep(WalkingIterator cloneOwner, Vector cloneList) throws CloneNotSupportedException {
        AxesWalker clone = findClone(this, cloneList);
        if (clone != null) {
            return clone;
        }
        AxesWalker clone2 = (AxesWalker) clone();
        clone2.setLocPathIterator(cloneOwner);
        if (cloneList != null) {
            cloneList.addElement(this);
            cloneList.addElement(clone2);
        }
        if (wi().m_lastUsedWalker == this) {
            cloneOwner.m_lastUsedWalker = clone2;
        }
        if (this.m_nextWalker != null) {
            clone2.m_nextWalker = this.m_nextWalker.cloneDeep(cloneOwner, cloneList);
        }
        if (cloneList != null) {
            if (this.m_prevWalker != null) {
                clone2.m_prevWalker = this.m_prevWalker.cloneDeep(cloneOwner, cloneList);
            }
        } else if (this.m_nextWalker != null) {
            clone2.m_nextWalker.m_prevWalker = clone2;
        }
        return clone2;
    }

    static AxesWalker findClone(AxesWalker key, Vector cloneList) {
        if (cloneList != null) {
            int n = cloneList.size();
            for (int i = 0; i < n; i += 2) {
                if (key == cloneList.elementAt(i)) {
                    return (AxesWalker) cloneList.elementAt(i + 1);
                }
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

    public int getAnalysisBits() {
        return WalkerFactory.getAnalysisBitFromAxes(getAxis());
    }

    public void setRoot(int root) {
        this.m_dtm = wi().getXPathContext().getDTM(root);
        this.m_traverser = this.m_dtm.getAxisTraverser(this.m_axis);
        this.m_isFresh = true;
        this.m_foundLast = false;
        this.m_root = root;
        this.m_currentNode = root;
        if (-1 != root) {
            resetProximityPositions();
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_SETTING_WALKER_ROOT_TO_NULL, null));
    }

    public final int getCurrentNode() {
        return this.m_currentNode;
    }

    public void setNextWalker(AxesWalker walker) {
        this.m_nextWalker = walker;
    }

    public AxesWalker getNextWalker() {
        return this.m_nextWalker;
    }

    public void setPrevWalker(AxesWalker walker) {
        this.m_prevWalker = walker;
    }

    public AxesWalker getPrevWalker() {
        return this.m_prevWalker;
    }

    private int returnNextNode(int n) {
        return n;
    }

    /* access modifiers changed from: protected */
    public int getNextNode() {
        if (this.m_foundLast) {
            return -1;
        }
        if (this.m_isFresh) {
            this.m_currentNode = this.m_traverser.first(this.m_root);
            this.m_isFresh = false;
        } else if (-1 != this.m_currentNode) {
            this.m_currentNode = this.m_traverser.next(this.m_root, this.m_currentNode);
        }
        if (-1 == this.m_currentNode) {
            this.m_foundLast = true;
        }
        return this.m_currentNode;
    }

    public int nextNode() {
        int nextNode = -1;
        AxesWalker walker = wi().getLastUsedWalker();
        while (true) {
            if (walker == null) {
                break;
            }
            nextNode = walker.getNextNode();
            if (-1 == nextNode) {
                walker = walker.m_prevWalker;
            } else if (walker.acceptNode(nextNode) != 1) {
                continue;
            } else if (walker.m_nextWalker == null) {
                wi().setLastUsedWalker(walker);
                break;
            } else {
                AxesWalker prev = walker;
                walker = walker.m_nextWalker;
                walker.setRoot(nextNode);
                walker.m_prevWalker = prev;
            }
        }
        return nextNode;
    }

    public int getLastPos(XPathContext xctxt) {
        int pos = getProximityPosition();
        try {
            AxesWalker savedWalker = (AxesWalker) clone();
            savedWalker.setPredicateCount(this.m_predicateIndex);
            savedWalker.setNextWalker(null);
            savedWalker.setPrevWalker(null);
            WalkingIterator lpi = wi();
            savedWalker = lpi.getLastUsedWalker();
            try {
                while (true) {
                    int nextNode = savedWalker.nextNode();
                    int i = nextNode;
                    if (-1 != nextNode) {
                        pos++;
                    } else {
                        lpi.setLastUsedWalker(savedWalker);
                        return pos;
                    }
                }
            } finally {
                lpi.setLastUsedWalker(savedWalker);
            }
        } catch (CloneNotSupportedException e) {
            return -1;
        }
    }

    public void setDefaultDTM(DTM dtm) {
        this.m_dtm = dtm;
    }

    public DTM getDTM(int node) {
        return wi().getXPathContext().getDTM(node);
    }

    public boolean isDocOrdered() {
        return true;
    }

    public int getAxis() {
        return this.m_axis;
    }

    public void callVisitors(ExpressionOwner owner, XPathVisitor visitor) {
        if (visitor.visitStep(owner, this)) {
            callPredicateVisitors(visitor);
            if (this.m_nextWalker != null) {
                this.m_nextWalker.callVisitors(this, visitor);
            }
        }
    }

    public Expression getExpression() {
        return this.m_nextWalker;
    }

    public void setExpression(Expression exp) {
        exp.exprSetParent(this);
        this.m_nextWalker = (AxesWalker) exp;
    }

    public boolean deepEquals(Expression expr) {
        if (super.deepEquals(expr) && this.m_axis == ((AxesWalker) expr).m_axis) {
            return true;
        }
        return false;
    }
}
