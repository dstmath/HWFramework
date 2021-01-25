package ohos.com.sun.org.apache.xerces.internal.parsers;

import java.io.IOException;
import java.util.Locale;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLDTDScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLNamespaceBinder;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDProcessor;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator;
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

public class DTDConfiguration extends BasicParserConfiguration implements XMLPullParserConfiguration {
    protected static final String ALLOW_JAVA_ENCODINGS = "http://apache.org/xml/features/allow-java-encodings";
    protected static final String CONTINUE_AFTER_FATAL_ERROR = "http://apache.org/xml/features/continue-after-fatal-error";
    protected static final String DATATYPE_VALIDATOR_FACTORY = "http://apache.org/xml/properties/internal/datatype-validator-factory";
    protected static final String DOCUMENT_SCANNER = "http://apache.org/xml/properties/internal/document-scanner";
    protected static final String DTD_PROCESSOR = "http://apache.org/xml/properties/internal/dtd-processor";
    protected static final String DTD_SCANNER = "http://apache.org/xml/properties/internal/dtd-scanner";
    protected static final String DTD_VALIDATOR = "http://apache.org/xml/properties/internal/validator/dtd";
    protected static final String ENTITY_MANAGER = "http://apache.org/xml/properties/internal/entity-manager";
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    protected static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    protected static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    protected static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    protected static final String LOCALE = "http://apache.org/xml/properties/locale";
    protected static final String NAMESPACE_BINDER = "http://apache.org/xml/properties/internal/namespace-binder";
    protected static final String NOTIFY_BUILTIN_REFS = "http://apache.org/xml/features/scanner/notify-builtin-refs";
    protected static final String NOTIFY_CHAR_REFS = "http://apache.org/xml/features/scanner/notify-char-refs";
    protected static final boolean PRINT_EXCEPTION_STACK_TRACE = false;
    private static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    protected static final String VALIDATION_MANAGER = "http://apache.org/xml/properties/internal/validation-manager";
    protected static final String WARN_ON_DUPLICATE_ATTDEF = "http://apache.org/xml/features/validation/warn-on-duplicate-attdef";
    protected static final String WARN_ON_DUPLICATE_ENTITYDEF = "http://apache.org/xml/features/warn-on-duplicate-entitydef";
    protected static final String WARN_ON_UNDECLARED_ELEMDEF = "http://apache.org/xml/features/validation/warn-on-undeclared-elemdef";
    protected static final String XMLGRAMMAR_POOL = "http://apache.org/xml/properties/internal/grammar-pool";
    protected static final String XML_SECURITY_PROPERTY_MANAGER = "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager";
    protected XMLDTDProcessor fDTDProcessor;
    protected XMLDTDScanner fDTDScanner;
    protected XMLDTDValidator fDTDValidator;
    protected DTDDVFactory fDatatypeValidatorFactory;
    protected XMLEntityManager fEntityManager;
    protected XMLErrorReporter fErrorReporter;
    protected XMLGrammarPool fGrammarPool;
    protected XMLInputSource fInputSource;
    protected XMLLocator fLocator;
    protected XMLNamespaceBinder fNamespaceBinder;
    protected boolean fParseInProgress;
    protected XMLDocumentScanner fScanner;
    protected ValidationManager fValidationManager;

    public DTDConfiguration() {
        this(null, null, null);
    }

    public DTDConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    }

    public DTDConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool) {
        this(symbolTable, xMLGrammarPool, null);
    }

    public DTDConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool, XMLComponentManager xMLComponentManager) {
        super(symbolTable, xMLComponentManager);
        this.fParseInProgress = false;
        addRecognizedFeatures(new String[]{CONTINUE_AFTER_FATAL_ERROR, LOAD_EXTERNAL_DTD, "jdk.xml.overrideDefaultParser"});
        setFeature(CONTINUE_AFTER_FATAL_ERROR, false);
        setFeature(LOAD_EXTERNAL_DTD, true);
        this.fFeatures.put("jdk.xml.overrideDefaultParser", Boolean.valueOf(JdkXmlUtils.OVERRIDE_PARSER_DEFAULT));
        addRecognizedProperties(new String[]{"http://apache.org/xml/properties/internal/error-reporter", ENTITY_MANAGER, DOCUMENT_SCANNER, DTD_SCANNER, DTD_PROCESSOR, DTD_VALIDATOR, NAMESPACE_BINDER, "http://apache.org/xml/properties/internal/grammar-pool", DATATYPE_VALIDATOR_FACTORY, VALIDATION_MANAGER, "http://java.sun.com/xml/jaxp/properties/schemaSource", "http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://apache.org/xml/properties/locale", "http://apache.org/xml/properties/security-manager", "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager"});
        this.fGrammarPool = xMLGrammarPool;
        XMLGrammarPool xMLGrammarPool2 = this.fGrammarPool;
        if (xMLGrammarPool2 != null) {
            setProperty("http://apache.org/xml/properties/internal/grammar-pool", xMLGrammarPool2);
        }
        this.fEntityManager = createEntityManager();
        setProperty(ENTITY_MANAGER, this.fEntityManager);
        addComponent(this.fEntityManager);
        this.fErrorReporter = createErrorReporter();
        this.fErrorReporter.setDocumentLocator(this.fEntityManager.getEntityScanner());
        setProperty("http://apache.org/xml/properties/internal/error-reporter", this.fErrorReporter);
        addComponent(this.fErrorReporter);
        this.fScanner = createDocumentScanner();
        setProperty(DOCUMENT_SCANNER, this.fScanner);
        XMLDocumentScanner xMLDocumentScanner = this.fScanner;
        if (xMLDocumentScanner instanceof XMLComponent) {
            addComponent((XMLComponent) xMLDocumentScanner);
        }
        this.fDTDScanner = createDTDScanner();
        XMLDTDScanner xMLDTDScanner = this.fDTDScanner;
        if (xMLDTDScanner != null) {
            setProperty(DTD_SCANNER, xMLDTDScanner);
            XMLDTDScanner xMLDTDScanner2 = this.fDTDScanner;
            if (xMLDTDScanner2 instanceof XMLComponent) {
                addComponent((XMLComponent) xMLDTDScanner2);
            }
        }
        this.fDTDProcessor = createDTDProcessor();
        XMLDTDProcessor xMLDTDProcessor = this.fDTDProcessor;
        if (xMLDTDProcessor != null) {
            setProperty(DTD_PROCESSOR, xMLDTDProcessor);
            XMLDTDProcessor xMLDTDProcessor2 = this.fDTDProcessor;
            if (xMLDTDProcessor2 instanceof XMLComponent) {
                addComponent(xMLDTDProcessor2);
            }
        }
        this.fDTDValidator = createDTDValidator();
        XMLDTDValidator xMLDTDValidator = this.fDTDValidator;
        if (xMLDTDValidator != null) {
            setProperty(DTD_VALIDATOR, xMLDTDValidator);
            addComponent(this.fDTDValidator);
        }
        this.fNamespaceBinder = createNamespaceBinder();
        XMLNamespaceBinder xMLNamespaceBinder = this.fNamespaceBinder;
        if (xMLNamespaceBinder != null) {
            setProperty(NAMESPACE_BINDER, xMLNamespaceBinder);
            addComponent(this.fNamespaceBinder);
        }
        this.fDatatypeValidatorFactory = createDatatypeValidatorFactory();
        DTDDVFactory dTDDVFactory = this.fDatatypeValidatorFactory;
        if (dTDDVFactory != null) {
            setProperty(DATATYPE_VALIDATOR_FACTORY, dTDDVFactory);
        }
        this.fValidationManager = createValidationManager();
        ValidationManager validationManager = this.fValidationManager;
        if (validationManager != null) {
            setProperty(VALIDATION_MANAGER, validationManager);
        }
        if (this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210") == null) {
            XMLMessageFormatter xMLMessageFormatter = new XMLMessageFormatter();
            this.fErrorReporter.putMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210", xMLMessageFormatter);
            this.fErrorReporter.putMessageFormatter("http://www.w3.org/TR/1999/REC-xml-names-19990114", xMLMessageFormatter);
        }
        try {
            setLocale(Locale.getDefault());
        } catch (XNIException unused) {
        }
        setProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", new XMLSecurityPropertyManager());
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
        XMLDTDValidator xMLDTDValidator = this.fDTDValidator;
        if (xMLDTDValidator != null) {
            this.fScanner.setDocumentHandler(xMLDTDValidator);
            if (this.fFeatures.get("http://xml.org/sax/features/namespaces") == Boolean.TRUE) {
                this.fDTDValidator.setDocumentHandler(this.fNamespaceBinder);
                this.fDTDValidator.setDocumentSource(this.fScanner);
                this.fNamespaceBinder.setDocumentHandler(this.fDocumentHandler);
                this.fNamespaceBinder.setDocumentSource(this.fDTDValidator);
                this.fLastComponent = this.fNamespaceBinder;
            } else {
                this.fDTDValidator.setDocumentHandler(this.fDocumentHandler);
                this.fDTDValidator.setDocumentSource(this.fScanner);
                this.fLastComponent = this.fDTDValidator;
            }
        } else if (this.fFeatures.get("http://xml.org/sax/features/namespaces") == Boolean.TRUE) {
            this.fScanner.setDocumentHandler(this.fNamespaceBinder);
            this.fNamespaceBinder.setDocumentHandler(this.fDocumentHandler);
            this.fNamespaceBinder.setDocumentSource(this.fScanner);
            this.fLastComponent = this.fNamespaceBinder;
        } else {
            this.fScanner.setDocumentHandler(this.fDocumentHandler);
            this.fLastComponent = this.fScanner;
        }
        configureDTDPipeline();
    }

    /* access modifiers changed from: protected */
    public void configureDTDPipeline() {
        if (this.fDTDScanner != null) {
            this.fProperties.put(DTD_SCANNER, this.fDTDScanner);
            if (this.fDTDProcessor != null) {
                this.fProperties.put(DTD_PROCESSOR, this.fDTDProcessor);
                this.fDTDScanner.setDTDHandler(this.fDTDProcessor);
                this.fDTDProcessor.setDTDSource(this.fDTDScanner);
                this.fDTDProcessor.setDTDHandler(this.fDTDHandler);
                if (this.fDTDHandler != null) {
                    this.fDTDHandler.setDTDSource(this.fDTDProcessor);
                }
                this.fDTDScanner.setDTDContentModelHandler(this.fDTDProcessor);
                this.fDTDProcessor.setDTDContentModelSource(this.fDTDScanner);
                this.fDTDProcessor.setDTDContentModelHandler(this.fDTDContentModelHandler);
                if (this.fDTDContentModelHandler != null) {
                    this.fDTDContentModelHandler.setDTDContentModelSource(this.fDTDProcessor);
                    return;
                }
                return;
            }
            this.fDTDScanner.setDTDHandler(this.fDTDHandler);
            if (this.fDTDHandler != null) {
                this.fDTDHandler.setDTDSource(this.fDTDScanner);
            }
            this.fDTDScanner.setDTDContentModelHandler(this.fDTDContentModelHandler);
            if (this.fDTDContentModelHandler != null) {
                this.fDTDContentModelHandler.setDTDContentModelSource(this.fDTDScanner);
            }
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
        if (!str.startsWith(Constants.XERCES_PROPERTY_PREFIX) || str.length() - 33 != 20 || !str.endsWith(Constants.DTD_SCANNER_PROPERTY)) {
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
    public XMLDocumentScanner createDocumentScanner() {
        return new XMLDocumentScannerImpl();
    }

    /* access modifiers changed from: protected */
    public XMLDTDScanner createDTDScanner() {
        return new XMLDTDScannerImpl();
    }

    /* access modifiers changed from: protected */
    public XMLDTDProcessor createDTDProcessor() {
        return new XMLDTDProcessor();
    }

    /* access modifiers changed from: protected */
    public XMLDTDValidator createDTDValidator() {
        return new XMLDTDValidator();
    }

    /* access modifiers changed from: protected */
    public XMLNamespaceBinder createNamespaceBinder() {
        return new XMLNamespaceBinder();
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
