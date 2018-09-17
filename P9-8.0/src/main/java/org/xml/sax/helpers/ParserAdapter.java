package org.xml.sax.helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.xml.XMLConstants;
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
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public class ParserAdapter implements XMLReader, DocumentHandler {
    private static final String FEATURES = "http://xml.org/sax/features/";
    private static final String NAMESPACES = "http://xml.org/sax/features/namespaces";
    private static final String NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes";
    private static final String XMLNS_URIs = "http://xml.org/sax/features/xmlns-uris";
    private AttributeListAdapter attAdapter;
    private AttributesImpl atts;
    ContentHandler contentHandler;
    DTDHandler dtdHandler;
    EntityResolver entityResolver;
    ErrorHandler errorHandler;
    Locator locator;
    private String[] nameParts;
    private boolean namespaces;
    private NamespaceSupport nsSupport;
    private Parser parser;
    private boolean parsing;
    private boolean prefixes;
    private boolean uris;

    final class AttributeListAdapter implements Attributes {
        private AttributeList qAtts;

        AttributeListAdapter() {
        }

        void setAttributeList(AttributeList qAtts) {
            this.qAtts = qAtts;
        }

        public int getLength() {
            return this.qAtts.getLength();
        }

        public String getURI(int i) {
            return "";
        }

        public String getLocalName(int i) {
            return "";
        }

        public String getQName(int i) {
            return this.qAtts.getName(i).intern();
        }

        public String getType(int i) {
            return this.qAtts.getType(i).intern();
        }

        public String getValue(int i) {
            return this.qAtts.getValue(i);
        }

        public int getIndex(String uri, String localName) {
            return -1;
        }

        public int getIndex(String qName) {
            int max = ParserAdapter.this.atts.getLength();
            for (int i = 0; i < max; i++) {
                if (this.qAtts.getName(i).equals(qName)) {
                    return i;
                }
            }
            return -1;
        }

        public String getType(String uri, String localName) {
            return null;
        }

        public String getType(String qName) {
            return this.qAtts.getType(qName).intern();
        }

        public String getValue(String uri, String localName) {
            return null;
        }

        public String getValue(String qName) {
            return this.qAtts.getValue(qName);
        }
    }

    public ParserAdapter() throws SAXException {
        this.parsing = false;
        this.nameParts = new String[3];
        this.parser = null;
        this.atts = null;
        this.namespaces = true;
        this.prefixes = false;
        this.uris = false;
        this.entityResolver = null;
        this.dtdHandler = null;
        this.contentHandler = null;
        this.errorHandler = null;
        String driver = System.getProperty("org.xml.sax.parser");
        try {
            setup(ParserFactory.makeParser());
        } catch (ClassNotFoundException e1) {
            throw new SAXException("Cannot find SAX1 driver class " + driver, e1);
        } catch (IllegalAccessException e2) {
            throw new SAXException("SAX1 driver class " + driver + " found but cannot be loaded", e2);
        } catch (InstantiationException e3) {
            throw new SAXException("SAX1 driver class " + driver + " loaded but cannot be instantiated", e3);
        } catch (ClassCastException e) {
            throw new SAXException("SAX1 driver class " + driver + " does not implement org.xml.sax.Parser");
        } catch (NullPointerException e4) {
            throw new SAXException("System property org.xml.sax.parser not specified");
        }
    }

    public ParserAdapter(Parser parser) {
        this.parsing = false;
        this.nameParts = new String[3];
        this.parser = null;
        this.atts = null;
        this.namespaces = true;
        this.prefixes = false;
        this.uris = false;
        this.entityResolver = null;
        this.dtdHandler = null;
        this.contentHandler = null;
        this.errorHandler = null;
        setup(parser);
    }

    private void setup(Parser parser) {
        if (parser == null) {
            throw new NullPointerException("Parser argument must not be null");
        }
        this.parser = parser;
        this.atts = new AttributesImpl();
        this.nsSupport = new NamespaceSupport();
        this.attAdapter = new AttributeListAdapter();
    }

    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name.equals(NAMESPACES)) {
            checkNotParsing("feature", name);
            this.namespaces = value;
            if (!this.namespaces && (this.prefixes ^ 1) != 0) {
                this.prefixes = true;
            }
        } else if (name.equals(NAMESPACE_PREFIXES)) {
            checkNotParsing("feature", name);
            this.prefixes = value;
            if (!this.prefixes && (this.namespaces ^ 1) != 0) {
                this.namespaces = true;
            }
        } else if (name.equals(XMLNS_URIs)) {
            checkNotParsing("feature", name);
            this.uris = value;
        } else {
            throw new SAXNotRecognizedException("Feature: " + name);
        }
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name.equals(NAMESPACES)) {
            return this.namespaces;
        }
        if (name.equals(NAMESPACE_PREFIXES)) {
            return this.prefixes;
        }
        if (name.equals(XMLNS_URIs)) {
            return this.uris;
        }
        throw new SAXNotRecognizedException("Feature: " + name);
    }

    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new SAXNotRecognizedException("Property: " + name);
    }

    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
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

    public void parse(String systemId) throws IOException, SAXException {
        parse(new InputSource(systemId));
    }

    public void parse(InputSource input) throws IOException, SAXException {
        if (this.parsing) {
            throw new SAXException("Parser is already in use");
        }
        setupParser();
        this.parsing = true;
        try {
            this.parser.parse(input);
            this.parsing = false;
        } finally {
            this.parsing = false;
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

    public void startElement(String qName, AttributeList qAtts) throws SAXException {
        Iterable exceptions = null;
        if (this.namespaces) {
            int i;
            String attQName;
            int n;
            String prefix;
            String value;
            this.nsSupport.pushContext();
            int length = qAtts.getLength();
            for (i = 0; i < length; i++) {
                attQName = qAtts.getName(i);
                if (attQName.startsWith(XMLConstants.XMLNS_ATTRIBUTE)) {
                    n = attQName.indexOf(58);
                    if (n == -1 && attQName.length() == 5) {
                        prefix = "";
                    } else if (n == 5) {
                        prefix = attQName.substring(n + 1);
                    }
                    value = qAtts.getValue(i);
                    if (!this.nsSupport.declarePrefix(prefix, value)) {
                        reportError("Illegal Namespace prefix: " + prefix);
                    } else if (this.contentHandler != null) {
                        this.contentHandler.startPrefixMapping(prefix, value);
                    }
                }
            }
            this.atts.clear();
            for (i = 0; i < length; i++) {
                attQName = qAtts.getName(i);
                String type = qAtts.getType(i);
                value = qAtts.getValue(i);
                if (attQName.startsWith(XMLConstants.XMLNS_ATTRIBUTE)) {
                    n = attQName.indexOf(58);
                    if (n == -1 && attQName.length() == 5) {
                        prefix = "";
                    } else if (n != 5) {
                        prefix = null;
                    } else {
                        prefix = attQName.substring(6);
                    }
                    if (prefix != null) {
                        if (this.prefixes) {
                            if (this.uris) {
                                this.atts.addAttribute("http://www.w3.org/XML/1998/namespace", prefix, attQName.intern(), type, value);
                            } else {
                                this.atts.addAttribute("", "", attQName.intern(), type, value);
                            }
                        }
                    }
                }
                try {
                    String[] attName = processName(attQName, true, true);
                    this.atts.addAttribute(attName[0], attName[1], attName[2], type, value);
                } catch (SAXException e) {
                    if (exceptions == null) {
                        exceptions = new ArrayList();
                    }
                    exceptions.add((SAXParseException) e);
                    this.atts.addAttribute("", attQName, attQName, type, value);
                }
            }
            if (!(exceptions == null || this.errorHandler == null)) {
                for (SAXParseException ex : exceptions) {
                    this.errorHandler.error(ex);
                }
            }
            if (this.contentHandler != null) {
                String[] name = processName(qName, false, false);
                this.contentHandler.startElement(name[0], name[1], name[2], this.atts);
            }
            return;
        }
        if (this.contentHandler != null) {
            this.attAdapter.setAttributeList(qAtts);
            this.contentHandler.startElement("", "", qName.intern(), this.attAdapter);
        }
    }

    public void endElement(String qName) throws SAXException {
        if (this.namespaces) {
            String[] names = processName(qName, false, false);
            if (this.contentHandler != null) {
                this.contentHandler.endElement(names[0], names[1], names[2]);
                Enumeration prefixes = this.nsSupport.getDeclaredPrefixes();
                while (prefixes.hasMoreElements()) {
                    this.contentHandler.endPrefixMapping((String) prefixes.nextElement());
                }
            }
            this.nsSupport.popContext();
            return;
        }
        if (this.contentHandler != null) {
            this.contentHandler.endElement("", "", qName.intern());
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

    private void setupParser() {
        if (this.prefixes || (this.namespaces ^ 1) == 0) {
            this.nsSupport.reset();
            if (this.uris) {
                this.nsSupport.setNamespaceDeclUris(true);
            }
            if (this.entityResolver != null) {
                this.parser.setEntityResolver(this.entityResolver);
            }
            if (this.dtdHandler != null) {
                this.parser.setDTDHandler(this.dtdHandler);
            }
            if (this.errorHandler != null) {
                this.parser.setErrorHandler(this.errorHandler);
            }
            this.parser.setDocumentHandler(this);
            this.locator = null;
            return;
        }
        throw new IllegalStateException();
    }

    private String[] processName(String qName, boolean isAttribute, boolean useException) throws SAXException {
        String[] parts = this.nsSupport.processName(qName, this.nameParts, isAttribute);
        if (parts != null) {
            return parts;
        }
        if (useException) {
            throw makeException("Undeclared prefix: " + qName);
        }
        reportError("Undeclared prefix: " + qName);
        parts = new String[3];
        String str = "";
        parts[1] = str;
        parts[0] = str;
        parts[2] = qName.intern();
        return parts;
    }

    void reportError(String message) throws SAXException {
        if (this.errorHandler != null) {
            this.errorHandler.error(makeException(message));
        }
    }

    private SAXParseException makeException(String message) {
        if (this.locator != null) {
            return new SAXParseException(message, this.locator);
        }
        return new SAXParseException(message, null, null, -1, -1);
    }

    private void checkNotParsing(String type, String name) throws SAXNotSupportedException {
        if (this.parsing) {
            throw new SAXNotSupportedException("Cannot change " + type + ' ' + name + " while parsing");
        }
    }
}
