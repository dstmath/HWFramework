package org.apache.xalan.extensions;

import java.util.Hashtable;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.StylesheetRoot;
import org.apache.xpath.XPathProcessorException;
import org.apache.xpath.functions.FuncExtFunction;

public class ExtensionsTable {
    public Hashtable m_extensionFunctionNamespaces = new Hashtable();
    private StylesheetRoot m_sroot;

    public ExtensionsTable(StylesheetRoot sroot) throws TransformerException {
        this.m_sroot = sroot;
        Vector extensions = this.m_sroot.getExtensions();
        for (int i = 0; i < extensions.size(); i++) {
            ExtensionNamespaceSupport extNamespaceSpt = (ExtensionNamespaceSupport) extensions.get(i);
            ExtensionHandler extHandler = extNamespaceSpt.launch();
            if (extHandler != null) {
                addExtensionNamespace(extNamespaceSpt.getNamespace(), extHandler);
            }
        }
    }

    public ExtensionHandler get(String extns) {
        return (ExtensionHandler) this.m_extensionFunctionNamespaces.get(extns);
    }

    public void addExtensionNamespace(String uri, ExtensionHandler extNS) {
        this.m_extensionFunctionNamespaces.put(uri, extNS);
    }

    public boolean functionAvailable(String ns, String funcName) throws TransformerException {
        if (ns == null) {
            return false;
        }
        ExtensionHandler extNS = (ExtensionHandler) this.m_extensionFunctionNamespaces.get(ns);
        if (extNS != null) {
            return extNS.isFunctionAvailable(funcName);
        }
        return false;
    }

    public boolean elementAvailable(String ns, String elemName) throws TransformerException {
        if (ns == null) {
            return false;
        }
        ExtensionHandler extNS = (ExtensionHandler) this.m_extensionFunctionNamespaces.get(ns);
        if (extNS != null) {
            return extNS.isElementAvailable(elemName);
        }
        return false;
    }

    public Object extFunction(String ns, String funcName, Vector argVec, Object methodKey, ExpressionContext exprContext) throws TransformerException {
        if (ns == null) {
            return null;
        }
        ExtensionHandler extNS = (ExtensionHandler) this.m_extensionFunctionNamespaces.get(ns);
        if (extNS != null) {
            try {
                return extNS.callFunction(funcName, argVec, methodKey, exprContext);
            } catch (TransformerException e) {
                throw e;
            } catch (Exception e2) {
                throw new TransformerException(e2);
            }
        }
        throw new XPathProcessorException(XSLMessages.createMessage(XSLTErrorResources.ER_EXTENSION_FUNC_UNKNOWN, new Object[]{ns, funcName}));
    }

    public Object extFunction(FuncExtFunction extFunction, Vector argVec, ExpressionContext exprContext) throws TransformerException {
        String ns = extFunction.getNamespace();
        if (ns == null) {
            return null;
        }
        ExtensionHandler extNS = (ExtensionHandler) this.m_extensionFunctionNamespaces.get(ns);
        if (extNS != null) {
            try {
                return extNS.callFunction(extFunction, argVec, exprContext);
            } catch (TransformerException e) {
                throw e;
            } catch (Exception e2) {
                throw new TransformerException(e2);
            }
        }
        throw new XPathProcessorException(XSLMessages.createMessage(XSLTErrorResources.ER_EXTENSION_FUNC_UNKNOWN, new Object[]{ns, extFunction.getFunctionName()}));
    }
}
