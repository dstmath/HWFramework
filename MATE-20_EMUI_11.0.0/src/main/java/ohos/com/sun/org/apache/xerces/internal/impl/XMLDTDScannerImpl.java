package ohos.com.sun.org.apache.xerces.internal.impl;

import java.io.EOFException;
import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLAttributesImpl;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.util.XMLStringBuffer;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLLimitAnalyzer;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDScanner;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.xml.internal.stream.dtd.nonvalidating.DTDGrammar;

public class XMLDTDScannerImpl extends XMLScanner implements XMLDTDScanner, XMLComponent, XMLEntityHandler {
    private static final boolean DEBUG_SCANNER_STATE = false;
    private static final Boolean[] FEATURE_DEFAULTS = {null, Boolean.FALSE};
    private static final Object[] PROPERTY_DEFAULTS = {null, null, null};
    private static final String[] RECOGNIZED_FEATURES = {"http://xml.org/sax/features/validation", "http://apache.org/xml/features/scanner/notify-char-refs"};
    private static final String[] RECOGNIZED_PROPERTIES = {"http://apache.org/xml/properties/internal/symbol-table", "http://apache.org/xml/properties/internal/error-reporter", "http://apache.org/xml/properties/internal/entity-manager"};
    protected static final int SCANNER_STATE_END_OF_INPUT = 0;
    protected static final int SCANNER_STATE_MARKUP_DECL = 2;
    protected static final int SCANNER_STATE_TEXT_DECL = 1;
    private XMLAttributesImpl fAttributes;
    private int fContentDepth;
    private int[] fContentStack;
    protected XMLDTDContentModelHandler fDTDContentModelHandler;
    public XMLDTDHandler fDTDHandler;
    private String[] fEnumeration;
    private int fEnumerationCount;
    private int fExtEntityDepth;
    private XMLStringBuffer fIgnoreConditionalBuffer;
    private int fIncludeSectDepth;
    private XMLString fLiteral;
    private XMLString fLiteral2;
    private int fMarkUpDepth;
    private int fPEDepth;
    private boolean[] fPEReport;
    private int[] fPEStack;
    protected int fScannerState;
    protected boolean fSeenExternalDTD;
    protected boolean fSeenExternalPE;
    protected boolean fStandalone;
    private boolean fStartDTDCalled;
    private XMLString fString;
    private XMLStringBuffer fStringBuffer;
    private XMLStringBuffer fStringBuffer2;
    private String[] fStrings;
    boolean nonValidatingMode;
    DTDGrammar nvGrammarInfo;

    public XMLDTDScannerImpl() {
        this.fDTDHandler = null;
        this.fAttributes = new XMLAttributesImpl();
        this.fContentStack = new int[5];
        this.fPEStack = new int[5];
        this.fPEReport = new boolean[5];
        this.fStrings = new String[3];
        this.fString = new XMLString();
        this.fStringBuffer = new XMLStringBuffer();
        this.fStringBuffer2 = new XMLStringBuffer();
        this.fLiteral = new XMLString();
        this.fLiteral2 = new XMLString();
        this.fEnumeration = new String[5];
        this.fIgnoreConditionalBuffer = new XMLStringBuffer(128);
        this.nvGrammarInfo = null;
        this.nonValidatingMode = false;
    }

    public XMLDTDScannerImpl(SymbolTable symbolTable, XMLErrorReporter xMLErrorReporter, XMLEntityManager xMLEntityManager) {
        this.fDTDHandler = null;
        this.fAttributes = new XMLAttributesImpl();
        this.fContentStack = new int[5];
        this.fPEStack = new int[5];
        this.fPEReport = new boolean[5];
        this.fStrings = new String[3];
        this.fString = new XMLString();
        this.fStringBuffer = new XMLStringBuffer();
        this.fStringBuffer2 = new XMLStringBuffer();
        this.fLiteral = new XMLString();
        this.fLiteral2 = new XMLString();
        this.fEnumeration = new String[5];
        this.fIgnoreConditionalBuffer = new XMLStringBuffer(128);
        this.nvGrammarInfo = null;
        this.nonValidatingMode = false;
        this.fSymbolTable = symbolTable;
        this.fErrorReporter = xMLErrorReporter;
        this.fEntityManager = xMLEntityManager;
        xMLEntityManager.setProperty("http://apache.org/xml/properties/internal/symbol-table", this.fSymbolTable);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDScanner
    public void setInputSource(XMLInputSource xMLInputSource) throws IOException {
        if (xMLInputSource == null) {
            XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
            if (xMLDTDHandler != null) {
                xMLDTDHandler.startDTD(null, null);
                this.fDTDHandler.endDTD(null);
            }
            if (this.nonValidatingMode) {
                this.nvGrammarInfo.startDTD(null, null);
                this.nvGrammarInfo.endDTD(null);
                return;
            }
            return;
        }
        this.fEntityManager.setEntityHandler(this);
        this.fEntityManager.startDTDEntity(xMLInputSource);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDScanner
    public void setLimitAnalyzer(XMLLimitAnalyzer xMLLimitAnalyzer) {
        this.fLimitAnalyzer = xMLLimitAnalyzer;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDScanner
    public boolean scanDTDExternalSubset(boolean z) throws IOException, XNIException {
        this.fEntityManager.setEntityHandler(this);
        if (this.fScannerState == 1) {
            this.fSeenExternalDTD = true;
            boolean scanTextDecl = scanTextDecl();
            if (this.fScannerState == 0) {
                return false;
            }
            setScannerState(2);
            if (scanTextDecl && !z) {
                return true;
            }
        }
        while (scanDecls(z)) {
            if (!z) {
                return true;
            }
        }
        return false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDScanner
    public boolean scanDTDInternalSubset(boolean z, boolean z2, boolean z3) throws IOException, XNIException {
        this.fEntityScanner = this.fEntityManager.getEntityScanner();
        this.fEntityManager.setEntityHandler(this);
        this.fStandalone = z2;
        if (this.fScannerState == 1) {
            XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
            if (xMLDTDHandler != null) {
                xMLDTDHandler.startDTD(this.fEntityScanner, null);
                this.fStartDTDCalled = true;
            }
            if (this.nonValidatingMode) {
                this.fStartDTDCalled = true;
                this.nvGrammarInfo.startDTD(this.fEntityScanner, null);
            }
            setScannerState(2);
        }
        while (scanDecls(z)) {
            if (!z) {
                return true;
            }
        }
        XMLDTDHandler xMLDTDHandler2 = this.fDTDHandler;
        if (xMLDTDHandler2 != null && !z3) {
            xMLDTDHandler2.endDTD(null);
        }
        if (this.nonValidatingMode && !z3) {
            this.nvGrammarInfo.endDTD(null);
        }
        setScannerState(1);
        this.fLimitAnalyzer.reset(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT);
        this.fLimitAnalyzer.reset(XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT);
        return false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDScanner
    public boolean skipDTD(boolean z) throws IOException {
        if (z) {
            return false;
        }
        this.fStringBuffer.clear();
        while (this.fEntityScanner.scanData("]", this.fStringBuffer)) {
            int peekChar = this.fEntityScanner.peekChar();
            if (peekChar != -1) {
                if (XMLChar.isHighSurrogate(peekChar)) {
                    scanSurrogates(this.fStringBuffer);
                }
                if (isInvalidLiteral(peekChar)) {
                    reportFatalError("InvalidCharInDTD", new Object[]{Integer.toHexString(peekChar)});
                    this.fEntityScanner.scanChar(null);
                }
            }
        }
        this.fEntityScanner.fCurrentEntity.position--;
        return true;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void reset(XMLComponentManager xMLComponentManager) throws XMLConfigurationException {
        super.reset(xMLComponentManager);
        init();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner
    public void reset() {
        super.reset();
        init();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner
    public void reset(PropertyManager propertyManager) {
        setPropertyManager(propertyManager);
        super.reset(propertyManager);
        init();
        this.nonValidatingMode = true;
        this.nvGrammarInfo = new DTDGrammar(this.fSymbolTable);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public String[] getRecognizedFeatures() {
        return (String[]) RECOGNIZED_FEATURES.clone();
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

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource
    public void setDTDHandler(XMLDTDHandler xMLDTDHandler) {
        this.fDTDHandler = xMLDTDHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource
    public XMLDTDHandler getDTDHandler() {
        return this.fDTDHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDContentModelSource
    public void setDTDContentModelHandler(XMLDTDContentModelHandler xMLDTDContentModelHandler) {
        this.fDTDContentModelHandler = xMLDTDContentModelHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDContentModelSource
    public XMLDTDContentModelHandler getDTDContentModelHandler() {
        return this.fDTDContentModelHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner, ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityHandler
    public void startEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
        super.startEntity(str, xMLResourceIdentifier, str2, augmentations);
        boolean equals = str.equals("[dtd]");
        if (equals) {
            XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
            if (xMLDTDHandler != null && !this.fStartDTDCalled) {
                xMLDTDHandler.startDTD(this.fEntityScanner, null);
            }
            XMLDTDHandler xMLDTDHandler2 = this.fDTDHandler;
            if (xMLDTDHandler2 != null) {
                xMLDTDHandler2.startExternalSubset(xMLResourceIdentifier, null);
            }
            this.fEntityManager.startExternalSubset();
            this.fEntityStore.startExternalSubset();
            this.fExtEntityDepth++;
        } else if (str.charAt(0) == '%') {
            pushPEStack(this.fMarkUpDepth, this.fReportEntity);
            if (this.fEntityScanner.isExternal()) {
                this.fExtEntityDepth++;
            }
        }
        if (this.fDTDHandler != null && !equals && this.fReportEntity) {
            this.fDTDHandler.startParameterEntity(str, xMLResourceIdentifier, str2, null);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner, ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityHandler
    public void endEntity(String str, Augmentations augmentations) throws XNIException, IOException {
        super.endEntity(str, augmentations);
        if (this.fScannerState != 0) {
            boolean z = this.fReportEntity;
            if (str.startsWith("%")) {
                z = peekReportEntity();
                int popPEStack = popPEStack();
                if (popPEStack == 0 && popPEStack < this.fMarkUpDepth) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "ILL_FORMED_PARAMETER_ENTITY_WHEN_USED_IN_DECL", new Object[]{this.fEntityManager.fCurrentEntity.name}, 2);
                }
                if (popPEStack != this.fMarkUpDepth) {
                    if (this.fValidation) {
                        this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "ImproperDeclarationNesting", new Object[]{str}, 1);
                    }
                    z = false;
                }
                if (this.fEntityScanner.isExternal()) {
                    this.fExtEntityDepth--;
                }
            }
            boolean equals = str.equals("[dtd]");
            XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
            if (xMLDTDHandler != null && !equals && z) {
                xMLDTDHandler.endParameterEntity(str, null);
            }
            if (equals) {
                if (this.fIncludeSectDepth != 0) {
                    reportFatalError("IncludeSectUnterminated", null);
                }
                this.fScannerState = 0;
                this.fEntityManager.endExternalSubset();
                this.fEntityStore.endExternalSubset();
                XMLDTDHandler xMLDTDHandler2 = this.fDTDHandler;
                if (xMLDTDHandler2 != null) {
                    xMLDTDHandler2.endExternalSubset(null);
                    this.fDTDHandler.endDTD(null);
                }
                this.fExtEntityDepth--;
            }
            if (augmentations != null && Boolean.TRUE.equals(augmentations.getItem(Constants.LAST_ENTITY))) {
                if (this.fMarkUpDepth != 0 || this.fExtEntityDepth != 0 || this.fIncludeSectDepth != 0) {
                    throw new EOFException();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public final void setScannerState(int i) {
        this.fScannerState = i;
    }

    private static String getScannerStateName(int i) {
        return "??? (" + i + ')';
    }

    /* access modifiers changed from: protected */
    public final boolean scanningInternalSubset() {
        return this.fExtEntityDepth == 0;
    }

    /* access modifiers changed from: protected */
    public void startPE(String str, boolean z) throws IOException, XNIException {
        int i = this.fPEDepth;
        String str2 = "%" + str;
        if (this.fValidation && !this.fEntityStore.isDeclaredEntity(str2)) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "EntityNotDeclared", new Object[]{str}, 1);
        }
        this.fEntityManager.startEntity(false, this.fSymbolTable.addSymbol(str2), z);
        if (i != this.fPEDepth && this.fEntityScanner.isExternal()) {
            scanTextDecl();
        }
    }

    /* access modifiers changed from: protected */
    public final boolean scanTextDecl() throws IOException, XNIException {
        boolean z = false;
        if (this.fEntityScanner.skipString("<?xml")) {
            this.fMarkUpDepth++;
            if (isValidNameChar(this.fEntityScanner.peekChar())) {
                this.fStringBuffer.clear();
                this.fStringBuffer.append("xml");
                while (isValidNameChar(this.fEntityScanner.peekChar())) {
                    this.fStringBuffer.append((char) this.fEntityScanner.scanChar(null));
                }
                scanPIData(this.fSymbolTable.addSymbol(this.fStringBuffer.ch, this.fStringBuffer.offset, this.fStringBuffer.length), this.fString);
            } else {
                scanXMLDeclOrTextDecl(true, this.fStrings);
                this.fMarkUpDepth--;
                String[] strArr = this.fStrings;
                String str = strArr[0];
                String str2 = strArr[1];
                this.fEntityScanner.setEncoding(str2);
                XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
                if (xMLDTDHandler != null) {
                    xMLDTDHandler.textDecl(str, str2, null);
                }
                z = true;
            }
        }
        this.fEntityManager.fCurrentEntity.mayReadChunks = true;
        return z;
    }

    /* access modifiers changed from: protected */
    public final void scanPIData(String str, XMLString xMLString) throws IOException, XNIException {
        this.fMarkUpDepth--;
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.processingInstruction(str, xMLString, null);
        }
    }

    /* access modifiers changed from: protected */
    public final void scanComment() throws IOException, XNIException {
        this.fReportEntity = false;
        scanComment(this.fStringBuffer);
        this.fMarkUpDepth--;
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.comment(this.fStringBuffer, null);
        }
        this.fReportEntity = true;
    }

    /* access modifiers changed from: protected */
    public final void scanElementDecl() throws IOException, XNIException {
        this.fReportEntity = false;
        if (!skipSeparator(true, !scanningInternalSubset())) {
            reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ELEMENT_TYPE_IN_ELEMENTDECL", null);
        }
        String scanName = this.fEntityScanner.scanName(XMLScanner.NameType.ELEMENTSTART);
        if (scanName == null) {
            reportFatalError("MSG_ELEMENT_TYPE_REQUIRED_IN_ELEMENTDECL", null);
        }
        if (!skipSeparator(true, !scanningInternalSubset())) {
            reportFatalError("MSG_SPACE_REQUIRED_BEFORE_CONTENTSPEC_IN_ELEMENTDECL", new Object[]{scanName});
        }
        XMLDTDContentModelHandler xMLDTDContentModelHandler = this.fDTDContentModelHandler;
        if (xMLDTDContentModelHandler != null) {
            xMLDTDContentModelHandler.startContentModel(scanName, null);
        }
        this.fReportEntity = true;
        String str = "EMPTY";
        if (this.fEntityScanner.skipString(str)) {
            XMLDTDContentModelHandler xMLDTDContentModelHandler2 = this.fDTDContentModelHandler;
            if (xMLDTDContentModelHandler2 != null) {
                xMLDTDContentModelHandler2.empty(null);
            }
        } else if (this.fEntityScanner.skipString("ANY")) {
            XMLDTDContentModelHandler xMLDTDContentModelHandler3 = this.fDTDContentModelHandler;
            if (xMLDTDContentModelHandler3 != null) {
                xMLDTDContentModelHandler3.any(null);
            }
            str = "ANY";
        } else {
            if (!this.fEntityScanner.skipChar(40, null)) {
                reportFatalError("MSG_OPEN_PAREN_OR_ELEMENT_TYPE_REQUIRED_IN_CHILDREN", new Object[]{scanName});
            }
            XMLDTDContentModelHandler xMLDTDContentModelHandler4 = this.fDTDContentModelHandler;
            if (xMLDTDContentModelHandler4 != null) {
                xMLDTDContentModelHandler4.startGroup(null);
            }
            this.fStringBuffer.clear();
            this.fStringBuffer.append('(');
            this.fMarkUpDepth++;
            skipSeparator(false, !scanningInternalSubset());
            if (this.fEntityScanner.skipString("#PCDATA")) {
                scanMixed(scanName);
            } else {
                scanChildren(scanName);
            }
            str = this.fStringBuffer.toString();
        }
        XMLDTDContentModelHandler xMLDTDContentModelHandler5 = this.fDTDContentModelHandler;
        if (xMLDTDContentModelHandler5 != null) {
            xMLDTDContentModelHandler5.endContentModel(null);
        }
        this.fReportEntity = false;
        skipSeparator(false, !scanningInternalSubset());
        if (!this.fEntityScanner.skipChar(62, null)) {
            reportFatalError("ElementDeclUnterminated", new Object[]{scanName});
        }
        this.fReportEntity = true;
        this.fMarkUpDepth--;
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.elementDecl(scanName, str, null);
        }
        if (this.nonValidatingMode) {
            this.nvGrammarInfo.elementDecl(scanName, str, null);
        }
    }

    private final void scanMixed(String str) throws IOException, XNIException {
        this.fStringBuffer.append("#PCDATA");
        XMLDTDContentModelHandler xMLDTDContentModelHandler = this.fDTDContentModelHandler;
        if (xMLDTDContentModelHandler != null) {
            xMLDTDContentModelHandler.pcdata(null);
        }
        skipSeparator(false, !scanningInternalSubset());
        String str2 = null;
        while (this.fEntityScanner.skipChar(124, null)) {
            this.fStringBuffer.append('|');
            XMLDTDContentModelHandler xMLDTDContentModelHandler2 = this.fDTDContentModelHandler;
            if (xMLDTDContentModelHandler2 != null) {
                xMLDTDContentModelHandler2.separator(0, null);
            }
            skipSeparator(false, !scanningInternalSubset());
            str2 = this.fEntityScanner.scanName(XMLScanner.NameType.ENTITY);
            if (str2 == null) {
                reportFatalError("MSG_ELEMENT_TYPE_REQUIRED_IN_MIXED_CONTENT", new Object[]{str});
            }
            this.fStringBuffer.append(str2);
            XMLDTDContentModelHandler xMLDTDContentModelHandler3 = this.fDTDContentModelHandler;
            if (xMLDTDContentModelHandler3 != null) {
                xMLDTDContentModelHandler3.element(str2, null);
            }
            skipSeparator(false, !scanningInternalSubset());
        }
        if (this.fEntityScanner.skipString(")*")) {
            this.fStringBuffer.append(")*");
            XMLDTDContentModelHandler xMLDTDContentModelHandler4 = this.fDTDContentModelHandler;
            if (xMLDTDContentModelHandler4 != null) {
                xMLDTDContentModelHandler4.endGroup(null);
                this.fDTDContentModelHandler.occurrence(3, null);
            }
        } else if (str2 != null) {
            reportFatalError("MixedContentUnterminated", new Object[]{str});
        } else if (this.fEntityScanner.skipChar(41, null)) {
            this.fStringBuffer.append(')');
            XMLDTDContentModelHandler xMLDTDContentModelHandler5 = this.fDTDContentModelHandler;
            if (xMLDTDContentModelHandler5 != null) {
                xMLDTDContentModelHandler5.endGroup(null);
            }
        } else {
            reportFatalError("MSG_CLOSE_PAREN_REQUIRED_IN_CHILDREN", new Object[]{str});
        }
        this.fMarkUpDepth--;
    }

    private final void scanChildren(String str) throws IOException, XNIException {
        int peekChar;
        this.fContentDepth = 0;
        pushContentStack(0);
        while (true) {
            int i = 0;
            while (!this.fEntityScanner.skipChar(40, null)) {
                skipSeparator(false, !scanningInternalSubset());
                String scanName = this.fEntityScanner.scanName(XMLScanner.NameType.ELEMENTSTART);
                if (scanName == null) {
                    reportFatalError("MSG_OPEN_PAREN_OR_ELEMENT_TYPE_REQUIRED_IN_CHILDREN", new Object[]{str});
                    return;
                }
                XMLDTDContentModelHandler xMLDTDContentModelHandler = this.fDTDContentModelHandler;
                if (xMLDTDContentModelHandler != null) {
                    xMLDTDContentModelHandler.element(scanName, null);
                }
                this.fStringBuffer.append(scanName);
                int peekChar2 = this.fEntityScanner.peekChar();
                if (peekChar2 == 63 || peekChar2 == 42 || peekChar2 == 43) {
                    if (this.fDTDContentModelHandler != null) {
                        this.fDTDContentModelHandler.occurrence(peekChar2 == 63 ? 2 : peekChar2 == 42 ? (short) 3 : 4, null);
                    }
                    this.fEntityScanner.scanChar(null);
                    this.fStringBuffer.append((char) peekChar2);
                }
                while (true) {
                    skipSeparator(false, !scanningInternalSubset());
                    peekChar = this.fEntityScanner.peekChar();
                    if (peekChar == 44 && i != 124) {
                        XMLDTDContentModelHandler xMLDTDContentModelHandler2 = this.fDTDContentModelHandler;
                        if (xMLDTDContentModelHandler2 != null) {
                            xMLDTDContentModelHandler2.separator(1, null);
                        }
                        this.fEntityScanner.scanChar(null);
                        this.fStringBuffer.append(',');
                    } else if (peekChar != 124 || i == 44) {
                        if (peekChar != 41) {
                            reportFatalError("MSG_CLOSE_PAREN_REQUIRED_IN_CHILDREN", new Object[]{str});
                        }
                        XMLDTDContentModelHandler xMLDTDContentModelHandler3 = this.fDTDContentModelHandler;
                        if (xMLDTDContentModelHandler3 != null) {
                            xMLDTDContentModelHandler3.endGroup(null);
                        }
                        int popContentStack = popContentStack();
                        if (this.fEntityScanner.skipString(")?")) {
                            this.fStringBuffer.append(")?");
                            XMLDTDContentModelHandler xMLDTDContentModelHandler4 = this.fDTDContentModelHandler;
                            if (xMLDTDContentModelHandler4 != null) {
                                xMLDTDContentModelHandler4.occurrence(2, null);
                            }
                        } else if (this.fEntityScanner.skipString(")+")) {
                            this.fStringBuffer.append(")+");
                            XMLDTDContentModelHandler xMLDTDContentModelHandler5 = this.fDTDContentModelHandler;
                            if (xMLDTDContentModelHandler5 != null) {
                                xMLDTDContentModelHandler5.occurrence(4, null);
                            }
                        } else if (this.fEntityScanner.skipString(")*")) {
                            this.fStringBuffer.append(")*");
                            XMLDTDContentModelHandler xMLDTDContentModelHandler6 = this.fDTDContentModelHandler;
                            if (xMLDTDContentModelHandler6 != null) {
                                xMLDTDContentModelHandler6.occurrence(3, null);
                            }
                        } else {
                            this.fEntityScanner.scanChar(null);
                            this.fStringBuffer.append(')');
                        }
                        this.fMarkUpDepth--;
                        if (this.fContentDepth != 0) {
                            i = popContentStack;
                        } else {
                            return;
                        }
                    } else {
                        XMLDTDContentModelHandler xMLDTDContentModelHandler7 = this.fDTDContentModelHandler;
                        if (xMLDTDContentModelHandler7 != null) {
                            xMLDTDContentModelHandler7.separator(0, null);
                        }
                        this.fEntityScanner.scanChar(null);
                        this.fStringBuffer.append('|');
                    }
                }
                skipSeparator(false, !scanningInternalSubset());
                i = peekChar;
            }
            this.fMarkUpDepth++;
            this.fStringBuffer.append('(');
            XMLDTDContentModelHandler xMLDTDContentModelHandler8 = this.fDTDContentModelHandler;
            if (xMLDTDContentModelHandler8 != null) {
                xMLDTDContentModelHandler8.startGroup(null);
            }
            pushContentStack(i);
            skipSeparator(false, !scanningInternalSubset());
        }
    }

    /* access modifiers changed from: protected */
    public final void scanAttlistDecl() throws IOException, XNIException {
        String[] strArr;
        String str;
        int i;
        String str2;
        int i2;
        this.fReportEntity = false;
        if (!skipSeparator(true, !scanningInternalSubset())) {
            reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ELEMENT_TYPE_IN_ATTLISTDECL", null);
        }
        String scanName = this.fEntityScanner.scanName(XMLScanner.NameType.ELEMENTSTART);
        if (scanName == null) {
            reportFatalError("MSG_ELEMENT_TYPE_REQUIRED_IN_ATTLISTDECL", null);
        }
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.startAttlist(scanName, null);
        }
        int i3 = 62;
        if (!skipSeparator(true, !scanningInternalSubset())) {
            if (this.fEntityScanner.skipChar(62, null)) {
                XMLDTDHandler xMLDTDHandler2 = this.fDTDHandler;
                if (xMLDTDHandler2 != null) {
                    xMLDTDHandler2.endAttlist(null);
                }
                this.fMarkUpDepth--;
                return;
            }
            reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ATTRIBUTE_NAME_IN_ATTDEF", new Object[]{scanName});
        }
        while (!this.fEntityScanner.skipChar(i3, null)) {
            String scanName2 = this.fEntityScanner.scanName(XMLScanner.NameType.ATTRIBUTENAME);
            if (scanName2 == null) {
                reportFatalError("AttNameRequiredInAttDef", new Object[]{scanName});
            }
            if (!skipSeparator(true, !scanningInternalSubset())) {
                reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ATTTYPE_IN_ATTDEF", new Object[]{scanName, scanName2});
            }
            String scanAttType = scanAttType(scanName, scanName2);
            if (!skipSeparator(true, !scanningInternalSubset())) {
                reportFatalError("MSG_SPACE_REQUIRED_BEFORE_DEFAULTDECL_IN_ATTDEF", new Object[]{scanName, scanName2});
            }
            String scanAttDefaultDecl = scanAttDefaultDecl(scanName, scanName2, scanAttType, this.fLiteral, this.fLiteral2);
            if ((this.fDTDHandler != null || this.nonValidatingMode) && (i2 = this.fEnumerationCount) != 0) {
                strArr = new String[i2];
                System.arraycopy(this.fEnumeration, 0, strArr, 0, i2);
            } else {
                strArr = null;
            }
            if (scanAttDefaultDecl == null || (!scanAttDefaultDecl.equals("#REQUIRED") && !scanAttDefaultDecl.equals("#IMPLIED"))) {
                i = i3;
                str = scanName;
                XMLDTDHandler xMLDTDHandler3 = this.fDTDHandler;
                if (xMLDTDHandler3 != null) {
                    xMLDTDHandler3.attributeDecl(str, scanName2, scanAttType, strArr, scanAttDefaultDecl, this.fLiteral, this.fLiteral2, null);
                }
                if (this.nonValidatingMode) {
                    this.nvGrammarInfo.attributeDecl(str, scanName2, scanAttType, strArr, scanAttDefaultDecl, this.fLiteral, this.fLiteral2, null);
                }
            } else {
                XMLDTDHandler xMLDTDHandler4 = this.fDTDHandler;
                if (xMLDTDHandler4 != null) {
                    str2 = scanName2;
                    i = i3;
                    str = scanName;
                    xMLDTDHandler4.attributeDecl(scanName, scanName2, scanAttType, strArr, scanAttDefaultDecl, null, null, null);
                } else {
                    str2 = scanName2;
                    i = i3;
                    str = scanName;
                }
                if (this.nonValidatingMode) {
                    this.nvGrammarInfo.attributeDecl(str, str2, scanAttType, strArr, scanAttDefaultDecl, null, null, null);
                }
            }
            skipSeparator(false, !scanningInternalSubset());
            i3 = i;
            scanName = str;
        }
        XMLDTDHandler xMLDTDHandler5 = this.fDTDHandler;
        if (xMLDTDHandler5 != null) {
            xMLDTDHandler5.endAttlist(null);
        }
        this.fMarkUpDepth--;
        this.fReportEntity = true;
    }

    private final String scanAttType(String str, String str2) throws IOException, XNIException {
        int scanChar;
        int scanChar2;
        this.fEnumerationCount = 0;
        if (this.fEntityScanner.skipString("CDATA")) {
            return "CDATA";
        }
        if (this.fEntityScanner.skipString(SchemaSymbols.ATTVAL_IDREFS)) {
            return SchemaSymbols.ATTVAL_IDREFS;
        }
        if (this.fEntityScanner.skipString(SchemaSymbols.ATTVAL_IDREF)) {
            return SchemaSymbols.ATTVAL_IDREF;
        }
        if (this.fEntityScanner.skipString(SchemaSymbols.ATTVAL_ID)) {
            return SchemaSymbols.ATTVAL_ID;
        }
        if (this.fEntityScanner.skipString(SchemaSymbols.ATTVAL_ENTITY)) {
            return SchemaSymbols.ATTVAL_ENTITY;
        }
        if (this.fEntityScanner.skipString(SchemaSymbols.ATTVAL_ENTITIES)) {
            return SchemaSymbols.ATTVAL_ENTITIES;
        }
        if (this.fEntityScanner.skipString(SchemaSymbols.ATTVAL_NMTOKENS)) {
            return SchemaSymbols.ATTVAL_NMTOKENS;
        }
        if (this.fEntityScanner.skipString(SchemaSymbols.ATTVAL_NMTOKEN)) {
            return SchemaSymbols.ATTVAL_NMTOKEN;
        }
        if (this.fEntityScanner.skipString(SchemaSymbols.ATTVAL_NOTATION)) {
            if (!skipSeparator(true, !scanningInternalSubset())) {
                reportFatalError("MSG_SPACE_REQUIRED_AFTER_NOTATION_IN_NOTATIONTYPE", new Object[]{str, str2});
            }
            if (this.fEntityScanner.scanChar(null) != 40) {
                reportFatalError("MSG_OPEN_PAREN_REQUIRED_IN_NOTATIONTYPE", new Object[]{str, str2});
            }
            this.fMarkUpDepth++;
            do {
                skipSeparator(false, !scanningInternalSubset());
                String scanName = this.fEntityScanner.scanName(XMLScanner.NameType.ATTRIBUTENAME);
                if (scanName == null) {
                    reportFatalError("MSG_NAME_REQUIRED_IN_NOTATIONTYPE", new Object[]{str, str2});
                }
                ensureEnumerationSize(this.fEnumerationCount + 1);
                String[] strArr = this.fEnumeration;
                int i = this.fEnumerationCount;
                this.fEnumerationCount = i + 1;
                strArr[i] = scanName;
                skipSeparator(false, !scanningInternalSubset());
                scanChar2 = this.fEntityScanner.scanChar(null);
            } while (scanChar2 == 124);
            if (scanChar2 != 41) {
                reportFatalError("NotationTypeUnterminated", new Object[]{str, str2});
            }
            this.fMarkUpDepth--;
            return SchemaSymbols.ATTVAL_NOTATION;
        }
        if (this.fEntityScanner.scanChar(null) != 40) {
            reportFatalError("AttTypeRequiredInAttDef", new Object[]{str, str2});
        }
        this.fMarkUpDepth++;
        do {
            skipSeparator(false, !scanningInternalSubset());
            String scanNmtoken = this.fEntityScanner.scanNmtoken();
            if (scanNmtoken == null) {
                reportFatalError("MSG_NMTOKEN_REQUIRED_IN_ENUMERATION", new Object[]{str, str2});
            }
            ensureEnumerationSize(this.fEnumerationCount + 1);
            String[] strArr2 = this.fEnumeration;
            int i2 = this.fEnumerationCount;
            this.fEnumerationCount = i2 + 1;
            strArr2[i2] = scanNmtoken;
            skipSeparator(false, !scanningInternalSubset());
            scanChar = this.fEntityScanner.scanChar(null);
        } while (scanChar == 124);
        if (scanChar != 41) {
            reportFatalError("EnumerationUnterminated", new Object[]{str, str2});
        }
        this.fMarkUpDepth--;
        return "ENUMERATION";
    }

    /* access modifiers changed from: protected */
    public final String scanAttDefaultDecl(String str, String str2, String str3, XMLString xMLString, XMLString xMLString2) throws IOException, XNIException {
        String str4;
        this.fString.clear();
        xMLString.clear();
        if (this.fEntityScanner.skipString("#REQUIRED")) {
            return "#REQUIRED";
        }
        if (this.fEntityScanner.skipString("#IMPLIED")) {
            return "#IMPLIED";
        }
        if (this.fEntityScanner.skipString("#FIXED")) {
            if (!skipSeparator(true, !scanningInternalSubset())) {
                reportFatalError("MSG_SPACE_REQUIRED_AFTER_FIXED_IN_DEFAULTDECL", new Object[]{str, str2});
            }
            str4 = "#FIXED";
        } else {
            str4 = null;
        }
        scanAttributeValue(xMLString, xMLString2, str2, this.fAttributes, 0, !this.fStandalone && (this.fSeenExternalDTD || this.fSeenExternalPE), str, false);
        return str4;
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x006e A[LOOP:0: B:24:0x006e->B:86:0x006e, LOOP_START, PHI: r2 
      PHI: (r2v12 boolean) = (r2v1 boolean), (r2v13 boolean) binds: [B:23:0x006c, B:86:0x006e] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c9  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00d9  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00fe  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x013f  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0184  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0194  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x01a7  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x01de  */
    private final void scanEntityDecl() throws IOException, XNIException {
        boolean z;
        boolean z2;
        String scanName;
        String str;
        String str2;
        this.fReportEntity = false;
        if (this.fEntityScanner.skipSpaces()) {
            if (this.fEntityScanner.skipChar(37, XMLScanner.NameType.REFERENCE)) {
                if (!skipSeparator(true, !scanningInternalSubset())) {
                    if (scanningInternalSubset()) {
                        reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ENTITY_NAME_IN_ENTITYDECL", null);
                    } else if (this.fEntityScanner.peekChar() == 37) {
                        skipSeparator(false, !scanningInternalSubset());
                    }
                }
                z2 = false;
                z = true;
                if (z2) {
                    while (true) {
                        String scanName2 = this.fEntityScanner.scanName(XMLScanner.NameType.REFERENCE);
                        if (scanName2 == null) {
                            reportFatalError("NameRequiredInPEReference", null);
                        } else if (!this.fEntityScanner.skipChar(59, XMLScanner.NameType.REFERENCE)) {
                            reportFatalError("SemicolonRequiredInPEReference", new Object[]{scanName2});
                        } else {
                            startPE(scanName2, false);
                        }
                        this.fEntityScanner.skipSpaces();
                        if (!this.fEntityScanner.skipChar(37, XMLScanner.NameType.REFERENCE)) {
                            break;
                        } else if (!z) {
                            if (skipSeparator(true, !scanningInternalSubset())) {
                                z = true;
                                break;
                            }
                            z = this.fEntityScanner.skipChar(37, XMLScanner.NameType.REFERENCE);
                        }
                    }
                }
                scanName = this.fEntityScanner.scanName(XMLScanner.NameType.ENTITY);
                if (scanName == null) {
                    reportFatalError("MSG_ENTITY_NAME_REQUIRED_IN_ENTITYDECL", null);
                }
                if (!skipSeparator(true, !scanningInternalSubset())) {
                    reportFatalError("MSG_SPACE_REQUIRED_AFTER_ENTITY_NAME_IN_ENTITYDECL", new Object[]{scanName});
                }
                scanExternalID(this.fStrings, false);
                String[] strArr = this.fStrings;
                str = strArr[0];
                String str3 = strArr[1];
                if (z && str != null) {
                    this.fSeenExternalPE = true;
                }
                boolean skipSeparator = skipSeparator(true, !scanningInternalSubset());
                if (!z || !this.fEntityScanner.skipString("NDATA")) {
                    str2 = null;
                } else {
                    if (!skipSeparator) {
                        reportFatalError("MSG_SPACE_REQUIRED_BEFORE_NDATA_IN_UNPARSED_ENTITYDECL", new Object[]{scanName});
                    }
                    if (!skipSeparator(true, !scanningInternalSubset())) {
                        reportFatalError("MSG_SPACE_REQUIRED_BEFORE_NOTATION_NAME_IN_UNPARSED_ENTITYDECL", new Object[]{scanName});
                    }
                    String scanName3 = this.fEntityScanner.scanName(XMLScanner.NameType.NOTATION);
                    if (scanName3 == null) {
                        reportFatalError("MSG_NOTATION_NAME_REQUIRED_FOR_UNPARSED_ENTITYDECL", new Object[]{scanName});
                    }
                    str2 = scanName3;
                }
                if (str == null) {
                    scanEntityValue(scanName, z, this.fLiteral, this.fLiteral2);
                    this.fStringBuffer.clear();
                    this.fStringBuffer2.clear();
                    this.fStringBuffer.append(this.fLiteral.ch, this.fLiteral.offset, this.fLiteral.length);
                    this.fStringBuffer2.append(this.fLiteral2.ch, this.fLiteral2.offset, this.fLiteral2.length);
                }
                skipSeparator(false, !scanningInternalSubset());
                if (!this.fEntityScanner.skipChar(62, null)) {
                    reportFatalError("EntityDeclUnterminated", new Object[]{scanName});
                }
                this.fMarkUpDepth--;
                if (z) {
                    scanName = "%" + scanName;
                }
                if (str != null) {
                    String baseSystemId = this.fEntityScanner.getBaseSystemId();
                    if (str2 != null) {
                        this.fEntityStore.addUnparsedEntity(scanName, str3, str, baseSystemId, str2);
                    } else {
                        this.fEntityStore.addExternalEntity(scanName, str3, str, baseSystemId);
                    }
                    if (this.fDTDHandler != null) {
                        this.fResourceIdentifier.setValues(str3, str, baseSystemId, XMLEntityManager.expandSystemId(str, baseSystemId));
                        if (str2 != null) {
                            this.fDTDHandler.unparsedEntityDecl(scanName, this.fResourceIdentifier, str2, null);
                        } else {
                            this.fDTDHandler.externalEntityDecl(scanName, this.fResourceIdentifier, null);
                        }
                    }
                } else {
                    this.fEntityStore.addInternalEntity(scanName, this.fStringBuffer.toString());
                    XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
                    if (xMLDTDHandler != null) {
                        xMLDTDHandler.internalEntityDecl(scanName, this.fStringBuffer, this.fStringBuffer2, null);
                    }
                }
                this.fReportEntity = true;
            }
            z2 = false;
            z = false;
            if (z2) {
            }
            scanName = this.fEntityScanner.scanName(XMLScanner.NameType.ENTITY);
            if (scanName == null) {
            }
            if (!skipSeparator(true, !scanningInternalSubset())) {
            }
            scanExternalID(this.fStrings, false);
            String[] strArr2 = this.fStrings;
            str = strArr2[0];
            String str32 = strArr2[1];
            this.fSeenExternalPE = true;
            boolean skipSeparator2 = skipSeparator(true, !scanningInternalSubset());
            if (!z) {
            }
            str2 = null;
            if (str == null) {
            }
            skipSeparator(false, !scanningInternalSubset());
            if (!this.fEntityScanner.skipChar(62, null)) {
            }
            this.fMarkUpDepth--;
            if (z) {
            }
            if (str != null) {
            }
            this.fReportEntity = true;
        } else if (scanningInternalSubset() || !this.fEntityScanner.skipChar(37, XMLScanner.NameType.REFERENCE)) {
            reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ENTITY_NAME_IN_ENTITYDECL", null);
            z2 = false;
            z = false;
            if (z2) {
            }
            scanName = this.fEntityScanner.scanName(XMLScanner.NameType.ENTITY);
            if (scanName == null) {
            }
            if (!skipSeparator(true, !scanningInternalSubset())) {
            }
            scanExternalID(this.fStrings, false);
            String[] strArr22 = this.fStrings;
            str = strArr22[0];
            String str322 = strArr22[1];
            this.fSeenExternalPE = true;
            boolean skipSeparator22 = skipSeparator(true, !scanningInternalSubset());
            if (!z) {
            }
            str2 = null;
            if (str == null) {
            }
            skipSeparator(false, !scanningInternalSubset());
            if (!this.fEntityScanner.skipChar(62, null)) {
            }
            this.fMarkUpDepth--;
            if (z) {
            }
            if (str != null) {
            }
            this.fReportEntity = true;
        } else if (this.fEntityScanner.skipSpaces()) {
            reportFatalError("MSG_SPACE_REQUIRED_BEFORE_PERCENT_IN_PEDECL", null);
            z2 = false;
            z = false;
            if (z2) {
            }
            scanName = this.fEntityScanner.scanName(XMLScanner.NameType.ENTITY);
            if (scanName == null) {
            }
            if (!skipSeparator(true, !scanningInternalSubset())) {
            }
            scanExternalID(this.fStrings, false);
            String[] strArr222 = this.fStrings;
            str = strArr222[0];
            String str3222 = strArr222[1];
            this.fSeenExternalPE = true;
            boolean skipSeparator222 = skipSeparator(true, !scanningInternalSubset());
            if (!z) {
            }
            str2 = null;
            if (str == null) {
            }
            skipSeparator(false, !scanningInternalSubset());
            if (!this.fEntityScanner.skipChar(62, null)) {
            }
            this.fMarkUpDepth--;
            if (z) {
            }
            if (str != null) {
            }
            this.fReportEntity = true;
        }
        z = false;
        z2 = true;
        if (z2) {
        }
        scanName = this.fEntityScanner.scanName(XMLScanner.NameType.ENTITY);
        if (scanName == null) {
        }
        if (!skipSeparator(true, !scanningInternalSubset())) {
        }
        scanExternalID(this.fStrings, false);
        String[] strArr2222 = this.fStrings;
        str = strArr2222[0];
        String str32222 = strArr2222[1];
        this.fSeenExternalPE = true;
        boolean skipSeparator2222 = skipSeparator(true, !scanningInternalSubset());
        if (!z) {
        }
        str2 = null;
        if (str == null) {
        }
        skipSeparator(false, !scanningInternalSubset());
        if (!this.fEntityScanner.skipChar(62, null)) {
        }
        this.fMarkUpDepth--;
        if (z) {
        }
        if (str != null) {
        }
        this.fReportEntity = true;
    }

    /* access modifiers changed from: protected */
    public final void scanEntityValue(String str, boolean z, XMLString xMLString, XMLString xMLString2) throws IOException, XNIException {
        XMLString xMLString3;
        int scanChar = this.fEntityScanner.scanChar(null);
        if (!(scanChar == 39 || scanChar == 34)) {
            reportFatalError("OpenQuoteMissingInDecl", null);
        }
        int i = this.fEntityDepth;
        XMLString xMLString4 = this.fString;
        if (this.fLimitAnalyzer == null) {
            this.fLimitAnalyzer = this.fEntityManager.fLimitAnalyzer;
        }
        this.fLimitAnalyzer.startEntity(str);
        if (this.fEntityScanner.scanLiteral(scanChar, this.fString, false) != scanChar) {
            this.fStringBuffer.clear();
            this.fStringBuffer2.clear();
            do {
                int i2 = this.fStringBuffer.length;
                this.fStringBuffer.append(this.fString);
                this.fStringBuffer2.append(this.fString);
                int i3 = 1;
                if (this.fEntityScanner.skipChar(38, XMLScanner.NameType.REFERENCE)) {
                    if (this.fEntityScanner.skipChar(35, XMLScanner.NameType.REFERENCE)) {
                        this.fStringBuffer2.append("&#");
                        scanCharReferenceValue(this.fStringBuffer, this.fStringBuffer2);
                    } else {
                        this.fStringBuffer.append('&');
                        this.fStringBuffer2.append('&');
                        String scanName = this.fEntityScanner.scanName(XMLScanner.NameType.REFERENCE);
                        if (scanName == null) {
                            reportFatalError("NameRequiredInReference", null);
                        } else {
                            this.fStringBuffer.append(scanName);
                            this.fStringBuffer2.append(scanName);
                        }
                        if (!this.fEntityScanner.skipChar(59, XMLScanner.NameType.REFERENCE)) {
                            reportFatalError("SemicolonRequiredInReference", new Object[]{scanName});
                        } else {
                            this.fStringBuffer.append(';');
                            this.fStringBuffer2.append(';');
                        }
                    }
                } else if (this.fEntityScanner.skipChar(37, XMLScanner.NameType.REFERENCE)) {
                    do {
                        this.fStringBuffer2.append('%');
                        String scanName2 = this.fEntityScanner.scanName(XMLScanner.NameType.REFERENCE);
                        if (scanName2 == null) {
                            reportFatalError("NameRequiredInPEReference", null);
                        } else if (!this.fEntityScanner.skipChar(59, XMLScanner.NameType.REFERENCE)) {
                            reportFatalError("SemicolonRequiredInPEReference", new Object[]{scanName2});
                        } else {
                            if (scanningInternalSubset()) {
                                reportFatalError("PEReferenceWithinMarkup", new Object[]{scanName2});
                            }
                            this.fStringBuffer2.append(scanName2);
                            this.fStringBuffer2.append(';');
                        }
                        startPE(scanName2, true);
                        this.fEntityScanner.skipSpaces();
                    } while (this.fEntityScanner.skipChar(37, XMLScanner.NameType.REFERENCE));
                } else {
                    int peekChar = this.fEntityScanner.peekChar();
                    if (XMLChar.isHighSurrogate(peekChar)) {
                        scanSurrogates(this.fStringBuffer2);
                        checkEntityLimit(z, str, (this.fStringBuffer.length - i2) + i3);
                    } else if (isInvalidLiteral(peekChar)) {
                        reportFatalError("InvalidCharInLiteral", new Object[]{Integer.toHexString(peekChar)});
                        this.fEntityScanner.scanChar(null);
                    } else if (!(peekChar == scanChar && i == this.fEntityDepth)) {
                        char c = (char) peekChar;
                        this.fStringBuffer.append(c);
                        this.fStringBuffer2.append(c);
                        this.fEntityScanner.scanChar(null);
                    }
                }
                i3 = 0;
                checkEntityLimit(z, str, (this.fStringBuffer.length - i2) + i3);
            } while (this.fEntityScanner.scanLiteral(scanChar, this.fString, false) != scanChar);
            checkEntityLimit(z, str, this.fString.length);
            this.fStringBuffer.append(this.fString);
            this.fStringBuffer2.append(this.fString);
            xMLString4 = this.fStringBuffer;
            xMLString3 = this.fStringBuffer2;
        } else {
            checkEntityLimit(z, str, xMLString4);
            xMLString3 = xMLString4;
        }
        xMLString.setValues(xMLString4);
        xMLString2.setValues(xMLString3);
        if (this.fLimitAnalyzer != null) {
            if (z) {
                this.fLimitAnalyzer.endEntity(XMLSecurityManager.Limit.PARAMETER_ENTITY_SIZE_LIMIT, str);
            } else {
                this.fLimitAnalyzer.endEntity(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT, str);
            }
        }
        if (!this.fEntityScanner.skipChar(scanChar, null)) {
            reportFatalError("CloseQuoteMissingInDecl", null);
        }
    }

    private final void scanNotationDecl() throws IOException, XNIException {
        this.fReportEntity = false;
        if (!skipSeparator(true, !scanningInternalSubset())) {
            reportFatalError("MSG_SPACE_REQUIRED_BEFORE_NOTATION_NAME_IN_NOTATIONDECL", null);
        }
        String scanName = this.fEntityScanner.scanName(XMLScanner.NameType.NOTATION);
        if (scanName == null) {
            reportFatalError("MSG_NOTATION_NAME_REQUIRED_IN_NOTATIONDECL", null);
        }
        if (!skipSeparator(true, !scanningInternalSubset())) {
            reportFatalError("MSG_SPACE_REQUIRED_AFTER_NOTATION_NAME_IN_NOTATIONDECL", new Object[]{scanName});
        }
        scanExternalID(this.fStrings, true);
        String[] strArr = this.fStrings;
        String str = strArr[0];
        String str2 = strArr[1];
        String baseSystemId = this.fEntityScanner.getBaseSystemId();
        if (str == null && str2 == null) {
            reportFatalError("ExternalIDorPublicIDRequired", new Object[]{scanName});
        }
        skipSeparator(false, !scanningInternalSubset());
        if (!this.fEntityScanner.skipChar(62, null)) {
            reportFatalError("NotationDeclUnterminated", new Object[]{scanName});
        }
        this.fMarkUpDepth--;
        this.fResourceIdentifier.setValues(str2, str, baseSystemId, XMLEntityManager.expandSystemId(str, baseSystemId));
        if (this.nonValidatingMode) {
            this.nvGrammarInfo.notationDecl(scanName, this.fResourceIdentifier, null);
        }
        XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
        if (xMLDTDHandler != null) {
            xMLDTDHandler.notationDecl(scanName, this.fResourceIdentifier, null);
        }
        this.fReportEntity = true;
    }

    private final void scanConditionalSect(int i) throws IOException, XNIException {
        this.fReportEntity = false;
        skipSeparator(false, !scanningInternalSubset());
        if (this.fEntityScanner.skipString("INCLUDE")) {
            skipSeparator(false, !scanningInternalSubset());
            if (i != this.fPEDepth && this.fValidation) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "INVALID_PE_IN_CONDITIONAL", new Object[]{this.fEntityManager.fCurrentEntity.name}, 1);
            }
            if (!this.fEntityScanner.skipChar(91, null)) {
                reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD", null);
            }
            XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
            if (xMLDTDHandler != null) {
                xMLDTDHandler.startConditional(0, null);
            }
            this.fIncludeSectDepth++;
            this.fReportEntity = true;
        } else if (this.fEntityScanner.skipString("IGNORE")) {
            skipSeparator(false, !scanningInternalSubset());
            if (i != this.fPEDepth && this.fValidation) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "INVALID_PE_IN_CONDITIONAL", new Object[]{this.fEntityManager.fCurrentEntity.name}, 1);
            }
            XMLDTDHandler xMLDTDHandler2 = this.fDTDHandler;
            if (xMLDTDHandler2 != null) {
                xMLDTDHandler2.startConditional(1, null);
            }
            if (!this.fEntityScanner.skipChar(91, null)) {
                reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD", null);
            }
            this.fReportEntity = true;
            int i2 = this.fIncludeSectDepth + 1;
            this.fIncludeSectDepth = i2;
            if (this.fDTDHandler != null) {
                this.fIgnoreConditionalBuffer.clear();
            }
            while (true) {
                if (this.fEntityScanner.skipChar(60, null)) {
                    if (this.fDTDHandler != null) {
                        this.fIgnoreConditionalBuffer.append('<');
                    }
                    if (this.fEntityScanner.skipChar(33, null)) {
                        if (this.fEntityScanner.skipChar(91, null)) {
                            if (this.fDTDHandler != null) {
                                this.fIgnoreConditionalBuffer.append("![");
                            }
                            this.fIncludeSectDepth++;
                        } else if (this.fDTDHandler != null) {
                            this.fIgnoreConditionalBuffer.append("!");
                        }
                    }
                } else if (this.fEntityScanner.skipChar(93, null)) {
                    if (this.fDTDHandler != null) {
                        this.fIgnoreConditionalBuffer.append(']');
                    }
                    if (!this.fEntityScanner.skipChar(93, null)) {
                        continue;
                    } else {
                        if (this.fDTDHandler != null) {
                            this.fIgnoreConditionalBuffer.append(']');
                        }
                        while (this.fEntityScanner.skipChar(93, null)) {
                            if (this.fDTDHandler != null) {
                                this.fIgnoreConditionalBuffer.append(']');
                            }
                        }
                        if (this.fEntityScanner.skipChar(62, null)) {
                            int i3 = this.fIncludeSectDepth;
                            this.fIncludeSectDepth = i3 - 1;
                            if (i3 == i2) {
                                this.fMarkUpDepth--;
                                if (this.fDTDHandler != null) {
                                    this.fLiteral.setValues(this.fIgnoreConditionalBuffer.ch, 0, this.fIgnoreConditionalBuffer.length - 2);
                                    this.fDTDHandler.ignoredCharacters(this.fLiteral, null);
                                    this.fDTDHandler.endConditional(null);
                                    return;
                                }
                                return;
                            } else if (this.fDTDHandler != null) {
                                this.fIgnoreConditionalBuffer.append('>');
                            }
                        } else {
                            continue;
                        }
                    }
                } else {
                    int scanChar = this.fEntityScanner.scanChar(null);
                    if (this.fScannerState == 0) {
                        reportFatalError("IgnoreSectUnterminated", null);
                        return;
                    } else if (this.fDTDHandler != null) {
                        this.fIgnoreConditionalBuffer.append((char) scanChar);
                    }
                }
            }
        } else {
            reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD", null);
        }
    }

    /* access modifiers changed from: protected */
    public final boolean scanDecls(boolean z) throws IOException, XNIException {
        skipSeparator(false, true);
        boolean z2 = true;
        while (z2 && this.fScannerState == 2) {
            if (this.fEntityScanner.skipChar(60, null)) {
                this.fMarkUpDepth++;
                if (this.fEntityScanner.skipChar(63, null)) {
                    this.fStringBuffer.clear();
                    scanPI(this.fStringBuffer);
                    this.fMarkUpDepth--;
                } else if (!this.fEntityScanner.skipChar(33, null)) {
                    this.fMarkUpDepth--;
                    reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD", null);
                } else if (this.fEntityScanner.skipChar(45, null)) {
                    if (!this.fEntityScanner.skipChar(45, null)) {
                        reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD", null);
                    } else {
                        scanComment();
                    }
                } else if (this.fEntityScanner.skipString("ELEMENT")) {
                    scanElementDecl();
                } else if (this.fEntityScanner.skipString("ATTLIST")) {
                    scanAttlistDecl();
                } else if (this.fEntityScanner.skipString(SchemaSymbols.ATTVAL_ENTITY)) {
                    scanEntityDecl();
                } else if (this.fEntityScanner.skipString(SchemaSymbols.ATTVAL_NOTATION)) {
                    scanNotationDecl();
                } else if (!this.fEntityScanner.skipChar(91, null) || scanningInternalSubset()) {
                    this.fMarkUpDepth--;
                    reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD", null);
                } else {
                    scanConditionalSect(this.fPEDepth);
                }
            } else if (this.fIncludeSectDepth > 0 && this.fEntityScanner.skipChar(93, null)) {
                if (!this.fEntityScanner.skipChar(93, null) || !this.fEntityScanner.skipChar(62, null)) {
                    reportFatalError("IncludeSectUnterminated", null);
                }
                XMLDTDHandler xMLDTDHandler = this.fDTDHandler;
                if (xMLDTDHandler != null) {
                    xMLDTDHandler.endConditional(null);
                }
                this.fIncludeSectDepth--;
                this.fMarkUpDepth--;
            } else if (scanningInternalSubset() && this.fEntityScanner.peekChar() == 93) {
                return false;
            } else {
                if (!this.fEntityScanner.skipSpaces()) {
                    reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD", null);
                }
            }
            skipSeparator(false, true);
            z2 = z;
        }
        return this.fScannerState != 0;
    }

    private boolean skipSeparator(boolean z, boolean z2) throws IOException, XNIException {
        int i = this.fPEDepth;
        boolean skipSpaces = this.fEntityScanner.skipSpaces();
        if (!z2 || !this.fEntityScanner.skipChar(37, XMLScanner.NameType.REFERENCE)) {
            return !z || skipSpaces || i != this.fPEDepth;
        }
        do {
            String scanName = this.fEntityScanner.scanName(XMLScanner.NameType.ENTITY);
            if (scanName == null) {
                reportFatalError("NameRequiredInPEReference", null);
            } else if (!this.fEntityScanner.skipChar(59, XMLScanner.NameType.REFERENCE)) {
                reportFatalError("SemicolonRequiredInPEReference", new Object[]{scanName});
            }
            startPE(scanName, false);
            this.fEntityScanner.skipSpaces();
        } while (this.fEntityScanner.skipChar(37, XMLScanner.NameType.REFERENCE));
        return true;
    }

    private final void pushContentStack(int i) {
        int[] iArr = this.fContentStack;
        int length = iArr.length;
        int i2 = this.fContentDepth;
        if (length == i2) {
            int[] iArr2 = new int[(i2 * 2)];
            System.arraycopy(iArr, 0, iArr2, 0, i2);
            this.fContentStack = iArr2;
        }
        int[] iArr3 = this.fContentStack;
        int i3 = this.fContentDepth;
        this.fContentDepth = i3 + 1;
        iArr3[i3] = i;
    }

    private final int popContentStack() {
        int[] iArr = this.fContentStack;
        int i = this.fContentDepth - 1;
        this.fContentDepth = i;
        return iArr[i];
    }

    private final void pushPEStack(int i, boolean z) {
        int[] iArr = this.fPEStack;
        int length = iArr.length;
        int i2 = this.fPEDepth;
        if (length == i2) {
            int[] iArr2 = new int[(i2 * 2)];
            System.arraycopy(iArr, 0, iArr2, 0, i2);
            this.fPEStack = iArr2;
            int i3 = this.fPEDepth;
            boolean[] zArr = new boolean[(i3 * 2)];
            System.arraycopy(this.fPEReport, 0, zArr, 0, i3);
            this.fPEReport = zArr;
        }
        boolean[] zArr2 = this.fPEReport;
        int i4 = this.fPEDepth;
        zArr2[i4] = z;
        int[] iArr3 = this.fPEStack;
        this.fPEDepth = i4 + 1;
        iArr3[i4] = i;
    }

    private final int popPEStack() {
        int[] iArr = this.fPEStack;
        int i = this.fPEDepth - 1;
        this.fPEDepth = i;
        return iArr[i];
    }

    private final boolean peekReportEntity() {
        return this.fPEReport[this.fPEDepth - 1];
    }

    private final void ensureEnumerationSize(int i) {
        String[] strArr = this.fEnumeration;
        if (strArr.length == i) {
            String[] strArr2 = new String[(i * 2)];
            System.arraycopy(strArr, 0, strArr2, 0, i);
            this.fEnumeration = strArr2;
        }
    }

    private void init() {
        this.fStartDTDCalled = false;
        this.fExtEntityDepth = 0;
        this.fIncludeSectDepth = 0;
        this.fMarkUpDepth = 0;
        this.fPEDepth = 0;
        this.fStandalone = false;
        this.fSeenExternalDTD = false;
        this.fSeenExternalPE = false;
        setScannerState(1);
        this.fLimitAnalyzer = this.fEntityManager.fLimitAnalyzer;
        this.fSecurityManager = this.fEntityManager.fSecurityManager;
    }

    public DTDGrammar getGrammar() {
        return this.nvGrammarInfo;
    }
}
