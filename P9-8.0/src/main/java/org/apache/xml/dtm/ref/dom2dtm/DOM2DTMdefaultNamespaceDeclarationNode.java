package org.apache.xml.dtm.ref.dom2dtm;

import org.apache.xalan.templates.Constants;
import org.apache.xml.dtm.DTMException;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

public class DOM2DTMdefaultNamespaceDeclarationNode implements Attr, TypeInfo {
    final String NOT_SUPPORTED_ERR = "Unsupported operation on pseudonode";
    int handle;
    String nodename;
    String prefix;
    Element pseudoparent;
    String uri;

    DOM2DTMdefaultNamespaceDeclarationNode(Element pseudoparent, String prefix, String uri, int handle) {
        this.pseudoparent = pseudoparent;
        this.prefix = prefix;
        this.uri = uri;
        this.handle = handle;
        this.nodename = Constants.ATTRNAME_XMLNS + prefix;
    }

    public String getNodeName() {
        return this.nodename;
    }

    public String getName() {
        return this.nodename;
    }

    public String getNamespaceURI() {
        return SerializerConstants.XMLNS_URI;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getLocalName() {
        return this.prefix;
    }

    public String getNodeValue() {
        return this.uri;
    }

    public String getValue() {
        return this.uri;
    }

    public Element getOwnerElement() {
        return this.pseudoparent;
    }

    public boolean isSupported(String feature, String version) {
        return false;
    }

    public boolean hasChildNodes() {
        return false;
    }

    public boolean hasAttributes() {
        return false;
    }

    public Node getParentNode() {
        return null;
    }

    public Node getFirstChild() {
        return null;
    }

    public Node getLastChild() {
        return null;
    }

    public Node getPreviousSibling() {
        return null;
    }

    public Node getNextSibling() {
        return null;
    }

    public boolean getSpecified() {
        return false;
    }

    public void normalize() {
    }

    public NodeList getChildNodes() {
        return null;
    }

    public NamedNodeMap getAttributes() {
        return null;
    }

    public short getNodeType() {
        return (short) 2;
    }

    public void setNodeValue(String value) {
        throw new DTMException("Unsupported operation on pseudonode");
    }

    public void setValue(String value) {
        throw new DTMException("Unsupported operation on pseudonode");
    }

    public void setPrefix(String value) {
        throw new DTMException("Unsupported operation on pseudonode");
    }

    public Node insertBefore(Node a, Node b) {
        throw new DTMException("Unsupported operation on pseudonode");
    }

    public Node replaceChild(Node a, Node b) {
        throw new DTMException("Unsupported operation on pseudonode");
    }

    public Node appendChild(Node a) {
        throw new DTMException("Unsupported operation on pseudonode");
    }

    public Node removeChild(Node a) {
        throw new DTMException("Unsupported operation on pseudonode");
    }

    public Document getOwnerDocument() {
        return this.pseudoparent.getOwnerDocument();
    }

    public Node cloneNode(boolean deep) {
        throw new DTMException("Unsupported operation on pseudonode");
    }

    public int getHandleOfNode() {
        return this.handle;
    }

    public String getTypeName() {
        return null;
    }

    public String getTypeNamespace() {
        return null;
    }

    public boolean isDerivedFrom(String ns, String localName, int derivationMethod) {
        return false;
    }

    public TypeInfo getSchemaTypeInfo() {
        return this;
    }

    public boolean isId() {
        return false;
    }

    public Object setUserData(String key, Object data, UserDataHandler handler) {
        return getOwnerDocument().setUserData(key, data, handler);
    }

    public Object getUserData(String key) {
        return getOwnerDocument().getUserData(key);
    }

    public Object getFeature(String feature, String version) {
        return isSupported(feature, version) ? this : null;
    }

    public boolean isEqualNode(Node arg) {
        if (arg == this) {
            return true;
        }
        if (arg.getNodeType() != getNodeType()) {
            return false;
        }
        if (getNodeName() == null) {
            if (arg.getNodeName() != null) {
                return false;
            }
        } else if (!getNodeName().equals(arg.getNodeName())) {
            return false;
        }
        if (getLocalName() == null) {
            if (arg.getLocalName() != null) {
                return false;
            }
        } else if (!getLocalName().equals(arg.getLocalName())) {
            return false;
        }
        if (getNamespaceURI() == null) {
            if (arg.getNamespaceURI() != null) {
                return false;
            }
        } else if (!getNamespaceURI().equals(arg.getNamespaceURI())) {
            return false;
        }
        if (getPrefix() == null) {
            if (arg.getPrefix() != null) {
                return false;
            }
        } else if (!getPrefix().equals(arg.getPrefix())) {
            return false;
        }
        if (getNodeValue() == null) {
            if (arg.getNodeValue() != null) {
                return false;
            }
        } else if (!getNodeValue().equals(arg.getNodeValue())) {
            return false;
        }
        return true;
    }

    public String lookupNamespaceURI(String specifiedPrefix) {
        switch (getNodeType()) {
            case (short) 1:
                String namespace = getNamespaceURI();
                String prefix = getPrefix();
                if (namespace != null) {
                    if (specifiedPrefix == null && prefix == specifiedPrefix) {
                        return namespace;
                    }
                    if (prefix != null && prefix.equals(specifiedPrefix)) {
                        return namespace;
                    }
                }
                if (hasAttributes()) {
                    NamedNodeMap map = getAttributes();
                    int length = map.getLength();
                    for (int i = 0; i < length; i++) {
                        Node attr = map.item(i);
                        String attrPrefix = attr.getPrefix();
                        String value = attr.getNodeValue();
                        namespace = attr.getNamespaceURI();
                        if (namespace != null && namespace.equals(SerializerConstants.XMLNS_URI)) {
                            if (specifiedPrefix == null && attr.getNodeName().equals("xmlns")) {
                                return value;
                            }
                            if (attrPrefix != null && attrPrefix.equals("xmlns") && attr.getLocalName().equals(specifiedPrefix)) {
                                return value;
                            }
                        }
                    }
                }
                return null;
            case (short) 2:
                if (getOwnerElement().getNodeType() == (short) 1) {
                    return getOwnerElement().lookupNamespaceURI(specifiedPrefix);
                }
                return null;
            case (short) 6:
            case (short) 10:
            case (short) 11:
            case (short) 12:
                return null;
            default:
                return null;
        }
    }

    public boolean isDefaultNamespace(String namespaceURI) {
        return false;
    }

    public String lookupPrefix(String namespaceURI) {
        if (namespaceURI == null) {
            return null;
        }
        switch (getNodeType()) {
            case (short) 2:
                if (getOwnerElement().getNodeType() == (short) 1) {
                    return getOwnerElement().lookupPrefix(namespaceURI);
                }
                return null;
            case (short) 6:
            case (short) 10:
            case (short) 11:
            case (short) 12:
                return null;
            default:
                return null;
        }
    }

    public boolean isSameNode(Node other) {
        return this == other;
    }

    public void setTextContent(String textContent) throws DOMException {
        setNodeValue(textContent);
    }

    public String getTextContent() throws DOMException {
        return getNodeValue();
    }

    public short compareDocumentPosition(Node other) throws DOMException {
        return (short) 0;
    }

    public String getBaseURI() {
        return null;
    }
}
