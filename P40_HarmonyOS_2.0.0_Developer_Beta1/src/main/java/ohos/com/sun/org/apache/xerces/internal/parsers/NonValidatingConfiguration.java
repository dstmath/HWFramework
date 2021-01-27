package ohos.com.sun.org.apache.xerces.internal.parsers;

import java.io.IOException;
import java.util.Locale;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLDTDScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLNSDocumentScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.DTDDVFactory;
import ohos.com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import ohos.com.sun.org.apache.xerces.internal.util.FeatureState;
import ohos.com.sun.org.apache.xerces.internal.util.PropertyState;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDScanner;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentScanner;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLPullParserConfiguration;
import ohos.jdk.xml.internal.JdkXmlUtils;

public class NonValidatingConfiguration extends BasicParserConfiguration implements XMLPullParserConfiguration {
    protected static final String ALLOW_JAVA_ENCODINGS = "http://apache.org/xml/features/allow-java-encodings";
    protected static final String CONTINUE_AFTER_FATAL_ERROR = "http://apache.org/xml/features/continue-after-fatal-error";
    protected static final String DATATYPE_VALIDATOR_FACTORY = "http://apache.org/xml/properties/internal/datatype-validator-factory";
    protected static final String DOCUMENT_SCANNER = "http://apache.org/xml/properties/internal/document-scanner";
    protected static final String DTD_SCANNER = "http://apache.org/xml/properties/internal/dtd-scanner";
    protected static final String DTD_VALIDATOR = "http://apache.org/xml/properties/internal/validator/dtd";
    protected static final String ENTITY_MANAGER = "http://apache.org/xml/properties/internal/entity-manager";
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    protected static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    protected static final String LOCALE = "http://apache.org/xml/properties/locale";
    protected static final String NAMESPACE_BINDER = "http://apache.org/xml/properties/internal/namespace-binder";
    protected static final String NORMALIZE_DATA = "http://apache.org/xml/features/validation/schema/normalized-value";
    protected static final String NOTIFY_BUILTIN_REFS = "http://apache.org/xml/features/scanner/notify-builtin-refs";
    protected static final String NOTIFY_CHAR_REFS = "http://apache.org/xml/features/scanner/notify-char-refs";
    private static final boolean PRINT_EXCEPTION_STACK_TRACE = false;
    protected static final String SCHEMA_ELEMENT_DEFAULT = "http://apache.org/xml/features/validation/schema/element-default";
    protected static final String SCHEMA_VALIDATOR = "http://apache.org/xml/properties/internal/validator/schema";
    private static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    protected static final String VALIDATION_MANAGER = "http://apache.org/xml/properties/internal/validation-manager";
    protected static final String WARN_ON_DUPLICATE_ATTDEF = "http://apache.org/xml/features/validation/warn-on-duplicate-attdef";
    protected static final String WARN_ON_DUPLICATE_ENTITYDEF = "http://apache.org/xml/features/warn-on-duplicate-entitydef";
    protected static final String WARN_ON_UNDECLARED_ELEMDEF = "http://apache.org/xml/features/validation/warn-on-undeclared-elemdef";
    protected static final String XMLGRAMMAR_POOL = "http://apache.org/xml/properties/internal/grammar-pool";
    protected static final String XML_SECURITY_PROPERTY_MANAGER = "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager";
    protected boolean fConfigUpdated;
    protected XMLDTDScanner fDTDScanner;
    protected DTDDVFactory fDatatypeValidatorFactory;
    protected XMLEntityManager fEntityManager;
    protected XMLErrorReporter fErrorReporter;
    protected XMLGrammarPool fGrammarPool;
    protected XMLInputSource fInputSource;
    protected XMLLocator fLocator;
    private XMLNSDocumentScannerImpl fNamespaceScanner;
    private XMLDocumentScannerImpl fNonNSScanner;
    protected boolean fParseInProgress;
    protected XMLDocumentScanner fScanner;
    protected ValidationManager fValidationManager;

    /* access modifiers changed from: protected */
    public XMLDocumentScanner createDocumentScanner() {
        return null;
    }

    public NonValidatingConfiguration() {
        this(null, null, null);
    }

    public NonValidatingConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    }

    public NonValidatingConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool) {
        this(symbolTable, xMLGrammarPool, null);
    }

    public NonValidatingConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool, XMLComponentManager xMLComponentManager) {
        super(symbolTable, xMLComponentManager);
        this.fConfigUpdated = false;
        this.fParseInProgress = false;
        addRecognizedFeatures(new String[]{"http://apache.org/xml/features/internal/parser-settings", "http://xml.org/sax/features/namespaces", CONTINUE_AFTER_FATAL_ERROR, "jdk.xml.overrideDefaultParser"});
        this.fFeatures.put(CONTINUE_AFTER_FATAL_ERROR, Boolean.FALSE);
        this.fFeatures.put("http://apache.org/xml/features/internal/parser-settings", Boolean.TRUE);
        this.fFeatures.put("http://xml.org/sax/features/namespaces", Boolean.TRUE);
        this.fFeatures.put("jdk.xml.overrideDefaultParser", Boolean.valueOf(JdkXmlUtils.OVERRIDE_PARSER_DEFAULT));
        addRecognizedProperties(new String[]{"http://apache.org/xml/properties/internal/error-reporter", ENTITY_MANAGER, DOCUMENT_SCANNER, DTD_SCANNER, DTD_VALIDATOR, NAMESPACE_BINDER, "http://apache.org/xml/properties/internal/grammar-pool", DATATYPE_VALIDATOR_FACTORY, VALIDATION_MANAGER, "http://apache.org/xml/properties/locale", "http://apache.org/xml/properties/security-manager", "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager"});
        this.fGrammarPool = xMLGrammarPool;
        if (this.fGrammarPool != null) {
            this.fProperties.put("http://apache.org/xml/properties/internal/grammar-pool", this.fGrammarPool);
        }
        this.fEntityManager = createEntityManager();
        this.fProperties.put(ENTITY_MANAGER, this.fEntityManager);
        addComponent(this.fEntityManager);
        this.fErrorReporter = createErrorReporter();
        this.fErrorReporter.setDocumentLocator(this.fEntityManager.getEntityScanner());
        this.fProperties.put("http://apache.org/xml/properties/internal/error-reporter", this.fErrorReporter);
        addComponent(this.fErrorReporter);
        this.fDTDScanner = createDTDScanner();
        if (this.fDTDScanner != null) {
            this.fProperties.put(DTD_SCANNER, this.fDTDScanner);
            XMLDTDScanner xMLDTDScanner = this.fDTDScanner;
            if (xMLDTDScanner instanceof XMLComponent) {
                addComponent((XMLComponent) xMLDTDScanner);
            }
        }
        this.fDatatypeValidatorFactory = createDatatypeValidatorFactory();
        if (this.fDatatypeValidatorFactory != null) {
            this.fProperties.put(DATATYPE_VALIDATOR_FACTORY, this.fDatatypeValidatorFactory);
        }
        this.fValidationManager = createValidationManager();
        if (this.fValidationManager != null) {
            this.fProperties.put(VALIDATION_MANAGER, this.fValidationManager);
        }
        if (this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210") == null) {
            XMLMessageFormatter xMLMessageFormatter = new XMLMessageFormatter();
            this.fErrorReporter.putMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210", xMLMessageFormatter);
            this.fErrorReporter.putMessageFormatter("http://www.w3.org/TR/1999/REC-xml-names-19990114", xMLMessageFormatter);
        }
        this.fConfigUpdated = false;
        try {
            setLocale(Locale.getDefault());
        } catch (XNIException unused) {
        }
        setProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", new XMLSecurityPropertyManager());
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.BasicParserConfiguration, ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setFeature(String str, boolean z) throws XMLConfigurationException {
        this.fConfigUpdated = true;
        super.setFeature(str, z);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager
    public PropertyState getPropertyState(String str) throws XMLConfigurationException {
        if ("http://apache.org/xml/properties/locale".equals(str)) {
            return PropertyState.is(getLocale());
        }
        return super.getPropertyState(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.BasicParserConfiguration, ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setProperty(String str, Object obj) throws XMLConfigurationException {
        this.fConfigUpdated = true;
        if ("http://apache.org/xml/properties/locale".equals(str)) {
            setLocale((Locale) obj);
        }
        super.setProperty(str, obj);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.BasicParserConfiguration, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setLocale(Locale locale) throws XNIException {
        super.setLocale(locale);
        this.fErrorReporter.setLocale(locale);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager
    public FeatureState getFeatureState(String str) throws XMLConfigurationException {
        if (str.equals("http://apache.org/xml/features/internal/parser-settings")) {
            return FeatureState.is(this.fConfigUpdated);
        }
        return super.getFeatureState(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLPullParserConfiguration
    public void setInputSource(XMLInputSource xMLInputSource) throws XMLConfigurationException, IOException {
        this.fInputSource = xMLInputSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLPullParserConfiguration
    public boolean parse(boolean z) throws XNIException, IOException {
        if (this.fInputSource != null) {
            try {
                reset();
                this.fScanner.setInputSource(this.fInputSource);
                this.fInputSource = null;
            } catch (XNIException e) {
                throw e;
            } catch (IOException e2) {
                throw e2;
            } catch (RuntimeException e3) {
                throw e3;
            } catch (Exception e4) {
                throw new XNIException(e4);
            }
        }
        try {
            return this.fScanner.scanDocument(z);
        } catch (XNIException e5) {
            throw e5;
        } catch (IOException e6) {
            throw e6;
        } catch (RuntimeException e7) {
            throw e7;
        } catch (Exception e8) {
            throw new XNIException(e8);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLPullParserConfiguration
    public void cleanup() {
        this.fEntityManager.closeReaders();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.BasicParserConfiguration, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void parse(XMLInputSource xMLInputSource) throws XNIException, IOException {
        if (!this.fParseInProgress) {
            this.fParseInProgress = true;
            try {
                setInputSource(xMLInputSource);
                parse(true);
                this.fParseInProgress = false;
                cleanup();
            } catch (XNIException e) {
                throw e;
            } catch (IOException e2) {
                throw e2;
            } catch (RuntimeException e3) {
                throw e3;
            } catch (Exception e4) {
                throw new XNIException(e4);
            } catch (Throwable th) {
                this.fParseInProgress = false;
                cleanup();
                throw th;
            }
        } else {
            throw new XNIException("FWK005 parse may not be called while parsing.");
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.BasicParserConfiguration
    public void reset() throws XNIException {
        ValidationManager validationManager = this.fValidationManager;
        if (validationManager != null) {
            validationManager.reset();
        }
        configurePipeline();
        super.reset();
    }

    /* access modifiers changed from: protected */
    public void configurePipeline() {
        if (this.fFeatures.get("http://xml.org/sax/features/namespaces") == Boolean.TRUE) {
            if (this.fNamespaceScanner == null) {
                this.fNamespaceScanner = new XMLNSDocumentScannerImpl();
                addComponent(this.fNamespaceScanner);
            }
            this.fProperties.put(DOCUMENT_SCANNER, this.fNamespaceScanner);
            this.fNamespaceScanner.setDTDValidator(null);
            this.fScanner = this.fNamespaceScanner;
        } else {
            if (this.fNonNSScanner == null) {
                this.fNonNSScanner = new XMLDocumentScannerImpl();
                addComponent(this.fNonNSScanner);
            }
            this.fProperties.put(DOCUMENT_SCANNER, this.fNonNSScanner);
            this.fScanner = this.fNonNSScanner;
        }
        this.fScanner.setDocumentHandler(this.fDocumentHandler);
        this.fLastComponent = this.fScanner;
        XMLDTDScanner xMLDTDScanner = this.fDTDScanner;
        if (xMLDTDScanner != null) {
            xMLDTDScanner.setDTDHandler(this.fDTDHandler);
            this.fDTDScanner.setDTDContentModelHandler(this.fDTDContentModelHandler);
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.BasicParserConfiguration, ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings
    public FeatureState checkFeature(String str) throws XMLConfigurationException {
        if (str.startsWith(Constants.XERCES_FEATURE_PREFIX)) {
            int length = str.length() - 31;
            if (length == 18 && str.endsWith(Constants.DYNAMIC_VALIDATION_FEATURE)) {
                return FeatureState.RECOGNIZED;
            }
            if (length == 35 && str.endsWith(Constants.DEFAULT_ATTRIBUTE_VALUES_FEATURE)) {
                return FeatureState.NOT_SUPPORTED;
            }
            if (length == 34 && str.endsWith(Constants.VALIDATE_CONTENT_MODELS_FEATURE)) {
                return FeatureState.NOT_SUPPORTED;
            }
            if (length == 30 && str.endsWith(Constants.LOAD_DTD_GRAMMAR_FEATURE)) {
                return FeatureState.RECOGNIZED;
            }
            if (length == 31 && str.endsWith(Constants.LOAD_EXTERNAL_DTD_FEATURE)) {
                return FeatureState.RECOGNIZED;
            }
            if (length == 29 && str.endsWith(Constants.VALIDATE_DATATYPES_FEATURE)) {
                return FeatureState.NOT_SUPPORTED;
            }
        }
        return super.checkFeature(str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.BasicParserConfiguration, ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings
    public PropertyState checkProperty(String str) throws XMLConfigurationException {
        if (str.startsWith(Constants.XERCES_PROPERTY_PREFIX) && str.length() - 33 == 20 && str.endsWith(Constants.DTD_SCANNER_PROPERTY)) {
            return PropertyState.RECOGNIZED;
        }
        if (!str.startsWith(Constants.JAXP_PROPERTY_PREFIX) || str.length() - 40 != 12 || !str.endsWith(Constants.SCHEMA_SOURCE)) {
            return super.checkProperty(str);
        }
        return PropertyState.RECOGNIZED;
    }

    /* access modifiers changed from: protected */
    public XMLEntityManager createEntityManager() {
        return new XMLEntityManager();
    }

    /* access modifiers changed from: protected */
    public XMLErrorReporter createErrorReporter() {
        return new XMLErrorReporter();
    }

    /* access modifiers changed from: protected */
    public XMLDTDScanner createDTDScanner() {
        return new XMLDTDScannerImpl();
    }

    /* access modifiers changed from: protected */
    public DTDDVFactory createDatatypeValidatorFactory() {
        return DTDDVFactory.getInstance();
    }

    /* access modifiers changed from: protected */
    public ValidationManager createValidationManager() {
        return new ValidationManager();
    }
}
