package ohos.com.sun.org.apache.xpath.internal.operations;

import java.util.Vector;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class Operation extends Expression implements ExpressionOwner {
    static final long serialVersionUID = -3037139537171050430L;
    protected Expression m_left;
    protected Expression m_right;

    public XObject operate(XObject xObject, XObject xObject2) throws TransformerException {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
        this.m_left.fixupVariables(vector, i);
        this.m_right.fixupVariables(vector, i);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean canTraverseOutsideSubtree() {
        Expression expression = this.m_left;
        if (expression != null && expression.canTraverseOutsideSubtree()) {
            return true;
        }
        Expression expression2 = this.m_right;
        if (expression2 == null || !expression2.canTraverseOutsideSubtree()) {
            return false;
        }
        return true;
    }

    public void setLeftRight(Expression expression, Expression expression2) {
        this.m_left = expression;
        this.m_right = expression2;
        expression.exprSetParent(this);
        expression2.exprSetParent(this);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        XObject execute = this.m_left.execute(xPathContext, true);
        XObject execute2 = this.m_right.execute(xPathContext, true);
        XObject operate = operate(execute, execute2);
        execute.detach();
        execute2.detach();
        return operate;
    }

    public Expression getLeftOperand() {
        return this.m_left;
    }

    public Expression getRightOperand() {
        return this.m_right;
    }

    class LeftExprOwner implements ExpressionOwner {
        LeftExprOwner() {
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public Expression getExpression() {
            return Operation.this.m_left;
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public void setExpression(Expression expression) {
            expression.exprSetParent(Operation.this);
            Operation.this.m_left = expression;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.XPathVisitable
    public void callVisitors(ExpressionOwner expressionOwner, XPathVisitor xPathVisitor) {
        if (xPathVisitor.visitBinaryOperation(expressionOwner, this)) {
            this.m_left.callVisitors(new LeftExprOwner(), xPathVisitor);
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
        if (!isSameClass(expression)) {
            return false;
        }
        Operation operation = (Operation) expression;
        if (this.m_left.deepEquals(operation.m_left) && this.m_right.deepEquals(operation.m_right)) {
            return true;
        }
        return false;
    }
}
