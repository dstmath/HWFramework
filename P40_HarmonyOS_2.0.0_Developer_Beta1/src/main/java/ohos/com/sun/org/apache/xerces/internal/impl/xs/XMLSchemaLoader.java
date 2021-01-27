package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMErrorImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMStringListImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidatedInfo;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.SchemaDVFactoryImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.models.CMBuilder;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.models.CMNodeFactory;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers.XSDHandler;
import ohos.com.sun.org.apache.xerces.internal.util.DOMEntityResolverWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.DOMErrorHandlerWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.DefaultErrorHandler;
import ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings;
import ohos.com.sun.org.apache.xerces.internal.util.Status;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XSGrammar;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xerces.internal.xs.LSInputList;
import ohos.com.sun.org.apache.xerces.internal.xs.StringList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSLoader;
import ohos.com.sun.org.apache.xerces.internal.xs.XSModel;
import ohos.org.w3c.dom.DOMConfiguration;
import ohos.org.w3c.dom.DOMErrorHandler;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.DOMStringList;
import ohos.org.w3c.dom.ls.LSInput;
import ohos.org.w3c.dom.ls.LSResourceResolver;
import ohos.org.xml.sax.InputSource;

public class XMLSchemaLoader implements XMLGrammarLoader, XMLComponent, XSLoader, DOMConfiguration {
    public static final String ACCESS_EXTERNAL_DTD = "http://ohos.javax.xml.XMLConstants/property/accessExternalDTD";
    public static final String ACCESS_EXTERNAL_SCHEMA = "http://ohos.javax.xml.XMLConstants/property/accessExternalSchema";
    protected static final String ALLOW_JAVA_ENCODINGS = "http://apache.org/xml/features/allow-java-encodings";
    protected static final String AUGMENT_PSVI = "http://apache.org/xml/features/validation/schema/augment-psvi";
    protected static final String CONTINUE_AFTER_FATAL_ERROR = "http://apache.org/xml/features/continue-after-fatal-error";
    protected static final String DISALLOW_DOCTYPE = "http://apache.org/xml/features/disallow-doctype-decl";
    protected static final String ENTITY_MANAGER = "http://apache.org/xml/properties/internal/entity-manager";
    public static final String ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
    protected static final String ERROR_HANDLER = "http://apache.org/xml/properties/internal/error-handler";
    public static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    protected static final String GENERATE_SYNTHETIC_ANNOTATIONS = "http://apache.org/xml/features/generate-synthetic-annotations";
    protected static final String HONOUR_ALL_SCHEMALOCATIONS = "http://apache.org/xml/features/honour-all-schemaLocations";
    protected static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    protected static final String LOCALE = "http://apache.org/xml/properties/locale";
    protected static final String NAMESPACE_GROWTH = "http://apache.org/xml/features/namespace-growth";
    protected static final String OVERRIDE_PARSER = "jdk.xml.overrideDefaultParser";
    protected static final String PARSER_SETTINGS = "http://apache.org/xml/features/internal/parser-settings";
    private static final String[] RECOGNIZED_FEATURES = {SCHEMA_FULL_CHECKING, AUGMENT_PSVI, CONTINUE_AFTER_FATAL_ERROR, ALLOW_JAVA_ENCODINGS, STANDARD_URI_CONFORMANT_FEATURE, DISALLOW_DOCTYPE, "http://apache.org/xml/features/generate-synthetic-annotations", VALIDATE_ANNOTATIONS, HONOUR_ALL_SCHEMALOCATIONS, NAMESPACE_GROWTH, TOLERATE_DUPLICATES, OVERRIDE_PARSER};
    private static final String[] RECOGNIZED_PROPERTIES = {ENTITY_MANAGER, "http://apache.org/xml/properties/internal/symbol-table", "http://apache.org/xml/properties/internal/error-reporter", ERROR_HANDLER, "http://apache.org/xml/properties/internal/entity-resolver", "http://apache.org/xml/properties/internal/grammar-pool", SCHEMA_LOCATION, SCHEMA_NONS_LOCATION, "http://java.sun.com/xml/jaxp/properties/schemaSource", "http://apache.org/xml/properties/security-manager", "http://apache.org/xml/properties/locale", SCHEMA_DV_FACTORY, "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager"};
    protected static final String SCHEMA_DV_FACTORY = "http://apache.org/xml/properties/internal/validation/schema/dv-factory";
    protected static final String SCHEMA_FULL_CHECKING = "http://apache.org/xml/features/validation/schema-full-checking";
    protected static final String SCHEMA_LOCATION = "http://apache.org/xml/properties/schema/external-schemaLocation";
    protected static final String SCHEMA_NONS_LOCATION = "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation";
    protected static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    protected static final String STANDARD_URI_CONFORMANT_FEATURE = "http://apache.org/xml/features/standard-uri-conformant";
    public static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    protected static final String TOLERATE_DUPLICATES = "http://apache.org/xml/features/internal/tolerate-duplicates";
    protected static final String VALIDATE_ANNOTATIONS = "http://apache.org/xml/features/validate-annotations";
    public static final String XMLGRAMMAR_POOL = "http://apache.org/xml/properties/internal/grammar-pool";
    private static final String XML_SECURITY_PROPERTY_MANAGER = "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager";
    private CMBuilder fCMBuilder;
    private XSDeclarationPool fDeclPool;
    private XMLEntityManager fEntityManager;
    private DOMErrorHandlerWrapper fErrorHandler;
    private XMLErrorReporter fErrorReporter;
    private String fExternalNoNSSchema;
    private String fExternalSchemas;
    private XSGrammarBucket fGrammarBucket;
    private XMLGrammarPool fGrammarPool;
    private boolean fIsCheckedFully;
    private Map fJAXPCache;
    private boolean fJAXPProcessed;
    private Object fJAXPSource;
    private ParserConfigurationSettings fLoaderConfig;
    private Locale fLocale;
    private final CMNodeFactory fNodeFactory;
    private DOMStringList fRecognizedParameters;
    private DOMEntityResolverWrapper fResourceResolver;
    private XSDHandler fSchemaHandler;
    private boolean fSettingsChanged;
    private SubstitutionGroupHandler fSubGroupHandler;
    private SymbolTable fSymbolTable;
    private XMLEntityResolver fUserEntityResolver;
    private XSDDescription fXSDDescription;
    private String faccessExternalSchema;

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSLoader
    public DOMConfiguration getConfig() {
        return this;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public Object getPropertyDefault(String str) {
        return null;
    }

    public XMLSchemaLoader() {
        this(new SymbolTable(), null, new XMLEntityManager(), null, null, null);
    }

    public XMLSchemaLoader(SymbolTable symbolTable) {
        this(symbolTable, null, new XMLEntityManager(), null, null, null);
    }

    XMLSchemaLoader(XMLErrorReporter xMLErrorReporter, XSGrammarBucket xSGrammarBucket, SubstitutionGroupHandler substitutionGroupHandler, CMBuilder cMBuilder) {
        this(null, xMLErrorReporter, null, xSGrammarBucket, substitutionGroupHandler, cMBuilder);
    }

    XMLSchemaLoader(SymbolTable symbolTable, XMLErrorReporter xMLErrorReporter, XMLEntityManager xMLEntityManager, XSGrammarBucket xSGrammarBucket, SubstitutionGroupHandler substitutionGroupHandler, CMBuilder cMBuilder) {
        this.fLoaderConfig = new ParserConfigurationSettings();
        this.fSymbolTable = null;
        this.fErrorReporter = new XMLErrorReporter();
        this.fEntityManager = null;
        this.fUserEntityResolver = null;
        this.fGrammarPool = null;
        this.fExternalSchemas = null;
        this.fExternalNoNSSchema = null;
        this.fJAXPSource = null;
        this.fIsCheckedFully = false;
        this.fJAXPProcessed = false;
        this.fSettingsChanged = true;
        this.fDeclPool = null;
        this.fNodeFactory = new CMNodeFactory();
        this.fXSDDescription = new XSDDescription();
        this.faccessExternalSchema = "all";
        this.fLocale = Locale.getDefault();
        this.fRecognizedParameters = null;
        this.fErrorHandler = null;
        this.fResourceResolver = null;
        this.fLoaderConfig.addRecognizedFeatures(RECOGNIZED_FEATURES);
        this.fLoaderConfig.addRecognizedProperties(RECOGNIZED_PROPERTIES);
        if (symbolTable != null) {
            this.fLoaderConfig.setProperty("http://apache.org/xml/properties/internal/symbol-table", symbolTable);
        }
        if (xMLErrorReporter == null) {
            xMLErrorReporter = new XMLErrorReporter();
            xMLErrorReporter.setLocale(this.fLocale);
            xMLErrorReporter.setProperty(ERROR_HANDLER, new DefaultErrorHandler());
        }
        this.fErrorReporter = xMLErrorReporter;
        if (this.fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN) == null) {
            this.fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, new XSMessageFormatter());
        }
        this.fLoaderConfig.setProperty("http://apache.org/xml/properties/internal/error-reporter", this.fErrorReporter);
        this.fEntityManager = xMLEntityManager;
        XMLEntityManager xMLEntityManager2 = this.fEntityManager;
        if (xMLEntityManager2 != null) {
            this.fLoaderConfig.setProperty(ENTITY_MANAGER, xMLEntityManager2);
        }
        this.fLoaderConfig.setFeature(AUGMENT_PSVI, true);
        this.fGrammarBucket = xSGrammarBucket == null ? new XSGrammarBucket() : xSGrammarBucket;
        this.fSubGroupHandler = substitutionGroupHandler == null ? new SubstitutionGroupHandler(this.fGrammarBucket) : substitutionGroupHandler;
        this.fCMBuilder = cMBuilder == null ? new CMBuilder(this.fNodeFactory) : cMBuilder;
        this.fSchemaHandler = new XSDHandler(this.fGrammarBucket);
        XSDeclarationPool xSDeclarationPool = this.fDeclPool;
        if (xSDeclarationPool != null) {
            xSDeclarationPool.reset();
        }
        this.fJAXPCache = new HashMap();
        this.fSettingsChanged = true;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public String[] getRecognizedFeatures() {
        return (String[]) RECOGNIZED_FEATURES.clone();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader
    public boolean getFeature(String str) throws XMLConfigurationException {
        return this.fLoaderConfig.getFeature(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setFeature(String str, boolean z) throws XMLConfigurationException {
        this.fSettingsChanged = true;
        if (str.equals(CONTINUE_AFTER_FATAL_ERROR)) {
            this.fErrorReporter.setFeature(CONTINUE_AFTER_FATAL_ERROR, z);
        } else if (str.equals("http://apache.org/xml/features/generate-synthetic-annotations")) {
            this.fSchemaHandler.setGenerateSyntheticAnnotations(z);
        }
        this.fLoaderConfig.setFeature(str, z);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public String[] getRecognizedProperties() {
        return (String[]) RECOGNIZED_PROPERTIES.clone();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader
    public Object getProperty(String str) throws XMLConfigurationException {
        return this.fLoaderConfig.getProperty(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setProperty(String str, Object obj) throws XMLConfigurationException {
        this.fSettingsChanged = true;
        this.fLoaderConfig.setProperty(str, obj);
        if (str.equals("http://java.sun.com/xml/jaxp/properties/schemaSource")) {
            this.fJAXPSource = obj;
            this.fJAXPProcessed = false;
        } else if (str.equals("http://apache.org/xml/properties/internal/grammar-pool")) {
            this.fGrammarPool = (XMLGrammarPool) obj;
        } else if (str.equals(SCHEMA_LOCATION)) {
            this.fExternalSchemas = (String) obj;
        } else if (str.equals(SCHEMA_NONS_LOCATION)) {
            this.fExternalNoNSSchema = (String) obj;
        } else if (str.equals("http://apache.org/xml/properties/locale")) {
            setLocale((Locale) obj);
        } else if (str.equals("http://apache.org/xml/properties/internal/entity-resolver")) {
            this.fEntityManager.setProperty("http://apache.org/xml/properties/internal/entity-resolver", obj);
        } else if (str.equals("http://apache.org/xml/properties/internal/error-reporter")) {
            this.fErrorReporter = (XMLErrorReporter) obj;
            if (this.fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN) == null) {
                this.fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, new XSMessageFormatter());
            }
        } else if (str.equals("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager")) {
            this.faccessExternalSchema = ((XMLSecurityPropertyManager) obj).getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_SCHEMA);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader
    public void setLocale(Locale locale) {
        this.fLocale = locale;
        this.fErrorReporter.setLocale(locale);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader
    public Locale getLocale() {
        return this.fLocale;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader
    public void setErrorHandler(XMLErrorHandler xMLErrorHandler) {
        this.fErrorReporter.setProperty(ERROR_HANDLER, xMLErrorHandler);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader
    public XMLErrorHandler getErrorHandler() {
        return this.fErrorReporter.getErrorHandler();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader
    public void setEntityResolver(XMLEntityResolver xMLEntityResolver) {
        this.fUserEntityResolver = xMLEntityResolver;
        this.fLoaderConfig.setProperty("http://apache.org/xml/properties/internal/entity-resolver", xMLEntityResolver);
        this.fEntityManager.setProperty("http://apache.org/xml/properties/internal/entity-resolver", xMLEntityResolver);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader
    public XMLEntityResolver getEntityResolver() {
        return this.fUserEntityResolver;
    }

    public void loadGrammar(XMLInputSource[] xMLInputSourceArr) throws IOException, XNIException {
        for (XMLInputSource xMLInputSource : xMLInputSourceArr) {
            loadGrammar(xMLInputSource);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader
    public Grammar loadGrammar(XMLInputSource xMLInputSource) throws IOException, XNIException {
        XMLGrammarPool xMLGrammarPool;
        reset(this.fLoaderConfig);
        this.fSettingsChanged = false;
        XSDDescription xSDDescription = new XSDDescription();
        xSDDescription.fContextType = 3;
        xSDDescription.setBaseSystemId(xMLInputSource.getBaseSystemId());
        xSDDescription.setLiteralSystemId(xMLInputSource.getSystemId());
        HashMap hashMap = new HashMap();
        processExternalHints(this.fExternalSchemas, this.fExternalNoNSSchema, hashMap, this.fErrorReporter);
        SchemaGrammar loadSchema = loadSchema(xSDDescription, xMLInputSource, hashMap);
        if (!(loadSchema == null || (xMLGrammarPool = this.fGrammarPool) == null)) {
            xMLGrammarPool.cacheGrammars("http://www.w3.org/2001/XMLSchema", this.fGrammarBucket.getGrammars());
            if (this.fIsCheckedFully && this.fJAXPCache.get(loadSchema) != loadSchema) {
                XSConstraints.fullSchemaChecking(this.fGrammarBucket, this.fSubGroupHandler, this.fCMBuilder, this.fErrorReporter);
            }
        }
        return loadSchema;
    }

    /* access modifiers changed from: package-private */
    public SchemaGrammar loadSchema(XSDDescription xSDDescription, XMLInputSource xMLInputSource, Map<String, LocationArray> map) throws IOException, XNIException {
        String checkAccess;
        if (!this.fJAXPProcessed) {
            processJAXPSchemaSource(map);
        }
        if (!xSDDescription.isExternal() || (checkAccess = SecuritySupport.checkAccess(xSDDescription.getExpandedSystemId(), this.faccessExternalSchema, "all")) == null) {
            return this.fSchemaHandler.parseSchema(xMLInputSource, xSDDescription, map);
        }
        throw new XNIException(this.fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN, "schema_reference.access", new Object[]{SecuritySupport.sanitizePath(xSDDescription.getExpandedSystemId()), checkAccess}, 1));
    }

    public static XMLInputSource resolveDocument(XSDDescription xSDDescription, Map<String, LocationArray> map, XMLEntityResolver xMLEntityResolver) throws IOException {
        String str;
        String[] locationHints;
        if (xSDDescription.getContextType() == 2 || xSDDescription.fromInstance()) {
            String targetNamespace = xSDDescription.getTargetNamespace();
            if (targetNamespace == null) {
                targetNamespace = XMLSymbols.EMPTY_STRING;
            }
            LocationArray locationArray = map.get(targetNamespace);
            if (locationArray != null) {
                str = locationArray.getFirstLocation();
                if (str == null && (locationHints = xSDDescription.getLocationHints()) != null && locationHints.length > 0) {
                    str = locationHints[0];
                }
                String expandSystemId = XMLEntityManager.expandSystemId(str, xSDDescription.getBaseSystemId(), false);
                xSDDescription.setLiteralSystemId(str);
                xSDDescription.setExpandedSystemId(expandSystemId);
                return xMLEntityResolver.resolveEntity(xSDDescription);
            }
        }
        str = null;
        str = locationHints[0];
        String expandSystemId2 = XMLEntityManager.expandSystemId(str, xSDDescription.getBaseSystemId(), false);
        xSDDescription.setLiteralSystemId(str);
        xSDDescription.setExpandedSystemId(expandSystemId2);
        return xMLEntityResolver.resolveEntity(xSDDescription);
    }

    public static void processExternalHints(String str, String str2, Map<String, LocationArray> map, XMLErrorReporter xMLErrorReporter) {
        if (str != null) {
            try {
                SchemaGrammar.SG_XSI.getGlobalAttributeDecl(SchemaSymbols.XSI_SCHEMALOCATION).fType.validate(str, (ValidationContext) null, (ValidatedInfo) null);
                if (!tokenizeSchemaLocationStr(str, map)) {
                    xMLErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN, "SchemaLocation", new Object[]{str}, 0);
                }
            } catch (InvalidDatatypeValueException e) {
                xMLErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN, e.getKey(), e.getArgs(), 0);
            }
        }
        if (str2 != null) {
            try {
                SchemaGrammar.SG_XSI.getGlobalAttributeDecl(SchemaSymbols.XSI_NONAMESPACESCHEMALOCATION).fType.validate(str2, (ValidationContext) null, (ValidatedInfo) null);
                LocationArray locationArray = map.get(XMLSymbols.EMPTY_STRING);
                if (locationArray == null) {
                    locationArray = new LocationArray();
                    map.put(XMLSymbols.EMPTY_STRING, locationArray);
                }
                locationArray.addLocation(str2);
            } catch (InvalidDatatypeValueException e2) {
                xMLErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN, e2.getKey(), e2.getArgs(), 0);
            }
        }
    }

    public static boolean tokenizeSchemaLocationStr(String str, Map<String, LocationArray> map) {
        if (str == null) {
            return true;
        }
        StringTokenizer stringTokenizer = new StringTokenizer(str, " \n\t\r");
        while (stringTokenizer.hasMoreTokens()) {
            String nextToken = stringTokenizer.nextToken();
            if (!stringTokenizer.hasMoreTokens()) {
                return false;
            }
            String nextToken2 = stringTokenizer.nextToken();
            LocationArray locationArray = map.get(nextToken);
            if (locationArray == null) {
                locationArray = new LocationArray();
                map.put(nextToken, locationArray);
            }
            locationArray.addLocation(nextToken2);
        }
        return true;
    }

    private void processJAXPSchemaSource(Map<String, LocationArray> map) throws IOException {
        SchemaGrammar schemaGrammar;
        SchemaGrammar schemaGrammar2;
        this.fJAXPProcessed = true;
        Object obj = this.fJAXPSource;
        if (obj != null) {
            Class<?> componentType = obj.getClass().getComponentType();
            if (componentType == null) {
                Object obj2 = this.fJAXPSource;
                if (((obj2 instanceof InputStream) || (obj2 instanceof InputSource)) && (schemaGrammar2 = (SchemaGrammar) this.fJAXPCache.get(this.fJAXPSource)) != null) {
                    this.fGrammarBucket.putGrammar(schemaGrammar2);
                    return;
                }
                this.fXSDDescription.reset();
                XMLInputSource xsdToXMLInputSource = xsdToXMLInputSource(this.fJAXPSource);
                String systemId = xsdToXMLInputSource.getSystemId();
                XSDDescription xSDDescription = this.fXSDDescription;
                xSDDescription.fContextType = 3;
                if (systemId != null) {
                    xSDDescription.setBaseSystemId(xsdToXMLInputSource.getBaseSystemId());
                    this.fXSDDescription.setLiteralSystemId(systemId);
                    this.fXSDDescription.setExpandedSystemId(systemId);
                    this.fXSDDescription.fLocationHints = new String[]{systemId};
                }
                SchemaGrammar loadSchema = loadSchema(this.fXSDDescription, xsdToXMLInputSource, map);
                if (loadSchema != null) {
                    Object obj3 = this.fJAXPSource;
                    if ((obj3 instanceof InputStream) || (obj3 instanceof InputSource)) {
                        this.fJAXPCache.put(this.fJAXPSource, loadSchema);
                        if (this.fIsCheckedFully) {
                            XSConstraints.fullSchemaChecking(this.fGrammarBucket, this.fSubGroupHandler, this.fCMBuilder, this.fErrorReporter);
                        }
                    }
                    this.fGrammarBucket.putGrammar(loadSchema);
                }
            } else if (componentType == Object.class || componentType == String.class || componentType == File.class || componentType == InputStream.class || componentType == InputSource.class) {
                Object[] objArr = (Object[]) this.fJAXPSource;
                Vector vector = new Vector();
                for (int i = 0; i < objArr.length; i++) {
                    if (((objArr[i] instanceof InputStream) || (objArr[i] instanceof InputSource)) && (schemaGrammar = (SchemaGrammar) this.fJAXPCache.get(objArr[i])) != null) {
                        this.fGrammarBucket.putGrammar(schemaGrammar);
                    } else {
                        this.fXSDDescription.reset();
                        XMLInputSource xsdToXMLInputSource2 = xsdToXMLInputSource(objArr[i]);
                        String systemId2 = xsdToXMLInputSource2.getSystemId();
                        XSDDescription xSDDescription2 = this.fXSDDescription;
                        xSDDescription2.fContextType = 3;
                        if (systemId2 != null) {
                            xSDDescription2.setBaseSystemId(xsdToXMLInputSource2.getBaseSystemId());
                            this.fXSDDescription.setLiteralSystemId(systemId2);
                            this.fXSDDescription.setExpandedSystemId(systemId2);
                            this.fXSDDescription.fLocationHints = new String[]{systemId2};
                        }
                        SchemaGrammar parseSchema = this.fSchemaHandler.parseSchema(xsdToXMLInputSource2, this.fXSDDescription, map);
                        if (this.fIsCheckedFully) {
                            XSConstraints.fullSchemaChecking(this.fGrammarBucket, this.fSubGroupHandler, this.fCMBuilder, this.fErrorReporter);
                        }
                        if (parseSchema != null) {
                            String targetNamespace = parseSchema.getTargetNamespace();
                            if (!vector.contains(targetNamespace)) {
                                vector.add(targetNamespace);
                                if ((objArr[i] instanceof InputStream) || (objArr[i] instanceof InputSource)) {
                                    this.fJAXPCache.put(objArr[i], parseSchema);
                                }
                                this.fGrammarBucket.putGrammar(parseSchema);
                            } else {
                                throw new IllegalArgumentException(" When using array of Objects as the value of SCHEMA_SOURCE property , no two Schemas should share the same targetNamespace. ");
                            }
                        } else {
                            continue;
                        }
                    }
                }
            } else {
                throw new XMLConfigurationException(Status.NOT_SUPPORTED, "\"http://java.sun.com/xml/jaxp/properties/schemaSource\" property cannot have an array of type {" + componentType.getName() + "}. Possible types of the array supported are Object, String, File, InputStream, InputSource.");
            }
        }
    }

    private XMLInputSource xsdToXMLInputSource(Object obj) {
        BufferedInputStream bufferedInputStream;
        XMLInputSource xMLInputSource;
        if (obj instanceof String) {
            String str = (String) obj;
            this.fXSDDescription.reset();
            this.fXSDDescription.setValues(null, str, null, null);
            try {
                xMLInputSource = this.fEntityManager.resolveEntity(this.fXSDDescription);
            } catch (IOException unused) {
                this.fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN, "schema_reference.4", new Object[]{str}, 1);
                xMLInputSource = null;
            }
            return xMLInputSource == null ? new XMLInputSource(null, str, null) : xMLInputSource;
        } else if (obj instanceof InputSource) {
            return saxToXMLInputSource((InputSource) obj);
        } else {
            if (obj instanceof InputStream) {
                return new XMLInputSource((String) null, (String) null, (String) null, (InputStream) obj, (String) null);
            }
            if (obj instanceof File) {
                File file = (File) obj;
                try {
                    bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                } catch (FileNotFoundException unused2) {
                    this.fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN, "schema_reference.4", new Object[]{file.toString()}, 1);
                    bufferedInputStream = null;
                }
                return new XMLInputSource((String) null, (String) null, (String) null, bufferedInputStream, (String) null);
            }
            throw new XMLConfigurationException(Status.NOT_SUPPORTED, "\"http://java.sun.com/xml/jaxp/properties/schemaSource\" property cannot have a value of type {" + obj.getClass().getName() + "}. Possible types of the value supported are String, File, InputStream, InputSource OR an array of these types.");
        }
    }

    private static XMLInputSource saxToXMLInputSource(InputSource inputSource) {
        String publicId = inputSource.getPublicId();
        String systemId = inputSource.getSystemId();
        Reader characterStream = inputSource.getCharacterStream();
        if (characterStream != null) {
            return new XMLInputSource(publicId, systemId, (String) null, characterStream, (String) null);
        }
        InputStream byteStream = inputSource.getByteStream();
        if (byteStream != null) {
            return new XMLInputSource(publicId, systemId, (String) null, byteStream, inputSource.getEncoding());
        }
        return new XMLInputSource(publicId, systemId, null);
    }

    public static class LocationArray {
        int length;
        String[] locations = new String[2];

        public void resize(int i, int i2) {
            String[] strArr = new String[i2];
            System.arraycopy(this.locations, 0, strArr, 0, Math.min(i, i2));
            this.locations = strArr;
            this.length = Math.min(i, i2);
        }

        public void addLocation(String str) {
            int i = this.length;
            if (i >= this.locations.length) {
                resize(i, Math.max(1, i * 2));
            }
            String[] strArr = this.locations;
            int i2 = this.length;
            this.length = i2 + 1;
            strArr[i2] = str;
        }

        public String[] getLocationArray() {
            int i = this.length;
            String[] strArr = this.locations;
            if (i < strArr.length) {
                resize(strArr.length, i);
            }
            return this.locations;
        }

        public String getFirstLocation() {
            if (this.length > 0) {
                return this.locations[0];
            }
            return null;
        }

        public int getLength() {
            return this.length;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public Boolean getFeatureDefault(String str) {
        if (str.equals(AUGMENT_PSVI)) {
            return Boolean.TRUE;
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void reset(XMLComponentManager xMLComponentManager) throws XMLConfigurationException {
        XMLSecurityPropertyManager xMLSecurityPropertyManager = (XMLSecurityPropertyManager) xMLComponentManager.getProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager");
        if (xMLSecurityPropertyManager == null) {
            xMLSecurityPropertyManager = new XMLSecurityPropertyManager();
            setProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", xMLSecurityPropertyManager);
        }
        if (((XMLSecurityManager) xMLComponentManager.getProperty("http://apache.org/xml/properties/security-manager")) == null) {
            setProperty("http://apache.org/xml/properties/security-manager", new XMLSecurityManager(true));
        }
        this.faccessExternalSchema = xMLSecurityPropertyManager.getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_SCHEMA);
        this.fGrammarBucket.reset();
        this.fSubGroupHandler.reset();
        if (!xMLComponentManager.getFeature(PARSER_SETTINGS, true) || !this.fSettingsChanged) {
            this.fJAXPProcessed = false;
            initGrammarBucket();
            return;
        }
        this.fNodeFactory.reset(xMLComponentManager);
        this.fEntityManager = (XMLEntityManager) xMLComponentManager.getProperty(ENTITY_MANAGER);
        this.fErrorReporter = (XMLErrorReporter) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/error-reporter");
        SchemaDVFactory dVFactory = this.fSchemaHandler.getDVFactory();
        if (dVFactory == null) {
            dVFactory = SchemaDVFactory.getInstance();
            this.fSchemaHandler.setDVFactory(dVFactory);
        }
        if (!xMLComponentManager.getFeature(AUGMENT_PSVI, false)) {
            XSDeclarationPool xSDeclarationPool = this.fDeclPool;
            if (xSDeclarationPool != null) {
                xSDeclarationPool.reset();
            } else {
                this.fDeclPool = new XSDeclarationPool();
            }
            this.fCMBuilder.setDeclPool(this.fDeclPool);
            this.fSchemaHandler.setDeclPool(this.fDeclPool);
            if (dVFactory instanceof SchemaDVFactoryImpl) {
                SchemaDVFactoryImpl schemaDVFactoryImpl = (SchemaDVFactoryImpl) dVFactory;
                this.fDeclPool.setDVFactory(schemaDVFactoryImpl);
                schemaDVFactoryImpl.setDeclPool(this.fDeclPool);
            }
        } else {
            this.fCMBuilder.setDeclPool(null);
            this.fSchemaHandler.setDeclPool(null);
        }
        try {
            this.fExternalSchemas = (String) xMLComponentManager.getProperty(SCHEMA_LOCATION);
            this.fExternalNoNSSchema = (String) xMLComponentManager.getProperty(SCHEMA_NONS_LOCATION);
        } catch (XMLConfigurationException unused) {
            this.fExternalSchemas = null;
            this.fExternalNoNSSchema = null;
        }
        this.fJAXPSource = xMLComponentManager.getProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", null);
        this.fJAXPProcessed = false;
        this.fGrammarPool = (XMLGrammarPool) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/grammar-pool", null);
        initGrammarBucket();
        try {
            boolean feature = xMLComponentManager.getFeature(CONTINUE_AFTER_FATAL_ERROR, false);
            if (!feature) {
                this.fErrorReporter.setFeature(CONTINUE_AFTER_FATAL_ERROR, feature);
            }
        } catch (XMLConfigurationException unused2) {
        }
        this.fIsCheckedFully = xMLComponentManager.getFeature(SCHEMA_FULL_CHECKING, false);
        this.fSchemaHandler.setGenerateSyntheticAnnotations(xMLComponentManager.getFeature("http://apache.org/xml/features/generate-synthetic-annotations", false));
        this.fSchemaHandler.reset(xMLComponentManager);
    }

    private void initGrammarBucket() {
        Grammar[] retrieveInitialGrammarSet;
        XMLGrammarPool xMLGrammarPool = this.fGrammarPool;
        if (xMLGrammarPool != null) {
            for (Grammar grammar : xMLGrammarPool.retrieveInitialGrammarSet("http://www.w3.org/2001/XMLSchema")) {
                if (!this.fGrammarBucket.putGrammar((SchemaGrammar) grammar, true)) {
                    this.fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN, "GrammarConflict", null, 0);
                }
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSLoader
    public XSModel load(LSInput lSInput) {
        try {
            return ((XSGrammar) loadGrammar(dom2xmlInputSource(lSInput))).toXSModel();
        } catch (Exception e) {
            this.reportDOMFatalError(e);
            return null;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSLoader
    public XSModel loadInputList(LSInputList lSInputList) {
        int length = lSInputList.getLength();
        SchemaGrammar[] schemaGrammarArr = new SchemaGrammar[length];
        for (int i = 0; i < length; i++) {
            try {
                schemaGrammarArr[i] = (SchemaGrammar) loadGrammar(dom2xmlInputSource(lSInputList.item(i)));
            } catch (Exception e) {
                reportDOMFatalError(e);
                return null;
            }
        }
        return new XSModelImpl(schemaGrammarArr);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSLoader
    public XSModel loadURI(String str) {
        try {
            return ((XSGrammar) loadGrammar(new XMLInputSource(null, str, null))).toXSModel();
        } catch (Exception e) {
            this.reportDOMFatalError(e);
            return null;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSLoader
    public XSModel loadURIList(StringList stringList) {
        int length = stringList.getLength();
        SchemaGrammar[] schemaGrammarArr = new SchemaGrammar[length];
        for (int i = 0; i < length; i++) {
            try {
                schemaGrammarArr[i] = (SchemaGrammar) loadGrammar(new XMLInputSource(null, stringList.item(i), null));
            } catch (Exception e) {
                reportDOMFatalError(e);
                return null;
            }
        }
        return new XSModelImpl(schemaGrammarArr);
    }

    /* access modifiers changed from: package-private */
    public void reportDOMFatalError(Exception exc) {
        if (this.fErrorHandler != null) {
            DOMErrorImpl dOMErrorImpl = new DOMErrorImpl();
            dOMErrorImpl.fException = exc;
            dOMErrorImpl.fMessage = exc.getMessage();
            dOMErrorImpl.fSeverity = 3;
            this.fErrorHandler.getErrorHandler().handleError(dOMErrorImpl);
        }
    }

    public boolean canSetParameter(String str, Object obj) {
        return obj instanceof Boolean ? str.equals(Constants.DOM_VALIDATE) || str.equals(SCHEMA_FULL_CHECKING) || str.equals(VALIDATE_ANNOTATIONS) || str.equals(CONTINUE_AFTER_FATAL_ERROR) || str.equals(ALLOW_JAVA_ENCODINGS) || str.equals(STANDARD_URI_CONFORMANT_FEATURE) || str.equals("http://apache.org/xml/features/generate-synthetic-annotations") || str.equals(HONOUR_ALL_SCHEMALOCATIONS) || str.equals(NAMESPACE_GROWTH) || str.equals(TOLERATE_DUPLICATES) || str.equals(OVERRIDE_PARSER) : str.equals(Constants.DOM_ERROR_HANDLER) || str.equals(Constants.DOM_RESOURCE_RESOLVER) || str.equals("http://apache.org/xml/properties/internal/symbol-table") || str.equals("http://apache.org/xml/properties/internal/error-reporter") || str.equals(ERROR_HANDLER) || str.equals("http://apache.org/xml/properties/internal/entity-resolver") || str.equals("http://apache.org/xml/properties/internal/grammar-pool") || str.equals(SCHEMA_LOCATION) || str.equals(SCHEMA_NONS_LOCATION) || str.equals("http://java.sun.com/xml/jaxp/properties/schemaSource") || str.equals(SCHEMA_DV_FACTORY);
    }

    public Object getParameter(String str) throws DOMException {
        if (str.equals(Constants.DOM_ERROR_HANDLER)) {
            DOMErrorHandlerWrapper dOMErrorHandlerWrapper = this.fErrorHandler;
            if (dOMErrorHandlerWrapper != null) {
                return dOMErrorHandlerWrapper.getErrorHandler();
            }
            return null;
        } else if (str.equals(Constants.DOM_RESOURCE_RESOLVER)) {
            DOMEntityResolverWrapper dOMEntityResolverWrapper = this.fResourceResolver;
            if (dOMEntityResolverWrapper != null) {
                return dOMEntityResolverWrapper.getEntityResolver();
            }
            return null;
        } else {
            try {
                return getFeature(str) ? Boolean.TRUE : Boolean.FALSE;
            } catch (Exception unused) {
                try {
                    return getProperty(str);
                } catch (Exception unused2) {
                    throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_SUPPORTED", new Object[]{str}));
                }
            }
        }
    }

    public DOMStringList getParameterNames() {
        if (this.fRecognizedParameters == null) {
            Vector vector = new Vector();
            vector.add(Constants.DOM_VALIDATE);
            vector.add(Constants.DOM_ERROR_HANDLER);
            vector.add(Constants.DOM_RESOURCE_RESOLVER);
            vector.add("http://apache.org/xml/properties/internal/symbol-table");
            vector.add("http://apache.org/xml/properties/internal/error-reporter");
            vector.add(ERROR_HANDLER);
            vector.add("http://apache.org/xml/properties/internal/entity-resolver");
            vector.add("http://apache.org/xml/properties/internal/grammar-pool");
            vector.add(SCHEMA_LOCATION);
            vector.add(SCHEMA_NONS_LOCATION);
            vector.add("http://java.sun.com/xml/jaxp/properties/schemaSource");
            vector.add(SCHEMA_FULL_CHECKING);
            vector.add(CONTINUE_AFTER_FATAL_ERROR);
            vector.add(ALLOW_JAVA_ENCODINGS);
            vector.add(STANDARD_URI_CONFORMANT_FEATURE);
            vector.add(VALIDATE_ANNOTATIONS);
            vector.add("http://apache.org/xml/features/generate-synthetic-annotations");
            vector.add(HONOUR_ALL_SCHEMALOCATIONS);
            vector.add(NAMESPACE_GROWTH);
            vector.add(TOLERATE_DUPLICATES);
            vector.add(OVERRIDE_PARSER);
            this.fRecognizedParameters = new DOMStringListImpl(vector);
        }
        return this.fRecognizedParameters;
    }

    public void setParameter(String str, Object obj) throws DOMException {
        if (obj instanceof Boolean) {
            boolean booleanValue = ((Boolean) obj).booleanValue();
            if (!str.equals(Constants.DOM_VALIDATE) || !booleanValue) {
                try {
                    setFeature(str, booleanValue);
                } catch (Exception unused) {
                    throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_SUPPORTED", new Object[]{str}));
                }
            }
        } else if (str.equals(Constants.DOM_ERROR_HANDLER)) {
            if (obj instanceof DOMErrorHandler) {
                try {
                    this.fErrorHandler = new DOMErrorHandlerWrapper((DOMErrorHandler) obj);
                    setErrorHandler(this.fErrorHandler);
                } catch (XMLConfigurationException unused2) {
                }
            } else {
                throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_SUPPORTED", new Object[]{str}));
            }
        } else if (!str.equals(Constants.DOM_RESOURCE_RESOLVER)) {
            try {
                setProperty(str, obj);
            } catch (Exception unused3) {
                throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_SUPPORTED", new Object[]{str}));
            }
        } else if (obj instanceof LSResourceResolver) {
            try {
                this.fResourceResolver = new DOMEntityResolverWrapper((LSResourceResolver) obj);
                setEntityResolver(this.fResourceResolver);
            } catch (XMLConfigurationException unused4) {
            }
        } else {
            throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_SUPPORTED", new Object[]{str}));
        }
    }

    /* access modifiers changed from: package-private */
    public XMLInputSource dom2xmlInputSource(LSInput lSInput) {
        if (lSInput.getCharacterStream() != null) {
            return new XMLInputSource(lSInput.getPublicId(), lSInput.getSystemId(), lSInput.getBaseURI(), lSInput.getCharacterStream(), "UTF-16");
        }
        if (lSInput.getByteStream() != null) {
            return new XMLInputSource(lSInput.getPublicId(), lSInput.getSystemId(), lSInput.getBaseURI(), lSInput.getByteStream(), lSInput.getEncoding());
        }
        if (lSInput.getStringData() == null || lSInput.getStringData().length() == 0) {
            return new XMLInputSource(lSInput.getPublicId(), lSInput.getSystemId(), lSInput.getBaseURI());
        }
        return new XMLInputSource(lSInput.getPublicId(), lSInput.getSystemId(), lSInput.getBaseURI(), new StringReader(lSInput.getStringData()), "UTF-16");
    }
}
