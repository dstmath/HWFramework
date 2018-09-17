package org.apache.harmony.xml.dom;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.TypeInfo;

public final class AttrImpl extends NodeImpl implements Attr {
    boolean isId;
    String localName;
    boolean namespaceAware;
    String namespaceURI;
    ElementImpl ownerElement;
    String prefix;
    private String value = "";

    AttrImpl(DocumentImpl document, String namespaceURI, String qualifiedName) {
        super(document);
        NodeImpl.setNameNS(this, namespaceURI, qualifiedName);
    }

    AttrImpl(DocumentImpl document, String name) {
        super(document);
        NodeImpl.setName(this, name);
    }

    public String getLocalName() {
        return this.namespaceAware ? this.localName : null;
    }

    public String getName() {
        if (this.prefix != null) {
            return this.prefix + ":" + this.localName;
        }
        return this.localName;
    }

    public String getNamespaceURI() {
        return this.namespaceURI;
    }

    public String getNodeName() {
        return getName();
    }

    public short getNodeType() {
        return (short) 2;
    }

    public String getNodeValue() {
        return getValue();
    }

    public Element getOwnerElement() {
        return this.ownerElement;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public boolean getSpecified() {
        return this.value != null;
    }

    public String getValue() {
        return this.value;
    }

    public void setPrefix(String prefix) {
        this.prefix = NodeImpl.validatePrefix(prefix, this.namespaceAware, this.namespaceURI);
    }

    public void setValue(String value) throws DOMException {
        this.value = value;
    }

    public TypeInfo getSchemaTypeInfo() {
        return NULL_TYPE_INFO;
    }

    public boolean isId() {
        return this.isId;
    }
}
