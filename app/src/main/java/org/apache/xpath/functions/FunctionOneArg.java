package org.apache.xpath.functions;

import java.util.Vector;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.res.XPATHMessages;

public class FunctionOneArg extends Function implements ExpressionOwner {
    static final long serialVersionUID = -5180174180765609758L;
    Expression m_arg0;

    public Expression getArg0() {
        return this.m_arg0;
    }

    public void setArg(Expression arg, int argNum) throws WrongNumberArgsException {
        if (argNum == 0) {
            this.m_arg0 = arg;
            arg.exprSetParent(this);
            return;
        }
        reportWrongNumberArgs();
    }

    public void checkNumberArgs(int argNum) throws WrongNumberArgsException {
        if (argNum != 1) {
            reportWrongNumberArgs();
        }
    }

    protected void reportWrongNumberArgs() throws WrongNumberArgsException {
        throw new WrongNumberArgsException(XPATHMessages.createXPATHMessage("one", null));
    }

    public boolean canTraverseOutsideSubtree() {
        return this.m_arg0.canTraverseOutsideSubtree();
    }

    public void fixupVariables(Vector vars, int globalsSize) {
        if (this.m_arg0 != null) {
            this.m_arg0.fixupVariables(vars, globalsSize);
        }
    }

    public void callArgVisitors(XPathVisitor visitor) {
        if (this.m_arg0 != null) {
            this.m_arg0.callVisitors(this, visitor);
        }
    }

    public Expression getExpression() {
        return this.m_arg0;
    }

    public void setExpression(Expression exp) {
        exp.exprSetParent(this);
        this.m_arg0 = exp;
    }

    public boolean deepEquals(Expression expr) {
        if (!super.deepEquals(expr)) {
            return false;
        }
        if (this.m_arg0 != null) {
            if (((FunctionOneArg) expr).m_arg0 == null || !this.m_arg0.deepEquals(((FunctionOneArg) expr).m_arg0)) {
                return false;
            }
        } else if (((FunctionOneArg) expr).m_arg0 != null) {
            return false;
        }
        return true;
    }
}
