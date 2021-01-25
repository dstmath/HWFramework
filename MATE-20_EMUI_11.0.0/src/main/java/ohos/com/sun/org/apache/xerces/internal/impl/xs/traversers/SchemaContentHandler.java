package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

import ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.SchemaDOMParser;
import ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.util.SAXLocatorWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLAttributesImpl;
import ohos.com.sun.org.apache.xerces.internal.util.XMLStringBuffer;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import ohos.org.w3c.dom.Document;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXParseException;
import ohos.org.xml.sax.helpers.LocatorImpl;

/* access modifiers changed from: package-private */
public final class SchemaContentHandler implements ContentHandler {
    private final QName fAttributeQName = new QName();
    private final XMLAttributesImpl fAttributes = new XMLAttributesImpl();
    private final QName fElementQName = new QName();
    private NamespaceSupport fNamespaceContext = new NamespaceSupport();
    private boolean fNamespacePrefixes = false;
    private boolean fNeedPushNSContext;
    private final SAXLocatorWrapper fSAXLocatorWrapper = new SAXLocatorWrapper();
    private SchemaDOMParser fSchemaDOMParser;
    private final XMLStringBuffer fStringBuffer = new XMLStringBuffer();
    private boolean fStringsInternalized = false;
    private SymbolTable fSymbolTable;
    private final XMLString fTempString = new XMLString();

    public void endPrefixMapping(String str) throws SAXException {
    }

    public void skippedEntity(String str) throws SAXException {
    }

    public Document getDocument() {
        return this.fSchemaDOMParser.getDocument();
    }

    public void setDocumentLocator(Locator locator) {
        this.fSAXLocatorWrapper.setLocator(locator);
    }

    public void startDocument() throws SAXException {
        this.fNeedPushNSContext = true;
        this.fNamespaceContext.reset();
        try {
            this.fSchemaDOMParser.startDocument(this.fSAXLocatorWrapper, null, this.fNamespaceContext, null);
        } catch (XMLParseException e) {
            convertToSAXParseException(e);
        } catch (XNIException e2) {
            convertToSAXException(e2);
        }
    }

    public void endDocument() throws SAXException {
        this.fSAXLocatorWrapper.setLocator(null);
        try {
            this.fSchemaDOMParser.endDocument(null);
        } catch (XMLParseException e) {
            convertToSAXParseException(e);
        } catch (XNIException e2) {
            convertToSAXException(e2);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0035, code lost:
        if (r4.length() == 0) goto L_0x0037;
     */
    public void startPrefixMapping(String str, String str2) throws SAXException {
        if (this.fNeedPushNSContext) {
            this.fNeedPushNSContext = false;
            this.fNamespaceContext.pushContext();
        }
        if (!this.fStringsInternalized) {
            str = str != null ? this.fSymbolTable.addSymbol(str) : XMLSymbols.EMPTY_STRING;
            if (str2 != null && str2.length() > 0) {
                str2 = this.fSymbolTable.addSymbol(str2);
                this.fNamespaceContext.declarePrefix(str, str2);
            }
        } else {
            if (str == null) {
                str = XMLSymbols.EMPTY_STRING;
            }
            if (str2 != null) {
            }
            this.fNamespaceContext.declarePrefix(str, str2);
        }
        str2 = null;
        this.fNamespaceContext.declarePrefix(str, str2);
    }

    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        int declaredPrefixCount;
        if (this.fNeedPushNSContext) {
            this.fNamespaceContext.pushContext();
        }
        this.fNeedPushNSContext = true;
        fillQName(this.fElementQName, str, str2, str3);
        fillXMLAttributes(attributes);
        if (!this.fNamespacePrefixes && (declaredPrefixCount = this.fNamespaceContext.getDeclaredPrefixCount()) > 0) {
            addNamespaceDeclarations(declaredPrefixCount);
        }
        try {
            this.fSchemaDOMParser.startElement(this.fElementQName, this.fAttributes, null);
        } catch (XMLParseException e) {
            convertToSAXParseException(e);
        } catch (XNIException e2) {
            convertToSAXException(e2);
        }
    }

    public void endElement(String str, String str2, String str3) throws SAXException {
        fillQName(this.fElementQName, str, str2, str3);
        try {
            this.fSchemaDOMParser.endElement(this.fElementQName, null);
        } catch (XMLParseException e) {
            convertToSAXParseException(e);
        } catch (XNIException e2) {
            convertToSAXException(e2);
        } catch (Throwable th) {
            this.fNamespaceContext.popContext();
            throw th;
        }
        this.fNamespaceContext.popContext();
    }

    public void characters(char[] cArr, int i, int i2) throws SAXException {
        try {
            this.fTempString.setValues(cArr, i, i2);
            this.fSchemaDOMParser.characters(this.fTempString, null);
        } catch (XMLParseException e) {
            convertToSAXParseException(e);
        } catch (XNIException e2) {
            convertToSAXException(e2);
        }
    }

    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
        try {
            this.fTempString.setValues(cArr, i, i2);
            this.fSchemaDOMParser.ignorableWhitespace(this.fTempString, null);
        } catch (XMLParseException e) {
            convertToSAXParseException(e);
        } catch (XNIException e2) {
            convertToSAXException(e2);
        }
    }

    public void processingInstruction(String str, String str2) throws SAXException {
        try {
            this.fTempString.setValues(str2.toCharArray(), 0, str2.length());
            this.fSchemaDOMParser.processingInstruction(str, this.fTempString, null);
        } catch (XMLParseException e) {
            convertToSAXParseException(e);
        } catch (XNIException e2) {
            convertToSAXException(e2);
        }
    }

    private void fillQName(QName qName, String str, String str2, String str3) {
        String str4;
        String str5 = null;
        if (!this.fStringsInternalized) {
            if (str != null && str.length() > 0) {
                str5 = this.fSymbolTable.addSymbol(str);
            }
            str4 = str2 != null ? this.fSymbolTable.addSymbol(str2) : XMLSymbols.EMPTY_STRING;
            str3 = str3 != null ? this.fSymbolTable.addSymbol(str3) : XMLSymbols.EMPTY_STRING;
        } else {
            if (str == null || str.length() != 0) {
                str5 = str;
            }
            if (str2 == null) {
                str2 = XMLSymbols.EMPTY_STRING;
            }
            str4 = str2;
            if (str3 == null) {
                str3 = XMLSymbols.EMPTY_STRING;
            }
        }
        String str6 = XMLSymbols.EMPTY_STRING;
        int indexOf = str3.indexOf(58);
        if (indexOf != -1) {
            str6 = this.fSymbolTable.addSymbol(str3.substring(0, indexOf));
            if (str4 == XMLSymbols.EMPTY_STRING) {
                str4 = this.fSymbolTable.addSymbol(str3.substring(indexOf + 1));
            }
        } else if (str4 == XMLSymbols.EMPTY_STRING) {
            str4 = str3;
        }
        qName.setValues(str6, str4, str3, str5);
    }

    private void fillXMLAttributes(Attributes attributes) {
        this.fAttributes.removeAllAttributes();
        int length = attributes.getLength();
        for (int i = 0; i < length; i++) {
            fillQName(this.fAttributeQName, attributes.getURI(i), attributes.getLocalName(i), attributes.getQName(i));
            String type = attributes.getType(i);
            XMLAttributesImpl xMLAttributesImpl = this.fAttributes;
            QName qName = this.fAttributeQName;
            if (type == null) {
                type = XMLSymbols.fCDATASymbol;
            }
            xMLAttributesImpl.addAttributeNS(qName, type, attributes.getValue(i));
            this.fAttributes.setSpecified(i, true);
        }
    }

    private void addNamespaceDeclarations(int i) {
        String str;
        String str2;
        for (int i2 = 0; i2 < i; i2++) {
            String declaredPrefixAt = this.fNamespaceContext.getDeclaredPrefixAt(i2);
            String uri = this.fNamespaceContext.getURI(declaredPrefixAt);
            if (declaredPrefixAt.length() > 0) {
                str2 = XMLSymbols.PREFIX_XMLNS;
                this.fStringBuffer.clear();
                this.fStringBuffer.append(str2);
                this.fStringBuffer.append(':');
                this.fStringBuffer.append(declaredPrefixAt);
                str = this.fSymbolTable.addSymbol(this.fStringBuffer.ch, this.fStringBuffer.offset, this.fStringBuffer.length);
            } else {
                str2 = XMLSymbols.EMPTY_STRING;
                declaredPrefixAt = XMLSymbols.PREFIX_XMLNS;
                str = XMLSymbols.PREFIX_XMLNS;
            }
            this.fAttributeQName.setValues(str2, declaredPrefixAt, str, NamespaceContext.XMLNS_URI);
            XMLAttributesImpl xMLAttributesImpl = this.fAttributes;
            QName qName = this.fAttributeQName;
            String str3 = XMLSymbols.fCDATASymbol;
            if (uri == null) {
                uri = XMLSymbols.EMPTY_STRING;
            }
            xMLAttributesImpl.addAttribute(qName, str3, uri);
        }
    }

    public void reset(SchemaDOMParser schemaDOMParser, SymbolTable symbolTable, boolean z, boolean z2) {
        this.fSchemaDOMParser = schemaDOMParser;
        this.fSymbolTable = symbolTable;
        this.fNamespacePrefixes = z;
        this.fStringsInternalized = z2;
    }

    static void convertToSAXParseException(XMLParseException xMLParseException) throws SAXException {
        SAXException exception = xMLParseException.getException();
        if (exception == null) {
            LocatorImpl locatorImpl = new LocatorImpl();
            locatorImpl.setPublicId(xMLParseException.getPublicId());
            locatorImpl.setSystemId(xMLParseException.getExpandedSystemId());
            locatorImpl.setLineNumber(xMLParseException.getLineNumber());
            locatorImpl.setColumnNumber(xMLParseException.getColumnNumber());
            throw new SAXParseException(xMLParseException.getMessage(), locatorImpl);
        } else if (exception instanceof SAXException) {
            throw exception;
        } else {
            throw new SAXException(exception);
        }
    }

    static void convertToSAXException(XNIException xNIException) throws SAXException {
        SAXException exception = xNIException.getException();
        if (exception == null) {
            throw new SAXException(xNIException.getMessage());
        } else if (exception instanceof SAXException) {
            throw exception;
        } else {
            throw new SAXException(exception);
        }
    }
}
