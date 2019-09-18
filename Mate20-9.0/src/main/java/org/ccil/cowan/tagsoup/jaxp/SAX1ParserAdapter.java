package org.ccil.cowan.tagsoup.jaxp;

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

public class SAX1ParserAdapter implements Parser {
    final XMLReader xmlReader;

    static final class AttributesWrapper implements AttributeList {
        Attributes attrs;

        public void setAttributes(Attributes a) {
            this.attrs = a;
        }

        public int getLength() {
            return this.attrs.getLength();
        }

        public String getName(int i) {
            String n = this.attrs.getQName(i);
            return n == null ? this.attrs.getLocalName(i) : n;
        }

        public String getType(int i) {
            return this.attrs.getType(i);
        }

        public String getType(String name) {
            return this.attrs.getType(name);
        }

        public String getValue(int i) {
            return this.attrs.getValue(i);
        }

        public String getValue(String name) {
            return this.attrs.getValue(name);
        }
    }

    static final class DocHandlerWrapper implements ContentHandler {
        final DocumentHandler docHandler;
        final AttributesWrapper mAttrWrapper = new AttributesWrapper();

        DocHandlerWrapper(DocumentHandler h) {
            this.docHandler = h;
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            this.docHandler.characters(ch, start, length);
        }

        public void endDocument() throws SAXException {
            this.docHandler.endDocument();
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName == null) {
                qName = localName;
            }
            this.docHandler.endElement(qName);
        }

        public void endPrefixMapping(String prefix) {
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            this.docHandler.ignorableWhitespace(ch, start, length);
        }

        public void processingInstruction(String target, String data) throws SAXException {
            this.docHandler.processingInstruction(target, data);
        }

        public void setDocumentLocator(Locator locator) {
            this.docHandler.setDocumentLocator(locator);
        }

        public void skippedEntity(String name) {
        }

        public void startDocument() throws SAXException {
            this.docHandler.startDocument();
        }

        public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
            if (qName == null) {
                qName = localName;
            }
            this.mAttrWrapper.setAttributes(attrs);
            this.docHandler.startElement(qName, this.mAttrWrapper);
        }

        public void startPrefixMapping(String prefix, String uri) {
        }
    }

    public SAX1ParserAdapter(XMLReader xr) {
        this.xmlReader = xr;
    }

    public void parse(InputSource source) throws SAXException {
        try {
            this.xmlReader.parse(source);
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    public void parse(String systemId) throws SAXException {
        try {
            this.xmlReader.parse(systemId);
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    public void setDocumentHandler(DocumentHandler h) {
        this.xmlReader.setContentHandler(new DocHandlerWrapper(h));
    }

    public void setDTDHandler(DTDHandler h) {
        this.xmlReader.setDTDHandler(h);
    }

    public void setEntityResolver(EntityResolver r) {
        this.xmlReader.setEntityResolver(r);
    }

    public void setErrorHandler(ErrorHandler h) {
        this.xmlReader.setErrorHandler(h);
    }

    public void setLocale(Locale locale) throws SAXException {
        throw new SAXNotSupportedException("TagSoup does not implement setLocale() method");
    }
}
