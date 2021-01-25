package ohos.com.sun.org.apache.xerces.internal.impl;

import java.io.IOException;
import java.util.ArrayList;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xerces.internal.util.Status;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl;
import ohos.com.sun.org.apache.xerces.internal.util.XMLStringBuffer;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLLimitAnalyzer;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializerConstants;
import ohos.com.sun.xml.internal.stream.Entity;
import ohos.com.sun.xml.internal.stream.XMLEntityStorage;
import ohos.data.search.model.IndexType;
import ohos.global.icu.impl.PatternTokenizer;
import ohos.global.icu.impl.locale.LanguageTag;
import ohos.javax.xml.stream.events.XMLEvent;

public abstract class XMLScanner implements XMLComponent {
    protected static final boolean DEBUG_ATTR_NORMALIZATION = false;
    protected static final String ENTITY_MANAGER = "http://apache.org/xml/properties/internal/entity-manager";
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    protected static final String NAMESPACES = "http://xml.org/sax/features/namespaces";
    protected static final String NOTIFY_CHAR_REFS = "http://apache.org/xml/features/scanner/notify-char-refs";
    protected static final String PARSER_SETTINGS = "http://apache.org/xml/features/internal/parser-settings";
    private static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    protected static final String VALIDATION = "http://xml.org/sax/features/validation";
    protected static final String fAmpSymbol = "amp".intern();
    protected static final String fAposSymbol = "apos".intern();
    protected static final String fEncodingSymbol = Constants.ATTRNAME_OUTPUT_ENCODING.intern();
    protected static final String fGtSymbol = "gt".intern();
    protected static final String fLtSymbol = "lt".intern();
    protected static final String fQuotSymbol = "quot".intern();
    protected static final String fStandaloneSymbol = Constants.ATTRNAME_OUTPUT_STANDALONE.intern();
    protected static final String fVersionSymbol = "version".intern();
    protected ArrayList<XMLString> attributeValueCache = new ArrayList<>();
    protected boolean fAttributeCacheInitDone = false;
    protected int fAttributeCacheUsedCount = 0;
    protected String fCharRefLiteral = null;
    protected int fEntityDepth;
    protected XMLEntityManager fEntityManager = null;
    protected XMLEntityScanner fEntityScanner = null;
    protected XMLEntityStorage fEntityStore = null;
    protected XMLErrorReporter fErrorReporter;
    protected XMLEvent fEvent;
    protected XMLLimitAnalyzer fLimitAnalyzer = null;
    protected boolean fNamespaces;
    private boolean fNeedNonNormalizedValue = false;
    protected boolean fNotifyCharRefs = false;
    protected boolean fParserSettings = true;
    protected PropertyManager fPropertyManager = null;
    protected boolean fReportEntity;
    protected XMLResourceIdentifierImpl fResourceIdentifier = new XMLResourceIdentifierImpl();
    protected boolean fScanningAttribute;
    protected XMLSecurityManager fSecurityManager = null;
    private XMLString fString = new XMLString();
    private XMLStringBuffer fStringBuffer = new XMLStringBuffer();
    private XMLStringBuffer fStringBuffer2 = new XMLStringBuffer();
    private XMLStringBuffer fStringBuffer3 = new XMLStringBuffer();
    protected int fStringBufferIndex = 0;
    protected SymbolTable fSymbolTable;
    protected boolean fValidation = false;
    int initialCacheCount = 6;
    protected ArrayList<XMLStringBuffer> stringBufferCache = new ArrayList<>();

    /* access modifiers changed from: protected */
    public boolean isValidNameStartHighSurrogate(int i) {
        return false;
    }

    public enum NameType {
        ATTRIBUTE("attribute"),
        ATTRIBUTENAME("attribute name"),
        COMMENT(Constants.ELEMNAME_COMMENT_STRING),
        DOCTYPE("doctype"),
        ELEMENTSTART("startelement"),
        ELEMENTEND("endelement"),
        ENTITY("entity"),
        NOTATION("notation"),
        PI(Constants.ELEMNAME_PI_OLD_STRING),
        REFERENCE("reference");
        
        final String literal;

        private NameType(String str) {
            this.literal = str;
        }

        /* access modifiers changed from: package-private */
        public String literal() {
            return this.literal;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void reset(XMLComponentManager xMLComponentManager) throws XMLConfigurationException {
        this.fParserSettings = xMLComponentManager.getFeature(PARSER_SETTINGS, true);
        if (!this.fParserSettings) {
            init();
            return;
        }
        this.fSymbolTable = (SymbolTable) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/symbol-table");
        this.fErrorReporter = (XMLErrorReporter) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/error-reporter");
        this.fEntityManager = (XMLEntityManager) xMLComponentManager.getProperty(ENTITY_MANAGER);
        this.fSecurityManager = (XMLSecurityManager) xMLComponentManager.getProperty("http://apache.org/xml/properties/security-manager");
        this.fEntityStore = this.fEntityManager.getEntityStore();
        this.fValidation = xMLComponentManager.getFeature(VALIDATION, false);
        this.fNamespaces = xMLComponentManager.getFeature("http://xml.org/sax/features/namespaces", true);
        this.fNotifyCharRefs = xMLComponentManager.getFeature(NOTIFY_CHAR_REFS, false);
        init();
    }

    /* access modifiers changed from: protected */
    public void setPropertyManager(PropertyManager propertyManager) {
        this.fPropertyManager = propertyManager;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setProperty(String str, Object obj) throws XMLConfigurationException {
        if (str.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            String substring = str.substring(33);
            if (substring.equals(Constants.SYMBOL_TABLE_PROPERTY)) {
                this.fSymbolTable = (SymbolTable) obj;
            } else if (substring.equals(Constants.ERROR_REPORTER_PROPERTY)) {
                this.fErrorReporter = (XMLErrorReporter) obj;
            } else if (substring.equals(Constants.ENTITY_MANAGER_PROPERTY)) {
                this.fEntityManager = (XMLEntityManager) obj;
            }
        }
        if (str.equals("http://apache.org/xml/properties/security-manager")) {
            this.fSecurityManager = (XMLSecurityManager) obj;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setFeature(String str, boolean z) throws XMLConfigurationException {
        if (VALIDATION.equals(str)) {
            this.fValidation = z;
        } else if (NOTIFY_CHAR_REFS.equals(str)) {
            this.fNotifyCharRefs = z;
        }
    }

    public boolean getFeature(String str) throws XMLConfigurationException {
        if (VALIDATION.equals(str)) {
            return this.fValidation;
        }
        if (NOTIFY_CHAR_REFS.equals(str)) {
            return this.fNotifyCharRefs;
        }
        throw new XMLConfigurationException(Status.NOT_RECOGNIZED, str);
    }

    /* access modifiers changed from: protected */
    public void reset() {
        init();
        this.fValidation = true;
        this.fNotifyCharRefs = false;
    }

    public void reset(PropertyManager propertyManager) {
        init();
        this.fSymbolTable = (SymbolTable) propertyManager.getProperty("http://apache.org/xml/properties/internal/symbol-table");
        this.fErrorReporter = (XMLErrorReporter) propertyManager.getProperty("http://apache.org/xml/properties/internal/error-reporter");
        this.fEntityManager = (XMLEntityManager) propertyManager.getProperty(ENTITY_MANAGER);
        this.fEntityStore = this.fEntityManager.getEntityStore();
        this.fEntityScanner = this.fEntityManager.getEntityScanner();
        this.fSecurityManager = (XMLSecurityManager) propertyManager.getProperty("http://apache.org/xml/properties/security-manager");
        this.fValidation = false;
        this.fNotifyCharRefs = false;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r19v0, resolved type: ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner */
    /* JADX DEBUG: Multi-variable search result rejected for r1v1, resolved type: ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityScanner */
    /* JADX DEBUG: Multi-variable search result rejected for r1v3, resolved type: ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityScanner */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v2, types: [ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner$NameType, java.lang.Object[]] */
    /* JADX WARN: Type inference failed for: r3v3 */
    /* JADX WARN: Type inference failed for: r3v5 */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00a0, code lost:
        if (r20 != false) goto L_0x00a2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00a5, code lost:
        r16 = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x015b, code lost:
        if (r20 != false) goto L_0x00a2;
     */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void scanXMLDeclOrTextDecl(boolean z, String[] strArr) throws IOException, XNIException {
        ?? r3;
        String xMLString;
        String xMLString2;
        boolean skipSpaces = this.fEntityScanner.skipSpaces();
        Entity.ScannedEntity currentEntity = this.fEntityManager.getCurrentEntity();
        boolean z2 = currentEntity.literal;
        currentEntity.literal = false;
        char c = 1;
        boolean z3 = skipSpaces;
        char c2 = 0;
        boolean z4 = false;
        String str = null;
        String str2 = null;
        String str3 = null;
        while (this.fEntityScanner.peekChar() != 63) {
            String scanPseudoAttribute = scanPseudoAttribute(z, this.fString);
            if (c2 != 0) {
                if (c2 != c) {
                    if (c2 != 2) {
                        reportFatalError("NoMorePseudoAttributes", null);
                    } else if (scanPseudoAttribute.equals(fStandaloneSymbol)) {
                        if (!z3) {
                            reportFatalError("SpaceRequiredBeforeStandalone", null);
                        }
                        xMLString2 = this.fString.toString();
                        if (!xMLString2.equals("yes") && !xMLString2.equals(IndexType.NO)) {
                            reportFatalError("SDDeclInvalid", new Object[]{xMLString2});
                        }
                    } else {
                        reportFatalError("SDDeclNameInvalid", null);
                    }
                    z3 = this.fEntityScanner.skipSpaces();
                    c = 1;
                    z4 = true;
                } else if (scanPseudoAttribute.equals(fEncodingSymbol)) {
                    if (!z3) {
                        reportFatalError(z ? "SpaceRequiredBeforeEncodingInTextDecl" : "SpaceRequiredBeforeEncodingInXMLDecl", null);
                    }
                    xMLString = this.fString.toString();
                } else if (z || !scanPseudoAttribute.equals(fStandaloneSymbol)) {
                    reportFatalError("EncodingDeclRequired", null);
                    z3 = this.fEntityScanner.skipSpaces();
                    c = 1;
                    z4 = true;
                } else {
                    if (!z3) {
                        reportFatalError("SpaceRequiredBeforeStandalone", null);
                    }
                    xMLString2 = this.fString.toString();
                    if (!xMLString2.equals("yes") && !xMLString2.equals(IndexType.NO)) {
                        reportFatalError("SDDeclInvalid", new Object[]{xMLString2});
                    }
                }
                str3 = xMLString2;
                c2 = 3;
                z3 = this.fEntityScanner.skipSpaces();
                c = 1;
                z4 = true;
            } else {
                if (scanPseudoAttribute.equals(fVersionSymbol)) {
                    if (!z3) {
                        reportFatalError(z ? "SpaceRequiredBeforeVersionInTextDecl" : "SpaceRequiredBeforeVersionInXMLDecl", null);
                    }
                    String xMLString3 = this.fString.toString();
                    if (!versionSupported(xMLString3)) {
                        reportFatalError("VersionNotSupported", new Object[]{xMLString3});
                    }
                    if (xMLString3.equals(SerializerConstants.XMLVERSION11)) {
                        Entity.ScannedEntity topLevelEntity = this.fEntityManager.getTopLevelEntity();
                        if (topLevelEntity != null && (topLevelEntity.version == null || topLevelEntity.version.equals("1.0"))) {
                            reportFatalError("VersionMismatch", null);
                        }
                        this.fEntityManager.setScannerVersion(2);
                    }
                    str = xMLString3;
                    c2 = 1;
                } else if (scanPseudoAttribute.equals(fEncodingSymbol)) {
                    if (!z) {
                        reportFatalError("VersionInfoRequired", null);
                    }
                    if (!z3) {
                        reportFatalError(z ? "SpaceRequiredBeforeEncodingInTextDecl" : "SpaceRequiredBeforeEncodingInXMLDecl", null);
                    }
                    xMLString = this.fString.toString();
                } else if (z) {
                    reportFatalError("EncodingDeclRequired", null);
                } else {
                    reportFatalError("VersionInfoRequired", null);
                }
                z3 = this.fEntityScanner.skipSpaces();
                c = 1;
                z4 = true;
            }
            char c3 = 3;
            str2 = xMLString;
            c2 = c3;
            z3 = this.fEntityScanner.skipSpaces();
            c = 1;
            z4 = true;
        }
        if (z2) {
            currentEntity.literal = true;
        }
        if (!z || c2 == 3) {
            r3 = 0;
        } else {
            r3 = 0;
            reportFatalError("MorePseudoAttributes", null);
        }
        if (z) {
            if (!z4 && str2 == null) {
                reportFatalError("EncodingDeclRequired", r3);
            }
        } else if (!z4 && str == null) {
            reportFatalError("VersionInfoRequired", r3);
        }
        if (!this.fEntityScanner.skipChar(63, r3)) {
            reportFatalError("XMLDeclUnterminated", r3);
        }
        if (!this.fEntityScanner.skipChar(62, r3)) {
            reportFatalError("XMLDeclUnterminated", r3);
        }
        strArr[0] = str;
        strArr[1] = str2;
        strArr[2] = str3;
    }

    /* access modifiers changed from: protected */
    public String scanPseudoAttribute(boolean z, XMLString xMLString) throws IOException, XNIException {
        String scanPseudoAttributeName = scanPseudoAttributeName();
        if (scanPseudoAttributeName == null) {
            reportFatalError("PseudoAttrNameExpected", null);
        }
        this.fEntityScanner.skipSpaces();
        if (!this.fEntityScanner.skipChar(61, null)) {
            reportFatalError(z ? "EqRequiredInTextDecl" : "EqRequiredInXMLDecl", new Object[]{scanPseudoAttributeName});
        }
        this.fEntityScanner.skipSpaces();
        int peekChar = this.fEntityScanner.peekChar();
        if (!(peekChar == 39 || peekChar == 34)) {
            reportFatalError(z ? "QuoteRequiredInTextDecl" : "QuoteRequiredInXMLDecl", new Object[]{scanPseudoAttributeName});
        }
        this.fEntityScanner.scanChar(NameType.ATTRIBUTE);
        int scanLiteral = this.fEntityScanner.scanLiteral(peekChar, xMLString, false);
        if (scanLiteral != peekChar) {
            this.fStringBuffer2.clear();
            do {
                this.fStringBuffer2.append(xMLString);
                if (scanLiteral != -1) {
                    if (scanLiteral == 38 || scanLiteral == 37 || scanLiteral == 60 || scanLiteral == 93) {
                        this.fStringBuffer2.append((char) this.fEntityScanner.scanChar(NameType.ATTRIBUTE));
                    } else if (XMLChar.isHighSurrogate(scanLiteral)) {
                        scanSurrogates(this.fStringBuffer2);
                    } else if (isInvalidLiteral(scanLiteral)) {
                        reportFatalError(z ? "InvalidCharInTextDecl" : "InvalidCharInXMLDecl", new Object[]{Integer.toString(scanLiteral, 16)});
                        this.fEntityScanner.scanChar(null);
                    }
                }
                scanLiteral = this.fEntityScanner.scanLiteral(peekChar, xMLString, false);
            } while (scanLiteral != peekChar);
            this.fStringBuffer2.append(xMLString);
            xMLString.setValues(this.fStringBuffer2);
        }
        if (!this.fEntityScanner.skipChar(peekChar, null)) {
            reportFatalError(z ? "CloseQuoteMissingInTextDecl" : "CloseQuoteMissingInXMLDecl", new Object[]{scanPseudoAttributeName});
        }
        return scanPseudoAttributeName;
    }

    private String scanPseudoAttributeName() throws IOException, XNIException {
        int peekChar = this.fEntityScanner.peekChar();
        if (peekChar != 101) {
            if (peekChar != 115) {
                if (peekChar == 118 && this.fEntityScanner.skipString(fVersionSymbol)) {
                    return fVersionSymbol;
                }
                return null;
            } else if (this.fEntityScanner.skipString(fStandaloneSymbol)) {
                return fStandaloneSymbol;
            } else {
                return null;
            }
        } else if (this.fEntityScanner.skipString(fEncodingSymbol)) {
            return fEncodingSymbol;
        } else {
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void scanPI(XMLStringBuffer xMLStringBuffer) throws IOException, XNIException {
        this.fReportEntity = false;
        String scanName = this.fEntityScanner.scanName(NameType.PI);
        if (scanName == null) {
            reportFatalError("PITargetRequired", null);
        }
        scanPIData(scanName, xMLStringBuffer);
        this.fReportEntity = true;
    }

    /* access modifiers changed from: protected */
    public void scanPIData(String str, XMLStringBuffer xMLStringBuffer) throws IOException, XNIException {
        if (str.length() == 3) {
            char lowerCase = Character.toLowerCase(str.charAt(0));
            char lowerCase2 = Character.toLowerCase(str.charAt(1));
            char lowerCase3 = Character.toLowerCase(str.charAt(2));
            if (lowerCase == 'x' && lowerCase2 == 'm' && lowerCase3 == 'l') {
                reportFatalError("ReservedPITarget", null);
            }
        }
        if (!this.fEntityScanner.skipSpaces()) {
            if (!this.fEntityScanner.skipString("?>")) {
                reportFatalError("SpaceRequiredInPI", null);
            } else {
                return;
            }
        }
        if (this.fEntityScanner.scanData("?>", xMLStringBuffer)) {
            do {
                int peekChar = this.fEntityScanner.peekChar();
                if (peekChar != -1) {
                    if (XMLChar.isHighSurrogate(peekChar)) {
                        scanSurrogates(xMLStringBuffer);
                    } else if (isInvalidLiteral(peekChar)) {
                        reportFatalError("InvalidCharInPI", new Object[]{Integer.toHexString(peekChar)});
                        this.fEntityScanner.scanChar(null);
                    }
                }
            } while (this.fEntityScanner.scanData("?>", xMLStringBuffer));
        }
    }

    /* access modifiers changed from: protected */
    public void scanComment(XMLStringBuffer xMLStringBuffer) throws IOException, XNIException {
        xMLStringBuffer.clear();
        while (this.fEntityScanner.scanData("--", xMLStringBuffer)) {
            int peekChar = this.fEntityScanner.peekChar();
            if (peekChar != -1) {
                if (XMLChar.isHighSurrogate(peekChar)) {
                    scanSurrogates(xMLStringBuffer);
                } else if (isInvalidLiteral(peekChar)) {
                    reportFatalError("InvalidCharInComment", new Object[]{Integer.toHexString(peekChar)});
                    this.fEntityScanner.scanChar(NameType.COMMENT);
                }
            }
        }
        if (!this.fEntityScanner.skipChar(62, NameType.COMMENT)) {
            reportFatalError("DashDashInComment", null);
        }
    }

    /* access modifiers changed from: protected */
    public void scanAttributeValue(XMLString xMLString, XMLString xMLString2, String str, XMLAttributes xMLAttributes, int i, boolean z, String str2, boolean z2) throws IOException, XNIException {
        int peekChar = this.fEntityScanner.peekChar();
        if (!(peekChar == 39 || peekChar == 34)) {
            reportFatalError("OpenQuoteExpected", new Object[]{str2, str});
        }
        this.fEntityScanner.scanChar(NameType.ATTRIBUTE);
        int i2 = this.fEntityDepth;
        int scanLiteral = this.fEntityScanner.scanLiteral(peekChar, xMLString, z2);
        if (this.fNeedNonNormalizedValue) {
            this.fStringBuffer2.clear();
            this.fStringBuffer2.append(xMLString);
        }
        if (this.fEntityScanner.whiteSpaceLen > 0) {
            normalizeWhitespace(xMLString);
        }
        if (scanLiteral != peekChar) {
            this.fScanningAttribute = true;
            XMLStringBuffer stringBuffer = getStringBuffer();
            stringBuffer.clear();
            while (true) {
                stringBuffer.append(xMLString);
                if (scanLiteral == 38) {
                    this.fEntityScanner.skipChar(38, NameType.REFERENCE);
                    if (i2 == this.fEntityDepth && this.fNeedNonNormalizedValue) {
                        this.fStringBuffer2.append('&');
                    }
                    if (this.fEntityScanner.skipChar(35, NameType.REFERENCE)) {
                        if (i2 == this.fEntityDepth && this.fNeedNonNormalizedValue) {
                            this.fStringBuffer2.append('#');
                        }
                        if (this.fNeedNonNormalizedValue) {
                            scanCharReferenceValue(stringBuffer, this.fStringBuffer2);
                        } else {
                            scanCharReferenceValue(stringBuffer, null);
                        }
                    } else {
                        String scanName = this.fEntityScanner.scanName(NameType.ENTITY);
                        if (scanName == null) {
                            reportFatalError("NameRequiredInReference", null);
                        } else if (i2 == this.fEntityDepth && this.fNeedNonNormalizedValue) {
                            this.fStringBuffer2.append(scanName);
                        }
                        if (!this.fEntityScanner.skipChar(59, NameType.REFERENCE)) {
                            reportFatalError("SemicolonRequiredInReference", new Object[]{scanName});
                        } else if (i2 == this.fEntityDepth && this.fNeedNonNormalizedValue) {
                            this.fStringBuffer2.append(';');
                        }
                        if (resolveCharacter(scanName, stringBuffer)) {
                            checkEntityLimit(false, this.fEntityScanner.fCurrentEntity.name, 1);
                        } else if (this.fEntityStore.isExternalEntity(scanName)) {
                            reportFatalError("ReferenceToExternalEntity", new Object[]{scanName});
                        } else {
                            if (!this.fEntityStore.isDeclaredEntity(scanName)) {
                                if (!z) {
                                    reportFatalError("EntityNotDeclared", new Object[]{scanName});
                                } else if (this.fValidation) {
                                    this.fErrorReporter.reportError((XMLLocator) this.fEntityScanner, "http://www.w3.org/TR/1998/REC-xml-19980210", "EntityNotDeclared", new Object[]{scanName}, (short) 1);
                                }
                            }
                            this.fEntityManager.startEntity(true, scanName, true);
                        }
                    }
                } else if (scanLiteral == 60) {
                    reportFatalError("LessthanInAttValue", new Object[]{str2, str});
                    this.fEntityScanner.scanChar(null);
                    if (i2 == this.fEntityDepth && this.fNeedNonNormalizedValue) {
                        this.fStringBuffer2.append((char) scanLiteral);
                    }
                } else if (scanLiteral == 37 || scanLiteral == 93) {
                    this.fEntityScanner.scanChar(null);
                    char c = (char) scanLiteral;
                    stringBuffer.append(c);
                    if (i2 == this.fEntityDepth && this.fNeedNonNormalizedValue) {
                        this.fStringBuffer2.append(c);
                    }
                } else if (scanLiteral == 10 || scanLiteral == 13) {
                    this.fEntityScanner.scanChar(null);
                    stringBuffer.append(' ');
                    if (i2 == this.fEntityDepth && this.fNeedNonNormalizedValue) {
                        this.fStringBuffer2.append('\n');
                    }
                } else if (scanLiteral != -1 && XMLChar.isHighSurrogate(scanLiteral)) {
                    this.fStringBuffer3.clear();
                    if (scanSurrogates(this.fStringBuffer3)) {
                        stringBuffer.append(this.fStringBuffer3);
                        if (i2 == this.fEntityDepth && this.fNeedNonNormalizedValue) {
                            this.fStringBuffer2.append(this.fStringBuffer3);
                        }
                    }
                } else if (scanLiteral != -1 && isInvalidLiteral(scanLiteral)) {
                    reportFatalError("InvalidCharInAttValue", new Object[]{str2, str, Integer.toString(scanLiteral, 16)});
                    this.fEntityScanner.scanChar(null);
                    if (i2 == this.fEntityDepth && this.fNeedNonNormalizedValue) {
                        this.fStringBuffer2.append((char) scanLiteral);
                    }
                }
                scanLiteral = this.fEntityScanner.scanLiteral(peekChar, xMLString, z2);
                if (i2 == this.fEntityDepth && this.fNeedNonNormalizedValue) {
                    this.fStringBuffer2.append(xMLString);
                }
                if (this.fEntityScanner.whiteSpaceLen > 0) {
                    normalizeWhitespace(xMLString);
                }
                if (scanLiteral == peekChar && i2 == this.fEntityDepth) {
                    break;
                }
            }
            stringBuffer.append(xMLString);
            xMLString.setValues(stringBuffer);
            this.fScanningAttribute = false;
        }
        if (this.fNeedNonNormalizedValue) {
            xMLString2.setValues(this.fStringBuffer2);
        }
        if (this.fEntityScanner.scanChar(NameType.ATTRIBUTE) != peekChar) {
            reportFatalError("CloseQuoteExpected", new Object[]{str2, str});
        }
    }

    /* access modifiers changed from: protected */
    public boolean resolveCharacter(String str, XMLStringBuffer xMLStringBuffer) {
        if (str == fAmpSymbol) {
            xMLStringBuffer.append('&');
            return true;
        } else if (str == fAposSymbol) {
            xMLStringBuffer.append(PatternTokenizer.SINGLE_QUOTE);
            return true;
        } else if (str == fLtSymbol) {
            xMLStringBuffer.append('<');
            return true;
        } else if (str == fGtSymbol) {
            checkEntityLimit(false, this.fEntityScanner.fCurrentEntity.name, 1);
            xMLStringBuffer.append('>');
            return true;
        } else if (str != fQuotSymbol) {
            return false;
        } else {
            checkEntityLimit(false, this.fEntityScanner.fCurrentEntity.name, 1);
            xMLStringBuffer.append('\"');
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void scanExternalID(String[] strArr, boolean z) throws IOException, XNIException {
        String str;
        String str2;
        if (this.fEntityScanner.skipString("PUBLIC")) {
            if (!this.fEntityScanner.skipSpaces()) {
                reportFatalError("SpaceRequiredAfterPUBLIC", null);
            }
            scanPubidLiteral(this.fString);
            str = this.fString.toString();
            if (!this.fEntityScanner.skipSpaces() && !z) {
                reportFatalError("SpaceRequiredBetweenPublicAndSystem", null);
            }
        } else {
            str = null;
        }
        if (str != null || this.fEntityScanner.skipString("SYSTEM")) {
            if (str == null && !this.fEntityScanner.skipSpaces()) {
                reportFatalError("SpaceRequiredAfterSYSTEM", null);
            }
            int peekChar = this.fEntityScanner.peekChar();
            if (!(peekChar == 39 || peekChar == 34)) {
                if (str == null || !z) {
                    reportFatalError("QuoteRequiredInSystemID", null);
                } else {
                    strArr[0] = null;
                    strArr[1] = str;
                    return;
                }
            }
            this.fEntityScanner.scanChar(null);
            XMLString xMLString = this.fString;
            if (this.fEntityScanner.scanLiteral(peekChar, xMLString, false) != peekChar) {
                this.fStringBuffer.clear();
                do {
                    this.fStringBuffer.append(xMLString);
                    int peekChar2 = this.fEntityScanner.peekChar();
                    if (XMLChar.isMarkup(peekChar2) || peekChar2 == 93) {
                        this.fStringBuffer.append((char) this.fEntityScanner.scanChar(null));
                    } else if (peekChar2 != -1 && isInvalidLiteral(peekChar2)) {
                        reportFatalError("InvalidCharInSystemID", new Object[]{Integer.toString(peekChar2, 16)});
                    }
                } while (this.fEntityScanner.scanLiteral(peekChar, xMLString, false) != peekChar);
                this.fStringBuffer.append(xMLString);
                xMLString = this.fStringBuffer;
            }
            str2 = xMLString.toString();
            if (!this.fEntityScanner.skipChar(peekChar, null)) {
                reportFatalError("SystemIDUnterminated", null);
            }
        } else {
            str2 = null;
        }
        strArr[0] = str2;
        strArr[1] = str;
    }

    /* access modifiers changed from: protected */
    public boolean scanPubidLiteral(XMLString xMLString) throws IOException, XNIException {
        int scanChar = this.fEntityScanner.scanChar(null);
        if (scanChar == 39 || scanChar == 34) {
            this.fStringBuffer.clear();
            boolean z = true;
            boolean z2 = true;
            while (true) {
                int scanChar2 = this.fEntityScanner.scanChar(null);
                if (scanChar2 == 32 || scanChar2 == 10 || scanChar2 == 13) {
                    if (!z) {
                        this.fStringBuffer.append(' ');
                        z = true;
                    }
                } else if (scanChar2 == scanChar) {
                    if (z) {
                        this.fStringBuffer.length--;
                    }
                    xMLString.setValues(this.fStringBuffer);
                    return z2;
                } else if (XMLChar.isPubid(scanChar2)) {
                    this.fStringBuffer.append((char) scanChar2);
                    z = false;
                } else if (scanChar2 == -1) {
                    reportFatalError("PublicIDUnterminated", null);
                    return false;
                } else {
                    reportFatalError("InvalidCharInPublicID", new Object[]{Integer.toHexString(scanChar2)});
                    z2 = false;
                }
            }
        } else {
            reportFatalError("QuoteRequiredInPublicID", null);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void normalizeWhitespace(XMLString xMLString) {
        int[] iArr = this.fEntityScanner.whiteSpaceLookup;
        int i = this.fEntityScanner.whiteSpaceLen;
        int i2 = xMLString.offset + xMLString.length;
        for (int i3 = 0; i3 < i; i3++) {
            int i4 = iArr[i3];
            if (i4 < i2) {
                xMLString.ch[i4] = ' ';
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityHandler
    public void startEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
        this.fEntityDepth++;
        this.fEntityScanner = this.fEntityManager.getEntityScanner();
        this.fEntityStore = this.fEntityManager.getEntityStore();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityHandler
    public void endEntity(String str, Augmentations augmentations) throws IOException, XNIException {
        this.fEntityDepth--;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x014b  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x0171  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0176  */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x0196  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x019a  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x01b0  */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x01ba  */
    public int scanCharReferenceValue(XMLStringBuffer xMLStringBuffer, XMLStringBuffer xMLStringBuffer2) throws IOException, XNIException {
        boolean z;
        int i;
        boolean z2;
        int i2 = xMLStringBuffer.length;
        if (this.fEntityScanner.skipChar(120, NameType.REFERENCE)) {
            if (xMLStringBuffer2 != null) {
                xMLStringBuffer2.append('x');
            }
            this.fStringBuffer3.clear();
            int peekChar = this.fEntityScanner.peekChar();
            int i3 = 70;
            if ((peekChar >= 48 && peekChar <= 57) || (peekChar >= 97 && peekChar <= 102) || (peekChar >= 65 && peekChar <= 70)) {
                if (xMLStringBuffer2 != null) {
                    xMLStringBuffer2.append((char) peekChar);
                }
                this.fEntityScanner.scanChar(NameType.REFERENCE);
                this.fStringBuffer3.append((char) peekChar);
                while (true) {
                    int peekChar2 = this.fEntityScanner.peekChar();
                    boolean z3 = (peekChar2 >= 48 && peekChar2 <= 57) || (peekChar2 >= 97 && peekChar2 <= 102) || (peekChar2 >= 65 && peekChar2 <= i3);
                    if (z3) {
                        if (xMLStringBuffer2 != null) {
                            xMLStringBuffer2.append((char) peekChar2);
                        }
                        this.fEntityScanner.scanChar(NameType.REFERENCE);
                        this.fStringBuffer3.append((char) peekChar2);
                    }
                    if (!z3) {
                        break;
                    }
                    i3 = 70;
                }
            } else {
                reportFatalError("HexdigitRequiredInCharRef", null);
            }
            z = true;
        } else {
            this.fStringBuffer3.clear();
            int peekChar3 = this.fEntityScanner.peekChar();
            if (peekChar3 >= 48 && peekChar3 <= 57) {
                if (xMLStringBuffer2 != null) {
                    xMLStringBuffer2.append((char) peekChar3);
                }
                this.fEntityScanner.scanChar(NameType.REFERENCE);
                this.fStringBuffer3.append((char) peekChar3);
                do {
                    int peekChar4 = this.fEntityScanner.peekChar();
                    z2 = peekChar4 >= 48 && peekChar4 <= 57;
                    if (z2) {
                        if (xMLStringBuffer2 != null) {
                            xMLStringBuffer2.append((char) peekChar4);
                        }
                        this.fEntityScanner.scanChar(NameType.REFERENCE);
                        this.fStringBuffer3.append((char) peekChar4);
                        continue;
                    }
                } while (z2);
            } else {
                reportFatalError("DigitRequiredInCharRef", null);
            }
            z = false;
        }
        if (!this.fEntityScanner.skipChar(59, NameType.REFERENCE)) {
            reportFatalError("SemicolonRequiredInCharRef", null);
        }
        if (xMLStringBuffer2 != null) {
            xMLStringBuffer2.append(';');
        }
        try {
            i = Integer.parseInt(this.fStringBuffer3.toString(), z ? 16 : 10);
            try {
                if (isInvalid(i)) {
                    StringBuffer stringBuffer = new StringBuffer(this.fStringBuffer3.length + 1);
                    if (z) {
                        stringBuffer.append('x');
                    }
                    stringBuffer.append(this.fStringBuffer3.ch, this.fStringBuffer3.offset, this.fStringBuffer3.length);
                    reportFatalError("InvalidCharRef", new Object[]{stringBuffer.toString()});
                }
            } catch (NumberFormatException unused) {
                StringBuffer stringBuffer2 = new StringBuffer(this.fStringBuffer3.length + 1);
                if (z) {
                    stringBuffer2.append('x');
                }
                stringBuffer2.append(this.fStringBuffer3.ch, this.fStringBuffer3.offset, this.fStringBuffer3.length);
                reportFatalError("InvalidCharRef", new Object[]{stringBuffer2.toString()});
                if (!XMLChar.isSupplemental(i)) {
                }
                StringBuilder sb = new StringBuilder();
                sb.append("#");
                sb.append(z ? LanguageTag.PRIVATEUSE : "");
                sb.append(this.fStringBuffer3.toString());
                String sb2 = sb.toString();
                if (!this.fScanningAttribute) {
                }
                if (this.fEntityScanner.fCurrentEntity.isGE) {
                }
                return i;
            }
        } catch (NumberFormatException unused2) {
            i = -1;
            StringBuffer stringBuffer22 = new StringBuffer(this.fStringBuffer3.length + 1);
            if (z) {
            }
            stringBuffer22.append(this.fStringBuffer3.ch, this.fStringBuffer3.offset, this.fStringBuffer3.length);
            reportFatalError("InvalidCharRef", new Object[]{stringBuffer22.toString()});
            if (!XMLChar.isSupplemental(i)) {
            }
            StringBuilder sb3 = new StringBuilder();
            sb3.append("#");
            sb3.append(z ? LanguageTag.PRIVATEUSE : "");
            sb3.append(this.fStringBuffer3.toString());
            String sb22 = sb3.toString();
            if (!this.fScanningAttribute) {
            }
            if (this.fEntityScanner.fCurrentEntity.isGE) {
            }
            return i;
        }
        if (!XMLChar.isSupplemental(i)) {
            xMLStringBuffer.append((char) i);
        } else {
            xMLStringBuffer.append(XMLChar.highSurrogate(i));
            xMLStringBuffer.append(XMLChar.lowSurrogate(i));
        }
        if (this.fNotifyCharRefs && i != -1) {
            StringBuilder sb32 = new StringBuilder();
            sb32.append("#");
            sb32.append(z ? LanguageTag.PRIVATEUSE : "");
            sb32.append(this.fStringBuffer3.toString());
            String sb222 = sb32.toString();
            if (!this.fScanningAttribute) {
                this.fCharRefLiteral = sb222;
            }
        }
        if (this.fEntityScanner.fCurrentEntity.isGE) {
            checkEntityLimit(false, this.fEntityScanner.fCurrentEntity.name, xMLStringBuffer.length - i2);
        }
        return i;
    }

    /* access modifiers changed from: protected */
    public boolean isInvalid(int i) {
        return XMLChar.isInvalid(i);
    }

    /* access modifiers changed from: protected */
    public boolean isInvalidLiteral(int i) {
        return XMLChar.isInvalid(i);
    }

    /* access modifiers changed from: protected */
    public boolean isValidNameChar(int i) {
        return XMLChar.isName(i);
    }

    /* access modifiers changed from: protected */
    public boolean isValidNCName(int i) {
        return XMLChar.isNCName(i);
    }

    /* access modifiers changed from: protected */
    public boolean isValidNameStartChar(int i) {
        return XMLChar.isNameStart(i);
    }

    /* access modifiers changed from: protected */
    public boolean versionSupported(String str) {
        return str.equals("1.0") || str.equals(SerializerConstants.XMLVERSION11);
    }

    /* access modifiers changed from: protected */
    public boolean scanSurrogates(XMLStringBuffer xMLStringBuffer) throws IOException, XNIException {
        int scanChar = this.fEntityScanner.scanChar(null);
        int peekChar = this.fEntityScanner.peekChar();
        if (!XMLChar.isLowSurrogate(peekChar)) {
            reportFatalError("InvalidCharInContent", new Object[]{Integer.toString(scanChar, 16)});
            return false;
        }
        this.fEntityScanner.scanChar(null);
        char c = (char) scanChar;
        char c2 = (char) peekChar;
        int supplemental = XMLChar.supplemental(c, c2);
        if (isInvalid(supplemental)) {
            reportFatalError("InvalidCharInContent", new Object[]{Integer.toString(supplemental, 16)});
            return false;
        }
        xMLStringBuffer.append(c);
        xMLStringBuffer.append(c2);
        return true;
    }

    /* access modifiers changed from: protected */
    public void reportFatalError(String str, Object[] objArr) throws XNIException {
        this.fErrorReporter.reportError((XMLLocator) this.fEntityScanner, "http://www.w3.org/TR/1998/REC-xml-19980210", str, objArr, (short) 2);
    }

    private void init() {
        this.fEntityScanner = null;
        this.fEntityDepth = 0;
        this.fReportEntity = true;
        this.fResourceIdentifier.clear();
        if (!this.fAttributeCacheInitDone) {
            for (int i = 0; i < this.initialCacheCount; i++) {
                this.attributeValueCache.add(new XMLString());
                this.stringBufferCache.add(new XMLStringBuffer());
            }
            this.fAttributeCacheInitDone = true;
        }
        this.fStringBufferIndex = 0;
        this.fAttributeCacheUsedCount = 0;
    }

    /* access modifiers changed from: package-private */
    public XMLStringBuffer getStringBuffer() {
        int i = this.fStringBufferIndex;
        if (i < this.initialCacheCount || i < this.stringBufferCache.size()) {
            ArrayList<XMLStringBuffer> arrayList = this.stringBufferCache;
            int i2 = this.fStringBufferIndex;
            this.fStringBufferIndex = i2 + 1;
            return arrayList.get(i2);
        }
        XMLStringBuffer xMLStringBuffer = new XMLStringBuffer();
        this.fStringBufferIndex++;
        this.stringBufferCache.add(xMLStringBuffer);
        return xMLStringBuffer;
    }

    /* access modifiers changed from: package-private */
    public void checkEntityLimit(boolean z, String str, XMLString xMLString) {
        checkEntityLimit(z, str, xMLString.length);
    }

    /* access modifiers changed from: package-private */
    public void checkEntityLimit(boolean z, String str, int i) {
        if (this.fLimitAnalyzer == null) {
            this.fLimitAnalyzer = this.fEntityManager.fLimitAnalyzer;
        }
        if (z) {
            XMLLimitAnalyzer xMLLimitAnalyzer = this.fLimitAnalyzer;
            XMLSecurityManager.Limit limit = XMLSecurityManager.Limit.PARAMETER_ENTITY_SIZE_LIMIT;
            xMLLimitAnalyzer.addValue(limit, "%" + str, i);
            if (this.fSecurityManager.isOverLimit(XMLSecurityManager.Limit.PARAMETER_ENTITY_SIZE_LIMIT, this.fLimitAnalyzer)) {
                this.fSecurityManager.debugPrint(this.fLimitAnalyzer);
                reportFatalError("MaxEntitySizeLimit", new Object[]{"%" + str, Integer.valueOf(this.fLimitAnalyzer.getValue(XMLSecurityManager.Limit.PARAMETER_ENTITY_SIZE_LIMIT)), Integer.valueOf(this.fSecurityManager.getLimit(XMLSecurityManager.Limit.PARAMETER_ENTITY_SIZE_LIMIT)), this.fSecurityManager.getStateLiteral(XMLSecurityManager.Limit.PARAMETER_ENTITY_SIZE_LIMIT)});
            }
        } else {
            this.fLimitAnalyzer.addValue(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT, str, i);
            if (this.fSecurityManager.isOverLimit(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT, this.fLimitAnalyzer)) {
                this.fSecurityManager.debugPrint(this.fLimitAnalyzer);
                reportFatalError("MaxEntitySizeLimit", new Object[]{str, Integer.valueOf(this.fLimitAnalyzer.getValue(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT)), Integer.valueOf(this.fSecurityManager.getLimit(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT)), this.fSecurityManager.getStateLiteral(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT)});
            }
        }
        if (this.fSecurityManager.isOverLimit(XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT, this.fLimitAnalyzer)) {
            this.fSecurityManager.debugPrint(this.fLimitAnalyzer);
            reportFatalError("TotalEntitySizeLimit", new Object[]{Integer.valueOf(this.fLimitAnalyzer.getTotalValue(XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT)), Integer.valueOf(this.fSecurityManager.getLimit(XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT)), this.fSecurityManager.getStateLiteral(XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT)});
        }
    }
}
