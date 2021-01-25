package ohos.com.sun.org.apache.xml.internal.dtm.ref.dom2dtm;

import ohos.com.sun.org.apache.xml.internal.dtm.DTMException;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.TypeInfo;
import ohos.org.w3c.dom.UserDataHandler;

public class DOM2DTMdefaultNamespaceDeclarationNode implements Attr, TypeInfo {
    final String NOT_SUPPORTED_ERR = "Unsupported operation on pseudonode";
    int handle;
    String nodename;
    String prefix;
    Element pseudoparent;
    String uri;

    public short compareDocumentPosition(Node node) throws DOMException {
        return 0;
    }

    public NamedNodeMap getAttributes() {
        return null;
    }

    public String getBaseURI() {
        return null;
    }

    public NodeList getChildNodes() {
        return null;
    }

    public Node getFirstChild() {
        return null;
    }

    public Node getLastChild() {
        return null;
    }

    public String getNamespaceURI() {
        return "http://www.w3.org/2000/xmlns/";
    }

    public Node getNextSibling() {
        return null;
    }

    public short getNodeType() {
        return 2;
    }

    public Node getParentNode() {
        return null;
    }

    public Node getPreviousSibling() {
        return null;
    }

    public TypeInfo getSchemaTypeInfo() {
        return this;
    }

    public boolean getSpecified() {
        return false;
    }

    public String getTypeName() {
        return null;
    }

    public String getTypeNamespace() {
        return null;
    }

    public boolean hasAttributes() {
        return false;
    }

    public boolean hasChildNodes() {
        return false;
    }

    public boolean isDefaultNamespace(String str) {
        return false;
    }

    public boolean isDerivedFrom(String str, String str2, int i) {
        return false;
    }

    public boolean isId() {
        return false;
    }

    public boolean isSameNode(Node node) {
        return this == node;
    }

    public boolean isSupported(String str, String str2) {
        return false;
    }

    public void normalize() {
    }

    DOM2DTMdefaultNamespaceDeclarationNode(Element element, String str, String str2, int i) {
        this.pseudoparent = element;
        this.prefix = str;
        this.uri = str2;
        this.handle = i;
        this.nodename = "xmlns:" + str;
    }

    public String getNodeName() {
        return this.nodename;
    }

    public String getName() {
        return this.nodename;
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

    public void setNodeValue(String str) {
        throw new DTMException("Unsupported operation on pseudonode");
    }

    public void setValue(String str) {
        throw new DTMException("Unsupported operation on pseudonode");
    }

    public void setPrefix(String str) {
        throw new DTMException("Unsupported operation on pseudonode");
    }

    public Node insertBefore(Node node, Node node2) {
        throw new DTMException("Unsupported operation on pseudonode");
    }

    public Node replaceChild(Node node, Node node2) {
        throw new DTMException("Unsupported operation on pseudonode");
    }

    public Node appendChild(Node node) {
        throw new DTMException("Unsupported operation on pseudonode");
    }

    public Node removeChild(Node node) {
        throw new DTMException("Unsupported operation on pseudonode");
    }

    public Document getOwnerDocument() {
        return this.pseudoparent.getOwnerDocument();
    }

    public Node cloneNode(boolean z) {
        throw new DTMException("Unsupported operation on pseudonode");
    }

    public int getHandleOfNode() {
        return this.handle;
    }

    public Object setUserData(String str, Object obj, UserDataHandler userDataHandler) {
        return getOwnerDocument().setUserData(str, obj, userDataHandler);
    }

    public Object getUserData(String str) {
        return getOwnerDocument().getUserData(str);
    }

    public Object getFeature(String str, String str2) {
        if (isSupported(str, str2)) {
            return this;
        }
        return null;
    }

    public boolean isEqualNode(Node node) {
        if (node == this) {
            return true;
        }
        if (node.getNodeType() != getNodeType()) {
            return false;
        }
        if (getNodeName() == null) {
            if (node.getNodeName() != null) {
                return false;
            }
        } else if (!getNodeName().equals(node.getNodeName())) {
            return false;
        }
        if (getLocalName() == null) {
            if (node.getLocalName() != null) {
                return false;
            }
        } else if (!getLocalName().equals(node.getLocalName())) {
            return false;
        }
        if (getNamespaceURI() == null) {
            if (node.getNamespaceURI() != null) {
                return false;
            }
        } else if (!getNamespaceURI().equals(node.getNamespaceURI())) {
            return false;
        }
        if (getPrefix() == null) {
            if (node.getPrefix() != null) {
                return false;
            }
        } else if (!getPrefix().equals(node.getPrefix())) {
            return false;
        }
        if (getNodeValue() == null) {
            if (node.getNodeValue() != null) {
                return false;
            }
        } else if (!getNodeValue().equals(node.getNodeValue())) {
            return false;
        }
        return true;
    }

    public String lookupNamespaceURI(String str) {
        short nodeType = getNodeType();
        if (nodeType == 1) {
            String namespaceURI = getNamespaceURI();
            String prefix2 = getPrefix();
            if (namespaceURI != null) {
                if (str == null && prefix2 == str) {
                    return namespaceURI;
                }
                if (prefix2 != null && prefix2.equals(str)) {
                    return namespaceURI;
                }
            }
            if (hasAttributes()) {
                NamedNodeMap attributes = getAttributes();
                int length = attributes.getLength();
                for (int i = 0; i < length; i++) {
                    Node item = attributes.item(i);
                    String prefix3 = item.getPrefix();
                    String nodeValue = item.getNodeValue();
                    String namespaceURI2 = item.getNamespaceURI();
                    if (namespaceURI2 != null && namespaceURI2.equals("http://www.w3.org/2000/xmlns/")) {
                        if (str == null && item.getNodeName().equals("xmlns")) {
                            return nodeValue;
                        }
                        if (prefix3 != null && prefix3.equals("xmlns") && item.getLocalName().equals(str)) {
                            return nodeValue;
                        }
                    }
                }
            }
            return null;
        } else if (nodeType != 2) {
            if (nodeType != 6) {
                switch (nodeType) {
                }
            }
            return null;
        } else if (getOwnerElement().getNodeType() == 1) {
            return getOwnerElement().lookupNamespaceURI(str);
        } else {
            return null;
        }
    }

    public String lookupPrefix(String str) {
        if (str == null) {
            return null;
        }
        short nodeType = getNodeType();
        if (nodeType != 2) {
            if (nodeType != 6) {
                switch (nodeType) {
                }
            }
            return null;
        } else if (getOwnerElement().getNodeType() == 1) {
            return getOwnerElement().lookupPrefix(str);
        } else {
            return null;
        }
    }

    public void setTextContent(String str) throws DOMException {
        setNodeValue(str);
    }

    public String getTextContent() throws DOMException {
        return getNodeValue();
    }
}
