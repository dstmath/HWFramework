package ohos.org.xml.sax;

public class HandlerBase implements EntityResolver, DTDHandler, DocumentHandler, ErrorHandler {
    @Override // ohos.org.xml.sax.DocumentHandler
    public void characters(char[] cArr, int i, int i2) throws SAXException {
    }

    @Override // ohos.org.xml.sax.DocumentHandler
    public void endDocument() throws SAXException {
    }

    @Override // ohos.org.xml.sax.DocumentHandler
    public void endElement(String str) throws SAXException {
    }

    @Override // ohos.org.xml.sax.ErrorHandler
    public void error(SAXParseException sAXParseException) throws SAXException {
    }

    @Override // ohos.org.xml.sax.DocumentHandler
    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
    }

    @Override // ohos.org.xml.sax.DTDHandler
    public void notationDecl(String str, String str2, String str3) {
    }

    @Override // ohos.org.xml.sax.DocumentHandler
    public void processingInstruction(String str, String str2) throws SAXException {
    }

    @Override // ohos.org.xml.sax.EntityResolver
    public InputSource resolveEntity(String str, String str2) throws SAXException {
        return null;
    }

    @Override // ohos.org.xml.sax.DocumentHandler
    public void setDocumentLocator(Locator locator) {
    }

    @Override // ohos.org.xml.sax.DocumentHandler
    public void startDocument() throws SAXException {
    }

    @Override // ohos.org.xml.sax.DocumentHandler
    public void startElement(String str, AttributeList attributeList) throws SAXException {
    }

    @Override // ohos.org.xml.sax.DTDHandler
    public void unparsedEntityDecl(String str, String str2, String str3, String str4) {
    }

    @Override // ohos.org.xml.sax.ErrorHandler
    public void warning(SAXParseException sAXParseException) throws SAXException {
    }

    @Override // ohos.org.xml.sax.ErrorHandler
    public void fatalError(SAXParseException sAXParseException) throws SAXException {
        throw sAXParseException;
    }
}
