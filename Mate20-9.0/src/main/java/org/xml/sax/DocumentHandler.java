package org.xml.sax;

@Deprecated
public interface DocumentHandler {
    void characters(char[] cArr, int i, int i2) throws SAXException;

    void endDocument() throws SAXException;

    void endElement(String str) throws SAXException;

    void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException;

    void processingInstruction(String str, String str2) throws SAXException;

    void setDocumentLocator(Locator locator);

    void startDocument() throws SAXException;

    void startElement(String str, AttributeList attributeList) throws SAXException;
}
