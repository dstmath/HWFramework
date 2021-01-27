package ohos.com.sun.org.apache.xpath.internal;

import java.io.PrintStream;
import java.io.PrintWriter;
import ohos.javax.xml.transform.TransformerException;
import ohos.org.w3c.dom.Node;

public class XPathException extends TransformerException {
    static final long serialVersionUID = 4263549717619045963L;
    protected Exception m_exception;
    Object m_styleNode = null;

    public Object getStylesheetNode() {
        return this.m_styleNode;
    }

    public void setStylesheetNode(Object obj) {
        this.m_styleNode = obj;
    }

    public XPathException(String str, ExpressionNode expressionNode) {
        super(str);
        setLocator(expressionNode);
        setStylesheetNode(getStylesheetNode(expressionNode));
    }

    public XPathException(String str) {
        super(str);
    }

    public Node getStylesheetNode(ExpressionNode expressionNode) {
        Node expressionOwner = getExpressionOwner(expressionNode);
        if (expressionOwner == null || !(expressionOwner instanceof Node)) {
            return null;
        }
        return expressionOwner;
    }

    /* access modifiers changed from: protected */
    public ExpressionNode getExpressionOwner(ExpressionNode expressionNode) {
        ExpressionNode exprGetParent = expressionNode.exprGetParent();
        while (exprGetParent != null && (exprGetParent instanceof Expression)) {
            exprGetParent = exprGetParent.exprGetParent();
        }
        return exprGetParent;
    }

    public XPathException(String str, Object obj) {
        super(str);
        this.m_styleNode = obj;
    }

    public XPathException(String str, Node node, Exception exc) {
        super(str);
        this.m_styleNode = node;
        this.m_exception = exc;
    }

    public XPathException(String str, Exception exc) {
        super(str);
        this.m_exception = exc;
    }

    public void printStackTrace(PrintStream printStream) {
        if (printStream == null) {
            printStream = System.err;
        }
        try {
            XPathException.super.printStackTrace(printStream);
        } catch (Exception unused) {
        }
        Throwable th = this.m_exception;
        for (int i = 0; i < 10 && th != null; i++) {
            printStream.println("---------");
            th.printStackTrace(printStream);
            if (th instanceof TransformerException) {
                Throwable exception = ((TransformerException) th).getException();
                if (th != exception) {
                    th = exception;
                } else {
                    return;
                }
            } else {
                th = null;
            }
        }
    }

    public String getMessage() {
        String message = XPathException.super.getMessage();
        Throwable th = this.m_exception;
        while (th != null) {
            String message2 = th.getMessage();
            if (message2 != null) {
                message = message2;
            }
            if (th instanceof TransformerException) {
                Throwable exception = ((TransformerException) th).getException();
                if (th == exception) {
                    break;
                }
                th = exception;
            } else {
                th = null;
            }
        }
        return message != null ? message : "";
    }

    public void printStackTrace(PrintWriter printWriter) {
        boolean z;
        if (printWriter == null) {
            printWriter = new PrintWriter(System.err);
        }
        try {
            XPathException.super.printStackTrace(printWriter);
        } catch (Exception unused) {
        }
        try {
            Throwable.class.getMethod("getCause", null);
            z = true;
        } catch (NoSuchMethodException unused2) {
            z = false;
        }
        if (!z) {
            Throwable th = this.m_exception;
            for (int i = 0; i < 10 && th != null; i++) {
                printWriter.println("---------");
                try {
                    th.printStackTrace(printWriter);
                } catch (Exception unused3) {
                    printWriter.println("Could not print stack trace...");
                }
                if (th instanceof TransformerException) {
                    Throwable exception = ((TransformerException) th).getException();
                    if (th != exception) {
                        th = exception;
                    } else {
                        return;
                    }
                } else {
                    th = null;
                }
            }
        }
    }

    public Throwable getException() {
        return this.m_exception;
    }
}
