package org.apache.xalan.templates;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xml.utils.XMLString;
import org.apache.xpath.Expression;
import org.apache.xpath.NodeSetDTM;
import org.apache.xpath.SourceTreeManager;
import org.apache.xpath.XPathContext;
import org.apache.xpath.functions.Function2Args;
import org.apache.xpath.functions.WrongNumberArgsException;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XObject;

public class FuncDocument extends Function2Args {
    static final long serialVersionUID = 2483304325971281424L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        FuncDocument funcDocument = this;
        XPathContext xPathContext = xctxt;
        int context = xctxt.getCurrentNode();
        int docContext = xPathContext.getDTM(context).getDocumentRoot(context);
        XObject arg = getArg0().execute(xPathContext);
        String base = "";
        Expression arg1Expr = getArg1();
        Object[] objArr = null;
        int i = -1;
        if (arg1Expr != null) {
            XObject arg2 = arg1Expr.execute(xPathContext);
            if (4 == arg2.getType()) {
                int baseNode = arg2.iter().nextNode();
                if (baseNode == -1) {
                    funcDocument.warn(xPathContext, XSLTErrorResources.WG_EMPTY_SECOND_ARG, null);
                    return new XNodeSet(xctxt.getDTMManager());
                }
                base = xPathContext.getDTM(baseNode).getDocumentBaseURI();
            } else {
                arg2.iter();
            }
        } else {
            funcDocument.assertion(xctxt.getNamespaceContext() != null, "Namespace context can not be null!");
            base = xctxt.getNamespaceContext().getBaseIdentifier();
        }
        XNodeSet nodes = new XNodeSet(xctxt.getDTMManager());
        NodeSetDTM mnl = nodes.mutableNodeset();
        DTMIterator iterator = 4 == arg.getType() ? arg.iter() : null;
        String base2 = base;
        int pos = -1;
        while (true) {
            if (iterator != null) {
                int nextNode = iterator.nextNode();
                pos = nextNode;
                if (i == nextNode) {
                    break;
                }
            }
            XMLString ref = iterator != null ? xPathContext.getDTM(pos).getStringValue(pos) : arg.xstr();
            if (arg1Expr == null && i != pos) {
                base2 = xPathContext.getDTM(pos).getDocumentBaseURI();
            }
            if (ref != null) {
                if (i == docContext) {
                    funcDocument.error(xPathContext, XSLTErrorResources.ER_NO_CONTEXT_OWNERDOC, objArr);
                }
                int indexOfColon = ref.indexOf(58);
                int indexOfSlash = ref.indexOf(47);
                if (!(indexOfColon == i || indexOfSlash == i || indexOfColon >= indexOfSlash)) {
                    base2 = null;
                }
                int newDoc = funcDocument.getDoc(xPathContext, context, ref.toString(), base2);
                if (-1 != newDoc && !mnl.contains(newDoc)) {
                    mnl.addElement(newDoc);
                }
                if (iterator == null || newDoc == -1) {
                    break;
                }
                i = -1;
                funcDocument = this;
                objArr = null;
            }
        }
        return nodes;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0172  */
    public int getDoc(XPathContext xctxt, int context, String uri, String base) throws TransformerException {
        String str;
        SourceTreeManager treeMgr;
        XPathContext xPathContext = xctxt;
        String str2 = base;
        SourceTreeManager treeMgr2 = xctxt.getSourceTreeManager();
        try {
            String uri2 = uri;
            try {
                Source source = treeMgr2.resolveURI(str2, uri2, xctxt.getSAXLocator());
                int newDoc = treeMgr2.getNode(source);
                if (-1 != newDoc) {
                    return newDoc;
                }
                if (uri.length() == 0) {
                    uri2 = xctxt.getNamespaceContext().getBaseIdentifier();
                    try {
                        source = treeMgr2.resolveURI(str2, uri2, xctxt.getSAXLocator());
                    } catch (IOException ioe) {
                        throw new TransformerException(ioe.getMessage(), xctxt.getSAXLocator(), ioe);
                    }
                }
                String diagnosticsString = null;
                if (uri2 != null) {
                    try {
                        if (uri2.length() > 0) {
                            newDoc = treeMgr2.getSourceTree(source, xctxt.getSAXLocator(), xPathContext);
                            SourceTreeManager sourceTreeManager = treeMgr2;
                            if (-1 == newDoc) {
                                if (diagnosticsString != null) {
                                    warn(xPathContext, XSLTErrorResources.WG_CANNOT_LOAD_REQUESTED_DOC, new Object[]{diagnosticsString});
                                } else {
                                    Object[] objArr = new Object[1];
                                    if (uri2 == null) {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(str2 == null ? "" : str2);
                                        sb.append(uri2);
                                        str = sb.toString();
                                    } else {
                                        str = uri2.toString();
                                    }
                                    objArr[0] = str;
                                    warn(xPathContext, XSLTErrorResources.WG_CANNOT_LOAD_REQUESTED_DOC, objArr);
                                }
                            }
                            return newDoc;
                        }
                    } catch (Throwable th) {
                        throwable = th;
                        newDoc = -1;
                        while (throwable instanceof WrappedRuntimeException) {
                            throwable = ((WrappedRuntimeException) throwable).getException();
                        }
                        if ((throwable instanceof NullPointerException) || (throwable instanceof ClassCastException)) {
                            throw new WrappedRuntimeException((Exception) throwable);
                        }
                        PrintWriter diagnosticsWriter = new PrintWriter(new StringWriter());
                        if (throwable instanceof TransformerException) {
                            Throwable e = (TransformerException) throwable;
                            while (e != null) {
                                if (e.getMessage() != null) {
                                    diagnosticsWriter.println(" (" + e.getClass().getName() + "): " + e.getMessage());
                                }
                                if (e instanceof TransformerException) {
                                    TransformerException spe2 = (TransformerException) e;
                                    SourceLocator locator = spe2.getLocator();
                                    if (locator == null || locator.getSystemId() == null) {
                                        treeMgr = treeMgr2;
                                    } else {
                                        StringBuilder sb2 = new StringBuilder();
                                        treeMgr = treeMgr2;
                                        sb2.append("   ID: ");
                                        sb2.append(locator.getSystemId());
                                        sb2.append(" Line #");
                                        sb2.append(locator.getLineNumber());
                                        sb2.append(" Column #");
                                        sb2.append(locator.getColumnNumber());
                                        diagnosticsWriter.println(sb2.toString());
                                    }
                                    Throwable e2 = spe2.getException();
                                    boolean z = e2 instanceof WrappedRuntimeException;
                                    Throwable e3 = e2;
                                    if (z) {
                                        e3 = ((WrappedRuntimeException) e2).getException();
                                    }
                                    e = e3;
                                    treeMgr2 = treeMgr;
                                } else {
                                    e = null;
                                }
                            }
                            SourceTreeManager sourceTreeManager2 = treeMgr2;
                        } else {
                            diagnosticsWriter.println(" (" + throwable.getClass().getName() + "): " + throwable.getMessage());
                        }
                        diagnosticsString = throwable.getMessage();
                    }
                }
                Object[] objArr2 = new Object[1];
                StringBuilder sb3 = new StringBuilder();
                sb3.append(str2 == null ? "" : str2);
                sb3.append(uri2);
                objArr2[0] = sb3.toString();
                warn(xPathContext, "WG_CANNOT_MAKE_URL_FROM", objArr2);
                SourceTreeManager sourceTreeManager3 = treeMgr2;
                if (-1 == newDoc) {
                }
                return newDoc;
            } catch (IOException e4) {
                ioe = e4;
                SourceTreeManager sourceTreeManager4 = treeMgr2;
                throw new TransformerException(ioe.getMessage(), xctxt.getSAXLocator(), ioe);
            } catch (TransformerException e5) {
                throwable = e5;
                SourceTreeManager sourceTreeManager5 = treeMgr2;
                throw new TransformerException(throwable);
            }
        } catch (IOException e6) {
            ioe = e6;
            String str3 = uri;
            SourceTreeManager sourceTreeManager42 = treeMgr2;
            throw new TransformerException(ioe.getMessage(), xctxt.getSAXLocator(), ioe);
        } catch (TransformerException e7) {
            throwable = e7;
            String str4 = uri;
            SourceTreeManager sourceTreeManager52 = treeMgr2;
            throw new TransformerException(throwable);
        }
    }

    public void error(XPathContext xctxt, String msg, Object[] args) throws TransformerException {
        String formattedMsg = XSLMessages.createMessage(msg, args);
        ErrorListener errHandler = xctxt.getErrorListener();
        TransformerException spe = new TransformerException(formattedMsg, xctxt.getSAXLocator());
        if (errHandler != null) {
            errHandler.error(spe);
        } else {
            System.out.println(formattedMsg);
        }
    }

    public void warn(XPathContext xctxt, String msg, Object[] args) throws TransformerException {
        String formattedMsg = XSLMessages.createWarning(msg, args);
        ErrorListener errHandler = xctxt.getErrorListener();
        TransformerException spe = new TransformerException(formattedMsg, xctxt.getSAXLocator());
        if (errHandler != null) {
            errHandler.warning(spe);
        } else {
            System.out.println(formattedMsg);
        }
    }

    public void checkNumberArgs(int argNum) throws WrongNumberArgsException {
        if (argNum < 1 || argNum > 2) {
            reportWrongNumberArgs();
        }
    }

    /* access modifiers changed from: protected */
    public void reportWrongNumberArgs() throws WrongNumberArgsException {
        throw new WrongNumberArgsException(XSLMessages.createMessage(XSLTErrorResources.ER_ONE_OR_TWO, null));
    }

    public boolean isNodesetExpr() {
        return true;
    }
}
