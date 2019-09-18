package org.xmlpull.v1.sax2;

import android.icu.text.PluralRules;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class Driver implements Locator, XMLReader, Attributes {
    protected static final String APACHE_DYNAMIC_VALIDATION_FEATURE = "http://apache.org/xml/features/validation/dynamic";
    protected static final String APACHE_SCHEMA_VALIDATION_FEATURE = "http://apache.org/xml/features/validation/schema";
    protected static final String DECLARATION_HANDLER_PROPERTY = "http://xml.org/sax/properties/declaration-handler";
    protected static final String LEXICAL_HANDLER_PROPERTY = "http://xml.org/sax/properties/lexical-handler";
    protected static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";
    protected static final String NAMESPACE_PREFIXES_FEATURE = "http://xml.org/sax/features/namespace-prefixes";
    protected static final String VALIDATION_FEATURE = "http://xml.org/sax/features/validation";
    protected ContentHandler contentHandler = new DefaultHandler();
    protected ErrorHandler errorHandler = new DefaultHandler();
    protected XmlPullParser pp;
    protected String systemId;

    public Driver() throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        this.pp = factory.newPullParser();
    }

    public Driver(XmlPullParser pp2) throws XmlPullParserException {
        this.pp = pp2;
    }

    public int getLength() {
        return this.pp.getAttributeCount();
    }

    public String getURI(int index) {
        return this.pp.getAttributeNamespace(index);
    }

    public String getLocalName(int index) {
        return this.pp.getAttributeName(index);
    }

    public String getQName(int index) {
        String prefix = this.pp.getAttributePrefix(index);
        if (prefix == null) {
            return this.pp.getAttributeName(index);
        }
        return prefix + ':' + this.pp.getAttributeName(index);
    }

    public String getType(int index) {
        return this.pp.getAttributeType(index);
    }

    public String getValue(int index) {
        return this.pp.getAttributeValue(index);
    }

    public int getIndex(String uri, String localName) {
        for (int i = 0; i < this.pp.getAttributeCount(); i++) {
            if (this.pp.getAttributeNamespace(i).equals(uri) && this.pp.getAttributeName(i).equals(localName)) {
                return i;
            }
        }
        return -1;
    }

    public int getIndex(String qName) {
        for (int i = 0; i < this.pp.getAttributeCount(); i++) {
            if (this.pp.getAttributeName(i).equals(qName)) {
                return i;
            }
        }
        return -1;
    }

    public String getType(String uri, String localName) {
        for (int i = 0; i < this.pp.getAttributeCount(); i++) {
            if (this.pp.getAttributeNamespace(i).equals(uri) && this.pp.getAttributeName(i).equals(localName)) {
                return this.pp.getAttributeType(i);
            }
        }
        return null;
    }

    public String getType(String qName) {
        for (int i = 0; i < this.pp.getAttributeCount(); i++) {
            if (this.pp.getAttributeName(i).equals(qName)) {
                return this.pp.getAttributeType(i);
            }
        }
        return null;
    }

    public String getValue(String uri, String localName) {
        return this.pp.getAttributeValue(uri, localName);
    }

    public String getValue(String qName) {
        return this.pp.getAttributeValue(null, qName);
    }

    public String getPublicId() {
        return null;
    }

    public String getSystemId() {
        return this.systemId;
    }

    public int getLineNumber() {
        return this.pp.getLineNumber();
    }

    public int getColumnNumber() {
        return this.pp.getColumnNumber();
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (NAMESPACES_FEATURE.equals(name)) {
            return this.pp.getFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES);
        }
        if (NAMESPACE_PREFIXES_FEATURE.equals(name)) {
            return this.pp.getFeature(XmlPullParser.FEATURE_REPORT_NAMESPACE_ATTRIBUTES);
        }
        if (VALIDATION_FEATURE.equals(name)) {
            return this.pp.getFeature(XmlPullParser.FEATURE_VALIDATION);
        }
        return this.pp.getFeature(name);
    }

    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        try {
            if (NAMESPACES_FEATURE.equals(name)) {
                this.pp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, value);
            } else if (NAMESPACE_PREFIXES_FEATURE.equals(name)) {
                if (this.pp.getFeature(XmlPullParser.FEATURE_REPORT_NAMESPACE_ATTRIBUTES) != value) {
                    this.pp.setFeature(XmlPullParser.FEATURE_REPORT_NAMESPACE_ATTRIBUTES, value);
                }
            } else if (VALIDATION_FEATURE.equals(name)) {
                this.pp.setFeature(XmlPullParser.FEATURE_VALIDATION, value);
            } else {
                this.pp.setFeature(name, value);
            }
        } catch (XmlPullParserException e) {
        }
    }

    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (!DECLARATION_HANDLER_PROPERTY.equals(name) && !LEXICAL_HANDLER_PROPERTY.equals(name)) {
            return this.pp.getProperty(name);
        }
        return null;
    }

    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (DECLARATION_HANDLER_PROPERTY.equals(name)) {
            throw new SAXNotSupportedException("not supported setting property " + name);
        } else if (!LEXICAL_HANDLER_PROPERTY.equals(name)) {
            try {
                this.pp.setProperty(name, value);
            } catch (XmlPullParserException ex) {
                throw new SAXNotSupportedException("not supported set property " + name + PluralRules.KEYWORD_RULE_SEPARATOR + ex);
            }
        } else {
            throw new SAXNotSupportedException("not supported setting property " + name);
        }
    }

    public void setEntityResolver(EntityResolver resolver) {
    }

    public EntityResolver getEntityResolver() {
        return null;
    }

    public void setDTDHandler(DTDHandler handler) {
    }

    public DTDHandler getDTDHandler() {
        return null;
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

    public void parse(InputSource source) throws SAXException, IOException {
        this.systemId = source.getSystemId();
        this.contentHandler.setDocumentLocator(this);
        Reader reader = source.getCharacterStream();
        if (reader == null) {
            try {
                InputStream stream = source.getByteStream();
                String encoding = source.getEncoding();
                if (stream == null) {
                    this.systemId = source.getSystemId();
                    if (this.systemId == null) {
                        this.errorHandler.fatalError(new SAXParseException("null source systemId", this));
                        return;
                    }
                    try {
                        stream = new URL(this.systemId).openStream();
                    } catch (MalformedURLException e) {
                        try {
                            stream = new FileInputStream(this.systemId);
                        } catch (FileNotFoundException fnfe) {
                            this.errorHandler.fatalError(new SAXParseException("could not open file with systemId " + this.systemId, this, fnfe));
                            return;
                        }
                    }
                }
                this.pp.setInput(stream, encoding);
            } catch (XmlPullParserException ex) {
                this.errorHandler.fatalError(new SAXParseException("parsing initialization error: " + ex, this, ex));
                return;
            }
        } else {
            this.pp.setInput(reader);
        }
        try {
            this.contentHandler.startDocument();
            this.pp.next();
            if (this.pp.getEventType() != 2) {
                this.errorHandler.fatalError(new SAXParseException("expected start tag not" + this.pp.getPositionDescription(), this));
                return;
            }
            parseSubTree(this.pp);
            this.contentHandler.endDocument();
        } catch (XmlPullParserException ex2) {
            this.errorHandler.fatalError(new SAXParseException("parsing initialization error: " + ex2, this, ex2));
        }
    }

    public void parse(String systemId2) throws SAXException, IOException {
        parse(new InputSource(systemId2));
    }

    public void parseSubTree(XmlPullParser pp2) throws SAXException, IOException {
        String str;
        String str2;
        XmlPullParser xmlPullParser = pp2;
        this.pp = xmlPullParser;
        boolean namespaceAware = xmlPullParser.getFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES);
        try {
            int type = 2;
            if (pp2.getEventType() == 2) {
                int[] holderForStartAndLength = new int[2];
                StringBuilder rawName = new StringBuilder(16);
                int i = 1;
                int level = pp2.getDepth() - 1;
                while (true) {
                    int countPrev = 0;
                    switch (type) {
                        case 1:
                            break;
                        case 2:
                            if (!namespaceAware) {
                                startElement(pp2.getNamespace(), pp2.getName(), pp2.getName());
                                break;
                            } else {
                                int depth = pp2.getDepth() - i;
                                int countPrev2 = level > depth ? xmlPullParser.getNamespaceCount(depth) : 0;
                                int count = xmlPullParser.getNamespaceCount(depth + 1);
                                for (int i2 = countPrev2; i2 < count; i2++) {
                                    this.contentHandler.startPrefixMapping(xmlPullParser.getNamespacePrefix(i2), xmlPullParser.getNamespaceUri(i2));
                                }
                                String name = pp2.getName();
                                String prefix = pp2.getPrefix();
                                if (prefix != null) {
                                    rawName.setLength(0);
                                    rawName.append(prefix);
                                    rawName.append(':');
                                    rawName.append(name);
                                }
                                String namespace = pp2.getNamespace();
                                if (prefix == null) {
                                    str = name;
                                } else {
                                    str = rawName.toString();
                                }
                                startElement(namespace, name, str);
                                break;
                            }
                        case 3:
                            if (!namespaceAware) {
                                this.contentHandler.endElement(pp2.getNamespace(), pp2.getName(), pp2.getName());
                                break;
                            } else {
                                String name2 = pp2.getName();
                                String prefix2 = pp2.getPrefix();
                                if (prefix2 != null) {
                                    rawName.setLength(0);
                                    rawName.append(prefix2);
                                    rawName.append(':');
                                    rawName.append(name2);
                                }
                                ContentHandler contentHandler2 = this.contentHandler;
                                String namespace2 = pp2.getNamespace();
                                if (prefix2 != null) {
                                    str2 = name2;
                                } else {
                                    str2 = rawName.toString();
                                }
                                contentHandler2.endElement(namespace2, name2, str2);
                                if (level > pp2.getDepth()) {
                                    countPrev = xmlPullParser.getNamespaceCount(pp2.getDepth());
                                }
                                for (int i3 = xmlPullParser.getNamespaceCount(pp2.getDepth() - i) - 1; i3 >= countPrev; i3--) {
                                    this.contentHandler.endPrefixMapping(xmlPullParser.getNamespacePrefix(i3));
                                }
                                break;
                            }
                        case 4:
                            this.contentHandler.characters(xmlPullParser.getTextCharacters(holderForStartAndLength), holderForStartAndLength[0], holderForStartAndLength[i]);
                            break;
                    }
                    type = pp2.next();
                    if (pp2.getDepth() > level) {
                        i = 1;
                    }
                }
                return;
            }
            throw new SAXException("start tag must be read before skiping subtree" + pp2.getPositionDescription());
        } catch (XmlPullParserException ex) {
            SAXParseException saxException = new SAXParseException("parsing error: " + ex, this, ex);
            ex.printStackTrace();
            this.errorHandler.fatalError(saxException);
        }
    }

    /* access modifiers changed from: protected */
    public void startElement(String namespace, String localName, String qName) throws SAXException {
        this.contentHandler.startElement(namespace, localName, qName, this);
    }
}
