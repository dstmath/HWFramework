package ohos.com.sun.org.apache.xpath.internal.functions;

import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.global.icu.text.PluralRules;

public class Function2Args extends FunctionOneArg {
    static final long serialVersionUID = 5574294996842710641L;
    Expression m_arg1;

    public Expression getArg1() {
        return this.m_arg1;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
        super.fixupVariables(vector, i);
        Expression expression = this.m_arg1;
        if (expression != null) {
            expression.fixupVariables(vector, i);
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void setArg(Expression expression, int i) throws WrongNumberArgsException {
        if (i == 0) {
            super.setArg(expression, i);
        } else if (1 == i) {
            this.m_arg1 = expression;
            expression.exprSetParent(this);
        } else {
            reportWrongNumberArgs();
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void checkNumberArgs(int i) throws WrongNumberArgsException {
        if (i != 2) {
            reportWrongNumberArgs();
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void reportWrongNumberArgs() throws WrongNumberArgsException {
        throw new WrongNumberArgsException(XSLMessages.createXPATHMessage(PluralRules.KEYWORD_TWO, null));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean canTraverseOutsideSubtree() {
        if (super.canTraverseOutsideSubtree()) {
            return true;
        }
        return this.m_arg1.canTraverseOutsideSubtree();
    }

    class Arg1Owner implements ExpressionOwner {
        Arg1Owner() {
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public Expression getExpression() {
            return Function2Args.this.m_arg1;
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public void setExpression(Expression expression) {
            expression.exprSetParent(Function2Args.this);
            Function2Args.this.m_arg1 = expression;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void callArgVisitors(XPathVisitor xPathVisitor) {
        super.callArgVisitors(xPathVisitor);
        Expression expression = this.m_arg1;
        if (expression != null) {
            expression.callVisitors(new Arg1Owner(), xPathVisitor);
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        if (!super.deepEquals(expression)) {
            return false;
        }
        Expression expression2 = this.m_arg1;
        if (expression2 != null) {
            Expression expression3 = ((Function2Args) expression).m_arg1;
            if (expression3 != null && expression2.deepEquals(expression3)) {
                return true;
            }
            return false;
        } else if (((Function2Args) expression).m_arg1 != null) {
            return false;
        } else {
            return true;
        }
    }
}
