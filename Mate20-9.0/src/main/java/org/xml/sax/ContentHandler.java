package org.xml.sax;

public interface ContentHandler {
    void characters(char[] cArr, int i, int i2) throws SAXException;

    void endDocument() throws SAXException;

    void endElement(String str, String str2, String str3) throws SAXException;

    void endPrefixMapping(String str) throws SAXException;

    void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException;

    void processingInstruction(String str, String str2) throws SAXException;

    void setDocumentLocator(Locator locator);

    void skippedEntity(String str) throws SAXException;

    void startDocument() throws SAXException;

    void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException;

    void startPrefixMapping(String str, String str2) throws SAXException;
}
