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

    AttrImpl(DocumentImpl document, String namespaceURI2, String qualifiedName) {
        super(document);
        setNameNS(this, namespaceURI2, qualifiedName);
    }

    AttrImpl(DocumentImpl document, String name) {
        super(document);
        setName(this, name);
    }

    public String getLocalName() {
        if (this.namespaceAware) {
            return this.localName;
        }
        return null;
    }

    public String getName() {
        if (this.prefix == null) {
            return this.localName;
        }
        return this.prefix + ":" + this.localName;
    }

    public String getNamespaceURI() {
        return this.namespaceURI;
    }

    public String getNodeName() {
        return getName();
    }

    public short getNodeType() {
        return 2;
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

    public void setPrefix(String prefix2) {
        this.prefix = validatePrefix(prefix2, this.namespaceAware, this.namespaceURI);
    }

    public void setValue(String value2) throws DOMException {
        this.value = value2;
    }

    public TypeInfo getSchemaTypeInfo() {
        return NULL_TYPE_INFO;
    }

    public boolean isId() {
        return this.isId;
    }
}
