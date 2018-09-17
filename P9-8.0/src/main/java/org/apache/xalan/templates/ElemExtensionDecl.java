package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;
import org.apache.xalan.extensions.ExtensionNamespaceSupport;
import org.apache.xalan.extensions.ExtensionNamespacesManager;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.StringVector;

public class ElemExtensionDecl extends ElemTemplateElement {
    static final long serialVersionUID = -4692738885172766789L;
    private StringVector m_elements = null;
    private StringVector m_functions = new StringVector();
    private String m_prefix = null;

    public void setPrefix(String v) {
        this.m_prefix = v;
    }

    public String getPrefix() {
        return this.m_prefix;
    }

    public void setFunctions(StringVector v) {
        this.m_functions = v;
    }

    public StringVector getFunctions() {
        return this.m_functions;
    }

    public String getFunction(int i) throws ArrayIndexOutOfBoundsException {
        if (this.m_functions != null) {
            return this.m_functions.elementAt(i);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getFunctionCount() {
        return this.m_functions != null ? this.m_functions.size() : 0;
    }

    public void setElements(StringVector v) {
        this.m_elements = v;
    }

    public StringVector getElements() {
        return this.m_elements;
    }

    public String getElement(int i) throws ArrayIndexOutOfBoundsException {
        if (this.m_elements != null) {
            return this.m_elements.elementAt(i);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getElementCount() {
        return this.m_elements != null ? this.m_elements.size() : 0;
    }

    public int getXSLToken() {
        return 85;
    }

    public void compose(StylesheetRoot sroot) throws TransformerException {
        super.compose(sroot);
        String declNamespace = getNamespaceForPrefix(getPrefix());
        String lang = null;
        String srcURL = null;
        String scriptSrc = null;
        if (declNamespace == null) {
            throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_NAMESPACE_DECL, new Object[]{prefix}));
        }
        for (ElemTemplateElement child = getFirstChildElem(); child != null; child = child.getNextSiblingElem()) {
            if (86 == child.getXSLToken()) {
                ElemExtensionScript sdecl = (ElemExtensionScript) child;
                lang = sdecl.getLang();
                srcURL = sdecl.getSrc();
                ElemTemplateElement childOfSDecl = sdecl.getFirstChildElem();
                if (childOfSDecl != null && 78 == childOfSDecl.getXSLToken()) {
                    scriptSrc = new String(((ElemTextLiteral) childOfSDecl).getChars());
                    if (scriptSrc.trim().length() == 0) {
                        scriptSrc = null;
                    }
                }
            }
        }
        if (lang == null) {
            lang = "javaclass";
        }
        if (!lang.equals("javaclass") || scriptSrc == null) {
            ExtensionNamespaceSupport extNsSpt = null;
            ExtensionNamespacesManager extNsMgr = sroot.getExtensionNamespacesManager();
            if (extNsMgr.namespaceIndex(declNamespace, extNsMgr.getExtensions()) == -1) {
                if (!lang.equals("javaclass")) {
                    extNsSpt = new ExtensionNamespaceSupport(declNamespace, "org.apache.xalan.extensions.ExtensionHandlerGeneral", new Object[]{declNamespace, this.m_elements, this.m_functions, lang, srcURL, scriptSrc, getSystemId()});
                } else if (srcURL == null) {
                    extNsSpt = extNsMgr.defineJavaNamespace(declNamespace);
                } else if (extNsMgr.namespaceIndex(srcURL, extNsMgr.getExtensions()) == -1) {
                    extNsSpt = extNsMgr.defineJavaNamespace(declNamespace, srcURL);
                }
            }
            if (extNsSpt != null) {
                extNsMgr.registerExtension(extNsSpt);
                return;
            }
            return;
        }
        throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_ELEM_CONTENT_NOT_ALLOWED, new Object[]{scriptSrc}));
    }

    public void runtimeInit(TransformerImpl transformer) throws TransformerException {
    }
}
