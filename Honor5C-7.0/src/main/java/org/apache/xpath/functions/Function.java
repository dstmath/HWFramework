package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.res.XPATHMessages;

public abstract class Function extends Expression {
    static final long serialVersionUID = 6927661240854599768L;

    public void setArg(Expression arg, int argNum) throws WrongNumberArgsException {
        reportWrongNumberArgs();
    }

    public void checkNumberArgs(int argNum) throws WrongNumberArgsException {
        if (argNum != 0) {
            reportWrongNumberArgs();
        }
    }

    protected void reportWrongNumberArgs() throws WrongNumberArgsException {
        throw new WrongNumberArgsException(XPATHMessages.createXPATHMessage("zero", null));
    }

    public XObject execute(XPathContext xctxt) throws TransformerException {
        System.out.println("Error! Function.execute should not be called!");
        return null;
    }

    public void callArgVisitors(XPathVisitor visitor) {
    }

    public void callVisitors(ExpressionOwner owner, XPathVisitor visitor) {
        if (visitor.visitFunction(owner, this)) {
            callArgVisitors(visitor);
        }
    }

    public boolean deepEquals(Expression expr) {
        if (isSameClass(expr)) {
            return true;
        }
        return false;
    }

    public void postCompileStep(Compiler compiler) {
    }
}
