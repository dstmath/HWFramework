package ohos.com.sun.org.apache.xpath.internal.functions;

import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionNode;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.ExtensionsProvider;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.com.sun.org.apache.xpath.internal.objects.XNull;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.com.sun.org.apache.xpath.internal.res.XPATHMessages;
import ohos.javax.xml.transform.TransformerException;

public class FuncExtFunction extends Function {
    static final long serialVersionUID = 5196115554693708718L;
    Vector m_argVec = new Vector();
    String m_extensionName;
    Object m_methodKey;
    String m_namespace;

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void checkNumberArgs(int i) throws WrongNumberArgsException {
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
        Vector vector2 = this.m_argVec;
        if (vector2 != null) {
            int size = vector2.size();
            for (int i2 = 0; i2 < size; i2++) {
                ((Expression) this.m_argVec.elementAt(i2)).fixupVariables(vector, i);
            }
        }
    }

    public String getNamespace() {
        return this.m_namespace;
    }

    public String getFunctionName() {
        return this.m_extensionName;
    }

    public Object getMethodKey() {
        return this.m_methodKey;
    }

    public Expression getArg(int i) {
        if (i < 0 || i >= this.m_argVec.size()) {
            return null;
        }
        return (Expression) this.m_argVec.elementAt(i);
    }

    public int getArgCount() {
        return this.m_argVec.size();
    }

    public FuncExtFunction(String str, String str2, Object obj) {
        this.m_namespace = str;
        this.m_extensionName = str2;
        this.m_methodKey = obj;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        if (!xPathContext.isSecureProcessing()) {
            Vector vector = new Vector();
            int size = this.m_argVec.size();
            for (int i = 0; i < size; i++) {
                XObject execute = ((Expression) this.m_argVec.elementAt(i)).execute(xPathContext);
                execute.allowDetachToRelease(false);
                vector.addElement(execute);
            }
            Object extFunction = ((ExtensionsProvider) xPathContext.getOwnerObject()).extFunction(this, vector);
            if (extFunction != null) {
                return XObject.create(extFunction, xPathContext);
            }
            return new XNull();
        }
        throw new TransformerException(XPATHMessages.createXPATHMessage("ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED", new Object[]{toString()}));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void setArg(Expression expression, int i) throws WrongNumberArgsException {
        this.m_argVec.addElement(expression);
        expression.exprSetParent(this);
    }

    class ArgExtOwner implements ExpressionOwner {
        Expression m_exp;

        ArgExtOwner(Expression expression) {
            this.m_exp = expression;
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public Expression getExpression() {
            return this.m_exp;
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public void setExpression(Expression expression) {
            expression.exprSetParent(FuncExtFunction.this);
            this.m_exp = expression;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void callArgVisitors(XPathVisitor xPathVisitor) {
        for (int i = 0; i < this.m_argVec.size(); i++) {
            Expression expression = (Expression) this.m_argVec.elementAt(i);
            expression.callVisitors(new ArgExtOwner(expression), xPathVisitor);
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression, ohos.com.sun.org.apache.xpath.internal.ExpressionNode
    public void exprSetParent(ExpressionNode expressionNode) {
        super.exprSetParent(expressionNode);
        int size = this.m_argVec.size();
        for (int i = 0; i < size; i++) {
            ((Expression) this.m_argVec.elementAt(i)).exprSetParent(expressionNode);
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void reportWrongNumberArgs() throws WrongNumberArgsException {
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_INCORRECT_PROGRAMMER_ASSERTION", new Object[]{"Programmer's assertion:  the method FunctionMultiArgs.reportWrongNumberArgs() should never be called."}));
    }

    @Override // java.lang.Object
    public String toString() {
        String str = this.m_namespace;
        if (str == null || str.length() <= 0) {
            return this.m_extensionName;
        }
        return "{" + this.m_namespace + "}" + this.m_extensionName;
    }
}
