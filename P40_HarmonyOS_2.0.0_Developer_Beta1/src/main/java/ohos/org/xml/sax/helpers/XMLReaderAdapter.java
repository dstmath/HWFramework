package ohos.org.xml.sax.helpers;

import java.io.IOException;
import java.util.Locale;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.xml.sax.AttributeList;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.DocumentHandler;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.Parser;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotSupportedException;
import ohos.org.xml.sax.XMLReader;

public class XMLReaderAdapter implements Parser, ContentHandler {
    DocumentHandler documentHandler;
    AttributesAdapter qAtts;
    XMLReader xmlReader;

    @Override // ohos.org.xml.sax.ContentHandler
    public void endPrefixMapping(String str) {
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void skippedEntity(String str) throws SAXException {
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void startPrefixMapping(String str, String str2) {
    }

    public XMLReaderAdapter() throws SAXException {
        setup(XMLReaderFactory.createXMLReader());
    }

    public XMLReaderAdapter(XMLReader xMLReader) {
        setup(xMLReader);
    }

    private void setup(XMLReader xMLReader) {
        if (xMLReader != null) {
            this.xmlReader = xMLReader;
            this.qAtts = new AttributesAdapter();
            return;
        }
        throw new NullPointerException("XMLReader must not be null");
    }

    @Override // ohos.org.xml.sax.Parser
    public void setLocale(Locale locale) throws SAXException {
        throw new SAXNotSupportedException("setLocale not supported");
    }

    @Override // ohos.org.xml.sax.Parser
    public void setEntityResolver(EntityResolver entityResolver) {
        this.xmlReader.setEntityResolver(entityResolver);
    }

    @Override // ohos.org.xml.sax.Parser
    public void setDTDHandler(DTDHandler dTDHandler) {
        this.xmlReader.setDTDHandler(dTDHandler);
    }

    @Override // ohos.org.xml.sax.Parser
    public void setDocumentHandler(DocumentHandler documentHandler2) {
        this.documentHandler = documentHandler2;
    }

    @Override // ohos.org.xml.sax.Parser
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.xmlReader.setErrorHandler(errorHandler);
    }

    @Override // ohos.org.xml.sax.Parser
    public void parse(String str) throws IOException, SAXException {
        parse(new InputSource(str));
    }

    @Override // ohos.org.xml.sax.Parser
    public void parse(InputSource inputSource) throws IOException, SAXException {
        setupXMLReader();
        this.xmlReader.parse(inputSource);
    }

    private void setupXMLReader() throws SAXException {
        this.xmlReader.setFeature(JdkXmlUtils.NAMESPACE_PREFIXES_FEATURE, true);
        try {
            this.xmlReader.setFeature(JdkXmlUtils.NAMESPACES_FEATURE, false);
        } catch (SAXException unused) {
        }
        this.xmlReader.setContentHandler(this);
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void setDocumentLocator(Locator locator) {
        DocumentHandler documentHandler2 = this.documentHandler;
        if (documentHandler2 != null) {
            documentHandler2.setDocumentLocator(locator);
        }
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void startDocument() throws SAXException {
        DocumentHandler documentHandler2 = this.documentHandler;
        if (documentHandler2 != null) {
            documentHandler2.startDocument();
        }
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void endDocument() throws SAXException {
        DocumentHandler documentHandler2 = this.documentHandler;
        if (documentHandler2 != null) {
            documentHandler2.endDocument();
        }
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        if (this.documentHandler != null) {
            this.qAtts.setAttributes(attributes);
            this.documentHandler.startElement(str3, this.qAtts);
        }
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void endElement(String str, String str2, String str3) throws SAXException {
        DocumentHandler documentHandler2 = this.documentHandler;
        if (documentHandler2 != null) {
            documentHandler2.endElement(str3);
        }
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void characters(char[] cArr, int i, int i2) throws SAXException {
        DocumentHandler documentHandler2 = this.documentHandler;
        if (documentHandler2 != null) {
            documentHandler2.characters(cArr, i, i2);
        }
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
        DocumentHandler documentHandler2 = this.documentHandler;
        if (documentHandler2 != null) {
            documentHandler2.ignorableWhitespace(cArr, i, i2);
        }
    }

    @Override // ohos.org.xml.sax.ContentHandler
    public void processingInstruction(String str, String str2) throws SAXException {
        DocumentHandler documentHandler2 = this.documentHandler;
        if (documentHandler2 != null) {
            documentHandler2.processingInstruction(str, str2);
        }
    }

    /* access modifiers changed from: package-private */
    public final class AttributesAdapter implements AttributeList {
        private Attributes attributes;

        AttributesAdapter() {
        }

        /* access modifiers changed from: package-private */
        public void setAttributes(Attributes attributes2) {
            this.attributes = attributes2;
        }

        @Override // ohos.org.xml.sax.AttributeList
        public int getLength() {
            return this.attributes.getLength();
        }

        @Override // ohos.org.xml.sax.AttributeList
        public String getName(int i) {
            return this.attributes.getQName(i);
        }

        @Override // ohos.org.xml.sax.AttributeList
        public String getType(int i) {
            return this.attributes.getType(i);
        }

        @Override // ohos.org.xml.sax.AttributeList
        public String getValue(int i) {
            return this.attributes.getValue(i);
        }

        @Override // ohos.org.xml.sax.AttributeList
        public String getType(String str) {
            return this.attributes.getType(str);
        }

        @Override // ohos.org.xml.sax.AttributeList
        public String getValue(String str) {
            return this.attributes.getValue(str);
        }
    }
}
