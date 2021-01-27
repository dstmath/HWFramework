package ohos.com.sun.org.apache.xerces.internal.parsers;

import java.io.StringReader;
import java.util.Locale;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMErrorImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMStringListImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.jaxp.JAXPConstants;
import ohos.com.sun.org.apache.xerces.internal.parsers.AbstractDOMParser;
import ohos.com.sun.org.apache.xerces.internal.util.DOMEntityResolverWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.DOMErrorHandlerWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.DOMUtil;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDContentModelSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;
import ohos.org.w3c.dom.DOMConfiguration;
import ohos.org.w3c.dom.DOMErrorHandler;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.DOMStringList;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.ls.LSException;
import ohos.org.w3c.dom.ls.LSInput;
import ohos.org.w3c.dom.ls.LSParser;
import ohos.org.w3c.dom.ls.LSParserFilter;
import ohos.org.w3c.dom.ls.LSResourceResolver;

public class DOMParserImpl extends AbstractDOMParser implements LSParser, DOMConfiguration {
    protected static final boolean DEBUG = false;
    protected static final String DISALLOW_DOCTYPE_DECL_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
    protected static final String DYNAMIC_VALIDATION = "http://apache.org/xml/features/validation/dynamic";
    protected static final String NAMESPACES = "http://xml.org/sax/features/namespaces";
    protected static final String NAMESPACE_GROWTH = "http://apache.org/xml/features/namespace-growth";
    protected static final String NORMALIZE_DATA = "http://apache.org/xml/features/validation/schema/normalized-value";
    protected static final String PSVI_AUGMENT = "http://apache.org/xml/features/validation/schema/augment-psvi";
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    protected static final String TOLERATE_DUPLICATES = "http://apache.org/xml/features/internal/tolerate-duplicates";
    protected static final String VALIDATION_FEATURE = "http://xml.org/sax/features/validation";
    protected static final String XMLSCHEMA = "http://apache.org/xml/features/validation/schema";
    protected static final String XMLSCHEMA_FULL_CHECKING = "http://apache.org/xml/features/validation/schema-full-checking";
    private AbortHandler abortHandler;
    private boolean abortNow;
    private Thread currentThread;
    protected boolean fBusy;
    protected boolean fNamespaceDeclarations;
    private DOMStringList fRecognizedParameters;
    private String fSchemaLocation;
    private Vector fSchemaLocations;
    protected String fSchemaType;

    public boolean getAsync() {
        return false;
    }

    public DOMConfiguration getDomConfig() {
        return this;
    }

    public DOMParserImpl(XMLParserConfiguration xMLParserConfiguration, String str) {
        this(xMLParserConfiguration);
        if (str == null) {
            return;
        }
        if (str.equals(Constants.NS_DTD)) {
            this.fConfiguration.setProperty(JAXPConstants.JAXP_SCHEMA_LANGUAGE, Constants.NS_DTD);
            this.fSchemaType = Constants.NS_DTD;
        } else if (str.equals(Constants.NS_XMLSCHEMA)) {
            this.fConfiguration.setProperty(JAXPConstants.JAXP_SCHEMA_LANGUAGE, Constants.NS_XMLSCHEMA);
        }
    }

    public DOMParserImpl(XMLParserConfiguration xMLParserConfiguration) {
        super(xMLParserConfiguration);
        this.fNamespaceDeclarations = true;
        this.fSchemaType = null;
        this.fBusy = false;
        this.abortNow = false;
        this.fSchemaLocations = new Vector();
        this.fSchemaLocation = null;
        this.abortHandler = null;
        this.fConfiguration.addRecognizedFeatures(new String[]{Constants.DOM_CANONICAL_FORM, Constants.DOM_CDATA_SECTIONS, Constants.DOM_CHARSET_OVERRIDES_XML_ENCODING, Constants.DOM_INFOSET, Constants.DOM_NAMESPACE_DECLARATIONS, Constants.DOM_SPLIT_CDATA, Constants.DOM_SUPPORTED_MEDIATYPES_ONLY, Constants.DOM_CERTIFIED, Constants.DOM_WELLFORMED, Constants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS});
        this.fConfiguration.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
        this.fConfiguration.setFeature(Constants.DOM_NAMESPACE_DECLARATIONS, true);
        this.fConfiguration.setFeature(Constants.DOM_WELLFORMED, true);
        this.fConfiguration.setFeature("http://apache.org/xml/features/include-comments", true);
        this.fConfiguration.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", true);
        this.fConfiguration.setFeature("http://xml.org/sax/features/namespaces", true);
        this.fConfiguration.setFeature(DYNAMIC_VALIDATION, false);
        this.fConfiguration.setFeature("http://apache.org/xml/features/dom/create-entity-ref-nodes", false);
        this.fConfiguration.setFeature("http://apache.org/xml/features/create-cdata-nodes", false);
        this.fConfiguration.setFeature(Constants.DOM_CANONICAL_FORM, false);
        this.fConfiguration.setFeature(Constants.DOM_CHARSET_OVERRIDES_XML_ENCODING, true);
        this.fConfiguration.setFeature(Constants.DOM_SPLIT_CDATA, true);
        this.fConfiguration.setFeature(Constants.DOM_SUPPORTED_MEDIATYPES_ONLY, false);
        this.fConfiguration.setFeature(Constants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS, true);
        this.fConfiguration.setFeature(Constants.DOM_CERTIFIED, true);
        try {
            this.fConfiguration.setFeature(NORMALIZE_DATA, false);
        } catch (XMLConfigurationException unused) {
        }
    }

    public DOMParserImpl(SymbolTable symbolTable) {
        this(new XIncludeAwareParserConfiguration());
        this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/symbol-table", symbolTable);
    }

    public DOMParserImpl(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool) {
        this(new XIncludeAwareParserConfiguration());
        this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/symbol-table", symbolTable);
        this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/grammar-pool", xMLGrammarPool);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractDOMParser, ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.parsers.XMLParser
    public void reset() {
        super.reset();
        this.fNamespaceDeclarations = this.fConfiguration.getFeature(Constants.DOM_NAMESPACE_DECLARATIONS);
        if (this.fSkippedElemStack != null) {
            this.fSkippedElemStack.removeAllElements();
        }
        this.fSchemaLocations.clear();
        this.fRejectedElementDepth = 0;
        this.fFilterReject = false;
        this.fSchemaType = null;
    }

    public LSParserFilter getFilter() {
        return this.fDOMFilter;
    }

    public void setFilter(LSParserFilter lSParserFilter) {
        this.fDOMFilter = lSParserFilter;
        if (this.fSkippedElemStack == null) {
            this.fSkippedElemStack = new Stack();
        }
    }

    public void setParameter(String str, Object obj) throws DOMException {
        boolean z = obj instanceof Boolean;
        String str2 = TOLERATE_DUPLICATES;
        if (z) {
            boolean booleanValue = ((Boolean) obj).booleanValue();
            try {
                if (str.equalsIgnoreCase(Constants.DOM_COMMENTS)) {
                    this.fConfiguration.setFeature("http://apache.org/xml/features/include-comments", booleanValue);
                } else if (str.equalsIgnoreCase(Constants.DOM_DATATYPE_NORMALIZATION)) {
                    this.fConfiguration.setFeature(NORMALIZE_DATA, booleanValue);
                } else if (str.equalsIgnoreCase(Constants.DOM_ENTITIES)) {
                    this.fConfiguration.setFeature("http://apache.org/xml/features/dom/create-entity-ref-nodes", booleanValue);
                } else if (str.equalsIgnoreCase(Constants.DOM_DISALLOW_DOCTYPE)) {
                    this.fConfiguration.setFeature(DISALLOW_DOCTYPE_DECL_FEATURE, booleanValue);
                } else {
                    if (!str.equalsIgnoreCase(Constants.DOM_SUPPORTED_MEDIATYPES_ONLY) && !str.equalsIgnoreCase(Constants.DOM_NORMALIZE_CHARACTERS) && !str.equalsIgnoreCase(Constants.DOM_CHECK_CHAR_NORMALIZATION)) {
                        if (!str.equalsIgnoreCase(Constants.DOM_CANONICAL_FORM)) {
                            if (str.equalsIgnoreCase("namespaces")) {
                                this.fConfiguration.setFeature("http://xml.org/sax/features/namespaces", booleanValue);
                                return;
                            } else if (str.equalsIgnoreCase(Constants.DOM_INFOSET)) {
                                if (booleanValue) {
                                    this.fConfiguration.setFeature("http://xml.org/sax/features/namespaces", true);
                                    this.fConfiguration.setFeature(Constants.DOM_NAMESPACE_DECLARATIONS, true);
                                    this.fConfiguration.setFeature("http://apache.org/xml/features/include-comments", true);
                                    this.fConfiguration.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", true);
                                    this.fConfiguration.setFeature(DYNAMIC_VALIDATION, false);
                                    this.fConfiguration.setFeature("http://apache.org/xml/features/dom/create-entity-ref-nodes", false);
                                    this.fConfiguration.setFeature(NORMALIZE_DATA, false);
                                    this.fConfiguration.setFeature("http://apache.org/xml/features/create-cdata-nodes", false);
                                    return;
                                }
                                return;
                            } else if (str.equalsIgnoreCase(Constants.DOM_CDATA_SECTIONS)) {
                                this.fConfiguration.setFeature("http://apache.org/xml/features/create-cdata-nodes", booleanValue);
                                return;
                            } else if (str.equalsIgnoreCase(Constants.DOM_NAMESPACE_DECLARATIONS)) {
                                this.fConfiguration.setFeature(Constants.DOM_NAMESPACE_DECLARATIONS, booleanValue);
                                return;
                            } else if (str.equalsIgnoreCase(Constants.DOM_WELLFORMED) || str.equalsIgnoreCase(Constants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS)) {
                                if (!booleanValue) {
                                    throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_SUPPORTED", new Object[]{str}));
                                }
                                return;
                            } else if (str.equalsIgnoreCase(Constants.DOM_VALIDATE)) {
                                this.fConfiguration.setFeature(VALIDATION_FEATURE, booleanValue);
                                if (this.fSchemaType != Constants.NS_DTD) {
                                    this.fConfiguration.setFeature(XMLSCHEMA, booleanValue);
                                    this.fConfiguration.setFeature(XMLSCHEMA_FULL_CHECKING, booleanValue);
                                }
                                if (booleanValue) {
                                    this.fConfiguration.setFeature(DYNAMIC_VALIDATION, false);
                                    return;
                                }
                                return;
                            } else if (str.equalsIgnoreCase(Constants.DOM_VALIDATE_IF_SCHEMA)) {
                                this.fConfiguration.setFeature(DYNAMIC_VALIDATION, booleanValue);
                                if (booleanValue) {
                                    this.fConfiguration.setFeature(VALIDATION_FEATURE, false);
                                    return;
                                }
                                return;
                            } else if (str.equalsIgnoreCase(Constants.DOM_ELEMENT_CONTENT_WHITESPACE)) {
                                this.fConfiguration.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", booleanValue);
                                return;
                            } else if (str.equalsIgnoreCase(Constants.DOM_PSVI)) {
                                this.fConfiguration.setFeature(PSVI_AUGMENT, true);
                                this.fConfiguration.setProperty("http://apache.org/xml/properties/dom/document-class-name", "ohos.com.sun.org.apache.xerces.internal.dom.PSVIDocumentImpl");
                                return;
                            } else {
                                if (str.equals(NAMESPACE_GROWTH)) {
                                    str2 = NAMESPACE_GROWTH;
                                } else if (!str.equals(str2)) {
                                    str2 = str.toLowerCase(Locale.ENGLISH);
                                }
                                this.fConfiguration.setFeature(str2, booleanValue);
                                return;
                            }
                        }
                    }
                    if (booleanValue) {
                        throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_SUPPORTED", new Object[]{str}));
                    }
                }
            } catch (XMLConfigurationException unused) {
                throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_FOUND", new Object[]{str}));
            }
        } else if (str.equalsIgnoreCase(Constants.DOM_ERROR_HANDLER)) {
            if ((obj instanceof DOMErrorHandler) || obj == null) {
                try {
                    this.fErrorHandler = new DOMErrorHandlerWrapper((DOMErrorHandler) obj);
                    this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/error-handler", this.fErrorHandler);
                } catch (XMLConfigurationException unused2) {
                }
            } else {
                throw new DOMException(17, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "TYPE_MISMATCH_ERR", new Object[]{str}));
            }
        } else if (str.equalsIgnoreCase(Constants.DOM_RESOURCE_RESOLVER)) {
            if ((obj instanceof LSResourceResolver) || obj == null) {
                this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/entity-resolver", new DOMEntityResolverWrapper((LSResourceResolver) obj));
                return;
            }
            throw new DOMException(17, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "TYPE_MISMATCH_ERR", new Object[]{str}));
        } else if (str.equalsIgnoreCase(Constants.DOM_SCHEMA_LOCATION)) {
            if (!(obj instanceof String) && obj != null) {
                throw new DOMException(17, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "TYPE_MISMATCH_ERR", new Object[]{str}));
            } else if (obj == null) {
                this.fSchemaLocation = null;
                this.fConfiguration.setProperty(JAXPConstants.JAXP_SCHEMA_SOURCE, null);
            } else {
                this.fSchemaLocation = (String) obj;
                StringTokenizer stringTokenizer = new StringTokenizer(this.fSchemaLocation, " \n\t\r");
                if (stringTokenizer.hasMoreTokens()) {
                    this.fSchemaLocations.clear();
                    this.fSchemaLocations.add(stringTokenizer.nextToken());
                    while (stringTokenizer.hasMoreTokens()) {
                        this.fSchemaLocations.add(stringTokenizer.nextToken());
                    }
                    this.fConfiguration.setProperty(JAXPConstants.JAXP_SCHEMA_SOURCE, this.fSchemaLocations.toArray());
                    return;
                }
                this.fConfiguration.setProperty(JAXPConstants.JAXP_SCHEMA_SOURCE, obj);
            }
        } else if (str.equalsIgnoreCase(Constants.DOM_SCHEMA_TYPE)) {
            if (!(obj instanceof String) && obj != null) {
                throw new DOMException(17, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "TYPE_MISMATCH_ERR", new Object[]{str}));
            } else if (obj == null) {
                this.fConfiguration.setFeature(XMLSCHEMA, false);
                this.fConfiguration.setFeature(XMLSCHEMA_FULL_CHECKING, false);
                this.fConfiguration.setProperty(JAXPConstants.JAXP_SCHEMA_LANGUAGE, null);
                this.fSchemaType = null;
            } else if (obj.equals(Constants.NS_XMLSCHEMA)) {
                this.fConfiguration.setFeature(XMLSCHEMA, true);
                this.fConfiguration.setFeature(XMLSCHEMA_FULL_CHECKING, true);
                this.fConfiguration.setProperty(JAXPConstants.JAXP_SCHEMA_LANGUAGE, Constants.NS_XMLSCHEMA);
                this.fSchemaType = Constants.NS_XMLSCHEMA;
            } else if (obj.equals(Constants.NS_DTD)) {
                this.fConfiguration.setFeature(XMLSCHEMA, false);
                this.fConfiguration.setFeature(XMLSCHEMA_FULL_CHECKING, false);
                this.fConfiguration.setProperty(JAXPConstants.JAXP_SCHEMA_LANGUAGE, Constants.NS_DTD);
                this.fSchemaType = Constants.NS_DTD;
            }
        } else if (str.equalsIgnoreCase("http://apache.org/xml/properties/dom/document-class-name")) {
            this.fConfiguration.setProperty("http://apache.org/xml/properties/dom/document-class-name", obj);
        } else {
            String lowerCase = str.toLowerCase(Locale.ENGLISH);
            try {
                this.fConfiguration.setProperty(lowerCase, obj);
            } catch (XMLConfigurationException unused3) {
                if (str.equals(NAMESPACE_GROWTH)) {
                    lowerCase = NAMESPACE_GROWTH;
                } else if (str.equals(str2)) {
                    lowerCase = str2;
                }
                this.fConfiguration.getFeature(lowerCase);
                throw newTypeMismatchError(str);
            } catch (XMLConfigurationException unused4) {
                throw newFeatureNotFoundError(str);
            }
        }
    }

    public Object getParameter(String str) throws DOMException {
        if (str.equalsIgnoreCase(Constants.DOM_COMMENTS)) {
            if (this.fConfiguration.getFeature("http://apache.org/xml/features/include-comments")) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        } else if (str.equalsIgnoreCase(Constants.DOM_DATATYPE_NORMALIZATION)) {
            if (this.fConfiguration.getFeature(NORMALIZE_DATA)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        } else if (str.equalsIgnoreCase(Constants.DOM_ENTITIES)) {
            if (this.fConfiguration.getFeature("http://apache.org/xml/features/dom/create-entity-ref-nodes")) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        } else if (str.equalsIgnoreCase("namespaces")) {
            if (this.fConfiguration.getFeature("http://xml.org/sax/features/namespaces")) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        } else if (str.equalsIgnoreCase(Constants.DOM_VALIDATE)) {
            if (this.fConfiguration.getFeature(VALIDATION_FEATURE)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        } else if (str.equalsIgnoreCase(Constants.DOM_VALIDATE_IF_SCHEMA)) {
            if (this.fConfiguration.getFeature(DYNAMIC_VALIDATION)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        } else if (str.equalsIgnoreCase(Constants.DOM_ELEMENT_CONTENT_WHITESPACE)) {
            if (this.fConfiguration.getFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace")) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        } else if (str.equalsIgnoreCase(Constants.DOM_DISALLOW_DOCTYPE)) {
            if (this.fConfiguration.getFeature(DISALLOW_DOCTYPE_DECL_FEATURE)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        } else if (str.equalsIgnoreCase(Constants.DOM_INFOSET)) {
            return this.fConfiguration.getFeature("http://xml.org/sax/features/namespaces") && this.fConfiguration.getFeature(Constants.DOM_NAMESPACE_DECLARATIONS) && this.fConfiguration.getFeature("http://apache.org/xml/features/include-comments") && this.fConfiguration.getFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace") && !this.fConfiguration.getFeature(DYNAMIC_VALIDATION) && !this.fConfiguration.getFeature("http://apache.org/xml/features/dom/create-entity-ref-nodes") && !this.fConfiguration.getFeature(NORMALIZE_DATA) && !this.fConfiguration.getFeature("http://apache.org/xml/features/create-cdata-nodes") ? Boolean.TRUE : Boolean.FALSE;
        } else if (str.equalsIgnoreCase(Constants.DOM_CDATA_SECTIONS)) {
            return this.fConfiguration.getFeature("http://apache.org/xml/features/create-cdata-nodes") ? Boolean.TRUE : Boolean.FALSE;
        } else {
            if (str.equalsIgnoreCase(Constants.DOM_CHECK_CHAR_NORMALIZATION) || str.equalsIgnoreCase(Constants.DOM_NORMALIZE_CHARACTERS)) {
                return Boolean.FALSE;
            }
            if (str.equalsIgnoreCase(Constants.DOM_NAMESPACE_DECLARATIONS) || str.equalsIgnoreCase(Constants.DOM_WELLFORMED) || str.equalsIgnoreCase(Constants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS) || str.equalsIgnoreCase(Constants.DOM_CANONICAL_FORM) || str.equalsIgnoreCase(Constants.DOM_SUPPORTED_MEDIATYPES_ONLY) || str.equalsIgnoreCase(Constants.DOM_SPLIT_CDATA) || str.equalsIgnoreCase(Constants.DOM_CHARSET_OVERRIDES_XML_ENCODING)) {
                if (this.fConfiguration.getFeature(str.toLowerCase(Locale.ENGLISH))) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            } else if (str.equalsIgnoreCase(Constants.DOM_ERROR_HANDLER)) {
                if (this.fErrorHandler != null) {
                    return this.fErrorHandler.getErrorHandler();
                }
                return null;
            } else if (str.equalsIgnoreCase(Constants.DOM_RESOURCE_RESOLVER)) {
                try {
                    XMLEntityResolver xMLEntityResolver = (XMLEntityResolver) this.fConfiguration.getProperty("http://apache.org/xml/properties/internal/entity-resolver");
                    if (xMLEntityResolver != null && (xMLEntityResolver instanceof DOMEntityResolverWrapper)) {
                        return ((DOMEntityResolverWrapper) xMLEntityResolver).getEntityResolver();
                    }
                } catch (XMLConfigurationException unused) {
                }
                return null;
            } else if (str.equalsIgnoreCase(Constants.DOM_SCHEMA_TYPE)) {
                return this.fConfiguration.getProperty(JAXPConstants.JAXP_SCHEMA_LANGUAGE);
            } else {
                if (str.equalsIgnoreCase(Constants.DOM_SCHEMA_LOCATION)) {
                    return this.fSchemaLocation;
                }
                if (str.equalsIgnoreCase("http://apache.org/xml/properties/internal/symbol-table")) {
                    return this.fConfiguration.getProperty("http://apache.org/xml/properties/internal/symbol-table");
                }
                if (str.equalsIgnoreCase("http://apache.org/xml/properties/dom/document-class-name")) {
                    return this.fConfiguration.getProperty("http://apache.org/xml/properties/dom/document-class-name");
                }
                String str2 = NAMESPACE_GROWTH;
                if (!str.equals(str2)) {
                    str2 = str.equals(TOLERATE_DUPLICATES) ? TOLERATE_DUPLICATES : str.toLowerCase(Locale.ENGLISH);
                }
                try {
                    return this.fConfiguration.getFeature(str2) ? Boolean.TRUE : Boolean.FALSE;
                } catch (XMLConfigurationException unused2) {
                    try {
                        return this.fConfiguration.getProperty(str2);
                    } catch (XMLConfigurationException unused3) {
                        throw newFeatureNotFoundError(str);
                    }
                }
            }
        }
    }

    public boolean canSetParameter(String str, Object obj) {
        String str2 = NAMESPACE_GROWTH;
        if (obj == null) {
            return true;
        }
        if (!(obj instanceof Boolean)) {
            return str.equalsIgnoreCase(Constants.DOM_ERROR_HANDLER) ? obj instanceof DOMErrorHandler : str.equalsIgnoreCase(Constants.DOM_RESOURCE_RESOLVER) ? obj instanceof LSResourceResolver : str.equalsIgnoreCase(Constants.DOM_SCHEMA_TYPE) ? (obj instanceof String) && (obj.equals(Constants.NS_XMLSCHEMA) || obj.equals(Constants.NS_DTD)) : str.equalsIgnoreCase(Constants.DOM_SCHEMA_LOCATION) ? obj instanceof String : str.equalsIgnoreCase("http://apache.org/xml/properties/dom/document-class-name");
        }
        boolean booleanValue = ((Boolean) obj).booleanValue();
        if (str.equalsIgnoreCase(Constants.DOM_SUPPORTED_MEDIATYPES_ONLY) || str.equalsIgnoreCase(Constants.DOM_NORMALIZE_CHARACTERS) || str.equalsIgnoreCase(Constants.DOM_CHECK_CHAR_NORMALIZATION) || str.equalsIgnoreCase(Constants.DOM_CANONICAL_FORM)) {
            return !booleanValue;
        }
        if (str.equalsIgnoreCase(Constants.DOM_WELLFORMED) || str.equalsIgnoreCase(Constants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS)) {
            return booleanValue;
        }
        if (str.equalsIgnoreCase(Constants.DOM_CDATA_SECTIONS) || str.equalsIgnoreCase(Constants.DOM_CHARSET_OVERRIDES_XML_ENCODING) || str.equalsIgnoreCase(Constants.DOM_COMMENTS) || str.equalsIgnoreCase(Constants.DOM_DATATYPE_NORMALIZATION) || str.equalsIgnoreCase(Constants.DOM_DISALLOW_DOCTYPE) || str.equalsIgnoreCase(Constants.DOM_ENTITIES) || str.equalsIgnoreCase(Constants.DOM_INFOSET) || str.equalsIgnoreCase("namespaces") || str.equalsIgnoreCase(Constants.DOM_NAMESPACE_DECLARATIONS) || str.equalsIgnoreCase(Constants.DOM_VALIDATE) || str.equalsIgnoreCase(Constants.DOM_VALIDATE_IF_SCHEMA) || str.equalsIgnoreCase(Constants.DOM_ELEMENT_CONTENT_WHITESPACE) || str.equalsIgnoreCase(Constants.DOM_XMLDECL)) {
            return true;
        }
        try {
            if (!str.equalsIgnoreCase(str2)) {
                str2 = str.equalsIgnoreCase(TOLERATE_DUPLICATES) ? TOLERATE_DUPLICATES : str.toLowerCase(Locale.ENGLISH);
            }
            this.fConfiguration.getFeature(str2);
            return true;
        } catch (XMLConfigurationException unused) {
            return false;
        }
    }

    public DOMStringList getParameterNames() {
        if (this.fRecognizedParameters == null) {
            Vector vector = new Vector();
            vector.add("namespaces");
            vector.add(Constants.DOM_CDATA_SECTIONS);
            vector.add(Constants.DOM_CANONICAL_FORM);
            vector.add(Constants.DOM_NAMESPACE_DECLARATIONS);
            vector.add(Constants.DOM_SPLIT_CDATA);
            vector.add(Constants.DOM_ENTITIES);
            vector.add(Constants.DOM_VALIDATE_IF_SCHEMA);
            vector.add(Constants.DOM_VALIDATE);
            vector.add(Constants.DOM_DATATYPE_NORMALIZATION);
            vector.add(Constants.DOM_CHARSET_OVERRIDES_XML_ENCODING);
            vector.add(Constants.DOM_CHECK_CHAR_NORMALIZATION);
            vector.add(Constants.DOM_SUPPORTED_MEDIATYPES_ONLY);
            vector.add(Constants.DOM_IGNORE_UNKNOWN_CHARACTER_DENORMALIZATIONS);
            vector.add(Constants.DOM_NORMALIZE_CHARACTERS);
            vector.add(Constants.DOM_WELLFORMED);
            vector.add(Constants.DOM_INFOSET);
            vector.add(Constants.DOM_DISALLOW_DOCTYPE);
            vector.add(Constants.DOM_ELEMENT_CONTENT_WHITESPACE);
            vector.add(Constants.DOM_COMMENTS);
            vector.add(Constants.DOM_ERROR_HANDLER);
            vector.add(Constants.DOM_RESOURCE_RESOLVER);
            vector.add(Constants.DOM_SCHEMA_LOCATION);
            vector.add(Constants.DOM_SCHEMA_TYPE);
            this.fRecognizedParameters = new DOMStringListImpl(vector);
        }
        return this.fRecognizedParameters;
    }

    public Document parseURI(String str) throws LSException {
        if (!this.fBusy) {
            XMLInputSource xMLInputSource = new XMLInputSource(null, str, null);
            try {
                this.currentThread = Thread.currentThread();
                this.fBusy = true;
                parse(xMLInputSource);
                this.fBusy = false;
                if (this.abortNow && this.currentThread.isInterrupted()) {
                    this.abortNow = false;
                    Thread.interrupted();
                }
            } catch (Exception e) {
                this.fBusy = false;
                if (this.abortNow && this.currentThread.isInterrupted()) {
                    Thread.interrupted();
                }
                if (this.abortNow) {
                    this.abortNow = false;
                    restoreHandlers();
                    return null;
                } else if (e != AbstractDOMParser.Abort.INSTANCE) {
                    if (!(e instanceof XMLParseException) && this.fErrorHandler != null) {
                        DOMErrorImpl dOMErrorImpl = new DOMErrorImpl();
                        dOMErrorImpl.fException = e;
                        dOMErrorImpl.fMessage = e.getMessage();
                        dOMErrorImpl.fSeverity = 3;
                        this.fErrorHandler.getErrorHandler().handleError(dOMErrorImpl);
                    }
                    throw DOMUtil.createLSException(81, e).fillInStackTrace();
                }
            }
            Document document = getDocument();
            dropDocumentReferences();
            return document;
        }
        throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
    }

    public Document parse(LSInput lSInput) throws LSException {
        XMLInputSource dom2xmlInputSource = dom2xmlInputSource(lSInput);
        if (!this.fBusy) {
            try {
                this.currentThread = Thread.currentThread();
                this.fBusy = true;
                parse(dom2xmlInputSource);
                this.fBusy = false;
                if (this.abortNow && this.currentThread.isInterrupted()) {
                    this.abortNow = false;
                    Thread.interrupted();
                }
            } catch (Exception e) {
                this.fBusy = false;
                if (this.abortNow && this.currentThread.isInterrupted()) {
                    Thread.interrupted();
                }
                if (this.abortNow) {
                    this.abortNow = false;
                    restoreHandlers();
                    return null;
                } else if (e != AbstractDOMParser.Abort.INSTANCE) {
                    if (!(e instanceof XMLParseException) && this.fErrorHandler != null) {
                        DOMErrorImpl dOMErrorImpl = new DOMErrorImpl();
                        dOMErrorImpl.fException = e;
                        dOMErrorImpl.fMessage = e.getMessage();
                        dOMErrorImpl.fSeverity = 3;
                        this.fErrorHandler.getErrorHandler().handleError(dOMErrorImpl);
                    }
                    throw DOMUtil.createLSException(81, e).fillInStackTrace();
                }
            }
            Document document = getDocument();
            dropDocumentReferences();
            return document;
        }
        throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
    }

    private void restoreHandlers() {
        this.fConfiguration.setDocumentHandler(this);
        this.fConfiguration.setDTDHandler(this);
        this.fConfiguration.setDTDContentModelHandler(this);
    }

    public Node parseWithContext(LSInput lSInput, Node node, short s) throws DOMException, LSException {
        throw new DOMException(9, "Not supported");
    }

    /* access modifiers changed from: package-private */
    public XMLInputSource dom2xmlInputSource(LSInput lSInput) {
        if (lSInput.getCharacterStream() != null) {
            return new XMLInputSource(lSInput.getPublicId(), lSInput.getSystemId(), lSInput.getBaseURI(), lSInput.getCharacterStream(), "UTF-16");
        }
        if (lSInput.getByteStream() != null) {
            return new XMLInputSource(lSInput.getPublicId(), lSInput.getSystemId(), lSInput.getBaseURI(), lSInput.getByteStream(), lSInput.getEncoding());
        }
        if (lSInput.getStringData() != null && lSInput.getStringData().length() > 0) {
            return new XMLInputSource(lSInput.getPublicId(), lSInput.getSystemId(), lSInput.getBaseURI(), new StringReader(lSInput.getStringData()), "UTF-16");
        }
        if ((lSInput.getSystemId() != null && lSInput.getSystemId().length() > 0) || (lSInput.getPublicId() != null && lSInput.getPublicId().length() > 0)) {
            return new XMLInputSource(lSInput.getPublicId(), lSInput.getSystemId(), lSInput.getBaseURI());
        }
        if (this.fErrorHandler != null) {
            DOMErrorImpl dOMErrorImpl = new DOMErrorImpl();
            dOMErrorImpl.fType = "no-input-specified";
            dOMErrorImpl.fMessage = "no-input-specified";
            dOMErrorImpl.fSeverity = 3;
            this.fErrorHandler.getErrorHandler().handleError(dOMErrorImpl);
        }
        throw new LSException(81, "no-input-specified");
    }

    public boolean getBusy() {
        return this.fBusy;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractDOMParser
    public void abort() {
        if (this.fBusy) {
            this.fBusy = false;
            if (this.currentThread != null) {
                this.abortNow = true;
                if (this.abortHandler == null) {
                    this.abortHandler = new AbortHandler();
                }
                this.fConfiguration.setDocumentHandler(this.abortHandler);
                this.fConfiguration.setDTDHandler(this.abortHandler);
                this.fConfiguration.setDTDContentModelHandler(this.abortHandler);
                if (this.currentThread != Thread.currentThread()) {
                    this.currentThread.interrupt();
                    return;
                }
                throw AbstractDOMParser.Abort.INSTANCE;
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractDOMParser, ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) {
        if (!this.fNamespaceDeclarations && this.fNamespaceAware) {
            for (int length = xMLAttributes.getLength() - 1; length >= 0; length--) {
                if (XMLSymbols.PREFIX_XMLNS == xMLAttributes.getPrefix(length) || XMLSymbols.PREFIX_XMLNS == xMLAttributes.getQName(length)) {
                    xMLAttributes.removeAttributeAt(length);
                }
            }
        }
        super.startElement(qName, xMLAttributes, augmentations);
    }

    private class AbortHandler implements XMLDocumentHandler, XMLDTDHandler, XMLDTDContentModelHandler {
        private XMLDocumentSource documentSource;
        private XMLDTDContentModelSource dtdContentSource;
        private XMLDTDSource dtdSource;

        private AbortHandler() {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void startDocument(XMLLocator xMLLocator, String str, NamespaceContext namespaceContext, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void xmlDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void doctypeDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void comment(XMLString xMLString, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void processingInstruction(String str, XMLString xMLString, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void startElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void emptyElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void startGeneralEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void textDecl(String str, String str2, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void endGeneralEntity(String str, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void characters(XMLString xMLString, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void ignorableWhitespace(XMLString xMLString, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void endElement(QName qName, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void startCDATA(Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void endCDATA(Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void endDocument(Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public void setDocumentSource(XMLDocumentSource xMLDocumentSource) {
            this.documentSource = xMLDocumentSource;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
        public XMLDocumentSource getDocumentSource() {
            return this.documentSource;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
        public void startDTD(XMLLocator xMLLocator, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
        public void startParameterEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
        public void endParameterEntity(String str, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
        public void startExternalSubset(XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
        public void endExternalSubset(Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
        public void elementDecl(String str, String str2, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
        public void startAttlist(String str, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
        public void attributeDecl(String str, String str2, String str3, String[] strArr, String str4, XMLString xMLString, XMLString xMLString2, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
        public void endAttlist(Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
        public void internalEntityDecl(String str, XMLString xMLString, XMLString xMLString2, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
        public void externalEntityDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
        public void unparsedEntityDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
        public void notationDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
        public void startConditional(short s, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
        public void ignoredCharacters(XMLString xMLString, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
        public void endConditional(Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
        public void endDTD(Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
        public void setDTDSource(XMLDTDSource xMLDTDSource) {
            this.dtdSource = xMLDTDSource;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
        public XMLDTDSource getDTDSource() {
            return this.dtdSource;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
        public void startContentModel(String str, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
        public void any(Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
        public void empty(Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
        public void startGroup(Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
        public void pcdata(Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
        public void element(String str, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
        public void separator(short s, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
        public void occurrence(short s, Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
        public void endGroup(Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
        public void endContentModel(Augmentations augmentations) throws XNIException {
            throw AbstractDOMParser.Abort.INSTANCE;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
        public void setDTDContentModelSource(XMLDTDContentModelSource xMLDTDContentModelSource) {
            this.dtdContentSource = xMLDTDContentModelSource;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
        public XMLDTDContentModelSource getDTDContentModelSource() {
            return this.dtdContentSource;
        }
    }

    private static DOMException newFeatureNotFoundError(String str) {
        return new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_FOUND", new Object[]{str}));
    }

    private static DOMException newTypeMismatchError(String str) {
        return new DOMException(17, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "TYPE_MISMATCH_ERR", new Object[]{str}));
    }
}
