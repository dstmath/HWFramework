package ohos.com.sun.org.apache.xml.internal.serializer;

import java.util.Vector;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXParseException;
import ohos.org.xml.sax.ext.LexicalHandler;

public abstract class ToSAXHandler extends SerializerBase {
    protected LexicalHandler m_lexHandler;
    protected ContentHandler m_saxHandler;
    private boolean m_shouldGenerateNSAttribute = true;
    protected TransformStateSetter m_state = null;

    /* access modifiers changed from: protected */
    public void closeCDATA() throws SAXException {
    }

    /* access modifiers changed from: protected */
    public void closeStartTag() throws SAXException {
    }

    public void processingInstruction(String str, String str2) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public void setCdataSectionElements(Vector vector) {
    }

    public void startDTD(String str, String str2, String str3) throws SAXException {
    }

    public ToSAXHandler() {
    }

    public ToSAXHandler(ContentHandler contentHandler, LexicalHandler lexicalHandler, String str) {
        setContentHandler(contentHandler);
        setLexHandler(lexicalHandler);
        setEncoding(str);
    }

    public ToSAXHandler(ContentHandler contentHandler, String str) {
        setContentHandler(contentHandler);
        setEncoding(str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase
    public void startDocumentInternal() throws SAXException {
        if (this.m_needToCallStartDocument) {
            super.startDocumentInternal();
            this.m_saxHandler.startDocument();
            this.m_needToCallStartDocument = false;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void characters(String str) throws SAXException {
        int length = str.length();
        if (length > this.m_charsBuff.length) {
            this.m_charsBuff = new char[((length * 2) + 1)];
        }
        str.getChars(0, length, this.m_charsBuff, 0);
        characters(this.m_charsBuff, 0, length);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedLexicalHandler
    public void comment(String str) throws SAXException {
        flushPending();
        if (this.m_lexHandler != null) {
            int length = str.length();
            if (length > this.m_charsBuff.length) {
                this.m_charsBuff = new char[((length * 2) + 1)];
            }
            str.getChars(0, length, this.m_charsBuff, 0);
            this.m_lexHandler.comment(this.m_charsBuff, 0, length);
            if (this.m_tracer != null) {
                super.fireCommentEvent(this.m_charsBuff, 0, length);
            }
        }
    }

    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        TransformStateSetter transformStateSetter = this.m_state;
        if (transformStateSetter != null) {
            transformStateSetter.resetState(getTransformer());
        }
        if (this.m_tracer != null) {
            super.fireStartElem(str3);
        }
    }

    public void setLexHandler(LexicalHandler lexicalHandler) {
        this.m_lexHandler = lexicalHandler;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public void setContentHandler(ContentHandler contentHandler) {
        this.m_saxHandler = contentHandler;
        if (this.m_lexHandler == null && (contentHandler instanceof LexicalHandler)) {
            this.m_lexHandler = (LexicalHandler) contentHandler;
        }
    }

    public void setShouldOutputNSAttr(boolean z) {
        this.m_shouldGenerateNSAttribute = z;
    }

    /* access modifiers changed from: package-private */
    public boolean getShouldOutputNSAttr() {
        return this.m_shouldGenerateNSAttribute;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public void flushPending() throws SAXException {
        if (this.m_needToCallStartDocument) {
            startDocumentInternal();
            this.m_needToCallStartDocument = false;
        }
        if (this.m_elemContext.m_startTagOpen) {
            closeStartTag();
            this.m_elemContext.m_startTagOpen = false;
        }
        if (this.m_cdataTagOpen) {
            closeCDATA();
            this.m_cdataTagOpen = false;
        }
    }

    public void setTransformState(TransformStateSetter transformStateSetter) {
        this.m_state = transformStateSetter;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void startElement(String str, String str2, String str3) throws SAXException {
        TransformStateSetter transformStateSetter = this.m_state;
        if (transformStateSetter != null) {
            transformStateSetter.resetState(getTransformer());
        }
        if (this.m_tracer != null) {
            super.fireStartElem(str3);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void startElement(String str) throws SAXException {
        TransformStateSetter transformStateSetter = this.m_state;
        if (transformStateSetter != null) {
            transformStateSetter.resetState(getTransformer());
        }
        if (this.m_tracer != null) {
            super.fireStartElem(str);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void characters(Node node) throws SAXException {
        TransformStateSetter transformStateSetter = this.m_state;
        if (transformStateSetter != null) {
            transformStateSetter.setCurrentNode(node);
        }
        String nodeValue = node.getNodeValue();
        if (nodeValue != null) {
            characters(nodeValue);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase
    public void fatalError(SAXParseException sAXParseException) throws SAXException {
        super.fatalError(sAXParseException);
        this.m_needToCallStartDocument = false;
        ErrorHandler errorHandler = this.m_saxHandler;
        if (errorHandler instanceof ErrorHandler) {
            errorHandler.fatalError(sAXParseException);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase
    public void error(SAXParseException sAXParseException) throws SAXException {
        super.error(sAXParseException);
        ErrorHandler errorHandler = this.m_saxHandler;
        if (errorHandler instanceof ErrorHandler) {
            errorHandler.error(sAXParseException);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase
    public void warning(SAXParseException sAXParseException) throws SAXException {
        super.warning(sAXParseException);
        ErrorHandler errorHandler = this.m_saxHandler;
        if (errorHandler instanceof ErrorHandler) {
            errorHandler.warning(sAXParseException);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public boolean reset() {
        if (!super.reset()) {
            return false;
        }
        resetToSAXHandler();
        return true;
    }

    private void resetToSAXHandler() {
        this.m_lexHandler = null;
        this.m_saxHandler = null;
        this.m_state = null;
        this.m_shouldGenerateNSAttribute = false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void addUniqueAttribute(String str, String str2, int i) throws SAXException {
        addAttribute(str, str2);
    }
}
