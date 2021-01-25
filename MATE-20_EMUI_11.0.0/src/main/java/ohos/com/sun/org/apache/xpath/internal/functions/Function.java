package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.com.sun.org.apache.xpath.internal.compiler.Compiler;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.global.icu.text.PluralRules;
import ohos.javax.xml.transform.TransformerException;

public abstract class Function extends Expression {
    static final long serialVersionUID = 6927661240854599768L;

    public void callArgVisitors(XPathVisitor xPathVisitor) {
    }

    public void postCompileStep(Compiler compiler) {
    }

    public void setArg(Expression expression, int i) throws WrongNumberArgsException {
        reportWrongNumberArgs();
    }

    public void checkNumberArgs(int i) throws WrongNumberArgsException {
        if (i != 0) {
            reportWrongNumberArgs();
        }
    }

    /* access modifiers changed from: protected */
    public void reportWrongNumberArgs() throws WrongNumberArgsException {
        throw new WrongNumberArgsException(XSLMessages.createXPATHMessage(PluralRules.KEYWORD_ZERO, null));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        System.out.println("Error! Function.execute should not be called!");
        return null;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.XPathVisitable
    public void callVisitors(ExpressionOwner expressionOwner, XPathVisitor xPathVisitor) {
        if (xPathVisitor.visitFunction(expressionOwner, this)) {
            callArgVisitors(xPathVisitor);
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        return isSameClass(expression);
    }
}
