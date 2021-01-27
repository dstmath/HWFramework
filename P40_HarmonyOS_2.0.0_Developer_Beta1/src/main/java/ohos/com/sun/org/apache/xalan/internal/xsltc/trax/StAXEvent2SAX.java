package ohos.com.sun.org.apache.xalan.internal.xsltc.trax;

import java.io.IOException;
import java.util.Iterator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SAXImpl;
import ohos.javax.xml.namespace.QName;
import ohos.javax.xml.stream.XMLEventReader;
import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.events.Attribute;
import ohos.javax.xml.stream.events.Characters;
import ohos.javax.xml.stream.events.EndElement;
import ohos.javax.xml.stream.events.Namespace;
import ohos.javax.xml.stream.events.ProcessingInstruction;
import ohos.javax.xml.stream.events.StartDocument;
import ohos.javax.xml.stream.events.StartElement;
import ohos.javax.xml.stream.events.XMLEvent;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;
import ohos.org.xml.sax.XMLReader;
import ohos.org.xml.sax.ext.LexicalHandler;
import ohos.org.xml.sax.ext.Locator2;
import ohos.org.xml.sax.helpers.AttributesImpl;

public class StAXEvent2SAX implements XMLReader, Locator {
    private LexicalHandler _lex = null;
    private ContentHandler _sax = null;
    private SAXImpl _saxImpl = null;
    private String encoding = null;
    private final XMLEventReader staxEventReader;
    private String version = null;

    private void handleAttribute() {
    }

    private void handleCDATA() {
    }

    private void handleComment() {
    }

    private void handleDTD() {
    }

    private void handleEntityDecl() {
    }

    private void handleEntityReference() {
    }

    private void handleNamespace() {
    }

    private void handleNotationDecl() {
    }

    private void handleSpace() {
    }

    public int getColumnNumber() {
        return 0;
    }

    public DTDHandler getDTDHandler() {
        return null;
    }

    public EntityResolver getEntityResolver() {
        return null;
    }

    public ErrorHandler getErrorHandler() {
        return null;
    }

    public boolean getFeature(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }

    public int getLineNumber() {
        return 0;
    }

    public Object getProperty(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    public String getPublicId() {
        return null;
    }

    public String getSystemId() {
        return null;
    }

    public void setDTDHandler(DTDHandler dTDHandler) throws NullPointerException {
    }

    public void setEntityResolver(EntityResolver entityResolver) throws NullPointerException {
    }

    public void setErrorHandler(ErrorHandler errorHandler) throws NullPointerException {
    }

    public void setFeature(String str, boolean z) throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public void setProperty(String str, Object obj) throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public StAXEvent2SAX(XMLEventReader xMLEventReader) {
        this.staxEventReader = xMLEventReader;
    }

    public ContentHandler getContentHandler() {
        return this._sax;
    }

    public void setContentHandler(ContentHandler contentHandler) throws NullPointerException {
        this._sax = contentHandler;
        if (contentHandler instanceof LexicalHandler) {
            this._lex = (LexicalHandler) contentHandler;
        }
        if (contentHandler instanceof SAXImpl) {
            this._saxImpl = (SAXImpl) contentHandler;
        }
    }

    public void parse(InputSource inputSource) throws IOException, SAXException {
        try {
            bridge();
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    public void parse() throws IOException, SAXException, XMLStreamException {
        bridge();
    }

    private void bridge() throws XMLStreamException {
        boolean z;
        try {
            StartDocument peek = this.staxEventReader.peek();
            if (!peek.isStartDocument()) {
                if (!peek.isStartElement()) {
                    throw new IllegalStateException();
                }
            }
            int i = 0;
            if (peek.getEventType() == 7) {
                this.version = peek.getVersion();
                if (peek.encodingSet()) {
                    this.encoding = peek.getCharacterEncodingScheme();
                }
                this.staxEventReader.nextEvent();
                peek = this.staxEventReader.nextEvent();
                z = true;
            } else {
                z = false;
            }
            handleStartDocument(peek);
            while (peek.getEventType() != 1) {
                int eventType = peek.getEventType();
                if (eventType == 3) {
                    handlePI((ProcessingInstruction) peek);
                } else if (eventType == 4) {
                    handleCharacters(peek.asCharacters());
                } else if (eventType == 5) {
                    handleComment();
                } else if (eventType == 6) {
                    handleSpace();
                } else if (eventType == 11) {
                    handleDTD();
                } else {
                    throw new InternalError("processing prolog event: " + peek);
                }
                peek = this.staxEventReader.nextEvent();
            }
            do {
                switch (peek.getEventType()) {
                    case 1:
                        i++;
                        handleStartElement(peek.asStartElement());
                        break;
                    case 2:
                        handleEndElement(peek.asEndElement());
                        i--;
                        break;
                    case 3:
                        handlePI((ProcessingInstruction) peek);
                        break;
                    case 4:
                        handleCharacters(peek.asCharacters());
                        break;
                    case 5:
                        handleComment();
                        break;
                    case 6:
                        handleSpace();
                        break;
                    case 7:
                    case 8:
                    default:
                        throw new InternalError("processing event: " + peek);
                    case 9:
                        handleEntityReference();
                        break;
                    case 10:
                        handleAttribute();
                        break;
                    case 11:
                        handleDTD();
                        break;
                    case 12:
                        handleCDATA();
                        break;
                    case 13:
                        handleNamespace();
                        break;
                    case 14:
                        handleNotationDecl();
                        break;
                    case 15:
                        handleEntityDecl();
                        break;
                }
                peek = this.staxEventReader.nextEvent();
            } while (i != 0);
            if (z) {
                while (peek.getEventType() != 8) {
                    int eventType2 = peek.getEventType();
                    if (eventType2 == 3) {
                        handlePI((ProcessingInstruction) peek);
                    } else if (eventType2 == 4) {
                        handleCharacters(peek.asCharacters());
                    } else if (eventType2 == 5) {
                        handleComment();
                    } else if (eventType2 == 6) {
                        handleSpace();
                    } else {
                        throw new InternalError("processing misc event after document element: " + peek);
                    }
                    peek = this.staxEventReader.nextEvent();
                }
            }
            handleEndDocument();
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    private void handleEndDocument() throws SAXException {
        this._sax.endDocument();
    }

    private void handleStartDocument(final XMLEvent xMLEvent) throws SAXException {
        this._sax.setDocumentLocator(new Locator2() {
            /* class ohos.com.sun.org.apache.xalan.internal.xsltc.trax.StAXEvent2SAX.AnonymousClass1 */

            public int getColumnNumber() {
                return xMLEvent.getLocation().getColumnNumber();
            }

            public int getLineNumber() {
                return xMLEvent.getLocation().getLineNumber();
            }

            public String getPublicId() {
                return xMLEvent.getLocation().getPublicId();
            }

            public String getSystemId() {
                return xMLEvent.getLocation().getSystemId();
            }

            public String getXMLVersion() {
                return StAXEvent2SAX.this.version;
            }

            public String getEncoding() {
                return StAXEvent2SAX.this.encoding;
            }
        });
        this._sax.startDocument();
    }

    private void handlePI(ProcessingInstruction processingInstruction) throws XMLStreamException {
        try {
            this._sax.processingInstruction(processingInstruction.getTarget(), processingInstruction.getData());
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    private void handleCharacters(Characters characters) throws XMLStreamException {
        try {
            this._sax.characters(characters.getData().toCharArray(), 0, characters.getData().length());
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    private void handleEndElement(EndElement endElement) throws XMLStreamException {
        String str;
        QName name = endElement.getName();
        if (name.getPrefix() == null || name.getPrefix().trim().length() == 0) {
            str = "";
        } else {
            str = name.getPrefix() + ":";
        }
        try {
            this._sax.endElement(name.getNamespaceURI(), name.getLocalPart(), str + name.getLocalPart());
            Iterator namespaces = endElement.getNamespaces();
            while (namespaces.hasNext()) {
                String str2 = (String) namespaces.next();
                if (str2 == null) {
                    str2 = "";
                }
                this._sax.endPrefixMapping(str2);
            }
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    private void handleStartElement(StartElement startElement) throws XMLStreamException {
        String str;
        try {
            Iterator namespaces = startElement.getNamespaces();
            while (namespaces.hasNext()) {
                String prefix = ((Namespace) namespaces.next()).getPrefix();
                if (prefix == null) {
                    prefix = "";
                }
                this._sax.startPrefixMapping(prefix, startElement.getNamespaceURI(prefix));
            }
            QName name = startElement.getName();
            String prefix2 = name.getPrefix();
            if (prefix2 != null) {
                if (prefix2.length() != 0) {
                    str = prefix2 + ':' + name.getLocalPart();
                    this._sax.startElement(name.getNamespaceURI(), name.getLocalPart(), str, getAttributes(startElement));
                }
            }
            str = name.getLocalPart();
            this._sax.startElement(name.getNamespaceURI(), name.getLocalPart(), str, getAttributes(startElement));
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    private Attributes getAttributes(StartElement startElement) {
        String str;
        AttributesImpl attributesImpl = new AttributesImpl();
        if (startElement.isStartElement()) {
            Iterator attributes = startElement.getAttributes();
            while (attributes.hasNext()) {
                Attribute attribute = (Attribute) attributes.next();
                String namespaceURI = attribute.getName().getNamespaceURI();
                if (namespaceURI == null) {
                    namespaceURI = "";
                }
                String localPart = attribute.getName().getLocalPart();
                String prefix = attribute.getName().getPrefix();
                if (prefix == null || prefix.length() == 0) {
                    str = localPart;
                } else {
                    str = prefix + ':' + localPart;
                }
                attributesImpl.addAttribute(namespaceURI, localPart, str, attribute.getDTDType(), attribute.getValue());
            }
            return attributesImpl;
        }
        throw new InternalError("getAttributes() attempting to process: " + startElement);
    }

    public void parse(String str) throws IOException, SAXException {
        throw new IOException("This method is not yet implemented.");
    }
}
