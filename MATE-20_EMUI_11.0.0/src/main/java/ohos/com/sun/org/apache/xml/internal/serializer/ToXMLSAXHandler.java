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

public final class ToXMLSAXHandler extends ToSAXHandler {
    protected boolean m_escapeSetting = true;

    public void attributeDecl(String str, String str2, String str3, String str4, String str5) throws SAXException {
    }

    public void elementDecl(String str, String str2) throws SAXException {
    }

    public void endCDATA() throws SAXException {
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

    public void indent(int i) throws SAXException {
    }

    public void internalEntityDecl(String str, String str2) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler, ohos.com.sun.org.apache.xml.internal.serializer.DOMSerializer
    public void serialize(Node node) throws IOException {
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

    public ToXMLSAXHandler() {
        this.m_prefixMap = new NamespaceMappings();
        initCDATA();
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
        String localName = getLocalName(this.m_elemContext.m_elementName);
        String namespaceURI = getNamespaceURI(this.m_elemContext.m_elementName, true);
        if (this.m_needToCallStartDocument) {
            startDocumentInternal();
        }
        this.m_saxHandler.startElement(namespaceURI, localName, this.m_elemContext.m_elementName, this.m_attributes);
        this.m_attributes.clear();
        if (this.m_state != null) {
            this.m_state.setCurrentNode(null);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler
    public void closeCDATA() throws SAXException {
        if (this.m_lexHandler != null && this.m_cdataTagOpen) {
            this.m_lexHandler.endCDATA();
        }
        this.m_cdataTagOpen = false;
    }

    public void endElement(String str, String str2, String str3) throws SAXException {
        flushPending();
        if (str == null) {
            if (this.m_elemContext.m_elementURI != null) {
                str = this.m_elemContext.m_elementURI;
            } else {
                str = getNamespaceURI(str3, true);
            }
        }
        if (str2 == null) {
            if (this.m_elemContext.m_elementLocalName != null) {
                str2 = this.m_elemContext.m_elementLocalName;
            } else {
                str2 = getLocalName(str3);
            }
        }
        this.m_saxHandler.endElement(str, str2, str3);
        if (this.m_tracer != null) {
            super.fireEndElem(str3);
        }
        this.m_prefixMap.popNamespaces(this.m_elemContext.m_currentElemDepth, this.m_saxHandler);
        this.m_elemContext = this.m_elemContext.m_prev;
    }

    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
        this.m_saxHandler.ignorableWhitespace(cArr, i, i2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase
    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        this.m_saxHandler.setDocumentLocator(locator);
    }

    public void skippedEntity(String str) throws SAXException {
        this.m_saxHandler.skippedEntity(str);
    }

    public void startPrefixMapping(String str, String str2) throws SAXException {
        startPrefixMapping(str, str2, true);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public boolean startPrefixMapping(String str, String str2, boolean z) throws SAXException {
        int i;
        if (z) {
            flushPending();
            i = this.m_elemContext.m_currentElemDepth + 1;
        } else {
            i = this.m_elemContext.m_currentElemDepth;
        }
        boolean pushNamespace = this.m_prefixMap.pushNamespace(str, str2, i);
        if (pushNamespace) {
            this.m_saxHandler.startPrefixMapping(str, str2);
            if (getShouldOutputNSAttr()) {
                if ("".equals(str)) {
                    addAttributeAlways("http://www.w3.org/2000/xmlns/", "xmlns", "xmlns", "CDATA", str2, false);
                } else if (!"".equals(str2)) {
                    addAttributeAlways("http://www.w3.org/2000/xmlns/", str, "xmlns:" + str, "CDATA", str2, false);
                }
            }
        }
        return pushNamespace;
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

    public void endDTD() throws SAXException {
        if (this.m_lexHandler != null) {
            this.m_lexHandler.endDTD();
        }
    }

    public void startEntity(String str) throws SAXException {
        if (this.m_lexHandler != null) {
            this.m_lexHandler.startEntity(str);
        }
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

    public ToXMLSAXHandler(ContentHandler contentHandler, String str) {
        super(contentHandler, str);
        initCDATA();
        this.m_prefixMap = new NamespaceMappings();
    }

    public ToXMLSAXHandler(ContentHandler contentHandler, LexicalHandler lexicalHandler, String str) {
        super(contentHandler, lexicalHandler, str);
        initCDATA();
        this.m_prefixMap = new NamespaceMappings();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void startElement(String str, String str2, String str3) throws SAXException {
        startElement(str, str2, str3, null);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void startElement(String str) throws SAXException {
        startElement(null, null, str, null);
    }

    public void characters(char[] cArr, int i, int i2) throws SAXException {
        if (this.m_needToCallStartDocument) {
            startDocumentInternal();
            this.m_needToCallStartDocument = false;
        }
        if (this.m_elemContext.m_startTagOpen) {
            closeStartTag();
            this.m_elemContext.m_startTagOpen = false;
        }
        if (this.m_elemContext.m_isCdataSection && !this.m_cdataTagOpen && this.m_lexHandler != null) {
            this.m_lexHandler.startCDATA();
            this.m_cdataTagOpen = true;
        }
        this.m_saxHandler.characters(cArr, i, i2);
        if (this.m_tracer != null) {
            fireCharEvent(cArr, i, i2);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void endElement(String str) throws SAXException {
        endElement(null, null, str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void namespaceAfterStartElement(String str, String str2) throws SAXException {
        startPrefixMapping(str, str2, false);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler
    public void processingInstruction(String str, String str2) throws SAXException {
        flushPending();
        this.m_saxHandler.processingInstruction(str, str2);
        if (this.m_tracer != null) {
            super.fireEscapingEvent(str, str2);
        }
    }

    /* access modifiers changed from: protected */
    public boolean popNamespace(String str) {
        try {
            if (!this.m_prefixMap.popNamespace(str)) {
                return false;
            }
            this.m_saxHandler.endPrefixMapping(str);
            return true;
        } catch (SAXException unused) {
            return false;
        }
    }

    public void startCDATA() throws SAXException {
        if (!this.m_cdataTagOpen) {
            flushPending();
            if (this.m_lexHandler != null) {
                this.m_lexHandler.startCDATA();
                this.m_cdataTagOpen = true;
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler
    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        flushPending();
        super.startElement(str, str2, str3, attributes);
        if (this.m_needToOutputDocTypeDecl) {
            String doctypeSystem = getDoctypeSystem();
            if (!(doctypeSystem == null || this.m_lexHandler == null)) {
                this.m_lexHandler.startDTD(str3, getDoctypePublic(), doctypeSystem);
            }
            this.m_needToOutputDocTypeDecl = false;
        }
        this.m_elemContext = this.m_elemContext.push(str, str2, str3);
        if (str != null) {
            ensurePrefixIsDeclared(str, str3);
        }
        if (attributes != null) {
            addAttributes(attributes);
        }
        this.m_elemContext.m_isCdataSection = isCdataSection();
    }

    private void ensurePrefixIsDeclared(String str, String str2) throws SAXException {
        String str3;
        if (str != null && str.length() > 0) {
            int indexOf = str2.indexOf(":");
            boolean z = indexOf < 0;
            if (z) {
                str3 = "";
            } else {
                str3 = str2.substring(0, indexOf);
            }
            if (str3 != null) {
                String lookupNamespace = this.m_prefixMap.lookupNamespace(str3);
                if (lookupNamespace == null || !lookupNamespace.equals(str)) {
                    startPrefixMapping(str3, str, false);
                    if (getShouldOutputNSAttr()) {
                        String str4 = "xmlns";
                        String str5 = z ? str4 : str3;
                        if (!z) {
                            str4 = "xmlns:" + str3;
                        }
                        addAttributeAlways("http://www.w3.org/2000/xmlns/", str5, str4, "CDATA", str, false);
                    }
                }
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void addAttribute(String str, String str2, String str3, String str4, String str5, boolean z) throws SAXException {
        if (this.m_elemContext.m_startTagOpen) {
            ensurePrefixIsDeclared(str, str3);
            addAttributeAlways(str, str2, str3, str4, str5, false);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToSAXHandler, ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public boolean reset() {
        if (!super.reset()) {
            return false;
        }
        resetToXMLSAXHandler();
        return true;
    }

    private void resetToXMLSAXHandler() {
        this.m_escapeSetting = true;
    }
}
