package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.util.DOMEntityResolverWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.ErrorHandlerWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.FeatureState;
import ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings;
import ohos.com.sun.org.apache.xerces.internal.util.PropertyState;
import ohos.com.sun.org.apache.xerces.internal.util.Status;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.org.w3c.dom.ls.LSResourceResolver;
import ohos.org.xml.sax.ErrorHandler;

/* access modifiers changed from: package-private */
public final class XMLSchemaValidatorComponentManager extends ParserConfigurationSettings implements XMLComponentManager {
    private static final String ENTITY_MANAGER = "http://apache.org/xml/properties/internal/entity-manager";
    private static final String ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
    private static final String ERROR_HANDLER = "http://apache.org/xml/properties/internal/error-handler";
    private static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    private static final String LOCALE = "http://apache.org/xml/properties/locale";
    private static final String NAMESPACE_CONTEXT = "http://apache.org/xml/properties/internal/namespace-context";
    private static final String SCHEMA_ELEMENT_DEFAULT = "http://apache.org/xml/features/validation/schema/element-default";
    private static final String SCHEMA_VALIDATION = "http://apache.org/xml/features/validation/schema";
    private static final String SCHEMA_VALIDATOR = "http://apache.org/xml/properties/internal/validator/schema";
    private static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    private static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    private static final String USE_GRAMMAR_POOL_ONLY = "http://apache.org/xml/features/internal/validation/schema/use-grammar-pool-only";
    private static final String VALIDATION = "http://xml.org/sax/features/validation";
    private static final String VALIDATION_MANAGER = "http://apache.org/xml/properties/internal/validation-manager";
    private static final String XMLGRAMMAR_POOL = "http://apache.org/xml/properties/internal/grammar-pool";
    private static final String XML_SECURITY_PROPERTY_MANAGER = "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager";
    private boolean _isSecureMode = false;
    private final HashMap fComponents;
    private boolean fConfigUpdated;
    private XMLEntityManager fEntityManager;
    private ErrorHandler fErrorHandler;
    private XMLErrorReporter fErrorReporter;
    private final HashMap fInitFeatures;
    private final HashMap fInitProperties;
    private XMLSecurityManager fInitSecurityManager;
    private Locale fLocale;
    private NamespaceContext fNamespaceContext;
    private LSResourceResolver fResourceResolver;
    private XMLSchemaValidator fSchemaValidator;
    private final XMLSecurityPropertyManager fSecurityPropertyMgr;
    private boolean fUseGrammarPoolOnly;
    private ValidationManager fValidationManager;

    public XMLSchemaValidatorComponentManager(XSGrammarPoolContainer xSGrammarPoolContainer) {
        boolean z = true;
        this.fConfigUpdated = true;
        this.fComponents = new HashMap();
        this.fInitFeatures = new HashMap();
        this.fInitProperties = new HashMap();
        this.fErrorHandler = null;
        this.fResourceResolver = null;
        this.fLocale = null;
        this.fEntityManager = new XMLEntityManager();
        this.fComponents.put(ENTITY_MANAGER, this.fEntityManager);
        this.fErrorReporter = new XMLErrorReporter();
        this.fComponents.put("http://apache.org/xml/properties/internal/error-reporter", this.fErrorReporter);
        this.fNamespaceContext = new NamespaceSupport();
        this.fComponents.put(NAMESPACE_CONTEXT, this.fNamespaceContext);
        this.fSchemaValidator = new XMLSchemaValidator();
        this.fComponents.put(SCHEMA_VALIDATOR, this.fSchemaValidator);
        this.fValidationManager = new ValidationManager();
        this.fComponents.put(VALIDATION_MANAGER, this.fValidationManager);
        this.fComponents.put("http://apache.org/xml/properties/internal/entity-resolver", null);
        this.fComponents.put(ERROR_HANDLER, null);
        this.fComponents.put("http://apache.org/xml/properties/internal/symbol-table", new SymbolTable());
        this.fComponents.put("http://apache.org/xml/properties/internal/grammar-pool", xSGrammarPoolContainer.getGrammarPool());
        this.fUseGrammarPoolOnly = xSGrammarPoolContainer.isFullyComposed();
        this.fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, new XSMessageFormatter());
        addRecognizedParamsAndSetDefaults(this.fEntityManager, xSGrammarPoolContainer);
        addRecognizedParamsAndSetDefaults(this.fErrorReporter, xSGrammarPoolContainer);
        addRecognizedParamsAndSetDefaults(this.fSchemaValidator, xSGrammarPoolContainer);
        boolean booleanValue = xSGrammarPoolContainer.getFeature(Constants.FEATURE_SECURE_PROCESSING).booleanValue();
        if (System.getSecurityManager() != null) {
            this._isSecureMode = true;
        } else {
            z = booleanValue;
        }
        this.fInitSecurityManager = (XMLSecurityManager) xSGrammarPoolContainer.getProperty("http://apache.org/xml/properties/security-manager");
        XMLSecurityManager xMLSecurityManager = this.fInitSecurityManager;
        if (xMLSecurityManager != null) {
            xMLSecurityManager.setSecureProcessing(z);
        } else {
            this.fInitSecurityManager = new XMLSecurityManager(z);
        }
        setProperty("http://apache.org/xml/properties/security-manager", this.fInitSecurityManager);
        this.fSecurityPropertyMgr = (XMLSecurityPropertyManager) xSGrammarPoolContainer.getProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager");
        setProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", this.fSecurityPropertyMgr);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager
    public FeatureState getFeatureState(String str) throws XMLConfigurationException {
        if ("http://apache.org/xml/features/internal/parser-settings".equals(str)) {
            return FeatureState.is(this.fConfigUpdated);
        }
        if (VALIDATION.equals(str) || SCHEMA_VALIDATION.equals(str)) {
            return FeatureState.is(true);
        }
        if (USE_GRAMMAR_POOL_ONLY.equals(str)) {
            return FeatureState.is(this.fUseGrammarPoolOnly);
        }
        if (Constants.FEATURE_SECURE_PROCESSING.equals(str)) {
            return FeatureState.is(this.fInitSecurityManager.isSecureProcessing());
        }
        if (SCHEMA_ELEMENT_DEFAULT.equals(str)) {
            return FeatureState.is(true);
        }
        return super.getFeatureState(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setFeature(String str, boolean z) throws XMLConfigurationException {
        if ("http://apache.org/xml/features/internal/parser-settings".equals(str)) {
            throw new XMLConfigurationException(Status.NOT_SUPPORTED, str);
        } else if (!z && (VALIDATION.equals(str) || SCHEMA_VALIDATION.equals(str))) {
            throw new XMLConfigurationException(Status.NOT_SUPPORTED, str);
        } else if (USE_GRAMMAR_POOL_ONLY.equals(str) && z != this.fUseGrammarPoolOnly) {
            throw new XMLConfigurationException(Status.NOT_SUPPORTED, str);
        } else if (!Constants.FEATURE_SECURE_PROCESSING.equals(str)) {
            this.fConfigUpdated = true;
            this.fEntityManager.setFeature(str, z);
            this.fErrorReporter.setFeature(str, z);
            this.fSchemaValidator.setFeature(str, z);
            if (!this.fInitFeatures.containsKey(str)) {
                this.fInitFeatures.put(str, super.getFeature(str) ? Boolean.TRUE : Boolean.FALSE);
            }
            super.setFeature(str, z);
        } else if (!this._isSecureMode || z) {
            this.fInitSecurityManager.setSecureProcessing(z);
            setProperty("http://apache.org/xml/properties/security-manager", this.fInitSecurityManager);
            if (z) {
                this.fSecurityPropertyMgr.setValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD, XMLSecurityPropertyManager.State.FSP, "");
                this.fSecurityPropertyMgr.setValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_SCHEMA, XMLSecurityPropertyManager.State.FSP, "");
                setProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", this.fSecurityPropertyMgr);
            }
        } else {
            throw new XMLConfigurationException(Status.NOT_ALLOWED, Constants.FEATURE_SECURE_PROCESSING);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager
    public PropertyState getPropertyState(String str) throws XMLConfigurationException {
        if ("http://apache.org/xml/properties/locale".equals(str)) {
            return PropertyState.is(getLocale());
        }
        Object obj = this.fComponents.get(str);
        if (obj != null) {
            return PropertyState.is(obj);
        }
        if (this.fComponents.containsKey(str)) {
            return PropertyState.is(null);
        }
        return super.getPropertyState(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setProperty(String str, Object obj) throws XMLConfigurationException {
        if (ENTITY_MANAGER.equals(str) || "http://apache.org/xml/properties/internal/error-reporter".equals(str) || NAMESPACE_CONTEXT.equals(str) || SCHEMA_VALIDATOR.equals(str) || "http://apache.org/xml/properties/internal/symbol-table".equals(str) || VALIDATION_MANAGER.equals(str) || "http://apache.org/xml/properties/internal/grammar-pool".equals(str)) {
            throw new XMLConfigurationException(Status.NOT_SUPPORTED, str);
        }
        this.fConfigUpdated = true;
        this.fEntityManager.setProperty(str, obj);
        this.fErrorReporter.setProperty(str, obj);
        this.fSchemaValidator.setProperty(str, obj);
        if ("http://apache.org/xml/properties/internal/entity-resolver".equals(str) || ERROR_HANDLER.equals(str) || "http://apache.org/xml/properties/security-manager".equals(str)) {
            this.fComponents.put(str, obj);
        } else if ("http://apache.org/xml/properties/locale".equals(str)) {
            setLocale((Locale) obj);
            this.fComponents.put(str, obj);
        } else {
            XMLSecurityManager xMLSecurityManager = this.fInitSecurityManager;
            if (xMLSecurityManager == null || !xMLSecurityManager.setLimit(str, XMLSecurityManager.State.APIPROPERTY, obj)) {
                XMLSecurityPropertyManager xMLSecurityPropertyManager = this.fSecurityPropertyMgr;
                if (xMLSecurityPropertyManager == null || !xMLSecurityPropertyManager.setValue(str, XMLSecurityPropertyManager.State.APIPROPERTY, obj)) {
                    if (!this.fInitProperties.containsKey(str)) {
                        this.fInitProperties.put(str, super.getProperty(str));
                    }
                    super.setProperty(str, obj);
                }
            }
        }
    }

    public void addRecognizedParamsAndSetDefaults(XMLComponent xMLComponent, XSGrammarPoolContainer xSGrammarPoolContainer) {
        String[] recognizedFeatures = xMLComponent.getRecognizedFeatures();
        addRecognizedFeatures(recognizedFeatures);
        String[] recognizedProperties = xMLComponent.getRecognizedProperties();
        addRecognizedProperties(recognizedProperties);
        setFeatureDefaults(xMLComponent, recognizedFeatures, xSGrammarPoolContainer);
        setPropertyDefaults(xMLComponent, recognizedProperties);
    }

    public void reset() throws XNIException {
        this.fNamespaceContext.reset();
        this.fValidationManager.reset();
        this.fEntityManager.reset(this);
        this.fErrorReporter.reset(this);
        this.fSchemaValidator.reset(this);
        this.fConfigUpdated = false;
    }

    /* access modifiers changed from: package-private */
    public void setErrorHandler(ErrorHandler errorHandler) {
        ErrorHandlerWrapper errorHandlerWrapper;
        this.fErrorHandler = errorHandler;
        if (errorHandler != null) {
            errorHandlerWrapper = new ErrorHandlerWrapper(errorHandler);
        } else {
            errorHandlerWrapper = new ErrorHandlerWrapper(DraconianErrorHandler.getInstance());
        }
        setProperty(ERROR_HANDLER, errorHandlerWrapper);
    }

    /* access modifiers changed from: package-private */
    public ErrorHandler getErrorHandler() {
        return this.fErrorHandler;
    }

    /* access modifiers changed from: package-private */
    public void setResourceResolver(LSResourceResolver lSResourceResolver) {
        this.fResourceResolver = lSResourceResolver;
        setProperty("http://apache.org/xml/properties/internal/entity-resolver", new DOMEntityResolverWrapper(lSResourceResolver));
    }

    /* access modifiers changed from: package-private */
    public LSResourceResolver getResourceResolver() {
        return this.fResourceResolver;
    }

    /* access modifiers changed from: package-private */
    public void setLocale(Locale locale) {
        this.fLocale = locale;
        this.fErrorReporter.setLocale(locale);
    }

    /* access modifiers changed from: package-private */
    public Locale getLocale() {
        return this.fLocale;
    }

    /* access modifiers changed from: package-private */
    public void restoreInitialState() {
        this.fConfigUpdated = true;
        this.fComponents.put("http://apache.org/xml/properties/internal/entity-resolver", null);
        this.fComponents.put(ERROR_HANDLER, null);
        setLocale(null);
        this.fComponents.put("http://apache.org/xml/properties/locale", null);
        this.fComponents.put("http://apache.org/xml/properties/security-manager", this.fInitSecurityManager);
        setLocale(null);
        this.fComponents.put("http://apache.org/xml/properties/locale", null);
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

    private void setFeatureDefaults(XMLComponent xMLComponent, String[] strArr, XSGrammarPoolContainer xSGrammarPoolContainer) {
        if (strArr != null) {
            for (String str : strArr) {
                Boolean feature = xSGrammarPoolContainer.getFeature(str);
                if (feature == null) {
                    feature = xMLComponent.getFeatureDefault(str);
                }
                if (feature != null && !this.fFeatures.containsKey(str)) {
                    this.fFeatures.put(str, feature);
                    this.fConfigUpdated = true;
                }
            }
        }
    }

    private void setPropertyDefaults(XMLComponent xMLComponent, String[] strArr) {
        if (strArr != null) {
            for (String str : strArr) {
                Object propertyDefault = xMLComponent.getPropertyDefault(str);
                if (propertyDefault != null && !this.fProperties.containsKey(str)) {
                    this.fProperties.put(str, propertyDefault);
                    this.fConfigUpdated = true;
                }
            }
        }
    }
}
