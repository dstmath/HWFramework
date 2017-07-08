package org.apache.xpath.operations;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.objects.XObject;

public abstract class UnaryOperation extends Expression implements ExpressionOwner {
    static final long serialVersionUID = 6536083808424286166L;
    protected Expression m_right;

    public abstract XObject operate(XObject xObject) throws TransformerException;

    public void fixupVariables(Vector vars, int globalsSize) {
        this.m_right.fixupVariables(vars, globalsSize);
    }

    public boolean canTraverseOutsideSubtree() {
        if (this.m_right == null || !this.m_right.canTraverseOutsideSubtree()) {
            return false;
        }
        return true;
    }

    public void setRight(Expression r) {
        this.m_right = r;
        r.exprSetParent(this);
    }

    public XObject execute(XPathContext xctxt) throws TransformerException {
        return operate(this.m_right.execute(xctxt));
    }

    public Expression getOperand() {
        return this.m_right;
    }

    public void callVisitors(ExpressionOwner owner, XPathVisitor visitor) {
        if (visitor.visitUnaryOperation(owner, this)) {
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
        if (isSameClass(expr) && this.m_right.deepEquals(((UnaryOperation) expr).m_right)) {
            return true;
        }
        return false;
    }
}
