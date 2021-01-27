package ohos.com.sun.org.apache.xpath.internal.functions;

import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;

public class FunctionMultiArgs extends Function3Args {
    static final long serialVersionUID = 7117257746138417181L;
    Expression[] m_args;

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function3Args, ohos.com.sun.org.apache.xpath.internal.functions.Function2Args, ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void checkNumberArgs(int i) throws WrongNumberArgsException {
    }

    public Expression[] getArgs() {
        return this.m_args;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function3Args, ohos.com.sun.org.apache.xpath.internal.functions.Function2Args, ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void setArg(Expression expression, int i) throws WrongNumberArgsException {
        if (i < 3) {
            super.setArg(expression, i);
            return;
        }
        Expression[] expressionArr = this.m_args;
        if (expressionArr == null) {
            this.m_args = new Expression[1];
            this.m_args[0] = expression;
        } else {
            Expression[] expressionArr2 = new Expression[(expressionArr.length + 1)];
            System.arraycopy(expressionArr, 0, expressionArr2, 0, expressionArr.length);
            expressionArr2[this.m_args.length] = expression;
            this.m_args = expressionArr2;
        }
        expression.exprSetParent(this);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function3Args, ohos.com.sun.org.apache.xpath.internal.functions.Function2Args, ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
        super.fixupVariables(vector, i);
        if (this.m_args != null) {
            int i2 = 0;
            while (true) {
                Expression[] expressionArr = this.m_args;
                if (i2 < expressionArr.length) {
                    expressionArr[i2].fixupVariables(vector, i);
                    i2++;
                } else {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function3Args, ohos.com.sun.org.apache.xpath.internal.functions.Function2Args, ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void reportWrongNumberArgs() throws WrongNumberArgsException {
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_INCORRECT_PROGRAMMER_ASSERTION", new Object[]{"Programmer's assertion:  the method FunctionMultiArgs.reportWrongNumberArgs() should never be called."}));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function3Args, ohos.com.sun.org.apache.xpath.internal.functions.Function2Args, ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean canTraverseOutsideSubtree() {
        if (super.canTraverseOutsideSubtree()) {
            return true;
        }
        int length = this.m_args.length;
        for (int i = 0; i < length; i++) {
            if (this.m_args[i].canTraverseOutsideSubtree()) {
                return true;
            }
        }
        return false;
    }

    class ArgMultiOwner implements ExpressionOwner {
        int m_argIndex;

        ArgMultiOwner(int i) {
            this.m_argIndex = i;
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public Expression getExpression() {
            return FunctionMultiArgs.this.m_args[this.m_argIndex];
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public void setExpression(Expression expression) {
            expression.exprSetParent(FunctionMultiArgs.this);
            FunctionMultiArgs.this.m_args[this.m_argIndex] = expression;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function3Args, ohos.com.sun.org.apache.xpath.internal.functions.Function2Args, ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void callArgVisitors(XPathVisitor xPathVisitor) {
        super.callArgVisitors(xPathVisitor);
        Expression[] expressionArr = this.m_args;
        if (expressionArr != null) {
            int length = expressionArr.length;
            for (int i = 0; i < length; i++) {
                this.m_args[i].callVisitors(new ArgMultiOwner(i), xPathVisitor);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function3Args, ohos.com.sun.org.apache.xpath.internal.functions.Function2Args, ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        if (!super.deepEquals(expression)) {
            return false;
        }
        FunctionMultiArgs functionMultiArgs = (FunctionMultiArgs) expression;
        Expression[] expressionArr = this.m_args;
        if (expressionArr != null) {
            int length = expressionArr.length;
            if (functionMultiArgs == null || functionMultiArgs.m_args.length != length) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                if (!this.m_args[i].deepEquals(functionMultiArgs.m_args[i])) {
                    return false;
                }
            }
            return true;
        } else if (functionMultiArgs.m_args != null) {
            return false;
        } else {
            return true;
        }
    }
}
