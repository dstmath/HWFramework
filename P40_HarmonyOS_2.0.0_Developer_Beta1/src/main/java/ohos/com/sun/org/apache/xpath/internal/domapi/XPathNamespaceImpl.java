package ohos.com.sun.org.apache.xpath.internal.domapi;

import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.UserDataHandler;
import ohos.org.w3c.dom.xpath.XPathNamespace;

class XPathNamespaceImpl implements XPathNamespace {
    private final Node m_attributeNode;
    private String textContent;

    public Node appendChild(Node node) throws DOMException {
        return null;
    }

    public short compareDocumentPosition(Node node) throws DOMException {
        return 0;
    }

    public String getBaseURI() {
        return null;
    }

    public Object getFeature(String str, String str2) {
        return null;
    }

    public String getNodeName() {
        return "#namespace";
    }

    public short getNodeType() {
        return 13;
    }

    public Object getUserData(String str) {
        return null;
    }

    public boolean hasChildNodes() {
        return false;
    }

    public Node insertBefore(Node node, Node node2) throws DOMException {
        return null;
    }

    public boolean isDefaultNamespace(String str) {
        return false;
    }

    public boolean isEqualNode(Node node) {
        return false;
    }

    public boolean isSameNode(Node node) {
        return false;
    }

    public String lookupNamespaceURI(String str) {
        return null;
    }

    public String lookupPrefix(String str) {
        return "";
    }

    public Node removeChild(Node node) throws DOMException {
        return null;
    }

    public Node replaceChild(Node node, Node node2) throws DOMException {
        return null;
    }

    public void setNodeValue(String str) throws DOMException {
    }

    public void setPrefix(String str) throws DOMException {
    }

    public Object setUserData(String str, Object obj, UserDataHandler userDataHandler) {
        return null;
    }

    XPathNamespaceImpl(Node node) {
        this.m_attributeNode = node;
    }

    public Element getOwnerElement() {
        return this.m_attributeNode.getOwnerElement();
    }

    public String getNodeValue() throws DOMException {
        return this.m_attributeNode.getNodeValue();
    }

    public Node getParentNode() {
        return this.m_attributeNode.getParentNode();
    }

    public NodeList getChildNodes() {
        return this.m_attributeNode.getChildNodes();
    }

    public Node getFirstChild() {
        return this.m_attributeNode.getFirstChild();
    }

    public Node getLastChild() {
        return this.m_attributeNode.getLastChild();
    }

    public Node getPreviousSibling() {
        return this.m_attributeNode.getPreviousSibling();
    }

    public Node getNextSibling() {
        return this.m_attributeNode.getNextSibling();
    }

    public NamedNodeMap getAttributes() {
        return this.m_attributeNode.getAttributes();
    }

    public Document getOwnerDocument() {
        return this.m_attributeNode.getOwnerDocument();
    }

    public Node cloneNode(boolean z) {
        throw new DOMException(9, (String) null);
    }

    public void normalize() {
        this.m_attributeNode.normalize();
    }

    public boolean isSupported(String str, String str2) {
        return this.m_attributeNode.isSupported(str, str2);
    }

    public String getNamespaceURI() {
        return this.m_attributeNode.getNodeValue();
    }

    public String getPrefix() {
        return this.m_attributeNode.getPrefix();
    }

    public String getLocalName() {
        return this.m_attributeNode.getPrefix();
    }

    public boolean hasAttributes() {
        return this.m_attributeNode.hasAttributes();
    }

    public String getTextContent() throws DOMException {
        return this.textContent;
    }

    public void setTextContent(String str) throws DOMException {
        this.textContent = str;
    }
}
