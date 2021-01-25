package ohos.com.sun.org.apache.xerces.internal.dom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.DTDDVFactory;
import ohos.com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.util.DOMEntityResolverWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.DOMErrorHandlerWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.MessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings;
import ohos.com.sun.org.apache.xerces.internal.util.PropertyState;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.w3c.dom.DOMConfiguration;
import ohos.org.w3c.dom.DOMErrorHandler;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.DOMStringList;
import ohos.org.w3c.dom.ls.LSResourceResolver;

public class DOMConfigurationImpl extends ParserConfigurationSettings implements XMLParserConfiguration, DOMConfiguration {
    protected static final short CDATA = 8;
    protected static final short COMMENTS = 32;
    protected static final String DTD_VALIDATOR_FACTORY_PROPERTY = "http://apache.org/xml/properties/internal/datatype-validator-factory";
    protected static final short DTNORMALIZATION = 2;
    protected static final String DYNAMIC_VALIDATION = "http://apache.org/xml/features/validation/dynamic";
    protected static final short ENTITIES = 4;
    protected static final String ENTITY_MANAGER = "http://apache.org/xml/properties/internal/entity-manager";
    protected static final String ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
    protected static final String ERROR_HANDLER = "http://apache.org/xml/properties/internal/error-handler";
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    protected static final String GRAMMAR_POOL = "http://apache.org/xml/properties/internal/grammar-pool";
    protected static final short INFOSET_FALSE_PARAMS = 14;
    protected static final short INFOSET_MASK = 815;
    protected static final short INFOSET_TRUE_PARAMS = 801;
    protected static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    protected static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    protected static final short NAMESPACES = 1;
    protected static final String NAMESPACE_GROWTH = "http://apache.org/xml/features/namespace-growth";
    protected static final String NORMALIZE_DATA = "http://apache.org/xml/features/validation/schema/normalized-value";
    protected static final short NSDECL = 512;
    protected static final short PSVI = 128;
    protected static final String SCHEMA = "http://apache.org/xml/features/validation/schema";
    protected static final String SCHEMA_DV_FACTORY = "http://apache.org/xml/properties/internal/validation/schema/dv-factory";
    protected static final String SCHEMA_FULL_CHECKING = "http://apache.org/xml/features/validation/schema-full-checking";
    private static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    protected static final String SEND_PSVI = "http://apache.org/xml/features/validation/schema/augment-psvi";
    protected static final short SPLITCDATA = 16;
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    protected static final String TOLERATE_DUPLICATES = "http://apache.org/xml/features/internal/tolerate-duplicates";
    protected static final short VALIDATE = 64;
    protected static final String VALIDATION_MANAGER = "http://apache.org/xml/properties/internal/validation-manager";
    protected static final short WELLFORMED = 256;
    protected static final String XERCES_NAMESPACES = "http://xml.org/sax/features/namespaces";
    protected static final String XERCES_VALIDATION = "http://xml.org/sax/features/validation";
    private static final String XML_SECURITY_PROPERTY_MANAGER = "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager";
    protected static final String XML_STRING = "http://xml.org/sax/properties/xml-string";
    protected ArrayList fComponents;
    XMLDocumentHandler fDocumentHandler;
    protected final DOMErrorHandlerWrapper fErrorHandlerWrapper;
    protected XMLErrorReporter fErrorReporter;
    protected Locale fLocale;
    private DOMStringList fRecognizedParameters;
    protected SymbolTable fSymbolTable;
    protected ValidationManager fValidationManager;
    protected short features;

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public XMLDTDContentModelHandler getDTDContentModelHandler() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public XMLDTDHandler getDTDHandler() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void parse(XMLInputSource xMLInputSource) throws XNIException, IOException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setDTDContentModelHandler(XMLDTDContentModelHandler xMLDTDContentModelHandler) {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setDTDHandler(XMLDTDHandler xMLDTDHandler) {
    }

    protected DOMConfigurationImpl() {
        this(null, null);
    }

    protected DOMConfigurationImpl(SymbolTable symbolTable) {
        this(symbolTable, null);
    }

    protected DOMConfigurationImpl(SymbolTable symbolTable, XMLComponentManager xMLComponentManager) {
        super(xMLComponentManager);
        this.features = 0;
        this.fErrorHandlerWrapper = new DOMErrorHandlerWrapper();
        this.fFeatures = new HashMap();
        this.fProperties = new HashMap();
        addRecognizedFeatures(new String[]{XERCES_VALIDATION, "http://xml.org/sax/features/namespaces", SCHEMA, SCHEMA_FULL_CHECKING, DYNAMIC_VALIDATION, NORMALIZE_DATA, SEND_PSVI, NAMESPACE_GROWTH, TOLERATE_DUPLICATES, "jdk.xml.overrideDefaultParser"});
        setFeature(XERCES_VALIDATION, false);
        setFeature(SCHEMA, false);
        setFeature(SCHEMA_FULL_CHECKING, false);
        setFeature(DYNAMIC_VALIDATION, false);
        setFeature(NORMALIZE_DATA, false);
        setFeature("http://xml.org/sax/features/namespaces", true);
        setFeature(SEND_PSVI, true);
        setFeature(NAMESPACE_GROWTH, false);
        setFeature("jdk.xml.overrideDefaultParser", JdkXmlUtils.OVERRIDE_PARSER_DEFAULT);
        addRecognizedProperties(new String[]{XML_STRING, "http://apache.org/xml/properties/internal/symbol-table", ERROR_HANDLER, "http://apache.org/xml/properties/internal/entity-resolver", "http://apache.org/xml/properties/internal/error-reporter", ENTITY_MANAGER, VALIDATION_MANAGER, "http://apache.org/xml/properties/internal/grammar-pool", "http://java.sun.com/xml/jaxp/properties/schemaSource", "http://java.sun.com/xml/jaxp/properties/schemaLanguage", DTD_VALIDATOR_FACTORY_PROPERTY, SCHEMA_DV_FACTORY, "http://apache.org/xml/properties/security-manager", "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager"});
        this.features = (short) (this.features | 1);
        this.features = (short) (this.features | 4);
        this.features = (short) (this.features | 32);
        this.features = (short) (this.features | 8);
        this.features = (short) (this.features | 16);
        this.features = (short) (this.features | 256);
        this.features = (short) (this.features | 512);
        this.fSymbolTable = symbolTable == null ? new SymbolTable() : symbolTable;
        this.fComponents = new ArrayList();
        setProperty("http://apache.org/xml/properties/internal/symbol-table", this.fSymbolTable);
        this.fErrorReporter = new XMLErrorReporter();
        setProperty("http://apache.org/xml/properties/internal/error-reporter", this.fErrorReporter);
        addComponent(this.fErrorReporter);
        setProperty(DTD_VALIDATOR_FACTORY_PROPERTY, DTDDVFactory.getInstance());
        XMLComponent xMLEntityManager = new XMLEntityManager();
        setProperty(ENTITY_MANAGER, xMLEntityManager);
        addComponent(xMLEntityManager);
        this.fValidationManager = createValidationManager();
        setProperty(VALIDATION_MANAGER, this.fValidationManager);
        setProperty("http://apache.org/xml/properties/security-manager", new XMLSecurityManager(true));
        setProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", new XMLSecurityPropertyManager());
        if (this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210") == null) {
            XMLMessageFormatter xMLMessageFormatter = new XMLMessageFormatter();
            this.fErrorReporter.putMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210", xMLMessageFormatter);
            this.fErrorReporter.putMessageFormatter("http://www.w3.org/TR/1999/REC-xml-names-19990114", xMLMessageFormatter);
        }
        if (this.fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN) == null) {
            MessageFormatter messageFormatter = null;
            try {
                messageFormatter = (MessageFormatter) ObjectFactory.newInstance("ohos.com.sun.org.apache.xerces.internal.impl.xs.XSMessageFormatter", true);
            } catch (Exception unused) {
            }
            if (messageFormatter != null) {
                this.fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, messageFormatter);
            }
        }
        try {
            setLocale(Locale.getDefault());
        } catch (XNIException unused2) {
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setDocumentHandler(XMLDocumentHandler xMLDocumentHandler) {
        this.fDocumentHandler = xMLDocumentHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public XMLDocumentHandler getDocumentHandler() {
        return this.fDocumentHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setEntityResolver(XMLEntityResolver xMLEntityResolver) {
        if (xMLEntityResolver != null) {
            this.fProperties.put("http://apache.org/xml/properties/internal/entity-resolver", xMLEntityResolver);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public XMLEntityResolver getEntityResolver() {
        return (XMLEntityResolver) this.fProperties.get("http://apache.org/xml/properties/internal/entity-resolver");
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setErrorHandler(XMLErrorHandler xMLErrorHandler) {
        if (xMLErrorHandler != null) {
            this.fProperties.put(ERROR_HANDLER, xMLErrorHandler);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public XMLErrorHandler getErrorHandler() {
        return (XMLErrorHandler) this.fProperties.get(ERROR_HANDLER);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setFeature(String str, boolean z) throws XMLConfigurationException {
        super.setFeature(str, z);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setProperty(String str, Object obj) throws XMLConfigurationException {
        super.setProperty(str, obj);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setLocale(Locale locale) throws XNIException {
        this.fLocale = locale;
        this.fErrorReporter.setLocale(locale);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public Locale getLocale() {
        return this.fLocale;
    }

    /* JADX WARNING: Removed duplicated region for block: B:102:0x01cb  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x01a8  */
    public void setParameter(String str, Object obj) throws DOMException {
        boolean z;
        boolean z2 = obj instanceof Boolean;
        if (z2) {
            boolean booleanValue = ((Boolean) obj).booleanValue();
            if (str.equalsIgnoreCase(Constants.DOM_COMMENTS)) {
                this.features = (short) (booleanValue ? this.features | 32 : this.features & -33);
            } else if (str.equalsIgnoreCase(Constants.DOM_DATATYPE_NORMALIZATION)) {
                setFeature(NORMALIZE_DATA, booleanValue);
                short s = this.features;
                this.features = (short) (booleanValue ? s | 2 : s & -3);
                if (booleanValue) {
                    this.features = (short) (this.features | 64);
                }
            } else if (str.equalsIgnoreCase("namespaces")) {
                this.features = (short) (booleanValue ? this.features | 1 : this.features & -2);
            } else if (str.equalsIgnoreCase(Constants.DOM_CDATA_SECTIONS)) {
                this.features = (short) (booleanValue ? this.features | 8 : this.features & -9);
            } else if (str.equalsIgnoreCase(Constants.DOM_ENTITIES)) {
                this.features = (short) (booleanValue ? this.features | 4 : this.features & -5);
            } else if (str.equalsIgnoreCase(Constants.DOM_SPLIT_CDATA)) {
                this.features = (short) (booleanValue ? this.features | 16 : this.features & -17);
            } else if (str.equalsIgnoreCase(Constants.DOM_VALIDATE)) {
                this.features = (short) (booleanValue ? this.features | 64 : this.features & -65);
            } else if (str.equalsIgnoreCase(Constants.DOM_WELLFORMED)) {
                this.features = (short) (booleanValue ? this.features | 256 : this.features & -257);
            } else if (str.equalsIgnoreCase(Constants.DOM_NAMESPACE_DECLARATIONS)) {
                this.features = (short) (booleanValue ? this.features | 512 : this.features & -513);
            } else if (str.equalsIgnoreCase(Constants.DOM_INFOSET)) {
                if (booleanValue) {
                    this.features = (short) (this.features | INFOSET_TRUE_PARAMS);
                    this.features = (short) (this.features & -15);
                    setFeature(NORMALIZE_DATA, false);
                }
            } else if (str.equalsIgnoreCase(Constants.DOM_NORMALIZE_CHARACTERS) || str.equalsIgnoreCase(Constants.DOM_CANONICAL_FORM) || str.equalsIgnoreCase(Constants.DOM_VALIDATE_IF_SCHEMA) || str.equalsIgnoreCase(Constants.DOM_CHECK_CHAR_NORMALIZATION)) {
                if (booleanValue) {
                    throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_SUPPORTED", new Object[]{str}));
                }
            } else if (str.equalsIgnoreCase(Constants.DOM_ELEMENT_CONTENT_WHITESPACE)) {
                if (!booleanValue) {
                    throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_SUPPORTED", new Object[]{str}));
                }
            } else if (str.equalsIgnoreCase(SEND_PSVI)) {
                if (!booleanValue) {
                    throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_SUPPORTED", new Object[]{str}));
                }
            } else if (str.equalsIgnoreCase(Constants.DOM_PSVI)) {
                this.features = (short) (booleanValue ? this.features | 128 : this.features & -129);
            } else {
                z = false;
                if (!z && z2) {
                    return;
                }
                if (!str.equalsIgnoreCase(Constants.DOM_ERROR_HANDLER)) {
                    if ((obj instanceof DOMErrorHandler) || obj == null) {
                        this.fErrorHandlerWrapper.setErrorHandler((DOMErrorHandler) obj);
                        setErrorHandler(this.fErrorHandlerWrapper);
                        return;
                    }
                    throw new DOMException(17, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "TYPE_MISMATCH_ERR", new Object[]{str}));
                } else if (str.equalsIgnoreCase(Constants.DOM_RESOURCE_RESOLVER)) {
                    if ((obj instanceof LSResourceResolver) || obj == null) {
                        try {
                            setEntityResolver(new DOMEntityResolverWrapper((LSResourceResolver) obj));
                            return;
                        } catch (XMLConfigurationException unused) {
                            return;
                        }
                    } else {
                        throw new DOMException(17, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "TYPE_MISMATCH_ERR", new Object[]{str}));
                    }
                } else if (str.equalsIgnoreCase(Constants.DOM_SCHEMA_LOCATION)) {
                    if ((obj instanceof String) || obj == null) {
                        setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", obj);
                        return;
                    }
                    throw new DOMException(17, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "TYPE_MISMATCH_ERR", new Object[]{str}));
                } else if (str.equalsIgnoreCase(Constants.DOM_SCHEMA_TYPE)) {
                    if (!(obj instanceof String) && obj != null) {
                        throw new DOMException(17, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "TYPE_MISMATCH_ERR", new Object[]{str}));
                    } else if (obj == null) {
                        setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", null);
                        return;
                    } else if (obj.equals(Constants.NS_XMLSCHEMA)) {
                        setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", Constants.NS_XMLSCHEMA);
                        return;
                    } else if (obj.equals(Constants.NS_DTD)) {
                        setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", Constants.NS_DTD);
                        return;
                    } else {
                        return;
                    }
                } else if (str.equalsIgnoreCase("http://apache.org/xml/properties/internal/symbol-table")) {
                    if (obj instanceof SymbolTable) {
                        setProperty("http://apache.org/xml/properties/internal/symbol-table", obj);
                        return;
                    }
                    throw new DOMException(17, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "TYPE_MISMATCH_ERR", new Object[]{str}));
                } else if (!str.equalsIgnoreCase("http://apache.org/xml/properties/internal/grammar-pool")) {
                    throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_FOUND", new Object[]{str}));
                } else if (obj instanceof XMLGrammarPool) {
                    setProperty("http://apache.org/xml/properties/internal/grammar-pool", obj);
                    return;
                } else {
                    throw new DOMException(17, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "TYPE_MISMATCH_ERR", new Object[]{str}));
                }
            }
        }
        z = true;
        if (!z) {
        }
        if (!str.equalsIgnoreCase(Constants.DOM_ERROR_HANDLER)) {
        }
    }

    public Object getParameter(String str) throws DOMException {
        if (str.equalsIgnoreCase(Constants.DOM_COMMENTS)) {
            return (this.features & 32) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase("namespaces")) {
            return (this.features & 1) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_DATATYPE_NORMALIZATION)) {
            return (this.features & 2) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_CDATA_SECTIONS)) {
            return (this.features & 8) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_ENTITIES)) {
            return (this.features & 4) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_SPLIT_CDATA)) {
            return (this.features & 16) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_VALIDATE)) {
            return (this.features & 64) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_WELLFORMED)) {
            return (this.features & 256) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_NAMESPACE_DECLARATIONS)) {
            return (this.features & 512) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_INFOSET)) {
            return (this.features & INFOSET_MASK) == 801 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_NORMALIZE_CHARACTERS) || str.equalsIgnoreCase(Constants.DOM_CANONICAL_FORM) || str.equalsIgnoreCase(Constants.DOM_VALIDATE_IF_SCHEMA) || str.equalsIgnoreCase(Constants.DOM_CHECK_CHAR_NORMALIZATION)) {
            return Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(SEND_PSVI)) {
            return Boolean.TRUE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_PSVI)) {
            return (this.features & 128) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_ELEMENT_CONTENT_WHITESPACE)) {
            return Boolean.TRUE;
        }
        if (str.equalsIgnoreCase(Constants.DOM_ERROR_HANDLER)) {
            return this.fErrorHandlerWrapper.getErrorHandler();
        }
        if (str.equalsIgnoreCase(Constants.DOM_RESOURCE_RESOLVER)) {
            XMLEntityResolver entityResolver = getEntityResolver();
            if (entityResolver == null || !(entityResolver instanceof DOMEntityResolverWrapper)) {
                return null;
            }
            return ((DOMEntityResolverWrapper) entityResolver).getEntityResolver();
        } else if (str.equalsIgnoreCase(Constants.DOM_SCHEMA_TYPE)) {
            return getProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage");
        } else {
            if (str.equalsIgnoreCase(Constants.DOM_SCHEMA_LOCATION)) {
                return getProperty("http://java.sun.com/xml/jaxp/properties/schemaSource");
            }
            if (str.equalsIgnoreCase("http://apache.org/xml/properties/internal/symbol-table")) {
                return getProperty("http://apache.org/xml/properties/internal/symbol-table");
            }
            if (str.equalsIgnoreCase("http://apache.org/xml/properties/internal/grammar-pool")) {
                return getProperty("http://apache.org/xml/properties/internal/grammar-pool");
            }
            throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_FOUND", new Object[]{str}));
        }
    }

    public boolean canSetParameter(String str, Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof Boolean) {
            if (str.equalsIgnoreCase(Constants.DOM_COMMENTS) || str.equalsIgnoreCase(Constants.DOM_DATATYPE_NORMALIZATION) || str.equalsIgnoreCase(Constants.DOM_CDATA_SECTIONS) || str.equalsIgnoreCase(Constants.DOM_ENTITIES) || str.equalsIgnoreCase(Constants.DOM_SPLIT_CDATA) || str.equalsIgnoreCase("namespaces") || str.equalsIgnoreCase(Constants.DOM_VALIDATE) || str.equalsIgnoreCase(Constants.DOM_WELLFORMED) || str.equalsIgnoreCase(Constants.DOM_INFOSET) || str.equalsIgnoreCase(Constants.DOM_NAMESPACE_DECLARATIONS)) {
                return true;
            }
            if (str.equalsIgnoreCase(Constants.DOM_NORMALIZE_CHARACTERS) || str.equalsIgnoreCase(Constants.DOM_CANONICAL_FORM) || str.equalsIgnoreCase(Constants.DOM_VALIDATE_IF_SCHEMA) || str.equalsIgnoreCase(Constants.DOM_CHECK_CHAR_NORMALIZATION)) {
                return true ^ obj.equals(Boolean.TRUE);
            }
            if (str.equalsIgnoreCase(Constants.DOM_ELEMENT_CONTENT_WHITESPACE) || str.equalsIgnoreCase(SEND_PSVI)) {
                return obj.equals(Boolean.TRUE);
            }
            return false;
        } else if (str.equalsIgnoreCase(Constants.DOM_ERROR_HANDLER)) {
            return obj instanceof DOMErrorHandler;
        } else {
            if (str.equalsIgnoreCase(Constants.DOM_RESOURCE_RESOLVER)) {
                return obj instanceof LSResourceResolver;
            }
            if (str.equalsIgnoreCase(Constants.DOM_SCHEMA_LOCATION)) {
                return obj instanceof String;
            }
            if (str.equalsIgnoreCase(Constants.DOM_SCHEMA_TYPE)) {
                return (obj instanceof String) && obj.equals(Constants.NS_XMLSCHEMA);
            }
            if (str.equalsIgnoreCase("http://apache.org/xml/properties/internal/symbol-table")) {
                return obj instanceof SymbolTable;
            }
            if (str.equalsIgnoreCase("http://apache.org/xml/properties/internal/grammar-pool")) {
                return obj instanceof XMLGrammarPool;
            }
            return false;
        }
    }

    public DOMStringList getParameterNames() {
        if (this.fRecognizedParameters == null) {
            Vector vector = new Vector();
            vector.add(Constants.DOM_COMMENTS);
            vector.add(Constants.DOM_DATATYPE_NORMALIZATION);
            vector.add(Constants.DOM_CDATA_SECTIONS);
            vector.add(Constants.DOM_ENTITIES);
            vector.add(Constants.DOM_SPLIT_CDATA);
            vector.add("namespaces");
            vector.add(Constants.DOM_VALIDATE);
            vector.add(Constants.DOM_INFOSET);
            vector.add(Constants.DOM_NORMALIZE_CHARACTERS);
            vector.add(Constants.DOM_CANONICAL_FORM);
            vector.add(Constants.DOM_VALIDATE_IF_SCHEMA);
            vector.add(Constants.DOM_CHECK_CHAR_NORMALIZATION);
            vector.add(Constants.DOM_WELLFORMED);
            vector.add(Constants.DOM_NAMESPACE_DECLARATIONS);
            vector.add(Constants.DOM_ELEMENT_CONTENT_WHITESPACE);
            vector.add(Constants.DOM_ERROR_HANDLER);
            vector.add(Constants.DOM_SCHEMA_TYPE);
            vector.add(Constants.DOM_SCHEMA_LOCATION);
            vector.add(Constants.DOM_RESOURCE_RESOLVER);
            vector.add("http://apache.org/xml/properties/internal/grammar-pool");
            vector.add("http://apache.org/xml/properties/internal/symbol-table");
            vector.add(SEND_PSVI);
            this.fRecognizedParameters = new DOMStringListImpl(vector);
        }
        return this.fRecognizedParameters;
    }

    /* access modifiers changed from: protected */
    public void reset() throws XNIException {
        ValidationManager validationManager = this.fValidationManager;
        if (validationManager != null) {
            validationManager.reset();
        }
        int size = this.fComponents.size();
        for (int i = 0; i < size; i++) {
            ((XMLComponent) this.fComponents.get(i)).reset(this);
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings
    public PropertyState checkProperty(String str) throws XMLConfigurationException {
        if (!str.startsWith(Constants.SAX_PROPERTY_PREFIX) || str.length() - 30 != 10 || !str.endsWith(Constants.XML_STRING_PROPERTY)) {
            return super.checkProperty(str);
        }
        return PropertyState.NOT_SUPPORTED;
    }

    /* access modifiers changed from: protected */
    public void addComponent(XMLComponent xMLComponent) {
        if (!this.fComponents.contains(xMLComponent)) {
            this.fComponents.add(xMLComponent);
            addRecognizedFeatures(xMLComponent.getRecognizedFeatures());
            addRecognizedProperties(xMLComponent.getRecognizedProperties());
        }
    }

    /* access modifiers changed from: protected */
    public ValidationManager createValidationManager() {
        return new ValidationManager();
    }
}
