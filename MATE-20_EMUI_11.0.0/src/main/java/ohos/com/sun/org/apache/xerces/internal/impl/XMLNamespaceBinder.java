package ohos.com.sun.org.apache.xerces.internal.impl;

import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;

public class XMLNamespaceBinder implements XMLComponent, XMLDocumentFilter {
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    private static final Boolean[] FEATURE_DEFAULTS = {null};
    protected static final String NAMESPACES = "http://xml.org/sax/features/namespaces";
    private static final Object[] PROPERTY_DEFAULTS = {null, null};
    private static final String[] RECOGNIZED_FEATURES = {"http://xml.org/sax/features/namespaces"};
    private static final String[] RECOGNIZED_PROPERTIES = {"http://apache.org/xml/properties/internal/symbol-table", "http://apache.org/xml/properties/internal/error-reporter"};
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    private QName fAttributeQName = new QName();
    protected XMLDocumentHandler fDocumentHandler;
    protected XMLDocumentSource fDocumentSource;
    protected XMLErrorReporter fErrorReporter;
    private NamespaceContext fNamespaceContext;
    protected boolean fNamespaces;
    protected boolean fOnlyPassPrefixMappingEvents;
    protected SymbolTable fSymbolTable;

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setFeature(String str, boolean z) throws XMLConfigurationException {
    }

    public void setOnlyPassPrefixMappingEvents(boolean z) {
        this.fOnlyPassPrefixMappingEvents = z;
    }

    public boolean getOnlyPassPrefixMappingEvents() {
        return this.fOnlyPassPrefixMappingEvents;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void reset(XMLComponentManager xMLComponentManager) throws XNIException {
        this.fNamespaces = xMLComponentManager.getFeature("http://xml.org/sax/features/namespaces", true);
        this.fSymbolTable = (SymbolTable) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/symbol-table");
        this.fErrorReporter = (XMLErrorReporter) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/error-reporter");
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public String[] getRecognizedFeatures() {
        return (String[]) RECOGNIZED_FEATURES.clone();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public String[] getRecognizedProperties() {
        return (String[]) RECOGNIZED_PROPERTIES.clone();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setProperty(String str, Object obj) throws XMLConfigurationException {
        if (str.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            int length = str.length() - 33;
            if (length == 21 && str.endsWith(Constants.SYMBOL_TABLE_PROPERTY)) {
                this.fSymbolTable = (SymbolTable) obj;
            } else if (length == 23 && str.endsWith(Constants.ERROR_REPORTER_PROPERTY)) {
                this.fErrorReporter = (XMLErrorReporter) obj;
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public Boolean getFeatureDefault(String str) {
        int i = 0;
        while (true) {
            String[] strArr = RECOGNIZED_FEATURES;
            if (i >= strArr.length) {
                return null;
            }
            if (strArr[i].equals(str)) {
                return FEATURE_DEFAULTS[i];
            }
            i++;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public Object getPropertyDefault(String str) {
        int i = 0;
        while (true) {
            String[] strArr = RECOGNIZED_PROPERTIES;
            if (i >= strArr.length) {
                return null;
            }
            if (strArr[i].equals(str)) {
                return PROPERTY_DEFAULTS[i];
            }
            i++;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource
    public void setDocumentHandler(XMLDocumentHandler xMLDocumentHandler) {
        this.fDocumentHandler = xMLDocumentHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource
    public XMLDocumentHandler getDocumentHandler() {
        return this.fDocumentHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void setDocumentSource(XMLDocumentSource xMLDocumentSource) {
        this.fDocumentSource = xMLDocumentSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public XMLDocumentSource getDocumentSource() {
        return this.fDocumentSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startGeneralEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && !this.fOnlyPassPrefixMappingEvents) {
            xMLDocumentHandler.startGeneralEntity(str, xMLResourceIdentifier, str2, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void textDecl(String str, String str2, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && !this.fOnlyPassPrefixMappingEvents) {
            xMLDocumentHandler.textDecl(str, str2, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startDocument(XMLLocator xMLLocator, String str, NamespaceContext namespaceContext, Augmentations augmentations) throws XNIException {
        this.fNamespaceContext = namespaceContext;
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && !this.fOnlyPassPrefixMappingEvents) {
            xMLDocumentHandler.startDocument(xMLLocator, str, namespaceContext, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void xmlDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && !this.fOnlyPassPrefixMappingEvents) {
            xMLDocumentHandler.xmlDecl(str, str2, str3, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void doctypeDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && !this.fOnlyPassPrefixMappingEvents) {
            xMLDocumentHandler.doctypeDecl(str, str2, str3, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void comment(XMLString xMLString, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && !this.fOnlyPassPrefixMappingEvents) {
            xMLDocumentHandler.comment(xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void processingInstruction(String str, XMLString xMLString, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && !this.fOnlyPassPrefixMappingEvents) {
            xMLDocumentHandler.processingInstruction(str, xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        if (this.fNamespaces) {
            handleStartElement(qName, xMLAttributes, augmentations, false);
            return;
        }
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            xMLDocumentHandler.startElement(qName, xMLAttributes, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void emptyElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        if (this.fNamespaces) {
            handleStartElement(qName, xMLAttributes, augmentations, true);
            handleEndElement(qName, augmentations, true);
            return;
        }
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            xMLDocumentHandler.emptyElement(qName, xMLAttributes, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void characters(XMLString xMLString, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && !this.fOnlyPassPrefixMappingEvents) {
            xMLDocumentHandler.characters(xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void ignorableWhitespace(XMLString xMLString, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && !this.fOnlyPassPrefixMappingEvents) {
            xMLDocumentHandler.ignorableWhitespace(xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endElement(QName qName, Augmentations augmentations) throws XNIException {
        if (this.fNamespaces) {
            handleEndElement(qName, augmentations, false);
            return;
        }
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            xMLDocumentHandler.endElement(qName, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startCDATA(Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && !this.fOnlyPassPrefixMappingEvents) {
            xMLDocumentHandler.startCDATA(augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endCDATA(Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && !this.fOnlyPassPrefixMappingEvents) {
            xMLDocumentHandler.endCDATA(augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endDocument(Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && !this.fOnlyPassPrefixMappingEvents) {
            xMLDocumentHandler.endDocument(augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endGeneralEntity(String str, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && !this.fOnlyPassPrefixMappingEvents) {
            xMLDocumentHandler.endGeneralEntity(str, augmentations);
        }
    }

    /* access modifiers changed from: protected */
    public void handleStartElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations, boolean z) throws XNIException {
        int i;
        this.fNamespaceContext.pushContext();
        if (qName.prefix == XMLSymbols.PREFIX_XMLNS) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "ElementXMLNSPrefix", new Object[]{qName.rawname}, 2);
        }
        int length = xMLAttributes.getLength();
        for (int i2 = 0; i2 < length; i2++) {
            String localName = xMLAttributes.getLocalName(i2);
            String prefix = xMLAttributes.getPrefix(i2);
            if (prefix == XMLSymbols.PREFIX_XMLNS || (prefix == XMLSymbols.EMPTY_STRING && localName == XMLSymbols.PREFIX_XMLNS)) {
                String addSymbol = this.fSymbolTable.addSymbol(xMLAttributes.getValue(i2));
                if (prefix == XMLSymbols.PREFIX_XMLNS && localName == XMLSymbols.PREFIX_XMLNS) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXMLNS", new Object[]{xMLAttributes.getQName(i2)}, 2);
                }
                if (addSymbol == NamespaceContext.XMLNS_URI) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXMLNS", new Object[]{xMLAttributes.getQName(i2)}, 2);
                }
                if (localName == XMLSymbols.PREFIX_XML) {
                    if (addSymbol != NamespaceContext.XML_URI) {
                        this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXML", new Object[]{xMLAttributes.getQName(i2)}, 2);
                    }
                } else if (addSymbol == NamespaceContext.XML_URI) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXML", new Object[]{xMLAttributes.getQName(i2)}, 2);
                }
                String str = localName != XMLSymbols.PREFIX_XMLNS ? localName : XMLSymbols.EMPTY_STRING;
                if (prefixBoundToNullURI(addSymbol, localName)) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "EmptyPrefixedAttName", new Object[]{xMLAttributes.getQName(i2)}, 2);
                } else {
                    NamespaceContext namespaceContext = this.fNamespaceContext;
                    if (addSymbol.length() == 0) {
                        addSymbol = null;
                    }
                    namespaceContext.declarePrefix(str, addSymbol);
                }
            }
        }
        qName.uri = this.fNamespaceContext.getURI(qName.prefix != null ? qName.prefix : XMLSymbols.EMPTY_STRING);
        if (qName.prefix == null && qName.uri != null) {
            qName.prefix = XMLSymbols.EMPTY_STRING;
        }
        if (qName.prefix != null && qName.uri == null) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "ElementPrefixUnbound", new Object[]{qName.prefix, qName.rawname}, 2);
        }
        int i3 = 0;
        while (true) {
            i = 3;
            if (i3 >= length) {
                break;
            }
            xMLAttributes.getName(i3, this.fAttributeQName);
            String str2 = this.fAttributeQName.prefix != null ? this.fAttributeQName.prefix : XMLSymbols.EMPTY_STRING;
            String str3 = this.fAttributeQName.rawname;
            if (str3 == XMLSymbols.PREFIX_XMLNS) {
                this.fAttributeQName.uri = this.fNamespaceContext.getURI(XMLSymbols.PREFIX_XMLNS);
                xMLAttributes.setName(i3, this.fAttributeQName);
            } else if (str2 != XMLSymbols.EMPTY_STRING) {
                this.fAttributeQName.uri = this.fNamespaceContext.getURI(str2);
                if (this.fAttributeQName.uri == null) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "AttributePrefixUnbound", new Object[]{qName.rawname, str3, str2}, 2);
                }
                xMLAttributes.setName(i3, this.fAttributeQName);
            }
            i3++;
        }
        int length2 = xMLAttributes.getLength();
        int i4 = 0;
        while (i4 < length2 - 1) {
            String uri = xMLAttributes.getURI(i4);
            if (!(uri == null || uri == NamespaceContext.XMLNS_URI)) {
                String localName2 = xMLAttributes.getLocalName(i4);
                int i5 = i4 + 1;
                while (i5 < length2) {
                    String localName3 = xMLAttributes.getLocalName(i5);
                    String uri2 = xMLAttributes.getURI(i5);
                    if (localName2 == localName3 && uri == uri2) {
                        XMLErrorReporter xMLErrorReporter = this.fErrorReporter;
                        Object[] objArr = new Object[i];
                        objArr[0] = qName.rawname;
                        objArr[1] = localName2;
                        objArr[2] = uri;
                        xMLErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "AttributeNSNotUnique", objArr, 2);
                    }
                    i5++;
                    i = 3;
                }
            }
            i4++;
            i = 3;
        }
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && !this.fOnlyPassPrefixMappingEvents) {
            if (z) {
                xMLDocumentHandler.emptyElement(qName, xMLAttributes, augmentations);
            } else {
                xMLDocumentHandler.startElement(qName, xMLAttributes, augmentations);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleEndElement(QName qName, Augmentations augmentations, boolean z) throws XNIException {
        String str = qName.prefix != null ? qName.prefix : XMLSymbols.EMPTY_STRING;
        qName.uri = this.fNamespaceContext.getURI(str);
        if (qName.uri != null) {
            qName.prefix = str;
        }
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && !this.fOnlyPassPrefixMappingEvents && !z) {
            xMLDocumentHandler.endElement(qName, augmentations);
        }
        this.fNamespaceContext.popContext();
    }

    /* access modifiers changed from: protected */
    public boolean prefixBoundToNullURI(String str, String str2) {
        return str == XMLSymbols.EMPTY_STRING && str2 != XMLSymbols.PREFIX_XMLNS;
    }
}
