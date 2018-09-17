package org.w3c.dom;

public interface Document extends Node {
    Node adoptNode(Node node) throws DOMException;

    Attr createAttribute(String str) throws DOMException;

    Attr createAttributeNS(String str, String str2) throws DOMException;

    CDATASection createCDATASection(String str) throws DOMException;

    Comment createComment(String str);

    DocumentFragment createDocumentFragment();

    Element createElement(String str) throws DOMException;

    Element createElementNS(String str, String str2) throws DOMException;

    EntityReference createEntityReference(String str) throws DOMException;

    ProcessingInstruction createProcessingInstruction(String str, String str2) throws DOMException;

    Text createTextNode(String str);

    DocumentType getDoctype();

    Element getDocumentElement();

    String getDocumentURI();

    DOMConfiguration getDomConfig();

    Element getElementById(String str);

    NodeList getElementsByTagName(String str);

    NodeList getElementsByTagNameNS(String str, String str2);

    DOMImplementation getImplementation();

    String getInputEncoding();

    boolean getStrictErrorChecking();

    String getXmlEncoding();

    boolean getXmlStandalone();

    String getXmlVersion();

    Node importNode(Node node, boolean z) throws DOMException;

    void normalizeDocument();

    Node renameNode(Node node, String str, String str2) throws DOMException;

    void setDocumentURI(String str);

    void setStrictErrorChecking(boolean z);

    void setXmlStandalone(boolean z) throws DOMException;

    void setXmlVersion(String str) throws DOMException;
}
