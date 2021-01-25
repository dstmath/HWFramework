package ohos.com.sun.org.apache.xpath.internal;

import java.io.Serializable;
import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.com.sun.org.apache.xpath.internal.objects.XNodeSet;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.SAXException;

public abstract class Expression implements Serializable, ExpressionNode, XPathVisitable {
    static final long serialVersionUID = 565665869777906902L;
    private ExpressionNode m_parent;

    public boolean canTraverseOutsideSubtree() {
        return false;
    }

    public abstract boolean deepEquals(Expression expression);

    public abstract XObject execute(XPathContext xPathContext) throws TransformerException;

    @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionNode
    public ExpressionNode exprGetChild(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionNode
    public int exprGetNumChildren() {
        return 0;
    }

    public abstract void fixupVariables(Vector vector, int i);

    public boolean isNodesetExpr() {
        return false;
    }

    public boolean isStableNumber() {
        return false;
    }

    public XObject execute(XPathContext xPathContext, int i) throws TransformerException {
        return execute(xPathContext);
    }

    public XObject execute(XPathContext xPathContext, int i, DTM dtm, int i2) throws TransformerException {
        return execute(xPathContext);
    }

    public XObject execute(XPathContext xPathContext, boolean z) throws TransformerException {
        return execute(xPathContext);
    }

    public double num(XPathContext xPathContext) throws TransformerException {
        return execute(xPathContext).num();
    }

    public boolean bool(XPathContext xPathContext) throws TransformerException {
        return execute(xPathContext).bool();
    }

    public XMLString xstr(XPathContext xPathContext) throws TransformerException {
        return execute(xPathContext).xstr();
    }

    public int asNode(XPathContext xPathContext) throws TransformerException {
        return execute(xPathContext).iter().nextNode();
    }

    public DTMIterator asIterator(XPathContext xPathContext, int i) throws TransformerException {
        try {
            xPathContext.pushCurrentNodeAndExpression(i, i);
            return execute(xPathContext).iter();
        } finally {
            xPathContext.popCurrentNodeAndExpression();
        }
    }

    public DTMIterator asIteratorRaw(XPathContext xPathContext, int i) throws TransformerException {
        try {
            xPathContext.pushCurrentNodeAndExpression(i, i);
            return ((XNodeSet) execute(xPathContext)).iterRaw();
        } finally {
            xPathContext.popCurrentNodeAndExpression();
        }
    }

    public void executeCharsToContentHandler(XPathContext xPathContext, ContentHandler contentHandler) throws TransformerException, SAXException {
        XObject execute = execute(xPathContext);
        execute.dispatchCharactersEvents(contentHandler);
        execute.detach();
    }

    /* access modifiers changed from: protected */
    public final boolean isSameClass(Expression expression) {
        return expression != null && getClass() == expression.getClass();
    }

    public void warn(XPathContext xPathContext, String str, Object[] objArr) throws TransformerException {
        String createXPATHWarning = XSLMessages.createXPATHWarning(str, objArr);
        if (xPathContext != null) {
            xPathContext.getErrorListener().warning(new TransformerException(createXPATHWarning, xPathContext.getSAXLocator()));
        }
    }

    public void assertion(boolean z, String str) {
        if (!z) {
            throw new RuntimeException(XSLMessages.createXPATHMessage("ER_INCORRECT_PROGRAMMER_ASSERTION", new Object[]{str}));
        }
    }

    public void error(XPathContext xPathContext, String str, Object[] objArr) throws TransformerException {
        String createXPATHMessage = XSLMessages.createXPATHMessage(str, objArr);
        if (xPathContext != null) {
            xPathContext.getErrorListener().fatalError(new TransformerException(createXPATHMessage, this));
        }
    }

    public ExpressionNode getExpressionOwner() {
        ExpressionNode exprGetParent = exprGetParent();
        while (exprGetParent != null && (exprGetParent instanceof Expression)) {
            exprGetParent = exprGetParent.exprGetParent();
        }
        return exprGetParent;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionNode
    public void exprSetParent(ExpressionNode expressionNode) {
        assertion(expressionNode != this, "Can not parent an expression to itself!");
        this.m_parent = expressionNode;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionNode
    public ExpressionNode exprGetParent() {
        return this.m_parent;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionNode
    public void exprAddChild(ExpressionNode expressionNode, int i) {
        assertion(false, "exprAddChild method not implemented!");
    }

    public String getPublicId() {
        ExpressionNode expressionNode = this.m_parent;
        if (expressionNode == null) {
            return null;
        }
        return expressionNode.getPublicId();
    }

    public String getSystemId() {
        ExpressionNode expressionNode = this.m_parent;
        if (expressionNode == null) {
            return null;
        }
        return expressionNode.getSystemId();
    }

    public int getLineNumber() {
        ExpressionNode expressionNode = this.m_parent;
        if (expressionNode == null) {
            return 0;
        }
        return expressionNode.getLineNumber();
    }

    public int getColumnNumber() {
        ExpressionNode expressionNode = this.m_parent;
        if (expressionNode == null) {
            return 0;
        }
        return expressionNode.getColumnNumber();
    }
}
