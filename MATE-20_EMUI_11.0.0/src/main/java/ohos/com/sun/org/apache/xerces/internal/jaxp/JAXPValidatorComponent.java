package ohos.com.sun.org.apache.xerces.internal.jaxp;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMInputImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.util.AttributesProxy;
import ohos.com.sun.org.apache.xerces.internal.util.AugmentationsImpl;
import ohos.com.sun.org.apache.xerces.internal.util.ErrorHandlerProxy;
import ohos.com.sun.org.apache.xerces.internal.util.ErrorHandlerWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.LocatorProxy;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.javax.xml.validation.TypeInfoProvider;
import ohos.javax.xml.validation.ValidatorHandler;
import ohos.org.w3c.dom.TypeInfo;
import ohos.org.w3c.dom.ls.LSInput;
import ohos.org.w3c.dom.ls.LSResourceResolver;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXParseException;
import ohos.org.xml.sax.helpers.DefaultHandler;

/* access modifiers changed from: package-private */
public final class JAXPValidatorComponent extends TeeXMLDocumentFilterImpl implements XMLComponent {
    private static final String ENTITY_MANAGER = "http://apache.org/xml/properties/internal/entity-manager";
    private static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    private static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    private static final TypeInfoProvider noInfoProvider = new TypeInfoProvider() {
        /* class ohos.com.sun.org.apache.xerces.internal.jaxp.JAXPValidatorComponent.AnonymousClass3 */

        public TypeInfo getAttributeTypeInfo(int i) {
            return null;
        }

        public TypeInfo getAttributeTypeInfo(String str) {
            return null;
        }

        public TypeInfo getAttributeTypeInfo(String str, String str2) {
            return null;
        }

        public TypeInfo getElementTypeInfo() {
            return null;
        }

        public boolean isIdAttribute(int i) {
            return false;
        }

        public boolean isSpecified(int i) {
            return false;
        }
    };
    private XMLAttributes fCurrentAttributes;
    private Augmentations fCurrentAug;
    private XMLEntityResolver fEntityResolver;
    private XMLErrorReporter fErrorReporter;
    private SymbolTable fSymbolTable;
    private final SAX2XNI sax2xni = new SAX2XNI();
    private final TypeInfoProvider typeInfoProvider;
    private final ValidatorHandler validator;
    private final XNI2SAX xni2sax = new XNI2SAX();

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public Boolean getFeatureDefault(String str) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public Object getPropertyDefault(String str) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public String[] getRecognizedFeatures() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setFeature(String str, boolean z) throws XMLConfigurationException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setProperty(String str, Object obj) throws XMLConfigurationException {
    }

    public JAXPValidatorComponent(ValidatorHandler validatorHandler) {
        this.validator = validatorHandler;
        TypeInfoProvider typeInfoProvider2 = validatorHandler.getTypeInfoProvider();
        this.typeInfoProvider = typeInfoProvider2 == null ? noInfoProvider : typeInfoProvider2;
        this.xni2sax.setContentHandler(this.validator);
        this.validator.setContentHandler(this.sax2xni);
        setSide(this.xni2sax);
        this.validator.setErrorHandler(new ErrorHandlerProxy() {
            /* class ohos.com.sun.org.apache.xerces.internal.jaxp.JAXPValidatorComponent.AnonymousClass1 */

            /* access modifiers changed from: protected */
            @Override // ohos.com.sun.org.apache.xerces.internal.util.ErrorHandlerProxy
            public XMLErrorHandler getErrorHandler() {
                XMLErrorHandler errorHandler = JAXPValidatorComponent.this.fErrorReporter.getErrorHandler();
                if (errorHandler != null) {
                    return errorHandler;
                }
                return new ErrorHandlerWrapper(DraconianErrorHandler.getInstance());
            }
        });
        this.validator.setResourceResolver(new LSResourceResolver() {
            /* class ohos.com.sun.org.apache.xerces.internal.jaxp.JAXPValidatorComponent.AnonymousClass2 */

            public LSInput resolveResource(String str, String str2, String str3, String str4, String str5) {
                if (JAXPValidatorComponent.this.fEntityResolver == null) {
                    return null;
                }
                try {
                    XMLInputSource resolveEntity = JAXPValidatorComponent.this.fEntityResolver.resolveEntity(new XMLResourceIdentifierImpl(str3, str4, str5, null));
                    if (resolveEntity == null) {
                        return null;
                    }
                    DOMInputImpl dOMInputImpl = new DOMInputImpl();
                    dOMInputImpl.setBaseURI(resolveEntity.getBaseSystemId());
                    dOMInputImpl.setByteStream(resolveEntity.getByteStream());
                    dOMInputImpl.setCharacterStream(resolveEntity.getCharacterStream());
                    dOMInputImpl.setEncoding(resolveEntity.getEncoding());
                    dOMInputImpl.setPublicId(resolveEntity.getPublicId());
                    dOMInputImpl.setSystemId(resolveEntity.getSystemId());
                    return dOMInputImpl;
                } catch (IOException e) {
                    throw new XNIException(e);
                }
            }
        });
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.TeeXMLDocumentFilterImpl, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        this.fCurrentAttributes = xMLAttributes;
        this.fCurrentAug = augmentations;
        this.xni2sax.startElement(qName, xMLAttributes, null);
        this.fCurrentAttributes = null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.TeeXMLDocumentFilterImpl, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endElement(QName qName, Augmentations augmentations) throws XNIException {
        this.fCurrentAug = augmentations;
        this.xni2sax.endElement(qName, null);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.TeeXMLDocumentFilterImpl, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void emptyElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        startElement(qName, xMLAttributes, augmentations);
        endElement(qName, augmentations);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.TeeXMLDocumentFilterImpl, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void characters(XMLString xMLString, Augmentations augmentations) throws XNIException {
        this.fCurrentAug = augmentations;
        this.xni2sax.characters(xMLString, null);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.TeeXMLDocumentFilterImpl, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void ignorableWhitespace(XMLString xMLString, Augmentations augmentations) throws XNIException {
        this.fCurrentAug = augmentations;
        this.xni2sax.ignorableWhitespace(xMLString, null);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void reset(XMLComponentManager xMLComponentManager) throws XMLConfigurationException {
        this.fSymbolTable = (SymbolTable) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/symbol-table");
        this.fErrorReporter = (XMLErrorReporter) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/error-reporter");
        try {
            this.fEntityResolver = (XMLEntityResolver) xMLComponentManager.getProperty(ENTITY_MANAGER);
        } catch (XMLConfigurationException unused) {
            this.fEntityResolver = null;
        }
    }

    private final class SAX2XNI extends DefaultHandler {
        private final Augmentations fAugmentations;
        private final QName fQName;

        private SAX2XNI() {
            this.fAugmentations = new AugmentationsImpl();
            this.fQName = new QName();
        }

        public void characters(char[] cArr, int i, int i2) throws SAXException {
            try {
                handler().characters(new XMLString(cArr, i, i2), aug());
            } catch (XNIException e) {
                throw toSAXException(e);
            }
        }

        public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
            try {
                handler().ignorableWhitespace(new XMLString(cArr, i, i2), aug());
            } catch (XNIException e) {
                throw toSAXException(e);
            }
        }

        public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
            try {
                JAXPValidatorComponent.this.updateAttributes(attributes);
                handler().startElement(toQName(str, str2, str3), JAXPValidatorComponent.this.fCurrentAttributes, elementAug());
            } catch (XNIException e) {
                throw toSAXException(e);
            }
        }

        public void endElement(String str, String str2, String str3) throws SAXException {
            try {
                handler().endElement(toQName(str, str2, str3), aug());
            } catch (XNIException e) {
                throw toSAXException(e);
            }
        }

        private Augmentations elementAug() {
            return aug();
        }

        private Augmentations aug() {
            if (JAXPValidatorComponent.this.fCurrentAug != null) {
                Augmentations augmentations = JAXPValidatorComponent.this.fCurrentAug;
                JAXPValidatorComponent.this.fCurrentAug = null;
                return augmentations;
            }
            this.fAugmentations.removeAllItems();
            return this.fAugmentations;
        }

        private XMLDocumentHandler handler() {
            return JAXPValidatorComponent.this.getDocumentHandler();
        }

        private SAXException toSAXException(XNIException xNIException) {
            SAXException exception = xNIException.getException();
            if (exception == null) {
                exception = xNIException;
            }
            if (exception instanceof SAXException) {
                return exception;
            }
            return new SAXException(exception);
        }

        private QName toQName(String str, String str2, String str3) {
            int indexOf = str3.indexOf(58);
            this.fQName.setValues(indexOf > 0 ? JAXPValidatorComponent.this.symbolize(str3.substring(0, indexOf)) : null, JAXPValidatorComponent.this.symbolize(str2), JAXPValidatorComponent.this.symbolize(str3), JAXPValidatorComponent.this.symbolize(str));
            return this.fQName;
        }
    }

    /* access modifiers changed from: private */
    public final class XNI2SAX extends DefaultXMLDocumentHandler {
        private final AttributesProxy fAttributesProxy;
        private ContentHandler fContentHandler;
        protected NamespaceContext fNamespaceContext;
        private String fVersion;

        private XNI2SAX() {
            this.fAttributesProxy = new AttributesProxy(null);
        }

        public void setContentHandler(ContentHandler contentHandler) {
            this.fContentHandler = contentHandler;
        }

        public ContentHandler getContentHandler() {
            return this.fContentHandler;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void xmlDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
            this.fVersion = str;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void startDocument(XMLLocator xMLLocator, String str, NamespaceContext namespaceContext, Augmentations augmentations) throws XNIException {
            this.fNamespaceContext = namespaceContext;
            this.fContentHandler.setDocumentLocator(new LocatorProxy(xMLLocator));
            try {
                this.fContentHandler.startDocument();
            } catch (SAXException e) {
                throw new XNIException((Exception) e);
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void endDocument(Augmentations augmentations) throws XNIException {
            try {
                this.fContentHandler.endDocument();
            } catch (SAXException e) {
                throw new XNIException((Exception) e);
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void processingInstruction(String str, XMLString xMLString, Augmentations augmentations) throws XNIException {
            try {
                this.fContentHandler.processingInstruction(str, xMLString.toString());
            } catch (SAXException e) {
                throw new XNIException((Exception) e);
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void startElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
            try {
                int declaredPrefixCount = this.fNamespaceContext.getDeclaredPrefixCount();
                String str = "";
                if (declaredPrefixCount > 0) {
                    for (int i = 0; i < declaredPrefixCount; i++) {
                        String declaredPrefixAt = this.fNamespaceContext.getDeclaredPrefixAt(i);
                        String uri = this.fNamespaceContext.getURI(declaredPrefixAt);
                        ContentHandler contentHandler = this.fContentHandler;
                        if (uri == null) {
                            uri = str;
                        }
                        contentHandler.startPrefixMapping(declaredPrefixAt, uri);
                    }
                }
                if (qName.uri != null) {
                    str = qName.uri;
                }
                String str2 = qName.localpart;
                this.fAttributesProxy.setAttributes(xMLAttributes);
                this.fContentHandler.startElement(str, str2, qName.rawname, this.fAttributesProxy);
            } catch (SAXException e) {
                throw new XNIException((Exception) e);
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void endElement(QName qName, Augmentations augmentations) throws XNIException {
            try {
                this.fContentHandler.endElement(qName.uri != null ? qName.uri : "", qName.localpart, qName.rawname);
                int declaredPrefixCount = this.fNamespaceContext.getDeclaredPrefixCount();
                if (declaredPrefixCount > 0) {
                    for (int i = 0; i < declaredPrefixCount; i++) {
                        this.fContentHandler.endPrefixMapping(this.fNamespaceContext.getDeclaredPrefixAt(i));
                    }
                }
            } catch (SAXException e) {
                throw new XNIException((Exception) e);
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void emptyElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
            startElement(qName, xMLAttributes, augmentations);
            endElement(qName, augmentations);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void characters(XMLString xMLString, Augmentations augmentations) throws XNIException {
            try {
                this.fContentHandler.characters(xMLString.ch, xMLString.offset, xMLString.length);
            } catch (SAXException e) {
                throw new XNIException((Exception) e);
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void ignorableWhitespace(XMLString xMLString, Augmentations augmentations) throws XNIException {
            try {
                this.fContentHandler.ignorableWhitespace(xMLString.ch, xMLString.offset, xMLString.length);
            } catch (SAXException e) {
                throw new XNIException((Exception) e);
            }
        }
    }

    private static final class DraconianErrorHandler implements ErrorHandler {
        private static final DraconianErrorHandler ERROR_HANDLER_INSTANCE = new DraconianErrorHandler();

        public void warning(SAXParseException sAXParseException) throws SAXException {
        }

        private DraconianErrorHandler() {
        }

        public static DraconianErrorHandler getInstance() {
            return ERROR_HANDLER_INSTANCE;
        }

        public void error(SAXParseException sAXParseException) throws SAXException {
            throw sAXParseException;
        }

        public void fatalError(SAXParseException sAXParseException) throws SAXException {
            throw sAXParseException;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAttributes(Attributes attributes) {
        String str;
        int length = attributes.getLength();
        for (int i = 0; i < length; i++) {
            String qName = attributes.getQName(i);
            int index = this.fCurrentAttributes.getIndex(qName);
            String value = attributes.getValue(i);
            if (index == -1) {
                int indexOf = qName.indexOf(58);
                if (indexOf < 0) {
                    str = null;
                } else {
                    str = symbolize(qName.substring(0, indexOf));
                }
                this.fCurrentAttributes.addAttribute(new QName(str, symbolize(attributes.getLocalName(i)), symbolize(qName), symbolize(attributes.getURI(i))), attributes.getType(i), value);
            } else if (!value.equals(this.fCurrentAttributes.getValue(index))) {
                this.fCurrentAttributes.setValue(index, value);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String symbolize(String str) {
        return this.fSymbolTable.addSymbol(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public String[] getRecognizedProperties() {
        return new String[]{ENTITY_MANAGER, "http://apache.org/xml/properties/internal/error-reporter", "http://apache.org/xml/properties/internal/symbol-table"};
    }
}
