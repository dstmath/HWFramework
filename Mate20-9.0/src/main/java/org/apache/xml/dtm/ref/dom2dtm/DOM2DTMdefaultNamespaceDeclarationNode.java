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

    DOM2DTMdefaultNamespaceDeclarationNode(Element pseudoparent2, String prefix2, String uri2, int handle2) {
        this.pseudoparent = pseudoparent2;
        this.prefix = prefix2;
        this.uri = uri2;
        this.handle = handle2;
        this.nodename = Constants.ATTRNAME_XMLNS + prefix2;
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
        return 2;
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
        if (isSupported(feature, version)) {
            return this;
        }
        return null;
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
        short type = getNodeType();
        if (type != 6) {
            switch (type) {
                case 1:
                    String namespace = getNamespaceURI();
                    String prefix2 = getPrefix();
                    if (namespace != null) {
                        if (specifiedPrefix == null && prefix2 == specifiedPrefix) {
                            return namespace;
                        }
                        if (prefix2 != null && prefix2.equals(specifiedPrefix)) {
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
                            String namespace2 = attr.getNamespaceURI();
                            if (namespace2 != null && namespace2.equals(SerializerConstants.XMLNS_URI)) {
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
                case 2:
                    if (getOwnerElement().getNodeType() == 1) {
                        return getOwnerElement().lookupNamespaceURI(specifiedPrefix);
                    }
                    return null;
                default:
                    switch (type) {
                        case 10:
                        case 11:
                        case 12:
                            break;
                        default:
                            return null;
                    }
            }
        }
        return null;
    }

    public boolean isDefaultNamespace(String namespaceURI) {
        return false;
    }

    public String lookupPrefix(String namespaceURI) {
        if (namespaceURI == null) {
            return null;
        }
        short type = getNodeType();
        if (type != 2) {
            if (type != 6) {
                switch (type) {
                    case 10:
                    case 11:
                    case 12:
                        break;
                    default:
                        return null;
                }
            }
            return null;
        } else if (getOwnerElement().getNodeType() == 1) {
            return getOwnerElement().lookupPrefix(namespaceURI);
        } else {
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
        return 0;
    }

    public String getBaseURI() {
        return null;
    }
}
