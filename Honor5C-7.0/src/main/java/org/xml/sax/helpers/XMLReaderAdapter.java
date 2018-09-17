package org.xml.sax.helpers;

import java.io.IOException;
import java.util.Locale;
import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

public class XMLReaderAdapter implements Parser, ContentHandler {
    DocumentHandler documentHandler;
    AttributesAdapter qAtts;
    XMLReader xmlReader;

    static final class AttributesAdapter implements AttributeList {
        private Attributes attributes;

        AttributesAdapter() {
        }

        void setAttributes(Attributes attributes) {
            this.attributes = attributes;
        }

        public int getLength() {
            return this.attributes.getLength();
        }

        public String getName(int i) {
            return this.attributes.getQName(i);
        }

        public String getType(int i) {
            return this.attributes.getType(i);
        }

        public String getValue(int i) {
            return this.attributes.getValue(i);
        }

        public String getType(String qName) {
            return this.attributes.getType(qName);
        }

        public String getValue(String qName) {
            return this.attributes.getValue(qName);
        }
    }

    public XMLReaderAdapter() throws SAXException {
        setup(XMLReaderFactory.createXMLReader());
    }

    public XMLReaderAdapter(XMLReader xmlReader) {
        setup(xmlReader);
    }

    private void setup(XMLReader xmlReader) {
        if (xmlReader == null) {
            throw new NullPointerException("XMLReader must not be null");
        }
        this.xmlReader = xmlReader;
        this.qAtts = new AttributesAdapter();
    }

    public void setLocale(Locale locale) throws SAXException {
        throw new SAXNotSupportedException("setLocale not supported");
    }

    public void setEntityResolver(EntityResolver resolver) {
        this.xmlReader.setEntityResolver(resolver);
    }

    public void setDTDHandler(DTDHandler handler) {
        this.xmlReader.setDTDHandler(handler);
    }

    public void setDocumentHandler(DocumentHandler handler) {
        this.documentHandler = handler;
    }

    public void setErrorHandler(ErrorHandler handler) {
        this.xmlReader.setErrorHandler(handler);
    }

    public void parse(String systemId) throws IOException, SAXException {
        parse(new InputSource(systemId));
    }

    public void parse(InputSource input) throws IOException, SAXException {
        setupXMLReader();
        this.xmlReader.parse(input);
    }

    private void setupXMLReader() throws SAXException {
        this.xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        try {
            this.xmlReader.setFeature("http://xml.org/sax/features/namespaces", false);
        } catch (SAXException e) {
        }
        this.xmlReader.setContentHandler(this);
    }

    public void setDocumentLocator(Locator locator) {
        if (this.documentHandler != null) {
            this.documentHandler.setDocumentLocator(locator);
        }
    }

    public void startDocument() throws SAXException {
        if (this.documentHandler != null) {
            this.documentHandler.startDocument();
        }
    }

    public void endDocument() throws SAXException {
        if (this.documentHandler != null) {
            this.documentHandler.endDocument();
        }
    }

    public void startPrefixMapping(String prefix, String uri) {
    }

    public void endPrefixMapping(String prefix) {
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (this.documentHandler != null) {
            this.qAtts.setAttributes(atts);
            this.documentHandler.startElement(qName, this.qAtts);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (this.documentHandler != null) {
            this.documentHandler.endElement(qName);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.documentHandler != null) {
            this.documentHandler.characters(ch, start, length);
        }
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        if (this.documentHandler != null) {
            this.documentHandler.ignorableWhitespace(ch, start, length);
        }
    }

    public void processingInstruction(String target, String data) throws SAXException {
        if (this.documentHandler != null) {
            this.documentHandler.processingInstruction(target, data);
        }
    }

    public void skippedEntity(String name) throws SAXException {
    }
}
