package org.apache.xml.serializer;

import javax.xml.transform.SourceLocator;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public interface ExtendedContentHandler extends ContentHandler {
    public static final int HTML_ATTREMPTY = 2;
    public static final int HTML_ATTRURL = 4;
    public static final int NO_BAD_CHARS = 1;

    void addAttribute(String str, String str2);

    void addAttribute(String str, String str2, String str3, String str4, String str5) throws SAXException;

    void addAttribute(String str, String str2, String str3, String str4, String str5, boolean z) throws SAXException;

    void addAttributes(Attributes attributes) throws SAXException;

    void addUniqueAttribute(String str, String str2, int i) throws SAXException;

    void addXSLAttribute(String str, String str2, String str3);

    void characters(String str) throws SAXException;

    void characters(Node node) throws SAXException;

    void endElement(String str) throws SAXException;

    void entityReference(String str) throws SAXException;

    NamespaceMappings getNamespaceMappings();

    String getNamespaceURI(String str, boolean z);

    String getNamespaceURIFromPrefix(String str);

    String getPrefix(String str);

    void namespaceAfterStartElement(String str, String str2) throws SAXException;

    void setSourceLocator(SourceLocator sourceLocator);

    void startElement(String str) throws SAXException;

    void startElement(String str, String str2, String str3) throws SAXException;

    boolean startPrefixMapping(String str, String str2, boolean z) throws SAXException;
}
