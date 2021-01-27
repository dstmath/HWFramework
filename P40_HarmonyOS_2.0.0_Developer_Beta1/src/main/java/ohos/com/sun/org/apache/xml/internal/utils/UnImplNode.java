package ohos.com.sun.org.apache.xml.internal.utils;

import java.io.PrintStream;
import ohos.com.sun.org.apache.xml.internal.res.XMLMessages;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.CDATASection;
import ohos.org.w3c.dom.Comment;
import ohos.org.w3c.dom.DOMConfiguration;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.DOMImplementation;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentFragment;
import ohos.org.w3c.dom.DocumentType;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.EntityReference;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.ProcessingInstruction;
import ohos.org.w3c.dom.Text;
import ohos.org.w3c.dom.TypeInfo;
import ohos.org.w3c.dom.UserDataHandler;

public class UnImplNode implements Node, Element, NodeList, Document {
    protected String actualEncoding;
    protected String fDocumentURI;
    private String xmlEncoding;
    private boolean xmlStandalone;
    private String xmlVersion;

    public short compareDocumentPosition(Node node) throws DOMException {
        return 0;
    }

    public String getBaseURI() {
        return null;
    }

    public DOMConfiguration getDomConfig() {
        return null;
    }

    public TypeInfo getSchemaTypeInfo() {
        return null;
    }

    public String getWholeText() {
        return null;
    }

    public boolean isDefaultNamespace(String str) {
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

    public boolean isWhitespaceInElementContent() {
        return false;
    }

    public void normalizeDocument() {
    }

    public Node renameNode(Node node, String str, String str2) throws DOMException {
        return node;
    }

    public Text replaceWholeText(String str) throws DOMException {
        return null;
    }

    public void setIdAttribute(String str, boolean z) {
    }

    public void setIdAttribute(boolean z) {
    }

    public void setIdAttributeNS(String str, String str2, boolean z) {
    }

    public void setIdAttributeNode(Attr attr, boolean z) {
    }

    public void error(String str) {
        PrintStream printStream = System.out;
        printStream.println("DOM ERROR! class: " + getClass().getName());
        throw new RuntimeException(XMLMessages.createXMLMessage(str, null));
    }

    public void error(String str, Object[] objArr) {
        PrintStream printStream = System.out;
        printStream.println("DOM ERROR! class: " + getClass().getName());
        throw new RuntimeException(XMLMessages.createXMLMessage(str, objArr));
    }

    public Node appendChild(Node node) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public boolean hasChildNodes() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return false;
    }

    public short getNodeType() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return 0;
    }

    public Node getParentNode() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public NodeList getChildNodes() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Node getFirstChild() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Node getLastChild() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Node getNextSibling() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public int getLength() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return 0;
    }

    public Node item(int i) {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Document getOwnerDocument() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public String getTagName() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public String getNodeName() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public void normalize() {
        error("ER_FUNCTION_NOT_SUPPORTED");
    }

    public NodeList getElementsByTagName(String str) {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Attr removeAttributeNode(Attr attr) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Attr setAttributeNode(Attr attr) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public boolean hasAttribute(String str) {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return false;
    }

    public boolean hasAttributeNS(String str, String str2) {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return false;
    }

    public Attr getAttributeNode(String str) {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public void removeAttribute(String str) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
    }

    public void setAttribute(String str, String str2) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
    }

    public String getAttribute(String str) {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public boolean hasAttributes() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return false;
    }

    public NodeList getElementsByTagNameNS(String str, String str2) {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Attr setAttributeNodeNS(Attr attr) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Attr getAttributeNodeNS(String str, String str2) {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public void removeAttributeNS(String str, String str2) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
    }

    public void setAttributeNS(String str, String str2, String str3) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
    }

    public String getAttributeNS(String str, String str2) {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Node getPreviousSibling() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Node cloneNode(boolean z) {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public String getNodeValue() throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public void setNodeValue(String str) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
    }

    public void setValue(String str) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
    }

    public Element getOwnerElement() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public boolean getSpecified() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return false;
    }

    public NamedNodeMap getAttributes() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Node insertBefore(Node node, Node node2) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Node replaceChild(Node node, Node node2) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Node removeChild(Node node) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public String getNamespaceURI() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public String getPrefix() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public void setPrefix(String str) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
    }

    public String getLocalName() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public DocumentType getDoctype() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public DOMImplementation getImplementation() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Element getDocumentElement() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Element createElement(String str) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public DocumentFragment createDocumentFragment() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Text createTextNode(String str) {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Comment createComment(String str) {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public CDATASection createCDATASection(String str) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public ProcessingInstruction createProcessingInstruction(String str, String str2) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Attr createAttribute(String str) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public EntityReference createEntityReference(String str) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Node importNode(Node node, boolean z) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Element createElementNS(String str, String str2) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Attr createAttributeNS(String str, String str2) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Element getElementById(String str) {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public void setData(String str) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
    }

    public String substringData(int i, int i2) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public void appendData(String str) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
    }

    public void insertData(int i, String str) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
    }

    public void deleteData(int i, int i2) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
    }

    public void replaceData(int i, int i2, String str) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
    }

    public Text splitText(int i) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public Node adoptNode(Node node) throws DOMException {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public String getInputEncoding() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public void setInputEncoding(String str) {
        error("ER_FUNCTION_NOT_SUPPORTED");
    }

    public boolean getStandalone() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return false;
    }

    public void setStandalone(boolean z) {
        error("ER_FUNCTION_NOT_SUPPORTED");
    }

    public boolean getStrictErrorChecking() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return false;
    }

    public void setStrictErrorChecking(boolean z) {
        error("ER_FUNCTION_NOT_SUPPORTED");
    }

    public String getVersion() {
        error("ER_FUNCTION_NOT_SUPPORTED");
        return null;
    }

    public void setVersion(String str) {
        error("ER_FUNCTION_NOT_SUPPORTED");
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
            String prefix = getPrefix();
            if (namespaceURI != null) {
                if (str == null && prefix == str) {
                    return namespaceURI;
                }
                if (prefix != null && prefix.equals(str)) {
                    return namespaceURI;
                }
            }
            if (hasAttributes()) {
                NamedNodeMap attributes = getAttributes();
                int length = attributes.getLength();
                for (int i = 0; i < length; i++) {
                    Node item = attributes.item(i);
                    String prefix2 = item.getPrefix();
                    String nodeValue = item.getNodeValue();
                    String namespaceURI2 = item.getNamespaceURI();
                    if (namespaceURI2 != null && namespaceURI2.equals("http://www.w3.org/2000/xmlns/")) {
                        if (str == null && item.getNodeName().equals("xmlns")) {
                            return nodeValue;
                        }
                        if (prefix2 != null && prefix2.equals("xmlns") && item.getLocalName().equals(str)) {
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

    public void setDocumentURI(String str) {
        this.fDocumentURI = str;
    }

    public String getDocumentURI() {
        return this.fDocumentURI;
    }

    public String getActualEncoding() {
        return this.actualEncoding;
    }

    public void setActualEncoding(String str) {
        this.actualEncoding = str;
    }

    public String getXmlEncoding() {
        return this.xmlEncoding;
    }

    public void setXmlEncoding(String str) {
        this.xmlEncoding = str;
    }

    public boolean getXmlStandalone() {
        return this.xmlStandalone;
    }

    public void setXmlStandalone(boolean z) throws DOMException {
        this.xmlStandalone = z;
    }

    public String getXmlVersion() {
        return this.xmlVersion;
    }

    public void setXmlVersion(String str) throws DOMException {
        this.xmlVersion = str;
    }
}
