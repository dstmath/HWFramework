package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import java.io.FileNotFoundException;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOMCache;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOMEnhancedForDTM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import ohos.com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMManager;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.EmptyIterator;
import ohos.com.sun.org.apache.xml.internal.utils.SystemIDResolver;
import ohos.javax.xml.transform.stream.StreamSource;

public final class LoadDocument {
    private static final String NAMESPACE_FEATURE = "http://xml.org/sax/features/namespaces";

    /* JADX WARN: Type inference failed for: r2v4, types: [java.lang.Throwable, ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public static DTMAxisIterator documentF(Object obj, DTMAxisIterator dTMAxisIterator, String str, AbstractTranslet abstractTranslet, DOM dom) throws TransletException {
        int next = dTMAxisIterator.next();
        if (next == -1) {
            return EmptyIterator.getInstance();
        }
        String documentURI = dom.getDocumentURI(next);
        if (!SystemIDResolver.isAbsoluteURI(documentURI)) {
            documentURI = SystemIDResolver.getAbsoluteURIFromRelative(documentURI);
        }
        try {
            if (obj instanceof String) {
                if (((String) obj).length() == 0) {
                    return document(str, "", abstractTranslet, dom);
                }
                return document((String) obj, documentURI, abstractTranslet, dom);
            } else if (obj instanceof DTMAxisIterator) {
                return document((DTMAxisIterator) obj, documentURI, abstractTranslet, dom);
            } else {
                throw new IllegalArgumentException("document(" + obj.toString() + ")");
            }
        } catch (Exception e) {
            throw new TransletException(e);
        }
    }

    /* JADX WARN: Type inference failed for: r4v1, types: [java.lang.Throwable, ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public static DTMAxisIterator documentF(Object obj, String str, AbstractTranslet abstractTranslet, DOM dom) throws TransletException {
        try {
            DOM dom2 = null;
            if (obj instanceof String) {
                if (str == null) {
                    str = "";
                }
                if (!SystemIDResolver.isAbsoluteURI(str)) {
                    str = SystemIDResolver.getAbsoluteURIFromRelative(str);
                }
                String str2 = (String) obj;
                if (str2.length() != 0) {
                    return document(str2, str, abstractTranslet, dom);
                }
                TemplatesImpl templates = abstractTranslet.getTemplates();
                if (templates != null) {
                    dom2 = templates.getStylesheetDOM();
                }
                if (dom2 != null) {
                    return document(dom2, abstractTranslet, dom);
                }
                return document("", str, abstractTranslet, dom, true);
            } else if (obj instanceof DTMAxisIterator) {
                return document((DTMAxisIterator) obj, (String) null, abstractTranslet, dom);
            } else {
                throw new IllegalArgumentException("document(" + obj.toString() + ")");
            }
        } catch (Exception e) {
            throw new TransletException(e);
        }
    }

    private static DTMAxisIterator document(String str, String str2, AbstractTranslet abstractTranslet, DOM dom) throws Exception {
        return document(str, str2, abstractTranslet, dom, false);
    }

    /* JADX WARN: Type inference failed for: r0v12, types: [java.lang.Throwable, ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private static DTMAxisIterator document(String str, String str2, AbstractTranslet abstractTranslet, DOM dom, boolean z) throws Exception {
        DOM dom2;
        TemplatesImpl templatesImpl;
        try {
            MultiDOM multiDOM = (MultiDOM) dom;
            String absoluteURI = (str2 == null || str2.equals("")) ? str : SystemIDResolver.getAbsoluteURI(str, str2);
            if (absoluteURI == null || absoluteURI.equals("")) {
                return EmptyIterator.getInstance();
            }
            if (multiDOM.getDocumentMask(absoluteURI) != -1) {
                DOM dOMImpl = ((DOMAdapter) multiDOM.getDOMAdapter(absoluteURI)).getDOMImpl();
                if (dOMImpl instanceof DOMEnhancedForDTM) {
                    return new SingletonIterator(((DOMEnhancedForDTM) dOMImpl).getDocument(), true);
                }
            }
            DOMCache dOMCache = abstractTranslet.getDOMCache();
            multiDOM.nextMask();
            if (dOMCache != null) {
                DOM retrieveDocument = dOMCache.retrieveDocument(str2, str, abstractTranslet);
                dom2 = retrieveDocument;
                if (retrieveDocument == null) {
                    throw new TransletException(new FileNotFoundException(str));
                }
            } else {
                String checkAccess = SecuritySupport.checkAccess(absoluteURI, abstractTranslet.getAllowedProtocols(), "all");
                if (checkAccess == null) {
                    DOMEnhancedForDTM dOMEnhancedForDTM = (DOMEnhancedForDTM) ((XSLTCDTMManager) multiDOM.getDTMManager()).getDTM(new StreamSource(absoluteURI), false, null, true, false, abstractTranslet.hasIdCall(), z);
                    if (z && (templatesImpl = (TemplatesImpl) abstractTranslet.getTemplates()) != null) {
                        templatesImpl.setStylesheetDOM(dOMEnhancedForDTM);
                    }
                    abstractTranslet.prepassDocument(dOMEnhancedForDTM);
                    dOMEnhancedForDTM.setDocumentURI(absoluteURI);
                    dom2 = dOMEnhancedForDTM;
                } else {
                    throw new Exception(new ErrorMsg(ErrorMsg.ACCESSING_XSLT_TARGET_ERR, SecuritySupport.sanitizePath(absoluteURI), checkAccess).toString());
                }
            }
            DOMAdapter makeDOMAdapter = abstractTranslet.makeDOMAdapter(dom2);
            multiDOM.addDOMAdapter(makeDOMAdapter);
            abstractTranslet.buildKeys(makeDOMAdapter, null, null, dom2.getDocument());
            return new SingletonIterator(dom2.getDocument(), true);
        } catch (Exception e) {
            throw e;
        }
    }

    private static DTMAxisIterator document(DTMAxisIterator dTMAxisIterator, String str, AbstractTranslet abstractTranslet, DOM dom) throws Exception {
        UnionIterator unionIterator = new UnionIterator(dom);
        while (true) {
            int next = dTMAxisIterator.next();
            if (next == -1) {
                return unionIterator;
            }
            String stringValueX = dom.getStringValueX(next);
            if (str == null) {
                str = dom.getDocumentURI(next);
                if (!SystemIDResolver.isAbsoluteURI(str)) {
                    str = SystemIDResolver.getAbsoluteURIFromRelative(str);
                }
            }
            unionIterator.addIterator(document(stringValueX, str, abstractTranslet, dom));
        }
    }

    private static DTMAxisIterator document(DOM dom, AbstractTranslet abstractTranslet, DOM dom2) throws Exception {
        MultiDOM multiDOM = (MultiDOM) dom2;
        DTMManager dTMManager = multiDOM.getDTMManager();
        if (dTMManager != null && (dom instanceof DTM)) {
            ((DTM) dom).migrateTo(dTMManager);
        }
        abstractTranslet.prepassDocument(dom);
        DOMAdapter makeDOMAdapter = abstractTranslet.makeDOMAdapter(dom);
        multiDOM.addDOMAdapter(makeDOMAdapter);
        abstractTranslet.buildKeys(makeDOMAdapter, null, null, dom.getDocument());
        return new SingletonIterator(dom.getDocument(), true);
    }
}
