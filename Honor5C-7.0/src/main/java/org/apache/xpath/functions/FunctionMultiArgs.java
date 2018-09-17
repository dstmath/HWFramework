package org.apache.xpath.functions;

import java.util.Vector;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.res.XPATHMessages;

public class FunctionMultiArgs extends Function3Args {
    static final long serialVersionUID = 7117257746138417181L;
    Expression[] m_args;

    class ArgMultiOwner implements ExpressionOwner {
        int m_argIndex;

        ArgMultiOwner(int index) {
            this.m_argIndex = index;
        }

        public Expression getExpression() {
            return FunctionMultiArgs.this.m_args[this.m_argIndex];
        }

        public void setExpression(Expression exp) {
            exp.exprSetParent(FunctionMultiArgs.this);
            FunctionMultiArgs.this.m_args[this.m_argIndex] = exp;
        }
    }

    public Expression[] getArgs() {
        return this.m_args;
    }

    public void setArg(Expression arg, int argNum) throws WrongNumberArgsException {
        if (argNum < 3) {
            super.setArg(arg, argNum);
            return;
        }
        if (this.m_args == null) {
            this.m_args = new Expression[1];
            this.m_args[0] = arg;
        } else {
            Expression[] args = new Expression[(this.m_args.length + 1)];
            System.arraycopy(this.m_args, 0, args, 0, this.m_args.length);
            args[this.m_args.length] = arg;
            this.m_args = args;
        }
        arg.exprSetParent(this);
    }

    public void fixupVariables(Vector vars, int globalsSize) {
        super.fixupVariables(vars, globalsSize);
        if (this.m_args != null) {
            for (Expression fixupVariables : this.m_args) {
                fixupVariables.fixupVariables(vars, globalsSize);
            }
        }
    }

    public void checkNumberArgs(int argNum) throws WrongNumberArgsException {
    }

    protected void reportWrongNumberArgs() throws WrongNumberArgsException {
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_INCORRECT_PROGRAMMER_ASSERTION, new Object[]{"Programmer's assertion:  the method FunctionMultiArgs.reportWrongNumberArgs() should never be called."}));
    }

    public boolean canTraverseOutsideSubtree() {
        if (super.canTraverseOutsideSubtree()) {
            return true;
        }
        for (Expression canTraverseOutsideSubtree : this.m_args) {
            if (canTraverseOutsideSubtree.canTraverseOutsideSubtree()) {
                return true;
            }
        }
        return false;
    }

    public void callArgVisitors(XPathVisitor visitor) {
        super.callArgVisitors(visitor);
        if (this.m_args != null) {
            int n = this.m_args.length;
            for (int i = 0; i < n; i++) {
                this.m_args[i].callVisitors(new ArgMultiOwner(i), visitor);
            }
        }
    }

    public boolean deepEquals(Expression expr) {
        if (!super.deepEquals(expr)) {
            return false;
        }
        FunctionMultiArgs fma = (FunctionMultiArgs) expr;
        if (this.m_args != null) {
            int n = this.m_args.length;
            if (fma == null || fma.m_args.length != n) {
                return false;
            }
            for (int i = 0; i < n; i++) {
                if (!this.m_args[i].deepEquals(fma.m_args[i])) {
                    return false;
                }
            }
        } else if (fma.m_args != null) {
            return false;
        }
        return true;
    }
}
