package org.apache.xml.dtm;

import javax.xml.transform.SourceLocator;
import org.apache.xml.utils.XMLString;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

public interface DTM {
    public static final short ATTRIBUTE_NODE = (short) 2;
    public static final short CDATA_SECTION_NODE = (short) 4;
    public static final short COMMENT_NODE = (short) 8;
    public static final short DOCUMENT_FRAGMENT_NODE = (short) 11;
    public static final short DOCUMENT_NODE = (short) 9;
    public static final short DOCUMENT_TYPE_NODE = (short) 10;
    public static final short ELEMENT_NODE = (short) 1;
    public static final short ENTITY_NODE = (short) 6;
    public static final short ENTITY_REFERENCE_NODE = (short) 5;
    public static final short NAMESPACE_NODE = (short) 13;
    public static final short NOTATION_NODE = (short) 12;
    public static final short NTYPES = (short) 14;
    public static final int NULL = -1;
    public static final short PROCESSING_INSTRUCTION_NODE = (short) 7;
    public static final short ROOT_NODE = (short) 0;
    public static final short TEXT_NODE = (short) 3;

    void appendChild(int i, boolean z, boolean z2);

    void appendTextChild(String str);

    void dispatchCharactersEvents(int i, ContentHandler contentHandler, boolean z) throws SAXException;

    void dispatchToEvents(int i, ContentHandler contentHandler) throws SAXException;

    void documentRegistration();

    void documentRelease();

    int getAttributeNode(int i, String str, String str2);

    DTMAxisIterator getAxisIterator(int i);

    DTMAxisTraverser getAxisTraverser(int i);

    ContentHandler getContentHandler();

    DTDHandler getDTDHandler();

    DeclHandler getDeclHandler();

    int getDocument();

    boolean getDocumentAllDeclarationsProcessed();

    String getDocumentBaseURI();

    String getDocumentEncoding(int i);

    int getDocumentRoot(int i);

    String getDocumentStandalone(int i);

    String getDocumentSystemIdentifier(int i);

    String getDocumentTypeDeclarationPublicIdentifier();

    String getDocumentTypeDeclarationSystemIdentifier();

    String getDocumentVersion(int i);

    int getElementById(String str);

    EntityResolver getEntityResolver();

    ErrorHandler getErrorHandler();

    int getExpandedTypeID(int i);

    int getExpandedTypeID(String str, String str2, int i);

    int getFirstAttribute(int i);

    int getFirstChild(int i);

    int getFirstNamespaceNode(int i, boolean z);

    int getLastChild(int i);

    short getLevel(int i);

    LexicalHandler getLexicalHandler();

    String getLocalName(int i);

    String getLocalNameFromExpandedNameID(int i);

    String getNamespaceFromExpandedNameID(int i);

    String getNamespaceURI(int i);

    int getNextAttribute(int i);

    int getNextNamespaceNode(int i, int i2, boolean z);

    int getNextSibling(int i);

    Node getNode(int i);

    String getNodeName(int i);

    String getNodeNameX(int i);

    short getNodeType(int i);

    String getNodeValue(int i);

    int getOwnerDocument(int i);

    int getParent(int i);

    String getPrefix(int i);

    int getPreviousSibling(int i);

    SourceLocator getSourceLocatorFor(int i);

    XMLString getStringValue(int i);

    char[] getStringValueChunk(int i, int i2, int[] iArr);

    int getStringValueChunkCount(int i);

    DTMAxisIterator getTypedAxisIterator(int i, int i2);

    String getUnparsedEntityURI(String str);

    boolean hasChildNodes(int i);

    boolean isAttributeSpecified(int i);

    boolean isCharacterElementContentWhitespace(int i);

    boolean isDocumentAllDeclarationsProcessed(int i);

    boolean isNodeAfter(int i, int i2);

    boolean isSupported(String str, String str2);

    void migrateTo(DTMManager dTMManager);

    boolean needsTwoThreads();

    void setDocumentBaseURI(String str);

    void setFeature(String str, boolean z);

    void setProperty(String str, Object obj);

    boolean supportsPreStripping();
}
