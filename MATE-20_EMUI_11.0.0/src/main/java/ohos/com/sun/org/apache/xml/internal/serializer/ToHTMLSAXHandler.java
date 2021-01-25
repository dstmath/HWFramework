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

public final class ToHTMLSAXHandler extends ToSAXHandler {
    private boolean m_dtdHandled = false;
    protected boolean m_escapeSetting = true;

    public void attributeDecl(String str, String str2, String str3, String str4, String str5) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public void close() {
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

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler, ohos.com.sun.org.apache.xml.internal.serializer.DOMSerializer
    public void serialize(Node node) throws IOException {
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

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public boolean setEscaping(boolean z) throws SAXException {
        boolean z2 = this.m_escapeSetting;
        this.m_escapeSetting = z;
        if (z) {
            processingInstruction("javax.xml.transform.enable-output-escaping", "");
        } else {
            processingInstruction("javax.xml.transform.disable-output-escaping", "");
        }
        return z2;
    }

    public void endElement(String str, String str2, String str3) throws SAXException {
        flushPending();
        this.m_saxHandler.endElement(str, str2, str3);
        if (this.m_tracer != null) {
            super.fireEndElem(str3);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler
    public void processingInstruction(String str, String str2) throws SAXException {
        flushPending();
        this.m_saxHandler.processingInstruction(str, str2);
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
        this.m_saxHandler.startElement(str, str2, str3, attributes);
        this.m_elemContext.m_startTagOpen = false;
    }

    public void comment(char[] cArr, int i, int i2) throws SAXException {
        flushPending();
        if (this.m_lexHandler != null) {
            this.m_lexHandler.comment(cArr, i, i2);
        }
        if (this.m_tracer != null) {
            super.fireCommentEvent(cArr, i, i2);
        }
    }

    public void endDocument() throws SAXException {
        flushPending();
        this.m_saxHandler.endDocument();
        if (this.m_tracer != null) {
            super.fireEndDoc();
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler
    public void closeStartTag() throws SAXException {
        this.m_elemContext.m_startTagOpen = false;
        this.m_saxHandler.startElement("", this.m_elemContext.m_elementName, this.m_elemContext.m_elementName, this.m_attributes);
        this.m_attributes.clear();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void characters(String str) throws SAXException {
        int length = str.length();
        if (length > this.m_charsBuff.length) {
            this.m_charsBuff = new char[((length * 2) + 1)];
        }
        str.getChars(0, length, this.m_charsBuff, 0);
        characters(this.m_charsBuff, 0, length);
    }

    public ToHTMLSAXHandler(ContentHandler contentHandler, String str) {
        super(contentHandler, str);
    }

    public ToHTMLSAXHandler(ContentHandler contentHandler, LexicalHandler lexicalHandler, String str) {
        super(contentHandler, lexicalHandler, str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void startElement(String str, String str2, String str3) throws SAXException {
        super.startElement(str, str2, str3);
        flushPending();
        if (!this.m_dtdHandled) {
            String doctypeSystem = getDoctypeSystem();
            String doctypePublic = getDoctypePublic();
            if (!((doctypeSystem == null && doctypePublic == null) || this.m_lexHandler == null)) {
                this.m_lexHandler.startDTD(str3, doctypePublic, doctypeSystem);
            }
            this.m_dtdHandled = true;
        }
        this.m_elemContext = this.m_elemContext.push(str, str2, str3);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void startElement(String str) throws SAXException {
        startElement(null, null, str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void endElement(String str) throws SAXException {
        flushPending();
        this.m_saxHandler.endElement("", str, str);
        if (this.m_tracer != null) {
            super.fireEndElem(str);
        }
    }

    public void characters(char[] cArr, int i, int i2) throws SAXException {
        flushPending();
        this.m_saxHandler.characters(cArr, i, i2);
        if (this.m_tracer != null) {
            super.fireCharEvent(cArr, i, i2);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler, ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public void flushPending() throws SAXException {
        if (this.m_needToCallStartDocument) {
            startDocumentInternal();
            this.m_needToCallStartDocument = false;
        }
        if (this.m_elemContext.m_startTagOpen) {
            closeStartTag();
            this.m_elemContext.m_startTagOpen = false;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public boolean startPrefixMapping(String str, String str2, boolean z) throws SAXException {
        if (z) {
            flushPending();
        }
        this.m_saxHandler.startPrefixMapping(str, str2);
        return false;
    }

    public void startPrefixMapping(String str, String str2) throws SAXException {
        startPrefixMapping(str, str2, true);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void namespaceAfterStartElement(String str, String str2) throws SAXException {
        if (this.m_elemContext.m_elementURI == null && getPrefixPart(this.m_elemContext.m_elementName) == null && "".equals(str)) {
            this.m_elemContext.m_elementURI = str2;
        }
        startPrefixMapping(str, str2, false);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler, ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public boolean reset() {
        if (!super.reset()) {
            return false;
        }
        resetToHTMLSAXHandler();
        return true;
    }

    private void resetToHTMLSAXHandler() {
        this.m_escapeSetting = true;
    }
}
