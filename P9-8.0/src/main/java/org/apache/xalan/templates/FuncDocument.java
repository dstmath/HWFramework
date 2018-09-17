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
        int context = xctxt.getCurrentNode();
        int docContext = xctxt.getDTM(context).getDocumentRoot(context);
        XObject arg = getArg0().execute(xctxt);
        String base = "";
        Expression arg1Expr = getArg1();
        if (arg1Expr != null) {
            XObject arg2 = arg1Expr.execute(xctxt);
            if (4 == arg2.getType()) {
                int baseNode = arg2.iter().nextNode();
                if (baseNode == -1) {
                    warn(xctxt, XSLTErrorResources.WG_EMPTY_SECOND_ARG, null);
                    return new XNodeSet(xctxt.getDTMManager());
                }
                base = xctxt.getDTM(baseNode).getDocumentBaseURI();
            } else {
                arg2.iter();
            }
        } else {
            assertion(xctxt.getNamespaceContext() != null, "Namespace context can not be null!");
            base = xctxt.getNamespaceContext().getBaseIdentifier();
        }
        XNodeSet xNodeSet = new XNodeSet(xctxt.getDTMManager());
        NodeSetDTM mnl = xNodeSet.mutableNodeset();
        DTMIterator iterator = 4 == arg.getType() ? arg.iter() : null;
        int pos = -1;
        while (true) {
            if (iterator != null) {
                pos = iterator.nextNode();
                if (-1 == pos) {
                    break;
                }
            }
            XMLString ref = iterator != null ? xctxt.getDTM(pos).getStringValue(pos) : arg.xstr();
            if (arg1Expr == null && -1 != pos) {
                base = xctxt.getDTM(pos).getDocumentBaseURI();
            }
            if (ref != null) {
                if (-1 == docContext) {
                    error(xctxt, XSLTErrorResources.ER_NO_CONTEXT_OWNERDOC, null);
                }
                int indexOfColon = ref.indexOf(58);
                int indexOfSlash = ref.indexOf(47);
                if (!(indexOfColon == -1 || indexOfSlash == -1 || indexOfColon >= indexOfSlash)) {
                    base = null;
                }
                int newDoc = getDoc(xctxt, context, ref.toString(), base);
                if (!(-1 == newDoc || mnl.contains(newDoc))) {
                    mnl.addElement(newDoc);
                }
                if (iterator == null || newDoc == -1) {
                    break;
                }
            }
        }
        return xNodeSet;
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x007a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int getDoc(XPathContext xctxt, int context, String uri, String base) throws TransformerException {
        SourceTreeManager treeMgr = xctxt.getSourceTreeManager();
        try {
            Source source = treeMgr.resolveURI(base, uri, xctxt.getSAXLocator());
            int newDoc = treeMgr.getNode(source);
            if (-1 != newDoc) {
                return newDoc;
            }
            String str;
            String[] strArr;
            String stringBuilder;
            if (uri.length() == 0) {
                uri = xctxt.getNamespaceContext().getBaseIdentifier();
                try {
                    source = treeMgr.resolveURI(base, uri, xctxt.getSAXLocator());
                } catch (IOException ioe) {
                    throw new TransformerException(ioe.getMessage(), xctxt.getSAXLocator(), ioe);
                }
            }
            String diagnosticsString = null;
            if (uri != null) {
                try {
                    if (uri.length() > 0) {
                        newDoc = treeMgr.getSourceTree(source, xctxt.getSAXLocator(), xctxt);
                        if (-1 == newDoc) {
                            if (diagnosticsString != null) {
                                warn(xctxt, XSLTErrorResources.WG_CANNOT_LOAD_REQUESTED_DOC, new Object[]{diagnosticsString});
                            } else {
                                str = XSLTErrorResources.WG_CANNOT_LOAD_REQUESTED_DOC;
                                strArr = new Object[1];
                                if (uri == null) {
                                    StringBuilder stringBuilder2 = new StringBuilder();
                                    if (base == null) {
                                        base = "";
                                    }
                                    stringBuilder = stringBuilder2.append(base).append(uri).toString();
                                } else {
                                    stringBuilder = uri.toString();
                                }
                                strArr[0] = stringBuilder;
                                warn(xctxt, str, strArr);
                            }
                        }
                        return newDoc;
                    }
                } catch (Throwable th) {
                    Throwable throwable = th;
                    newDoc = -1;
                    while (throwable instanceof WrappedRuntimeException) {
                        throwable = ((WrappedRuntimeException) throwable).getException();
                    }
                    if ((throwable instanceof NullPointerException) || (throwable instanceof ClassCastException)) {
                        WrappedRuntimeException wrappedRuntimeException = new WrappedRuntimeException((Exception) throwable);
                    } else {
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
                                    if (!(locator == null || locator.getSystemId() == null)) {
                                        diagnosticsWriter.println("   ID: " + locator.getSystemId() + " Line #" + locator.getLineNumber() + " Column #" + locator.getColumnNumber());
                                    }
                                    e = spe2.getException();
                                    if (e instanceof WrappedRuntimeException) {
                                        e = ((WrappedRuntimeException) e).getException();
                                    }
                                } else {
                                    e = null;
                                }
                            }
                        } else {
                            diagnosticsWriter.println(" (" + throwable.getClass().getName() + "): " + throwable.getMessage());
                        }
                        diagnosticsString = throwable.getMessage();
                    }
                }
            }
            str = "WG_CANNOT_MAKE_URL_FROM";
            strArr = new Object[1];
            StringBuilder stringBuilder3 = new StringBuilder();
            if (base == null) {
                stringBuilder = "";
            } else {
                stringBuilder = base;
            }
            strArr[0] = stringBuilder3.append(stringBuilder).append(uri).toString();
            warn(xctxt, str, strArr);
            if (-1 == newDoc) {
            }
            return newDoc;
        } catch (IOException ioe2) {
            throw new TransformerException(ioe2.getMessage(), xctxt.getSAXLocator(), ioe2);
        } catch (TransformerException te) {
            throw new TransformerException(te);
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

    protected void reportWrongNumberArgs() throws WrongNumberArgsException {
        throw new WrongNumberArgsException(XSLMessages.createMessage(XSLTErrorResources.ER_ONE_OR_TWO, null));
    }

    public boolean isNodesetExpr() {
        return true;
    }
}
