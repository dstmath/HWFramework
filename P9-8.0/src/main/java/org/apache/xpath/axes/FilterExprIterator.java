package org.apache.xpath.axes;

import java.util.Vector;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.objects.XNodeSet;

public class FilterExprIterator extends BasicTestIterator {
    static final long serialVersionUID = 2552176105165737614L;
    private boolean m_canDetachNodeset = true;
    private Expression m_expr;
    private transient XNodeSet m_exprObj;
    private boolean m_mustHardReset = false;

    class filterExprOwner implements ExpressionOwner {
        filterExprOwner() {
        }

        public Expression getExpression() {
            return FilterExprIterator.this.m_expr;
        }

        public void setExpression(Expression exp) {
            exp.exprSetParent(FilterExprIterator.this);
            FilterExprIterator.this.m_expr = exp;
        }
    }

    public FilterExprIterator() {
        super(null);
    }

    public FilterExprIterator(Expression expr) {
        super(null);
        this.m_expr = expr;
    }

    public void setRoot(int context, Object environment) {
        super.setRoot(context, environment);
        this.m_exprObj = FilterExprIteratorSimple.executeFilterExpr(context, this.m_execContext, getPrefixResolver(), getIsTopLevel(), this.m_stackFrame, this.m_expr);
    }

    protected int getNextNode() {
        if (this.m_exprObj != null) {
            this.m_lastFetched = this.m_exprObj.nextNode();
        } else {
            this.m_lastFetched = -1;
        }
        return this.m_lastFetched;
    }

    public void detach() {
        super.detach();
        this.m_exprObj.detach();
        this.m_exprObj = null;
    }

    public void fixupVariables(Vector vars, int globalsSize) {
        super.fixupVariables(vars, globalsSize);
        this.m_expr.fixupVariables(vars, globalsSize);
    }

    public Expression getInnerExpression() {
        return this.m_expr;
    }

    public void setInnerExpression(Expression expr) {
        expr.exprSetParent(this);
        this.m_expr = expr;
    }

    public int getAnalysisBits() {
        if (this.m_expr == null || !(this.m_expr instanceof PathComponent)) {
            return WalkerFactory.BIT_FILTER;
        }
        return ((PathComponent) this.m_expr).getAnalysisBits();
    }

    public boolean isDocOrdered() {
        return this.m_exprObj.isDocOrdered();
    }

    public void callPredicateVisitors(XPathVisitor visitor) {
        this.m_expr.callVisitors(new filterExprOwner(), visitor);
        super.callPredicateVisitors(visitor);
    }

    public boolean deepEquals(Expression expr) {
        if (!super.deepEquals(expr)) {
            return false;
        }
        if (this.m_expr.deepEquals(((FilterExprIterator) expr).m_expr)) {
            return true;
        }
        return false;
    }
}
