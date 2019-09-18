package org.w3c.dom;

public interface Node {
    public static final short ATTRIBUTE_NODE = 2;
    public static final short CDATA_SECTION_NODE = 4;
    public static final short COMMENT_NODE = 8;
    public static final short DOCUMENT_FRAGMENT_NODE = 11;
    public static final short DOCUMENT_NODE = 9;
    public static final short DOCUMENT_POSITION_CONTAINED_BY = 16;
    public static final short DOCUMENT_POSITION_CONTAINS = 8;
    public static final short DOCUMENT_POSITION_DISCONNECTED = 1;
    public static final short DOCUMENT_POSITION_FOLLOWING = 4;
    public static final short DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC = 32;
    public static final short DOCUMENT_POSITION_PRECEDING = 2;
    public static final short DOCUMENT_TYPE_NODE = 10;
    public static final short ELEMENT_NODE = 1;
    public static final short ENTITY_NODE = 6;
    public static final short ENTITY_REFERENCE_NODE = 5;
    public static final short NOTATION_NODE = 12;
    public static final short PROCESSING_INSTRUCTION_NODE = 7;
    public static final short TEXT_NODE = 3;

    Node appendChild(Node node) throws DOMException;

    Node cloneNode(boolean z);

    short compareDocumentPosition(Node node) throws DOMException;

    NamedNodeMap getAttributes();

    String getBaseURI();

    NodeList getChildNodes();

    Object getFeature(String str, String str2);

    Node getFirstChild();

    Node getLastChild();

    String getLocalName();

    String getNamespaceURI();

    Node getNextSibling();

    String getNodeName();

    short getNodeType();

    String getNodeValue() throws DOMException;

    Document getOwnerDocument();

    Node getParentNode();

    String getPrefix();

    Node getPreviousSibling();

    String getTextContent() throws DOMException;

    Object getUserData(String str);

    boolean hasAttributes();

    boolean hasChildNodes();

    Node insertBefore(Node node, Node node2) throws DOMException;

    boolean isDefaultNamespace(String str);

    boolean isEqualNode(Node node);

    boolean isSameNode(Node node);

    boolean isSupported(String str, String str2);

    String lookupNamespaceURI(String str);

    String lookupPrefix(String str);

    void normalize();

    Node removeChild(Node node) throws DOMException;

    Node replaceChild(Node node, Node node2) throws DOMException;

    void setNodeValue(String str) throws DOMException;

    void setPrefix(String str) throws DOMException;

    void setTextContent(String str) throws DOMException;

    Object setUserData(String str, Object obj, UserDataHandler userDataHandler);
}
