package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl;
import ohos.com.sun.org.apache.xerces.internal.util.URI;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.DOMException;

public class ElementNSImpl extends ElementImpl {
    static final long serialVersionUID = -9142310625494392642L;
    static final String xmlURI = "http://www.w3.org/XML/1998/namespace";
    protected String localName;
    protected String namespaceURI;
    transient XSTypeDefinition type;

    protected ElementNSImpl() {
    }

    protected ElementNSImpl(CoreDocumentImpl coreDocumentImpl, String str, String str2) throws DOMException {
        super(coreDocumentImpl, str2);
        setName(str, str2);
    }

    private void setName(String str, String str2) {
        this.namespaceURI = str;
        if (str != null) {
            this.namespaceURI = str.length() == 0 ? null : str;
        }
        if (str2 != null) {
            int indexOf = str2.indexOf(58);
            int lastIndexOf = str2.lastIndexOf(58);
            this.ownerDocument.checkNamespaceWF(str2, indexOf, lastIndexOf);
            if (indexOf < 0) {
                this.localName = str2;
                if (this.ownerDocument.errorChecking) {
                    this.ownerDocument.checkQName(null, this.localName);
                    if ((str2.equals("xmlns") && (str == null || !str.equals(NamespaceContext.XMLNS_URI))) || (str != null && str.equals(NamespaceContext.XMLNS_URI) && !str2.equals("xmlns"))) {
                        throw new DOMException(14, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NAMESPACE_ERR", null));
                    }
                    return;
                }
                return;
            }
            String substring = str2.substring(0, indexOf);
            this.localName = str2.substring(lastIndexOf + 1);
            if (!this.ownerDocument.errorChecking) {
                return;
            }
            if (str == null || (substring.equals("xml") && !str.equals(NamespaceContext.XML_URI))) {
                throw new DOMException(14, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NAMESPACE_ERR", null));
            }
            this.ownerDocument.checkQName(substring, this.localName);
            this.ownerDocument.checkDOMNSErr(substring, str);
            return;
        }
        throw new DOMException(14, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NAMESPACE_ERR", null));
    }

    protected ElementNSImpl(CoreDocumentImpl coreDocumentImpl, String str, String str2, String str3) throws DOMException {
        super(coreDocumentImpl, str2);
        this.localName = str3;
        this.namespaceURI = str;
    }

    protected ElementNSImpl(CoreDocumentImpl coreDocumentImpl, String str) {
        super(coreDocumentImpl, str);
    }

    /* access modifiers changed from: package-private */
    public void rename(String str, String str2) {
        if (needsSyncData()) {
            synchronizeData();
        }
        this.name = str2;
        setName(str, str2);
        reconcileDefaultAttributes();
    }

    /* access modifiers changed from: protected */
    public void setValues(CoreDocumentImpl coreDocumentImpl, String str, String str2, String str3) {
        this.firstChild = null;
        this.previousSibling = null;
        this.nextSibling = null;
        this.fNodeListCache = null;
        this.attributes = null;
        ((ElementImpl) this).flags = 0;
        setOwnerDocument(coreDocumentImpl);
        needsSyncData(true);
        this.name = str2;
        this.localName = str3;
        this.namespaceURI = str;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public String getNamespaceURI() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return this.namespaceURI;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public String getPrefix() {
        if (needsSyncData()) {
            synchronizeData();
        }
        int indexOf = this.name.indexOf(58);
        if (indexOf < 0) {
            return null;
        }
        return this.name.substring(0, indexOf);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void setPrefix(String str) throws DOMException {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (this.ownerDocument.errorChecking) {
            if (isReadOnly()) {
                throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
            } else if (!(str == null || str.length() == 0)) {
                if (!CoreDocumentImpl.isXMLName(str, this.ownerDocument.isXML11Version())) {
                    throw new DOMException(5, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR", null));
                } else if (this.namespaceURI == null || str.indexOf(58) >= 0) {
                    throw new DOMException(14, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NAMESPACE_ERR", null));
                } else if (str.equals("xml") && !this.namespaceURI.equals("http://www.w3.org/XML/1998/namespace")) {
                    throw new DOMException(14, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NAMESPACE_ERR", null));
                }
            }
        }
        if (str == null || str.length() == 0) {
            this.name = this.localName;
            return;
        }
        this.name = str + ":" + this.localName;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public String getLocalName() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return this.localName;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ElementImpl, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public String getBaseURI() {
        Attr namedItemNS;
        if (needsSyncData()) {
            synchronizeData();
        }
        if (!(this.attributes == null || (namedItemNS = this.attributes.getNamedItemNS("http://www.w3.org/XML/1998/namespace", "base")) == null)) {
            String nodeValue = namedItemNS.getNodeValue();
            if (nodeValue.length() != 0) {
                try {
                    return new URI(nodeValue).toString();
                } catch (URI.MalformedURIException unused) {
                    NodeImpl parentNode = this.parentNode() != null ? this.parentNode() : this.ownerNode;
                    String baseURI = parentNode != null ? parentNode.getBaseURI() : null;
                    if (baseURI != null) {
                        try {
                            return new URI(new URI(baseURI), nodeValue).toString();
                        } catch (URI.MalformedURIException unused2) {
                            return null;
                        }
                    }
                    return null;
                }
            }
        }
        String baseURI2 = parentNode() != null ? parentNode().getBaseURI() : null;
        if (baseURI2 != null) {
            try {
                return new URI(baseURI2).toString();
            } catch (URI.MalformedURIException unused3) {
                return null;
            }
        } else {
            String baseURI3 = this.ownerNode != null ? this.ownerNode.getBaseURI() : null;
            if (baseURI3 != null) {
                try {
                    return new URI(baseURI3).toString();
                } catch (URI.MalformedURIException unused4) {
                }
            }
            return null;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ElementImpl
    public String getTypeName() {
        XSTypeDefinition xSTypeDefinition = this.type;
        if (xSTypeDefinition == null) {
            return null;
        }
        if (xSTypeDefinition instanceof XSSimpleTypeDecl) {
            return ((XSSimpleTypeDecl) xSTypeDefinition).getTypeName();
        }
        if (xSTypeDefinition instanceof XSComplexTypeDecl) {
            return ((XSComplexTypeDecl) xSTypeDefinition).getTypeName();
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ElementImpl
    public String getTypeNamespace() {
        XSTypeDefinition xSTypeDefinition = this.type;
        if (xSTypeDefinition != null) {
            return xSTypeDefinition.getNamespace();
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ElementImpl
    public boolean isDerivedFrom(String str, String str2, int i) {
        if (needsSyncData()) {
            synchronizeData();
        }
        XSTypeDefinition xSTypeDefinition = this.type;
        if (xSTypeDefinition == null) {
            return false;
        }
        if (xSTypeDefinition instanceof XSSimpleTypeDecl) {
            return ((XSSimpleTypeDecl) xSTypeDefinition).isDOMDerivedFrom(str, str2, i);
        }
        if (xSTypeDefinition instanceof XSComplexTypeDecl) {
            return ((XSComplexTypeDecl) xSTypeDefinition).isDOMDerivedFrom(str, str2, i);
        }
        return false;
    }

    public void setType(XSTypeDefinition xSTypeDefinition) {
        this.type = xSTypeDefinition;
    }
}
