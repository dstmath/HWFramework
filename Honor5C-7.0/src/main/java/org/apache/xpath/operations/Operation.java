package org.apache.xpath.operations;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.objects.XObject;

public class Operation extends Expression implements ExpressionOwner {
    static final long serialVersionUID = -3037139537171050430L;
    protected Expression m_left;
    protected Expression m_right;

    class LeftExprOwner implements ExpressionOwner {
        LeftExprOwner() {
        }

        public Expression getExpression() {
            return Operation.this.m_left;
        }

        public void setExpression(Expression exp) {
            exp.exprSetParent(Operation.this);
            Operation.this.m_left = exp;
        }
    }

    public void fixupVariables(Vector vars, int globalsSize) {
        this.m_left.fixupVariables(vars, globalsSize);
        this.m_right.fixupVariables(vars, globalsSize);
    }

    public boolean canTraverseOutsideSubtree() {
        if (this.m_left != null && this.m_left.canTraverseOutsideSubtree()) {
            return true;
        }
        if (this.m_right == null || !this.m_right.canTraverseOutsideSubtree()) {
            return false;
        }
        return true;
    }

    public void setLeftRight(Expression l, Expression r) {
        this.m_left = l;
        this.m_right = r;
        l.exprSetParent(this);
        r.exprSetParent(this);
    }

    public XObject execute(XPathContext xctxt) throws TransformerException {
        XObject left = this.m_left.execute(xctxt, true);
        XObject right = this.m_right.execute(xctxt, true);
        XObject result = operate(left, right);
        left.detach();
        right.detach();
        return result;
    }

    public XObject operate(XObject left, XObject right) throws TransformerException {
        return null;
    }

    public Expression getLeftOperand() {
        return this.m_left;
    }

    public Expression getRightOperand() {
        return this.m_right;
    }

    public void callVisitors(ExpressionOwner owner, XPathVisitor visitor) {
        if (visitor.visitBinaryOperation(owner, this)) {
            this.m_left.callVisitors(new LeftExprOwner(), visitor);
            this.m_right.callVisitors(this, visitor);
        }
    }

    public Expression getExpression() {
        return this.m_right;
    }

    public void setExpression(Expression exp) {
        exp.exprSetParent(this);
        this.m_right = exp;
    }

    public boolean deepEquals(Expression expr) {
        if (isSameClass(expr) && this.m_left.deepEquals(((Operation) expr).m_left) && this.m_right.deepEquals(((Operation) expr).m_right)) {
            return true;
        }
        return false;
    }
}
