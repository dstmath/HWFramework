package org.apache.xpath;

import java.io.PrintStream;
import java.io.PrintWriter;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Node;

public class XPathException extends TransformerException {
    static final long serialVersionUID = 4263549717619045963L;
    protected Exception m_exception;
    Object m_styleNode = null;

    public Object getStylesheetNode() {
        return this.m_styleNode;
    }

    public void setStylesheetNode(Object styleNode) {
        this.m_styleNode = styleNode;
    }

    public XPathException(String message, ExpressionNode ex) {
        super(message);
        setLocator(ex);
        setStylesheetNode(getStylesheetNode(ex));
    }

    public XPathException(String message) {
        super(message);
    }

    public Node getStylesheetNode(ExpressionNode ex) {
        ExpressionNode owner = getExpressionOwner(ex);
        if (owner == null || !(owner instanceof Node)) {
            return null;
        }
        return (Node) owner;
    }

    protected ExpressionNode getExpressionOwner(ExpressionNode ex) {
        ExpressionNode parent = ex.exprGetParent();
        while (parent != null && (parent instanceof Expression)) {
            parent = parent.exprGetParent();
        }
        return parent;
    }

    public XPathException(String message, Object styleNode) {
        super(message);
        this.m_styleNode = styleNode;
    }

    public XPathException(String message, Node styleNode, Exception e) {
        super(message);
        this.m_styleNode = styleNode;
        this.m_exception = e;
    }

    public XPathException(String message, Exception e) {
        super(message);
        this.m_exception = e;
    }

    public void printStackTrace(PrintStream s) {
        if (s == null) {
            s = System.err;
        }
        try {
            super.printStackTrace(s);
        } catch (Exception e) {
        }
        Throwable exception = this.m_exception;
        for (int i = 0; i < 10 && exception != null; i++) {
            s.println("---------");
            exception.printStackTrace(s);
            if (exception instanceof TransformerException) {
                Throwable prev = exception;
                exception = ((TransformerException) exception).getException();
                if (prev == exception) {
                    return;
                }
            } else {
                exception = null;
            }
        }
    }

    public String getMessage() {
        String lastMessage = super.getMessage();
        Throwable exception = this.m_exception;
        while (exception != null) {
            String nextMessage = exception.getMessage();
            if (nextMessage != null) {
                lastMessage = nextMessage;
            }
            if (exception instanceof TransformerException) {
                Throwable prev = exception;
                exception = ((TransformerException) exception).getException();
                if (prev == exception) {
                    break;
                }
            } else {
                exception = null;
            }
        }
        return lastMessage != null ? lastMessage : "";
    }

    public void printStackTrace(PrintWriter s) {
        if (s == null) {
            s = new PrintWriter(System.err);
        }
        try {
            super.printStackTrace(s);
        } catch (Exception e) {
        }
        boolean isJdk14OrHigher = false;
        try {
            Throwable.class.getMethod("getCause", new Class[]{(Class) null});
            isJdk14OrHigher = true;
        } catch (NoSuchMethodException e2) {
        }
        if (!isJdk14OrHigher) {
            Throwable exception = this.m_exception;
            for (int i = 0; i < 10 && exception != null; i++) {
                s.println("---------");
                try {
                    exception.printStackTrace(s);
                } catch (Exception e3) {
                    s.println("Could not print stack trace...");
                }
                if (exception instanceof TransformerException) {
                    Throwable prev = exception;
                    exception = ((TransformerException) exception).getException();
                    if (prev == exception) {
                        return;
                    }
                } else {
                    exception = null;
                }
            }
        }
    }

    public Throwable getException() {
        return this.m_exception;
    }
}
