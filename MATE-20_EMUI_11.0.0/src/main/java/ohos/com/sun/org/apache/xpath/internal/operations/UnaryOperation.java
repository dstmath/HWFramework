package ohos.com.sun.org.apache.xpath.internal.operations;

import java.util.Vector;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public abstract class UnaryOperation extends Expression implements ExpressionOwner {
    static final long serialVersionUID = 6536083808424286166L;
    protected Expression m_right;

    public abstract XObject operate(XObject xObject) throws TransformerException;

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
        this.m_right.fixupVariables(vector, i);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean canTraverseOutsideSubtree() {
        Expression expression = this.m_right;
        return expression != null && expression.canTraverseOutsideSubtree();
    }

    public void setRight(Expression expression) {
        this.m_right = expression;
        expression.exprSetParent(this);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        return operate(this.m_right.execute(xPathContext));
    }

    public Expression getOperand() {
        return this.m_right;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.XPathVisitable
    public void callVisitors(ExpressionOwner expressionOwner, XPathVisitor xPathVisitor) {
        if (xPathVisitor.visitUnaryOperation(expressionOwner, this)) {
            this.m_right.callVisitors(this, xPathVisitor);
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
    public Expression getExpression() {
        return this.m_right;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
    public void setExpression(Expression expression) {
        expression.exprSetParent(this);
        this.m_right = expression;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        if (isSameClass(expression) && this.m_right.deepEquals(((UnaryOperation) expression).m_right)) {
            return true;
        }
        return false;
    }
}
