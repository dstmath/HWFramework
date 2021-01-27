package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDecl;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.org.w3c.dom.DOMException;

public class AttrNSImpl extends AttrImpl {
    static final long serialVersionUID = -781906615369795414L;
    static final String xmlURI = "http://www.w3.org/XML/1998/namespace";
    static final String xmlnsURI = "http://www.w3.org/2000/xmlns/";
    protected String localName;
    protected String namespaceURI;

    public AttrNSImpl() {
    }

    protected AttrNSImpl(CoreDocumentImpl coreDocumentImpl, String str, String str2) {
        super(coreDocumentImpl, str2);
        setName(str, str2);
    }

    private void setName(String str, String str2) {
        CoreDocumentImpl ownerDocument = ownerDocument();
        this.namespaceURI = str;
        if (str != null) {
            this.namespaceURI = str.length() == 0 ? null : str;
        }
        int indexOf = str2.indexOf(58);
        int lastIndexOf = str2.lastIndexOf(58);
        ownerDocument.checkNamespaceWF(str2, indexOf, lastIndexOf);
        if (indexOf < 0) {
            this.localName = str2;
            if (ownerDocument.errorChecking) {
                ownerDocument.checkQName(null, this.localName);
                if ((str2.equals("xmlns") && (str == null || !str.equals(NamespaceContext.XMLNS_URI))) || (str != null && str.equals(NamespaceContext.XMLNS_URI) && !str2.equals("xmlns"))) {
                    throw new DOMException(14, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NAMESPACE_ERR", null));
                }
                return;
            }
            return;
        }
        String substring = str2.substring(0, indexOf);
        this.localName = str2.substring(lastIndexOf + 1);
        ownerDocument.checkQName(substring, this.localName);
        ownerDocument.checkDOMNSErr(substring, str);
    }

    public AttrNSImpl(CoreDocumentImpl coreDocumentImpl, String str, String str2, String str3) {
        super(coreDocumentImpl, str2);
        this.localName = str3;
        this.namespaceURI = str;
    }

    protected AttrNSImpl(CoreDocumentImpl coreDocumentImpl, String str) {
        super(coreDocumentImpl, str);
    }

    /* access modifiers changed from: package-private */
    public void rename(String str, String str2) {
        if (needsSyncData()) {
            synchronizeData();
        }
        this.name = str2;
        setName(str, str2);
    }

    public void setValues(CoreDocumentImpl coreDocumentImpl, String str, String str2, String str3) {
        this.textNode = null;
        ((AttrImpl) this).flags = 0;
        isSpecified(true);
        hasStringValue(true);
        super.setOwnerDocument(coreDocumentImpl);
        this.localName = str3;
        this.namespaceURI = str;
        this.name = str2;
        this.value = null;
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
        if (ownerDocument().errorChecking) {
            if (isReadOnly()) {
                throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
            } else if (!(str == null || str.length() == 0)) {
                if (!CoreDocumentImpl.isXMLName(str, ownerDocument().isXML11Version())) {
                    throw new DOMException(5, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR", null));
                } else if (this.namespaceURI == null || str.indexOf(58) >= 0) {
                    throw new DOMException(14, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NAMESPACE_ERR", null));
                } else if (str.equals("xmlns")) {
                    if (!this.namespaceURI.equals("http://www.w3.org/2000/xmlns/")) {
                        throw new DOMException(14, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NAMESPACE_ERR", null));
                    }
                } else if (str.equals("xml")) {
                    if (!this.namespaceURI.equals("http://www.w3.org/XML/1998/namespace")) {
                        throw new DOMException(14, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NAMESPACE_ERR", null));
                    }
                } else if (this.name.equals("xmlns")) {
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

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.AttrImpl
    public String getTypeName() {
        if (this.type == null) {
            return null;
        }
        if (this.type instanceof XSSimpleTypeDecl) {
            return ((XSSimpleTypeDecl) this.type).getName();
        }
        return (String) this.type;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.AttrImpl
    public boolean isDerivedFrom(String str, String str2, int i) {
        if (this.type == null || !(this.type instanceof XSSimpleTypeDecl)) {
            return false;
        }
        return ((XSSimpleTypeDecl) this.type).isDOMDerivedFrom(str, str2, i);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.AttrImpl
    public String getTypeNamespace() {
        if (this.type != null) {
            return this.type instanceof XSSimpleTypeDecl ? ((XSSimpleTypeDecl) this.type).getNamespace() : XMLGrammarDescription.XML_DTD;
        }
        return null;
    }
}
