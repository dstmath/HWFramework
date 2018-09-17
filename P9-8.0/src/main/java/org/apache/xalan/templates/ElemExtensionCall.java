package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;
import org.apache.xalan.extensions.ExtensionHandler;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.XPathContext;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ElemExtensionCall extends ElemLiteralResult {
    static final long serialVersionUID = 3171339708500216920L;
    ElemExtensionDecl m_decl = null;
    String m_extns;
    String m_lang;
    String m_scriptSrc;
    String m_srcURL;

    public int getXSLToken() {
        return 79;
    }

    public void compose(StylesheetRoot sroot) throws TransformerException {
        super.compose(sroot);
        this.m_extns = getNamespace();
        this.m_decl = getElemExtensionDecl(sroot, this.m_extns);
        if (this.m_decl == null) {
            sroot.getExtensionNamespacesManager().registerExtension(this.m_extns);
        }
    }

    private ElemExtensionDecl getElemExtensionDecl(StylesheetRoot stylesheet, String namespace) {
        int n = stylesheet.getGlobalImportCount();
        for (int i = 0; i < n; i++) {
            for (ElemTemplateElement child = stylesheet.getGlobalImport(i).getFirstChildElem(); child != null; child = child.getNextSiblingElem()) {
                if (85 == child.getXSLToken()) {
                    ElemExtensionDecl decl = (ElemExtensionDecl) child;
                    if (namespace.equals(child.getNamespaceForPrefix(decl.getPrefix()))) {
                        return decl;
                    }
                }
            }
        }
        return null;
    }

    private void executeFallbacks(TransformerImpl transformer) throws TransformerException {
        for (ElemTemplateElement child = this.m_firstChild; child != null; child = child.m_nextSibling) {
            if (child.getXSLToken() == 57) {
                try {
                    transformer.pushElemTemplateElement(child);
                    ((ElemFallback) child).executeFallback(transformer);
                } finally {
                    transformer.popElemTemplateElement();
                }
            }
        }
    }

    private boolean hasFallbackChildren() {
        for (ElemTemplateElement child = this.m_firstChild; child != null; child = child.m_nextSibling) {
            if (child.getXSLToken() == 57) {
                return true;
            }
        }
        return false;
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
        if (transformer.getStylesheet().isSecureProcessing()) {
            throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_EXTENSION_ELEMENT_NOT_ALLOWED_IN_SECURE_PROCESSING, new Object[]{getRawName()}));
        }
        try {
            transformer.getResultTreeHandler().flushPending();
            ExtensionHandler nsh = transformer.getExtensionsTable().get(this.m_extns);
            if (nsh == null) {
                if (hasFallbackChildren()) {
                    executeFallbacks(transformer);
                } else {
                    transformer.getErrorListener().fatalError(new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_CALL_TO_EXT_FAILED, new Object[]{getNodeName()})));
                }
                return;
            }
            try {
                nsh.processElement(getLocalName(), this, transformer, getStylesheet(), this);
            } catch (Exception e) {
                if (hasFallbackChildren()) {
                    executeFallbacks(transformer);
                } else if (e instanceof TransformerException) {
                    TransformerException te = (TransformerException) e;
                    if (te.getLocator() == null) {
                        te.setLocator(this);
                    }
                    transformer.getErrorListener().fatalError(te);
                } else if (e instanceof RuntimeException) {
                    transformer.getErrorListener().fatalError(new TransformerException(e));
                } else {
                    transformer.getErrorListener().warning(new TransformerException(e));
                }
            }
        } catch (TransformerException e2) {
            transformer.getErrorListener().fatalError(e2);
        } catch (SAXException se) {
            throw new TransformerException(se);
        }
    }

    public String getAttribute(String rawName, Node sourceNode, TransformerImpl transformer) throws TransformerException {
        AVT avt = getLiteralResultAttribute(rawName);
        if (avt == null || !avt.getRawName().equals(rawName)) {
            return null;
        }
        XPathContext xctxt = transformer.getXPathContext();
        return avt.evaluate(xctxt, xctxt.getDTMHandleFromNode(sourceNode), this);
    }

    protected boolean accept(XSLTVisitor visitor) {
        return visitor.visitExtensionElement(this);
    }
}
