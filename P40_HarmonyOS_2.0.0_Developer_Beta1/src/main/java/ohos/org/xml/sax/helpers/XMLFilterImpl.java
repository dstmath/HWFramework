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
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;
import ohos.org.xml.sax.SAXParseException;
import ohos.org.xml.sax.XMLFilter;
import ohos.org.xml.sax.XMLReader;

public class XMLFilterImpl implements XMLFilter, EntityResolver, DTDHandler, ContentHandler, ErrorHandler {
    private ContentHandler contentHandler = null;
    private DTDHandler dtdHandler = null;
    private EntityResolver entityResolver = null;
    private ErrorHandler errorHandler = null;
    private Locator locator = null;
    private XMLReader parent = null;

    public XMLFilterImpl() {
    }

    public XMLFilterImpl(XMLReader xMLReader) {
        setParent(xMLReader);
    }

    @Override // ohos.org.xml.sax.XMLFilter
    public void setParent(XMLReader xMLReader) {
        this.parent = xMLReader;
    }

    @Override // ohos.org.xml.sax.XMLFilter
    public XMLReader getParent() {
        return this.parent;
    }

    @Override // ohos.org.xml.sax.XMLReader
    public void setFeature(String str, boolean z) throws SAXNotRecognizedException, SAXNotSupportedException {
        XMLReader xMLReader = this.parent;
        if (xMLReader != null) {
            xMLReader.setFeature(str, z);
            return;
        }
        throw new SAXNotRecognizedException("Feature: " + str);
    }

    @Override // ohos.org.xml.sax.XMLReader
    public boolean getFeature(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        XMLReader xMLReader = this.parent;
        if (xMLReader != null) {
            return xMLReader.getFeature(str);
        }
        throw new SAXNotRecognizedException("Feature: " + str);
    }

    @Override // ohos.org.xml.sax.XMLReader
    public void setProperty(String str, Object obj) throws SAXNotRecognizedException, SAXNotSupportedException {
        XMLReader xMLReader = this.parent;
        if (xMLReader != null) {
            xMLReader.setProperty(str, obj);
            return;
        }
        throw new SAXNotRecognizedException("Property: " + str);
    }

    @Override // ohos.org.xml.sax.XMLReader
    public Object getProperty(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        XMLReader xMLReader = this.parent;
        if (xMLReader != null) {
            return xMLReader.getProperty(str);
        }
        throw new SAXNotRecognizedException("Property: " + str);
    }

    @Override // ohos.org.xml.sax.XMLReader
    public void setEntityResolver(EntityResolver entityResolver2) {
        this.entityResolver = entityResolver2;
    }

    @Override // ohos.org.xml.sax.XMLReader
    public EntityResolver getEntityResolver() {
        return this.entityResolver;
    }

    @Override // ohos.org.xml.sax.XMLReader
    public void setDTDHandler(DTDHandler dTDHandler) {
        this.dtdHandler = dTDHandler;
    }

    @Override // ohos.org.xml.sax.XMLReader
    public DTDHandler getDTDHandler() {
        return this.dtdHandler;
    }

    @Override // ohos.org.xml.sax.XMLReader
    public void setContentHandler(ContentHandler contentHandler2) {
        this.contentHandler = contentHandler2;
    }

    @Override // ohos.org.xml.sax.XMLReader
    public ContentHandler getContentHandler() {
        return this.contentHandler;
    }

    @Override // ohos.org.xml.sax.XMLReader
    public void setErrorHandler(ErrorHandler errorHandler2) {
        this.errorHandler = errorHandler2;
    }

    @Override // ohos.org.xml.sax.XMLReader
    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    @Override // ohos.org.xml.sax.XMLReader
    public void parse(InputSource inputSource) throws SAXException, IOException {
        setupParse();
        this.parent.parse(inputSource);
    }

    @Override // ohos.org.xml.sax.XMLReader
    public void parse(String str) throws SAXException, IOException {
        parse(new InputSource(str));
    }

    @Override // ohos.org.xml.sax.EntityResolver
    public InputSource resolveEntity(String str, String str2) throws SAXException, IOException {
        EntityResolver entityResolver2 = this.entityResolver;
        if (entityResolver2 != null) {
            return entityResolver2.resolveEntity(str, str2);
        }
        return null;
    }

    @Override // ohos.org.xml.sax.DTDHandler
    public void notationDecl(String str, String str2, String str3) throws SAXException {
        DTDHandler dTDHandler = this.dtdHandler;
        if (dTDHandler != null) {
            dTDHandler.notationDecl(str, str2, str3);
        }
    }

    @Override // ohos.org.xml.sax.DTDHandler
    public void unparsedEntityDecl(String str, String str2, String str3, String str4) throws SAXException {
        DTDHandler dTDHandler = this.dtdHandler;
        if (dTDHandler != null) {
            dTDHandler.unparsedEntityDecl(str, str2, str3, str4);
        }
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void setDocumentLocator(Locator locator2) {
        this.locator = locator2;
        ContentHandler contentHandler2 = this.contentHandler;
        if (contentHandler2 != null) {
            contentHandler2.setDocumentLocator(locator2);
        }
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void startDocument() throws SAXException {
        ContentHandler contentHandler2 = this.contentHandler;
        if (contentHandler2 != null) {
            contentHandler2.startDocument();
        }
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void endDocument() throws SAXException {
        ContentHandler contentHandler2 = this.contentHandler;
        if (contentHandler2 != null) {
            contentHandler2.endDocument();
        }
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void startPrefixMapping(String str, String str2) throws SAXException {
        ContentHandler contentHandler2 = this.contentHandler;
        if (contentHandler2 != null) {
            contentHandler2.startPrefixMapping(str, str2);
        }
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void endPrefixMapping(String str) throws SAXException {
        ContentHandler contentHandler2 = this.contentHandler;
        if (contentHandler2 != null) {
            contentHandler2.endPrefixMapping(str);
        }
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        ContentHandler contentHandler2 = this.contentHandler;
        if (contentHandler2 != null) {
            contentHandler2.startElement(str, str2, str3, attributes);
        }
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void endElement(String str, String str2, String str3) throws SAXException {
        ContentHandler contentHandler2 = this.contentHandler;
        if (contentHandler2 != null) {
            contentHandler2.endElement(str, str2, str3);
        }
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void characters(char[] cArr, int i, int i2) throws SAXException {
        ContentHandler contentHandler2 = this.contentHandler;
        if (contentHandler2 != null) {
            contentHandler2.characters(cArr, i, i2);
        }
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
        ContentHandler contentHandler2 = this.contentHandler;
        if (contentHandler2 != null) {
            contentHandler2.ignorableWhitespace(cArr, i, i2);
        }
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void processingInstruction(String str, String str2) throws SAXException {
        ContentHandler contentHandler2 = this.contentHandler;
        if (contentHandler2 != null) {
            contentHandler2.processingInstruction(str, str2);
        }
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void skippedEntity(String str) throws SAXException {
        ContentHandler contentHandler2 = this.contentHandler;
        if (contentHandler2 != null) {
            contentHandler2.skippedEntity(str);
        }
    }

    @Override // ohos.org.xml.sax.ErrorHandler
    public void warning(SAXParseException sAXParseException) throws SAXException {
        ErrorHandler errorHandler2 = this.errorHandler;
        if (errorHandler2 != null) {
            errorHandler2.warning(sAXParseException);
        }
    }

    @Override // ohos.org.xml.sax.ErrorHandler
    public void error(SAXParseException sAXParseException) throws SAXException {
        ErrorHandler errorHandler2 = this.errorHandler;
        if (errorHandler2 != null) {
            errorHandler2.error(sAXParseException);
        }
    }

    @Override // ohos.org.xml.sax.ErrorHandler
    public void fatalError(SAXParseException sAXParseException) throws SAXException {
        ErrorHandler errorHandler2 = this.errorHandler;
        if (errorHandler2 != null) {
            errorHandler2.fatalError(sAXParseException);
        }
    }

    private void setupParse() {
        XMLReader xMLReader = this.parent;
        if (xMLReader != null) {
            xMLReader.setEntityResolver(this);
            this.parent.setDTDHandler(this);
            this.parent.setContentHandler(this);
            this.parent.setErrorHandler(this);
            return;
        }
        throw new NullPointerException("No parent for filter");
    }
}
