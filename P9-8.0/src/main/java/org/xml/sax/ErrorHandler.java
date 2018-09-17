package org.xml.sax;

public interface ErrorHandler {
    void error(SAXParseException sAXParseException) throws SAXException;

    void fatalError(SAXParseException sAXParseException) throws SAXException;

    void warning(SAXParseException sAXParseException) throws SAXException;
}
