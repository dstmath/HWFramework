package org.xml.sax.helpers;

import java.io.IOException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

public class XMLFilterImpl implements XMLFilter, EntityResolver, DTDHandler, ContentHandler, ErrorHandler {
    private ContentHandler contentHandler = null;
    private DTDHandler dtdHandler = null;
    private EntityResolver entityResolver = null;
    private ErrorHandler errorHandler = null;
    private Locator locator = null;
    private XMLReader parent = null;

    public XMLFilterImpl(XMLReader parent) {
        setParent(parent);
    }

    public void setParent(XMLReader parent) {
        this.parent = parent;
    }

    public XMLReader getParent() {
        return this.parent;
    }

    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (this.parent != null) {
            this.parent.setFeature(name, value);
            return;
        }
        throw new SAXNotRecognizedException("Feature: " + name);
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (this.parent != null) {
            return this.parent.getFeature(name);
        }
        throw new SAXNotRecognizedException("Feature: " + name);
    }

    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (this.parent != null) {
            this.parent.setProperty(name, value);
            return;
        }
        throw new SAXNotRecognizedException("Property: " + name);
    }

    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (this.parent != null) {
            return this.parent.getProperty(name);
        }
        throw new SAXNotRecognizedException("Property: " + name);
    }

    public void setEntityResolver(EntityResolver resolver) {
        this.entityResolver = resolver;
    }

    public EntityResolver getEntityResolver() {
        return this.entityResolver;
    }

    public void setDTDHandler(DTDHandler handler) {
        this.dtdHandler = handler;
    }

    public DTDHandler getDTDHandler() {
        return this.dtdHandler;
    }

    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
    }

    public ContentHandler getContentHandler() {
        return this.contentHandler;
    }

    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }

    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    public void parse(InputSource input) throws SAXException, IOException {
        setupParse();
        this.parent.parse(input);
    }

    public void parse(String systemId) throws SAXException, IOException {
        parse(new InputSource(systemId));
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (this.entityResolver != null) {
            return this.entityResolver.resolveEntity(publicId, systemId);
        }
        return null;
    }

    public void notationDecl(String name, String publicId, String systemId) throws SAXException {
        if (this.dtdHandler != null) {
            this.dtdHandler.notationDecl(name, publicId, systemId);
        }
    }

    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
        if (this.dtdHandler != null) {
            this.dtdHandler.unparsedEntityDecl(name, publicId, systemId, notationName);
        }
    }

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
        if (this.contentHandler != null) {
            this.contentHandler.setDocumentLocator(locator);
        }
    }

    public void startDocument() throws SAXException {
        if (this.contentHandler != null) {
            this.contentHandler.startDocument();
        }
    }

    public void endDocument() throws SAXException {
        if (this.contentHandler != null) {
            this.contentHandler.endDocument();
        }
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (this.contentHandler != null) {
            this.contentHandler.startPrefixMapping(prefix, uri);
        }
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        if (this.contentHandler != null) {
            this.contentHandler.endPrefixMapping(prefix);
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (this.contentHandler != null) {
            this.contentHandler.startElement(uri, localName, qName, atts);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (this.contentHandler != null) {
            this.contentHandler.endElement(uri, localName, qName);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.contentHandler != null) {
            this.contentHandler.characters(ch, start, length);
        }
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        if (this.contentHandler != null) {
            this.contentHandler.ignorableWhitespace(ch, start, length);
        }
    }

    public void processingInstruction(String target, String data) throws SAXException {
        if (this.contentHandler != null) {
            this.contentHandler.processingInstruction(target, data);
        }
    }

    public void skippedEntity(String name) throws SAXException {
        if (this.contentHandler != null) {
            this.contentHandler.skippedEntity(name);
        }
    }

    public void warning(SAXParseException e) throws SAXException {
        if (this.errorHandler != null) {
            this.errorHandler.warning(e);
        }
    }

    public void error(SAXParseException e) throws SAXException {
        if (this.errorHandler != null) {
            this.errorHandler.error(e);
        }
    }

    public void fatalError(SAXParseException e) throws SAXException {
        if (this.errorHandler != null) {
            this.errorHandler.fatalError(e);
        }
    }

    private void setupParse() {
        if (this.parent == null) {
            throw new NullPointerException("No parent for filter");
        }
        this.parent.setEntityResolver(this);
        this.parent.setDTDHandler(this);
        this.parent.setContentHandler(this);
        this.parent.setErrorHandler(this);
    }
}
