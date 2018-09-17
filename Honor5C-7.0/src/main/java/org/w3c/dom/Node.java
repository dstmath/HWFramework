package org.w3c.dom;

public interface Node {
    public static final short ATTRIBUTE_NODE = (short) 2;
    public static final short CDATA_SECTION_NODE = (short) 4;
    public static final short COMMENT_NODE = (short) 8;
    public static final short DOCUMENT_FRAGMENT_NODE = (short) 11;
    public static final short DOCUMENT_NODE = (short) 9;
    public static final short DOCUMENT_POSITION_CONTAINED_BY = (short) 16;
    public static final short DOCUMENT_POSITION_CONTAINS = (short) 8;
    public static final short DOCUMENT_POSITION_DISCONNECTED = (short) 1;
    public static final short DOCUMENT_POSITION_FOLLOWING = (short) 4;
    public static final short DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC = (short) 32;
    public static final short DOCUMENT_POSITION_PRECEDING = (short) 2;
    public static final short DOCUMENT_TYPE_NODE = (short) 10;
    public static final short ELEMENT_NODE = (short) 1;
    public static final short ENTITY_NODE = (short) 6;
    public static final short ENTITY_REFERENCE_NODE = (short) 5;
    public static final short NOTATION_NODE = (short) 12;
    public static final short PROCESSING_INSTRUCTION_NODE = (short) 7;
    public static final short TEXT_NODE = (short) 3;

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
