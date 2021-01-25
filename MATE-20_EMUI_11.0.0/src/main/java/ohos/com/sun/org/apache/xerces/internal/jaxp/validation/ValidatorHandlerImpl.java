package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.impl.validation.EntityState;
import ohos.com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator;
import ohos.com.sun.org.apache.xerces.internal.parsers.SAXParser;
import ohos.com.sun.org.apache.xerces.internal.util.AttributesProxy;
import ohos.com.sun.org.apache.xerces.internal.util.SAXLocatorWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.SAXMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.util.Status;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.URI;
import ohos.com.sun.org.apache.xerces.internal.util.XMLAttributesImpl;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import ohos.com.sun.org.apache.xerces.internal.xs.AttributePSVI;
import ohos.com.sun.org.apache.xerces.internal.xs.ElementPSVI;
import ohos.com.sun.org.apache.xerces.internal.xs.ItemPSVI;
import ohos.com.sun.org.apache.xerces.internal.xs.PSVIProvider;
import ohos.javax.xml.parsers.FactoryConfigurationError;
import ohos.javax.xml.transform.Result;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.sax.SAXResult;
import ohos.javax.xml.transform.sax.SAXSource;
import ohos.javax.xml.validation.TypeInfoProvider;
import ohos.javax.xml.validation.ValidatorHandler;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.w3c.dom.TypeInfo;
import ohos.org.w3c.dom.ls.LSInput;
import ohos.org.w3c.dom.ls.LSResourceResolver;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;
import ohos.org.xml.sax.XMLReader;
import ohos.org.xml.sax.ext.Attributes2;
import ohos.org.xml.sax.ext.EntityResolver2;

/* access modifiers changed from: package-private */
public final class ValidatorHandlerImpl extends ValidatorHandler implements DTDHandler, EntityState, PSVIProvider, ValidatorHelper, XMLDocumentHandler {
    private static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    private static final String NAMESPACE_CONTEXT = "http://apache.org/xml/properties/internal/namespace-context";
    private static final String NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes";
    private static final String SCHEMA_VALIDATOR = "http://apache.org/xml/properties/internal/validator/schema";
    private static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    protected static final String STRING_INTERNING = "http://xml.org/sax/features/string-interning";
    private static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    private static final String VALIDATION_MANAGER = "http://apache.org/xml/properties/internal/validation-manager";
    private static final String XML_SECURITY_PROPERTY_MANAGER = "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager";
    private final AttributesProxy fAttrAdapter;
    private final QName fAttributeQName;
    private final XMLAttributesImpl fAttributes;
    private XMLSchemaValidatorComponentManager fComponentManager;
    private ContentHandler fContentHandler;
    private final QName fElementQName;
    private XMLErrorReporter fErrorReporter;
    private NamespaceContext fNamespaceContext;
    private boolean fNeedPushNSContext;
    private final ResolutionForwarder fResolutionForwarder;
    private final SAXLocatorWrapper fSAXLocatorWrapper;
    private XMLSchemaValidator fSchemaValidator;
    private boolean fStringsInternalized;
    private SymbolTable fSymbolTable;
    private final XMLString fTempString;
    private final XMLSchemaTypeInfoProvider fTypeInfoProvider;
    private HashMap fUnparsedEntities;
    private ValidationManager fValidationManager;

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void comment(XMLString xMLString, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void doctypeDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endCDATA(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endGeneralEntity(String str, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.validation.EntityState
    public boolean isEntityDeclared(String str) {
        return false;
    }

    public void notationDecl(String str, String str2, String str3) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void setDocumentSource(XMLDocumentSource xMLDocumentSource) {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startCDATA(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startGeneralEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void textDecl(String str, String str2, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void xmlDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
    }

    public ValidatorHandlerImpl(XSGrammarPoolContainer xSGrammarPoolContainer) {
        this(new XMLSchemaValidatorComponentManager(xSGrammarPoolContainer));
        this.fComponentManager.addRecognizedFeatures(new String[]{NAMESPACE_PREFIXES});
        this.fComponentManager.setFeature(NAMESPACE_PREFIXES, false);
        setErrorHandler(null);
        setResourceResolver(null);
    }

    public ValidatorHandlerImpl(XMLSchemaValidatorComponentManager xMLSchemaValidatorComponentManager) {
        this.fSAXLocatorWrapper = new SAXLocatorWrapper();
        this.fNeedPushNSContext = true;
        this.fUnparsedEntities = null;
        this.fStringsInternalized = false;
        this.fElementQName = new QName();
        this.fAttributeQName = new QName();
        this.fAttributes = new XMLAttributesImpl();
        this.fAttrAdapter = new AttributesProxy(this.fAttributes);
        this.fTempString = new XMLString();
        this.fContentHandler = null;
        this.fTypeInfoProvider = new XMLSchemaTypeInfoProvider();
        this.fResolutionForwarder = new ResolutionForwarder(null);
        this.fComponentManager = xMLSchemaValidatorComponentManager;
        this.fErrorReporter = (XMLErrorReporter) this.fComponentManager.getProperty("http://apache.org/xml/properties/internal/error-reporter");
        this.fNamespaceContext = (NamespaceContext) this.fComponentManager.getProperty(NAMESPACE_CONTEXT);
        this.fSchemaValidator = (XMLSchemaValidator) this.fComponentManager.getProperty(SCHEMA_VALIDATOR);
        this.fSymbolTable = (SymbolTable) this.fComponentManager.getProperty("http://apache.org/xml/properties/internal/symbol-table");
        this.fValidationManager = (ValidationManager) this.fComponentManager.getProperty(VALIDATION_MANAGER);
    }

    public void setContentHandler(ContentHandler contentHandler) {
        this.fContentHandler = contentHandler;
    }

    public ContentHandler getContentHandler() {
        return this.fContentHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.fComponentManager.setErrorHandler(errorHandler);
    }

    public ErrorHandler getErrorHandler() {
        return this.fComponentManager.getErrorHandler();
    }

    public void setResourceResolver(LSResourceResolver lSResourceResolver) {
        this.fComponentManager.setResourceResolver(lSResourceResolver);
    }

    public LSResourceResolver getResourceResolver() {
        return this.fComponentManager.getResourceResolver();
    }

    public TypeInfoProvider getTypeInfoProvider() {
        return this.fTypeInfoProvider;
    }

    public boolean getFeature(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str != null) {
            try {
                return this.fComponentManager.getFeature(str);
            } catch (XMLConfigurationException e) {
                throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fComponentManager.getLocale(), e.getType() == Status.NOT_RECOGNIZED ? "feature-not-recognized" : "feature-not-supported", new Object[]{e.getIdentifier()}));
            }
        } else {
            throw new NullPointerException();
        }
    }

    public void setFeature(String str, boolean z) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str != null) {
            try {
                this.fComponentManager.setFeature(str, z);
            } catch (XMLConfigurationException e) {
                String identifier = e.getIdentifier();
                if (e.getType() != Status.NOT_ALLOWED) {
                    throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fComponentManager.getLocale(), e.getType() == Status.NOT_RECOGNIZED ? "feature-not-recognized" : "feature-not-supported", new Object[]{identifier}));
                }
                throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fComponentManager.getLocale(), "jaxp-secureprocessing-feature", null));
            }
        } else {
            throw new NullPointerException();
        }
    }

    public Object getProperty(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str != null) {
            try {
                return this.fComponentManager.getProperty(str);
            } catch (XMLConfigurationException e) {
                throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fComponentManager.getLocale(), e.getType() == Status.NOT_RECOGNIZED ? "property-not-recognized" : "property-not-supported", new Object[]{e.getIdentifier()}));
            }
        } else {
            throw new NullPointerException();
        }
    }

    public void setProperty(String str, Object obj) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str != null) {
            try {
                this.fComponentManager.setProperty(str, obj);
            } catch (XMLConfigurationException e) {
                throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fComponentManager.getLocale(), e.getType() == Status.NOT_RECOGNIZED ? "property-not-recognized" : "property-not-supported", new Object[]{e.getIdentifier()}));
            }
        } else {
            throw new NullPointerException();
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.validation.EntityState
    public boolean isEntityUnparsed(String str) {
        HashMap hashMap = this.fUnparsedEntities;
        if (hashMap != null) {
            return hashMap.containsKey(str);
        }
        return false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startDocument(XMLLocator xMLLocator, String str, NamespaceContext namespaceContext, Augmentations augmentations) throws XNIException {
        ContentHandler contentHandler = this.fContentHandler;
        if (contentHandler != null) {
            try {
                contentHandler.startDocument();
            } catch (SAXException e) {
                throw new XNIException((Exception) e);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void processingInstruction(String str, XMLString xMLString, Augmentations augmentations) throws XNIException {
        ContentHandler contentHandler = this.fContentHandler;
        if (contentHandler != null) {
            try {
                contentHandler.processingInstruction(str, xMLString.toString());
            } catch (SAXException e) {
                throw new XNIException((Exception) e);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        if (this.fContentHandler != null) {
            try {
                this.fTypeInfoProvider.beginStartElement(augmentations, xMLAttributes);
                this.fContentHandler.startElement(qName.uri != null ? qName.uri : XMLSymbols.EMPTY_STRING, qName.localpart, qName.rawname, this.fAttrAdapter);
                this.fTypeInfoProvider.finishStartElement();
            } catch (SAXException e) {
                throw new XNIException((Exception) e);
            } catch (Throwable th) {
                this.fTypeInfoProvider.finishStartElement();
                throw th;
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void emptyElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        startElement(qName, xMLAttributes, augmentations);
        endElement(qName, augmentations);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void characters(XMLString xMLString, Augmentations augmentations) throws XNIException {
        if (this.fContentHandler != null && xMLString.length != 0) {
            try {
                this.fContentHandler.characters(xMLString.ch, xMLString.offset, xMLString.length);
            } catch (SAXException e) {
                throw new XNIException((Exception) e);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void ignorableWhitespace(XMLString xMLString, Augmentations augmentations) throws XNIException {
        ContentHandler contentHandler = this.fContentHandler;
        if (contentHandler != null) {
            try {
                contentHandler.ignorableWhitespace(xMLString.ch, xMLString.offset, xMLString.length);
            } catch (SAXException e) {
                throw new XNIException((Exception) e);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endElement(QName qName, Augmentations augmentations) throws XNIException {
        if (this.fContentHandler != null) {
            try {
                this.fTypeInfoProvider.beginEndElement(augmentations);
                this.fContentHandler.endElement(qName.uri != null ? qName.uri : XMLSymbols.EMPTY_STRING, qName.localpart, qName.rawname);
                this.fTypeInfoProvider.finishEndElement();
            } catch (SAXException e) {
                throw new XNIException((Exception) e);
            } catch (Throwable th) {
                this.fTypeInfoProvider.finishEndElement();
                throw th;
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endDocument(Augmentations augmentations) throws XNIException {
        ContentHandler contentHandler = this.fContentHandler;
        if (contentHandler != null) {
            try {
                contentHandler.endDocument();
            } catch (SAXException e) {
                throw new XNIException((Exception) e);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public XMLDocumentSource getDocumentSource() {
        return this.fSchemaValidator;
    }

    public void setDocumentLocator(Locator locator) {
        this.fSAXLocatorWrapper.setLocator(locator);
        ContentHandler contentHandler = this.fContentHandler;
        if (contentHandler != null) {
            contentHandler.setDocumentLocator(locator);
        }
    }

    public void startDocument() throws SAXException {
        this.fComponentManager.reset();
        this.fSchemaValidator.setDocumentHandler(this);
        this.fValidationManager.setEntityState(this);
        this.fTypeInfoProvider.finishStartElement();
        this.fNeedPushNSContext = true;
        HashMap hashMap = this.fUnparsedEntities;
        if (hashMap != null && !hashMap.isEmpty()) {
            this.fUnparsedEntities.clear();
        }
        this.fErrorReporter.setDocumentLocator(this.fSAXLocatorWrapper);
        try {
            this.fSchemaValidator.startDocument(this.fSAXLocatorWrapper, this.fSAXLocatorWrapper.getEncoding(), this.fNamespaceContext, null);
        } catch (XMLParseException e) {
            throw Util.toSAXParseException(e);
        } catch (XNIException e2) {
            throw Util.toSAXException(e2);
        }
    }

    public void endDocument() throws SAXException {
        this.fSAXLocatorWrapper.setLocator(null);
        try {
            this.fSchemaValidator.endDocument(null);
        } catch (XMLParseException e) {
            throw Util.toSAXParseException(e);
        } catch (XNIException e2) {
            throw Util.toSAXException(e2);
        }
    }

    public void startPrefixMapping(String str, String str2) throws SAXException {
        String str3;
        String str4 = null;
        if (!this.fStringsInternalized) {
            str3 = str != null ? this.fSymbolTable.addSymbol(str) : XMLSymbols.EMPTY_STRING;
            if (str2 != null && str2.length() > 0) {
                str4 = this.fSymbolTable.addSymbol(str2);
            }
        } else {
            if (str != null) {
                str3 = str;
            } else {
                str3 = XMLSymbols.EMPTY_STRING;
            }
            if (str2 != null && str2.length() > 0) {
                str4 = str2;
            }
        }
        if (this.fNeedPushNSContext) {
            this.fNeedPushNSContext = false;
            this.fNamespaceContext.pushContext();
        }
        this.fNamespaceContext.declarePrefix(str3, str4);
        ContentHandler contentHandler = this.fContentHandler;
        if (contentHandler != null) {
            contentHandler.startPrefixMapping(str, str2);
        }
    }

    public void endPrefixMapping(String str) throws SAXException {
        ContentHandler contentHandler = this.fContentHandler;
        if (contentHandler != null) {
            contentHandler.endPrefixMapping(str);
        }
    }

    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        if (this.fNeedPushNSContext) {
            this.fNamespaceContext.pushContext();
        }
        this.fNeedPushNSContext = true;
        fillQName(this.fElementQName, str, str2, str3);
        if (attributes instanceof Attributes2) {
            fillXMLAttributes2((Attributes2) attributes);
        } else {
            fillXMLAttributes(attributes);
        }
        try {
            this.fSchemaValidator.startElement(this.fElementQName, this.fAttributes, null);
        } catch (XMLParseException e) {
            throw Util.toSAXParseException(e);
        } catch (XNIException e2) {
            throw Util.toSAXException(e2);
        }
    }

    public void endElement(String str, String str2, String str3) throws SAXException {
        fillQName(this.fElementQName, str, str2, str3);
        try {
            this.fSchemaValidator.endElement(this.fElementQName, null);
            this.fNamespaceContext.popContext();
        } catch (XMLParseException e) {
            throw Util.toSAXParseException(e);
        } catch (XNIException e2) {
            throw Util.toSAXException(e2);
        } catch (Throwable th) {
            this.fNamespaceContext.popContext();
            throw th;
        }
    }

    public void characters(char[] cArr, int i, int i2) throws SAXException {
        try {
            this.fTempString.setValues(cArr, i, i2);
            this.fSchemaValidator.characters(this.fTempString, null);
        } catch (XMLParseException e) {
            throw Util.toSAXParseException(e);
        } catch (XNIException e2) {
            throw Util.toSAXException(e2);
        }
    }

    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
        try {
            this.fTempString.setValues(cArr, i, i2);
            this.fSchemaValidator.ignorableWhitespace(this.fTempString, null);
        } catch (XMLParseException e) {
            throw Util.toSAXParseException(e);
        } catch (XNIException e2) {
            throw Util.toSAXException(e2);
        }
    }

    public void processingInstruction(String str, String str2) throws SAXException {
        ContentHandler contentHandler = this.fContentHandler;
        if (contentHandler != null) {
            contentHandler.processingInstruction(str, str2);
        }
    }

    public void skippedEntity(String str) throws SAXException {
        ContentHandler contentHandler = this.fContentHandler;
        if (contentHandler != null) {
            contentHandler.skippedEntity(str);
        }
    }

    public void unparsedEntityDecl(String str, String str2, String str3, String str4) throws SAXException {
        if (this.fUnparsedEntities == null) {
            this.fUnparsedEntities = new HashMap();
        }
        this.fUnparsedEntities.put(str, str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.ValidatorHelper
    public void validate(Source source, Result result) throws SAXException, IOException {
        if ((result instanceof SAXResult) || result == null) {
            SAXSource sAXSource = (SAXSource) source;
            SAXResult sAXResult = (SAXResult) result;
            if (result != null) {
                setContentHandler(sAXResult.getHandler());
            }
            try {
                XMLReader xMLReader = sAXSource.getXMLReader();
                if (xMLReader == null) {
                    xMLReader = JdkXmlUtils.getXMLReader(this.fComponentManager.getFeature("jdk.xml.overrideDefaultParser"), this.fComponentManager.getFeature(Constants.FEATURE_SECURE_PROCESSING));
                    try {
                        if (xMLReader instanceof SAXParser) {
                            XMLSecurityManager xMLSecurityManager = (XMLSecurityManager) this.fComponentManager.getProperty("http://apache.org/xml/properties/security-manager");
                            if (xMLSecurityManager != null) {
                                try {
                                    xMLReader.setProperty("http://apache.org/xml/properties/security-manager", xMLSecurityManager);
                                } catch (SAXException unused) {
                                }
                            }
                            try {
                                xMLReader.setProperty("http://ohos.javax.xml.XMLConstants/property/accessExternalDTD", ((XMLSecurityPropertyManager) this.fComponentManager.getProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager")).getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD));
                            } catch (SAXException e) {
                                XMLSecurityManager.printWarning(xMLReader.getClass().getName(), "http://ohos.javax.xml.XMLConstants/property/accessExternalDTD", e);
                            }
                        }
                    } catch (Exception e2) {
                        throw new FactoryConfigurationError(e2);
                    }
                }
                try {
                    this.fStringsInternalized = xMLReader.getFeature(STRING_INTERNING);
                } catch (SAXException unused2) {
                    this.fStringsInternalized = false;
                }
                DraconianErrorHandler errorHandler = this.fComponentManager.getErrorHandler();
                if (errorHandler == null) {
                    errorHandler = DraconianErrorHandler.getInstance();
                }
                xMLReader.setErrorHandler(errorHandler);
                xMLReader.setEntityResolver(this.fResolutionForwarder);
                this.fResolutionForwarder.setEntityResolver(this.fComponentManager.getResourceResolver());
                xMLReader.setContentHandler(this);
                xMLReader.setDTDHandler(this);
                xMLReader.parse(sAXSource.getInputSource());
            } finally {
                setContentHandler(null);
            }
        } else {
            throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(this.fComponentManager.getLocale(), "SourceResultMismatch", new Object[]{source.getClass().getName(), result.getClass().getName()}));
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.PSVIProvider
    public ElementPSVI getElementPSVI() {
        return this.fTypeInfoProvider.getElementPSVI();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.PSVIProvider
    public AttributePSVI getAttributePSVI(int i) {
        return this.fTypeInfoProvider.getAttributePSVI(i);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.PSVIProvider
    public AttributePSVI getAttributePSVIByName(String str, String str2) {
        return this.fTypeInfoProvider.getAttributePSVIByName(str, str2);
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
        }
        qName.setValues(str6, str4, str3, str5);
    }

    private void fillXMLAttributes(Attributes attributes) {
        this.fAttributes.removeAllAttributes();
        int length = attributes.getLength();
        for (int i = 0; i < length; i++) {
            fillXMLAttribute(attributes, i);
            this.fAttributes.setSpecified(i, true);
        }
    }

    private void fillXMLAttributes2(Attributes2 attributes2) {
        this.fAttributes.removeAllAttributes();
        int length = attributes2.getLength();
        for (int i = 0; i < length; i++) {
            fillXMLAttribute(attributes2, i);
            this.fAttributes.setSpecified(i, attributes2.isSpecified(i));
            if (attributes2.isDeclared(i)) {
                this.fAttributes.getAugmentations(i).putItem(Constants.ATTRIBUTE_DECLARED, Boolean.TRUE);
            }
        }
    }

    private void fillXMLAttribute(Attributes attributes, int i) {
        fillQName(this.fAttributeQName, attributes.getURI(i), attributes.getLocalName(i), attributes.getQName(i));
        String type = attributes.getType(i);
        XMLAttributesImpl xMLAttributesImpl = this.fAttributes;
        QName qName = this.fAttributeQName;
        if (type == null) {
            type = XMLSymbols.fCDATASymbol;
        }
        xMLAttributesImpl.addAttributeNS(qName, type, attributes.getValue(i));
    }

    /* access modifiers changed from: private */
    public class XMLSchemaTypeInfoProvider extends TypeInfoProvider {
        private XMLAttributes fAttributes;
        private Augmentations fElementAugs;
        private boolean fInEndElement;
        private boolean fInStartElement;

        private XMLSchemaTypeInfoProvider() {
            this.fInStartElement = false;
            this.fInEndElement = false;
        }

        /* access modifiers changed from: package-private */
        public void beginStartElement(Augmentations augmentations, XMLAttributes xMLAttributes) {
            this.fInStartElement = true;
            this.fElementAugs = augmentations;
            this.fAttributes = xMLAttributes;
        }

        /* access modifiers changed from: package-private */
        public void finishStartElement() {
            this.fInStartElement = false;
            this.fElementAugs = null;
            this.fAttributes = null;
        }

        /* access modifiers changed from: package-private */
        public void beginEndElement(Augmentations augmentations) {
            this.fInEndElement = true;
            this.fElementAugs = augmentations;
        }

        /* access modifiers changed from: package-private */
        public void finishEndElement() {
            this.fInEndElement = false;
            this.fElementAugs = null;
        }

        private void checkState(boolean z) {
            if (this.fInStartElement) {
                return;
            }
            if (!this.fInEndElement || !z) {
                throw new IllegalStateException(JAXPValidationMessageFormatter.formatMessage(ValidatorHandlerImpl.this.fComponentManager.getLocale(), "TypeInfoProviderIllegalState", null));
            }
        }

        public TypeInfo getAttributeTypeInfo(int i) {
            checkState(false);
            return getAttributeType(i);
        }

        private TypeInfo getAttributeType(int i) {
            checkState(false);
            if (i < 0 || this.fAttributes.getLength() <= i) {
                throw new IndexOutOfBoundsException(Integer.toString(i));
            }
            Augmentations augmentations = this.fAttributes.getAugmentations(i);
            if (augmentations == null) {
                return null;
            }
            return getTypeInfoFromPSVI((AttributePSVI) augmentations.getItem(Constants.ATTRIBUTE_PSVI));
        }

        public TypeInfo getAttributeTypeInfo(String str, String str2) {
            checkState(false);
            return getAttributeTypeInfo(this.fAttributes.getIndex(str, str2));
        }

        public TypeInfo getAttributeTypeInfo(String str) {
            checkState(false);
            return getAttributeTypeInfo(this.fAttributes.getIndex(str));
        }

        public TypeInfo getElementTypeInfo() {
            checkState(true);
            Augmentations augmentations = this.fElementAugs;
            if (augmentations == null) {
                return null;
            }
            return getTypeInfoFromPSVI((ElementPSVI) augmentations.getItem(Constants.ELEMENT_PSVI));
        }

        private TypeInfo getTypeInfoFromPSVI(ItemPSVI itemPSVI) {
            TypeInfo memberTypeDefinition;
            if (itemPSVI == null) {
                return null;
            }
            if (itemPSVI.getValidity() != 2 || (memberTypeDefinition = itemPSVI.getMemberTypeDefinition()) == null) {
                TypeInfo typeDefinition = itemPSVI.getTypeDefinition();
                if (typeDefinition == null || !(typeDefinition instanceof TypeInfo)) {
                    return null;
                }
                return typeDefinition;
            } else if (memberTypeDefinition instanceof TypeInfo) {
                return memberTypeDefinition;
            } else {
                return null;
            }
        }

        public boolean isIdAttribute(int i) {
            checkState(false);
            XSSimpleType attributeType = getAttributeType(i);
            if (attributeType == null) {
                return false;
            }
            return attributeType.isIDType();
        }

        public boolean isSpecified(int i) {
            checkState(false);
            return this.fAttributes.isSpecified(i);
        }

        /* access modifiers changed from: package-private */
        public ElementPSVI getElementPSVI() {
            Augmentations augmentations = this.fElementAugs;
            if (augmentations != null) {
                return (ElementPSVI) augmentations.getItem(Constants.ELEMENT_PSVI);
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public AttributePSVI getAttributePSVI(int i) {
            Augmentations augmentations;
            XMLAttributes xMLAttributes = this.fAttributes;
            if (xMLAttributes == null || (augmentations = xMLAttributes.getAugmentations(i)) == null) {
                return null;
            }
            return (AttributePSVI) augmentations.getItem(Constants.ATTRIBUTE_PSVI);
        }

        /* access modifiers changed from: package-private */
        public AttributePSVI getAttributePSVIByName(String str, String str2) {
            Augmentations augmentations;
            XMLAttributes xMLAttributes = this.fAttributes;
            if (xMLAttributes == null || (augmentations = xMLAttributes.getAugmentations(str, str2)) == null) {
                return null;
            }
            return (AttributePSVI) augmentations.getItem(Constants.ATTRIBUTE_PSVI);
        }
    }

    static final class ResolutionForwarder implements EntityResolver2 {
        private static final String XML_TYPE = "http://www.w3.org/TR/REC-xml";
        protected LSResourceResolver fEntityResolver;

        public InputSource getExternalSubset(String str, String str2) throws SAXException, IOException {
            return null;
        }

        public ResolutionForwarder() {
        }

        public ResolutionForwarder(LSResourceResolver lSResourceResolver) {
            setEntityResolver(lSResourceResolver);
        }

        public void setEntityResolver(LSResourceResolver lSResourceResolver) {
            this.fEntityResolver = lSResourceResolver;
        }

        public LSResourceResolver getEntityResolver() {
            return this.fEntityResolver;
        }

        public InputSource resolveEntity(String str, String str2, String str3, String str4) throws SAXException, IOException {
            LSInput resolveResource;
            LSResourceResolver lSResourceResolver = this.fEntityResolver;
            if (lSResourceResolver == null || (resolveResource = lSResourceResolver.resolveResource("http://www.w3.org/TR/REC-xml", (String) null, str2, str4, str3)) == null) {
                return null;
            }
            String publicId = resolveResource.getPublicId();
            resolveResource.getSystemId();
            String baseURI = resolveResource.getBaseURI();
            Reader characterStream = resolveResource.getCharacterStream();
            InputStream byteStream = resolveResource.getByteStream();
            String stringData = resolveResource.getStringData();
            String encoding = resolveResource.getEncoding();
            InputSource inputSource = new InputSource();
            inputSource.setPublicId(publicId);
            if (baseURI != null) {
                str4 = resolveSystemId(str4, baseURI);
            }
            inputSource.setSystemId(str4);
            if (characterStream != null) {
                inputSource.setCharacterStream(characterStream);
            } else if (byteStream != null) {
                inputSource.setByteStream(byteStream);
            } else if (!(stringData == null || stringData.length() == 0)) {
                inputSource.setCharacterStream(new StringReader(stringData));
            }
            inputSource.setEncoding(encoding);
            return inputSource;
        }

        public InputSource resolveEntity(String str, String str2) throws SAXException, IOException {
            return resolveEntity(null, str, null, str2);
        }

        private String resolveSystemId(String str, String str2) {
            try {
                return XMLEntityManager.expandSystemId(str, str2, false);
            } catch (URI.MalformedURIException unused) {
                return str;
            }
        }
    }
}
