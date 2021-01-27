package ohos.com.sun.org.apache.xerces.internal.parsers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.XML11DTDScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.XML11DocumentScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.XML11NSDocumentScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLDTDScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityHandler;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLNSDocumentScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLVersionDetector;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.XML11DTDProcessor;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.XML11DTDValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.XML11NSDTDValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDProcessor;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLNSDTDValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.DTDDVFactory;
import ohos.com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import ohos.com.sun.org.apache.xerces.internal.util.FeatureState;
import ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings;
import ohos.com.sun.org.apache.xerces.internal.util.PropertyState;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDScanner;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentScanner;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLPullParserConfiguration;

public class XML11DTDConfiguration extends ParserConfigurationSettings implements XMLPullParserConfiguration, XML11Configurable {
    protected static final String CONTINUE_AFTER_FATAL_ERROR = "http://apache.org/xml/features/continue-after-fatal-error";
    protected static final String DATATYPE_VALIDATOR_FACTORY = "http://apache.org/xml/properties/internal/datatype-validator-factory";
    protected static final String DOCUMENT_SCANNER = "http://apache.org/xml/properties/internal/document-scanner";
    protected static final String DTD_PROCESSOR = "http://apache.org/xml/properties/internal/dtd-processor";
    protected static final String DTD_SCANNER = "http://apache.org/xml/properties/internal/dtd-scanner";
    protected static final String DTD_VALIDATOR = "http://apache.org/xml/properties/internal/validator/dtd";
    protected static final String ENTITY_MANAGER = "http://apache.org/xml/properties/internal/entity-manager";
    protected static final String ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
    protected static final String ERROR_HANDLER = "http://apache.org/xml/properties/internal/error-handler";
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    protected static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
    protected static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
    protected static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    protected static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    protected static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    protected static final String NAMESPACES = "http://xml.org/sax/features/namespaces";
    protected static final String NAMESPACE_BINDER = "http://apache.org/xml/properties/internal/namespace-binder";
    protected static final boolean PRINT_EXCEPTION_STACK_TRACE = false;
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    protected static final String VALIDATION = "http://xml.org/sax/features/validation";
    protected static final String VALIDATION_MANAGER = "http://apache.org/xml/properties/internal/validation-manager";
    protected static final String XML11_DATATYPE_VALIDATOR_FACTORY = "ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd.XML11DTDDVFactoryImpl";
    protected static final String XMLGRAMMAR_POOL = "http://apache.org/xml/properties/internal/grammar-pool";
    protected static final String XML_STRING = "http://xml.org/sax/properties/xml-string";
    private boolean f11Initialized;
    protected ArrayList fCommonComponents;
    protected ArrayList fComponents;
    protected boolean fConfigUpdated;
    protected XMLDTDScanner fCurrentDTDScanner;
    protected DTDDVFactory fCurrentDVFactory;
    protected XMLDocumentScanner fCurrentScanner;
    protected XMLDTDContentModelHandler fDTDContentModelHandler;
    protected XMLDTDHandler fDTDHandler;
    protected XMLDTDProcessor fDTDProcessor;
    protected XMLDTDScanner fDTDScanner;
    protected XMLDTDValidator fDTDValidator;
    protected DTDDVFactory fDatatypeValidatorFactory;
    protected XMLDocumentHandler fDocumentHandler;
    protected XMLEntityManager fEntityManager;
    protected XMLErrorReporter fErrorReporter;
    protected XMLGrammarPool fGrammarPool;
    protected XMLInputSource fInputSource;
    protected XMLDocumentSource fLastComponent;
    protected Locale fLocale;
    protected XMLLocator fLocator;
    protected XMLNSDocumentScannerImpl fNamespaceScanner;
    protected XMLDTDValidator fNonNSDTDValidator;
    protected XMLDocumentScannerImpl fNonNSScanner;
    protected boolean fParseInProgress;
    protected SymbolTable fSymbolTable;
    protected ValidationManager fValidationManager;
    protected XMLVersionDetector fVersionDetector;
    protected ArrayList fXML11Components;
    protected XML11DTDProcessor fXML11DTDProcessor;
    protected XML11DTDScannerImpl fXML11DTDScanner;
    protected XML11DTDValidator fXML11DTDValidator;
    protected DTDDVFactory fXML11DatatypeFactory;
    protected XML11DocumentScannerImpl fXML11DocScanner;
    protected XML11NSDTDValidator fXML11NSDTDValidator;
    protected XML11NSDocumentScannerImpl fXML11NSDocScanner;

    public XML11DTDConfiguration() {
        this(null, null, null);
    }

    public XML11DTDConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    }

    public XML11DTDConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool) {
        this(symbolTable, xMLGrammarPool, null);
    }

    public XML11DTDConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool, XMLComponentManager xMLComponentManager) {
        super(xMLComponentManager);
        this.fXML11Components = null;
        this.fCommonComponents = null;
        this.fParseInProgress = false;
        this.fConfigUpdated = false;
        this.fXML11DatatypeFactory = null;
        this.fXML11NSDocScanner = null;
        this.fXML11DocScanner = null;
        this.fXML11NSDTDValidator = null;
        this.fXML11DTDValidator = null;
        this.fXML11DTDScanner = null;
        this.fXML11DTDProcessor = null;
        this.f11Initialized = false;
        this.fComponents = new ArrayList();
        this.fXML11Components = new ArrayList();
        this.fCommonComponents = new ArrayList();
        this.fFeatures = new HashMap();
        this.fProperties = new HashMap();
        addRecognizedFeatures(new String[]{CONTINUE_AFTER_FATAL_ERROR, LOAD_EXTERNAL_DTD, VALIDATION, "http://xml.org/sax/features/namespaces", EXTERNAL_GENERAL_ENTITIES, EXTERNAL_PARAMETER_ENTITIES, "http://apache.org/xml/features/internal/parser-settings"});
        this.fFeatures.put(VALIDATION, Boolean.FALSE);
        this.fFeatures.put("http://xml.org/sax/features/namespaces", Boolean.TRUE);
        this.fFeatures.put(EXTERNAL_GENERAL_ENTITIES, Boolean.TRUE);
        this.fFeatures.put(EXTERNAL_PARAMETER_ENTITIES, Boolean.TRUE);
        this.fFeatures.put(CONTINUE_AFTER_FATAL_ERROR, Boolean.FALSE);
        this.fFeatures.put(LOAD_EXTERNAL_DTD, Boolean.TRUE);
        this.fFeatures.put("http://apache.org/xml/features/internal/parser-settings", Boolean.TRUE);
        addRecognizedProperties(new String[]{"http://apache.org/xml/properties/internal/symbol-table", ERROR_HANDLER, "http://apache.org/xml/properties/internal/entity-resolver", "http://apache.org/xml/properties/internal/error-reporter", ENTITY_MANAGER, DOCUMENT_SCANNER, DTD_SCANNER, DTD_PROCESSOR, DTD_VALIDATOR, DATATYPE_VALIDATOR_FACTORY, VALIDATION_MANAGER, XML_STRING, "http://apache.org/xml/properties/internal/grammar-pool", "http://java.sun.com/xml/jaxp/properties/schemaSource", "http://java.sun.com/xml/jaxp/properties/schemaLanguage"});
        this.fSymbolTable = symbolTable == null ? new SymbolTable() : symbolTable;
        this.fProperties.put("http://apache.org/xml/properties/internal/symbol-table", this.fSymbolTable);
        this.fGrammarPool = xMLGrammarPool;
        if (this.fGrammarPool != null) {
            this.fProperties.put("http://apache.org/xml/properties/internal/grammar-pool", this.fGrammarPool);
        }
        this.fEntityManager = new XMLEntityManager();
        this.fProperties.put(ENTITY_MANAGER, this.fEntityManager);
        addCommonComponent(this.fEntityManager);
        this.fErrorReporter = new XMLErrorReporter();
        this.fErrorReporter.setDocumentLocator(this.fEntityManager.getEntityScanner());
        this.fProperties.put("http://apache.org/xml/properties/internal/error-reporter", this.fErrorReporter);
        addCommonComponent(this.fErrorReporter);
        this.fNamespaceScanner = new XMLNSDocumentScannerImpl();
        this.fProperties.put(DOCUMENT_SCANNER, this.fNamespaceScanner);
        addComponent(this.fNamespaceScanner);
        this.fDTDScanner = new XMLDTDScannerImpl();
        this.fProperties.put(DTD_SCANNER, this.fDTDScanner);
        addComponent((XMLComponent) this.fDTDScanner);
        this.fDTDProcessor = new XMLDTDProcessor();
        this.fProperties.put(DTD_PROCESSOR, this.fDTDProcessor);
        addComponent(this.fDTDProcessor);
        this.fDTDValidator = new XMLNSDTDValidator();
        this.fProperties.put(DTD_VALIDATOR, this.fDTDValidator);
        addComponent(this.fDTDValidator);
        this.fDatatypeValidatorFactory = DTDDVFactory.getInstance();
        this.fProperties.put(DATATYPE_VALIDATOR_FACTORY, this.fDatatypeValidatorFactory);
        this.fValidationManager = new ValidationManager();
        this.fProperties.put(VALIDATION_MANAGER, this.fValidationManager);
        this.fVersionDetector = new XMLVersionDetector();
        if (this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210") == null) {
            XMLMessageFormatter xMLMessageFormatter = new XMLMessageFormatter();
            this.fErrorReporter.putMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210", xMLMessageFormatter);
            this.fErrorReporter.putMessageFormatter("http://www.w3.org/TR/1999/REC-xml-names-19990114", xMLMessageFormatter);
        }
        try {
            setLocale(Locale.getDefault());
        } catch (XNIException unused) {
        }
        this.fConfigUpdated = false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLPullParserConfiguration
    public void setInputSource(XMLInputSource xMLInputSource) throws XMLConfigurationException, IOException {
        this.fInputSource = xMLInputSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setLocale(Locale locale) throws XNIException {
        this.fLocale = locale;
        this.fErrorReporter.setLocale(locale);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setDocumentHandler(XMLDocumentHandler xMLDocumentHandler) {
        this.fDocumentHandler = xMLDocumentHandler;
        XMLDocumentSource xMLDocumentSource = this.fLastComponent;
        if (xMLDocumentSource != null) {
            xMLDocumentSource.setDocumentHandler(this.fDocumentHandler);
            XMLDocumentHandler xMLDocumentHandler2 = this.fDocumentHandler;
            if (xMLDocumentHandler2 != null) {
                xMLDocumentHandler2.setDocumentSource(this.fLastComponent);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public XMLDocumentHandler getDocumentHandler() {
        return this.fDocumentHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setDTDHandler(XMLDTDHandler xMLDTDHandler) {
        this.fDTDHandler = xMLDTDHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public XMLDTDHandler getDTDHandler() {
        return this.fDTDHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setDTDContentModelHandler(XMLDTDContentModelHandler xMLDTDContentModelHandler) {
        this.fDTDContentModelHandler = xMLDTDContentModelHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public XMLDTDContentModelHandler getDTDContentModelHandler() {
        return this.fDTDContentModelHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setEntityResolver(XMLEntityResolver xMLEntityResolver) {
        this.fProperties.put("http://apache.org/xml/properties/internal/entity-resolver", xMLEntityResolver);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public XMLEntityResolver getEntityResolver() {
        return (XMLEntityResolver) this.fProperties.get("http://apache.org/xml/properties/internal/entity-resolver");
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setErrorHandler(XMLErrorHandler xMLErrorHandler) {
        this.fProperties.put(ERROR_HANDLER, xMLErrorHandler);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public XMLErrorHandler getErrorHandler() {
        return (XMLErrorHandler) this.fProperties.get(ERROR_HANDLER);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLPullParserConfiguration
    public void cleanup() {
        this.fEntityManager.closeReaders();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
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

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLPullParserConfiguration
    public boolean parse(boolean z) throws XNIException, IOException {
        if (this.fInputSource != null) {
            try {
                this.fValidationManager.reset();
                this.fVersionDetector.reset(this);
                resetCommon();
                short determineDocVersion = this.fVersionDetector.determineDocVersion(this.fInputSource);
                if (determineDocVersion == 2) {
                    initXML11Components();
                    configureXML11Pipeline();
                    resetXML11();
                } else {
                    configurePipeline();
                    reset();
                }
                this.fConfigUpdated = false;
                this.fVersionDetector.startDocumentParsing((XMLEntityHandler) this.fCurrentScanner, determineDocVersion);
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
            return this.fCurrentScanner.scanDocument(z);
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

    @Override // ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager
    public FeatureState getFeatureState(String str) throws XMLConfigurationException {
        if (str.equals("http://apache.org/xml/features/internal/parser-settings")) {
            return FeatureState.is(this.fConfigUpdated);
        }
        return super.getFeatureState(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setFeature(String str, boolean z) throws XMLConfigurationException {
        this.fConfigUpdated = true;
        int size = this.fComponents.size();
        for (int i = 0; i < size; i++) {
            ((XMLComponent) this.fComponents.get(i)).setFeature(str, z);
        }
        int size2 = this.fCommonComponents.size();
        for (int i2 = 0; i2 < size2; i2++) {
            ((XMLComponent) this.fCommonComponents.get(i2)).setFeature(str, z);
        }
        int size3 = this.fXML11Components.size();
        for (int i3 = 0; i3 < size3; i3++) {
            try {
                ((XMLComponent) this.fXML11Components.get(i3)).setFeature(str, z);
            } catch (Exception unused) {
            }
        }
        super.setFeature(str, z);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setProperty(String str, Object obj) throws XMLConfigurationException {
        this.fConfigUpdated = true;
        int size = this.fComponents.size();
        for (int i = 0; i < size; i++) {
            ((XMLComponent) this.fComponents.get(i)).setProperty(str, obj);
        }
        int size2 = this.fCommonComponents.size();
        for (int i2 = 0; i2 < size2; i2++) {
            ((XMLComponent) this.fCommonComponents.get(i2)).setProperty(str, obj);
        }
        int size3 = this.fXML11Components.size();
        for (int i3 = 0; i3 < size3; i3++) {
            try {
                ((XMLComponent) this.fXML11Components.get(i3)).setProperty(str, obj);
            } catch (Exception unused) {
            }
        }
        super.setProperty(str, obj);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public Locale getLocale() {
        return this.fLocale;
    }

    /* access modifiers changed from: protected */
    public void reset() throws XNIException {
        int size = this.fComponents.size();
        for (int i = 0; i < size; i++) {
            ((XMLComponent) this.fComponents.get(i)).reset(this);
        }
    }

    /* access modifiers changed from: protected */
    public void resetCommon() throws XNIException {
        int size = this.fCommonComponents.size();
        for (int i = 0; i < size; i++) {
            ((XMLComponent) this.fCommonComponents.get(i)).reset(this);
        }
    }

    /* access modifiers changed from: protected */
    public void resetXML11() throws XNIException {
        int size = this.fXML11Components.size();
        for (int i = 0; i < size; i++) {
            ((XMLComponent) this.fXML11Components.get(i)).reset(this);
        }
    }

    /* access modifiers changed from: protected */
    public void configureXML11Pipeline() {
        DTDDVFactory dTDDVFactory = this.fCurrentDVFactory;
        DTDDVFactory dTDDVFactory2 = this.fXML11DatatypeFactory;
        if (dTDDVFactory != dTDDVFactory2) {
            this.fCurrentDVFactory = dTDDVFactory2;
            setProperty(DATATYPE_VALIDATOR_FACTORY, this.fCurrentDVFactory);
        }
        XMLDTDScanner xMLDTDScanner = this.fCurrentDTDScanner;
        XML11DTDScannerImpl xML11DTDScannerImpl = this.fXML11DTDScanner;
        if (xMLDTDScanner != xML11DTDScannerImpl) {
            this.fCurrentDTDScanner = xML11DTDScannerImpl;
            setProperty(DTD_SCANNER, this.fCurrentDTDScanner);
            setProperty(DTD_PROCESSOR, this.fXML11DTDProcessor);
        }
        this.fXML11DTDScanner.setDTDHandler(this.fXML11DTDProcessor);
        this.fXML11DTDProcessor.setDTDSource(this.fXML11DTDScanner);
        this.fXML11DTDProcessor.setDTDHandler(this.fDTDHandler);
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.setDTDSource(this.fXML11DTDProcessor);
        }
        this.fXML11DTDScanner.setDTDContentModelHandler(this.fXML11DTDProcessor);
        this.fXML11DTDProcessor.setDTDContentModelSource(this.fXML11DTDScanner);
        this.fXML11DTDProcessor.setDTDContentModelHandler(this.fDTDContentModelHandler);
        XMLDTDContentModelHandler xMLDTDContentModelHandler = this.fDTDContentModelHandler;
        if (xMLDTDContentModelHandler != null) {
            xMLDTDContentModelHandler.setDTDContentModelSource(this.fXML11DTDProcessor);
        }
        if (this.fFeatures.get("http://xml.org/sax/features/namespaces") == Boolean.TRUE) {
            XMLDocumentScanner xMLDocumentScanner = this.fCurrentScanner;
            XML11NSDocumentScannerImpl xML11NSDocumentScannerImpl = this.fXML11NSDocScanner;
            if (xMLDocumentScanner != xML11NSDocumentScannerImpl) {
                this.fCurrentScanner = xML11NSDocumentScannerImpl;
                setProperty(DOCUMENT_SCANNER, xML11NSDocumentScannerImpl);
                setProperty(DTD_VALIDATOR, this.fXML11NSDTDValidator);
            }
            this.fXML11NSDocScanner.setDTDValidator(this.fXML11NSDTDValidator);
            this.fXML11NSDocScanner.setDocumentHandler(this.fXML11NSDTDValidator);
            this.fXML11NSDTDValidator.setDocumentSource(this.fXML11NSDocScanner);
            this.fXML11NSDTDValidator.setDocumentHandler(this.fDocumentHandler);
            XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
            if (xMLDocumentHandler != null) {
                xMLDocumentHandler.setDocumentSource(this.fXML11NSDTDValidator);
            }
            this.fLastComponent = this.fXML11NSDTDValidator;
            return;
        }
        if (this.fXML11DocScanner == null) {
            this.fXML11DocScanner = new XML11DocumentScannerImpl();
            addXML11Component(this.fXML11DocScanner);
            this.fXML11DTDValidator = new XML11DTDValidator();
            addXML11Component(this.fXML11DTDValidator);
        }
        XMLDocumentScanner xMLDocumentScanner2 = this.fCurrentScanner;
        XML11DocumentScannerImpl xML11DocumentScannerImpl = this.fXML11DocScanner;
        if (xMLDocumentScanner2 != xML11DocumentScannerImpl) {
            this.fCurrentScanner = xML11DocumentScannerImpl;
            setProperty(DOCUMENT_SCANNER, xML11DocumentScannerImpl);
            setProperty(DTD_VALIDATOR, this.fXML11DTDValidator);
        }
        this.fXML11DocScanner.setDocumentHandler(this.fXML11DTDValidator);
        this.fXML11DTDValidator.setDocumentSource(this.fXML11DocScanner);
        this.fXML11DTDValidator.setDocumentHandler(this.fDocumentHandler);
        XMLDocumentHandler xMLDocumentHandler2 = this.fDocumentHandler;
        if (xMLDocumentHandler2 != null) {
            xMLDocumentHandler2.setDocumentSource(this.fXML11DTDValidator);
        }
        this.fLastComponent = this.fXML11DTDValidator;
    }

    /* access modifiers changed from: protected */
    public void configurePipeline() {
        DTDDVFactory dTDDVFactory = this.fCurrentDVFactory;
        DTDDVFactory dTDDVFactory2 = this.fDatatypeValidatorFactory;
        if (dTDDVFactory != dTDDVFactory2) {
            this.fCurrentDVFactory = dTDDVFactory2;
            setProperty(DATATYPE_VALIDATOR_FACTORY, this.fCurrentDVFactory);
        }
        XMLDTDScanner xMLDTDScanner = this.fCurrentDTDScanner;
        XMLDTDScanner xMLDTDScanner2 = this.fDTDScanner;
        if (xMLDTDScanner != xMLDTDScanner2) {
            this.fCurrentDTDScanner = xMLDTDScanner2;
            setProperty(DTD_SCANNER, this.fCurrentDTDScanner);
            setProperty(DTD_PROCESSOR, this.fDTDProcessor);
        }
        this.fDTDScanner.setDTDHandler(this.fDTDProcessor);
        this.fDTDProcessor.setDTDSource(this.fDTDScanner);
        this.fDTDProcessor.setDTDHandler(this.fDTDHandler);
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.setDTDSource(this.fDTDProcessor);
        }
        this.fDTDScanner.setDTDContentModelHandler(this.fDTDProcessor);
        this.fDTDProcessor.setDTDContentModelSource(this.fDTDScanner);
        this.fDTDProcessor.setDTDContentModelHandler(this.fDTDContentModelHandler);
        XMLDTDContentModelHandler xMLDTDContentModelHandler = this.fDTDContentModelHandler;
        if (xMLDTDContentModelHandler != null) {
            xMLDTDContentModelHandler.setDTDContentModelSource(this.fDTDProcessor);
        }
        if (this.fFeatures.get("http://xml.org/sax/features/namespaces") == Boolean.TRUE) {
            XMLDocumentScanner xMLDocumentScanner = this.fCurrentScanner;
            XMLNSDocumentScannerImpl xMLNSDocumentScannerImpl = this.fNamespaceScanner;
            if (xMLDocumentScanner != xMLNSDocumentScannerImpl) {
                this.fCurrentScanner = xMLNSDocumentScannerImpl;
                setProperty(DOCUMENT_SCANNER, xMLNSDocumentScannerImpl);
                setProperty(DTD_VALIDATOR, this.fDTDValidator);
            }
            this.fNamespaceScanner.setDTDValidator(this.fDTDValidator);
            this.fNamespaceScanner.setDocumentHandler(this.fDTDValidator);
            this.fDTDValidator.setDocumentSource(this.fNamespaceScanner);
            this.fDTDValidator.setDocumentHandler(this.fDocumentHandler);
            XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
            if (xMLDocumentHandler != null) {
                xMLDocumentHandler.setDocumentSource(this.fDTDValidator);
            }
            this.fLastComponent = this.fDTDValidator;
            return;
        }
        if (this.fNonNSScanner == null) {
            this.fNonNSScanner = new XMLDocumentScannerImpl();
            this.fNonNSDTDValidator = new XMLDTDValidator();
            addComponent(this.fNonNSScanner);
            addComponent(this.fNonNSDTDValidator);
        }
        XMLDocumentScanner xMLDocumentScanner2 = this.fCurrentScanner;
        XMLDocumentScannerImpl xMLDocumentScannerImpl = this.fNonNSScanner;
        if (xMLDocumentScanner2 != xMLDocumentScannerImpl) {
            this.fCurrentScanner = xMLDocumentScannerImpl;
            setProperty(DOCUMENT_SCANNER, xMLDocumentScannerImpl);
            setProperty(DTD_VALIDATOR, this.fNonNSDTDValidator);
        }
        this.fNonNSScanner.setDocumentHandler(this.fNonNSDTDValidator);
        this.fNonNSDTDValidator.setDocumentSource(this.fNonNSScanner);
        this.fNonNSDTDValidator.setDocumentHandler(this.fDocumentHandler);
        XMLDocumentHandler xMLDocumentHandler2 = this.fDocumentHandler;
        if (xMLDocumentHandler2 != null) {
            xMLDocumentHandler2.setDocumentSource(this.fNonNSDTDValidator);
        }
        this.fLastComponent = this.fNonNSDTDValidator;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings
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
            if (length == 24 && str.endsWith(Constants.PARSER_SETTINGS)) {
                return FeatureState.NOT_SUPPORTED;
            }
        }
        return super.checkFeature(str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings
    public PropertyState checkProperty(String str) throws XMLConfigurationException {
        if (str.startsWith(Constants.XERCES_PROPERTY_PREFIX) && str.length() - 33 == 20 && str.endsWith(Constants.DTD_SCANNER_PROPERTY)) {
            return PropertyState.RECOGNIZED;
        }
        if (!str.startsWith(Constants.SAX_PROPERTY_PREFIX) || str.length() - 30 != 10 || !str.endsWith(Constants.XML_STRING_PROPERTY)) {
            return super.checkProperty(str);
        }
        return PropertyState.NOT_SUPPORTED;
    }

    /* access modifiers changed from: protected */
    public void addComponent(XMLComponent xMLComponent) {
        if (!this.fComponents.contains(xMLComponent)) {
            this.fComponents.add(xMLComponent);
            addRecognizedParamsAndSetDefaults(xMLComponent);
        }
    }

    /* access modifiers changed from: protected */
    public void addCommonComponent(XMLComponent xMLComponent) {
        if (!this.fCommonComponents.contains(xMLComponent)) {
            this.fCommonComponents.add(xMLComponent);
            addRecognizedParamsAndSetDefaults(xMLComponent);
        }
    }

    /* access modifiers changed from: protected */
    public void addXML11Component(XMLComponent xMLComponent) {
        if (!this.fXML11Components.contains(xMLComponent)) {
            this.fXML11Components.add(xMLComponent);
            addRecognizedParamsAndSetDefaults(xMLComponent);
        }
    }

    /* access modifiers changed from: protected */
    public void addRecognizedParamsAndSetDefaults(XMLComponent xMLComponent) {
        String[] recognizedFeatures = xMLComponent.getRecognizedFeatures();
        addRecognizedFeatures(recognizedFeatures);
        String[] recognizedProperties = xMLComponent.getRecognizedProperties();
        addRecognizedProperties(recognizedProperties);
        if (recognizedFeatures != null) {
            for (String str : recognizedFeatures) {
                Boolean featureDefault = xMLComponent.getFeatureDefault(str);
                if (featureDefault != null && !this.fFeatures.containsKey(str)) {
                    this.fFeatures.put(str, featureDefault);
                    this.fConfigUpdated = true;
                }
            }
        }
        if (recognizedProperties != null) {
            for (String str2 : recognizedProperties) {
                Object propertyDefault = xMLComponent.getPropertyDefault(str2);
                if (propertyDefault != null && !this.fProperties.containsKey(str2)) {
                    this.fProperties.put(str2, propertyDefault);
                    this.fConfigUpdated = true;
                }
            }
        }
    }

    private void initXML11Components() {
        if (!this.f11Initialized) {
            this.fXML11DatatypeFactory = DTDDVFactory.getInstance(XML11_DATATYPE_VALIDATOR_FACTORY);
            this.fXML11DTDScanner = new XML11DTDScannerImpl();
            addXML11Component(this.fXML11DTDScanner);
            this.fXML11DTDProcessor = new XML11DTDProcessor();
            addXML11Component(this.fXML11DTDProcessor);
            this.fXML11NSDocScanner = new XML11NSDocumentScannerImpl();
            addXML11Component(this.fXML11NSDocScanner);
            this.fXML11NSDTDValidator = new XML11NSDTDValidator();
            addXML11Component(this.fXML11NSDTDValidator);
            this.f11Initialized = true;
        }
    }
}
