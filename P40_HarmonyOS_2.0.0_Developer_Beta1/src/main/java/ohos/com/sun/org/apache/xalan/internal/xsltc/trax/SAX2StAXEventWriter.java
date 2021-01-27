package ohos.com.sun.org.apache.xalan.internal.xsltc.trax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import ohos.javax.xml.stream.XMLEventFactory;
import ohos.javax.xml.stream.XMLEventWriter;
import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.events.Attribute;
import ohos.javax.xml.stream.events.Namespace;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.SAXException;

public class SAX2StAXEventWriter extends SAX2StAXBaseWriter {
    private XMLEventFactory eventFactory;
    private List namespaceStack;
    private boolean needToCallStartDocument;
    private XMLEventWriter writer;

    public SAX2StAXEventWriter() {
        this.namespaceStack = new ArrayList();
        this.needToCallStartDocument = false;
        this.eventFactory = XMLEventFactory.newInstance();
    }

    public SAX2StAXEventWriter(XMLEventWriter xMLEventWriter) {
        this.namespaceStack = new ArrayList();
        this.needToCallStartDocument = false;
        this.writer = xMLEventWriter;
        this.eventFactory = XMLEventFactory.newInstance();
    }

    public SAX2StAXEventWriter(XMLEventWriter xMLEventWriter, XMLEventFactory xMLEventFactory) {
        this.namespaceStack = new ArrayList();
        this.needToCallStartDocument = false;
        this.writer = xMLEventWriter;
        if (xMLEventFactory != null) {
            this.eventFactory = xMLEventFactory;
        } else {
            this.eventFactory = XMLEventFactory.newInstance();
        }
    }

    public XMLEventWriter getEventWriter() {
        return this.writer;
    }

    public void setEventWriter(XMLEventWriter xMLEventWriter) {
        this.writer = xMLEventWriter;
    }

    public XMLEventFactory getEventFactory() {
        return this.eventFactory;
    }

    public void setEventFactory(XMLEventFactory xMLEventFactory) {
        this.eventFactory = xMLEventFactory;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter
    public void startDocument() throws SAXException {
        super.startDocument();
        this.namespaceStack.clear();
        this.eventFactory.setLocation(getCurrentLocation());
        this.needToCallStartDocument = true;
    }

    private void writeStartDocument() throws SAXException {
        try {
            if (this.docLocator == null) {
                this.writer.add(this.eventFactory.createStartDocument());
            } else {
                try {
                    this.writer.add(this.eventFactory.createStartDocument(this.docLocator.getEncoding(), this.docLocator.getXMLVersion()));
                } catch (ClassCastException unused) {
                    this.writer.add(this.eventFactory.createStartDocument());
                }
            }
            this.needToCallStartDocument = false;
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter
    public void endDocument() throws SAXException {
        this.eventFactory.setLocation(getCurrentLocation());
        try {
            this.writer.add(this.eventFactory.createEndDocument());
            super.endDocument();
            this.namespaceStack.clear();
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter
    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        if (this.needToCallStartDocument) {
            writeStartDocument();
        }
        this.eventFactory.setLocation(getCurrentLocation());
        Collection[] collectionArr = {null, null};
        createStartEvents(attributes, collectionArr);
        this.namespaceStack.add(collectionArr[0]);
        try {
            String[] strArr = {null, null};
            parseQName(str3, strArr);
            this.writer.add(this.eventFactory.createStartElement(strArr[0], str, strArr[1], collectionArr[1].iterator(), collectionArr[0].iterator()));
            super.startElement(str, str2, str3, attributes);
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        } catch (Throwable th) {
            super.startElement(str, str2, str3, attributes);
            throw th;
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter
    public void endElement(String str, String str2, String str3) throws SAXException {
        super.endElement(str, str2, str3);
        this.eventFactory.setLocation(getCurrentLocation());
        String[] strArr = {null, null};
        parseQName(str3, strArr);
        List list = this.namespaceStack;
        try {
            this.writer.add(this.eventFactory.createEndElement(strArr[0], str, strArr[1], ((Collection) list.remove(list.size() - 1)).iterator()));
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter
    public void comment(char[] cArr, int i, int i2) throws SAXException {
        if (this.needToCallStartDocument) {
            writeStartDocument();
        }
        super.comment(cArr, i, i2);
        this.eventFactory.setLocation(getCurrentLocation());
        try {
            this.writer.add(this.eventFactory.createComment(new String(cArr, i, i2)));
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter
    public void characters(char[] cArr, int i, int i2) throws SAXException {
        super.characters(cArr, i, i2);
        try {
            if (!this.isCDATA) {
                this.eventFactory.setLocation(getCurrentLocation());
                this.writer.add(this.eventFactory.createCharacters(new String(cArr, i, i2)));
            }
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
        super.ignorableWhitespace(cArr, i, i2);
        characters(cArr, i, i2);
    }

    public void processingInstruction(String str, String str2) throws SAXException {
        if (this.needToCallStartDocument) {
            writeStartDocument();
        }
        super.processingInstruction(str, str2);
        try {
            this.writer.add(this.eventFactory.createProcessingInstruction(str, str2));
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter
    public void endCDATA() throws SAXException {
        this.eventFactory.setLocation(getCurrentLocation());
        try {
            this.writer.add(this.eventFactory.createCData(this.CDATABuffer.toString()));
            super.endCDATA();
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    /* access modifiers changed from: protected */
    public void createStartEvents(Attributes attributes, Collection[] collectionArr) {
        HashMap hashMap;
        Attribute attribute;
        if (this.namespaces != null) {
            int size = this.namespaces.size();
            int i = 0;
            hashMap = null;
            while (i < size) {
                int i2 = i + 1;
                String str = (String) this.namespaces.elementAt(i);
                Namespace createNamespace = createNamespace(str, (String) this.namespaces.elementAt(i2));
                if (hashMap == null) {
                    hashMap = new HashMap();
                }
                hashMap.put(str, createNamespace);
                i = i2 + 1;
            }
        } else {
            hashMap = null;
        }
        String[] strArr = {null, null};
        int length = attributes.getLength();
        List list = null;
        for (int i3 = 0; i3 < length; i3++) {
            parseQName(attributes.getQName(i3), strArr);
            String str2 = strArr[0];
            String str3 = strArr[1];
            String qName = attributes.getQName(i3);
            String value = attributes.getValue(i3);
            String uri = attributes.getURI(i3);
            if ("xmlns".equals(qName) || "xmlns".equals(str2)) {
                if (hashMap == null) {
                    hashMap = new HashMap();
                }
                if (!hashMap.containsKey(str3)) {
                    hashMap.put(str3, createNamespace(str3, value));
                }
            } else {
                if (str2.length() > 0) {
                    attribute = this.eventFactory.createAttribute(str2, uri, str3, value);
                } else {
                    attribute = this.eventFactory.createAttribute(str3, value);
                }
                if (list == null) {
                    list = new ArrayList();
                }
                list.add(attribute);
            }
        }
        collectionArr[0] = hashMap == null ? Collections.EMPTY_LIST : hashMap.values();
        if (list == null) {
            list = Collections.EMPTY_LIST;
        }
        collectionArr[1] = list;
    }

    /* access modifiers changed from: protected */
    public Namespace createNamespace(String str, String str2) {
        if (str == null || str.length() == 0) {
            return this.eventFactory.createNamespace(str2);
        }
        return this.eventFactory.createNamespace(str, str2);
    }
}
