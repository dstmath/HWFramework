package org.apache.xpath.axes;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.VariableStack;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.objects.XNodeSet;

public class FilterExprIteratorSimple extends LocPathIterator {
    static final long serialVersionUID = -6978977187025375579L;
    private boolean m_canDetachNodeset = true;
    private Expression m_expr;
    private transient XNodeSet m_exprObj;
    private boolean m_mustHardReset = false;

    class filterExprOwner implements ExpressionOwner {
        filterExprOwner() {
        }

        public Expression getExpression() {
            return FilterExprIteratorSimple.this.m_expr;
        }

        public void setExpression(Expression exp) {
            exp.exprSetParent(FilterExprIteratorSimple.this);
            FilterExprIteratorSimple.this.m_expr = exp;
        }
    }

    public FilterExprIteratorSimple() {
        super(null);
    }

    public FilterExprIteratorSimple(Expression expr) {
        super(null);
        this.m_expr = expr;
    }

    public void setRoot(int context, Object environment) {
        super.setRoot(context, environment);
        this.m_exprObj = executeFilterExpr(context, this.m_execContext, getPrefixResolver(), getIsTopLevel(), this.m_stackFrame, this.m_expr);
    }

    public static XNodeSet executeFilterExpr(int context, XPathContext xctxt, PrefixResolver prefixResolver, boolean isTopLevel, int stackFrame, Expression expr) throws WrappedRuntimeException {
        PrefixResolver savedResolver = xctxt.getNamespaceContext();
        try {
            XNodeSet result;
            xctxt.pushCurrentNode(context);
            xctxt.setNamespaceContext(prefixResolver);
            if (isTopLevel) {
                VariableStack vars = xctxt.getVarStack();
                int savedStart = vars.getStackFrame();
                vars.setStackFrame(stackFrame);
                result = (XNodeSet) expr.execute(xctxt);
                result.setShouldCacheNodes(true);
                vars.setStackFrame(savedStart);
            } else {
                result = (XNodeSet) expr.execute(xctxt);
            }
            xctxt.popCurrentNode();
            xctxt.setNamespaceContext(savedResolver);
            return result;
        } catch (TransformerException se) {
            throw new WrappedRuntimeException(se);
        } catch (Throwable th) {
            xctxt.popCurrentNode();
            xctxt.setNamespaceContext(savedResolver);
        }
    }

    public int nextNode() {
        if (this.m_foundLast) {
            return -1;
        }
        int next;
        if (this.m_exprObj != null) {
            next = this.m_exprObj.nextNode();
            this.m_lastFetched = next;
        } else {
            next = -1;
            this.m_lastFetched = -1;
        }
        if (-1 != next) {
            this.m_pos++;
            return next;
        }
        this.m_foundLast = true;
        return -1;
    }

    public void detach() {
        if (this.m_allowDetach) {
            super.detach();
            this.m_exprObj.detach();
            this.m_exprObj = null;
        }
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
        if (this.m_expr.deepEquals(((FilterExprIteratorSimple) expr).m_expr)) {
            return true;
        }
        return false;
    }

    public int getAxis() {
        if (this.m_exprObj != null) {
            return this.m_exprObj.getAxis();
        }
        return 20;
    }
}
