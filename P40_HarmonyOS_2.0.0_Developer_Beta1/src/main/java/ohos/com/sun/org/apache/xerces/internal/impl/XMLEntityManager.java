package ohos.com.sun.org.apache.xerces.internal.impl;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import ohos.ai.asr.util.AsrConstants;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.io.ASCIIReader;
import ohos.com.sun.org.apache.xerces.internal.impl.io.UCSReader;
import ohos.com.sun.org.apache.xerces.internal.impl.io.UTF8Reader;
import ohos.com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import ohos.com.sun.org.apache.xerces.internal.util.AugmentationsImpl;
import ohos.com.sun.org.apache.xerces.internal.util.EncodingMap;
import ohos.com.sun.org.apache.xerces.internal.util.HTTPInputSource;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.URI;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.util.XMLEntityDescriptionImpl;
import ohos.com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl;
import ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLLimitAnalyzer;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.com.sun.xml.internal.stream.Entity;
import ohos.com.sun.xml.internal.stream.StaxEntityResolverWrapper;
import ohos.com.sun.xml.internal.stream.StaxXMLInputSource;
import ohos.com.sun.xml.internal.stream.XMLEntityStorage;
import ohos.global.icu.impl.PatternTokenizer;

public class XMLEntityManager implements XMLComponent, XMLEntityResolver {
    protected static final String ALLOW_JAVA_ENCODINGS = "http://apache.org/xml/features/allow-java-encodings";
    protected static final String BUFFER_SIZE = "http://apache.org/xml/properties/input-buffer-size";
    private static final boolean DEBUG_BUFFER = false;
    private static final boolean DEBUG_ENCODINGS = false;
    private static final boolean DEBUG_ENTITIES = false;
    private static final boolean DEBUG_RESOLVER = false;
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    public static final int DEFAULT_INTERNAL_BUFFER_SIZE = 1024;
    public static final int DEFAULT_XMLDECL_BUFFER_SIZE = 64;
    private static final String DTDEntity = "[dtd]".intern();
    protected static final String ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    static final String EXTERNAL_ACCESS_DEFAULT = "all";
    protected static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
    protected static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
    private static final Boolean[] FEATURE_DEFAULTS = {null, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE};
    protected static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    protected static final String PARSER_SETTINGS = "http://apache.org/xml/features/internal/parser-settings";
    private static final Object[] PROPERTY_DEFAULTS = {null, null, null, null, new Integer(8192), null, null};
    private static final String[] RECOGNIZED_FEATURES = {VALIDATION, EXTERNAL_GENERAL_ENTITIES, EXTERNAL_PARAMETER_ENTITIES, ALLOW_JAVA_ENCODINGS, WARN_ON_DUPLICATE_ENTITYDEF, STANDARD_URI_CONFORMANT};
    private static final String[] RECOGNIZED_PROPERTIES = {"http://apache.org/xml/properties/internal/symbol-table", "http://apache.org/xml/properties/internal/error-reporter", "http://apache.org/xml/properties/internal/entity-resolver", VALIDATION_MANAGER, "http://apache.org/xml/properties/input-buffer-size", "http://apache.org/xml/properties/security-manager", "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager"};
    protected static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    protected static final String STANDARD_URI_CONFORMANT = "http://apache.org/xml/features/standard-uri-conformant";
    protected static final String STAX_ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/stax-entity-resolver";
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    protected static final String VALIDATION = "http://xml.org/sax/features/validation";
    protected static final String VALIDATION_MANAGER = "http://apache.org/xml/properties/internal/validation-manager";
    protected static final String WARN_ON_DUPLICATE_ENTITYDEF = "http://apache.org/xml/features/warn-on-duplicate-entitydef";
    private static final String XMLEntity = "[xml]".intern();
    private static final String XML_SECURITY_PROPERTY_MANAGER = "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager";
    private static char[] gAfterEscaping1 = new char[128];
    private static char[] gAfterEscaping2 = new char[128];
    private static char[] gHexChs = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static boolean[] gNeedEscaping = new boolean[128];
    private static String gUserDir;
    private static URI gUserDirURI;
    protected final Object[] defaultEncoding;
    protected int entityExpansionIndex;
    protected String fAccessExternalDTD;
    protected boolean fAllowJavaEncodings;
    private CharacterBufferPool fBufferPool;
    protected int fBufferSize;
    protected Entity.ScannedEntity fCurrentEntity;
    protected Map<String, Entity> fEntities;
    private final Augmentations fEntityAugs;
    protected int fEntityExpansionCount;
    protected XMLEntityHandler fEntityHandler;
    protected XMLEntityResolver fEntityResolver;
    protected XMLEntityScanner fEntityScanner;
    protected Stack<Entity> fEntityStack;
    protected XMLEntityStorage fEntityStorage;
    protected XMLErrorReporter fErrorReporter;
    protected boolean fExternalGeneralEntities;
    protected boolean fExternalParameterEntities;
    boolean fISCreatedByResolver;
    protected boolean fInExternalSubset;
    protected XMLLimitAnalyzer fLimitAnalyzer;
    protected boolean fLoadExternalDTD;
    protected PropertyManager fPropertyManager;
    boolean fReplaceEntityReferences;
    private final XMLResourceIdentifierImpl fResourceIdentifier;
    protected XMLSecurityManager fSecurityManager;
    protected boolean fStandalone;
    protected StaxEntityResolverWrapper fStaxEntityResolver;
    protected boolean fStrictURI;
    boolean fSupportDTD;
    boolean fSupportExternalEntities;
    protected SymbolTable fSymbolTable;
    protected boolean fValidation;
    protected ValidationManager fValidationManager;
    protected boolean fWarnDuplicateEntityDef;
    protected XMLEntityScanner fXML10EntityScanner;
    protected XMLEntityScanner fXML11EntityScanner;

    public void closeReaders() {
    }

    /* access modifiers changed from: package-private */
    public final void print() {
    }

    static {
        for (int i = 0; i <= 31; i++) {
            gNeedEscaping[i] = true;
            char[] cArr = gAfterEscaping1;
            char[] cArr2 = gHexChs;
            cArr[i] = cArr2[i >> 4];
            gAfterEscaping2[i] = cArr2[i & 15];
        }
        gNeedEscaping[127] = true;
        gAfterEscaping1[127] = '7';
        gAfterEscaping2[127] = 'F';
        char[] cArr3 = {' ', '<', '>', '#', '%', '\"', '{', '}', '|', PatternTokenizer.BACK_SLASH, '^', '~', '[', ']', '`'};
        for (char c : cArr3) {
            gNeedEscaping[c] = true;
            char[] cArr4 = gAfterEscaping1;
            char[] cArr5 = gHexChs;
            cArr4[c] = cArr5[c >> 4];
            gAfterEscaping2[c] = cArr5[c & 15];
        }
    }

    public XMLEntityManager() {
        this.fAllowJavaEncodings = true;
        this.fLoadExternalDTD = true;
        this.fSupportDTD = true;
        this.fReplaceEntityReferences = true;
        this.fSupportExternalEntities = true;
        this.fAccessExternalDTD = "all";
        this.fBufferSize = 8192;
        this.fSecurityManager = null;
        this.fLimitAnalyzer = null;
        this.fInExternalSubset = false;
        this.fEntityExpansionCount = 0;
        this.fEntities = new HashMap();
        this.fEntityStack = new Stack<>();
        this.fCurrentEntity = null;
        this.fISCreatedByResolver = false;
        this.defaultEncoding = new Object[]{"UTF-8", null};
        this.fResourceIdentifier = new XMLResourceIdentifierImpl();
        this.fEntityAugs = new AugmentationsImpl();
        this.fBufferPool = new CharacterBufferPool(this.fBufferSize, 1024);
        this.fSecurityManager = new XMLSecurityManager(true);
        this.fEntityStorage = new XMLEntityStorage(this);
        setScannerVersion(1);
    }

    public XMLEntityManager(PropertyManager propertyManager) {
        this.fAllowJavaEncodings = true;
        this.fLoadExternalDTD = true;
        this.fSupportDTD = true;
        this.fReplaceEntityReferences = true;
        this.fSupportExternalEntities = true;
        this.fAccessExternalDTD = "all";
        this.fBufferSize = 8192;
        this.fSecurityManager = null;
        this.fLimitAnalyzer = null;
        this.fInExternalSubset = false;
        this.fEntityExpansionCount = 0;
        this.fEntities = new HashMap();
        this.fEntityStack = new Stack<>();
        this.fCurrentEntity = null;
        this.fISCreatedByResolver = false;
        this.defaultEncoding = new Object[]{"UTF-8", null};
        this.fResourceIdentifier = new XMLResourceIdentifierImpl();
        this.fEntityAugs = new AugmentationsImpl();
        this.fBufferPool = new CharacterBufferPool(this.fBufferSize, 1024);
        this.fPropertyManager = propertyManager;
        this.fEntityStorage = new XMLEntityStorage(this);
        this.fEntityScanner = new XMLEntityScanner(propertyManager, this);
        reset(propertyManager);
    }

    public void addInternalEntity(String str, String str2) {
        if (!this.fEntities.containsKey(str)) {
            this.fEntities.put(str, new Entity.InternalEntity(str, str2, this.fInExternalSubset));
        } else if (this.fWarnDuplicateEntityDef) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DUPLICATE_ENTITY_DEFINITION", new Object[]{str}, 0);
        }
    }

    public void addExternalEntity(String str, String str2, String str3, String str4) throws IOException {
        Entity.ScannedEntity scannedEntity;
        if (!this.fEntities.containsKey(str)) {
            if (str4 == null) {
                int size = this.fEntityStack.size();
                if (!(size != 0 || (scannedEntity = this.fCurrentEntity) == null || scannedEntity.entityLocation == null)) {
                    str4 = this.fCurrentEntity.entityLocation.getExpandedSystemId();
                }
                int i = size - 1;
                while (true) {
                    if (i < 0) {
                        break;
                    }
                    Entity.ScannedEntity scannedEntity2 = (Entity.ScannedEntity) this.fEntityStack.elementAt(i);
                    if (scannedEntity2.entityLocation != null && scannedEntity2.entityLocation.getExpandedSystemId() != null) {
                        str4 = scannedEntity2.entityLocation.getExpandedSystemId();
                        break;
                    }
                    i--;
                }
            }
            this.fEntities.put(str, new Entity.ExternalEntity(str, new XMLEntityDescriptionImpl(str, str2, str3, str4, expandSystemId(str3, str4, false)), null, this.fInExternalSubset));
        } else if (this.fWarnDuplicateEntityDef) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DUPLICATE_ENTITY_DEFINITION", new Object[]{str}, 0);
        }
    }

    public void addUnparsedEntity(String str, String str2, String str3, String str4, String str5) {
        if (!this.fEntities.containsKey(str)) {
            this.fEntities.put(str, new Entity.ExternalEntity(str, new XMLEntityDescriptionImpl(str, str2, str3, str4, null), str5, this.fInExternalSubset));
        } else if (this.fWarnDuplicateEntityDef) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DUPLICATE_ENTITY_DEFINITION", new Object[]{str}, 0);
        }
    }

    public XMLEntityStorage getEntityStore() {
        return this.fEntityStorage;
    }

    public XMLEntityScanner getEntityScanner() {
        if (this.fEntityScanner == null) {
            if (this.fXML10EntityScanner == null) {
                this.fXML10EntityScanner = new XMLEntityScanner();
            }
            this.fXML10EntityScanner.reset(this.fSymbolTable, this, this.fErrorReporter);
            this.fEntityScanner = this.fXML10EntityScanner;
        }
        return this.fEntityScanner;
    }

    public void setScannerVersion(short s) {
        if (s == 1) {
            if (this.fXML10EntityScanner == null) {
                this.fXML10EntityScanner = new XMLEntityScanner();
            }
            this.fXML10EntityScanner.reset(this.fSymbolTable, this, this.fErrorReporter);
            this.fEntityScanner = this.fXML10EntityScanner;
            this.fEntityScanner.setCurrentEntity(this.fCurrentEntity);
            return;
        }
        if (this.fXML11EntityScanner == null) {
            this.fXML11EntityScanner = new XML11EntityScanner();
        }
        this.fXML11EntityScanner.reset(this.fSymbolTable, this, this.fErrorReporter);
        this.fEntityScanner = this.fXML11EntityScanner;
        this.fEntityScanner.setCurrentEntity(this.fCurrentEntity);
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0099  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00ec  */
    public String setupCurrentEntity(boolean z, String str, XMLInputSource xMLInputSource, boolean z2, boolean z3) throws IOException, XNIException {
        Reader reader;
        RewindableInputStream rewindableInputStream;
        String str2;
        InputStream inputStream;
        Reader reader2;
        Boolean bool;
        int i;
        Boolean bool2;
        int i2;
        Boolean bool3;
        boolean z4;
        String publicId = xMLInputSource.getPublicId();
        String systemId = xMLInputSource.getSystemId();
        String baseSystemId = xMLInputSource.getBaseSystemId();
        String encoding = xMLInputSource.getEncoding();
        boolean z5 = encoding != null;
        Reader characterStream = xMLInputSource.getCharacterStream();
        String expandSystemId = expandSystemId(systemId, baseSystemId, this.fStrictURI);
        if (baseSystemId == null) {
            baseSystemId = expandSystemId;
        }
        if (characterStream == null) {
            InputStream byteStream = xMLInputSource.getByteStream();
            if (byteStream == null) {
                URLConnection openConnection = new URL(expandSystemId).openConnection();
                if (!(openConnection instanceof HttpURLConnection)) {
                    byteStream = openConnection.getInputStream();
                } else {
                    if (xMLInputSource instanceof HTTPInputSource) {
                        HttpURLConnection httpURLConnection = (HttpURLConnection) openConnection;
                        HTTPInputSource hTTPInputSource = (HTTPInputSource) xMLInputSource;
                        Iterator<Map.Entry<String, String>> hTTPRequestProperties = hTTPInputSource.getHTTPRequestProperties();
                        while (hTTPRequestProperties.hasNext()) {
                            Map.Entry<String, String> next = hTTPRequestProperties.next();
                            httpURLConnection.setRequestProperty(next.getKey(), next.getValue());
                        }
                        z4 = hTTPInputSource.getFollowHTTPRedirects();
                        if (!z4) {
                            setInstanceFollowRedirects(httpURLConnection, z4);
                        }
                    } else {
                        z4 = true;
                    }
                    inputStream = openConnection.getInputStream();
                    if (z4) {
                        String url = openConnection.getURL().toString();
                        if (!url.equals(expandSystemId)) {
                            systemId = url;
                            expandSystemId = systemId;
                        }
                    }
                    RewindableInputStream rewindableInputStream2 = new RewindableInputStream(inputStream);
                    if (encoding != null) {
                        byte[] bArr = new byte[4];
                        int i3 = 0;
                        while (i3 < 4) {
                            bArr[i3] = (byte) rewindableInputStream2.read();
                            i3++;
                        }
                        if (i3 == 4) {
                            Object[] encodingName = getEncodingName(bArr, i3);
                            String str3 = (String) encodingName[0];
                            Boolean bool4 = (Boolean) encodingName[1];
                            rewindableInputStream2.reset();
                            if (i3 > 2 && str3.equals("UTF-8")) {
                                int i4 = bArr[0] & 255;
                                int i5 = bArr[1] & 255;
                                int i6 = bArr[2] & 255;
                                if (i4 == 239 && i5 == 187 && i6 == 191) {
                                    rewindableInputStream2.skip(3);
                                }
                            }
                            reader2 = createReader(rewindableInputStream2, str3, bool4);
                            encoding = str3;
                        } else {
                            reader2 = createReader(rewindableInputStream2, encoding, null);
                        }
                    } else {
                        encoding = encoding.toUpperCase(Locale.ENGLISH);
                        if (encoding.equals("UTF-8")) {
                            int[] iArr = new int[3];
                            int i7 = 0;
                            while (i7 < 3) {
                                iArr[i7] = rewindableInputStream2.read();
                                if (iArr[i7] == -1) {
                                    break;
                                }
                                i7++;
                            }
                            if (i7 != 3) {
                                rewindableInputStream2.reset();
                            } else if (!(iArr[0] == 239 && iArr[1] == 187 && iArr[2] == 191)) {
                                rewindableInputStream2.reset();
                            }
                        } else {
                            String str4 = "UTF-16";
                            if (encoding.equals(str4)) {
                                int[] iArr2 = new int[4];
                                int i8 = 0;
                                for (int i9 = 4; i8 < i9; i9 = 4) {
                                    iArr2[i8] = rewindableInputStream2.read();
                                    if (iArr2[i8] == -1) {
                                        break;
                                    }
                                    i8++;
                                }
                                rewindableInputStream2.reset();
                                if (i8 >= 2) {
                                    int i10 = iArr2[0];
                                    int i11 = iArr2[1];
                                    if (i10 == 254 && i11 == 255) {
                                        bool3 = Boolean.TRUE;
                                        str4 = "UTF-16BE";
                                    } else if (i10 == 255 && i11 == 254) {
                                        bool3 = Boolean.FALSE;
                                        str4 = "UTF-16LE";
                                    } else if (i8 == 4) {
                                        int i12 = iArr2[2];
                                        int i13 = iArr2[3];
                                        if (i10 == 0 && i11 == 60 && i12 == 0) {
                                            i2 = 63;
                                            if (i13 == 63) {
                                                bool2 = Boolean.TRUE;
                                                str4 = "UTF-16BE";
                                                if (i10 != 60 && i11 == 0 && i12 == i2 && i13 == 0) {
                                                    bool3 = Boolean.FALSE;
                                                    str4 = "UTF-16LE";
                                                } else {
                                                    bool = bool2;
                                                    createReader(rewindableInputStream2, str4, bool);
                                                }
                                            }
                                        } else {
                                            i2 = 63;
                                        }
                                        bool2 = null;
                                        if (i10 != 60) {
                                        }
                                        bool = bool2;
                                        createReader(rewindableInputStream2, str4, bool);
                                    }
                                    bool = bool3;
                                    createReader(rewindableInputStream2, str4, bool);
                                }
                                bool = null;
                                createReader(rewindableInputStream2, str4, bool);
                            } else if (encoding.equals("ISO-10646-UCS-4")) {
                                int[] iArr3 = new int[4];
                                int i14 = 0;
                                while (i14 < 4) {
                                    iArr3[i14] = rewindableInputStream2.read();
                                    if (iArr3[i14] == -1) {
                                        break;
                                    }
                                    i14++;
                                }
                                rewindableInputStream2.reset();
                                if (i14 == 4) {
                                    if (iArr3[0] == 0 && iArr3[1] == 0 && iArr3[2] == 0) {
                                        i = 60;
                                        if (iArr3[3] == 60) {
                                            bool = Boolean.TRUE;
                                        }
                                    } else {
                                        i = 60;
                                    }
                                    if (iArr3[0] == i && iArr3[1] == 0 && iArr3[2] == 0 && iArr3[3] == 0) {
                                        bool = Boolean.FALSE;
                                    }
                                }
                            } else if (encoding.equals("ISO-10646-UCS-2")) {
                                int[] iArr4 = new int[4];
                                int i15 = 0;
                                while (i15 < 4) {
                                    iArr4[i15] = rewindableInputStream2.read();
                                    if (iArr4[i15] == -1) {
                                        break;
                                    }
                                    i15++;
                                }
                                rewindableInputStream2.reset();
                                if (i15 == 4) {
                                    if (iArr4[0] == 0 && iArr4[1] == 60 && iArr4[2] == 0 && iArr4[3] == 63) {
                                        bool = Boolean.TRUE;
                                    } else if (iArr4[0] == 60 && iArr4[1] == 0 && iArr4[2] == 63 && iArr4[3] == 0) {
                                        bool = Boolean.FALSE;
                                    }
                                }
                            }
                            reader2 = createReader(rewindableInputStream2, encoding, bool);
                        }
                        bool = null;
                        reader2 = createReader(rewindableInputStream2, encoding, bool);
                    }
                    rewindableInputStream = rewindableInputStream2;
                    reader = reader2;
                    str2 = expandSystemId;
                }
            }
            inputStream = byteStream;
            RewindableInputStream rewindableInputStream22 = new RewindableInputStream(inputStream);
            if (encoding != null) {
            }
            rewindableInputStream = rewindableInputStream22;
            reader = reader2;
            str2 = expandSystemId;
        } else {
            reader = characterStream;
            str2 = expandSystemId;
            rewindableInputStream = null;
        }
        Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
        if (scannedEntity != null) {
            this.fEntityStack.push(scannedEntity);
        }
        this.fCurrentEntity = new Entity.ScannedEntity(z, str, new XMLResourceIdentifierImpl(publicId, systemId, baseSystemId, str2), rewindableInputStream, reader, encoding, z2, z5, z3);
        this.fCurrentEntity.setEncodingExternallySpecified(z5);
        this.fEntityScanner.setCurrentEntity(this.fCurrentEntity);
        this.fResourceIdentifier.setValues(publicId, systemId, baseSystemId, str2);
        XMLLimitAnalyzer xMLLimitAnalyzer = this.fLimitAnalyzer;
        if (xMLLimitAnalyzer != null) {
            xMLLimitAnalyzer.startEntity(str);
        }
        return encoding;
    }

    public boolean isExternalEntity(String str) {
        Entity entity = this.fEntities.get(str);
        if (entity == null) {
            return false;
        }
        return entity.isExternal();
    }

    public boolean isEntityDeclInExternalSubset(String str) {
        Entity entity = this.fEntities.get(str);
        if (entity == null) {
            return false;
        }
        return entity.isEntityDeclInExternalSubset();
    }

    public void setStandalone(boolean z) {
        this.fStandalone = z;
    }

    public boolean isStandalone() {
        return this.fStandalone;
    }

    public boolean isDeclaredEntity(String str) {
        return this.fEntities.get(str) != null;
    }

    public boolean isUnparsedEntity(String str) {
        Entity entity = this.fEntities.get(str);
        if (entity == null) {
            return false;
        }
        return entity.isUnparsed();
    }

    public XMLResourceIdentifier getCurrentResourceIdentifier() {
        return this.fResourceIdentifier;
    }

    public void setEntityHandler(XMLEntityHandler xMLEntityHandler) {
        this.fEntityHandler = xMLEntityHandler;
    }

    public StaxXMLInputSource resolveEntityAsPerStax(XMLResourceIdentifier xMLResourceIdentifier) throws IOException {
        XMLResourceIdentifierImpl xMLResourceIdentifierImpl;
        StaxXMLInputSource staxXMLInputSource;
        Entity.ScannedEntity scannedEntity;
        XMLInputSource xMLInputSource = null;
        if (xMLResourceIdentifier == null) {
            return null;
        }
        String publicId = xMLResourceIdentifier.getPublicId();
        String literalSystemId = xMLResourceIdentifier.getLiteralSystemId();
        String baseSystemId = xMLResourceIdentifier.getBaseSystemId();
        String expandedSystemId = xMLResourceIdentifier.getExpandedSystemId();
        boolean z = expandedSystemId == null;
        if (!(baseSystemId != null || (scannedEntity = this.fCurrentEntity) == null || scannedEntity.entityLocation == null || (baseSystemId = this.fCurrentEntity.entityLocation.getExpandedSystemId()) == null)) {
            z = true;
        }
        if (z) {
            expandedSystemId = expandSystemId(literalSystemId, baseSystemId, false);
        }
        if (xMLResourceIdentifier instanceof XMLResourceIdentifierImpl) {
            xMLResourceIdentifierImpl = (XMLResourceIdentifierImpl) xMLResourceIdentifier;
        } else {
            this.fResourceIdentifier.clear();
            xMLResourceIdentifierImpl = this.fResourceIdentifier;
        }
        xMLResourceIdentifierImpl.setValues(publicId, literalSystemId, baseSystemId, expandedSystemId);
        this.fISCreatedByResolver = false;
        StaxEntityResolverWrapper staxEntityResolverWrapper = this.fStaxEntityResolver;
        if (staxEntityResolverWrapper != null) {
            staxXMLInputSource = staxEntityResolverWrapper.resolveEntity(xMLResourceIdentifierImpl);
            if (staxXMLInputSource != null) {
                this.fISCreatedByResolver = true;
            }
        } else {
            staxXMLInputSource = null;
        }
        XMLEntityResolver xMLEntityResolver = this.fEntityResolver;
        if (!(xMLEntityResolver == null || (xMLInputSource = xMLEntityResolver.resolveEntity(xMLResourceIdentifierImpl)) == null)) {
            this.fISCreatedByResolver = true;
        }
        if (xMLInputSource != null) {
            staxXMLInputSource = new StaxXMLInputSource(xMLInputSource, this.fISCreatedByResolver);
        }
        if (staxXMLInputSource == null) {
            return new StaxXMLInputSource(new XMLInputSource(publicId, literalSystemId, baseSystemId));
        }
        staxXMLInputSource.hasXMLStreamOrXMLEventReader();
        return staxXMLInputSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver
    public XMLInputSource resolveEntity(XMLResourceIdentifier xMLResourceIdentifier) throws IOException, XNIException {
        Entity.ScannedEntity scannedEntity;
        XMLInputSource xMLInputSource = null;
        if (xMLResourceIdentifier == null) {
            return null;
        }
        String publicId = xMLResourceIdentifier.getPublicId();
        String literalSystemId = xMLResourceIdentifier.getLiteralSystemId();
        String baseSystemId = xMLResourceIdentifier.getBaseSystemId();
        String expandedSystemId = xMLResourceIdentifier.getExpandedSystemId();
        boolean z = expandedSystemId == null;
        if (!(baseSystemId != null || (scannedEntity = this.fCurrentEntity) == null || scannedEntity.entityLocation == null || (baseSystemId = this.fCurrentEntity.entityLocation.getExpandedSystemId()) == null)) {
            z = true;
        }
        if (z) {
            expandedSystemId = expandSystemId(literalSystemId, baseSystemId, false);
        }
        if (this.fEntityResolver != null) {
            xMLResourceIdentifier.setBaseSystemId(baseSystemId);
            xMLResourceIdentifier.setExpandedSystemId(expandedSystemId);
            xMLInputSource = this.fEntityResolver.resolveEntity(xMLResourceIdentifier);
        }
        return xMLInputSource == null ? new XMLInputSource(publicId, literalSystemId, baseSystemId) : xMLInputSource;
    }

    public void startEntity(boolean z, String str, boolean z2) throws IOException, XNIException {
        String str2;
        String str3;
        String str4;
        Entity.ExternalEntity externalEntity;
        XMLInputSource xMLInputSource;
        String checkAccess;
        Entity entity;
        Entity entity2 = this.fEntityStorage.getEntity(str);
        if (entity2 != null) {
            boolean isExternal = entity2.isExternal();
            if (isExternal) {
                externalEntity = (Entity.ExternalEntity) entity2;
                str4 = externalEntity.entityLocation != null ? externalEntity.entityLocation.getLiteralSystemId() : null;
                str3 = externalEntity.entityLocation != null ? externalEntity.entityLocation.getBaseSystemId() : null;
                str2 = expandSystemId(str4, str3);
                boolean isUnparsed = entity2.isUnparsed();
                boolean startsWith = str.startsWith("%");
                boolean z3 = !startsWith;
                if (isUnparsed || ((z3 && !this.fExternalGeneralEntities) || ((startsWith && !this.fExternalParameterEntities) || !this.fSupportDTD || !this.fSupportExternalEntities))) {
                    if (this.fEntityHandler != null) {
                        this.fResourceIdentifier.clear();
                        this.fResourceIdentifier.setValues(externalEntity.entityLocation != null ? externalEntity.entityLocation.getPublicId() : null, str4, str3, str2);
                        this.fEntityAugs.removeAllItems();
                        this.fEntityAugs.putItem(Constants.ENTITY_SKIPPED, Boolean.TRUE);
                        this.fEntityHandler.startEntity(str, this.fResourceIdentifier, null, this.fEntityAugs);
                        this.fEntityAugs.removeAllItems();
                        this.fEntityAugs.putItem(Constants.ENTITY_SKIPPED, Boolean.TRUE);
                        this.fEntityHandler.endEntity(str, this.fEntityAugs);
                        return;
                    }
                    return;
                }
            } else {
                externalEntity = null;
                str4 = null;
                str3 = null;
                str2 = null;
            }
            int size = this.fEntityStack.size();
            for (int i = size; i >= 0; i--) {
                if (i == size) {
                    entity = this.fCurrentEntity;
                } else {
                    entity = this.fEntityStack.elementAt(i);
                }
                if (entity.name == str) {
                    String str5 = str;
                    for (int i2 = i + 1; i2 < size; i2++) {
                        str5 = str5 + " -> " + this.fEntityStack.elementAt(i2).name;
                    }
                    this.fErrorReporter.reportError((XMLLocator) getEntityScanner(), "http://www.w3.org/TR/1998/REC-xml-19980210", "RecursiveReference", new Object[]{str, (str5 + " -> " + this.fCurrentEntity.name) + " -> " + str}, (short) 2);
                    if (this.fEntityHandler != null) {
                        this.fResourceIdentifier.clear();
                        if (isExternal) {
                            this.fResourceIdentifier.setValues(externalEntity.entityLocation != null ? externalEntity.entityLocation.getPublicId() : null, str4, str3, str2);
                        }
                        this.fEntityAugs.removeAllItems();
                        this.fEntityAugs.putItem(Constants.ENTITY_SKIPPED, Boolean.TRUE);
                        this.fEntityHandler.startEntity(str, this.fResourceIdentifier, null, this.fEntityAugs);
                        this.fEntityAugs.removeAllItems();
                        this.fEntityAugs.putItem(Constants.ENTITY_SKIPPED, Boolean.TRUE);
                        this.fEntityHandler.endEntity(str, this.fEntityAugs);
                        return;
                    }
                    return;
                }
            }
            if (isExternal) {
                xMLInputSource = resolveEntityAsPerStax(externalEntity.entityLocation).getXMLInputSource();
                if (!this.fISCreatedByResolver && this.fLoadExternalDTD && (checkAccess = SecuritySupport.checkAccess(str2, this.fAccessExternalDTD, "all")) != null) {
                    this.fErrorReporter.reportError((XMLLocator) getEntityScanner(), "http://www.w3.org/TR/1998/REC-xml-19980210", "AccessExternalEntity", new Object[]{SecuritySupport.sanitizePath(str2), checkAccess}, (short) 2);
                }
            } else {
                xMLInputSource = new XMLInputSource((String) null, (String) null, (String) null, new StringReader(((Entity.InternalEntity) entity2).text), (String) null);
            }
            startEntity(z, str, xMLInputSource, z2, isExternal);
        } else if (this.fEntityHandler != null) {
            this.fResourceIdentifier.clear();
            this.fEntityAugs.removeAllItems();
            this.fEntityAugs.putItem(Constants.ENTITY_SKIPPED, Boolean.TRUE);
            this.fEntityHandler.startEntity(str, this.fResourceIdentifier, null, this.fEntityAugs);
            this.fEntityAugs.removeAllItems();
            this.fEntityAugs.putItem(Constants.ENTITY_SKIPPED, Boolean.TRUE);
            this.fEntityHandler.endEntity(str, this.fEntityAugs);
        }
    }

    public void startDocumentEntity(XMLInputSource xMLInputSource) throws IOException, XNIException {
        startEntity(false, XMLEntity, xMLInputSource, false, true);
    }

    public void startDTDEntity(XMLInputSource xMLInputSource) throws IOException, XNIException {
        startEntity(false, DTDEntity, xMLInputSource, false, true);
    }

    public void startExternalSubset() {
        this.fInExternalSubset = true;
    }

    public void endExternalSubset() {
        this.fInExternalSubset = false;
    }

    public void startEntity(boolean z, String str, XMLInputSource xMLInputSource, boolean z2, boolean z3) throws IOException, XNIException {
        String str2 = setupCurrentEntity(z, str, xMLInputSource, z2, z3);
        this.fEntityExpansionCount++;
        XMLLimitAnalyzer xMLLimitAnalyzer = this.fLimitAnalyzer;
        if (xMLLimitAnalyzer != null) {
            xMLLimitAnalyzer.addValue(this.entityExpansionIndex, str, 1);
        }
        XMLSecurityManager xMLSecurityManager = this.fSecurityManager;
        if (xMLSecurityManager != null && xMLSecurityManager.isOverLimit(this.entityExpansionIndex, this.fLimitAnalyzer)) {
            this.fSecurityManager.debugPrint(this.fLimitAnalyzer);
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "EntityExpansionLimit", new Object[]{this.fSecurityManager.getLimitValueByIndex(this.entityExpansionIndex)}, 2);
            this.fEntityExpansionCount = 0;
        }
        XMLEntityHandler xMLEntityHandler = this.fEntityHandler;
        if (xMLEntityHandler != null) {
            xMLEntityHandler.startEntity(str, this.fResourceIdentifier, str2, null);
        }
    }

    public Entity.ScannedEntity getCurrentEntity() {
        return this.fCurrentEntity;
    }

    public Entity.ScannedEntity getTopLevelEntity() {
        return (Entity.ScannedEntity) (this.fEntityStack.empty() ? null : this.fEntityStack.elementAt(0));
    }

    public void endEntity() throws IOException, XNIException {
        Entity.ScannedEntity scannedEntity = this.fEntityStack.size() > 0 ? (Entity.ScannedEntity) this.fEntityStack.pop() : null;
        if (this.fCurrentEntity != null) {
            try {
                if (this.fLimitAnalyzer != null) {
                    this.fLimitAnalyzer.endEntity(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT, this.fCurrentEntity.name);
                    if (this.fCurrentEntity.name.equals("[xml]")) {
                        this.fSecurityManager.debugPrint(this.fLimitAnalyzer);
                    }
                }
                this.fCurrentEntity.close();
            } catch (IOException e) {
                throw new XNIException(e);
            }
        }
        XMLEntityHandler xMLEntityHandler = this.fEntityHandler;
        if (xMLEntityHandler != null) {
            if (scannedEntity == null) {
                this.fEntityAugs.removeAllItems();
                this.fEntityAugs.putItem(Constants.LAST_ENTITY, Boolean.TRUE);
                this.fEntityHandler.endEntity(this.fCurrentEntity.name, this.fEntityAugs);
                this.fEntityAugs.removeAllItems();
            } else {
                xMLEntityHandler.endEntity(this.fCurrentEntity.name, null);
            }
        }
        boolean z = false;
        boolean z2 = this.fCurrentEntity.name == XMLEntity;
        this.fCurrentEntity = scannedEntity;
        this.fEntityScanner.setCurrentEntity(this.fCurrentEntity);
        if (this.fCurrentEntity == null) {
            z = true;
        }
        if ((!z2) && z) {
            throw new EOFException();
        }
    }

    public void reset(PropertyManager propertyManager) {
        this.fSymbolTable = (SymbolTable) propertyManager.getProperty("http://apache.org/xml/properties/internal/symbol-table");
        this.fErrorReporter = (XMLErrorReporter) propertyManager.getProperty("http://apache.org/xml/properties/internal/error-reporter");
        try {
            this.fStaxEntityResolver = (StaxEntityResolverWrapper) propertyManager.getProperty(STAX_ENTITY_RESOLVER);
        } catch (XMLConfigurationException unused) {
            this.fStaxEntityResolver = null;
        }
        this.fSupportDTD = ((Boolean) propertyManager.getProperty("javax.xml.stream.supportDTD")).booleanValue();
        this.fReplaceEntityReferences = ((Boolean) propertyManager.getProperty("javax.xml.stream.isReplacingEntityReferences")).booleanValue();
        this.fSupportExternalEntities = ((Boolean) propertyManager.getProperty("javax.xml.stream.isSupportingExternalEntities")).booleanValue();
        this.fLoadExternalDTD = !((Boolean) propertyManager.getProperty("http://java.sun.com/xml/stream/properties/ignore-external-dtd")).booleanValue();
        this.fAccessExternalDTD = ((XMLSecurityPropertyManager) propertyManager.getProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager")).getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD);
        this.fSecurityManager = (XMLSecurityManager) propertyManager.getProperty("http://apache.org/xml/properties/security-manager");
        this.fLimitAnalyzer = new XMLLimitAnalyzer();
        this.fEntityStorage.reset(propertyManager);
        this.fEntityScanner.reset(propertyManager);
        this.fEntities.clear();
        this.fEntityStack.removeAllElements();
        this.fCurrentEntity = null;
        this.fValidation = false;
        this.fExternalGeneralEntities = true;
        this.fExternalParameterEntities = true;
        this.fAllowJavaEncodings = true;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void reset(XMLComponentManager xMLComponentManager) throws XMLConfigurationException {
        if (!xMLComponentManager.getFeature(PARSER_SETTINGS, true)) {
            reset();
            XMLEntityScanner xMLEntityScanner = this.fEntityScanner;
            if (xMLEntityScanner != null) {
                xMLEntityScanner.reset(xMLComponentManager);
            }
            XMLEntityStorage xMLEntityStorage = this.fEntityStorage;
            if (xMLEntityStorage != null) {
                xMLEntityStorage.reset(xMLComponentManager);
                return;
            }
            return;
        }
        this.fValidation = xMLComponentManager.getFeature(VALIDATION, false);
        this.fExternalGeneralEntities = xMLComponentManager.getFeature(EXTERNAL_GENERAL_ENTITIES, true);
        this.fExternalParameterEntities = xMLComponentManager.getFeature(EXTERNAL_PARAMETER_ENTITIES, true);
        this.fAllowJavaEncodings = xMLComponentManager.getFeature(ALLOW_JAVA_ENCODINGS, false);
        this.fWarnDuplicateEntityDef = xMLComponentManager.getFeature(WARN_ON_DUPLICATE_ENTITYDEF, false);
        this.fStrictURI = xMLComponentManager.getFeature(STANDARD_URI_CONFORMANT, false);
        this.fLoadExternalDTD = xMLComponentManager.getFeature(LOAD_EXTERNAL_DTD, true);
        this.fSymbolTable = (SymbolTable) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/symbol-table");
        this.fErrorReporter = (XMLErrorReporter) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/error-reporter");
        this.fEntityResolver = (XMLEntityResolver) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/entity-resolver", null);
        this.fStaxEntityResolver = (StaxEntityResolverWrapper) xMLComponentManager.getProperty(STAX_ENTITY_RESOLVER, null);
        this.fValidationManager = (ValidationManager) xMLComponentManager.getProperty(VALIDATION_MANAGER, null);
        this.fSecurityManager = (XMLSecurityManager) xMLComponentManager.getProperty("http://apache.org/xml/properties/security-manager", null);
        this.entityExpansionIndex = this.fSecurityManager.getIndex("http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit");
        this.fSupportDTD = true;
        this.fReplaceEntityReferences = true;
        this.fSupportExternalEntities = true;
        XMLSecurityPropertyManager xMLSecurityPropertyManager = (XMLSecurityPropertyManager) xMLComponentManager.getProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", null);
        if (xMLSecurityPropertyManager == null) {
            xMLSecurityPropertyManager = new XMLSecurityPropertyManager();
        }
        this.fAccessExternalDTD = xMLSecurityPropertyManager.getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD);
        reset();
        this.fEntityScanner.reset(xMLComponentManager);
        this.fEntityStorage.reset(xMLComponentManager);
    }

    public void reset() {
        this.fLimitAnalyzer = new XMLLimitAnalyzer();
        this.fStandalone = false;
        this.fEntities.clear();
        this.fEntityStack.removeAllElements();
        this.fEntityExpansionCount = 0;
        this.fCurrentEntity = null;
        XMLEntityScanner xMLEntityScanner = this.fXML10EntityScanner;
        if (xMLEntityScanner != null) {
            xMLEntityScanner.reset(this.fSymbolTable, this, this.fErrorReporter);
        }
        XMLEntityScanner xMLEntityScanner2 = this.fXML11EntityScanner;
        if (xMLEntityScanner2 != null) {
            xMLEntityScanner2.reset(this.fSymbolTable, this, this.fErrorReporter);
        }
        this.fEntityHandler = null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public String[] getRecognizedFeatures() {
        return (String[]) RECOGNIZED_FEATURES.clone();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setFeature(String str, boolean z) throws XMLConfigurationException {
        if (str.startsWith(Constants.XERCES_FEATURE_PREFIX)) {
            int length = str.length() - 31;
            if (length == 20 && str.endsWith(Constants.ALLOW_JAVA_ENCODINGS_FEATURE)) {
                this.fAllowJavaEncodings = z;
            }
            if (length == 31 && str.endsWith(Constants.LOAD_EXTERNAL_DTD_FEATURE)) {
                this.fLoadExternalDTD = z;
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setProperty(String str, Object obj) {
        Integer num;
        if (str.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            int length = str.length() - 33;
            if (length == 21 && str.endsWith(Constants.SYMBOL_TABLE_PROPERTY)) {
                this.fSymbolTable = (SymbolTable) obj;
                return;
            } else if (length == 23 && str.endsWith(Constants.ERROR_REPORTER_PROPERTY)) {
                this.fErrorReporter = (XMLErrorReporter) obj;
                return;
            } else if (length != 24 || !str.endsWith(Constants.ENTITY_RESOLVER_PROPERTY)) {
                if (length == 17 && str.endsWith(Constants.BUFFER_SIZE_PROPERTY) && (num = (Integer) obj) != null && num.intValue() > 64) {
                    this.fBufferSize = num.intValue();
                    this.fEntityScanner.setBufferSize(this.fBufferSize);
                    this.fBufferPool.setExternalBufferSize(this.fBufferSize);
                }
                if (length == 16 && str.endsWith(Constants.SECURITY_MANAGER_PROPERTY)) {
                    this.fSecurityManager = (XMLSecurityManager) obj;
                }
            } else {
                this.fEntityResolver = (XMLEntityResolver) obj;
                return;
            }
        }
        if (str.equals("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager")) {
            this.fAccessExternalDTD = ((XMLSecurityPropertyManager) obj).getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD);
        }
    }

    public void setLimitAnalyzer(XMLLimitAnalyzer xMLLimitAnalyzer) {
        this.fLimitAnalyzer = xMLLimitAnalyzer;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public String[] getRecognizedProperties() {
        return (String[]) RECOGNIZED_PROPERTIES.clone();
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

    public static String expandSystemId(String str) {
        return expandSystemId(str, null);
    }

    private static synchronized URI getUserDir() throws URI.MalformedURIException {
        char charAt;
        char upperCase;
        synchronized (XMLEntityManager.class) {
            String str = "";
            try {
                str = SecuritySupport.getSystemProperty("user.dir");
            } catch (SecurityException unused) {
            }
            if (str.length() == 0) {
                return new URI(AsrConstants.ASR_SRC_FILE, "", "", null, null);
            }
            if (gUserDirURI != null && str.equals(gUserDir)) {
                return gUserDirURI;
            }
            gUserDir = str;
            String replace = str.replace(File.separatorChar, '/');
            int length = replace.length();
            StringBuffer stringBuffer = new StringBuffer(length * 3);
            if (length >= 2 && replace.charAt(1) == ':' && (upperCase = Character.toUpperCase(replace.charAt(0))) >= 'A' && upperCase <= 'Z') {
                stringBuffer.append('/');
            }
            int i = 0;
            while (i < length && (charAt = replace.charAt(i)) < 128) {
                if (gNeedEscaping[charAt]) {
                    stringBuffer.append('%');
                    stringBuffer.append(gAfterEscaping1[charAt]);
                    stringBuffer.append(gAfterEscaping2[charAt]);
                } else {
                    stringBuffer.append((char) charAt);
                }
                i++;
            }
            if (i < length) {
                try {
                    byte[] bytes = replace.substring(i).getBytes("UTF-8");
                    for (byte b : bytes) {
                        if (b < 0) {
                            int i2 = b + 256;
                            stringBuffer.append('%');
                            stringBuffer.append(gHexChs[i2 >> 4]);
                            stringBuffer.append(gHexChs[i2 & 15]);
                        } else if (gNeedEscaping[b]) {
                            stringBuffer.append('%');
                            stringBuffer.append(gAfterEscaping1[b]);
                            stringBuffer.append(gAfterEscaping2[b]);
                        } else {
                            stringBuffer.append((char) b);
                        }
                    }
                } catch (UnsupportedEncodingException unused2) {
                    return new URI(AsrConstants.ASR_SRC_FILE, "", replace, null, null);
                }
            }
            if (!replace.endsWith(PsuedoNames.PSEUDONAME_ROOT)) {
                stringBuffer.append('/');
            }
            gUserDirURI = new URI(AsrConstants.ASR_SRC_FILE, "", stringBuffer.toString(), null, null);
            return gUserDirURI;
        }
    }

    public static void absolutizeAgainstUserDir(URI uri) throws URI.MalformedURIException {
        uri.absolutize(getUserDir());
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x008d A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x008e  */
    public static String expandSystemId(String str, String str2) {
        URI uri;
        URI uri2;
        if (str == null || str.length() == 0) {
            return str;
        }
        try {
            new URI(str);
            return str;
        } catch (URI.MalformedURIException unused) {
            String fixURI = fixURI(str);
            URI uri3 = null;
            if (str2 != null) {
                try {
                    if (str2.length() != 0 && !str2.equals(str)) {
                        try {
                            uri2 = new URI(fixURI(str2));
                        } catch (URI.MalformedURIException unused2) {
                            if (str2.indexOf(58) != -1) {
                                uri2 = new URI(AsrConstants.ASR_SRC_FILE, "", fixURI(str2), null, null);
                            } else {
                                String uri4 = getUserDir().toString();
                                uri = new URI(AsrConstants.ASR_SRC_FILE, "", uri4 + fixURI(str2), null, null);
                            }
                        }
                        uri = uri2;
                        uri3 = new URI(uri, fixURI);
                        if (uri3 == null) {
                            return str;
                        }
                        return uri3.toString();
                    }
                } catch (Exception unused3) {
                    if (uri3 == null) {
                    }
                }
            }
            uri = new URI(AsrConstants.ASR_SRC_FILE, "", getUserDir().toString(), null, null);
            uri3 = new URI(uri, fixURI);
            if (uri3 == null) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x00cd A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00ce  */
    public static String expandSystemId(String str, String str2, boolean z) throws URI.MalformedURIException {
        URI uri;
        URI uri2;
        URI uri3;
        if (str == null) {
            return null;
        }
        if (z) {
            try {
                new URI(str);
                return str;
            } catch (URI.MalformedURIException unused) {
                if (str2 == null || str2.length() == 0) {
                    uri3 = new URI(AsrConstants.ASR_SRC_FILE, "", getUserDir().toString(), null, null);
                } else {
                    try {
                        uri3 = new URI(str2);
                    } catch (URI.MalformedURIException unused2) {
                        uri3 = new URI(AsrConstants.ASR_SRC_FILE, "", getUserDir().toString() + str2, null, null);
                    }
                }
                return new URI(uri3, str).toString();
            }
        } else {
            try {
                return expandSystemIdStrictOff(str, str2);
            } catch (URI.MalformedURIException unused3) {
                try {
                    return expandSystemIdStrictOff1(str, str2);
                } catch (URISyntaxException unused4) {
                    if (str.length() == 0) {
                        return str;
                    }
                    String fixURI = fixURI(str);
                    if (str2 != null) {
                        try {
                            if (str2.length() != 0 && !str2.equals(str)) {
                                try {
                                    uri2 = new URI(fixURI(str2).trim());
                                } catch (URI.MalformedURIException unused5) {
                                    uri2 = str2.indexOf(58) != -1 ? new URI(AsrConstants.ASR_SRC_FILE, "", fixURI(str2).trim(), null, null) : new URI(getUserDir(), fixURI(str2));
                                }
                                uri = new URI(uri2, fixURI.trim());
                                if (uri == null) {
                                }
                            }
                        } catch (Exception unused6) {
                            uri = null;
                            if (uri == null) {
                                return str;
                            }
                            return uri.toString();
                        }
                    }
                    uri2 = getUserDir();
                    uri = new URI(uri2, fixURI.trim());
                    if (uri == null) {
                    }
                }
            }
        }
    }

    private static String expandSystemIdStrictOn(String str, String str2) throws URI.MalformedURIException {
        URI uri;
        URI uri2 = new URI(str, true);
        if (uri2.isAbsoluteURI()) {
            return str;
        }
        if (str2 == null || str2.length() == 0) {
            uri = getUserDir();
        } else {
            uri = new URI(str2, true);
            if (!uri.isAbsoluteURI()) {
                uri.absolutize(getUserDir());
            }
        }
        uri2.absolutize(uri);
        return uri2.toString();
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x001a: APUT  (r1v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r7v1 java.lang.Boolean) */
    public static void setInstanceFollowRedirects(HttpURLConnection httpURLConnection, boolean z) {
        try {
            Method method = HttpURLConnection.class.getMethod("setInstanceFollowRedirects", Boolean.TYPE);
            Object[] objArr = new Object[1];
            objArr[0] = z ? Boolean.TRUE : Boolean.FALSE;
            method.invoke(httpURLConnection, objArr);
        } catch (Exception unused) {
        }
    }

    private static String expandSystemIdStrictOff(String str, String str2) throws URI.MalformedURIException {
        URI uri;
        URI uri2 = new URI(str, true);
        if (!uri2.isAbsoluteURI()) {
            if (str2 == null || str2.length() == 0) {
                uri = getUserDir();
            } else {
                uri = new URI(str2, true);
                if (!uri.isAbsoluteURI()) {
                    uri.absolutize(getUserDir());
                }
            }
            uri2.absolutize(uri);
            return uri2.toString();
        } else if (uri2.getScheme().length() > 1) {
            return str;
        } else {
            throw new URI.MalformedURIException();
        }
    }

    private static String expandSystemIdStrictOff1(String str, String str2) throws URISyntaxException, URI.MalformedURIException {
        URI uri;
        java.net.URI uri2 = new java.net.URI(str);
        if (!uri2.isAbsolute()) {
            if (str2 == null || str2.length() == 0) {
                uri = getUserDir();
            } else {
                uri = new URI(str2, true);
                if (!uri.isAbsoluteURI()) {
                    uri.absolutize(getUserDir());
                }
            }
            return new java.net.URI(uri.toString()).resolve(uri2).toString();
        } else if (uri2.getScheme().length() > 1) {
            return str;
        } else {
            throw new URISyntaxException(str, "the scheme's length is only one character");
        }
    }

    /* access modifiers changed from: protected */
    public Object[] getEncodingName(byte[] bArr, int i) {
        if (i < 2) {
            return this.defaultEncoding;
        }
        int i2 = bArr[0] & 255;
        int i3 = bArr[1] & 255;
        if (i2 == 254 && i3 == 255) {
            return new Object[]{"UTF-16BE", new Boolean(true)};
        }
        if (i2 == 255 && i3 == 254) {
            return new Object[]{"UTF-16LE", new Boolean(false)};
        }
        if (i < 3) {
            return this.defaultEncoding;
        }
        int i4 = bArr[2] & 255;
        if (i2 == 239 && i3 == 187 && i4 == 191) {
            return this.defaultEncoding;
        }
        if (i < 4) {
            return this.defaultEncoding;
        }
        int i5 = bArr[3] & 255;
        return (i2 == 0 && i3 == 0 && i4 == 0 && i5 == 60) ? new Object[]{"ISO-10646-UCS-4", new Boolean(true)} : (i2 == 60 && i3 == 0 && i4 == 0 && i5 == 0) ? new Object[]{"ISO-10646-UCS-4", new Boolean(false)} : (i2 == 0 && i3 == 0 && i4 == 60 && i5 == 0) ? new Object[]{"ISO-10646-UCS-4", null} : (i2 == 0 && i3 == 60 && i4 == 0 && i5 == 0) ? new Object[]{"ISO-10646-UCS-4", null} : (i2 == 0 && i3 == 60 && i4 == 0 && i5 == 63) ? new Object[]{"UTF-16BE", new Boolean(true)} : (i2 == 60 && i3 == 0 && i4 == 63 && i5 == 0) ? new Object[]{"UTF-16LE", new Boolean(false)} : (i2 == 76 && i3 == 111 && i4 == 167 && i5 == 148) ? new Object[]{"CP037", null} : this.defaultEncoding;
    }

    /* access modifiers changed from: protected */
    public Reader createReader(InputStream inputStream, String str, Boolean bool) throws IOException {
        if (str == null) {
            str = "UTF-8";
        }
        String upperCase = str.toUpperCase(Locale.ENGLISH);
        if (upperCase.equals("UTF-8")) {
            return new UTF8Reader(inputStream, this.fBufferSize, this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210"), this.fErrorReporter.getLocale());
        }
        if (upperCase.equals("US-ASCII")) {
            return new ASCIIReader(inputStream, this.fBufferSize, this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210"), this.fErrorReporter.getLocale());
        }
        if (upperCase.equals("ISO-10646-UCS-4")) {
            if (bool == null) {
                this.fErrorReporter.reportError((XMLLocator) getEntityScanner(), "http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingByteOrderUnsupported", new Object[]{str}, (short) 2);
            } else if (bool.booleanValue()) {
                return new UCSReader(inputStream, 8);
            } else {
                return new UCSReader(inputStream, 4);
            }
        }
        if (upperCase.equals("ISO-10646-UCS-2")) {
            if (bool == null) {
                this.fErrorReporter.reportError((XMLLocator) getEntityScanner(), "http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingByteOrderUnsupported", new Object[]{str}, (short) 2);
            } else if (bool.booleanValue()) {
                return new UCSReader(inputStream, 2);
            } else {
                return new UCSReader(inputStream, 1);
            }
        }
        boolean isValidIANAEncoding = XMLChar.isValidIANAEncoding(str);
        boolean isValidJavaEncoding = XMLChar.isValidJavaEncoding(str);
        if (!isValidIANAEncoding || (this.fAllowJavaEncodings && !isValidJavaEncoding)) {
            this.fErrorReporter.reportError((XMLLocator) getEntityScanner(), "http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingDeclInvalid", new Object[]{str}, (short) 2);
            str = "ISO-8859-1";
        }
        String iANA2JavaMapping = EncodingMap.getIANA2JavaMapping(upperCase);
        if (iANA2JavaMapping != null) {
            str = iANA2JavaMapping;
        } else if (!this.fAllowJavaEncodings) {
            this.fErrorReporter.reportError((XMLLocator) getEntityScanner(), "http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingDeclInvalid", new Object[]{str}, (short) 2);
            str = "ISO8859_1";
        }
        return new BufferedReader(new InputStreamReader(inputStream, str));
    }

    public String getPublicId() {
        Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
        if (scannedEntity == null || scannedEntity.entityLocation == null) {
            return null;
        }
        return this.fCurrentEntity.entityLocation.getPublicId();
    }

    public String getExpandedSystemId() {
        Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
        if (scannedEntity == null) {
            return null;
        }
        if (!(scannedEntity.entityLocation == null || this.fCurrentEntity.entityLocation.getExpandedSystemId() == null)) {
            return this.fCurrentEntity.entityLocation.getExpandedSystemId();
        }
        for (int size = this.fEntityStack.size() - 1; size >= 0; size--) {
            Entity.ScannedEntity scannedEntity2 = (Entity.ScannedEntity) this.fEntityStack.elementAt(size);
            if (!(scannedEntity2.entityLocation == null || scannedEntity2.entityLocation.getExpandedSystemId() == null)) {
                return scannedEntity2.entityLocation.getExpandedSystemId();
            }
        }
        return null;
    }

    public String getLiteralSystemId() {
        Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
        if (scannedEntity == null) {
            return null;
        }
        if (!(scannedEntity.entityLocation == null || this.fCurrentEntity.entityLocation.getLiteralSystemId() == null)) {
            return this.fCurrentEntity.entityLocation.getLiteralSystemId();
        }
        for (int size = this.fEntityStack.size() - 1; size >= 0; size--) {
            Entity.ScannedEntity scannedEntity2 = (Entity.ScannedEntity) this.fEntityStack.elementAt(size);
            if (!(scannedEntity2.entityLocation == null || scannedEntity2.entityLocation.getLiteralSystemId() == null)) {
                return scannedEntity2.entityLocation.getLiteralSystemId();
            }
        }
        return null;
    }

    public int getLineNumber() {
        Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
        if (scannedEntity == null) {
            return -1;
        }
        if (scannedEntity.isExternal()) {
            return this.fCurrentEntity.lineNumber;
        }
        for (int size = this.fEntityStack.size() - 1; size > 0; size--) {
            Entity.ScannedEntity scannedEntity2 = (Entity.ScannedEntity) this.fEntityStack.elementAt(size);
            if (scannedEntity2.isExternal()) {
                return scannedEntity2.lineNumber;
            }
        }
        return -1;
    }

    public int getColumnNumber() {
        Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
        if (scannedEntity == null) {
            return -1;
        }
        if (scannedEntity.isExternal()) {
            return this.fCurrentEntity.columnNumber;
        }
        for (int size = this.fEntityStack.size() - 1; size > 0; size--) {
            Entity.ScannedEntity scannedEntity2 = (Entity.ScannedEntity) this.fEntityStack.elementAt(size);
            if (scannedEntity2.isExternal()) {
                return scannedEntity2.columnNumber;
            }
        }
        return -1;
    }

    protected static String fixURI(String str) {
        String replace = str.replace(File.separatorChar, '/');
        if (replace.length() >= 2) {
            char charAt = replace.charAt(1);
            if (charAt == ':') {
                char upperCase = Character.toUpperCase(replace.charAt(0));
                if (upperCase >= 'A' && upperCase <= 'Z') {
                    replace = PsuedoNames.PSEUDONAME_ROOT + replace;
                }
            } else if (charAt == '/' && replace.charAt(0) == '/') {
                replace = "file:" + replace;
            }
        }
        int indexOf = replace.indexOf(32);
        if (indexOf < 0) {
            return replace;
        }
        StringBuilder sb = new StringBuilder(replace.length());
        for (int i = 0; i < indexOf; i++) {
            sb.append(replace.charAt(i));
        }
        sb.append("%20");
        for (int i2 = indexOf + 1; i2 < replace.length(); i2++) {
            if (replace.charAt(i2) == ' ') {
                sb.append("%20");
            } else {
                sb.append(replace.charAt(i2));
            }
        }
        return sb.toString();
    }

    /* access modifiers changed from: private */
    public static class CharacterBuffer {
        private char[] ch;
        private boolean isExternal;

        public CharacterBuffer(boolean z, int i) {
            this.isExternal = z;
            this.ch = new char[i];
        }
    }

    private static class CharacterBufferPool {
        private static final int DEFAULT_POOL_SIZE = 3;
        private CharacterBuffer[] fExternalBufferPool;
        private int fExternalBufferSize;
        private int fExternalTop;
        private CharacterBuffer[] fInternalBufferPool;
        private int fInternalBufferSize;
        private int fInternalTop;
        private int poolSize;

        public CharacterBufferPool(int i, int i2) {
            this(3, i, i2);
        }

        public CharacterBufferPool(int i, int i2, int i3) {
            this.fExternalBufferSize = i2;
            this.fInternalBufferSize = i3;
            this.poolSize = i;
            init();
        }

        private void init() {
            int i = this.poolSize;
            this.fInternalBufferPool = new CharacterBuffer[i];
            this.fExternalBufferPool = new CharacterBuffer[i];
            this.fInternalTop = -1;
            this.fExternalTop = -1;
        }

        public CharacterBuffer getBuffer(boolean z) {
            if (z) {
                int i = this.fExternalTop;
                if (i <= -1) {
                    return new CharacterBuffer(true, this.fExternalBufferSize);
                }
                CharacterBuffer[] characterBufferArr = this.fExternalBufferPool;
                this.fExternalTop = i - 1;
                return characterBufferArr[i];
            }
            int i2 = this.fInternalTop;
            if (i2 <= -1) {
                return new CharacterBuffer(false, this.fInternalBufferSize);
            }
            CharacterBuffer[] characterBufferArr2 = this.fInternalBufferPool;
            this.fInternalTop = i2 - 1;
            return characterBufferArr2[i2];
        }

        public void returnToPool(CharacterBuffer characterBuffer) {
            if (characterBuffer.isExternal) {
                int i = this.fExternalTop;
                CharacterBuffer[] characterBufferArr = this.fExternalBufferPool;
                if (i < characterBufferArr.length - 1) {
                    int i2 = i + 1;
                    this.fExternalTop = i2;
                    characterBufferArr[i2] = characterBuffer;
                    return;
                }
                return;
            }
            int i3 = this.fInternalTop;
            CharacterBuffer[] characterBufferArr2 = this.fInternalBufferPool;
            if (i3 < characterBufferArr2.length - 1) {
                int i4 = i3 + 1;
                this.fInternalTop = i4;
                characterBufferArr2[i4] = characterBuffer;
            }
        }

        public void setExternalBufferSize(int i) {
            this.fExternalBufferSize = i;
            this.fExternalBufferPool = new CharacterBuffer[this.poolSize];
            this.fExternalTop = -1;
        }
    }

    /* access modifiers changed from: protected */
    public final class RewindableInputStream extends InputStream {
        private byte[] fData = new byte[64];
        private int fEndOffset;
        private InputStream fInputStream;
        private int fLength;
        private int fMark;
        private int fOffset;
        private int fStartOffset;

        @Override // java.io.InputStream
        public boolean markSupported() {
            return true;
        }

        public RewindableInputStream(InputStream inputStream) {
            this.fInputStream = inputStream;
            this.fStartOffset = 0;
            this.fEndOffset = -1;
            this.fOffset = 0;
            this.fLength = 0;
            this.fMark = 0;
        }

        public void setStartOffset(int i) {
            this.fStartOffset = i;
        }

        public void rewind() {
            this.fOffset = this.fStartOffset;
        }

        @Override // java.io.InputStream
        public int read() throws IOException {
            int i = this.fOffset;
            if (i < this.fLength) {
                byte[] bArr = this.fData;
                this.fOffset = i + 1;
                return bArr[i] & 255;
            } else if (i == this.fEndOffset) {
                return -1;
            } else {
                byte[] bArr2 = this.fData;
                if (i == bArr2.length) {
                    byte[] bArr3 = new byte[(i << 1)];
                    System.arraycopy(bArr2, 0, bArr3, 0, i);
                    this.fData = bArr3;
                }
                int read = this.fInputStream.read();
                if (read == -1) {
                    this.fEndOffset = this.fOffset;
                    return -1;
                }
                byte[] bArr4 = this.fData;
                int i2 = this.fLength;
                this.fLength = i2 + 1;
                bArr4[i2] = (byte) read;
                this.fOffset++;
                return read & 255;
            }
        }

        @Override // java.io.InputStream
        public int read(byte[] bArr, int i, int i2) throws IOException {
            int i3 = this.fLength;
            int i4 = this.fOffset;
            int i5 = i3 - i4;
            if (i5 != 0) {
                if (i2 >= i5) {
                    i2 = i5;
                } else if (i2 <= 0) {
                    return 0;
                }
                if (bArr != null) {
                    System.arraycopy(this.fData, this.fOffset, bArr, i, i2);
                }
                this.fOffset += i2;
                return i2;
            } else if (i4 == this.fEndOffset) {
                return -1;
            } else {
                if (XMLEntityManager.this.fCurrentEntity.mayReadChunks || !XMLEntityManager.this.fCurrentEntity.xmlDeclChunkRead) {
                    if (!XMLEntityManager.this.fCurrentEntity.xmlDeclChunkRead) {
                        XMLEntityManager.this.fCurrentEntity.xmlDeclChunkRead = true;
                        i2 = 28;
                    }
                    return this.fInputStream.read(bArr, i, i2);
                }
                int read = read();
                if (read == -1) {
                    this.fEndOffset = this.fOffset;
                    return -1;
                }
                bArr[i] = (byte) read;
                return 1;
            }
        }

        @Override // java.io.InputStream
        public long skip(long j) throws IOException {
            if (j <= 0) {
                return 0;
            }
            int i = this.fLength;
            int i2 = this.fOffset;
            int i3 = i - i2;
            if (i3 != 0) {
                long j2 = (long) i3;
                if (j <= j2) {
                    this.fOffset = (int) (((long) i2) + j);
                    return j;
                }
                this.fOffset = i2 + i3;
                if (this.fOffset == this.fEndOffset) {
                    return j2;
                }
                return this.fInputStream.skip(j - j2) + j2;
            } else if (i2 == this.fEndOffset) {
                return 0;
            } else {
                return this.fInputStream.skip(j);
            }
        }

        @Override // java.io.InputStream
        public int available() throws IOException {
            int i = this.fLength;
            int i2 = this.fOffset;
            int i3 = i - i2;
            if (i3 != 0) {
                return i3;
            }
            if (i2 == this.fEndOffset) {
                return -1;
            }
            if (XMLEntityManager.this.fCurrentEntity.mayReadChunks) {
                return this.fInputStream.available();
            }
            return 0;
        }

        @Override // java.io.InputStream
        public void mark(int i) {
            this.fMark = this.fOffset;
        }

        @Override // java.io.InputStream
        public void reset() {
            this.fOffset = this.fMark;
        }

        @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            InputStream inputStream = this.fInputStream;
            if (inputStream != null) {
                inputStream.close();
                this.fInputStream = null;
            }
        }
    }

    public void test() {
        this.fEntityStorage.addExternalEntity("entityUsecase1", null, "/space/home/stax/sun/6thJan2004/zephyr/data/test.txt", "/space/home/stax/sun/6thJan2004/zephyr/data/entity.xml");
        this.fEntityStorage.addInternalEntity("entityUsecase2", "<Test>value</Test>");
        this.fEntityStorage.addInternalEntity("entityUsecase3", "value3");
        this.fEntityStorage.addInternalEntity("text", "Hello World.");
        this.fEntityStorage.addInternalEntity("empty-element", "<foo/>");
        this.fEntityStorage.addInternalEntity("balanced-element", "<foo></foo>");
        this.fEntityStorage.addInternalEntity("balanced-element-with-text", "<foo>Hello, World</foo>");
        this.fEntityStorage.addInternalEntity("balanced-element-with-entity", "<foo>&text;</foo>");
        this.fEntityStorage.addInternalEntity("unbalanced-entity", "<foo>");
        this.fEntityStorage.addInternalEntity("recursive-entity", "<foo>&recursive-entity2;</foo>");
        this.fEntityStorage.addInternalEntity("recursive-entity2", "<bar>&recursive-entity3;</bar>");
        this.fEntityStorage.addInternalEntity("recursive-entity3", "<baz>&recursive-entity;</baz>");
        this.fEntityStorage.addInternalEntity("ch", "&#x00A9;");
        this.fEntityStorage.addInternalEntity("ch1", "&#84;");
        this.fEntityStorage.addInternalEntity("% ch2", Constants.ELEMNAME_PARAMVARIABLE_STRING);
    }
}
