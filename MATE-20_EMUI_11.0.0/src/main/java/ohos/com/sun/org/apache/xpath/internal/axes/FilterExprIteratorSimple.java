package ohos.com.sun.org.apache.xpath.internal.axes;

import java.util.Vector;
import ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver;
import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.VariableStack;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.com.sun.org.apache.xpath.internal.objects.XNodeSet;
import ohos.javax.xml.transform.TransformerException;

public class FilterExprIteratorSimple extends LocPathIterator {
    static final long serialVersionUID = -6978977187025375579L;
    private boolean m_canDetachNodeset = true;
    private Expression m_expr;
    private transient XNodeSet m_exprObj;
    private boolean m_mustHardReset = false;

    public FilterExprIteratorSimple() {
        super(null);
    }

    public FilterExprIteratorSimple(Expression expression) {
        super(null);
        this.m_expr = expression;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void setRoot(int i, Object obj) {
        super.setRoot(i, obj);
        this.m_exprObj = executeFilterExpr(i, this.m_execContext, getPrefixResolver(), getIsTopLevel(), this.m_stackFrame, this.m_expr);
    }

    public static XNodeSet executeFilterExpr(int i, XPathContext xPathContext, PrefixResolver prefixResolver, boolean z, int i2, Expression expression) throws WrappedRuntimeException {
        XNodeSet xNodeSet;
        PrefixResolver namespaceContext = xPathContext.getNamespaceContext();
        try {
            xPathContext.pushCurrentNode(i);
            xPathContext.setNamespaceContext(prefixResolver);
            if (z) {
                VariableStack varStack = xPathContext.getVarStack();
                int stackFrame = varStack.getStackFrame();
                varStack.setStackFrame(i2);
                xNodeSet = (XNodeSet) expression.execute(xPathContext);
                xNodeSet.setShouldCacheNodes(true);
                varStack.setStackFrame(stackFrame);
            } else {
                xNodeSet = (XNodeSet) expression.execute(xPathContext);
            }
            xPathContext.popCurrentNode();
            xPathContext.setNamespaceContext(namespaceContext);
            return xNodeSet;
        } catch (TransformerException e) {
            throw new WrappedRuntimeException(e);
        } catch (Throwable th) {
            xPathContext.popCurrentNode();
            xPathContext.setNamespaceContext(namespaceContext);
            throw th;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int nextNode() {
        int i;
        if (this.m_foundLast) {
            return -1;
        }
        XNodeSet xNodeSet = this.m_exprObj;
        if (xNodeSet != null) {
            i = xNodeSet.nextNode();
            this.m_lastFetched = i;
        } else {
            this.m_lastFetched = -1;
            i = -1;
        }
        if (-1 != i) {
            this.m_pos++;
            return i;
        }
        this.m_foundLast = true;
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void detach() {
        if (this.m_allowDetach) {
            super.detach();
            this.m_exprObj.detach();
            this.m_exprObj = null;
        }
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
            return FilterExprIteratorSimple.this.m_expr;
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public void setExpression(Expression expression) {
            expression.exprSetParent(FilterExprIteratorSimple.this);
            FilterExprIteratorSimple.this.m_expr = expression;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest
    public void callPredicateVisitors(XPathVisitor xPathVisitor) {
        this.m_expr.callVisitors(new filterExprOwner(), xPathVisitor);
        super.callPredicateVisitors(xPathVisitor);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        if (super.deepEquals(expression) && this.m_expr.deepEquals(((FilterExprIteratorSimple) expression).m_expr)) {
            return true;
        }
        return false;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getAxis() {
        XNodeSet xNodeSet = this.m_exprObj;
        if (xNodeSet != null) {
            return xNodeSet.getAxis();
        }
        return 20;
    }
}
