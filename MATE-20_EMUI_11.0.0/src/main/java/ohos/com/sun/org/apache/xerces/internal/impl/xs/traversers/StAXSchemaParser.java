package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

import java.util.ArrayList;
import java.util.Iterator;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.SchemaDOMParser;
import ohos.com.sun.org.apache.xerces.internal.util.JAXPNamespaceContextWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.StAXLocationWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLAttributesImpl;
import ohos.com.sun.org.apache.xerces.internal.util.XMLStringBuffer;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.javax.xml.stream.XMLEventReader;
import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.XMLStreamReader;
import ohos.javax.xml.stream.events.Attribute;
import ohos.javax.xml.stream.events.EndElement;
import ohos.javax.xml.stream.events.Namespace;
import ohos.javax.xml.stream.events.ProcessingInstruction;
import ohos.javax.xml.stream.events.StartElement;
import ohos.javax.xml.stream.events.XMLEvent;
import ohos.org.w3c.dom.Document;

/* access modifiers changed from: package-private */
public final class StAXSchemaParser {
    private static final int CHUNK_MASK = 1023;
    private static final int CHUNK_SIZE = 1024;
    private final QName fAttributeQName = new QName();
    private final XMLAttributesImpl fAttributes = new XMLAttributesImpl();
    private final char[] fCharBuffer = new char[1024];
    private final ArrayList fDeclaredPrefixes = new ArrayList();
    private int fDepth;
    private final QName fElementQName = new QName();
    private final StAXLocationWrapper fLocationWrapper = new StAXLocationWrapper();
    private final JAXPNamespaceContextWrapper fNamespaceContext = new JAXPNamespaceContextWrapper(this.fSymbolTable);
    private SchemaDOMParser fSchemaDOMParser;
    private final XMLStringBuffer fStringBuffer = new XMLStringBuffer();
    private SymbolTable fSymbolTable;
    private final XMLString fTempString = new XMLString();

    public StAXSchemaParser() {
        this.fNamespaceContext.setDeclaredPrefixes(this.fDeclaredPrefixes);
    }

    public void reset(SchemaDOMParser schemaDOMParser, SymbolTable symbolTable) {
        this.fSchemaDOMParser = schemaDOMParser;
        this.fSymbolTable = symbolTable;
        this.fNamespaceContext.setSymbolTable(this.fSymbolTable);
        this.fNamespaceContext.reset();
    }

    public Document getDocument() {
        return this.fSchemaDOMParser.getDocument();
    }

    public void parse(XMLEventReader xMLEventReader) throws XMLStreamException, XNIException {
        XMLEvent peek = xMLEventReader.peek();
        if (peek != null) {
            int eventType = peek.getEventType();
            if (eventType == 7 || eventType == 1) {
                this.fLocationWrapper.setLocation(peek.getLocation());
                this.fSchemaDOMParser.startDocument(this.fLocationWrapper, null, this.fNamespaceContext, null);
                while (xMLEventReader.hasNext()) {
                    ProcessingInstruction nextEvent = xMLEventReader.nextEvent();
                    switch (nextEvent.getEventType()) {
                        case 1:
                            this.fDepth++;
                            StartElement asStartElement = nextEvent.asStartElement();
                            fillQName(this.fElementQName, asStartElement.getName());
                            this.fLocationWrapper.setLocation(asStartElement.getLocation());
                            this.fNamespaceContext.setNamespaceContext(asStartElement.getNamespaceContext());
                            fillXMLAttributes(asStartElement);
                            fillDeclaredPrefixes(asStartElement);
                            addNamespaceDeclarations();
                            this.fNamespaceContext.pushContext();
                            this.fSchemaDOMParser.startElement(this.fElementQName, this.fAttributes, null);
                            break;
                        case 2:
                            EndElement asEndElement = nextEvent.asEndElement();
                            fillQName(this.fElementQName, asEndElement.getName());
                            fillDeclaredPrefixes(asEndElement);
                            this.fLocationWrapper.setLocation(asEndElement.getLocation());
                            this.fSchemaDOMParser.endElement(this.fElementQName, null);
                            this.fNamespaceContext.popContext();
                            this.fDepth--;
                            if (this.fDepth > 0) {
                                break;
                            } else {
                                this.fLocationWrapper.setLocation(null);
                                this.fNamespaceContext.setNamespaceContext(null);
                                this.fSchemaDOMParser.endDocument(null);
                                return;
                            }
                        case 3:
                            ProcessingInstruction processingInstruction = nextEvent;
                            fillProcessingInstruction(processingInstruction.getData());
                            this.fSchemaDOMParser.processingInstruction(processingInstruction.getTarget(), this.fTempString, null);
                            break;
                        case 4:
                            sendCharactersToSchemaParser(nextEvent.asCharacters().getData(), false);
                            break;
                        case 6:
                            sendCharactersToSchemaParser(nextEvent.asCharacters().getData(), true);
                            break;
                        case 7:
                            this.fDepth++;
                            break;
                        case 12:
                            this.fSchemaDOMParser.startCDATA(null);
                            sendCharactersToSchemaParser(nextEvent.asCharacters().getData(), false);
                            this.fSchemaDOMParser.endCDATA(null);
                            break;
                    }
                }
                this.fLocationWrapper.setLocation(null);
                this.fNamespaceContext.setNamespaceContext(null);
                this.fSchemaDOMParser.endDocument(null);
                return;
            }
            throw new XMLStreamException();
        }
    }

    public void parse(XMLStreamReader xMLStreamReader) throws XMLStreamException, XNIException {
        if (xMLStreamReader.hasNext()) {
            int eventType = xMLStreamReader.getEventType();
            if (eventType == 7 || eventType == 1) {
                this.fLocationWrapper.setLocation(xMLStreamReader.getLocation());
                this.fSchemaDOMParser.startDocument(this.fLocationWrapper, null, this.fNamespaceContext, null);
                int i = eventType;
                boolean z = true;
                while (xMLStreamReader.hasNext()) {
                    if (!z) {
                        i = xMLStreamReader.next();
                    } else {
                        z = false;
                    }
                    switch (i) {
                        case 1:
                            this.fDepth++;
                            this.fLocationWrapper.setLocation(xMLStreamReader.getLocation());
                            this.fNamespaceContext.setNamespaceContext(xMLStreamReader.getNamespaceContext());
                            fillQName(this.fElementQName, xMLStreamReader.getNamespaceURI(), xMLStreamReader.getLocalName(), xMLStreamReader.getPrefix());
                            fillXMLAttributes(xMLStreamReader);
                            fillDeclaredPrefixes(xMLStreamReader);
                            addNamespaceDeclarations();
                            this.fNamespaceContext.pushContext();
                            this.fSchemaDOMParser.startElement(this.fElementQName, this.fAttributes, null);
                            break;
                        case 2:
                            this.fLocationWrapper.setLocation(xMLStreamReader.getLocation());
                            this.fNamespaceContext.setNamespaceContext(xMLStreamReader.getNamespaceContext());
                            fillQName(this.fElementQName, xMLStreamReader.getNamespaceURI(), xMLStreamReader.getLocalName(), xMLStreamReader.getPrefix());
                            fillDeclaredPrefixes(xMLStreamReader);
                            this.fSchemaDOMParser.endElement(this.fElementQName, null);
                            this.fNamespaceContext.popContext();
                            this.fDepth--;
                            if (this.fDepth > 0) {
                                break;
                            } else {
                                this.fLocationWrapper.setLocation(null);
                                this.fNamespaceContext.setNamespaceContext(null);
                                this.fSchemaDOMParser.endDocument(null);
                                return;
                            }
                        case 3:
                            fillProcessingInstruction(xMLStreamReader.getPIData());
                            this.fSchemaDOMParser.processingInstruction(xMLStreamReader.getPITarget(), this.fTempString, null);
                            break;
                        case 4:
                            this.fTempString.setValues(xMLStreamReader.getTextCharacters(), xMLStreamReader.getTextStart(), xMLStreamReader.getTextLength());
                            this.fSchemaDOMParser.characters(this.fTempString, null);
                            break;
                        case 6:
                            this.fTempString.setValues(xMLStreamReader.getTextCharacters(), xMLStreamReader.getTextStart(), xMLStreamReader.getTextLength());
                            this.fSchemaDOMParser.ignorableWhitespace(this.fTempString, null);
                            break;
                        case 7:
                            this.fDepth++;
                            break;
                        case 12:
                            this.fSchemaDOMParser.startCDATA(null);
                            this.fTempString.setValues(xMLStreamReader.getTextCharacters(), xMLStreamReader.getTextStart(), xMLStreamReader.getTextLength());
                            this.fSchemaDOMParser.characters(this.fTempString, null);
                            this.fSchemaDOMParser.endCDATA(null);
                            break;
                    }
                }
                this.fLocationWrapper.setLocation(null);
                this.fNamespaceContext.setNamespaceContext(null);
                this.fSchemaDOMParser.endDocument(null);
                return;
            }
            throw new XMLStreamException();
        }
    }

    private void sendCharactersToSchemaParser(String str, boolean z) {
        if (str != null) {
            int length = str.length();
            int i = length & 1023;
            if (i > 0) {
                str.getChars(0, i, this.fCharBuffer, 0);
                this.fTempString.setValues(this.fCharBuffer, 0, i);
                if (z) {
                    this.fSchemaDOMParser.ignorableWhitespace(this.fTempString, null);
                } else {
                    this.fSchemaDOMParser.characters(this.fTempString, null);
                }
            }
            while (i < length) {
                int i2 = i + 1024;
                str.getChars(i, i2, this.fCharBuffer, 0);
                this.fTempString.setValues(this.fCharBuffer, 0, 1024);
                if (z) {
                    this.fSchemaDOMParser.ignorableWhitespace(this.fTempString, null);
                } else {
                    this.fSchemaDOMParser.characters(this.fTempString, null);
                }
                i = i2;
            }
        }
    }

    private void fillProcessingInstruction(String str) {
        int length = str.length();
        char[] cArr = this.fCharBuffer;
        if (cArr.length < length) {
            cArr = str.toCharArray();
        } else {
            str.getChars(0, length, cArr, 0);
        }
        this.fTempString.setValues(cArr, 0, length);
    }

    private void fillXMLAttributes(StartElement startElement) {
        this.fAttributes.removeAllAttributes();
        Iterator attributes = startElement.getAttributes();
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();
            fillQName(this.fAttributeQName, attribute.getName());
            String dTDType = attribute.getDTDType();
            int length = this.fAttributes.getLength();
            XMLAttributesImpl xMLAttributesImpl = this.fAttributes;
            QName qName = this.fAttributeQName;
            if (dTDType == null) {
                dTDType = XMLSymbols.fCDATASymbol;
            }
            xMLAttributesImpl.addAttributeNS(qName, dTDType, attribute.getValue());
            this.fAttributes.setSpecified(length, attribute.isSpecified());
        }
    }

    private void fillXMLAttributes(XMLStreamReader xMLStreamReader) {
        this.fAttributes.removeAllAttributes();
        int attributeCount = xMLStreamReader.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            fillQName(this.fAttributeQName, xMLStreamReader.getAttributeNamespace(i), xMLStreamReader.getAttributeLocalName(i), xMLStreamReader.getAttributePrefix(i));
            String attributeType = xMLStreamReader.getAttributeType(i);
            XMLAttributesImpl xMLAttributesImpl = this.fAttributes;
            QName qName = this.fAttributeQName;
            if (attributeType == null) {
                attributeType = XMLSymbols.fCDATASymbol;
            }
            xMLAttributesImpl.addAttributeNS(qName, attributeType, xMLStreamReader.getAttributeValue(i));
            this.fAttributes.setSpecified(i, xMLStreamReader.isAttributeSpecified(i));
        }
    }

    private void addNamespaceDeclarations() {
        String str;
        String str2;
        Iterator it = this.fDeclaredPrefixes.iterator();
        while (it.hasNext()) {
            String str3 = (String) it.next();
            String uri = this.fNamespaceContext.getURI(str3);
            if (str3.length() > 0) {
                str2 = XMLSymbols.PREFIX_XMLNS;
                this.fStringBuffer.clear();
                this.fStringBuffer.append(str2);
                this.fStringBuffer.append(':');
                this.fStringBuffer.append(str3);
                str = this.fSymbolTable.addSymbol(this.fStringBuffer.ch, this.fStringBuffer.offset, this.fStringBuffer.length);
            } else {
                str2 = XMLSymbols.EMPTY_STRING;
                str3 = XMLSymbols.PREFIX_XMLNS;
                str = XMLSymbols.PREFIX_XMLNS;
            }
            this.fAttributeQName.setValues(str2, str3, str, NamespaceContext.XMLNS_URI);
            XMLAttributesImpl xMLAttributesImpl = this.fAttributes;
            QName qName = this.fAttributeQName;
            String str4 = XMLSymbols.fCDATASymbol;
            if (uri == null) {
                uri = XMLSymbols.EMPTY_STRING;
            }
            xMLAttributesImpl.addAttribute(qName, str4, uri);
        }
    }

    private void fillDeclaredPrefixes(StartElement startElement) {
        fillDeclaredPrefixes(startElement.getNamespaces());
    }

    private void fillDeclaredPrefixes(EndElement endElement) {
        fillDeclaredPrefixes(endElement.getNamespaces());
    }

    private void fillDeclaredPrefixes(Iterator it) {
        this.fDeclaredPrefixes.clear();
        while (it.hasNext()) {
            String prefix = ((Namespace) it.next()).getPrefix();
            ArrayList arrayList = this.fDeclaredPrefixes;
            if (prefix == null) {
                prefix = "";
            }
            arrayList.add(prefix);
        }
    }

    private void fillDeclaredPrefixes(XMLStreamReader xMLStreamReader) {
        this.fDeclaredPrefixes.clear();
        int namespaceCount = xMLStreamReader.getNamespaceCount();
        for (int i = 0; i < namespaceCount; i++) {
            String namespacePrefix = xMLStreamReader.getNamespacePrefix(i);
            ArrayList arrayList = this.fDeclaredPrefixes;
            if (namespacePrefix == null) {
                namespacePrefix = "";
            }
            arrayList.add(namespacePrefix);
        }
    }

    private void fillQName(QName qName, ohos.javax.xml.namespace.QName qName2) {
        fillQName(qName, qName2.getNamespaceURI(), qName2.getLocalPart(), qName2.getPrefix());
    }

    /* access modifiers changed from: package-private */
    public final void fillQName(QName qName, String str, String str2, String str3) {
        String str4;
        String addSymbol = (str == null || str.length() <= 0) ? null : this.fSymbolTable.addSymbol(str);
        String addSymbol2 = str2 != null ? this.fSymbolTable.addSymbol(str2) : XMLSymbols.EMPTY_STRING;
        String addSymbol3 = (str3 == null || str3.length() <= 0) ? XMLSymbols.EMPTY_STRING : this.fSymbolTable.addSymbol(str3);
        if (addSymbol3 != XMLSymbols.EMPTY_STRING) {
            this.fStringBuffer.clear();
            this.fStringBuffer.append(addSymbol3);
            this.fStringBuffer.append(':');
            this.fStringBuffer.append(addSymbol2);
            str4 = this.fSymbolTable.addSymbol(this.fStringBuffer.ch, this.fStringBuffer.offset, this.fStringBuffer.length);
        } else {
            str4 = addSymbol2;
        }
        qName.setValues(addSymbol3, addSymbol2, str4, addSymbol);
    }
}
