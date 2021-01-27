package ohos.com.sun.org.apache.xerces.internal.xinclude;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Stack;
import java.util.StringTokenizer;
import ohos.ai.asr.util.AsrConstants;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.impl.io.MalformedByteSequenceException;
import ohos.com.sun.org.apache.xerces.internal.util.AugmentationsImpl;
import ohos.com.sun.org.apache.xerces.internal.util.HTTPInputSource;
import ohos.com.sun.org.apache.xerces.internal.util.IntStack;
import ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.URI;
import ohos.com.sun.org.apache.xerces.internal.util.XMLAttributesImpl;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDFilter;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;
import ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerHandler;
import ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerProcessor;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializerConstants;
import ohos.global.icu.impl.PatternTokenizer;

public class XIncludeHandler implements XMLComponent, XMLDocumentFilter, XMLDTDFilter {
    protected static final String ALLOW_UE_AND_NOTATION_EVENTS = "http://xml.org/sax/features/allow-dtd-events-after-endDTD";
    public static final String BUFFER_SIZE = "http://apache.org/xml/properties/input-buffer-size";
    public static final String CURRENT_BASE_URI = "currentBaseURI";
    protected static final String DYNAMIC_VALIDATION = "http://apache.org/xml/features/validation/dynamic";
    protected static final String ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    private static final Boolean[] FEATURE_DEFAULTS = {Boolean.TRUE, Boolean.TRUE, Boolean.TRUE};
    public static final String HTTP_ACCEPT = "Accept";
    public static final String HTTP_ACCEPT_LANGUAGE = "Accept-Language";
    private static final int INITIAL_SIZE = 8;
    public static final QName NEW_NS_ATTR_QNAME;
    protected static final String PARSER_SETTINGS = "http://apache.org/xml/features/internal/parser-settings";
    private static final Object[] PROPERTY_DEFAULTS = {null, null, null, new Integer(8192)};
    private static final String[] RECOGNIZED_FEATURES = {ALLOW_UE_AND_NOTATION_EVENTS, XINCLUDE_FIXUP_BASE_URIS, XINCLUDE_FIXUP_LANGUAGE};
    private static final String[] RECOGNIZED_PROPERTIES = {"http://apache.org/xml/properties/internal/error-reporter", "http://apache.org/xml/properties/internal/entity-resolver", "http://apache.org/xml/properties/security-manager", BUFFER_SIZE};
    protected static final String SCHEMA_VALIDATION = "http://apache.org/xml/features/validation/schema";
    protected static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    private static final int STATE_EXPECT_FALLBACK = 3;
    private static final int STATE_IGNORE = 2;
    private static final int STATE_NORMAL_PROCESSING = 1;
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    protected static final String VALIDATION = "http://xml.org/sax/features/validation";
    public static final String XINCLUDE_ATTR_ACCEPT = "accept".intern();
    public static final String XINCLUDE_ATTR_ACCEPT_LANGUAGE = "accept-language".intern();
    public static final String XINCLUDE_ATTR_ENCODING = Constants.ATTRNAME_OUTPUT_ENCODING.intern();
    public static final String XINCLUDE_ATTR_HREF = Constants.ATTRNAME_HREF.intern();
    public static final String XINCLUDE_ATTR_PARSE = "parse".intern();
    public static final String XINCLUDE_BASE = "base".intern();
    public static final String XINCLUDE_DEFAULT_CONFIGURATION = "ohos.com.sun.org.apache.xerces.internal.parsers.XIncludeParserConfiguration";
    public static final String XINCLUDE_FALLBACK = Constants.ELEMNAME_FALLBACK_STRING.intern();
    protected static final String XINCLUDE_FIXUP_BASE_URIS = "http://apache.org/xml/features/xinclude/fixup-base-uris";
    protected static final String XINCLUDE_FIXUP_LANGUAGE = "http://apache.org/xml/features/xinclude/fixup-language";
    public static final String XINCLUDE_INCLUDE = Constants.ELEMNAME_INCLUDE_STRING.intern();
    public static final String XINCLUDE_INCLUDED = "[included]".intern();
    public static final String XINCLUDE_LANG = "lang".intern();
    public static final String XINCLUDE_NS_URI = "http://www.w3.org/2001/XInclude".intern();
    public static final String XINCLUDE_PARSE_TEXT = "text".intern();
    public static final String XINCLUDE_PARSE_XML = "xml".intern();
    public static final QName XML_BASE_QNAME;
    public static final QName XML_LANG_QNAME;
    protected static final String XML_SECURITY_PROPERTY_MANAGER = "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager";
    public static final String XPOINTER = "xpointer";
    private static final char[] gAfterEscaping1 = new char[128];
    private static final char[] gAfterEscaping2 = new char[128];
    private static final char[] gHexChs = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final boolean[] gNeedEscaping = new boolean[128];
    protected Stack fBaseURI;
    protected IntStack fBaseURIScope;
    protected int fBufferSize = 8192;
    protected XMLParserConfiguration fChildConfig;
    protected XMLResourceIdentifier fCurrentBaseURI;
    protected String fCurrentLanguage;
    protected XMLDTDHandler fDTDHandler;
    protected XMLDTDSource fDTDSource;
    private int fDepth = 0;
    protected XMLLocator fDocLocation;
    protected XMLDocumentHandler fDocumentHandler;
    protected XMLDocumentSource fDocumentSource;
    protected XMLEntityResolver fEntityResolver;
    protected XMLErrorReporter fErrorReporter;
    protected Stack fExpandedSystemID;
    private boolean fFixupBaseURIs = true;
    private boolean fFixupLanguage = true;
    private boolean fInDTD;
    private boolean fIsXML11;
    protected IntStack fLanguageScope;
    protected Stack fLanguageStack;
    protected Stack fLiteralSystemID;
    protected XIncludeNamespaceSupport fNamespaceContext;
    private boolean fNeedCopyFeatures = true;
    private ArrayList fNotations;
    protected String fParentRelativeURI;
    protected XIncludeHandler fParentXIncludeHandler;
    private int fResultDepth;
    private boolean[] fSawFallback = new boolean[8];
    private boolean[] fSawInclude = new boolean[8];
    protected XMLSecurityManager fSecurityManager;
    protected XMLSecurityPropertyManager fSecurityPropertyMgr;
    private boolean fSeenRootElement;
    private boolean fSendUEAndNotationEvents;
    protected ParserConfigurationSettings fSettings;
    private int[] fState = new int[8];
    protected SymbolTable fSymbolTable;
    private ArrayList fUnparsedEntities;
    protected XIncludeTextReader fXInclude10TextReader;
    protected XIncludeTextReader fXInclude11TextReader;
    protected XMLParserConfiguration fXIncludeChildConfig;
    protected XIncludeMessageFormatter fXIncludeMessageFormatter = new XIncludeMessageFormatter();
    protected XMLParserConfiguration fXPointerChildConfig;
    protected XPointerProcessor fXPtrProcessor = null;

    static {
        XML_BASE_QNAME = new QName(XMLSymbols.PREFIX_XML, XINCLUDE_BASE, (XMLSymbols.PREFIX_XML + ":" + XINCLUDE_BASE).intern(), NamespaceContext.XML_URI);
        XML_LANG_QNAME = new QName(XMLSymbols.PREFIX_XML, XINCLUDE_LANG, (XMLSymbols.PREFIX_XML + ":" + XINCLUDE_LANG).intern(), NamespaceContext.XML_URI);
        NEW_NS_ATTR_QNAME = new QName(XMLSymbols.PREFIX_XMLNS, "", XMLSymbols.PREFIX_XMLNS + ":", NamespaceContext.XMLNS_URI);
        char[] cArr = {' ', '<', '>', '\"', '{', '}', '|', PatternTokenizer.BACK_SLASH, '^', '`'};
        for (char c : cArr) {
            gNeedEscaping[c] = true;
            char[] cArr2 = gAfterEscaping1;
            char[] cArr3 = gHexChs;
            cArr2[c] = cArr3[c >> 4];
            gAfterEscaping2[c] = cArr3[c & 15];
        }
    }

    public XIncludeHandler() {
        boolean[] zArr = this.fSawFallback;
        int i = this.fDepth;
        zArr[i] = false;
        this.fSawInclude[i] = false;
        this.fState[i] = 1;
        this.fNotations = new ArrayList();
        this.fUnparsedEntities = new ArrayList();
        this.fBaseURIScope = new IntStack();
        this.fBaseURI = new Stack();
        this.fLiteralSystemID = new Stack();
        this.fExpandedSystemID = new Stack();
        this.fCurrentBaseURI = new XMLResourceIdentifierImpl();
        this.fLanguageScope = new IntStack();
        this.fLanguageStack = new Stack();
        this.fCurrentLanguage = null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:103:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x0165  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x016e  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x0185 A[Catch:{ XMLConfigurationException -> 0x0199 }] */
    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void reset(XMLComponentManager xMLComponentManager) throws XNIException {
        XIncludeTextReader xIncludeTextReader;
        XIncludeTextReader xIncludeTextReader2;
        this.fNamespaceContext = null;
        this.fDepth = 0;
        this.fResultDepth = isRootDocument() ? 0 : this.fParentXIncludeHandler.getResultDepth();
        this.fNotations.clear();
        this.fUnparsedEntities.clear();
        this.fParentRelativeURI = null;
        this.fIsXML11 = false;
        this.fInDTD = false;
        this.fSeenRootElement = false;
        this.fBaseURIScope.clear();
        this.fBaseURI.clear();
        this.fLiteralSystemID.clear();
        this.fExpandedSystemID.clear();
        this.fLanguageScope.clear();
        this.fLanguageStack.clear();
        int i = 0;
        while (true) {
            int[] iArr = this.fState;
            if (i >= iArr.length) {
                break;
            }
            iArr[i] = 1;
            i++;
        }
        int i2 = 0;
        while (true) {
            boolean[] zArr = this.fSawFallback;
            if (i2 >= zArr.length) {
                break;
            }
            zArr[i2] = false;
            i2++;
        }
        int i3 = 0;
        while (true) {
            boolean[] zArr2 = this.fSawInclude;
            if (i3 >= zArr2.length) {
                break;
            }
            zArr2[i3] = false;
            i3++;
        }
        try {
            if (!xMLComponentManager.getFeature(PARSER_SETTINGS)) {
                return;
            }
        } catch (XMLConfigurationException unused) {
        }
        this.fNeedCopyFeatures = true;
        try {
            this.fSendUEAndNotationEvents = xMLComponentManager.getFeature(ALLOW_UE_AND_NOTATION_EVENTS);
            if (this.fChildConfig != null) {
                this.fChildConfig.setFeature(ALLOW_UE_AND_NOTATION_EVENTS, this.fSendUEAndNotationEvents);
            }
        } catch (XMLConfigurationException unused2) {
        }
        try {
            this.fFixupBaseURIs = xMLComponentManager.getFeature(XINCLUDE_FIXUP_BASE_URIS);
            if (this.fChildConfig != null) {
                this.fChildConfig.setFeature(XINCLUDE_FIXUP_BASE_URIS, this.fFixupBaseURIs);
            }
        } catch (XMLConfigurationException unused3) {
            this.fFixupBaseURIs = true;
        }
        try {
            this.fFixupLanguage = xMLComponentManager.getFeature(XINCLUDE_FIXUP_LANGUAGE);
            if (this.fChildConfig != null) {
                this.fChildConfig.setFeature(XINCLUDE_FIXUP_LANGUAGE, this.fFixupLanguage);
            }
        } catch (XMLConfigurationException unused4) {
            this.fFixupLanguage = true;
        }
        try {
            SymbolTable symbolTable = (SymbolTable) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/symbol-table");
            if (symbolTable != null) {
                this.fSymbolTable = symbolTable;
                if (this.fChildConfig != null) {
                    this.fChildConfig.setProperty("http://apache.org/xml/properties/internal/symbol-table", symbolTable);
                }
            }
        } catch (XMLConfigurationException unused5) {
            this.fSymbolTable = null;
        }
        try {
            XMLErrorReporter xMLErrorReporter = (XMLErrorReporter) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/error-reporter");
            if (xMLErrorReporter != null) {
                setErrorReporter(xMLErrorReporter);
                if (this.fChildConfig != null) {
                    this.fChildConfig.setProperty("http://apache.org/xml/properties/internal/error-reporter", xMLErrorReporter);
                }
            }
        } catch (XMLConfigurationException unused6) {
            this.fErrorReporter = null;
        }
        try {
            XMLEntityResolver xMLEntityResolver = (XMLEntityResolver) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/entity-resolver");
            if (xMLEntityResolver != null) {
                this.fEntityResolver = xMLEntityResolver;
                if (this.fChildConfig != null) {
                    this.fChildConfig.setProperty("http://apache.org/xml/properties/internal/entity-resolver", xMLEntityResolver);
                }
            }
        } catch (XMLConfigurationException unused7) {
            this.fEntityResolver = null;
        }
        try {
            XMLSecurityManager xMLSecurityManager = (XMLSecurityManager) xMLComponentManager.getProperty("http://apache.org/xml/properties/security-manager");
            if (xMLSecurityManager != null) {
                this.fSecurityManager = xMLSecurityManager;
                if (this.fChildConfig != null) {
                    this.fChildConfig.setProperty("http://apache.org/xml/properties/security-manager", xMLSecurityManager);
                }
            }
        } catch (XMLConfigurationException unused8) {
            this.fSecurityManager = null;
        }
        this.fSecurityPropertyMgr = (XMLSecurityPropertyManager) xMLComponentManager.getProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager");
        try {
            Integer num = (Integer) xMLComponentManager.getProperty(BUFFER_SIZE);
            if (num == null || num.intValue() <= 0) {
                this.fBufferSize = ((Integer) getPropertyDefault(BUFFER_SIZE)).intValue();
                xIncludeTextReader = this.fXInclude10TextReader;
                if (xIncludeTextReader != null) {
                    xIncludeTextReader.setBufferSize(this.fBufferSize);
                }
                xIncludeTextReader2 = this.fXInclude11TextReader;
                if (xIncludeTextReader2 != null) {
                    xIncludeTextReader2.setBufferSize(this.fBufferSize);
                }
                this.fSettings = new ParserConfigurationSettings();
                copyFeatures(xMLComponentManager, this.fSettings);
                try {
                    if (!xMLComponentManager.getFeature(SCHEMA_VALIDATION)) {
                        this.fSettings.setFeature(SCHEMA_VALIDATION, false);
                        if (xMLComponentManager.getFeature(VALIDATION)) {
                            this.fSettings.setFeature(DYNAMIC_VALIDATION, true);
                        }
                    }
                } catch (XMLConfigurationException unused9) {
                }
            } else {
                this.fBufferSize = num.intValue();
                if (this.fChildConfig != null) {
                    this.fChildConfig.setProperty(BUFFER_SIZE, num);
                }
                xIncludeTextReader = this.fXInclude10TextReader;
                if (xIncludeTextReader != null) {
                }
                xIncludeTextReader2 = this.fXInclude11TextReader;
                if (xIncludeTextReader2 != null) {
                }
                this.fSettings = new ParserConfigurationSettings();
                copyFeatures(xMLComponentManager, this.fSettings);
                if (!xMLComponentManager.getFeature(SCHEMA_VALIDATION)) {
                }
            }
        } catch (XMLConfigurationException unused10) {
            this.fBufferSize = ((Integer) getPropertyDefault(BUFFER_SIZE)).intValue();
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public String[] getRecognizedFeatures() {
        return (String[]) RECOGNIZED_FEATURES.clone();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setFeature(String str, boolean z) throws XMLConfigurationException {
        if (str.equals(ALLOW_UE_AND_NOTATION_EVENTS)) {
            this.fSendUEAndNotationEvents = z;
        }
        ParserConfigurationSettings parserConfigurationSettings = this.fSettings;
        if (parserConfigurationSettings != null) {
            this.fNeedCopyFeatures = true;
            parserConfigurationSettings.setFeature(str, z);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public String[] getRecognizedProperties() {
        return (String[]) RECOGNIZED_PROPERTIES.clone();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setProperty(String str, Object obj) throws XMLConfigurationException {
        if (str.equals("http://apache.org/xml/properties/internal/symbol-table")) {
            this.fSymbolTable = (SymbolTable) obj;
            XMLParserConfiguration xMLParserConfiguration = this.fChildConfig;
            if (xMLParserConfiguration != null) {
                xMLParserConfiguration.setProperty(str, obj);
            }
        } else if (str.equals("http://apache.org/xml/properties/internal/error-reporter")) {
            setErrorReporter((XMLErrorReporter) obj);
            XMLParserConfiguration xMLParserConfiguration2 = this.fChildConfig;
            if (xMLParserConfiguration2 != null) {
                xMLParserConfiguration2.setProperty(str, obj);
            }
        } else if (str.equals("http://apache.org/xml/properties/internal/entity-resolver")) {
            this.fEntityResolver = (XMLEntityResolver) obj;
            XMLParserConfiguration xMLParserConfiguration3 = this.fChildConfig;
            if (xMLParserConfiguration3 != null) {
                xMLParserConfiguration3.setProperty(str, obj);
            }
        } else if (str.equals("http://apache.org/xml/properties/security-manager")) {
            this.fSecurityManager = (XMLSecurityManager) obj;
            XMLParserConfiguration xMLParserConfiguration4 = this.fChildConfig;
            if (xMLParserConfiguration4 != null) {
                xMLParserConfiguration4.setProperty(str, obj);
            }
        } else if (str.equals("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager")) {
            this.fSecurityPropertyMgr = (XMLSecurityPropertyManager) obj;
            XMLParserConfiguration xMLParserConfiguration5 = this.fChildConfig;
            if (xMLParserConfiguration5 != null) {
                xMLParserConfiguration5.setProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", obj);
            }
        } else if (str.equals(BUFFER_SIZE)) {
            Integer num = (Integer) obj;
            XMLParserConfiguration xMLParserConfiguration6 = this.fChildConfig;
            if (xMLParserConfiguration6 != null) {
                xMLParserConfiguration6.setProperty(str, obj);
            }
            if (num != null && num.intValue() > 0) {
                this.fBufferSize = num.intValue();
                XIncludeTextReader xIncludeTextReader = this.fXInclude10TextReader;
                if (xIncludeTextReader != null) {
                    xIncludeTextReader.setBufferSize(this.fBufferSize);
                }
                XIncludeTextReader xIncludeTextReader2 = this.fXInclude11TextReader;
                if (xIncludeTextReader2 != null) {
                    xIncludeTextReader2.setBufferSize(this.fBufferSize);
                }
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public Boolean getFeatureDefault(String str) {
        int i = 0;
        while (true) {
            String[] strArr = RECOGNIZED_FEATURES;
            if (i >= strArr.length) {
                return null;
            }
            if (strArr[i].equals(str)) {
                return FEATURE_DEFAULTS[i];
            }
            i++;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public Object getPropertyDefault(String str) {
        int i = 0;
        while (true) {
            String[] strArr = RECOGNIZED_PROPERTIES;
            if (i >= strArr.length) {
                return null;
            }
            if (strArr[i].equals(str)) {
                return PROPERTY_DEFAULTS[i];
            }
            i++;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource
    public void setDocumentHandler(XMLDocumentHandler xMLDocumentHandler) {
        this.fDocumentHandler = xMLDocumentHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource
    public XMLDocumentHandler getDocumentHandler() {
        return this.fDocumentHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startDocument(XMLLocator xMLLocator, String str, NamespaceContext namespaceContext, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler;
        this.fErrorReporter.setDocumentLocator(xMLLocator);
        if (!isRootDocument() && this.fParentXIncludeHandler.searchForRecursiveIncludes(xMLLocator)) {
            reportFatalError("RecursiveInclude", new Object[]{xMLLocator.getExpandedSystemId()});
        }
        if (!(namespaceContext instanceof XIncludeNamespaceSupport)) {
            reportFatalError("IncompatibleNamespaceContext");
        }
        this.fNamespaceContext = (XIncludeNamespaceSupport) namespaceContext;
        this.fDocLocation = xMLLocator;
        this.fCurrentBaseURI.setBaseSystemId(xMLLocator.getBaseSystemId());
        this.fCurrentBaseURI.setExpandedSystemId(xMLLocator.getExpandedSystemId());
        this.fCurrentBaseURI.setLiteralSystemId(xMLLocator.getLiteralSystemId());
        saveBaseURI();
        if (augmentations == null) {
            augmentations = new AugmentationsImpl();
        }
        augmentations.putItem(CURRENT_BASE_URI, this.fCurrentBaseURI);
        this.fCurrentLanguage = XMLSymbols.EMPTY_STRING;
        saveLanguage(this.fCurrentLanguage);
        if (isRootDocument() && (xMLDocumentHandler = this.fDocumentHandler) != null) {
            xMLDocumentHandler.startDocument(xMLLocator, str, namespaceContext, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void xmlDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler;
        this.fIsXML11 = SerializerConstants.XMLVERSION11.equals(str);
        if (isRootDocument() && (xMLDocumentHandler = this.fDocumentHandler) != null) {
            xMLDocumentHandler.xmlDecl(str, str2, str3, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void doctypeDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler;
        if (isRootDocument() && (xMLDocumentHandler = this.fDocumentHandler) != null) {
            xMLDocumentHandler.doctypeDecl(str, str2, str3, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void comment(XMLString xMLString, Augmentations augmentations) throws XNIException {
        if (this.fInDTD) {
            XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
            if (xMLDTDHandler != null) {
                xMLDTDHandler.comment(xMLString, augmentations);
            }
        } else if (this.fDocumentHandler != null && getState() == 1) {
            this.fDepth++;
            this.fDocumentHandler.comment(xMLString, modifyAugmentations(augmentations));
            this.fDepth--;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void processingInstruction(String str, XMLString xMLString, Augmentations augmentations) throws XNIException {
        if (this.fInDTD) {
            XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
            if (xMLDTDHandler != null) {
                xMLDTDHandler.processingInstruction(str, xMLString, augmentations);
            }
        } else if (this.fDocumentHandler != null && getState() == 1) {
            this.fDepth++;
            this.fDocumentHandler.processingInstruction(str, xMLString, modifyAugmentations(augmentations));
            this.fDepth--;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        this.fDepth++;
        int state = getState(this.fDepth - 1);
        if (state == 3 && getState(this.fDepth - 2) == 3) {
            setState(2);
        } else {
            setState(state);
        }
        processXMLBaseAttributes(xMLAttributes);
        if (this.fFixupLanguage) {
            processXMLLangAttributes(xMLAttributes);
        }
        if (isIncludeElement(qName)) {
            if (handleIncludeElement(xMLAttributes)) {
                setState(2);
            } else {
                setState(3);
            }
        } else if (isFallbackElement(qName)) {
            handleFallbackElement();
        } else if (hasXIncludeNamespace(qName)) {
            if (getSawInclude(this.fDepth - 1)) {
                reportFatalError("IncludeChild", new Object[]{qName.rawname});
            }
            if (getSawFallback(this.fDepth - 1)) {
                reportFatalError("FallbackChild", new Object[]{qName.rawname});
            }
            if (getState() == 1) {
                int i = this.fResultDepth;
                this.fResultDepth = i + 1;
                if (i == 0) {
                    checkMultipleRootElements();
                }
                if (this.fDocumentHandler != null) {
                    this.fDocumentHandler.startElement(qName, processAttributes(xMLAttributes), modifyAugmentations(augmentations));
                }
            }
        } else if (getState() == 1) {
            int i2 = this.fResultDepth;
            this.fResultDepth = i2 + 1;
            if (i2 == 0) {
                checkMultipleRootElements();
            }
            if (this.fDocumentHandler != null) {
                this.fDocumentHandler.startElement(qName, processAttributes(xMLAttributes), modifyAugmentations(augmentations));
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void emptyElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        this.fDepth++;
        int state = getState(this.fDepth - 1);
        if (state == 3 && getState(this.fDepth - 2) == 3) {
            setState(2);
        } else {
            setState(state);
        }
        processXMLBaseAttributes(xMLAttributes);
        if (this.fFixupLanguage) {
            processXMLLangAttributes(xMLAttributes);
        }
        if (isIncludeElement(qName)) {
            if (handleIncludeElement(xMLAttributes)) {
                setState(2);
            } else {
                reportFatalError("NoFallback", new Object[]{xMLAttributes.getValue(null, Constants.ATTRNAME_HREF)});
            }
        } else if (isFallbackElement(qName)) {
            handleFallbackElement();
        } else if (hasXIncludeNamespace(qName)) {
            if (getSawInclude(this.fDepth - 1)) {
                reportFatalError("IncludeChild", new Object[]{qName.rawname});
            }
            if (getSawFallback(this.fDepth - 1)) {
                reportFatalError("FallbackChild", new Object[]{qName.rawname});
            }
            if (getState() == 1) {
                if (this.fResultDepth == 0) {
                    checkMultipleRootElements();
                }
                if (this.fDocumentHandler != null) {
                    this.fDocumentHandler.emptyElement(qName, processAttributes(xMLAttributes), modifyAugmentations(augmentations));
                }
            }
        } else if (getState() == 1) {
            if (this.fResultDepth == 0) {
                checkMultipleRootElements();
            }
            if (this.fDocumentHandler != null) {
                this.fDocumentHandler.emptyElement(qName, processAttributes(xMLAttributes), modifyAugmentations(augmentations));
            }
        }
        setSawFallback(this.fDepth + 1, false);
        setSawInclude(this.fDepth, false);
        if (this.fBaseURIScope.size() > 0 && this.fDepth == this.fBaseURIScope.peek()) {
            restoreBaseURI();
        }
        this.fDepth--;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endElement(QName qName, Augmentations augmentations) throws XNIException {
        if (isIncludeElement(qName) && getState() == 3 && !getSawFallback(this.fDepth + 1)) {
            reportFatalError("NoFallback", new Object[]{"unknown"});
        }
        if (isFallbackElement(qName)) {
            if (getState() == 1) {
                setState(2);
            }
        } else if (getState() == 1) {
            this.fResultDepth--;
            XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
            if (xMLDocumentHandler != null) {
                xMLDocumentHandler.endElement(qName, augmentations);
            }
        }
        setSawFallback(this.fDepth + 1, false);
        setSawInclude(this.fDepth, false);
        if (this.fBaseURIScope.size() > 0 && this.fDepth == this.fBaseURIScope.peek()) {
            restoreBaseURI();
        }
        if (this.fLanguageScope.size() > 0 && this.fDepth == this.fLanguageScope.peek()) {
            this.fCurrentLanguage = restoreLanguage();
        }
        this.fDepth--;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startGeneralEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
        if (getState() != 1) {
            return;
        }
        if (this.fResultDepth != 0) {
            XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
            if (xMLDocumentHandler != null) {
                xMLDocumentHandler.startGeneralEntity(str, xMLResourceIdentifier, str2, augmentations);
            }
        } else if (augmentations != null && Boolean.TRUE.equals(augmentations.getItem(ohos.com.sun.org.apache.xerces.internal.impl.Constants.ENTITY_SKIPPED))) {
            reportFatalError("UnexpandedEntityReferenceIllegal");
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void textDecl(String str, String str2, Augmentations augmentations) throws XNIException {
        if (this.fDocumentHandler != null && getState() == 1) {
            this.fDocumentHandler.textDecl(str, str2, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endGeneralEntity(String str, Augmentations augmentations) throws XNIException {
        if (this.fDocumentHandler != null && getState() == 1 && this.fResultDepth != 0) {
            this.fDocumentHandler.endGeneralEntity(str, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void characters(XMLString xMLString, Augmentations augmentations) throws XNIException {
        if (getState() != 1) {
            return;
        }
        if (this.fResultDepth == 0) {
            checkWhitespace(xMLString);
        } else if (this.fDocumentHandler != null) {
            this.fDepth++;
            this.fDocumentHandler.characters(xMLString, modifyAugmentations(augmentations));
            this.fDepth--;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void ignorableWhitespace(XMLString xMLString, Augmentations augmentations) throws XNIException {
        if (this.fDocumentHandler != null && getState() == 1 && this.fResultDepth != 0) {
            this.fDocumentHandler.ignorableWhitespace(xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startCDATA(Augmentations augmentations) throws XNIException {
        if (this.fDocumentHandler != null && getState() == 1 && this.fResultDepth != 0) {
            this.fDocumentHandler.startCDATA(augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endCDATA(Augmentations augmentations) throws XNIException {
        if (this.fDocumentHandler != null && getState() == 1 && this.fResultDepth != 0) {
            this.fDocumentHandler.endCDATA(augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endDocument(Augmentations augmentations) throws XNIException {
        if (isRootDocument()) {
            if (!this.fSeenRootElement) {
                reportFatalError("RootElementRequired");
            }
            XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
            if (xMLDocumentHandler != null) {
                xMLDocumentHandler.endDocument(augmentations);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void setDocumentSource(XMLDocumentSource xMLDocumentSource) {
        this.fDocumentSource = xMLDocumentSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public XMLDocumentSource getDocumentSource() {
        return this.fDocumentSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void attributeDecl(String str, String str2, String str3, String[] strArr, String str4, XMLString xMLString, XMLString xMLString2, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.attributeDecl(str, str2, str3, strArr, str4, xMLString, xMLString2, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void elementDecl(String str, String str2, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.elementDecl(str, str2, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endAttlist(Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.endAttlist(augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endConditional(Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.endConditional(augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endDTD(Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.endDTD(augmentations);
        }
        this.fInDTD = false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endExternalSubset(Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.endExternalSubset(augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endParameterEntity(String str, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.endParameterEntity(str, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void externalEntityDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.externalEntityDecl(str, xMLResourceIdentifier, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public XMLDTDSource getDTDSource() {
        return this.fDTDSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void ignoredCharacters(XMLString xMLString, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.ignoredCharacters(xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void internalEntityDecl(String str, XMLString xMLString, XMLString xMLString2, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.internalEntityDecl(str, xMLString, xMLString2, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void notationDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
        addNotation(str, xMLResourceIdentifier, augmentations);
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.notationDecl(str, xMLResourceIdentifier, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void setDTDSource(XMLDTDSource xMLDTDSource) {
        this.fDTDSource = xMLDTDSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startAttlist(String str, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.startAttlist(str, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startConditional(short s, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.startConditional(s, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startDTD(XMLLocator xMLLocator, Augmentations augmentations) throws XNIException {
        this.fInDTD = true;
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.startDTD(xMLLocator, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startExternalSubset(XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.startExternalSubset(xMLResourceIdentifier, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startParameterEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.startParameterEntity(str, xMLResourceIdentifier, str2, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void unparsedEntityDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
        addUnparsedEntity(str, xMLResourceIdentifier, str2, augmentations);
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.unparsedEntityDecl(str, xMLResourceIdentifier, str2, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource
    public XMLDTDHandler getDTDHandler() {
        return this.fDTDHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource
    public void setDTDHandler(XMLDTDHandler xMLDTDHandler) {
        this.fDTDHandler = xMLDTDHandler;
    }

    private void setErrorReporter(XMLErrorReporter xMLErrorReporter) {
        this.fErrorReporter = xMLErrorReporter;
        XMLErrorReporter xMLErrorReporter2 = this.fErrorReporter;
        if (xMLErrorReporter2 != null) {
            xMLErrorReporter2.putMessageFormatter(XIncludeMessageFormatter.XINCLUDE_DOMAIN, this.fXIncludeMessageFormatter);
            XMLLocator xMLLocator = this.fDocLocation;
            if (xMLLocator != null) {
                this.fErrorReporter.setDocumentLocator(xMLLocator);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleFallbackElement() {
        if (!getSawInclude(this.fDepth - 1)) {
            if (getState() != 2) {
                reportFatalError("FallbackParent");
            } else {
                return;
            }
        }
        setSawInclude(this.fDepth, false);
        this.fNamespaceContext.setContextInvalid();
        if (getSawFallback(this.fDepth)) {
            reportFatalError("MultipleFallbacks");
        } else {
            setSawFallback(this.fDepth, true);
        }
        if (getState() == 3) {
            setState(1);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:180:0x0382, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:181:0x0383, code lost:
        r18 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:182:0x0387, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:183:0x0388, code lost:
        r18 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:188:?, code lost:
        r18.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:189:0x039d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:190:0x039e, code lost:
        reportResourceError("TextResourceError", new java.lang.Object[]{r15, r0.getMessage()});
     */
    /* JADX WARNING: Code restructure failed: missing block: B:192:0x03ad, code lost:
        r1 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:199:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:200:0x03be, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:201:0x03bf, code lost:
        reportResourceError("TextResourceError", new java.lang.Object[]{r15, r0.getMessage()});
     */
    /* JADX WARNING: Code restructure failed: missing block: B:202:0x03cd, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:205:0x03d2, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:206:0x03d3, code lost:
        r18 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:210:?, code lost:
        r18.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:211:0x03ed, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:212:0x03ee, code lost:
        reportResourceError("TextResourceError", new java.lang.Object[]{r15, r0.getMessage()});
     */
    /* JADX WARNING: Code restructure failed: missing block: B:213:0x03fc, code lost:
        return false;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x0382 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:152:0x0329] */
    /* JADX WARNING: Removed duplicated region for block: B:182:0x0387 A[ExcHandler: IOException (e java.io.IOException), Splitter:B:152:0x0329] */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0399 A[SYNTHETIC, Splitter:B:187:0x0399] */
    /* JADX WARNING: Removed duplicated region for block: B:193:? A[ExcHandler: CharConversionException (unused java.io.CharConversionException), SYNTHETIC, Splitter:B:152:0x0329] */
    /* JADX WARNING: Removed duplicated region for block: B:198:0x03ba A[SYNTHETIC, Splitter:B:198:0x03ba] */
    /* JADX WARNING: Removed duplicated region for block: B:209:0x03e9 A[SYNTHETIC, Splitter:B:209:0x03e9] */
    /* JADX WARNING: Removed duplicated region for block: B:216:0x0400 A[SYNTHETIC, Splitter:B:216:0x0400] */
    public boolean handleIncludeElement(XMLAttributes xMLAttributes) throws XNIException {
        String str;
        String str2;
        String str3;
        String str4;
        XMLInputSource xMLInputSource;
        XIncludeTextReader xIncludeTextReader;
        Throwable th;
        XIncludeTextReader xIncludeTextReader2;
        MalformedByteSequenceException e;
        XIncludeTextReader xIncludeTextReader3;
        IOException e2;
        if (getSawInclude(this.fDepth - 1)) {
            reportFatalError("IncludeChild", new Object[]{XINCLUDE_INCLUDE});
        }
        if (getState() == 2) {
            return true;
        }
        setSawInclude(this.fDepth, true);
        this.fNamespaceContext.setContextInvalid();
        String value = xMLAttributes.getValue(XINCLUDE_ATTR_HREF);
        String value2 = xMLAttributes.getValue(XINCLUDE_ATTR_PARSE);
        String value3 = xMLAttributes.getValue(XPOINTER);
        String value4 = xMLAttributes.getValue(XINCLUDE_ATTR_ACCEPT);
        String value5 = xMLAttributes.getValue(XINCLUDE_ATTR_ACCEPT_LANGUAGE);
        if (value2 == null) {
            value2 = XINCLUDE_PARSE_XML;
        }
        if (value == null) {
            value = XMLSymbols.EMPTY_STRING;
        }
        if (value.length() == 0 && XINCLUDE_PARSE_XML.equals(value2)) {
            if (value3 == null) {
                reportFatalError("XpointerMissing");
            } else {
                XMLErrorReporter xMLErrorReporter = this.fErrorReporter;
                reportResourceError("XMLResourceError", new Object[]{value, this.fXIncludeMessageFormatter.formatMessage(xMLErrorReporter != null ? xMLErrorReporter.getLocale() : null, "XPointerStreamability", null)});
                return false;
            }
        }
        try {
            if (new URI(value, true).getFragment() != null) {
                reportFatalError("HrefFragmentIdentifierIllegal", new Object[]{value});
            }
        } catch (URI.MalformedURIException unused) {
            String escapeHref = escapeHref(value);
            if (value != escapeHref) {
                try {
                    if (new URI(escapeHref, true).getFragment() != null) {
                        reportFatalError("HrefFragmentIdentifierIllegal", new Object[]{escapeHref});
                    }
                } catch (URI.MalformedURIException unused2) {
                    reportFatalError("HrefSyntacticallyInvalid", new Object[]{escapeHref});
                }
                str = escapeHref;
            } else {
                reportFatalError("HrefSyntacticallyInvalid", new Object[]{value});
            }
        }
        str = value;
        if (value4 == null || isValidInHTTPHeader(value4)) {
            str2 = value4;
        } else {
            reportFatalError("AcceptMalformed", null);
            str2 = null;
        }
        if (value5 == null || isValidInHTTPHeader(value5)) {
            str3 = value5;
        } else {
            reportFatalError("AcceptLanguageMalformed", null);
            str3 = null;
        }
        if (this.fEntityResolver != null) {
            try {
                xMLInputSource = this.fEntityResolver.resolveEntity(new XMLResourceIdentifierImpl(null, str, this.fCurrentBaseURI.getExpandedSystemId(), XMLEntityManager.expandSystemId(str, this.fCurrentBaseURI.getExpandedSystemId(), false)));
                if (xMLInputSource == null || (xMLInputSource instanceof HTTPInputSource) || !((str2 != null || str3 != null) && xMLInputSource.getCharacterStream() == null && xMLInputSource.getByteStream() == null)) {
                    str4 = str;
                } else {
                    str4 = str;
                    try {
                        xMLInputSource = createInputSource(xMLInputSource.getPublicId(), xMLInputSource.getSystemId(), xMLInputSource.getBaseSystemId(), str2, str3);
                    } catch (IOException e3) {
                        e2 = e3;
                    }
                }
            } catch (IOException e4) {
                e2 = e4;
                str4 = str;
                reportResourceError("XMLResourceError", new Object[]{str4, e2.getMessage()});
                return false;
            }
        } else {
            str4 = str;
            xMLInputSource = null;
        }
        if (xMLInputSource == null) {
            if (str2 == null && str3 == null) {
                xMLInputSource = new XMLInputSource(null, str4, this.fCurrentBaseURI.getExpandedSystemId());
            } else {
                xMLInputSource = createInputSource(null, str4, this.fCurrentBaseURI.getExpandedSystemId(), str2, str3);
            }
        }
        if (value2.equals(XINCLUDE_PARSE_XML)) {
            if ((value3 != null && this.fXPointerChildConfig == null) || (value3 == null && this.fXIncludeChildConfig == null)) {
                this.fChildConfig = (XMLParserConfiguration) ObjectFactory.newInstance(value3 != null ? "ohos.com.sun.org.apache.xerces.internal.parsers.XPointerParserConfiguration" : XINCLUDE_DEFAULT_CONFIGURATION, true);
                SymbolTable symbolTable = this.fSymbolTable;
                if (symbolTable != null) {
                    this.fChildConfig.setProperty("http://apache.org/xml/properties/internal/symbol-table", symbolTable);
                }
                XMLErrorReporter xMLErrorReporter2 = this.fErrorReporter;
                if (xMLErrorReporter2 != null) {
                    this.fChildConfig.setProperty("http://apache.org/xml/properties/internal/error-reporter", xMLErrorReporter2);
                }
                XMLEntityResolver xMLEntityResolver = this.fEntityResolver;
                if (xMLEntityResolver != null) {
                    this.fChildConfig.setProperty("http://apache.org/xml/properties/internal/entity-resolver", xMLEntityResolver);
                }
                this.fChildConfig.setProperty("http://apache.org/xml/properties/security-manager", this.fSecurityManager);
                this.fChildConfig.setProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", this.fSecurityPropertyMgr);
                this.fChildConfig.setProperty(BUFFER_SIZE, new Integer(this.fBufferSize));
                this.fNeedCopyFeatures = true;
                this.fChildConfig.setProperty("http://apache.org/xml/properties/internal/namespace-context", this.fNamespaceContext);
                this.fChildConfig.setFeature(XINCLUDE_FIXUP_BASE_URIS, this.fFixupBaseURIs);
                this.fChildConfig.setFeature(XINCLUDE_FIXUP_LANGUAGE, this.fFixupLanguage);
                if (value3 != null) {
                    XPointerHandler xPointerHandler = (XPointerHandler) this.fChildConfig.getProperty("http://apache.org/xml/properties/internal/xpointer-handler");
                    this.fXPtrProcessor = xPointerHandler;
                    ((XPointerHandler) this.fXPtrProcessor).setProperty("http://apache.org/xml/properties/internal/namespace-context", this.fNamespaceContext);
                    ((XPointerHandler) this.fXPtrProcessor).setProperty(XINCLUDE_FIXUP_BASE_URIS, Boolean.valueOf(this.fFixupBaseURIs));
                    ((XPointerHandler) this.fXPtrProcessor).setProperty(XINCLUDE_FIXUP_LANGUAGE, Boolean.valueOf(this.fFixupLanguage));
                    XMLErrorReporter xMLErrorReporter3 = this.fErrorReporter;
                    if (xMLErrorReporter3 != null) {
                        ((XPointerHandler) this.fXPtrProcessor).setProperty("http://apache.org/xml/properties/internal/error-reporter", xMLErrorReporter3);
                    }
                    xPointerHandler.setParent(this);
                    xPointerHandler.setDocumentHandler(getDocumentHandler());
                    this.fXPointerChildConfig = this.fChildConfig;
                } else {
                    XIncludeHandler xIncludeHandler = (XIncludeHandler) this.fChildConfig.getProperty("http://apache.org/xml/properties/internal/xinclude-handler");
                    xIncludeHandler.setParent(this);
                    xIncludeHandler.setDocumentHandler(getDocumentHandler());
                    this.fXIncludeChildConfig = this.fChildConfig;
                }
            }
            if (value3 != null) {
                this.fChildConfig = this.fXPointerChildConfig;
                try {
                    this.fXPtrProcessor.parseXPointer(value3);
                } catch (XNIException e5) {
                    reportResourceError("XMLResourceError", new Object[]{str4, e5.getMessage()});
                    return false;
                }
            } else {
                this.fChildConfig = this.fXIncludeChildConfig;
            }
            if (this.fNeedCopyFeatures) {
                copyFeatures(this.fSettings, this.fChildConfig);
            }
            this.fNeedCopyFeatures = false;
            try {
                this.fNamespaceContext.pushScope();
                this.fChildConfig.parse(xMLInputSource);
                if (this.fErrorReporter != null) {
                    this.fErrorReporter.setDocumentLocator(this.fDocLocation);
                }
                if (value3 != null && !this.fXPtrProcessor.isXPointerResolved()) {
                    reportResourceError("XMLResourceError", new Object[]{str4, this.fXIncludeMessageFormatter.formatMessage(this.fErrorReporter != null ? this.fErrorReporter.getLocale() : null, "XPointerResolutionUnsuccessful", null)});
                    this.fNamespaceContext.popScope();
                    return false;
                }
            } catch (XNIException e6) {
                if (this.fErrorReporter != null) {
                    this.fErrorReporter.setDocumentLocator(this.fDocLocation);
                }
                reportFatalError("XMLParseError", new Object[]{str4, e6.getMessage()});
            } catch (IOException e7) {
                if (this.fErrorReporter != null) {
                    this.fErrorReporter.setDocumentLocator(this.fDocLocation);
                }
                reportResourceError("XMLResourceError", new Object[]{str4, e7.getMessage()});
                this.fNamespaceContext.popScope();
                return false;
            } catch (Throwable th2) {
                this.fNamespaceContext.popScope();
                throw th2;
            }
            this.fNamespaceContext.popScope();
        } else if (value2.equals(XINCLUDE_PARSE_TEXT)) {
            xMLInputSource.setEncoding(xMLAttributes.getValue(XINCLUDE_ATTR_ENCODING));
            try {
                if (!this.fIsXML11) {
                    if (this.fXInclude10TextReader == null) {
                        this.fXInclude10TextReader = new XIncludeTextReader(xMLInputSource, this, this.fBufferSize);
                    } else {
                        this.fXInclude10TextReader.setInputSource(xMLInputSource);
                    }
                    xIncludeTextReader3 = this.fXInclude10TextReader;
                } else {
                    if (this.fXInclude11TextReader == null) {
                        this.fXInclude11TextReader = new XInclude11TextReader(xMLInputSource, this, this.fBufferSize);
                    } else {
                        this.fXInclude11TextReader.setInputSource(xMLInputSource);
                    }
                    xIncludeTextReader3 = this.fXInclude11TextReader;
                }
                XIncludeTextReader xIncludeTextReader4 = xIncludeTextReader3;
                try {
                    xIncludeTextReader4.setErrorReporter(this.fErrorReporter);
                    xIncludeTextReader4.parse();
                    try {
                        xIncludeTextReader4.close();
                    } catch (IOException e8) {
                        reportResourceError("TextResourceError", new Object[]{str4, e8.getMessage()});
                        return false;
                    }
                } catch (MalformedByteSequenceException e9) {
                    e = e9;
                    xIncludeTextReader2 = xIncludeTextReader4;
                    this.fErrorReporter.reportError(e.getDomain(), e.getKey(), e.getArguments(), 2);
                    if (xIncludeTextReader2 != null) {
                    }
                    return true;
                } catch (CharConversionException unused3) {
                    try {
                        this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "CharConversionFailure", null, 2);
                        if (xIncludeTextReader4 != null) {
                        }
                        return true;
                    } catch (Throwable th3) {
                        th = th3;
                        xIncludeTextReader = xIncludeTextReader4;
                        if (xIncludeTextReader != null) {
                            try {
                                xIncludeTextReader.close();
                            } catch (IOException e10) {
                                reportResourceError("TextResourceError", new Object[]{str4, e10.getMessage()});
                                return false;
                            }
                        }
                        throw th;
                    }
                } catch (IOException e11) {
                    IOException e12 = e11;
                    xIncludeTextReader = xIncludeTextReader4;
                    try {
                        reportResourceError("TextResourceError", new Object[]{str4, e12.getMessage()});
                        if (xIncludeTextReader != null) {
                        }
                        return false;
                    } catch (Throwable th4) {
                        th = th4;
                        if (xIncludeTextReader != null) {
                        }
                        throw th;
                    }
                }
            } catch (MalformedByteSequenceException e13) {
                e = e13;
                xIncludeTextReader2 = null;
                this.fErrorReporter.reportError(e.getDomain(), e.getKey(), e.getArguments(), 2);
                if (xIncludeTextReader2 != null) {
                }
                return true;
            } catch (CharConversionException unused4) {
            } catch (IOException e14) {
            } catch (Throwable th5) {
            }
        } else {
            reportFatalError("InvalidParseValue", new Object[]{value2});
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean hasXIncludeNamespace(QName qName) {
        return qName.uri == XINCLUDE_NS_URI || this.fNamespaceContext.getURI(qName.prefix) == XINCLUDE_NS_URI;
    }

    /* access modifiers changed from: protected */
    public boolean isIncludeElement(QName qName) {
        return qName.localpart.equals(XINCLUDE_INCLUDE) && hasXIncludeNamespace(qName);
    }

    /* access modifiers changed from: protected */
    public boolean isFallbackElement(QName qName) {
        return qName.localpart.equals(XINCLUDE_FALLBACK) && hasXIncludeNamespace(qName);
    }

    /* access modifiers changed from: protected */
    public boolean sameBaseURIAsIncludeParent() {
        String includeParentBaseURI = getIncludeParentBaseURI();
        return includeParentBaseURI != null && includeParentBaseURI.equals(this.fCurrentBaseURI.getExpandedSystemId());
    }

    /* access modifiers changed from: protected */
    public boolean sameLanguageAsIncludeParent() {
        String includeParentLanguage = getIncludeParentLanguage();
        return includeParentLanguage != null && includeParentLanguage.equalsIgnoreCase(this.fCurrentLanguage);
    }

    /* access modifiers changed from: protected */
    public boolean searchForRecursiveIncludes(XMLLocator xMLLocator) {
        String expandedSystemId = xMLLocator.getExpandedSystemId();
        if (expandedSystemId == null) {
            try {
                expandedSystemId = XMLEntityManager.expandSystemId(xMLLocator.getLiteralSystemId(), xMLLocator.getBaseSystemId(), false);
            } catch (URI.MalformedURIException unused) {
                reportFatalError("ExpandedSystemId");
            }
        }
        if (expandedSystemId.equals(this.fCurrentBaseURI.getExpandedSystemId())) {
            return true;
        }
        XIncludeHandler xIncludeHandler = this.fParentXIncludeHandler;
        if (xIncludeHandler == null) {
            return false;
        }
        return xIncludeHandler.searchForRecursiveIncludes(xMLLocator);
    }

    /* access modifiers changed from: protected */
    public boolean isTopLevelIncludedItem() {
        return isTopLevelIncludedItemViaInclude() || isTopLevelIncludedItemViaFallback();
    }

    /* access modifiers changed from: protected */
    public boolean isTopLevelIncludedItemViaInclude() {
        return this.fDepth == 1 && !isRootDocument();
    }

    /* access modifiers changed from: protected */
    public boolean isTopLevelIncludedItemViaFallback() {
        return getSawFallback(this.fDepth - 1);
    }

    /* access modifiers changed from: protected */
    public XMLAttributes processAttributes(XMLAttributes xMLAttributes) {
        String str;
        String str2;
        String str3;
        String str4;
        if (isTopLevelIncludedItem()) {
            if (this.fFixupBaseURIs && !sameBaseURIAsIncludeParent()) {
                if (xMLAttributes == null) {
                    xMLAttributes = new XMLAttributesImpl();
                }
                try {
                    str4 = getRelativeBaseURI();
                } catch (URI.MalformedURIException unused) {
                    str4 = this.fCurrentBaseURI.getExpandedSystemId();
                }
                xMLAttributes.setSpecified(xMLAttributes.addAttribute(XML_BASE_QNAME, XMLSymbols.fCDATASymbol, str4), true);
            }
            if (this.fFixupLanguage && !sameLanguageAsIncludeParent()) {
                if (xMLAttributes == null) {
                    xMLAttributes = new XMLAttributesImpl();
                }
                xMLAttributes.setSpecified(xMLAttributes.addAttribute(XML_LANG_QNAME, XMLSymbols.fCDATASymbol, this.fCurrentLanguage), true);
            }
            Enumeration allPrefixes = this.fNamespaceContext.getAllPrefixes();
            while (allPrefixes.hasMoreElements()) {
                String str5 = (String) allPrefixes.nextElement();
                String uRIFromIncludeParent = this.fNamespaceContext.getURIFromIncludeParent(str5);
                String uri = this.fNamespaceContext.getURI(str5);
                if (!(uRIFromIncludeParent == uri || xMLAttributes == null)) {
                    if (str5 == XMLSymbols.EMPTY_STRING) {
                        if (xMLAttributes.getValue(NamespaceContext.XMLNS_URI, XMLSymbols.PREFIX_XMLNS) == null) {
                            QName qName = (QName) NEW_NS_ATTR_QNAME.clone();
                            qName.prefix = null;
                            qName.localpart = XMLSymbols.PREFIX_XMLNS;
                            qName.rawname = XMLSymbols.PREFIX_XMLNS;
                            String str6 = XMLSymbols.fCDATASymbol;
                            if (uri != null) {
                                str3 = uri;
                            } else {
                                str3 = XMLSymbols.EMPTY_STRING;
                            }
                            xMLAttributes.setSpecified(xMLAttributes.addAttribute(qName, str6, str3), true);
                            this.fNamespaceContext.declarePrefix(str5, uri);
                        }
                    } else if (xMLAttributes.getValue(NamespaceContext.XMLNS_URI, str5) == null) {
                        QName qName2 = (QName) NEW_NS_ATTR_QNAME.clone();
                        qName2.localpart = str5;
                        qName2.rawname += str5;
                        SymbolTable symbolTable = this.fSymbolTable;
                        if (symbolTable != null) {
                            str = symbolTable.addSymbol(qName2.rawname);
                        } else {
                            str = qName2.rawname.intern();
                        }
                        qName2.rawname = str;
                        String str7 = XMLSymbols.fCDATASymbol;
                        if (uri != null) {
                            str2 = uri;
                        } else {
                            str2 = XMLSymbols.EMPTY_STRING;
                        }
                        xMLAttributes.setSpecified(xMLAttributes.addAttribute(qName2, str7, str2), true);
                        this.fNamespaceContext.declarePrefix(str5, uri);
                    }
                }
            }
        }
        if (xMLAttributes != null) {
            int length = xMLAttributes.getLength();
            for (int i = 0; i < length; i++) {
                String type = xMLAttributes.getType(i);
                String value = xMLAttributes.getValue(i);
                if (type == XMLSymbols.fENTITYSymbol) {
                    checkUnparsedEntity(value);
                }
                if (type == XMLSymbols.fENTITIESSymbol) {
                    StringTokenizer stringTokenizer = new StringTokenizer(value);
                    while (stringTokenizer.hasMoreTokens()) {
                        checkUnparsedEntity(stringTokenizer.nextToken());
                    }
                } else if (type == XMLSymbols.fNOTATIONSymbol) {
                    checkNotation(value);
                }
            }
        }
        return xMLAttributes;
    }

    /* access modifiers changed from: protected */
    public String getRelativeBaseURI() throws URI.MalformedURIException {
        int includeParentDepth = getIncludeParentDepth();
        String relativeURI = getRelativeURI(includeParentDepth);
        if (isRootDocument()) {
            return relativeURI;
        }
        if (relativeURI.equals("")) {
            relativeURI = this.fCurrentBaseURI.getLiteralSystemId();
        }
        if (includeParentDepth != 0) {
            return relativeURI;
        }
        if (this.fParentRelativeURI == null) {
            this.fParentRelativeURI = this.fParentXIncludeHandler.getRelativeBaseURI();
        }
        if (this.fParentRelativeURI.equals("")) {
            return relativeURI;
        }
        URI uri = new URI(this.fParentRelativeURI, true);
        URI uri2 = new URI(uri, relativeURI);
        if (!Objects.equals(uri.getScheme(), uri2.getScheme())) {
            return relativeURI;
        }
        if (!Objects.equals(uri.getAuthority(), uri2.getAuthority())) {
            return uri2.getSchemeSpecificPart();
        }
        String path = uri2.getPath();
        String queryString = uri2.getQueryString();
        String fragment = uri2.getFragment();
        if (queryString == null && fragment == null) {
            return path;
        }
        StringBuilder sb = new StringBuilder();
        if (path != null) {
            sb.append(path);
        }
        if (queryString != null) {
            sb.append('?');
            sb.append(queryString);
        }
        if (fragment != null) {
            sb.append('#');
            sb.append(fragment);
        }
        return sb.toString();
    }

    private String getIncludeParentBaseURI() {
        int includeParentDepth = getIncludeParentDepth();
        if (isRootDocument() || includeParentDepth != 0) {
            return getBaseURI(includeParentDepth);
        }
        return this.fParentXIncludeHandler.getIncludeParentBaseURI();
    }

    private String getIncludeParentLanguage() {
        int includeParentDepth = getIncludeParentDepth();
        if (isRootDocument() || includeParentDepth != 0) {
            return getLanguage(includeParentDepth);
        }
        return this.fParentXIncludeHandler.getIncludeParentLanguage();
    }

    private int getIncludeParentDepth() {
        for (int i = this.fDepth - 1; i >= 0; i--) {
            if (!(getSawInclude(i) || getSawFallback(i))) {
                return i;
            }
        }
        return 0;
    }

    private int getResultDepth() {
        return this.fResultDepth;
    }

    /* access modifiers changed from: protected */
    public Augmentations modifyAugmentations(Augmentations augmentations) {
        return modifyAugmentations(augmentations, false);
    }

    /* access modifiers changed from: protected */
    public Augmentations modifyAugmentations(Augmentations augmentations, boolean z) {
        if (z || isTopLevelIncludedItem()) {
            if (augmentations == null) {
                augmentations = new AugmentationsImpl();
            }
            augmentations.putItem(XINCLUDE_INCLUDED, Boolean.TRUE);
        }
        return augmentations;
    }

    /* access modifiers changed from: protected */
    public int getState(int i) {
        return this.fState[i];
    }

    /* access modifiers changed from: protected */
    public int getState() {
        return this.fState[this.fDepth];
    }

    /* access modifiers changed from: protected */
    public void setState(int i) {
        int i2 = this.fDepth;
        int[] iArr = this.fState;
        if (i2 >= iArr.length) {
            int[] iArr2 = new int[(i2 * 2)];
            System.arraycopy(iArr, 0, iArr2, 0, iArr.length);
            this.fState = iArr2;
        }
        this.fState[this.fDepth] = i;
    }

    /* access modifiers changed from: protected */
    public void setSawFallback(int i, boolean z) {
        boolean[] zArr = this.fSawFallback;
        if (i >= zArr.length) {
            boolean[] zArr2 = new boolean[(i * 2)];
            System.arraycopy(zArr, 0, zArr2, 0, zArr.length);
            this.fSawFallback = zArr2;
        }
        this.fSawFallback[i] = z;
    }

    /* access modifiers changed from: protected */
    public boolean getSawFallback(int i) {
        boolean[] zArr = this.fSawFallback;
        if (i >= zArr.length) {
            return false;
        }
        return zArr[i];
    }

    /* access modifiers changed from: protected */
    public void setSawInclude(int i, boolean z) {
        boolean[] zArr = this.fSawInclude;
        if (i >= zArr.length) {
            boolean[] zArr2 = new boolean[(i * 2)];
            System.arraycopy(zArr, 0, zArr2, 0, zArr.length);
            this.fSawInclude = zArr2;
        }
        this.fSawInclude[i] = z;
    }

    /* access modifiers changed from: protected */
    public boolean getSawInclude(int i) {
        boolean[] zArr = this.fSawInclude;
        if (i >= zArr.length) {
            return false;
        }
        return zArr[i];
    }

    /* access modifiers changed from: protected */
    public void reportResourceError(String str) {
        reportFatalError(str, null);
    }

    /* access modifiers changed from: protected */
    public void reportResourceError(String str, Object[] objArr) {
        reportError(str, objArr, 0);
    }

    /* access modifiers changed from: protected */
    public void reportFatalError(String str) {
        reportFatalError(str, null);
    }

    /* access modifiers changed from: protected */
    public void reportFatalError(String str, Object[] objArr) {
        reportError(str, objArr, 2);
    }

    private void reportError(String str, Object[] objArr, short s) {
        XMLErrorReporter xMLErrorReporter = this.fErrorReporter;
        if (xMLErrorReporter != null) {
            xMLErrorReporter.reportError(XIncludeMessageFormatter.XINCLUDE_DOMAIN, str, objArr, s);
        }
    }

    /* access modifiers changed from: protected */
    public void setParent(XIncludeHandler xIncludeHandler) {
        this.fParentXIncludeHandler = xIncludeHandler;
    }

    /* access modifiers changed from: protected */
    public boolean isRootDocument() {
        return this.fParentXIncludeHandler == null;
    }

    /* access modifiers changed from: protected */
    public void addUnparsedEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) {
        UnparsedEntity unparsedEntity = new UnparsedEntity();
        unparsedEntity.name = str;
        unparsedEntity.systemId = xMLResourceIdentifier.getLiteralSystemId();
        unparsedEntity.publicId = xMLResourceIdentifier.getPublicId();
        unparsedEntity.baseURI = xMLResourceIdentifier.getBaseSystemId();
        unparsedEntity.expandedSystemId = xMLResourceIdentifier.getExpandedSystemId();
        unparsedEntity.notation = str2;
        unparsedEntity.augmentations = augmentations;
        this.fUnparsedEntities.add(unparsedEntity);
    }

    /* access modifiers changed from: protected */
    public void addNotation(String str, XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) {
        Notation notation = new Notation();
        notation.name = str;
        notation.systemId = xMLResourceIdentifier.getLiteralSystemId();
        notation.publicId = xMLResourceIdentifier.getPublicId();
        notation.baseURI = xMLResourceIdentifier.getBaseSystemId();
        notation.expandedSystemId = xMLResourceIdentifier.getExpandedSystemId();
        notation.augmentations = augmentations;
        this.fNotations.add(notation);
    }

    /* access modifiers changed from: protected */
    public void checkUnparsedEntity(String str) {
        UnparsedEntity unparsedEntity = new UnparsedEntity();
        unparsedEntity.name = str;
        int indexOf = this.fUnparsedEntities.indexOf(unparsedEntity);
        if (indexOf != -1) {
            UnparsedEntity unparsedEntity2 = (UnparsedEntity) this.fUnparsedEntities.get(indexOf);
            checkNotation(unparsedEntity2.notation);
            checkAndSendUnparsedEntity(unparsedEntity2);
        }
    }

    /* access modifiers changed from: protected */
    public void checkNotation(String str) {
        Notation notation = new Notation();
        notation.name = str;
        int indexOf = this.fNotations.indexOf(notation);
        if (indexOf != -1) {
            checkAndSendNotation((Notation) this.fNotations.get(indexOf));
        }
    }

    /* access modifiers changed from: protected */
    public void checkAndSendUnparsedEntity(UnparsedEntity unparsedEntity) {
        XMLDTDHandler xMLDTDHandler;
        if (isRootDocument()) {
            int indexOf = this.fUnparsedEntities.indexOf(unparsedEntity);
            if (indexOf == -1) {
                XMLResourceIdentifierImpl xMLResourceIdentifierImpl = new XMLResourceIdentifierImpl(unparsedEntity.publicId, unparsedEntity.systemId, unparsedEntity.baseURI, unparsedEntity.expandedSystemId);
                addUnparsedEntity(unparsedEntity.name, xMLResourceIdentifierImpl, unparsedEntity.notation, unparsedEntity.augmentations);
                if (this.fSendUEAndNotationEvents && (xMLDTDHandler = this.fDTDHandler) != null) {
                    xMLDTDHandler.unparsedEntityDecl(unparsedEntity.name, xMLResourceIdentifierImpl, unparsedEntity.notation, unparsedEntity.augmentations);
                }
            } else if (!unparsedEntity.isDuplicate((UnparsedEntity) this.fUnparsedEntities.get(indexOf))) {
                reportFatalError("NonDuplicateUnparsedEntity", new Object[]{unparsedEntity.name});
            }
        } else {
            this.fParentXIncludeHandler.checkAndSendUnparsedEntity(unparsedEntity);
        }
    }

    /* access modifiers changed from: protected */
    public void checkAndSendNotation(Notation notation) {
        XMLDTDHandler xMLDTDHandler;
        if (isRootDocument()) {
            int indexOf = this.fNotations.indexOf(notation);
            if (indexOf == -1) {
                XMLResourceIdentifierImpl xMLResourceIdentifierImpl = new XMLResourceIdentifierImpl(notation.publicId, notation.systemId, notation.baseURI, notation.expandedSystemId);
                addNotation(notation.name, xMLResourceIdentifierImpl, notation.augmentations);
                if (this.fSendUEAndNotationEvents && (xMLDTDHandler = this.fDTDHandler) != null) {
                    xMLDTDHandler.notationDecl(notation.name, xMLResourceIdentifierImpl, notation.augmentations);
                }
            } else if (!notation.isDuplicate((Notation) this.fNotations.get(indexOf))) {
                reportFatalError("NonDuplicateNotation", new Object[]{notation.name});
            }
        } else {
            this.fParentXIncludeHandler.checkAndSendNotation(notation);
        }
    }

    private void checkWhitespace(XMLString xMLString) {
        int i = xMLString.offset + xMLString.length;
        for (int i2 = xMLString.offset; i2 < i; i2++) {
            if (!XMLChar.isSpace(xMLString.ch[i2])) {
                reportFatalError("ContentIllegalAtTopLevel");
                return;
            }
        }
    }

    private void checkMultipleRootElements() {
        if (getRootElementProcessed()) {
            reportFatalError("MultipleRootElements");
        }
        setRootElementProcessed(true);
    }

    private void setRootElementProcessed(boolean z) {
        if (isRootDocument()) {
            this.fSeenRootElement = z;
        } else {
            this.fParentXIncludeHandler.setRootElementProcessed(z);
        }
    }

    private boolean getRootElementProcessed() {
        return isRootDocument() ? this.fSeenRootElement : this.fParentXIncludeHandler.getRootElementProcessed();
    }

    /* access modifiers changed from: protected */
    public void copyFeatures(XMLComponentManager xMLComponentManager, ParserConfigurationSettings parserConfigurationSettings) {
        copyFeatures1(ohos.com.sun.org.apache.xerces.internal.impl.Constants.getXercesFeatures(), ohos.com.sun.org.apache.xerces.internal.impl.Constants.XERCES_FEATURE_PREFIX, xMLComponentManager, parserConfigurationSettings);
        copyFeatures1(ohos.com.sun.org.apache.xerces.internal.impl.Constants.getSAXFeatures(), ohos.com.sun.org.apache.xerces.internal.impl.Constants.SAX_FEATURE_PREFIX, xMLComponentManager, parserConfigurationSettings);
    }

    /* access modifiers changed from: protected */
    public void copyFeatures(XMLComponentManager xMLComponentManager, XMLParserConfiguration xMLParserConfiguration) {
        copyFeatures1(ohos.com.sun.org.apache.xerces.internal.impl.Constants.getXercesFeatures(), ohos.com.sun.org.apache.xerces.internal.impl.Constants.XERCES_FEATURE_PREFIX, xMLComponentManager, xMLParserConfiguration);
        copyFeatures1(ohos.com.sun.org.apache.xerces.internal.impl.Constants.getSAXFeatures(), ohos.com.sun.org.apache.xerces.internal.impl.Constants.SAX_FEATURE_PREFIX, xMLComponentManager, xMLParserConfiguration);
    }

    private void copyFeatures1(Enumeration enumeration, String str, XMLComponentManager xMLComponentManager, ParserConfigurationSettings parserConfigurationSettings) {
        while (enumeration.hasMoreElements()) {
            String str2 = str + ((String) enumeration.nextElement());
            parserConfigurationSettings.addRecognizedFeatures(new String[]{str2});
            try {
                parserConfigurationSettings.setFeature(str2, xMLComponentManager.getFeature(str2));
            } catch (XMLConfigurationException unused) {
            }
        }
    }

    private void copyFeatures1(Enumeration enumeration, String str, XMLComponentManager xMLComponentManager, XMLParserConfiguration xMLParserConfiguration) {
        while (enumeration.hasMoreElements()) {
            String str2 = str + ((String) enumeration.nextElement());
            try {
                xMLParserConfiguration.setFeature(str2, xMLComponentManager.getFeature(str2));
            } catch (XMLConfigurationException unused) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public static class Notation {
        public Augmentations augmentations;
        public String baseURI;
        public String expandedSystemId;
        public String name;
        public String publicId;
        public String systemId;

        protected Notation() {
        }

        public boolean equals(Object obj) {
            return obj == this || ((obj instanceof Notation) && Objects.equals(this.name, ((Notation) obj).name));
        }

        public int hashCode() {
            return Objects.hashCode(this.name);
        }

        public boolean isDuplicate(Object obj) {
            if (obj == null || !(obj instanceof Notation)) {
                return false;
            }
            Notation notation = (Notation) obj;
            if (!Objects.equals(this.name, notation.name) || !Objects.equals(this.publicId, notation.publicId) || !Objects.equals(this.expandedSystemId, notation.expandedSystemId)) {
                return false;
            }
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public static class UnparsedEntity {
        public Augmentations augmentations;
        public String baseURI;
        public String expandedSystemId;
        public String name;
        public String notation;
        public String publicId;
        public String systemId;

        protected UnparsedEntity() {
        }

        public boolean equals(Object obj) {
            return obj == this || ((obj instanceof UnparsedEntity) && Objects.equals(this.name, ((UnparsedEntity) obj).name));
        }

        public int hashCode() {
            return Objects.hashCode(this.name);
        }

        public boolean isDuplicate(Object obj) {
            if (obj == null || !(obj instanceof UnparsedEntity)) {
                return false;
            }
            UnparsedEntity unparsedEntity = (UnparsedEntity) obj;
            if (!Objects.equals(this.name, unparsedEntity.name) || !Objects.equals(this.publicId, unparsedEntity.publicId) || !Objects.equals(this.expandedSystemId, unparsedEntity.expandedSystemId) || !Objects.equals(this.notation, unparsedEntity.notation)) {
                return false;
            }
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void saveBaseURI() {
        this.fBaseURIScope.push(this.fDepth);
        this.fBaseURI.push(this.fCurrentBaseURI.getBaseSystemId());
        this.fLiteralSystemID.push(this.fCurrentBaseURI.getLiteralSystemId());
        this.fExpandedSystemID.push(this.fCurrentBaseURI.getExpandedSystemId());
    }

    /* access modifiers changed from: protected */
    public void restoreBaseURI() {
        this.fBaseURI.pop();
        this.fLiteralSystemID.pop();
        this.fExpandedSystemID.pop();
        this.fBaseURIScope.pop();
        this.fCurrentBaseURI.setBaseSystemId((String) this.fBaseURI.peek());
        this.fCurrentBaseURI.setLiteralSystemId((String) this.fLiteralSystemID.peek());
        this.fCurrentBaseURI.setExpandedSystemId((String) this.fExpandedSystemID.peek());
    }

    /* access modifiers changed from: protected */
    public void saveLanguage(String str) {
        this.fLanguageScope.push(this.fDepth);
        this.fLanguageStack.push(str);
    }

    public String restoreLanguage() {
        this.fLanguageStack.pop();
        this.fLanguageScope.pop();
        return (String) this.fLanguageStack.peek();
    }

    public String getBaseURI(int i) {
        return (String) this.fExpandedSystemID.elementAt(scopeOfBaseURI(i));
    }

    public String getLanguage(int i) {
        return (String) this.fLanguageStack.elementAt(scopeOfLanguage(i));
    }

    public String getRelativeURI(int i) throws URI.MalformedURIException {
        int scopeOfBaseURI = scopeOfBaseURI(i) + 1;
        if (scopeOfBaseURI == this.fBaseURIScope.size()) {
            return "";
        }
        URI uri = new URI(AsrConstants.ASR_SRC_FILE, (String) this.fLiteralSystemID.elementAt(scopeOfBaseURI));
        int i2 = scopeOfBaseURI + 1;
        while (i2 < this.fBaseURIScope.size()) {
            i2++;
            uri = new URI(uri, (String) this.fLiteralSystemID.elementAt(i2));
        }
        return uri.getPath();
    }

    private int scopeOfBaseURI(int i) {
        for (int size = this.fBaseURIScope.size() - 1; size >= 0; size--) {
            if (this.fBaseURIScope.elementAt(size) <= i) {
                return size;
            }
        }
        return -1;
    }

    private int scopeOfLanguage(int i) {
        for (int size = this.fLanguageScope.size() - 1; size >= 0; size--) {
            if (this.fLanguageScope.elementAt(size) <= i) {
                return size;
            }
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public void processXMLBaseAttributes(XMLAttributes xMLAttributes) {
        String value = xMLAttributes.getValue(NamespaceContext.XML_URI, "base");
        if (value != null) {
            try {
                String expandSystemId = XMLEntityManager.expandSystemId(value, this.fCurrentBaseURI.getExpandedSystemId(), false);
                this.fCurrentBaseURI.setLiteralSystemId(value);
                this.fCurrentBaseURI.setBaseSystemId(this.fCurrentBaseURI.getExpandedSystemId());
                this.fCurrentBaseURI.setExpandedSystemId(expandSystemId);
                saveBaseURI();
            } catch (URI.MalformedURIException unused) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public void processXMLLangAttributes(XMLAttributes xMLAttributes) {
        String value = xMLAttributes.getValue(NamespaceContext.XML_URI, "lang");
        if (value != null) {
            this.fCurrentLanguage = value;
            saveLanguage(this.fCurrentLanguage);
        }
    }

    private boolean isValidInHTTPHeader(String str) {
        for (int length = str.length() - 1; length >= 0; length--) {
            char charAt = str.charAt(length);
            if (charAt < ' ' || charAt > '~') {
                return false;
            }
        }
        return true;
    }

    private XMLInputSource createInputSource(String str, String str2, String str3, String str4, String str5) {
        HTTPInputSource hTTPInputSource = new HTTPInputSource(str, str2, str3);
        if (str4 != null && str4.length() > 0) {
            hTTPInputSource.setHTTPRequestProperty(HTTP_ACCEPT, str4);
        }
        if (str5 != null && str5.length() > 0) {
            hTTPInputSource.setHTTPRequestProperty(HTTP_ACCEPT_LANGUAGE, str5);
        }
        return hTTPInputSource;
    }

    private String escapeHref(String str) {
        int supplemental;
        char charAt;
        int length = str.length();
        StringBuilder sb = new StringBuilder(length * 3);
        int i = 0;
        while (i < length && (charAt = str.charAt(i)) <= '~') {
            if (charAt < ' ') {
                return str;
            }
            if (gNeedEscaping[charAt]) {
                sb.append('%');
                sb.append(gAfterEscaping1[charAt]);
                sb.append(gAfterEscaping2[charAt]);
            } else {
                sb.append((char) charAt);
            }
            i++;
        }
        if (i < length) {
            int i2 = i;
            while (i2 < length) {
                char charAt2 = str.charAt(i2);
                if ((charAt2 < ' ' || charAt2 > '~') && ((charAt2 < 160 || charAt2 > 55295) && ((charAt2 < 63744 || charAt2 > 64975) && (charAt2 < 65008 || charAt2 > 65519)))) {
                    if (XMLChar.isHighSurrogate(charAt2) && (i2 = i2 + 1) < length) {
                        char charAt3 = str.charAt(i2);
                        if (XMLChar.isLowSurrogate(charAt3) && (supplemental = XMLChar.supplemental((char) charAt2, (char) charAt3)) < 983040 && (supplemental & 65535) <= 65533) {
                        }
                    }
                    return str;
                }
                i2++;
            }
            try {
                byte[] bytes = str.substring(i).getBytes("UTF-8");
                int length2 = bytes.length;
                for (byte b : bytes) {
                    if (b < 0) {
                        int i3 = b + 256;
                        sb.append('%');
                        sb.append(gHexChs[i3 >> 4]);
                        sb.append(gHexChs[i3 & 15]);
                    } else if (gNeedEscaping[b]) {
                        sb.append('%');
                        sb.append(gAfterEscaping1[b]);
                        sb.append(gAfterEscaping2[b]);
                    } else {
                        sb.append((char) b);
                    }
                }
                length = length2;
            } catch (UnsupportedEncodingException unused) {
                return str;
            }
        }
        return sb.length() != length ? sb.toString() : str;
    }
}
