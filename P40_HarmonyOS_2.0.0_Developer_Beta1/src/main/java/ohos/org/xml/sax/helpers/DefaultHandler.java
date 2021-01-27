package ohos.org.xml.sax.helpers;

import java.io.IOException;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXParseException;

public class DefaultHandler implements EntityResolver, DTDHandler, ContentHandler, ErrorHandler {
    @Override // ohos.org.xml.sax.ContentHandler
    public void characters(char[] cArr, int i, int i2) throws SAXException {
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void endDocument() throws SAXException {
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void endElement(String str, String str2, String str3) throws SAXException {
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void endPrefixMapping(String str) throws SAXException {
    }

    @Override // ohos.org.xml.sax.ErrorHandler
    public void error(SAXParseException sAXParseException) throws SAXException {
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
    }

    @Override // ohos.org.xml.sax.DTDHandler
    public void notationDecl(String str, String str2, String str3) throws SAXException {
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void processingInstruction(String str, String str2) throws SAXException {
    }

    @Override // ohos.org.xml.sax.EntityResolver
    public InputSource resolveEntity(String str, String str2) throws IOException, SAXException {
        return null;
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void setDocumentLocator(Locator locator) {
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void skippedEntity(String str) throws SAXException {
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void startDocument() throws SAXException {
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void startPrefixMapping(String str, String str2) throws SAXException {
    }

    @Override // ohos.org.xml.sax.DTDHandler
    public void unparsedEntityDecl(String str, String str2, String str3, String str4) throws SAXException {
    }

    @Override // ohos.org.xml.sax.ErrorHandler
    public void warning(SAXParseException sAXParseException) throws SAXException {
    }

    @Override // ohos.org.xml.sax.ErrorHandler
    public void fatalError(SAXParseException sAXParseException) throws SAXException {
        throw sAXParseException;
    }
}
