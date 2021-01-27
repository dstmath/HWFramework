package ohos.com.sun.org.apache.xpath.internal.axes;

import java.util.Vector;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.com.sun.org.apache.xpath.internal.objects.XNodeSet;

public class FilterExprIterator extends BasicTestIterator {
    static final long serialVersionUID = 2552176105165737614L;
    private boolean m_canDetachNodeset = true;
    private Expression m_expr;
    private transient XNodeSet m_exprObj;
    private boolean m_mustHardReset = false;

    public FilterExprIterator() {
        super(null);
    }

    public FilterExprIterator(Expression expression) {
        super(null);
        this.m_expr = expression;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void setRoot(int i, Object obj) {
        super.setRoot(i, obj);
        this.m_exprObj = FilterExprIteratorSimple.executeFilterExpr(i, this.m_execContext, getPrefixResolver(), getIsTopLevel(), this.m_stackFrame, this.m_expr);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.axes.BasicTestIterator
    public int getNextNode() {
        XNodeSet xNodeSet = this.m_exprObj;
        if (xNodeSet != null) {
            this.m_lastFetched = xNodeSet.nextNode();
        } else {
            this.m_lastFetched = -1;
        }
        return this.m_lastFetched;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void detach() {
        super.detach();
        this.m_exprObj.detach();
        this.m_exprObj = null;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
        super.fixupVariables(vector, i);
        this.m_expr.fixupVariables(vector, i);
    }

    public Expression getInnerExpression() {
        return this.m_expr;
    }

    public void setInnerExpression(Expression expression) {
        expression.exprSetParent(this);
        this.m_expr = expression;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xpath.internal.axes.PathComponent
    public int getAnalysisBits() {
        Expression expression = this.m_expr;
        if (expression == null || !(expression instanceof PathComponent)) {
            return 67108864;
        }
        return ((PathComponent) expression).getAnalysisBits();
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public boolean isDocOrdered() {
        return this.m_exprObj.isDocOrdered();
    }

    class filterExprOwner implements ExpressionOwner {
        filterExprOwner() {
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public Expression getExpression() {
            return FilterExprIterator.this.m_expr;
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public void setExpression(Expression expression) {
            expression.exprSetParent(FilterExprIterator.this);
            FilterExprIterator.this.m_expr = expression;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest
    public void callPredicateVisitors(XPathVisitor xPathVisitor) {
        this.m_expr.callVisitors(new filterExprOwner(), xPathVisitor);
        super.callPredicateVisitors(xPathVisitor);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        if (super.deepEquals(expression) && this.m_expr.deepEquals(((FilterExprIterator) expression).m_expr)) {
            return true;
        }
        return false;
    }
}
