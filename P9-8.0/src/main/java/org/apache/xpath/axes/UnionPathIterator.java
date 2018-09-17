package org.apache.xpath.axes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.compiler.OpMap;

public class UnionPathIterator extends LocPathIterator implements Cloneable, DTMIterator, Serializable, PathComponent {
    static final long serialVersionUID = -3910351546843826781L;
    protected LocPathIterator[] m_exprs;
    protected DTMIterator[] m_iterators;

    class iterOwner implements ExpressionOwner {
        int m_index;

        iterOwner(int index) {
            this.m_index = index;
        }

        public Expression getExpression() {
            return UnionPathIterator.this.m_exprs[this.m_index];
        }

        public void setExpression(Expression exp) {
            if (exp instanceof LocPathIterator) {
                exp.exprSetParent(UnionPathIterator.this);
            } else {
                Expression wi = new WalkingIterator(UnionPathIterator.this.getPrefixResolver());
                FilterExprWalker few = new FilterExprWalker(wi);
                wi.setFirstWalker(few);
                few.setInnerExpression(exp);
                wi.exprSetParent(UnionPathIterator.this);
                few.exprSetParent(wi);
                exp.exprSetParent(few);
                exp = wi;
            }
            UnionPathIterator.this.m_exprs[this.m_index] = (LocPathIterator) exp;
        }
    }

    public UnionPathIterator() {
        this.m_iterators = null;
        this.m_exprs = null;
    }

    public void setRoot(int context, Object environment) {
        super.setRoot(context, environment);
        try {
            if (this.m_exprs != null) {
                int n = this.m_exprs.length;
                DTMIterator[] newIters = new DTMIterator[n];
                for (int i = 0; i < n; i++) {
                    DTMIterator iter = this.m_exprs[i].asIterator(this.m_execContext, context);
                    newIters[i] = iter;
                    iter.nextNode();
                }
                this.m_iterators = newIters;
            }
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    public void addIterator(DTMIterator expr) {
        if (this.m_iterators == null) {
            this.m_iterators = new DTMIterator[1];
            this.m_iterators[0] = expr;
        } else {
            DTMIterator[] exprs = this.m_iterators;
            int len = this.m_iterators.length;
            this.m_iterators = new DTMIterator[(len + 1)];
            System.arraycopy(exprs, 0, this.m_iterators, 0, len);
            this.m_iterators[len] = expr;
        }
        expr.nextNode();
        if (expr instanceof Expression) {
            ((Expression) expr).exprSetParent(this);
        }
    }

    public void detach() {
        if (this.m_allowDetach && this.m_iterators != null) {
            for (DTMIterator detach : this.m_iterators) {
                detach.detach();
            }
            this.m_iterators = null;
        }
    }

    public UnionPathIterator(Compiler compiler, int opPos) throws TransformerException {
        loadLocationPaths(compiler, OpMap.getFirstChildPos(opPos), 0);
    }

    public static LocPathIterator createUnionIterator(Compiler compiler, int opPos) throws TransformerException {
        UnionPathIterator upi = new UnionPathIterator(compiler, opPos);
        boolean isAllChildIterators = true;
        int i = 0;
        while (i < nPaths) {
            LocPathIterator lpi = upi.m_exprs[i];
            if (lpi.getAxis() != 3) {
                isAllChildIterators = false;
                break;
            } else if (HasPositionalPredChecker.check(lpi)) {
                isAllChildIterators = false;
                break;
            } else {
                i++;
            }
        }
        if (!isAllChildIterators) {
            return upi;
        }
        UnionChildIterator uci = new UnionChildIterator();
        for (PredicatedNodeTest lpi2 : upi.m_exprs) {
            uci.addNodeTest(lpi2);
        }
        return uci;
    }

    public int getAnalysisBits() {
        int bits = 0;
        if (this.m_exprs != null) {
            for (LocPathIterator analysisBits : this.m_exprs) {
                bits |= analysisBits.getAnalysisBits();
            }
        }
        return bits;
    }

    private void readObject(ObjectInputStream stream) throws IOException, TransformerException {
        try {
            stream.defaultReadObject();
            this.m_clones = new IteratorPool(this);
        } catch (ClassNotFoundException cnfe) {
            throw new TransformerException(cnfe);
        }
    }

    public Object clone() throws CloneNotSupportedException {
        UnionPathIterator clone = (UnionPathIterator) super.clone();
        if (this.m_iterators != null) {
            int n = this.m_iterators.length;
            clone.m_iterators = new DTMIterator[n];
            for (int i = 0; i < n; i++) {
                clone.m_iterators[i] = (DTMIterator) this.m_iterators[i].clone();
            }
        }
        return clone;
    }

    protected LocPathIterator createDTMIterator(Compiler compiler, int opPos) throws TransformerException {
        boolean z = false;
        if (compiler.getLocationPathDepth() <= 0) {
            z = true;
        }
        return (LocPathIterator) WalkerFactory.newDTMIterator(compiler, opPos, z);
    }

    protected void loadLocationPaths(Compiler compiler, int opPos, int count) throws TransformerException {
        int steptype = compiler.getOp(opPos);
        if (steptype == 28) {
            loadLocationPaths(compiler, compiler.getNextOpPos(opPos), count + 1);
            this.m_exprs[count] = createDTMIterator(compiler, opPos);
            this.m_exprs[count].exprSetParent(this);
            return;
        }
        switch (steptype) {
            case 22:
            case 23:
            case 24:
            case 25:
                loadLocationPaths(compiler, compiler.getNextOpPos(opPos), count + 1);
                WalkingIterator iter = new WalkingIterator(compiler.getNamespaceContext());
                iter.exprSetParent(this);
                if (compiler.getLocationPathDepth() <= 0) {
                    iter.setIsTopLevel(true);
                }
                iter.m_firstWalker = new FilterExprWalker(iter);
                iter.m_firstWalker.init(compiler, opPos, steptype);
                this.m_exprs[count] = iter;
                return;
            default:
                this.m_exprs = new LocPathIterator[count];
                return;
        }
    }

    public int nextNode() {
        if (this.m_foundLast) {
            return -1;
        }
        int earliestNode = -1;
        if (this.m_iterators != null) {
            int n = this.m_iterators.length;
            int iteratorUsed = -1;
            for (int i = 0; i < n; i++) {
                int node = this.m_iterators[i].getCurrentNode();
                if (-1 != node) {
                    if (-1 == earliestNode) {
                        iteratorUsed = i;
                        earliestNode = node;
                    } else if (node == earliestNode) {
                        this.m_iterators[i].nextNode();
                    } else if (getDTM(node).isNodeAfter(node, earliestNode)) {
                        iteratorUsed = i;
                        earliestNode = node;
                    }
                }
            }
            if (-1 != earliestNode) {
                this.m_iterators[iteratorUsed].nextNode();
                incrementCurrentPos();
            } else {
                this.m_foundLast = true;
            }
        }
        this.m_lastFetched = earliestNode;
        return earliestNode;
    }

    public void fixupVariables(Vector vars, int globalsSize) {
        for (LocPathIterator fixupVariables : this.m_exprs) {
            fixupVariables.fixupVariables(vars, globalsSize);
        }
    }

    public int getAxis() {
        return -1;
    }

    public void callVisitors(ExpressionOwner owner, XPathVisitor visitor) {
        if (visitor.visitUnionPath(owner, this) && this.m_exprs != null) {
            int n = this.m_exprs.length;
            for (int i = 0; i < n; i++) {
                this.m_exprs[i].callVisitors(new iterOwner(i), visitor);
            }
        }
    }

    public boolean deepEquals(Expression expr) {
        if (!super.deepEquals(expr)) {
            return false;
        }
        UnionPathIterator upi = (UnionPathIterator) expr;
        if (this.m_exprs != null) {
            int n = this.m_exprs.length;
            if (upi.m_exprs == null || upi.m_exprs.length != n) {
                return false;
            }
            for (int i = 0; i < n; i++) {
                if (!this.m_exprs[i].deepEquals(upi.m_exprs[i])) {
                    return false;
                }
            }
        } else if (upi.m_exprs != null) {
            return false;
        }
        return true;
    }
}
