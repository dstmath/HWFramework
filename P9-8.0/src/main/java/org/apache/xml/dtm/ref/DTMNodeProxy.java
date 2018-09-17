package org.apache.xml.dtm.ref;

import java.util.Vector;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMDOMException;
import org.apache.xpath.NodeSet;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

public class DTMNodeProxy implements Node, Document, Text, Element, Attr, ProcessingInstruction, Comment, DocumentFragment {
    private static final String EMPTYSTRING = "";
    static final DOMImplementation implementation = new DTMNodeProxyImplementation();
    protected String actualEncoding;
    public DTM dtm;
    protected String fDocumentURI;
    int node;
    private String xmlEncoding;
    private boolean xmlStandalone;
    private String xmlVersion;

    static class DTMNodeProxyImplementation implements DOMImplementation {
        DTMNodeProxyImplementation() {
        }

        public DocumentType createDocumentType(String qualifiedName, String publicId, String systemId) {
            throw new DTMDOMException((short) 9);
        }

        public Document createDocument(String namespaceURI, String qualfiedName, DocumentType doctype) {
            throw new DTMDOMException((short) 9);
        }

        public boolean hasFeature(String feature, String version) {
            if (("CORE".equals(feature.toUpperCase()) || "XML".equals(feature.toUpperCase())) && (SerializerConstants.XMLVERSION10.equals(version) || "2.0".equals(version))) {
                return true;
            }
            return false;
        }

        public Object getFeature(String feature, String version) {
            return null;
        }
    }

    public DTMNodeProxy(DTM dtm, int node) {
        this.dtm = dtm;
        this.node = node;
    }

    public final DTM getDTM() {
        return this.dtm;
    }

    public final int getDTMNodeNumber() {
        return this.node;
    }

    public final boolean equals(Node node) {
        boolean z = false;
        try {
            DTMNodeProxy dtmp = (DTMNodeProxy) node;
            if (dtmp.node == this.node && dtmp.dtm == this.dtm) {
                z = true;
            }
            return z;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public final boolean equals(Object node) {
        try {
            return equals((Node) node);
        } catch (ClassCastException e) {
            return false;
        }
    }

    public final boolean sameNodeAs(Node other) {
        boolean z = false;
        if (!(other instanceof DTMNodeProxy)) {
            return false;
        }
        DTMNodeProxy that = (DTMNodeProxy) other;
        if (this.dtm == that.dtm && this.node == that.node) {
            z = true;
        }
        return z;
    }

    public final String getNodeName() {
        return this.dtm.getNodeName(this.node);
    }

    public final String getTarget() {
        return this.dtm.getNodeName(this.node);
    }

    public final String getLocalName() {
        return this.dtm.getLocalName(this.node);
    }

    public final String getPrefix() {
        return this.dtm.getPrefix(this.node);
    }

    public final void setPrefix(String prefix) throws DOMException {
        throw new DTMDOMException((short) 7);
    }

    public final String getNamespaceURI() {
        return this.dtm.getNamespaceURI(this.node);
    }

    public final boolean supports(String feature, String version) {
        return implementation.hasFeature(feature, version);
    }

    public final boolean isSupported(String feature, String version) {
        return implementation.hasFeature(feature, version);
    }

    public final String getNodeValue() throws DOMException {
        return this.dtm.getNodeValue(this.node);
    }

    public final String getStringValue() throws DOMException {
        return this.dtm.getStringValue(this.node).toString();
    }

    public final void setNodeValue(String nodeValue) throws DOMException {
        throw new DTMDOMException((short) 7);
    }

    public final short getNodeType() {
        return this.dtm.getNodeType(this.node);
    }

    public final Node getParentNode() {
        Node node = null;
        if (getNodeType() == (short) 2) {
            return null;
        }
        int newnode = this.dtm.getParent(this.node);
        if (newnode != -1) {
            node = this.dtm.getNode(newnode);
        }
        return node;
    }

    public final Node getOwnerNode() {
        int newnode = this.dtm.getParent(this.node);
        return newnode == -1 ? null : this.dtm.getNode(newnode);
    }

    public final NodeList getChildNodes() {
        return new DTMChildIterNodeList(this.dtm, this.node);
    }

    public final Node getFirstChild() {
        int newnode = this.dtm.getFirstChild(this.node);
        return newnode == -1 ? null : this.dtm.getNode(newnode);
    }

    public final Node getLastChild() {
        int newnode = this.dtm.getLastChild(this.node);
        return newnode == -1 ? null : this.dtm.getNode(newnode);
    }

    public final Node getPreviousSibling() {
        int newnode = this.dtm.getPreviousSibling(this.node);
        return newnode == -1 ? null : this.dtm.getNode(newnode);
    }

    public final Node getNextSibling() {
        Node node = null;
        if (this.dtm.getNodeType(this.node) == (short) 2) {
            return null;
        }
        int newnode = this.dtm.getNextSibling(this.node);
        if (newnode != -1) {
            node = this.dtm.getNode(newnode);
        }
        return node;
    }

    public final NamedNodeMap getAttributes() {
        return new DTMNamedNodeMap(this.dtm, this.node);
    }

    public boolean hasAttribute(String name) {
        return -1 != this.dtm.getAttributeNode(this.node, null, name);
    }

    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return -1 != this.dtm.getAttributeNode(this.node, namespaceURI, localName);
    }

    public final Document getOwnerDocument() {
        return (Document) this.dtm.getNode(this.dtm.getOwnerDocument(this.node));
    }

    public final Node insertBefore(Node newChild, Node refChild) throws DOMException {
        throw new DTMDOMException((short) 7);
    }

    public final Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        throw new DTMDOMException((short) 7);
    }

    public final Node removeChild(Node oldChild) throws DOMException {
        throw new DTMDOMException((short) 7);
    }

    public final Node appendChild(Node newChild) throws DOMException {
        throw new DTMDOMException((short) 7);
    }

    public final boolean hasChildNodes() {
        return -1 != this.dtm.getFirstChild(this.node);
    }

    public final Node cloneNode(boolean deep) {
        throw new DTMDOMException((short) 9);
    }

    public final DocumentType getDoctype() {
        return null;
    }

    public final DOMImplementation getImplementation() {
        return implementation;
    }

    public final Element getDocumentElement() {
        int dochandle = this.dtm.getDocument();
        int elementhandle = -1;
        int kidhandle = this.dtm.getFirstChild(dochandle);
        while (kidhandle != -1) {
            switch (this.dtm.getNodeType(kidhandle)) {
                case (short) 1:
                    if (elementhandle == -1) {
                        elementhandle = kidhandle;
                        break;
                    }
                    elementhandle = -1;
                    kidhandle = this.dtm.getLastChild(dochandle);
                    break;
                case (short) 7:
                case (short) 8:
                case (short) 10:
                    break;
                default:
                    elementhandle = -1;
                    kidhandle = this.dtm.getLastChild(dochandle);
                    break;
            }
            kidhandle = this.dtm.getNextSibling(kidhandle);
        }
        if (elementhandle != -1) {
            return (Element) this.dtm.getNode(elementhandle);
        }
        throw new DTMDOMException((short) 9);
    }

    public final Element createElement(String tagName) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public final DocumentFragment createDocumentFragment() {
        throw new DTMDOMException((short) 9);
    }

    public final Text createTextNode(String data) {
        throw new DTMDOMException((short) 9);
    }

    public final Comment createComment(String data) {
        throw new DTMDOMException((short) 9);
    }

    public final CDATASection createCDATASection(String data) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public final ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public final Attr createAttribute(String name) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public final EntityReference createEntityReference(String name) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public final NodeList getElementsByTagName(String tagname) {
        int i;
        Vector listVector = new Vector();
        Node retNode = this.dtm.getNode(this.node);
        if (retNode != null) {
            boolean isTagNameWildCard = "*".equals(tagname);
            if ((short) 1 == retNode.getNodeType()) {
                NodeList nodeList = retNode.getChildNodes();
                for (i = 0; i < nodeList.getLength(); i++) {
                    traverseChildren(listVector, nodeList.item(i), tagname, isTagNameWildCard);
                }
            } else if ((short) 9 == retNode.getNodeType()) {
                traverseChildren(listVector, this.dtm.getNode(this.node), tagname, isTagNameWildCard);
            }
        }
        int size = listVector.size();
        NodeSet nodeSet = new NodeSet(size);
        for (i = 0; i < size; i++) {
            nodeSet.addNode((Node) listVector.elementAt(i));
        }
        return nodeSet;
    }

    private final void traverseChildren(Vector listVector, Node tempNode, String tagname, boolean isTagNameWildCard) {
        if (tempNode != null) {
            if (tempNode.getNodeType() == (short) 1 && (isTagNameWildCard || tempNode.getNodeName().equals(tagname))) {
                listVector.add(tempNode);
            }
            if (tempNode.hasChildNodes()) {
                NodeList nodeList = tempNode.getChildNodes();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    traverseChildren(listVector, nodeList.item(i), tagname, isTagNameWildCard);
                }
            }
        }
    }

    public final Node importNode(Node importedNode, boolean deep) throws DOMException {
        throw new DTMDOMException((short) 7);
    }

    public final Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public final Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public final NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        int i;
        Vector listVector = new Vector();
        Node retNode = this.dtm.getNode(this.node);
        if (retNode != null) {
            boolean isNamespaceURIWildCard = "*".equals(namespaceURI);
            boolean isLocalNameWildCard = "*".equals(localName);
            if ((short) 1 == retNode.getNodeType()) {
                NodeList nodeList = retNode.getChildNodes();
                for (i = 0; i < nodeList.getLength(); i++) {
                    traverseChildren(listVector, nodeList.item(i), namespaceURI, localName, isNamespaceURIWildCard, isLocalNameWildCard);
                }
            } else if ((short) 9 == retNode.getNodeType()) {
                traverseChildren(listVector, this.dtm.getNode(this.node), namespaceURI, localName, isNamespaceURIWildCard, isLocalNameWildCard);
            }
        }
        int size = listVector.size();
        NodeSet nodeSet = new NodeSet(size);
        for (i = 0; i < size; i++) {
            nodeSet.addNode((Node) listVector.elementAt(i));
        }
        return nodeSet;
    }

    private final void traverseChildren(Vector listVector, Node tempNode, String namespaceURI, String localname, boolean isNamespaceURIWildCard, boolean isLocalNameWildCard) {
        if (tempNode != null) {
            if (tempNode.getNodeType() == (short) 1 && (isLocalNameWildCard || tempNode.getLocalName().equals(localname))) {
                String nsURI = tempNode.getNamespaceURI();
                if ((namespaceURI == null && nsURI == null) || isNamespaceURIWildCard || (namespaceURI != null && namespaceURI.equals(nsURI))) {
                    listVector.add(tempNode);
                }
            }
            if (tempNode.hasChildNodes()) {
                NodeList nl = tempNode.getChildNodes();
                for (int i = 0; i < nl.getLength(); i++) {
                    traverseChildren(listVector, nl.item(i), namespaceURI, localname, isNamespaceURIWildCard, isLocalNameWildCard);
                }
            }
        }
    }

    public final Element getElementById(String elementId) {
        return (Element) this.dtm.getNode(this.dtm.getElementById(elementId));
    }

    public final Text splitText(int offset) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public final String getData() throws DOMException {
        return this.dtm.getNodeValue(this.node);
    }

    public final void setData(String data) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public final int getLength() {
        return this.dtm.getNodeValue(this.node).length();
    }

    public final String substringData(int offset, int count) throws DOMException {
        return getData().substring(offset, offset + count);
    }

    public final void appendData(String arg) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public final void insertData(int offset, String arg) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public final void deleteData(int offset, int count) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public final void replaceData(int offset, int count, String arg) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public final String getTagName() {
        return this.dtm.getNodeName(this.node);
    }

    public final String getAttribute(String name) {
        Node node = new DTMNamedNodeMap(this.dtm, this.node).getNamedItem(name);
        return node == null ? "" : node.getNodeValue();
    }

    public final void setAttribute(String name, String value) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public final void removeAttribute(String name) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public final Attr getAttributeNode(String name) {
        return (Attr) new DTMNamedNodeMap(this.dtm, this.node).getNamedItem(name);
    }

    public final Attr setAttributeNode(Attr newAttr) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public final Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public boolean hasAttributes() {
        return -1 != this.dtm.getFirstAttribute(this.node);
    }

    public final void normalize() {
        throw new DTMDOMException((short) 9);
    }

    public final String getAttributeNS(String namespaceURI, String localName) {
        Node retNode = null;
        int n = this.dtm.getAttributeNode(this.node, namespaceURI, localName);
        if (n != -1) {
            retNode = this.dtm.getNode(n);
        }
        return retNode == null ? "" : retNode.getNodeValue();
    }

    public final void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public final void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public final Attr getAttributeNodeNS(String namespaceURI, String localName) {
        int n = this.dtm.getAttributeNode(this.node, namespaceURI, localName);
        if (n != -1) {
            return (Attr) this.dtm.getNode(n);
        }
        return null;
    }

    public final Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public final String getName() {
        return this.dtm.getNodeName(this.node);
    }

    public final boolean getSpecified() {
        return true;
    }

    public final String getValue() {
        return this.dtm.getNodeValue(this.node);
    }

    public final void setValue(String value) {
        throw new DTMDOMException((short) 9);
    }

    public final Element getOwnerElement() {
        Element element = null;
        if (getNodeType() != (short) 2) {
            return null;
        }
        int newnode = this.dtm.getParent(this.node);
        if (newnode != -1) {
            element = (Element) this.dtm.getNode(newnode);
        }
        return element;
    }

    public Node adoptNode(Node source) throws DOMException {
        throw new DTMDOMException((short) 9);
    }

    public String getInputEncoding() {
        throw new DTMDOMException((short) 9);
    }

    public boolean getStrictErrorChecking() {
        throw new DTMDOMException((short) 9);
    }

    public void setStrictErrorChecking(boolean strictErrorChecking) {
        throw new DTMDOMException((short) 9);
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
        return this.dtm.getStringValue(this.node).toString();
    }

    public short compareDocumentPosition(Node other) throws DOMException {
        return (short) 0;
    }

    public String getBaseURI() {
        return null;
    }

    public Node renameNode(Node n, String namespaceURI, String name) throws DOMException {
        return n;
    }

    public void normalizeDocument() {
    }

    public DOMConfiguration getDomConfig() {
        return null;
    }

    public void setDocumentURI(String documentURI) {
        this.fDocumentURI = documentURI;
    }

    public String getDocumentURI() {
        return this.fDocumentURI;
    }

    public String getActualEncoding() {
        return this.actualEncoding;
    }

    public void setActualEncoding(String value) {
        this.actualEncoding = value;
    }

    public Text replaceWholeText(String content) throws DOMException {
        return null;
    }

    public String getWholeText() {
        return null;
    }

    public boolean isElementContentWhitespace() {
        return false;
    }

    public void setIdAttribute(boolean id) {
    }

    public void setIdAttribute(String name, boolean makeId) {
    }

    public void setIdAttributeNode(Attr at, boolean makeId) {
    }

    public void setIdAttributeNS(String namespaceURI, String localName, boolean makeId) {
    }

    public TypeInfo getSchemaTypeInfo() {
        return null;
    }

    public boolean isId() {
        return false;
    }

    public String getXmlEncoding() {
        return this.xmlEncoding;
    }

    public void setXmlEncoding(String xmlEncoding) {
        this.xmlEncoding = xmlEncoding;
    }

    public boolean getXmlStandalone() {
        return this.xmlStandalone;
    }

    public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
        this.xmlStandalone = xmlStandalone;
    }

    public String getXmlVersion() {
        return this.xmlVersion;
    }

    public void setXmlVersion(String xmlVersion) throws DOMException {
        this.xmlVersion = xmlVersion;
    }
}
