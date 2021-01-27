package ohos.com.sun.org.apache.xerces.internal.jaxp;

import java.io.IOException;
import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMImplementationImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator;
import ohos.com.sun.org.apache.xerces.internal.jaxp.validation.XSGrammarPoolContainer;
import ohos.com.sun.org.apache.xerces.internal.parsers.DOMParser;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;
import ohos.javax.xml.parsers.DocumentBuilder;
import ohos.javax.xml.validation.Schema;
import ohos.org.w3c.dom.DOMImplementation;
import ohos.org.w3c.dom.Document;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;

public class DocumentBuilderImpl extends DocumentBuilder implements JAXPConstants {
    public static final String ACCESS_EXTERNAL_DTD = "http://ohos.javax.xml.XMLConstants/property/accessExternalDTD";
    public static final String ACCESS_EXTERNAL_SCHEMA = "http://ohos.javax.xml.XMLConstants/property/accessExternalSchema";
    private static final String CREATE_CDATA_NODES_FEATURE = "http://apache.org/xml/features/create-cdata-nodes";
    private static final String CREATE_ENTITY_REF_NODES_FEATURE = "http://apache.org/xml/features/dom/create-entity-ref-nodes";
    private static final String INCLUDE_COMMENTS_FEATURE = "http://apache.org/xml/features/include-comments";
    private static final String INCLUDE_IGNORABLE_WHITESPACE = "http://apache.org/xml/features/dom/include-ignorable-whitespace";
    private static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";
    private static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    private static final String VALIDATION_FEATURE = "http://xml.org/sax/features/validation";
    private static final String XINCLUDE_FEATURE = "http://apache.org/xml/features/xinclude";
    private static final String XMLSCHEMA_VALIDATION_FEATURE = "http://apache.org/xml/features/validation/schema";
    private static final String XML_SECURITY_PROPERTY_MANAGER = "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager";
    private final DOMParser domParser;
    private final EntityResolver fInitEntityResolver;
    private final ErrorHandler fInitErrorHandler;
    private final ValidationManager fSchemaValidationManager;
    private final XMLComponent fSchemaValidator;
    private final XMLComponentManager fSchemaValidatorComponentManager;
    private XMLSecurityManager fSecurityManager;
    private XMLSecurityPropertyManager fSecurityPropertyMgr;
    private final UnparsedEntityHandler fUnparsedEntityHandler;
    private final Schema grammar;

    DocumentBuilderImpl(DocumentBuilderFactoryImpl documentBuilderFactoryImpl, Map<String, Object> map, Map<String, Boolean> map2) throws SAXNotRecognizedException, SAXNotSupportedException {
        this(documentBuilderFactoryImpl, map, map2, false);
    }

    DocumentBuilderImpl(DocumentBuilderFactoryImpl documentBuilderFactoryImpl, Map<String, Object> map, Map<String, Boolean> map2, boolean z) throws SAXNotRecognizedException, SAXNotSupportedException {
        XMLComponent xMLComponent;
        Boolean bool;
        this.domParser = new DOMParser();
        if (documentBuilderFactoryImpl.isValidating()) {
            this.fInitErrorHandler = new DefaultValidationErrorHandler(this.domParser.getXMLParserConfiguration().getLocale());
            setErrorHandler(this.fInitErrorHandler);
        } else {
            this.fInitErrorHandler = this.domParser.getErrorHandler();
        }
        this.domParser.setFeature(VALIDATION_FEATURE, documentBuilderFactoryImpl.isValidating());
        this.domParser.setFeature("http://xml.org/sax/features/namespaces", documentBuilderFactoryImpl.isNamespaceAware());
        this.domParser.setFeature(INCLUDE_IGNORABLE_WHITESPACE, !documentBuilderFactoryImpl.isIgnoringElementContentWhitespace());
        this.domParser.setFeature(CREATE_ENTITY_REF_NODES_FEATURE, !documentBuilderFactoryImpl.isExpandEntityReferences());
        this.domParser.setFeature(INCLUDE_COMMENTS_FEATURE, !documentBuilderFactoryImpl.isIgnoringComments());
        this.domParser.setFeature(CREATE_CDATA_NODES_FEATURE, !documentBuilderFactoryImpl.isCoalescing());
        if (documentBuilderFactoryImpl.isXIncludeAware()) {
            this.domParser.setFeature(XINCLUDE_FEATURE, true);
        }
        this.fSecurityPropertyMgr = new XMLSecurityPropertyManager();
        this.domParser.setProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", this.fSecurityPropertyMgr);
        this.fSecurityManager = new XMLSecurityManager(z);
        this.domParser.setProperty("http://apache.org/xml/properties/security-manager", this.fSecurityManager);
        if (z && map2 != null && (bool = map2.get(Constants.FEATURE_SECURE_PROCESSING)) != null && bool.booleanValue()) {
            this.fSecurityPropertyMgr.setValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD, XMLSecurityPropertyManager.State.FSP, "");
            this.fSecurityPropertyMgr.setValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_SCHEMA, XMLSecurityPropertyManager.State.FSP, "");
        }
        this.grammar = documentBuilderFactoryImpl.getSchema();
        if (this.grammar != null) {
            XMLParserConfiguration xMLParserConfiguration = this.domParser.getXMLParserConfiguration();
            Schema schema = this.grammar;
            if (schema instanceof XSGrammarPoolContainer) {
                xMLComponent = new XMLSchemaValidator();
                this.fSchemaValidationManager = new ValidationManager();
                this.fUnparsedEntityHandler = new UnparsedEntityHandler(this.fSchemaValidationManager);
                xMLParserConfiguration.setDTDHandler(this.fUnparsedEntityHandler);
                this.fUnparsedEntityHandler.setDTDHandler(this.domParser);
                this.domParser.setDTDSource(this.fUnparsedEntityHandler);
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
            setFeatures(map2);
            xMLParserConfiguration.setDocumentHandler((XMLDocumentHandler) xMLComponent);
            XMLDocumentSource xMLDocumentSource = (XMLDocumentSource) xMLComponent;
            xMLDocumentSource.setDocumentHandler(this.domParser);
            this.domParser.setDocumentSource(xMLDocumentSource);
            this.fSchemaValidator = xMLComponent;
        } else {
            this.fSchemaValidationManager = null;
            this.fUnparsedEntityHandler = null;
            this.fSchemaValidatorComponentManager = null;
            this.fSchemaValidator = null;
            setFeatures(map2);
        }
        setDocumentBuilderFactoryAttributes(map);
        this.fInitEntityResolver = this.domParser.getEntityResolver();
    }

    private void setFeatures(Map<String, Boolean> map) throws SAXNotSupportedException, SAXNotRecognizedException {
        if (map != null) {
            for (Map.Entry<String, Boolean> entry : map.entrySet()) {
                this.domParser.setFeature(entry.getKey(), entry.getValue().booleanValue());
            }
        }
    }

    private void setDocumentBuilderFactoryAttributes(Map<String, Object> map) throws SAXNotSupportedException, SAXNotRecognizedException {
        XMLSecurityPropertyManager xMLSecurityPropertyManager;
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Boolean) {
                    this.domParser.setFeature(key, ((Boolean) value).booleanValue());
                } else if (JAXPConstants.JAXP_SCHEMA_LANGUAGE.equals(key)) {
                    if ("http://www.w3.org/2001/XMLSchema".equals(value) && isValidating()) {
                        this.domParser.setFeature(XMLSCHEMA_VALIDATION_FEATURE, true);
                        this.domParser.setProperty(JAXPConstants.JAXP_SCHEMA_LANGUAGE, "http://www.w3.org/2001/XMLSchema");
                    }
                } else if (!JAXPConstants.JAXP_SCHEMA_SOURCE.equals(key)) {
                    XMLSecurityManager xMLSecurityManager = this.fSecurityManager;
                    if ((xMLSecurityManager == null || !xMLSecurityManager.setLimit(key, XMLSecurityManager.State.APIPROPERTY, value)) && ((xMLSecurityPropertyManager = this.fSecurityPropertyMgr) == null || !xMLSecurityPropertyManager.setValue(key, XMLSecurityPropertyManager.State.APIPROPERTY, value))) {
                        this.domParser.setProperty(key, value);
                    }
                } else if (!isValidating()) {
                    continue;
                } else {
                    String str = (String) map.get(JAXPConstants.JAXP_SCHEMA_LANGUAGE);
                    if (str == null || !"http://www.w3.org/2001/XMLSchema".equals(str)) {
                        throw new IllegalArgumentException(DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "jaxp-order-not-supported", new Object[]{JAXPConstants.JAXP_SCHEMA_LANGUAGE, JAXPConstants.JAXP_SCHEMA_SOURCE}));
                    }
                    this.domParser.setProperty(key, value);
                }
            }
        }
    }

    public Document newDocument() {
        return new DocumentImpl();
    }

    public DOMImplementation getDOMImplementation() {
        return DOMImplementationImpl.getDOMImplementation();
    }

    public Document parse(InputSource inputSource) throws SAXException, IOException {
        if (inputSource != null) {
            if (this.fSchemaValidator != null) {
                ValidationManager validationManager = this.fSchemaValidationManager;
                if (validationManager != null) {
                    validationManager.reset();
                    this.fUnparsedEntityHandler.reset();
                }
                resetSchemaValidator();
            }
            this.domParser.parse(inputSource);
            Document document = this.domParser.getDocument();
            this.domParser.dropDocumentReferences();
            return document;
        }
        throw new IllegalArgumentException(DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "jaxp-null-input-source", null));
    }

    public boolean isNamespaceAware() {
        try {
            return this.domParser.getFeature("http://xml.org/sax/features/namespaces");
        } catch (SAXException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public boolean isValidating() {
        try {
            return this.domParser.getFeature(VALIDATION_FEATURE);
        } catch (SAXException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public boolean isXIncludeAware() {
        try {
            return this.domParser.getFeature(XINCLUDE_FEATURE);
        } catch (SAXException unused) {
            return false;
        }
    }

    public void setEntityResolver(EntityResolver entityResolver) {
        this.domParser.setEntityResolver(entityResolver);
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.domParser.setErrorHandler(errorHandler);
    }

    public Schema getSchema() {
        return this.grammar;
    }

    public void reset() {
        ErrorHandler errorHandler = this.domParser.getErrorHandler();
        ErrorHandler errorHandler2 = this.fInitErrorHandler;
        if (errorHandler != errorHandler2) {
            this.domParser.setErrorHandler(errorHandler2);
        }
        EntityResolver entityResolver = this.domParser.getEntityResolver();
        EntityResolver entityResolver2 = this.fInitEntityResolver;
        if (entityResolver != entityResolver2) {
            this.domParser.setEntityResolver(entityResolver2);
        }
    }

    /* access modifiers changed from: package-private */
    public DOMParser getDOMParser() {
        return this.domParser;
    }

    private void resetSchemaValidator() throws SAXException {
        try {
            this.fSchemaValidator.reset(this.fSchemaValidatorComponentManager);
        } catch (XMLConfigurationException e) {
            throw new SAXException(e);
        }
    }
}
