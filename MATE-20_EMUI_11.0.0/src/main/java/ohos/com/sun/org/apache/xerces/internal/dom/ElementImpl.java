package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.com.sun.org.apache.xerces.internal.util.URI;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.Text;
import ohos.org.w3c.dom.TypeInfo;

public class ElementImpl extends ParentNode implements Element, TypeInfo {
    static final long serialVersionUID = 3717253516652722278L;
    protected AttributeMap attributes;
    protected String name;

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public short getNodeType() {
        return 1;
    }

    public String getTypeName() {
        return null;
    }

    public String getTypeNamespace() {
        return null;
    }

    public boolean isDerivedFrom(String str, String str2, int i) {
        return false;
    }

    public ElementImpl(CoreDocumentImpl coreDocumentImpl, String str) {
        super(coreDocumentImpl);
        this.name = str;
        needsSyncData(true);
    }

    protected ElementImpl() {
    }

    /* access modifiers changed from: package-private */
    public void rename(String str) {
        if (needsSyncData()) {
            synchronizeData();
        }
        this.name = str;
        reconcileDefaultAttributes();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public String getNodeName() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return this.name;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public NamedNodeMap getAttributes() {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (this.attributes == null) {
            this.attributes = new AttributeMap(this, null);
        }
        return this.attributes;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ParentNode, ohos.com.sun.org.apache.xerces.internal.dom.ChildNode, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node cloneNode(boolean z) {
        ElementImpl cloneNode = super.cloneNode(z);
        AttributeMap attributeMap = this.attributes;
        if (attributeMap != null) {
            cloneNode.attributes = (AttributeMap) attributeMap.cloneMap(cloneNode);
        }
        return cloneNode;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public String getBaseURI() {
        Attr namedItem;
        if (needsSyncData()) {
            synchronizeData();
        }
        AttributeMap attributeMap = this.attributes;
        if (!(attributeMap == null || (namedItem = attributeMap.getNamedItem("xml:base")) == null)) {
            String nodeValue = namedItem.getNodeValue();
            if (nodeValue.length() != 0) {
                try {
                    return new URI(nodeValue).toString();
                } catch (URI.MalformedURIException unused) {
                    String baseURI = this.ownerNode != null ? this.ownerNode.getBaseURI() : null;
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
        String baseURI2 = this.ownerNode != null ? this.ownerNode.getBaseURI() : null;
        if (baseURI2 != null) {
            try {
                return new URI(baseURI2).toString();
            } catch (URI.MalformedURIException unused3) {
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ParentNode, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void setOwnerDocument(CoreDocumentImpl coreDocumentImpl) {
        super.setOwnerDocument(coreDocumentImpl);
        AttributeMap attributeMap = this.attributes;
        if (attributeMap != null) {
            attributeMap.setOwnerDocument(coreDocumentImpl);
        }
    }

    public String getAttribute(String str) {
        Attr namedItem;
        if (needsSyncData()) {
            synchronizeData();
        }
        AttributeMap attributeMap = this.attributes;
        if (attributeMap == null || (namedItem = attributeMap.getNamedItem(str)) == null) {
            return "";
        }
        return namedItem.getValue();
    }

    public Attr getAttributeNode(String str) {
        if (needsSyncData()) {
            synchronizeData();
        }
        AttributeMap attributeMap = this.attributes;
        if (attributeMap == null) {
            return null;
        }
        return attributeMap.getNamedItem(str);
    }

    public NodeList getElementsByTagName(String str) {
        return new DeepNodeListImpl(this, str);
    }

    public String getTagName() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return this.name;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ParentNode, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void normalize() {
        if (!isNormalized()) {
            if (needsSyncChildren()) {
                synchronizeChildren();
            }
            Text text = this.firstChild;
            while (text != null) {
                Text text2 = text.nextSibling;
                if (text.getNodeType() == 3) {
                    if (text2 != null && text2.getNodeType() == 3) {
                        text.appendData(text2.getNodeValue());
                        removeChild(text2);
                    } else if (text.getNodeValue() == null || text.getNodeValue().length() == 0) {
                        removeChild(text);
                    }
                } else if (text.getNodeType() == 1) {
                    text.normalize();
                }
                text = text2;
            }
            if (this.attributes != null) {
                for (int i = 0; i < this.attributes.getLength(); i++) {
                    this.attributes.item(i).normalize();
                }
            }
            isNormalized(true);
        }
    }

    public void removeAttribute(String str) {
        if (!this.ownerDocument.errorChecking || !isReadOnly()) {
            if (needsSyncData()) {
                synchronizeData();
            }
            AttributeMap attributeMap = this.attributes;
            if (attributeMap != null) {
                attributeMap.safeRemoveNamedItem(str);
                return;
            }
            return;
        }
        throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
    }

    public Attr removeAttributeNode(Attr attr) throws DOMException {
        if (!this.ownerDocument.errorChecking || !isReadOnly()) {
            if (needsSyncData()) {
                synchronizeData();
            }
            AttributeMap attributeMap = this.attributes;
            if (attributeMap != null) {
                return attributeMap.removeItem(attr, true);
            }
            throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null));
        }
        throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
    }

    public void setAttribute(String str, String str2) {
        if (!this.ownerDocument.errorChecking || !isReadOnly()) {
            if (needsSyncData()) {
                synchronizeData();
            }
            Attr attributeNode = getAttributeNode(str);
            if (attributeNode == null) {
                Node createAttribute = getOwnerDocument().createAttribute(str);
                if (this.attributes == null) {
                    this.attributes = new AttributeMap(this, null);
                }
                createAttribute.setNodeValue(str2);
                this.attributes.setNamedItem(createAttribute);
                return;
            }
            attributeNode.setNodeValue(str2);
            return;
        }
        throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
    }

    public Attr setAttributeNode(Attr attr) throws DOMException {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (this.ownerDocument.errorChecking) {
            if (isReadOnly()) {
                throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
            } else if (attr.getOwnerDocument() != this.ownerDocument) {
                throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
            }
        }
        if (this.attributes == null) {
            this.attributes = new AttributeMap(this, null);
        }
        return this.attributes.setNamedItem(attr);
    }

    public String getAttributeNS(String str, String str2) {
        Attr namedItemNS;
        if (needsSyncData()) {
            synchronizeData();
        }
        AttributeMap attributeMap = this.attributes;
        if (attributeMap == null || (namedItemNS = attributeMap.getNamedItemNS(str, str2)) == null) {
            return "";
        }
        return namedItemNS.getValue();
    }

    public void setAttributeNS(String str, String str2, String str3) {
        String str4;
        String str5;
        if (!this.ownerDocument.errorChecking || !isReadOnly()) {
            if (needsSyncData()) {
                synchronizeData();
            }
            int indexOf = str2.indexOf(58);
            if (indexOf < 0) {
                str5 = str2;
                str4 = null;
            } else {
                str4 = str2.substring(0, indexOf);
                str5 = str2.substring(indexOf + 1);
            }
            Node attributeNodeNS = getAttributeNodeNS(str, str5);
            if (attributeNodeNS == null) {
                Node createAttributeNS = getOwnerDocument().createAttributeNS(str, str2);
                if (this.attributes == null) {
                    this.attributes = new AttributeMap(this, null);
                }
                createAttributeNS.setNodeValue(str3);
                this.attributes.setNamedItemNS(createAttributeNS);
                return;
            }
            if (attributeNodeNS instanceof AttrNSImpl) {
                AttrNSImpl attrNSImpl = (AttrNSImpl) attributeNodeNS;
                String str6 = attrNSImpl.name;
                if (str4 != null) {
                    str5 = str4 + ":" + str5;
                }
                attrNSImpl.name = str5;
                if (!str5.equals(str6)) {
                    attributeNodeNS = (Attr) this.attributes.removeItem(attributeNodeNS, false);
                    this.attributes.addItem(attributeNodeNS);
                }
            } else {
                attributeNodeNS = new AttrNSImpl(getOwnerDocument(), str, str2, str5);
                this.attributes.setNamedItemNS(attributeNodeNS);
            }
            attributeNodeNS.setNodeValue(str3);
            return;
        }
        throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
    }

    public void removeAttributeNS(String str, String str2) {
        if (!this.ownerDocument.errorChecking || !isReadOnly()) {
            if (needsSyncData()) {
                synchronizeData();
            }
            AttributeMap attributeMap = this.attributes;
            if (attributeMap != null) {
                attributeMap.safeRemoveNamedItemNS(str, str2);
                return;
            }
            return;
        }
        throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
    }

    public Attr getAttributeNodeNS(String str, String str2) {
        if (needsSyncData()) {
            synchronizeData();
        }
        AttributeMap attributeMap = this.attributes;
        if (attributeMap == null) {
            return null;
        }
        return attributeMap.getNamedItemNS(str, str2);
    }

    public Attr setAttributeNodeNS(Attr attr) throws DOMException {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (this.ownerDocument.errorChecking) {
            if (isReadOnly()) {
                throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
            } else if (attr.getOwnerDocument() != this.ownerDocument) {
                throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
            }
        }
        if (this.attributes == null) {
            this.attributes = new AttributeMap(this, null);
        }
        return this.attributes.setNamedItemNS(attr);
    }

    /* access modifiers changed from: protected */
    public int setXercesAttributeNode(Attr attr) {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (this.attributes == null) {
            this.attributes = new AttributeMap(this, null);
        }
        return this.attributes.addItem(attr);
    }

    /* access modifiers changed from: protected */
    public int getXercesAttribute(String str, String str2) {
        if (needsSyncData()) {
            synchronizeData();
        }
        AttributeMap attributeMap = this.attributes;
        if (attributeMap == null) {
            return -1;
        }
        return attributeMap.getNamedItemIndex(str, str2);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public boolean hasAttributes() {
        if (needsSyncData()) {
            synchronizeData();
        }
        AttributeMap attributeMap = this.attributes;
        return (attributeMap == null || attributeMap.getLength() == 0) ? false : true;
    }

    public boolean hasAttribute(String str) {
        return getAttributeNode(str) != null;
    }

    public boolean hasAttributeNS(String str, String str2) {
        return getAttributeNodeNS(str, str2) != null;
    }

    public NodeList getElementsByTagNameNS(String str, String str2) {
        return new DeepNodeListImpl(this, str, str2);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ParentNode, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public boolean isEqualNode(Node node) {
        if (!super.isEqualNode(node)) {
            return false;
        }
        boolean hasAttributes = hasAttributes();
        Element element = (Element) node;
        if (hasAttributes != element.hasAttributes()) {
            return false;
        }
        if (!hasAttributes) {
            return true;
        }
        NamedNodeMap attributes2 = getAttributes();
        NamedNodeMap attributes3 = element.getAttributes();
        int length = attributes2.getLength();
        if (length != attributes3.getLength()) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            NodeImpl item = attributes2.item(i);
            if (item.getLocalName() == null) {
                Node namedItem = attributes3.getNamedItem(item.getNodeName());
                if (namedItem == null || !item.isEqualNode(namedItem)) {
                    return false;
                }
            } else {
                Node namedItemNS = attributes3.getNamedItemNS(item.getNamespaceURI(), item.getLocalName());
                if (namedItemNS == null || !item.isEqualNode(namedItemNS)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void setIdAttributeNode(Attr attr, boolean z) {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (this.ownerDocument.errorChecking) {
            if (isReadOnly()) {
                throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
            } else if (attr.getOwnerElement() != this) {
                throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null));
            }
        }
        ((AttrImpl) attr).isIdAttribute(z);
        if (!z) {
            this.ownerDocument.removeIdentifier(attr.getValue());
        } else {
            this.ownerDocument.putIdentifier(attr.getValue(), this);
        }
    }

    public void setIdAttribute(String str, boolean z) {
        if (needsSyncData()) {
            synchronizeData();
        }
        AttrImpl attributeNode = getAttributeNode(str);
        if (attributeNode != null) {
            if (this.ownerDocument.errorChecking) {
                if (isReadOnly()) {
                    throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
                } else if (attributeNode.getOwnerElement() != this) {
                    throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null));
                }
            }
            attributeNode.isIdAttribute(z);
            if (!z) {
                this.ownerDocument.removeIdentifier(attributeNode.getValue());
            } else {
                this.ownerDocument.putIdentifier(attributeNode.getValue(), this);
            }
        } else {
            throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null));
        }
    }

    public void setIdAttributeNS(String str, String str2, boolean z) {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (str != null && str.length() == 0) {
            str = null;
        }
        AttrImpl attributeNodeNS = getAttributeNodeNS(str, str2);
        if (attributeNodeNS != null) {
            if (this.ownerDocument.errorChecking) {
                if (isReadOnly()) {
                    throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
                } else if (attributeNodeNS.getOwnerElement() != this) {
                    throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null));
                }
            }
            attributeNodeNS.isIdAttribute(z);
            if (!z) {
                this.ownerDocument.removeIdentifier(attributeNodeNS.getValue());
            } else {
                this.ownerDocument.putIdentifier(attributeNodeNS.getValue(), this);
            }
        } else {
            throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null));
        }
    }

    public TypeInfo getSchemaTypeInfo() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return this;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ParentNode, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void setReadOnly(boolean z, boolean z2) {
        super.setReadOnly(z, z2);
        AttributeMap attributeMap = this.attributes;
        if (attributeMap != null) {
            attributeMap.setReadOnly(z, true);
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void synchronizeData() {
        needsSyncData(false);
        boolean mutationEvents = this.ownerDocument.getMutationEvents();
        this.ownerDocument.setMutationEvents(false);
        setupDefaultAttributes();
        this.ownerDocument.setMutationEvents(mutationEvents);
    }

    /* access modifiers changed from: package-private */
    public void moveSpecifiedAttributes(ElementImpl elementImpl) {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (elementImpl.hasAttributes()) {
            if (this.attributes == null) {
                this.attributes = new AttributeMap(this, null);
            }
            this.attributes.moveSpecifiedAttributes(elementImpl.attributes);
        }
    }

    /* access modifiers changed from: protected */
    public void setupDefaultAttributes() {
        NamedNodeMapImpl defaultAttributes = getDefaultAttributes();
        if (defaultAttributes != null) {
            this.attributes = new AttributeMap(this, defaultAttributes);
        }
    }

    /* access modifiers changed from: protected */
    public void reconcileDefaultAttributes() {
        if (this.attributes != null) {
            this.attributes.reconcileDefaults(getDefaultAttributes());
        }
    }

    /* access modifiers changed from: protected */
    public NamedNodeMapImpl getDefaultAttributes() {
        ElementDefinitionImpl namedItem;
        DocumentTypeImpl doctype = this.ownerDocument.getDoctype();
        if (doctype == null || (namedItem = doctype.getElements().getNamedItem(getNodeName())) == null) {
            return null;
        }
        return namedItem.getAttributes();
    }
}
