package ohos.com.sun.org.apache.xalan.internal.xsltc.trax;

import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.XMLStreamWriter;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.SAXException;

public class SAX2StAXStreamWriter extends SAX2StAXBaseWriter {
    private boolean needToCallStartDocument = false;
    private XMLStreamWriter writer;

    public SAX2StAXStreamWriter() {
    }

    public SAX2StAXStreamWriter(XMLStreamWriter xMLStreamWriter) {
        this.writer = xMLStreamWriter;
    }

    public XMLStreamWriter getStreamWriter() {
        return this.writer;
    }

    public void setStreamWriter(XMLStreamWriter xMLStreamWriter) {
        this.writer = xMLStreamWriter;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter
    public void startDocument() throws SAXException {
        super.startDocument();
        this.needToCallStartDocument = true;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter
    public void endDocument() throws SAXException {
        try {
            this.writer.writeEndDocument();
            super.endDocument();
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter
    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        if (this.needToCallStartDocument) {
            try {
                if (this.docLocator == null) {
                    this.writer.writeStartDocument();
                } else {
                    try {
                        this.writer.writeStartDocument(this.docLocator.getXMLVersion());
                    } catch (ClassCastException unused) {
                        this.writer.writeStartDocument();
                    }
                }
                this.needToCallStartDocument = false;
            } catch (XMLStreamException e) {
                throw new SAXException(e);
            }
        }
        try {
            String[] strArr = {null, null};
            parseQName(str3, strArr);
            this.writer.writeStartElement(str3);
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                parseQName(attributes.getQName(i), strArr);
                String str4 = strArr[0];
                String str5 = strArr[1];
                String qName = attributes.getQName(i);
                String value = attributes.getValue(i);
                String uri = attributes.getURI(i);
                if (!"xmlns".equals(str4)) {
                    if (!"xmlns".equals(qName)) {
                        if (str4.length() > 0) {
                            this.writer.writeAttribute(str4, uri, str5, value);
                        } else {
                            this.writer.writeAttribute(qName, value);
                        }
                    }
                }
                if (str5.length() == 0) {
                    this.writer.setDefaultNamespace(value);
                } else {
                    this.writer.setPrefix(str5, value);
                }
                this.writer.writeNamespace(str5, value);
            }
            super.startElement(str, str2, str3, attributes);
        } catch (XMLStreamException e2) {
            throw new SAXException(e2);
        } catch (Throwable th) {
            super.startElement(str, str2, str3, attributes);
            throw th;
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter
    public void endElement(String str, String str2, String str3) throws SAXException {
        try {
            this.writer.writeEndElement();
            super.endElement(str, str2, str3);
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        } catch (Throwable th) {
            super.endElement(str, str2, str3);
            throw th;
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter
    public void comment(char[] cArr, int i, int i2) throws SAXException {
        super.comment(cArr, i, i2);
        try {
            this.writer.writeComment(new String(cArr, i, i2));
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter
    public void characters(char[] cArr, int i, int i2) throws SAXException {
        super.characters(cArr, i, i2);
        try {
            if (!this.isCDATA) {
                this.writer.writeCharacters(cArr, i, i2);
            }
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter
    public void endCDATA() throws SAXException {
        try {
            this.writer.writeCData(this.CDATABuffer.toString());
            super.endCDATA();
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
        super.ignorableWhitespace(cArr, i, i2);
        try {
            this.writer.writeCharacters(cArr, i, i2);
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    public void processingInstruction(String str, String str2) throws SAXException {
        super.processingInstruction(str, str2);
        try {
            this.writer.writeProcessingInstruction(str, str2);
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }
}
