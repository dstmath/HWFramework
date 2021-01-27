package ohos.com.sun.org.apache.xpath.internal.functions;

import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;

public class FunctionOneArg extends Function implements ExpressionOwner {
    static final long serialVersionUID = -5180174180765609758L;
    Expression m_arg0;

    public Expression getArg0() {
        return this.m_arg0;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void setArg(Expression expression, int i) throws WrongNumberArgsException {
        if (i == 0) {
            this.m_arg0 = expression;
            expression.exprSetParent(this);
            return;
        }
        reportWrongNumberArgs();
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void checkNumberArgs(int i) throws WrongNumberArgsException {
        if (i != 1) {
            reportWrongNumberArgs();
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void reportWrongNumberArgs() throws WrongNumberArgsException {
        throw new WrongNumberArgsException(XSLMessages.createXPATHMessage("one", null));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean canTraverseOutsideSubtree() {
        return this.m_arg0.canTraverseOutsideSubtree();
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
        Expression expression = this.m_arg0;
        if (expression != null) {
            expression.fixupVariables(vector, i);
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void callArgVisitors(XPathVisitor xPathVisitor) {
        Expression expression = this.m_arg0;
        if (expression != null) {
            expression.callVisitors(this, xPathVisitor);
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
    public Expression getExpression() {
        return this.m_arg0;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
    public void setExpression(Expression expression) {
        expression.exprSetParent(this);
        this.m_arg0 = expression;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        if (!super.deepEquals(expression)) {
            return false;
        }
        Expression expression2 = this.m_arg0;
        if (expression2 != null) {
            Expression expression3 = ((FunctionOneArg) expression).m_arg0;
            if (expression3 != null && expression2.deepEquals(expression3)) {
                return true;
            }
            return false;
        } else if (((FunctionOneArg) expression).m_arg0 != null) {
            return false;
        } else {
            return true;
        }
    }
}
