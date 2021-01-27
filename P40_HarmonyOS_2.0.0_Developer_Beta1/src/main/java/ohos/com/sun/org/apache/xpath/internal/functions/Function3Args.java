package ohos.com.sun.org.apache.xpath.internal.functions;

import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;

public class Function3Args extends Function2Args {
    static final long serialVersionUID = 7915240747161506646L;
    Expression m_arg2;

    public Expression getArg2() {
        return this.m_arg2;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function2Args, ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
        super.fixupVariables(vector, i);
        Expression expression = this.m_arg2;
        if (expression != null) {
            expression.fixupVariables(vector, i);
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function2Args, ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void setArg(Expression expression, int i) throws WrongNumberArgsException {
        if (i < 2) {
            super.setArg(expression, i);
        } else if (2 == i) {
            this.m_arg2 = expression;
            expression.exprSetParent(this);
        } else {
            reportWrongNumberArgs();
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function2Args, ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void checkNumberArgs(int i) throws WrongNumberArgsException {
        if (i != 3) {
            reportWrongNumberArgs();
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function2Args, ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void reportWrongNumberArgs() throws WrongNumberArgsException {
        throw new WrongNumberArgsException(XSLMessages.createXPATHMessage("three", null));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function2Args, ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean canTraverseOutsideSubtree() {
        if (super.canTraverseOutsideSubtree()) {
            return true;
        }
        return this.m_arg2.canTraverseOutsideSubtree();
    }

    /* access modifiers changed from: package-private */
    public class Arg2Owner implements ExpressionOwner {
        Arg2Owner() {
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public Expression getExpression() {
            return Function3Args.this.m_arg2;
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public void setExpression(Expression expression) {
            expression.exprSetParent(Function3Args.this);
            Function3Args.this.m_arg2 = expression;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function2Args, ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void callArgVisitors(XPathVisitor xPathVisitor) {
        super.callArgVisitors(xPathVisitor);
        Expression expression = this.m_arg2;
        if (expression != null) {
            expression.callVisitors(new Arg2Owner(), xPathVisitor);
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function2Args, ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        if (!super.deepEquals(expression)) {
            return false;
        }
        Expression expression2 = this.m_arg2;
        if (expression2 != null) {
            Expression expression3 = ((Function3Args) expression).m_arg2;
            if (expression3 != null && expression2.deepEquals(expression3)) {
                return true;
            }
            return false;
        } else if (((Function3Args) expression).m_arg2 != null) {
            return false;
        } else {
            return true;
        }
    }
}
