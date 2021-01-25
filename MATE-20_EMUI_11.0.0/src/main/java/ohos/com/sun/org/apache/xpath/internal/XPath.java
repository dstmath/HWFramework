package ohos.com.sun.org.apache.xpath.internal;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xml.internal.utils.DefaultErrorHandler;
import ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver;
import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;
import ohos.com.sun.org.apache.xpath.internal.compiler.Compiler;
import ohos.com.sun.org.apache.xpath.internal.compiler.FunctionTable;
import ohos.com.sun.org.apache.xpath.internal.compiler.XPathParser;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.ErrorListener;
import ohos.javax.xml.transform.SourceLocator;
import ohos.javax.xml.transform.TransformerException;
import ohos.org.w3c.dom.Node;

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

    @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
    public Expression getExpression() {
        return this.m_mainExp;
    }

    public void fixupVariables(Vector vector, int i) {
        this.m_mainExp.fixupVariables(vector, i);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
    public void setExpression(Expression expression) {
        Expression expression2 = this.m_mainExp;
        if (expression2 != null) {
            expression.exprSetParent(expression2.exprGetParent());
        }
        this.m_mainExp = expression;
    }

    public SourceLocator getLocator() {
        return this.m_mainExp;
    }

    public String getPatternString() {
        return this.m_patternString;
    }

    public XPath(String str, SourceLocator sourceLocator, PrefixResolver prefixResolver, int i, ErrorListener errorListener) throws TransformerException {
        this.m_funcTable = null;
        initFunctionTable();
        errorListener = errorListener == null ? new DefaultErrorHandler() : errorListener;
        this.m_patternString = str;
        XPathParser xPathParser = new XPathParser(errorListener, sourceLocator);
        Compiler compiler = new Compiler(errorListener, sourceLocator, this.m_funcTable);
        if (i == 0) {
            xPathParser.initXPath(compiler, str, prefixResolver);
        } else if (1 == i) {
            xPathParser.initMatchPattern(compiler, str, prefixResolver);
        } else {
            throw new RuntimeException(XSLMessages.createXPATHMessage("ER_CANNOT_DEAL_XPATH_TYPE", new Object[]{Integer.toString(i)}));
        }
        Expression compileExpression = compiler.compileExpression(0);
        setExpression(compileExpression);
        if (sourceLocator != null && (sourceLocator instanceof ExpressionNode)) {
            compileExpression.exprSetParent((ExpressionNode) sourceLocator);
        }
    }

    public XPath(String str, SourceLocator sourceLocator, PrefixResolver prefixResolver, int i, ErrorListener errorListener, FunctionTable functionTable) throws TransformerException {
        this.m_funcTable = null;
        this.m_funcTable = functionTable;
        errorListener = errorListener == null ? new DefaultErrorHandler() : errorListener;
        this.m_patternString = str;
        XPathParser xPathParser = new XPathParser(errorListener, sourceLocator);
        Compiler compiler = new Compiler(errorListener, sourceLocator, this.m_funcTable);
        if (i == 0) {
            xPathParser.initXPath(compiler, str, prefixResolver);
        } else if (1 == i) {
            xPathParser.initMatchPattern(compiler, str, prefixResolver);
        } else {
            throw new RuntimeException(XSLMessages.createXPATHMessage("ER_CANNOT_DEAL_XPATH_TYPE", new Object[]{Integer.toString(i)}));
        }
        Expression compileExpression = compiler.compileExpression(0);
        setExpression(compileExpression);
        if (sourceLocator != null && (sourceLocator instanceof ExpressionNode)) {
            compileExpression.exprSetParent((ExpressionNode) sourceLocator);
        }
    }

    public XPath(String str, SourceLocator sourceLocator, PrefixResolver prefixResolver, int i) throws TransformerException {
        this(str, sourceLocator, prefixResolver, i, null);
    }

    public XPath(Expression expression) {
        this.m_funcTable = null;
        setExpression(expression);
        initFunctionTable();
    }

    public XObject execute(XPathContext xPathContext, Node node, PrefixResolver prefixResolver) throws TransformerException {
        return execute(xPathContext, xPathContext.getDTMHandleFromNode(node), prefixResolver);
    }

    public XObject execute(XPathContext xPathContext, int i, PrefixResolver prefixResolver) throws TransformerException {
        xPathContext.pushNamespaceContext(prefixResolver);
        xPathContext.pushCurrentNodeAndExpression(i, i);
        XObject xObject = null;
        try {
            xObject = this.m_mainExp.execute(xPathContext);
        } catch (TransformerException e) {
            e.setLocator(getLocator());
            ErrorListener errorListener = xPathContext.getErrorListener();
            if (errorListener != null) {
                errorListener.error(e);
            } else {
                throw e;
            }
        } catch (Exception e2) {
            e = e2;
            while (e instanceof WrappedRuntimeException) {
                e = ((WrappedRuntimeException) e).getException();
            }
            String message = e.getMessage();
            if (message == null || message.length() == 0) {
                message = XSLMessages.createXPATHMessage("ER_XPATH_ERROR", null);
            }
            TransformerException transformerException = new TransformerException(message, getLocator(), e);
            ErrorListener errorListener2 = xPathContext.getErrorListener();
            if (errorListener2 != null) {
                errorListener2.fatalError(transformerException);
            } else {
                throw transformerException;
            }
        } catch (Throwable th) {
            xPathContext.popNamespaceContext();
            xPathContext.popCurrentNodeAndExpression();
            throw th;
        }
        xPathContext.popNamespaceContext();
        xPathContext.popCurrentNodeAndExpression();
        return xObject;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0059, code lost:
        r2.popNamespaceContext();
        r2.popCurrentNodeAndExpression();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0060, code lost:
        return false;
     */
    public boolean bool(XPathContext xPathContext, int i, PrefixResolver prefixResolver) throws TransformerException {
        xPathContext.pushNamespaceContext(prefixResolver);
        xPathContext.pushCurrentNodeAndExpression(i, i);
        try {
            boolean bool = this.m_mainExp.bool(xPathContext);
            xPathContext.popNamespaceContext();
            xPathContext.popCurrentNodeAndExpression();
            return bool;
        } catch (TransformerException e) {
            e.setLocator(this.getLocator());
            ErrorListener errorListener = xPathContext.getErrorListener();
            if (errorListener != null) {
                errorListener.error(e);
            } else {
                throw e;
            }
        } catch (Exception e2) {
            e = e2;
            while (e instanceof WrappedRuntimeException) {
                e = ((WrappedRuntimeException) e).getException();
            }
            String message = e.getMessage();
            if (message == null || message.length() == 0) {
                message = XSLMessages.createXPATHMessage("ER_XPATH_ERROR", null);
            }
            TransformerException transformerException = new TransformerException(message, this.getLocator(), e);
            ErrorListener errorListener2 = xPathContext.getErrorListener();
            if (errorListener2 != null) {
                errorListener2.fatalError(transformerException);
            } else {
                throw transformerException;
            }
        } catch (Throwable th) {
            xPathContext.popNamespaceContext();
            xPathContext.popCurrentNodeAndExpression();
            throw th;
        }
    }

    public double getMatchScore(XPathContext xPathContext, int i) throws TransformerException {
        xPathContext.pushCurrentNode(i);
        xPathContext.pushCurrentExpressionNode(i);
        try {
            return this.m_mainExp.execute(xPathContext).num();
        } finally {
            xPathContext.popCurrentNode();
            xPathContext.popCurrentExpressionNode();
        }
    }

    public void warn(XPathContext xPathContext, int i, String str, Object[] objArr) throws TransformerException {
        String createXPATHWarning = XSLMessages.createXPATHWarning(str, objArr);
        ErrorListener errorListener = xPathContext.getErrorListener();
        if (errorListener != null) {
            errorListener.warning(new TransformerException(createXPATHWarning, xPathContext.getSAXLocator()));
        }
    }

    public void assertion(boolean z, String str) {
        if (!z) {
            throw new RuntimeException(XSLMessages.createXPATHMessage("ER_INCORRECT_PROGRAMMER_ASSERTION", new Object[]{str}));
        }
    }

    public void error(XPathContext xPathContext, int i, String str, Object[] objArr) throws TransformerException {
        String createXPATHMessage = XSLMessages.createXPATHMessage(str, objArr);
        ErrorListener errorListener = xPathContext.getErrorListener();
        if (errorListener != null) {
            errorListener.fatalError(new TransformerException(createXPATHMessage, xPathContext.getSAXLocator()));
            return;
        }
        SourceLocator sAXLocator = xPathContext.getSAXLocator();
        PrintStream printStream = System.out;
        printStream.println(createXPATHMessage + "; file " + sAXLocator.getSystemId() + "; line " + sAXLocator.getLineNumber() + "; column " + sAXLocator.getColumnNumber());
    }

    public void callVisitors(ExpressionOwner expressionOwner, XPathVisitor xPathVisitor) {
        this.m_mainExp.callVisitors(this, xPathVisitor);
    }
}
