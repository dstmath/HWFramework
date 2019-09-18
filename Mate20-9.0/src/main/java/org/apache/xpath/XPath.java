package org.apache.xpath;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Vector;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xml.utils.DefaultErrorHandler;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.SAXSourceLocator;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.compiler.FunctionTable;
import org.apache.xpath.compiler.XPathParser;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.res.XPATHErrorResources;
import org.w3c.dom.Node;

public class XPath implements Serializable, ExpressionOwner {
    private static final boolean DEBUG_MATCHES = false;
    public static final int MATCH = 1;
    public static final double MATCH_SCORE_NODETEST = -0.5d;
    public static final double MATCH_SCORE_NONE = Double.NEGATIVE_INFINITY;
    public static final double MATCH_SCORE_NSWILD = -0.25d;
    public static final double MATCH_SCORE_OTHER = 0.5d;
    public static final double MATCH_SCORE_QNAME = 0.0d;
    public static final int SELECT = 0;
    static final long serialVersionUID = 3976493477939110553L;
    private transient FunctionTable m_funcTable;
    private Expression m_mainExp;
    String m_patternString;

    private void initFunctionTable() {
        this.m_funcTable = new FunctionTable();
    }

    public Expression getExpression() {
        return this.m_mainExp;
    }

    public void fixupVariables(Vector vars, int globalsSize) {
        this.m_mainExp.fixupVariables(vars, globalsSize);
    }

    public void setExpression(Expression exp) {
        if (this.m_mainExp != null) {
            exp.exprSetParent(this.m_mainExp.exprGetParent());
        }
        this.m_mainExp = exp;
    }

    public SourceLocator getLocator() {
        return this.m_mainExp;
    }

    public String getPatternString() {
        return this.m_patternString;
    }

    public XPath(String exprString, SourceLocator locator, PrefixResolver prefixResolver, int type, ErrorListener errorListener) throws TransformerException {
        this.m_funcTable = null;
        initFunctionTable();
        errorListener = errorListener == null ? new DefaultErrorHandler() : errorListener;
        this.m_patternString = exprString;
        XPathParser parser = new XPathParser(errorListener, locator);
        Compiler compiler = new Compiler(errorListener, locator, this.m_funcTable);
        if (type == 0) {
            parser.initXPath(compiler, exprString, prefixResolver);
        } else if (1 == type) {
            parser.initMatchPattern(compiler, exprString, prefixResolver);
        } else {
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_CANNOT_DEAL_XPATH_TYPE, new Object[]{Integer.toString(type)}));
        }
        Expression expr = compiler.compile(0);
        setExpression(expr);
        if (locator != null && (locator instanceof ExpressionNode)) {
            expr.exprSetParent((ExpressionNode) locator);
        }
    }

    public XPath(String exprString, SourceLocator locator, PrefixResolver prefixResolver, int type, ErrorListener errorListener, FunctionTable aTable) throws TransformerException {
        this.m_funcTable = null;
        this.m_funcTable = aTable;
        errorListener = errorListener == null ? new DefaultErrorHandler() : errorListener;
        this.m_patternString = exprString;
        XPathParser parser = new XPathParser(errorListener, locator);
        Compiler compiler = new Compiler(errorListener, locator, this.m_funcTable);
        if (type == 0) {
            parser.initXPath(compiler, exprString, prefixResolver);
        } else if (1 == type) {
            parser.initMatchPattern(compiler, exprString, prefixResolver);
        } else {
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_CANNOT_DEAL_XPATH_TYPE, new Object[]{Integer.toString(type)}));
        }
        Expression expr = compiler.compile(0);
        setExpression(expr);
        if (locator != null && (locator instanceof ExpressionNode)) {
            expr.exprSetParent((ExpressionNode) locator);
        }
    }

    public XPath(String exprString, SourceLocator locator, PrefixResolver prefixResolver, int type) throws TransformerException {
        this(exprString, locator, prefixResolver, type, null);
    }

    public XPath(Expression expr) {
        this.m_funcTable = null;
        setExpression(expr);
        initFunctionTable();
    }

    public XObject execute(XPathContext xctxt, Node contextNode, PrefixResolver namespaceContext) throws TransformerException {
        return execute(xctxt, xctxt.getDTMHandleFromNode(contextNode), namespaceContext);
    }

    public XObject execute(XPathContext xctxt, int contextNode, PrefixResolver namespaceContext) throws TransformerException {
        xctxt.pushNamespaceContext(namespaceContext);
        xctxt.pushCurrentNodeAndExpression(contextNode, contextNode);
        XObject xobj = null;
        try {
            xobj = this.m_mainExp.execute(xctxt);
        } catch (TransformerException te) {
            te.setLocator(getLocator());
            ErrorListener el = xctxt.getErrorListener();
            if (el != null) {
                el.error(te);
            } else {
                throw te;
            }
        } catch (Exception e) {
            e = e;
            while (e instanceof WrappedRuntimeException) {
                e = ((WrappedRuntimeException) e).getException();
            }
            String msg = e.getMessage();
            if (msg == null || msg.length() == 0) {
                msg = XSLMessages.createXPATHMessage(XPATHErrorResources.ER_XPATH_ERROR, null);
            }
            TransformerException te2 = new TransformerException(msg, getLocator(), e);
            ErrorListener el2 = xctxt.getErrorListener();
            if (el2 != null) {
                el2.fatalError(te2);
            } else {
                throw te2;
            }
        } catch (Throwable th) {
            xctxt.popNamespaceContext();
            xctxt.popCurrentNodeAndExpression();
            throw th;
        }
        xctxt.popNamespaceContext();
        xctxt.popCurrentNodeAndExpression();
        return xobj;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005c, code lost:
        r5.popNamespaceContext();
        r5.popCurrentNodeAndExpression();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0064, code lost:
        return false;
     */
    public boolean bool(XPathContext xctxt, int contextNode, PrefixResolver namespaceContext) throws TransformerException {
        xctxt.pushNamespaceContext(namespaceContext);
        xctxt.pushCurrentNodeAndExpression(contextNode, contextNode);
        try {
            boolean bool = this.m_mainExp.bool(xctxt);
            xctxt.popNamespaceContext();
            xctxt.popCurrentNodeAndExpression();
            return bool;
        } catch (TransformerException te) {
            te.setLocator(getLocator());
            ErrorListener el = xctxt.getErrorListener();
            if (el != null) {
                el.error(te);
            } else {
                throw te;
            }
        } catch (Exception e) {
            e = e;
            while (e instanceof WrappedRuntimeException) {
                e = ((WrappedRuntimeException) e).getException();
            }
            String msg = e.getMessage();
            if (msg == null || msg.length() == 0) {
                msg = XSLMessages.createXPATHMessage(XPATHErrorResources.ER_XPATH_ERROR, null);
            }
            TransformerException te2 = new TransformerException(msg, getLocator(), e);
            ErrorListener el2 = xctxt.getErrorListener();
            if (el2 != null) {
                el2.fatalError(te2);
            } else {
                throw te2;
            }
        } catch (Throwable th) {
            xctxt.popNamespaceContext();
            xctxt.popCurrentNodeAndExpression();
            throw th;
        }
    }

    public double getMatchScore(XPathContext xctxt, int context) throws TransformerException {
        xctxt.pushCurrentNode(context);
        xctxt.pushCurrentExpressionNode(context);
        try {
            return this.m_mainExp.execute(xctxt).num();
        } finally {
            xctxt.popCurrentNode();
            xctxt.popCurrentExpressionNode();
        }
    }

    public void warn(XPathContext xctxt, int sourceNode, String msg, Object[] args) throws TransformerException {
        String fmsg = XSLMessages.createXPATHWarning(msg, args);
        ErrorListener ehandler = xctxt.getErrorListener();
        if (ehandler != null) {
            ehandler.warning(new TransformerException(fmsg, (SAXSourceLocator) xctxt.getSAXLocator()));
        }
    }

    public void assertion(boolean b, String msg) {
        if (!b) {
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_INCORRECT_PROGRAMMER_ASSERTION, new Object[]{msg}));
        }
    }

    public void error(XPathContext xctxt, int sourceNode, String msg, Object[] args) throws TransformerException {
        String fmsg = XSLMessages.createXPATHMessage(msg, args);
        ErrorListener ehandler = xctxt.getErrorListener();
        if (ehandler != null) {
            ehandler.fatalError(new TransformerException(fmsg, (SAXSourceLocator) xctxt.getSAXLocator()));
            return;
        }
        SourceLocator slocator = xctxt.getSAXLocator();
        PrintStream printStream = System.out;
        printStream.println(fmsg + "; file " + slocator.getSystemId() + "; line " + slocator.getLineNumber() + "; column " + slocator.getColumnNumber());
    }

    public void callVisitors(ExpressionOwner owner, XPathVisitor visitor) {
        this.m_mainExp.callVisitors(this, visitor);
    }
}
