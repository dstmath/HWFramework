package ohos.com.sun.org.apache.xerces.internal.impl.xs.opti;

import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.UserDataHandler;

public class DefaultNode implements Node {
    public Node cloneNode(boolean z) {
        return null;
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

    public Object getFeature(String str, String str2) {
        return null;
    }

    public Node getFirstChild() {
        return null;
    }

    public Node getLastChild() {
        return null;
    }

    public String getLocalName() {
        return null;
    }

    public String getNamespaceURI() {
        return null;
    }

    public Node getNextSibling() {
        return null;
    }

    public String getNodeName() {
        return null;
    }

    public short getNodeType() {
        return -1;
    }

    public String getNodeValue() throws DOMException {
        return null;
    }

    public Document getOwnerDocument() {
        return null;
    }

    public Node getParentNode() {
        return null;
    }

    public String getPrefix() {
        return null;
    }

    public Node getPreviousSibling() {
        return null;
    }

    public Object getUserData(String str) {
        return null;
    }

    public boolean hasAttributes() {
        return false;
    }

    public boolean hasChildNodes() {
        return false;
    }

    public boolean isSupported(String str, String str2) {
        return false;
    }

    public void normalize() {
    }

    public void setNodeValue(String str) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public Node insertBefore(Node node, Node node2) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public Node replaceChild(Node node, Node node2) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public Node removeChild(Node node) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public Node appendChild(Node node) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public void setPrefix(String str) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public short compareDocumentPosition(Node node) {
        throw new DOMException(9, "Method not supported");
    }

    public String getTextContent() throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public void setTextContent(String str) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public boolean isSameNode(Node node) {
        throw new DOMException(9, "Method not supported");
    }

    public String lookupPrefix(String str) {
        throw new DOMException(9, "Method not supported");
    }

    public boolean isDefaultNamespace(String str) {
        throw new DOMException(9, "Method not supported");
    }

    public String lookupNamespaceURI(String str) {
        throw new DOMException(9, "Method not supported");
    }

    public boolean isEqualNode(Node node) {
        throw new DOMException(9, "Method not supported");
    }

    public Object setUserData(String str, Object obj, UserDataHandler userDataHandler) {
        throw new DOMException(9, "Method not supported");
    }
}
