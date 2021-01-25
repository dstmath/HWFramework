package ohos.com.sun.org.apache.xerces.internal.jaxp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator;
import ohos.com.sun.org.apache.xerces.internal.jaxp.validation.XSGrammarPoolContainer;
import ohos.com.sun.org.apache.xerces.internal.util.SAXMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.util.Status;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;
import ohos.com.sun.org.apache.xerces.internal.xs.AttributePSVI;
import ohos.com.sun.org.apache.xerces.internal.xs.ElementPSVI;
import ohos.com.sun.org.apache.xerces.internal.xs.PSVIProvider;
import ohos.javax.xml.parsers.SAXParser;
import ohos.javax.xml.validation.Schema;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.HandlerBase;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.Parser;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;
import ohos.org.xml.sax.XMLReader;
import ohos.org.xml.sax.helpers.DefaultHandler;

public class SAXParserImpl extends SAXParser implements JAXPConstants, PSVIProvider {
    private static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";
    private static final String NAMESPACE_PREFIXES_FEATURE = "http://xml.org/sax/features/namespace-prefixes";
    private static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    private static final String VALIDATION_FEATURE = "http://xml.org/sax/features/validation";
    private static final String XINCLUDE_FEATURE = "http://apache.org/xml/features/xinclude";
    private static final String XMLSCHEMA_VALIDATION_FEATURE = "http://apache.org/xml/features/validation/schema";
    private static final String XML_SECURITY_PROPERTY_MANAGER = "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager";
    private final EntityResolver fInitEntityResolver;
    private final ErrorHandler fInitErrorHandler;
    private final ValidationManager fSchemaValidationManager;
    private final XMLComponent fSchemaValidator;
    private final XMLComponentManager fSchemaValidatorComponentManager;
    private final XMLSecurityManager fSecurityManager;
    private final XMLSecurityPropertyManager fSecurityPropertyMgr;
    private final UnparsedEntityHandler fUnparsedEntityHandler;
    private final Schema grammar;
    private String schemaLanguage;
    private final JAXPSAXParser xmlReader;

    SAXParserImpl(SAXParserFactoryImpl sAXParserFactoryImpl, Map<String, Boolean> map) throws SAXException {
        this(sAXParserFactoryImpl, map, false);
    }

    SAXParserImpl(SAXParserFactoryImpl sAXParserFactoryImpl, Map<String, Boolean> map, boolean z) throws SAXException {
        XMLComponent xMLComponent;
        Boolean bool;
        this.schemaLanguage = null;
        this.fSecurityManager = new XMLSecurityManager(z);
        this.fSecurityPropertyMgr = new XMLSecurityPropertyManager();
        this.xmlReader = new JAXPSAXParser(this, this.fSecurityPropertyMgr, this.fSecurityManager);
        this.xmlReader.setFeature0("http://xml.org/sax/features/namespaces", sAXParserFactoryImpl.isNamespaceAware());
        this.xmlReader.setFeature0(NAMESPACE_PREFIXES_FEATURE, !sAXParserFactoryImpl.isNamespaceAware());
        if (sAXParserFactoryImpl.isXIncludeAware()) {
            this.xmlReader.setFeature0(XINCLUDE_FEATURE, true);
        }
        this.xmlReader.setProperty0("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", this.fSecurityPropertyMgr);
        this.xmlReader.setProperty0("http://apache.org/xml/properties/security-manager", this.fSecurityManager);
        if (z && map != null && (bool = map.get(Constants.FEATURE_SECURE_PROCESSING)) != null && bool.booleanValue()) {
            this.fSecurityPropertyMgr.setValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD, XMLSecurityPropertyManager.State.FSP, "");
            this.fSecurityPropertyMgr.setValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_SCHEMA, XMLSecurityPropertyManager.State.FSP, "");
        }
        setFeatures(map);
        if (sAXParserFactoryImpl.isValidating()) {
            this.fInitErrorHandler = new DefaultValidationErrorHandler(this.xmlReader.getLocale());
            this.xmlReader.setErrorHandler(this.fInitErrorHandler);
        } else {
            this.fInitErrorHandler = this.xmlReader.getErrorHandler();
        }
        this.xmlReader.setFeature0(VALIDATION_FEATURE, sAXParserFactoryImpl.isValidating());
        this.grammar = sAXParserFactoryImpl.getSchema();
        if (this.grammar != null) {
            XMLParserConfiguration xMLParserConfiguration = this.xmlReader.getXMLParserConfiguration();
            Schema schema = this.grammar;
            if (schema instanceof XSGrammarPoolContainer) {
                xMLComponent = new XMLSchemaValidator();
                this.fSchemaValidationManager = new ValidationManager();
                this.fUnparsedEntityHandler = new UnparsedEntityHandler(this.fSchemaValidationManager);
                xMLParserConfiguration.setDTDHandler(this.fUnparsedEntityHandler);
                this.fUnparsedEntityHandler.setDTDHandler(this.xmlReader);
                this.xmlReader.setDTDSource(this.fUnparsedEntityHandler);
                this.fSchemaValidatorComponentManager = new SchemaValidatorConfiguration(xMLParserConfiguration, this.grammar, this.fSchemaValidationManager);
            } else {
                JAXPValidatorComponent jAXPValidatorComponent = new JAXPValidatorComponent(schema.newValidatorHandler());
                this.fSchemaValidationManager = null;
                this.fUnparsedEntityHandler = null;
                this.fSchemaValidatorComponentManager = xMLParserConfiguration;
                xMLComponent = jAXPValidatorComponent;
            }
            xMLParserConfiguration.addRecognizedFeatures(xMLComponent.getRecognizedFeatures());
            xMLParserConfiguration.addRecognizedProperties(xMLComponent.getRecognizedProperties());
            xMLParserConfiguration.setDocumentHandler((XMLDocumentHandler) xMLComponent);
            XMLDocumentSource xMLDocumentSource = (XMLDocumentSource) xMLComponent;
            xMLDocumentSource.setDocumentHandler(this.xmlReader);
            this.xmlReader.setDocumentSource(xMLDocumentSource);
            this.fSchemaValidator = xMLComponent;
        } else {
            this.fSchemaValidationManager = null;
            this.fUnparsedEntityHandler = null;
            this.fSchemaValidatorComponentManager = null;
            this.fSchemaValidator = null;
        }
        this.fInitEntityResolver = this.xmlReader.getEntityResolver();
    }

    private void setFeatures(Map<String, Boolean> map) throws SAXNotSupportedException, SAXNotRecognizedException {
        if (map != null) {
            for (Map.Entry<String, Boolean> entry : map.entrySet()) {
                this.xmlReader.setFeature0(entry.getKey(), entry.getValue().booleanValue());
            }
        }
    }

    public Parser getParser() throws SAXException {
        return this.xmlReader;
    }

    public XMLReader getXMLReader() {
        return this.xmlReader;
    }

    public boolean isNamespaceAware() {
        try {
            return this.xmlReader.getFeature("http://xml.org/sax/features/namespaces");
        } catch (SAXException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public boolean isValidating() {
        try {
            return this.xmlReader.getFeature(VALIDATION_FEATURE);
        } catch (SAXException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public boolean isXIncludeAware() {
        try {
            return this.xmlReader.getFeature(XINCLUDE_FEATURE);
        } catch (SAXException unused) {
            return false;
        }
    }

    public void setProperty(String str, Object obj) throws SAXNotRecognizedException, SAXNotSupportedException {
        this.xmlReader.setProperty(str, obj);
    }

    public Object getProperty(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        return this.xmlReader.getProperty(str);
    }

    public void parse(InputSource inputSource, DefaultHandler defaultHandler) throws SAXException, IOException {
        if (inputSource != null) {
            if (defaultHandler != null) {
                this.xmlReader.setContentHandler(defaultHandler);
                this.xmlReader.setEntityResolver(defaultHandler);
                this.xmlReader.setErrorHandler(defaultHandler);
                this.xmlReader.setDTDHandler(defaultHandler);
                this.xmlReader.setDocumentHandler(null);
            }
            this.xmlReader.parse(inputSource);
            return;
        }
        throw new IllegalArgumentException();
    }

    public void parse(InputSource inputSource, HandlerBase handlerBase) throws SAXException, IOException {
        if (inputSource != null) {
            if (handlerBase != null) {
                this.xmlReader.setDocumentHandler(handlerBase);
                this.xmlReader.setEntityResolver(handlerBase);
                this.xmlReader.setErrorHandler(handlerBase);
                this.xmlReader.setDTDHandler(handlerBase);
                this.xmlReader.setContentHandler(null);
            }
            this.xmlReader.parse(inputSource);
            return;
        }
        throw new IllegalArgumentException();
    }

    public Schema getSchema() {
        return this.grammar;
    }

    public void reset() {
        try {
            this.xmlReader.restoreInitState();
        } catch (SAXException unused) {
        }
        this.xmlReader.setContentHandler(null);
        this.xmlReader.setDTDHandler(null);
        ErrorHandler errorHandler = this.xmlReader.getErrorHandler();
        ErrorHandler errorHandler2 = this.fInitErrorHandler;
        if (errorHandler != errorHandler2) {
            this.xmlReader.setErrorHandler(errorHandler2);
        }
        EntityResolver entityResolver = this.xmlReader.getEntityResolver();
        EntityResolver entityResolver2 = this.fInitEntityResolver;
        if (entityResolver != entityResolver2) {
            this.xmlReader.setEntityResolver(entityResolver2);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.PSVIProvider
    public ElementPSVI getElementPSVI() {
        return this.xmlReader.getElementPSVI();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.PSVIProvider
    public AttributePSVI getAttributePSVI(int i) {
        return this.xmlReader.getAttributePSVI(i);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.PSVIProvider
    public AttributePSVI getAttributePSVIByName(String str, String str2) {
        return this.xmlReader.getAttributePSVIByName(str, str2);
    }

    public static class JAXPSAXParser extends ohos.com.sun.org.apache.xerces.internal.parsers.SAXParser {
        private final HashMap fInitFeatures;
        private final HashMap fInitProperties;
        private final SAXParserImpl fSAXParser;
        private XMLSecurityManager fSecurityManager;
        private XMLSecurityPropertyManager fSecurityPropertyMgr;

        public JAXPSAXParser() {
            this(null, null, null);
        }

        JAXPSAXParser(SAXParserImpl sAXParserImpl, XMLSecurityPropertyManager xMLSecurityPropertyManager, XMLSecurityManager xMLSecurityManager) {
            this.fInitFeatures = new HashMap();
            this.fInitProperties = new HashMap();
            this.fSAXParser = sAXParserImpl;
            this.fSecurityManager = xMLSecurityManager;
            this.fSecurityPropertyMgr = xMLSecurityPropertyManager;
            if (this.fSecurityManager == null) {
                this.fSecurityManager = new XMLSecurityManager(true);
                try {
                    super.setProperty("http://apache.org/xml/properties/security-manager", this.fSecurityManager);
                } catch (SAXException e) {
                    throw new UnsupportedOperationException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "property-not-recognized", new Object[]{"http://apache.org/xml/properties/security-manager"}), e);
                }
            }
            if (this.fSecurityPropertyMgr == null) {
                this.fSecurityPropertyMgr = new XMLSecurityPropertyManager();
                try {
                    super.setProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", this.fSecurityPropertyMgr);
                } catch (SAXException e2) {
                    throw new UnsupportedOperationException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "property-not-recognized", new Object[]{"http://apache.org/xml/properties/security-manager"}), e2);
                }
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser
        public synchronized void setFeature(String str, boolean z) throws SAXNotRecognizedException, SAXNotSupportedException {
            if (str == null) {
                throw new NullPointerException();
            } else if (str.equals(Constants.FEATURE_SECURE_PROCESSING)) {
                try {
                    this.fSecurityManager.setSecureProcessing(z);
                    setProperty("http://apache.org/xml/properties/security-manager", this.fSecurityManager);
                } catch (SAXNotRecognizedException e) {
                    if (z) {
                        throw e;
                    }
                } catch (SAXNotSupportedException e2) {
                    if (z) {
                        throw e2;
                    }
                }
            } else {
                if (!this.fInitFeatures.containsKey(str)) {
                    this.fInitFeatures.put(str, super.getFeature(str) ? Boolean.TRUE : Boolean.FALSE);
                }
                if (!(this.fSAXParser == null || this.fSAXParser.fSchemaValidator == null)) {
                    setSchemaValidatorFeature(str, z);
                }
                super.setFeature(str, z);
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser, ohos.com.sun.org.apache.xerces.internal.parsers.XMLParser
        public synchronized boolean getFeature(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
            if (str == null) {
                throw new NullPointerException();
            } else if (str.equals(Constants.FEATURE_SECURE_PROCESSING)) {
                return this.fSecurityManager.isSecureProcessing();
            } else {
                return super.getFeature(str);
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.parsers.SAXParser, ohos.com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser
        public synchronized void setProperty(String str, Object obj) throws SAXNotRecognizedException, SAXNotSupportedException {
            if (str != null) {
                if (this.fSAXParser != null) {
                    if (JAXPConstants.JAXP_SCHEMA_LANGUAGE.equals(str)) {
                        if (this.fSAXParser.grammar == null) {
                            if ("http://www.w3.org/2001/XMLSchema".equals(obj)) {
                                if (this.fSAXParser.isValidating()) {
                                    this.fSAXParser.schemaLanguage = "http://www.w3.org/2001/XMLSchema";
                                    setFeature(SAXParserImpl.XMLSCHEMA_VALIDATION_FEATURE, true);
                                    if (!this.fInitProperties.containsKey(JAXPConstants.JAXP_SCHEMA_LANGUAGE)) {
                                        this.fInitProperties.put(JAXPConstants.JAXP_SCHEMA_LANGUAGE, super.getProperty(JAXPConstants.JAXP_SCHEMA_LANGUAGE));
                                    }
                                    super.setProperty(JAXPConstants.JAXP_SCHEMA_LANGUAGE, "http://www.w3.org/2001/XMLSchema");
                                }
                            } else if (obj == null) {
                                this.fSAXParser.schemaLanguage = null;
                                setFeature(SAXParserImpl.XMLSCHEMA_VALIDATION_FEATURE, false);
                            } else {
                                throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "schema-not-supported", null));
                            }
                            return;
                        }
                        throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "schema-already-specified", new Object[]{str}));
                    } else if (JAXPConstants.JAXP_SCHEMA_SOURCE.equals(str)) {
                        if (this.fSAXParser.grammar == null) {
                            String str2 = (String) getProperty(JAXPConstants.JAXP_SCHEMA_LANGUAGE);
                            if (str2 == null || !"http://www.w3.org/2001/XMLSchema".equals(str2)) {
                                throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "jaxp-order-not-supported", new Object[]{JAXPConstants.JAXP_SCHEMA_LANGUAGE, JAXPConstants.JAXP_SCHEMA_SOURCE}));
                            }
                            if (!this.fInitProperties.containsKey(JAXPConstants.JAXP_SCHEMA_SOURCE)) {
                                this.fInitProperties.put(JAXPConstants.JAXP_SCHEMA_SOURCE, super.getProperty(JAXPConstants.JAXP_SCHEMA_SOURCE));
                            }
                            super.setProperty(str, obj);
                            return;
                        }
                        throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "schema-already-specified", new Object[]{str}));
                    }
                }
                if (!(this.fSAXParser == null || this.fSAXParser.fSchemaValidator == null)) {
                    setSchemaValidatorProperty(str, obj);
                }
                if ((this.fSecurityManager == null || !this.fSecurityManager.setLimit(str, XMLSecurityManager.State.APIPROPERTY, obj)) && (this.fSecurityPropertyMgr == null || !this.fSecurityPropertyMgr.setValue(str, XMLSecurityPropertyManager.State.APIPROPERTY, obj))) {
                    if (!this.fInitProperties.containsKey(str)) {
                        this.fInitProperties.put(str, super.getProperty(str));
                    }
                    super.setProperty(str, obj);
                }
                return;
            }
            throw new NullPointerException();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser
        public synchronized Object getProperty(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
            if (str == null) {
                throw new NullPointerException();
            } else if (this.fSAXParser == null || !JAXPConstants.JAXP_SCHEMA_LANGUAGE.equals(str)) {
                String str2 = null;
                String limitAsString = this.fSecurityManager != null ? this.fSecurityManager.getLimitAsString(str) : null;
                if (limitAsString != null) {
                    return limitAsString;
                }
                if (this.fSecurityPropertyMgr != null) {
                    str2 = this.fSecurityPropertyMgr.getValue(str);
                }
                if (str2 != null) {
                    return str2;
                }
                return super.getProperty(str);
            } else {
                return this.fSAXParser.schemaLanguage;
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void restoreInitState() throws SAXNotRecognizedException, SAXNotSupportedException {
            if (!this.fInitFeatures.isEmpty()) {
                for (Map.Entry entry : this.fInitFeatures.entrySet()) {
                    super.setFeature((String) entry.getKey(), ((Boolean) entry.getValue()).booleanValue());
                }
                this.fInitFeatures.clear();
            }
            if (!this.fInitProperties.isEmpty()) {
                for (Map.Entry entry2 : this.fInitProperties.entrySet()) {
                    super.setProperty((String) entry2.getKey(), entry2.getValue());
                }
                this.fInitProperties.clear();
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser
        public void parse(InputSource inputSource) throws SAXException, IOException {
            SAXParserImpl sAXParserImpl = this.fSAXParser;
            if (!(sAXParserImpl == null || sAXParserImpl.fSchemaValidator == null)) {
                if (this.fSAXParser.fSchemaValidationManager != null) {
                    this.fSAXParser.fSchemaValidationManager.reset();
                    this.fSAXParser.fUnparsedEntityHandler.reset();
                }
                resetSchemaValidator();
            }
            super.parse(inputSource);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser
        public void parse(String str) throws SAXException, IOException {
            SAXParserImpl sAXParserImpl = this.fSAXParser;
            if (!(sAXParserImpl == null || sAXParserImpl.fSchemaValidator == null)) {
                if (this.fSAXParser.fSchemaValidationManager != null) {
                    this.fSAXParser.fSchemaValidationManager.reset();
                    this.fSAXParser.fUnparsedEntityHandler.reset();
                }
                resetSchemaValidator();
            }
            super.parse(str);
        }

        /* access modifiers changed from: package-private */
        public XMLParserConfiguration getXMLParserConfiguration() {
            return this.fConfiguration;
        }

        /* access modifiers changed from: package-private */
        public void setFeature0(String str, boolean z) throws SAXNotRecognizedException, SAXNotSupportedException {
            super.setFeature(str, z);
        }

        /* access modifiers changed from: package-private */
        public boolean getFeature0(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
            return super.getFeature(str);
        }

        /* access modifiers changed from: package-private */
        public void setProperty0(String str, Object obj) throws SAXNotRecognizedException, SAXNotSupportedException {
            super.setProperty(str, obj);
        }

        /* access modifiers changed from: package-private */
        public Object getProperty0(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
            return super.getProperty(str);
        }

        /* access modifiers changed from: package-private */
        public Locale getLocale() {
            return this.fConfiguration.getLocale();
        }

        private void setSchemaValidatorFeature(String str, boolean z) throws SAXNotRecognizedException, SAXNotSupportedException {
            try {
                this.fSAXParser.fSchemaValidator.setFeature(str, z);
            } catch (XMLConfigurationException e) {
                String identifier = e.getIdentifier();
                if (e.getType() == Status.NOT_RECOGNIZED) {
                    throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "feature-not-recognized", new Object[]{identifier}));
                }
                throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "feature-not-supported", new Object[]{identifier}));
            }
        }

        private void setSchemaValidatorProperty(String str, Object obj) throws SAXNotRecognizedException, SAXNotSupportedException {
            try {
                this.fSAXParser.fSchemaValidator.setProperty(str, obj);
            } catch (XMLConfigurationException e) {
                String identifier = e.getIdentifier();
                if (e.getType() == Status.NOT_RECOGNIZED) {
                    throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "property-not-recognized", new Object[]{identifier}));
                }
                throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "property-not-supported", new Object[]{identifier}));
            }
        }

        private void resetSchemaValidator() throws SAXException {
            try {
                this.fSAXParser.fSchemaValidator.reset(this.fSAXParser.fSchemaValidatorComponentManager);
            } catch (XMLConfigurationException e) {
                throw new SAXException(e);
            }
        }
    }
}
