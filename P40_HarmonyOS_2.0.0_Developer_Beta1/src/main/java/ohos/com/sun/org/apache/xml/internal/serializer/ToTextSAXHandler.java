package ohos.com.sun.org.apache.xml.internal.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.LexicalHandler;

public final class ToTextSAXHandler extends ToSAXHandler {
    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void addAttribute(String str, String str2) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void addAttribute(String str, String str2, String str3, String str4, String str5, boolean z) {
    }

    public void attributeDecl(String str, String str2, String str3, String str4, String str5) throws SAXException {
    }

    public void elementDecl(String str, String str2) throws SAXException {
    }

    public void endCDATA() throws SAXException {
    }

    public void endDTD() throws SAXException {
    }

    public void endPrefixMapping(String str) throws SAXException {
    }

    public void externalEntityDecl(String str, String str2, String str3) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public Properties getOutputFormat() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public OutputStream getOutputStream() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public Writer getWriter() {
        return null;
    }

    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
    }

    public void indent(int i) throws SAXException {
    }

    public void internalEntityDecl(String str, String str2) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void namespaceAfterStartElement(String str, String str2) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler, ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public boolean reset() {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler, ohos.com.sun.org.apache.xml.internal.serializer.DOMSerializer
    public void serialize(Node node) throws IOException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public boolean setEscaping(boolean z) {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public void setIndent(boolean z) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public void setOutputFormat(Properties properties) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public void setOutputStream(OutputStream outputStream) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public void setWriter(Writer writer) {
    }

    public void skippedEntity(String str) throws SAXException {
    }

    public void startCDATA() throws SAXException {
    }

    public void startEntity(String str) throws SAXException {
    }

    public void startPrefixMapping(String str, String str2) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public boolean startPrefixMapping(String str, String str2, boolean z) throws SAXException {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void endElement(String str) throws SAXException {
        if (this.m_tracer != null) {
            super.fireEndElem(str);
        }
    }

    public void endElement(String str, String str2, String str3) throws SAXException {
        if (this.m_tracer != null) {
            super.fireEndElem(str3);
        }
    }

    public ToTextSAXHandler(ContentHandler contentHandler, LexicalHandler lexicalHandler, String str) {
        super(contentHandler, lexicalHandler, str);
    }

    public ToTextSAXHandler(ContentHandler contentHandler, String str) {
        super(contentHandler, str);
    }

    public void comment(char[] cArr, int i, int i2) throws SAXException {
        if (this.m_tracer != null) {
            super.fireCommentEvent(cArr, i, i2);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler, ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedLexicalHandler
    public void comment(String str) throws SAXException {
        int length = str.length();
        if (length > this.m_charsBuff.length) {
            this.m_charsBuff = new char[((length * 2) + 1)];
        }
        str.getChars(0, length, this.m_charsBuff, 0);
        comment(this.m_charsBuff, 0, length);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler
    public void processingInstruction(String str, String str2) throws SAXException {
        if (this.m_tracer != null) {
            super.fireEscapingEvent(str, str2);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase
    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler
    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        flushPending();
        super.startElement(str, str2, str3, attributes);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void startElement(String str, String str2, String str3) throws SAXException {
        super.startElement(str, str2, str3);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void startElement(String str) throws SAXException {
        super.startElement(str);
    }

    public void endDocument() throws SAXException {
        flushPending();
        this.m_saxHandler.endDocument();
        if (this.m_tracer != null) {
            super.fireEndDoc();
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void characters(String str) throws SAXException {
        int length = str.length();
        if (length > this.m_charsBuff.length) {
            this.m_charsBuff = new char[((length * 2) + 1)];
        }
        str.getChars(0, length, this.m_charsBuff, 0);
        this.m_saxHandler.characters(this.m_charsBuff, 0, length);
    }

    public void characters(char[] cArr, int i, int i2) throws SAXException {
        this.m_saxHandler.characters(cArr, i, i2);
        if (this.m_tracer != null) {
            super.fireCharEvent(cArr, i, i2);
        }
    }
}
