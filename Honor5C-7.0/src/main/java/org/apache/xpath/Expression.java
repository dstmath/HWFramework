package org.apache.xpath;

import java.io.Serializable;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.utils.XMLString;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.res.XPATHMessages;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public abstract class Expression implements Serializable, ExpressionNode, XPathVisitable {
    static final long serialVersionUID = 565665869777906902L;
    private ExpressionNode m_parent;

    public abstract boolean deepEquals(Expression expression);

    public abstract XObject execute(XPathContext xPathContext) throws TransformerException;

    public abstract void fixupVariables(Vector vector, int i);

    public boolean canTraverseOutsideSubtree() {
        return false;
    }

    public XObject execute(XPathContext xctxt, int currentNode) throws TransformerException {
        return execute(xctxt);
    }

    public XObject execute(XPathContext xctxt, int currentNode, DTM dtm, int expType) throws TransformerException {
        return execute(xctxt);
    }

    public XObject execute(XPathContext xctxt, boolean destructiveOK) throws TransformerException {
        return execute(xctxt);
    }

    public double num(XPathContext xctxt) throws TransformerException {
        return execute(xctxt).num();
    }

    public boolean bool(XPathContext xctxt) throws TransformerException {
        return execute(xctxt).bool();
    }

    public XMLString xstr(XPathContext xctxt) throws TransformerException {
        return execute(xctxt).xstr();
    }

    public boolean isNodesetExpr() {
        return false;
    }

    public int asNode(XPathContext xctxt) throws TransformerException {
        return execute(xctxt).iter().nextNode();
    }

    public DTMIterator asIterator(XPathContext xctxt, int contextNode) throws TransformerException {
        try {
            xctxt.pushCurrentNodeAndExpression(contextNode, contextNode);
            DTMIterator iter = execute(xctxt).iter();
            return iter;
        } finally {
            xctxt.popCurrentNodeAndExpression();
        }
    }

    public DTMIterator asIteratorRaw(XPathContext xctxt, int contextNode) throws TransformerException {
        try {
            xctxt.pushCurrentNodeAndExpression(contextNode, contextNode);
            DTMIterator iterRaw = ((XNodeSet) execute(xctxt)).iterRaw();
            return iterRaw;
        } finally {
            xctxt.popCurrentNodeAndExpression();
        }
    }

    public void executeCharsToContentHandler(XPathContext xctxt, ContentHandler handler) throws TransformerException, SAXException {
        XObject obj = execute(xctxt);
        obj.dispatchCharactersEvents(handler);
        obj.detach();
    }

    public boolean isStableNumber() {
        return false;
    }

    protected final boolean isSameClass(Expression expr) {
        boolean z = false;
        if (expr == null) {
            return false;
        }
        if (getClass() == expr.getClass()) {
            z = true;
        }
        return z;
    }

    public void warn(XPathContext xctxt, String msg, Object[] args) throws TransformerException {
        String fmsg = XPATHMessages.createXPATHWarning(msg, args);
        if (xctxt != null) {
            xctxt.getErrorListener().warning(new TransformerException(fmsg, xctxt.getSAXLocator()));
        }
    }

    public void assertion(boolean b, String msg) {
        if (!b) {
            throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_INCORRECT_PROGRAMMER_ASSERTION, new Object[]{msg}));
        }
    }

    public void error(XPathContext xctxt, String msg, Object[] args) throws TransformerException {
        String fmsg = XPATHMessages.createXPATHMessage(msg, args);
        if (xctxt != null) {
            xctxt.getErrorListener().fatalError(new TransformerException(fmsg, this));
        }
    }

    public ExpressionNode getExpressionOwner() {
        ExpressionNode parent = exprGetParent();
        while (parent != null && (parent instanceof Expression)) {
            parent = parent.exprGetParent();
        }
        return parent;
    }

    public void exprSetParent(ExpressionNode n) {
        assertion(n != this, "Can not parent an expression to itself!");
        this.m_parent = n;
    }

    public ExpressionNode exprGetParent() {
        return this.m_parent;
    }

    public void exprAddChild(ExpressionNode n, int i) {
        assertion(false, "exprAddChild method not implemented!");
    }

    public ExpressionNode exprGetChild(int i) {
        return null;
    }

    public int exprGetNumChildren() {
        return 0;
    }

    public String getPublicId() {
        if (this.m_parent == null) {
            return null;
        }
        return this.m_parent.getPublicId();
    }

    public String getSystemId() {
        if (this.m_parent == null) {
            return null;
        }
        return this.m_parent.getSystemId();
    }

    public int getLineNumber() {
        if (this.m_parent == null) {
            return 0;
        }
        return this.m_parent.getLineNumber();
    }

    public int getColumnNumber() {
        if (this.m_parent == null) {
            return 0;
        }
        return this.m_parent.getColumnNumber();
    }
}
