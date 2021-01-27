package ohos.com.sun.org.apache.xml.internal.serializer;

import java.io.IOException;
import java.io.Writer;
import ohos.com.sun.org.apache.xml.internal.serializer.utils.Utils;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.SAXException;

public final class ToTextStream extends ToStream {
    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void addAttribute(String str, String str2) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void addAttribute(String str, String str2, String str3, String str4, String str5, boolean z) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void addUniqueAttribute(String str, String str2, int i) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void endCDATA() throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void namespaceAfterStartElement(String str, String str2) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void startPrefixMapping(String str, String str2) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public boolean startPrefixMapping(String str, String str2, boolean z) throws SAXException {
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase
    public void startDocumentInternal() throws SAXException {
        super.startDocumentInternal();
        this.m_needToCallStartDocument = false;
    }

    public void endDocument() throws SAXException {
        flushPending();
        flushWriter();
        if (this.m_tracer != null) {
            super.fireEndDoc();
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        if (this.m_tracer != null) {
            super.fireStartElem(str3);
            firePseudoAttributes();
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void endElement(String str, String str2, String str3) throws SAXException {
        if (this.m_tracer != null) {
            super.fireEndElem(str3);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void characters(char[] cArr, int i, int i2) throws SAXException {
        flushPending();
        try {
            if (inTemporaryOutputState()) {
                this.m_writer.write(cArr, i, i2);
            } else {
                writeNormalizedChars(cArr, i, i2, this.m_lineSepUse);
            }
            if (this.m_tracer != null) {
                super.fireCharEvent(cArr, i, i2);
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void charactersRaw(char[] cArr, int i, int i2) throws SAXException {
        try {
            writeNormalizedChars(cArr, i, i2, this.m_lineSepUse);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /* access modifiers changed from: package-private */
    public void writeNormalizedChars(char[] cArr, int i, int i2, boolean z) throws IOException, SAXException {
        String encoding = getEncoding();
        Writer writer = this.m_writer;
        int i3 = i2 + i;
        while (i < i3) {
            char c = cArr[i];
            if ('\n' == c && z) {
                writer.write(this.m_lineSep, 0, this.m_lineSepLen);
            } else if (this.m_encodingInfo.isInEncoding(c)) {
                writer.write(c);
            } else if (Encodings.isHighUTF16Surrogate(c) || Encodings.isLowUTF16Surrogate(c)) {
                int writeUTF16Surrogate = writeUTF16Surrogate(c, cArr, i, i3);
                if (writeUTF16Surrogate >= 0) {
                    if (Encodings.isHighUTF16Surrogate(c)) {
                        i++;
                    }
                    if (writeUTF16Surrogate > 0) {
                        System.err.println(Utils.messages.createMessage("ER_ILLEGAL_CHARACTER", new Object[]{Integer.toString(writeUTF16Surrogate), encoding}));
                    }
                }
            } else if (encoding != null) {
                writer.write(38);
                writer.write(35);
                writer.write(Integer.toString(c));
                writer.write(59);
                System.err.println(Utils.messages.createMessage("ER_ILLEGAL_CHARACTER", new Object[]{Integer.toString(c), encoding}));
            } else {
                writer.write(c);
            }
            i++;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void cdata(char[] cArr, int i, int i2) throws SAXException {
        try {
            writeNormalizedChars(cArr, i, i2, this.m_lineSepUse);
            if (this.m_tracer != null) {
                super.fireCDATAEvent(cArr, i, i2);
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
        try {
            writeNormalizedChars(cArr, i, i2, this.m_lineSepUse);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void processingInstruction(String str, String str2) throws SAXException {
        flushPending();
        if (this.m_tracer != null) {
            super.fireEscapingEvent(str, str2);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedLexicalHandler
    public void comment(String str) throws SAXException {
        int length = str.length();
        if (length > this.m_charsBuff.length) {
            this.m_charsBuff = new char[((length * 2) + 1)];
        }
        str.getChars(0, length, this.m_charsBuff, 0);
        comment(this.m_charsBuff, 0, length);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void comment(char[] cArr, int i, int i2) throws SAXException {
        flushPending();
        if (this.m_tracer != null) {
            super.fireCommentEvent(cArr, i, i2);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void entityReference(String str) throws SAXException {
        if (this.m_tracer != null) {
            super.fireEntityReference(str);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void endElement(String str) throws SAXException {
        if (this.m_tracer != null) {
            super.fireEndElem(str);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void startElement(String str, String str2, String str3) throws SAXException {
        if (this.m_needToCallStartDocument) {
            startDocumentInternal();
        }
        if (this.m_tracer != null) {
            super.fireStartElem(str3);
            firePseudoAttributes();
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void characters(String str) throws SAXException {
        int length = str.length();
        if (length > this.m_charsBuff.length) {
            this.m_charsBuff = new char[((length * 2) + 1)];
        }
        str.getChars(0, length, this.m_charsBuff, 0);
        characters(this.m_charsBuff, 0, length);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream, ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public void flushPending() throws SAXException {
        if (this.m_needToCallStartDocument) {
            startDocumentInternal();
            this.m_needToCallStartDocument = false;
        }
    }
}
