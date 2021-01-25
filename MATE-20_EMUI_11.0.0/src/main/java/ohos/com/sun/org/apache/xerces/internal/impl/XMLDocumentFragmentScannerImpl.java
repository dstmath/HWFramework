package ohos.com.sun.org.apache.xerces.internal.impl;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner;
import ohos.com.sun.org.apache.xerces.internal.util.AugmentationsImpl;
import ohos.com.sun.org.apache.xerces.internal.util.XMLAttributesIteratorImpl;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.util.XMLStringBuffer;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentScanner;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.xml.internal.stream.XMLBufferListener;
import ohos.com.sun.xml.internal.stream.XMLEntityStorage;
import ohos.com.sun.xml.internal.stream.dtd.DTDGrammarUtil;
import ohos.global.icu.impl.PatternTokenizer;

public class XMLDocumentFragmentScannerImpl extends XMLScanner implements XMLDocumentScanner, XMLComponent, XMLEntityHandler, XMLBufferListener {
    protected static final boolean DEBUG = false;
    protected static final boolean DEBUG_COALESCE = false;
    private static final boolean DEBUG_DISPATCHER = false;
    protected static final boolean DEBUG_NEXT = false;
    private static final boolean DEBUG_SCANNER_STATE = false;
    static final boolean DEBUG_SKIP_ALGORITHM = false;
    protected static final boolean DEBUG_START_END_ELEMENT = false;
    static final short ELEMENT_ARRAY_LENGTH = 200;
    protected static final String ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
    static final String EXTERNAL_ACCESS_DEFAULT = "all";
    private static final Boolean[] FEATURE_DEFAULTS = {Boolean.TRUE, null, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE};
    static final short MAX_DEPTH_LIMIT = 5;
    static final short MAX_POINTER_AT_A_DEPTH = 4;
    protected static final String NOTIFY_BUILTIN_REFS = "http://apache.org/xml/features/scanner/notify-builtin-refs";
    private static final Object[] PROPERTY_DEFAULTS = {null, null, null, null};
    private static final String[] RECOGNIZED_FEATURES = {"http://xml.org/sax/features/namespaces", "http://xml.org/sax/features/validation", NOTIFY_BUILTIN_REFS, "http://apache.org/xml/features/scanner/notify-char-refs", Constants.STAX_REPORT_CDATA_EVENT};
    private static final String[] RECOGNIZED_PROPERTIES = {"http://apache.org/xml/properties/internal/symbol-table", "http://apache.org/xml/properties/internal/error-reporter", "http://apache.org/xml/properties/internal/entity-manager", "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager"};
    protected static final int SCANNER_STATE_ATTRIBUTE = 29;
    protected static final int SCANNER_STATE_ATTRIBUTE_VALUE = 30;
    protected static final int SCANNER_STATE_BUILT_IN_REFS = 41;
    protected static final int SCANNER_STATE_CDATA = 35;
    protected static final int SCANNER_STATE_CHARACTER_DATA = 37;
    protected static final int SCANNER_STATE_CHAR_REFERENCE = 40;
    protected static final int SCANNER_STATE_COMMENT = 27;
    protected static final int SCANNER_STATE_CONTENT = 22;
    protected static final int SCANNER_STATE_DOCTYPE = 24;
    protected static final int SCANNER_STATE_END_ELEMENT_TAG = 39;
    protected static final int SCANNER_STATE_END_OF_INPUT = 33;
    protected static final int SCANNER_STATE_PI = 23;
    protected static final int SCANNER_STATE_REFERENCE = 28;
    protected static final int SCANNER_STATE_ROOT_ELEMENT = 26;
    protected static final int SCANNER_STATE_START_ELEMENT_TAG = 38;
    protected static final int SCANNER_STATE_START_OF_MARKUP = 21;
    protected static final int SCANNER_STATE_TERMINATED = 34;
    protected static final int SCANNER_STATE_TEXT_DECL = 36;
    protected static final int SCANNER_STATE_XML_DECL = 25;
    protected static final String STANDARD_URI_CONFORMANT = "http://apache.org/xml/features/standard-uri-conformant";
    private static final String XML_SECURITY_PROPERTY_MANAGER = "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager";
    private static final char[] cdata = {'[', 'C', 'D', 'A', 'T', 'A', '['};
    static final char[] xmlDecl = {'<', '?', 'x', 'm', 'l'};
    protected DTDGrammarUtil dtdGrammarUtil = null;
    protected String fAccessExternalDTD = "all";
    protected boolean fAdd = false;
    protected boolean fAddDefaultAttr = false;
    protected QName fAttributeQName = new QName();
    protected XMLAttributesIteratorImpl fAttributes = new XMLAttributesIteratorImpl();
    protected XMLStringBuffer fContentBuffer = new XMLStringBuffer();
    protected Driver fContentDriver = createContentDriver();
    protected QName fCurrentElement;
    private String fCurrentEntityName = null;
    protected String fDeclaredEncoding = null;
    protected boolean fDisallowDoctype = false;
    protected XMLDocumentHandler fDocumentHandler;
    protected Driver fDriver;
    String[] fElementArray = new String[200];
    protected int fElementAttributeLimit;
    short fElementPointer = 0;
    protected QName fElementQName = new QName();
    protected String fElementRawname;
    protected ElementStack fElementStack = new ElementStack();
    protected ElementStack2 fElementStack2 = new ElementStack2();
    protected boolean fEmptyElement;
    protected int[] fEntityStack = new int[4];
    protected XMLEntityStorage fEntityStore;
    protected ExternalSubsetResolver fExternalSubsetResolver;
    protected boolean fHasExternalDTD;
    protected boolean fInScanContent = false;
    protected boolean fIsCoalesce = false;
    short fLastPointerLocation = 0;
    protected boolean fLastSectionWasCData = false;
    protected boolean fLastSectionWasCharacterData = false;
    protected boolean fLastSectionWasEntityReference = false;
    protected int fMarkupDepth;
    protected boolean fNotifyBuiltInRefs = false;
    protected XMLString fPIData = new XMLString();
    protected String fPITarget;
    short[][] fPointerInfo = ((short[][]) Array.newInstance(short.class, 5, 4));
    protected boolean fReadingAttributes = false;
    protected boolean fReplaceEntityReferences = true;
    protected boolean fReportCdataEvent = false;
    protected boolean fScanToEnd = false;
    protected int fScannerLastState;
    protected int fScannerState;
    protected boolean fShouldSkip = false;
    private final char[] fSingleChar = new char[1];
    protected boolean fSkip = false;
    protected boolean fStandalone;
    protected boolean fStandaloneSet;
    protected boolean fStrictURI;
    protected XMLStringBuffer fStringBuffer = new XMLStringBuffer();
    protected XMLStringBuffer fStringBuffer2 = new XMLStringBuffer();
    private String[] fStrings = new String[3];
    protected boolean fSupportDTD = true;
    protected boolean fSupportExternalEntities = false;
    private Augmentations fTempAugmentations = null;
    protected XMLString fTempString = new XMLString();
    protected XMLString fTempString2 = new XMLString();
    protected boolean fUsebuffer;
    protected String fVersion;
    protected int fXMLNameLimit;
    protected boolean foundBuiltInRefs = false;

    /* access modifiers changed from: protected */
    public interface Driver {
        int next() throws IOException, XNIException;
    }

    public String getDriverName(Driver driver) {
        return "null";
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentScanner
    public void setInputSource(XMLInputSource xMLInputSource) throws IOException {
        this.fEntityManager.setEntityHandler(this);
        this.fEntityManager.startEntity(false, "$fragment$", xMLInputSource, false, true);
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0094  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x009b A[RETURN] */
    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentScanner
    public boolean scanDocument(boolean z) throws IOException, XNIException {
        this.fEntityManager.setEntityHandler(this);
        int next = next();
        do {
            switch (next) {
                case 1:
                case 2:
                case 6:
                case 7:
                case 10:
                case 11:
                case 13:
                case 14:
                case 15:
                    break;
                case 3:
                    this.fEntityScanner.checkNodeCount(this.fEntityScanner.fCurrentEntity);
                    this.fDocumentHandler.processingInstruction(getPITarget(), getPIData(), null);
                    break;
                case 4:
                    this.fEntityScanner.checkNodeCount(this.fEntityScanner.fCurrentEntity);
                    this.fDocumentHandler.characters(getCharacterData(), null);
                    break;
                case 5:
                    this.fEntityScanner.checkNodeCount(this.fEntityScanner.fCurrentEntity);
                    this.fDocumentHandler.comment(getCharacterData(), null);
                    break;
                case 8:
                default:
                    throw new InternalError("processing event: " + next);
                case 9:
                    this.fEntityScanner.checkNodeCount(this.fEntityScanner.fCurrentEntity);
                    break;
                case 12:
                    this.fEntityScanner.checkNodeCount(this.fEntityScanner.fCurrentEntity);
                    this.fDocumentHandler.startCDATA(null);
                    this.fDocumentHandler.characters(getCharacterData(), null);
                    this.fDocumentHandler.endCDATA(null);
                    break;
            }
            next = next();
            if (next != 8) {
            }
            if (next == 8) {
                return true;
            }
            this.fDocumentHandler.endDocument(null);
            return false;
        } while (z);
        if (next == 8) {
        }
    }

    public QName getElementQName() {
        if (this.fScannerLastState == 2) {
            this.fElementQName.setValues(this.fElementStack.getLastPoppedElement());
        }
        return this.fElementQName;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentScanner
    public int next() throws IOException, XNIException {
        return this.fDriver.next();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void reset(XMLComponentManager xMLComponentManager) throws XMLConfigurationException {
        super.reset(xMLComponentManager);
        this.fReportCdataEvent = xMLComponentManager.getFeature(Constants.STAX_REPORT_CDATA_EVENT, true);
        this.fSecurityManager = (XMLSecurityManager) xMLComponentManager.getProperty("http://apache.org/xml/properties/security-manager", null);
        this.fNotifyBuiltInRefs = xMLComponentManager.getFeature(NOTIFY_BUILTIN_REFS, false);
        Object property = xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/entity-resolver", null);
        this.fExternalSubsetResolver = property instanceof ExternalSubsetResolver ? (ExternalSubsetResolver) property : null;
        this.fReadingAttributes = false;
        this.fSupportExternalEntities = true;
        this.fReplaceEntityReferences = true;
        this.fIsCoalesce = false;
        setScannerState(22);
        setDriver(this.fContentDriver);
        this.fAccessExternalDTD = ((XMLSecurityPropertyManager) xMLComponentManager.getProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", null)).getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD);
        this.fStrictURI = xMLComponentManager.getFeature(STANDARD_URI_CONFORMANT, false);
        resetCommon();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner
    public void reset(PropertyManager propertyManager) {
        super.reset(propertyManager);
        this.fNamespaces = ((Boolean) propertyManager.getProperty("javax.xml.stream.isNamespaceAware")).booleanValue();
        boolean z = false;
        this.fNotifyBuiltInRefs = false;
        this.fReplaceEntityReferences = ((Boolean) propertyManager.getProperty("javax.xml.stream.isReplacingEntityReferences")).booleanValue();
        this.fSupportExternalEntities = ((Boolean) propertyManager.getProperty("javax.xml.stream.isSupportingExternalEntities")).booleanValue();
        Boolean bool = (Boolean) propertyManager.getProperty("http://java.sun.com/xml/stream/properties/report-cdata-event");
        if (bool != null) {
            this.fReportCdataEvent = bool.booleanValue();
        }
        Boolean bool2 = (Boolean) propertyManager.getProperty("javax.xml.stream.isCoalescing");
        if (bool2 != null) {
            this.fIsCoalesce = bool2.booleanValue();
        }
        boolean z2 = true;
        if (!this.fIsCoalesce && this.fReportCdataEvent) {
            z = true;
        }
        this.fReportCdataEvent = z;
        if (!this.fIsCoalesce) {
            z2 = this.fReplaceEntityReferences;
        }
        this.fReplaceEntityReferences = z2;
        this.fAccessExternalDTD = ((XMLSecurityPropertyManager) propertyManager.getProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager")).getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD);
        this.fSecurityManager = (XMLSecurityManager) propertyManager.getProperty("http://apache.org/xml/properties/security-manager");
        resetCommon();
    }

    /* access modifiers changed from: package-private */
    public void resetCommon() {
        this.fMarkupDepth = 0;
        this.fCurrentElement = null;
        this.fElementStack.clear();
        this.fHasExternalDTD = false;
        this.fStandaloneSet = false;
        this.fStandalone = false;
        this.fInScanContent = false;
        this.fShouldSkip = false;
        this.fAdd = false;
        this.fSkip = false;
        this.fEntityStore = this.fEntityManager.getEntityStore();
        this.dtdGrammarUtil = null;
        if (this.fSecurityManager != null) {
            this.fElementAttributeLimit = this.fSecurityManager.getLimit(XMLSecurityManager.Limit.ELEMENT_ATTRIBUTE_LIMIT);
            this.fXMLNameLimit = this.fSecurityManager.getLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT);
        } else {
            this.fElementAttributeLimit = 0;
            this.fXMLNameLimit = XMLSecurityManager.Limit.MAX_NAME_LIMIT.defaultValue();
        }
        this.fLimitAnalyzer = this.fEntityManager.fLimitAnalyzer;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public String[] getRecognizedFeatures() {
        return (String[]) RECOGNIZED_FEATURES.clone();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setFeature(String str, boolean z) throws XMLConfigurationException {
        super.setFeature(str, z);
        if (str.startsWith(Constants.XERCES_FEATURE_PREFIX) && str.substring(31).equals(Constants.NOTIFY_BUILTIN_REFS_FEATURE)) {
            this.fNotifyBuiltInRefs = z;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public String[] getRecognizedProperties() {
        return (String[]) RECOGNIZED_PROPERTIES.clone();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setProperty(String str, Object obj) throws XMLConfigurationException {
        super.setProperty(str, obj);
        if (str.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            int length = str.length() - 33;
            if (length == 23 && str.endsWith(Constants.ENTITY_MANAGER_PROPERTY)) {
                this.fEntityManager = (XMLEntityManager) obj;
                return;
            } else if (length == 24 && str.endsWith(Constants.ENTITY_RESOLVER_PROPERTY)) {
                this.fExternalSubsetResolver = obj instanceof ExternalSubsetResolver ? (ExternalSubsetResolver) obj : null;
                return;
            }
        }
        if (str.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            if (str.substring(33).equals(Constants.ENTITY_MANAGER_PROPERTY)) {
                this.fEntityManager = (XMLEntityManager) obj;
            }
        } else if (str.equals("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager")) {
            this.fAccessExternalDTD = ((XMLSecurityPropertyManager) obj).getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD);
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

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner, ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityHandler
    public void startEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
        int i = this.fEntityDepth;
        int[] iArr = this.fEntityStack;
        if (i == iArr.length) {
            int[] iArr2 = new int[(iArr.length * 2)];
            System.arraycopy(iArr, 0, iArr2, 0, iArr.length);
            this.fEntityStack = iArr2;
        }
        this.fEntityStack[this.fEntityDepth] = this.fMarkupDepth;
        super.startEntity(str, xMLResourceIdentifier, str2, augmentations);
        if (this.fStandalone && this.fEntityStore.isEntityDeclInExternalSubset(str)) {
            reportFatalError("MSG_REFERENCE_TO_EXTERNALLY_DECLARED_ENTITY_WHEN_STANDALONE", new Object[]{str});
        }
        if (this.fDocumentHandler != null && !this.fScanningAttribute && !str.equals("[xml]")) {
            this.fDocumentHandler.startGeneralEntity(str, xMLResourceIdentifier, str2, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner, ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityHandler
    public void endEntity(String str, Augmentations augmentations) throws IOException, XNIException {
        super.endEntity(str, augmentations);
        if (this.fMarkupDepth != this.fEntityStack[this.fEntityDepth]) {
            reportFatalError("MarkupEntityMismatch", null);
        }
        if (this.fDocumentHandler != null && !this.fScanningAttribute && !str.equals("[xml]")) {
            this.fDocumentHandler.endGeneralEntity(str, augmentations);
        }
    }

    /* access modifiers changed from: protected */
    public Driver createContentDriver() {
        return new FragmentContentDriver();
    }

    /* access modifiers changed from: protected */
    public void scanXMLDeclOrTextDecl(boolean z) throws IOException, XNIException {
        super.scanXMLDeclOrTextDecl(z, this.fStrings);
        boolean z2 = true;
        this.fMarkupDepth--;
        String[] strArr = this.fStrings;
        String str = strArr[0];
        String str2 = strArr[1];
        String str3 = strArr[2];
        this.fDeclaredEncoding = str2;
        this.fStandaloneSet = str3 != null;
        if (!this.fStandaloneSet || !str3.equals("yes")) {
            z2 = false;
        }
        this.fStandalone = z2;
        this.fEntityManager.setStandalone(this.fStandalone);
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            if (z) {
                xMLDocumentHandler.textDecl(str, str2, null);
            } else {
                xMLDocumentHandler.xmlDecl(str, str2, str3, null);
            }
        }
        if (str != null) {
            this.fEntityScanner.setVersion(str);
            this.fEntityScanner.setXMLVersion(str);
        }
        if (str2 != null && !this.fEntityScanner.getCurrentEntity().isEncodingExternallySpecified()) {
            this.fEntityScanner.setEncoding(str2);
        }
    }

    public String getPITarget() {
        return this.fPITarget;
    }

    public XMLStringBuffer getPIData() {
        return this.fContentBuffer;
    }

    public XMLString getCharacterData() {
        if (this.fUsebuffer) {
            return this.fContentBuffer;
        }
        return this.fTempString;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner
    public void scanPIData(String str, XMLStringBuffer xMLStringBuffer) throws IOException, XNIException {
        super.scanPIData(str, xMLStringBuffer);
        this.fPITarget = str;
        this.fMarkupDepth--;
    }

    /* access modifiers changed from: protected */
    public void scanComment() throws IOException, XNIException {
        this.fContentBuffer.clear();
        scanComment(this.fContentBuffer);
        this.fUsebuffer = true;
        this.fMarkupDepth--;
    }

    public String getComment() {
        return this.fContentBuffer.toString();
    }

    /* access modifiers changed from: package-private */
    public void addElement(String str) {
        short storePointerForADepth;
        short s = this.fElementPointer;
        if (s < 200) {
            this.fElementArray[s] = str;
            if (this.fElementStack.fDepth < 5 && (storePointerForADepth = storePointerForADepth(this.fElementPointer)) > 0) {
                short elementPointer = getElementPointer((short) this.fElementStack.fDepth, (short) (storePointerForADepth - 1));
                if (str == this.fElementArray[elementPointer]) {
                    this.fShouldSkip = true;
                    this.fLastPointerLocation = elementPointer;
                    resetPointer((short) this.fElementStack.fDepth, storePointerForADepth);
                    this.fElementArray[this.fElementPointer] = null;
                    return;
                }
                this.fShouldSkip = false;
            }
            this.fElementPointer = (short) (this.fElementPointer + 1);
        }
    }

    /* access modifiers changed from: package-private */
    public void resetPointer(short s, short s2) {
        this.fPointerInfo[s][s2] = 0;
    }

    /* access modifiers changed from: package-private */
    public short storePointerForADepth(short s) {
        short s2 = (short) this.fElementStack.fDepth;
        for (short s3 = 0; s3 < 4; s3 = (short) (s3 + 1)) {
            if (canStore(s2, s3)) {
                this.fPointerInfo[s2][s3] = s;
                return s3;
            }
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public boolean canStore(short s, short s2) {
        return this.fPointerInfo[s][s2] == 0;
    }

    /* access modifiers changed from: package-private */
    public short getElementPointer(short s, short s2) {
        return this.fPointerInfo[s][s2];
    }

    /* access modifiers changed from: package-private */
    public boolean skipFromTheBuffer(String str) throws IOException {
        if (!this.fEntityScanner.skipString(str)) {
            return false;
        }
        char peekChar = (char) this.fEntityScanner.peekChar();
        if (peekChar != ' ' && peekChar != '/' && peekChar != '>') {
            return false;
        }
        this.fElementRawname = str;
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean skipQElement(String str) throws IOException {
        if (XMLChar.isName(this.fEntityScanner.getChar(str.length()))) {
            return false;
        }
        return this.fEntityScanner.skipString(str);
    }

    /* access modifiers changed from: protected */
    public boolean skipElement() throws IOException {
        if (!this.fShouldSkip) {
            return false;
        }
        short s = this.fLastPointerLocation;
        if (s != 0) {
            String str = this.fElementArray[s + 1];
            if (str == null || !skipFromTheBuffer(str)) {
                this.fLastPointerLocation = 0;
            } else {
                this.fLastPointerLocation = (short) (this.fLastPointerLocation + 1);
                return true;
            }
        }
        if (!this.fShouldSkip || !skipElement(0)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean skipElement(short s) throws IOException {
        short s2 = (short) this.fElementStack.fDepth;
        if (s2 > 5) {
            this.fShouldSkip = false;
            return false;
        }
        while (s < 4) {
            short elementPointer = getElementPointer(s2, s);
            if (elementPointer == 0) {
                this.fShouldSkip = false;
                return false;
            }
            String[] strArr = this.fElementArray;
            if (strArr[elementPointer] == null || !skipFromTheBuffer(strArr[elementPointer])) {
                s = (short) (s + 1);
            } else {
                this.fLastPointerLocation = elementPointer;
                this.fShouldSkip = true;
                return true;
            }
        }
        this.fShouldSkip = false;
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean scanStartElement() throws IOException, XNIException {
        if (this.fSkip && !this.fAdd) {
            QName next = this.fElementStack.getNext();
            this.fSkip = this.fEntityScanner.skipString(next.rawname);
            if (this.fSkip) {
                this.fElementStack.push();
                this.fElementQName = next;
            } else {
                this.fElementStack.reposition();
            }
        }
        if (!this.fSkip || this.fAdd) {
            this.fElementQName = this.fElementStack.nextElement();
            if (this.fNamespaces) {
                this.fEntityScanner.scanQName(this.fElementQName, XMLScanner.NameType.ELEMENTSTART);
            } else {
                String scanName = this.fEntityScanner.scanName(XMLScanner.NameType.ELEMENTSTART);
                this.fElementQName.setValues(null, scanName, scanName, null);
            }
        }
        if (this.fAdd) {
            this.fElementStack.matchElement(this.fElementQName);
        }
        QName qName = this.fElementQName;
        this.fCurrentElement = qName;
        String str = qName.rawname;
        this.fEmptyElement = false;
        this.fAttributes.removeAllAttributes();
        checkDepth(str);
        if (!seekCloseOfStartTag()) {
            this.fReadingAttributes = true;
            this.fAttributeCacheUsedCount = 0;
            this.fStringBufferIndex = 0;
            this.fAddDefaultAttr = true;
            do {
                scanAttribute(this.fAttributes);
                if (this.fSecurityManager != null && !this.fSecurityManager.isNoLimit(this.fElementAttributeLimit) && this.fAttributes.getLength() > this.fElementAttributeLimit) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "ElementAttributeLimit", new Object[]{str, Integer.valueOf(this.fElementAttributeLimit)}, 2);
                }
            } while (!seekCloseOfStartTag());
            this.fReadingAttributes = false;
        }
        if (this.fEmptyElement) {
            this.fMarkupDepth--;
            if (this.fMarkupDepth < this.fEntityStack[this.fEntityDepth - 1]) {
                reportFatalError("ElementEntityMismatch", new Object[]{this.fCurrentElement.rawname});
            }
            XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
            if (xMLDocumentHandler != null) {
                xMLDocumentHandler.emptyElement(this.fElementQName, this.fAttributes, null);
            }
            this.fElementStack.popElement();
        } else {
            DTDGrammarUtil dTDGrammarUtil = this.dtdGrammarUtil;
            if (dTDGrammarUtil != null) {
                dTDGrammarUtil.startElement(this.fElementQName, this.fAttributes);
            }
            XMLDocumentHandler xMLDocumentHandler2 = this.fDocumentHandler;
            if (xMLDocumentHandler2 != null) {
                xMLDocumentHandler2.startElement(this.fElementQName, this.fAttributes, null);
            }
        }
        return this.fEmptyElement;
    }

    /* access modifiers changed from: protected */
    public boolean seekCloseOfStartTag() throws IOException, XNIException {
        boolean skipSpaces = this.fEntityScanner.skipSpaces();
        int peekChar = this.fEntityScanner.peekChar();
        if (peekChar == 62) {
            this.fEntityScanner.scanChar(null);
            return true;
        } else if (peekChar == 47) {
            this.fEntityScanner.scanChar(null);
            if (!this.fEntityScanner.skipChar(62, XMLScanner.NameType.ELEMENTEND)) {
                reportFatalError("ElementUnterminated", new Object[]{this.fElementQName.rawname});
            }
            this.fEmptyElement = true;
            return true;
        } else {
            if ((!isValidNameStartChar(peekChar) || !skipSpaces) && (!isValidNameStartHighSurrogate(peekChar) || !skipSpaces)) {
                reportFatalError("ElementUnterminated", new Object[]{this.fElementQName.rawname});
            }
            return false;
        }
    }

    public boolean hasAttributes() {
        return this.fAttributes.getLength() > 0;
    }

    public XMLAttributesIteratorImpl getAttributeIterator() {
        DTDGrammarUtil dTDGrammarUtil = this.dtdGrammarUtil;
        if (dTDGrammarUtil != null && this.fAddDefaultAttr) {
            dTDGrammarUtil.addDTDDefaultAttrs(this.fElementQName, this.fAttributes);
            this.fAddDefaultAttr = false;
        }
        return this.fAttributes;
    }

    public boolean standaloneSet() {
        return this.fStandaloneSet;
    }

    public boolean isStandAlone() {
        return this.fStandalone;
    }

    /* access modifiers changed from: protected */
    public void scanAttribute(XMLAttributes xMLAttributes) throws IOException, XNIException {
        if (this.fNamespaces) {
            this.fEntityScanner.scanQName(this.fAttributeQName, XMLScanner.NameType.ATTRIBUTENAME);
        } else {
            String scanName = this.fEntityScanner.scanName(XMLScanner.NameType.ATTRIBUTENAME);
            this.fAttributeQName.setValues(null, scanName, scanName, null);
        }
        this.fEntityScanner.skipSpaces();
        if (!this.fEntityScanner.skipChar(61, XMLScanner.NameType.ATTRIBUTE)) {
            reportFatalError("EqRequiredInAttribute", new Object[]{this.fCurrentElement.rawname, this.fAttributeQName.rawname});
        }
        this.fEntityScanner.skipSpaces();
        boolean z = this.fHasExternalDTD && !this.fStandalone;
        XMLString string = getString();
        scanAttributeValue(string, this.fTempString2, this.fAttributeQName.rawname, xMLAttributes, 0, z, this.fCurrentElement.rawname, false);
        int length = xMLAttributes.getLength();
        int addAttribute = xMLAttributes.addAttribute(this.fAttributeQName, XMLSymbols.fCDATASymbol, null);
        if (length == xMLAttributes.getLength()) {
            reportFatalError("AttributeNotUnique", new Object[]{this.fCurrentElement.rawname, this.fAttributeQName.rawname});
        }
        xMLAttributes.setValue(addAttribute, null, string);
        xMLAttributes.setSpecified(addAttribute, true);
    }

    /* access modifiers changed from: protected */
    public int scanContent(XMLStringBuffer xMLStringBuffer) throws IOException, XNIException {
        this.fTempString.length = 0;
        int scanContent = this.fEntityScanner.scanContent(this.fTempString);
        xMLStringBuffer.append(this.fTempString);
        this.fTempString.length = 0;
        int i = -1;
        if (scanContent == 13) {
            this.fEntityScanner.scanChar(null);
            xMLStringBuffer.append((char) scanContent);
        } else if (scanContent == 93) {
            xMLStringBuffer.append((char) this.fEntityScanner.scanChar(null));
            this.fInScanContent = true;
            if (this.fEntityScanner.skipChar(93, null)) {
                xMLStringBuffer.append(']');
                while (this.fEntityScanner.skipChar(93, null)) {
                    xMLStringBuffer.append(']');
                }
                if (this.fEntityScanner.skipChar(62, null)) {
                    reportFatalError("CDEndInContent", null);
                }
            }
            this.fInScanContent = false;
        } else {
            i = scanContent;
        }
        if (this.fDocumentHandler != null) {
            int i2 = xMLStringBuffer.length;
        }
        return i;
    }

    /* access modifiers changed from: protected */
    public boolean scanCDATASection(XMLStringBuffer xMLStringBuffer, boolean z) throws IOException, XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        while (this.fEntityScanner.scanData("]]>", xMLStringBuffer)) {
            int peekChar = this.fEntityScanner.peekChar();
            if (peekChar != -1 && isInvalidLiteral(peekChar)) {
                if (XMLChar.isHighSurrogate(peekChar)) {
                    scanSurrogates(xMLStringBuffer);
                } else {
                    reportFatalError("InvalidCharInCDSect", new Object[]{Integer.toString(peekChar, 16)});
                    this.fEntityScanner.scanChar(null);
                }
            }
            XMLDocumentHandler xMLDocumentHandler2 = this.fDocumentHandler;
        }
        this.fMarkupDepth--;
        if (this.fDocumentHandler != null) {
            int i = xMLStringBuffer.length;
        }
        XMLDocumentHandler xMLDocumentHandler3 = this.fDocumentHandler;
        return true;
    }

    /* access modifiers changed from: protected */
    public int scanEndElement() throws IOException, XNIException {
        QName popElement = this.fElementStack.popElement();
        String str = popElement.rawname;
        if (!this.fEntityScanner.skipString(popElement.rawname)) {
            reportFatalError("ETagRequired", new Object[]{str});
        }
        this.fEntityScanner.skipSpaces();
        if (!this.fEntityScanner.skipChar(62, XMLScanner.NameType.ELEMENTEND)) {
            reportFatalError("ETagUnterminated", new Object[]{str});
        }
        this.fMarkupDepth--;
        this.fMarkupDepth--;
        if (this.fMarkupDepth < this.fEntityStack[this.fEntityDepth - 1]) {
            reportFatalError("ElementEntityMismatch", new Object[]{str});
        }
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            xMLDocumentHandler.endElement(popElement, null);
        }
        DTDGrammarUtil dTDGrammarUtil = this.dtdGrammarUtil;
        if (dTDGrammarUtil != null) {
            dTDGrammarUtil.endElement(popElement);
        }
        return this.fMarkupDepth;
    }

    /* access modifiers changed from: protected */
    public void scanCharReference() throws IOException, XNIException {
        this.fStringBuffer2.clear();
        int scanCharReferenceValue = scanCharReferenceValue(this.fStringBuffer2, null);
        this.fMarkupDepth--;
        if (scanCharReferenceValue != -1 && this.fDocumentHandler != null) {
            if (this.fNotifyCharRefs) {
                this.fDocumentHandler.startGeneralEntity(this.fCharRefLiteral, null, null, null);
            }
            if (this.fValidation && scanCharReferenceValue <= 32) {
                Augmentations augmentations = this.fTempAugmentations;
                if (augmentations != null) {
                    augmentations.removeAllItems();
                } else {
                    this.fTempAugmentations = new AugmentationsImpl();
                }
                this.fTempAugmentations.putItem(Constants.CHAR_REF_PROBABLE_WS, Boolean.TRUE);
            }
            if (this.fNotifyCharRefs) {
                this.fDocumentHandler.endGeneralEntity(this.fCharRefLiteral, null);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void scanEntityReference(XMLStringBuffer xMLStringBuffer) throws IOException, XNIException {
        String scanName = this.fEntityScanner.scanName(XMLScanner.NameType.REFERENCE);
        if (scanName == null) {
            reportFatalError("NameRequiredInReference", null);
            return;
        }
        if (!this.fEntityScanner.skipChar(59, XMLScanner.NameType.REFERENCE)) {
            reportFatalError("SemicolonRequiredInReference", new Object[]{scanName});
        }
        if (this.fEntityStore.isUnparsedEntity(scanName)) {
            reportFatalError("ReferenceToUnparsedEntity", new Object[]{scanName});
        }
        this.fMarkupDepth--;
        this.fCurrentEntityName = scanName;
        if (scanName == fAmpSymbol) {
            handleCharacter('&', fAmpSymbol, xMLStringBuffer);
            this.fScannerState = 41;
        } else if (scanName == fLtSymbol) {
            handleCharacter('<', fLtSymbol, xMLStringBuffer);
            this.fScannerState = 41;
        } else if (scanName == fGtSymbol) {
            handleCharacter('>', fGtSymbol, xMLStringBuffer);
            this.fScannerState = 41;
        } else if (scanName == fQuotSymbol) {
            handleCharacter('\"', fQuotSymbol, xMLStringBuffer);
            this.fScannerState = 41;
        } else if (scanName == fAposSymbol) {
            handleCharacter(PatternTokenizer.SINGLE_QUOTE, fAposSymbol, xMLStringBuffer);
            this.fScannerState = 41;
        } else {
            boolean isExternalEntity = this.fEntityStore.isExternalEntity(scanName);
            if ((!isExternalEntity || this.fSupportExternalEntities) && ((isExternalEntity || this.fReplaceEntityReferences) && !this.foundBuiltInRefs)) {
                if (!this.fEntityStore.isDeclaredEntity(scanName)) {
                    if (!this.fSupportDTD && this.fReplaceEntityReferences) {
                        reportFatalError("EntityNotDeclared", new Object[]{scanName});
                        return;
                    } else if (!this.fHasExternalDTD || this.fStandalone) {
                        reportFatalError("EntityNotDeclared", new Object[]{scanName});
                    } else if (this.fValidation) {
                        this.fErrorReporter.reportError((XMLLocator) this.fEntityScanner, "http://www.w3.org/TR/1998/REC-xml-19980210", "EntityNotDeclared", new Object[]{scanName}, (short) 1);
                    }
                }
                this.fEntityManager.startEntity(true, scanName, false);
                return;
            }
            this.fScannerState = 28;
        }
    }

    /* access modifiers changed from: package-private */
    public void checkDepth(String str) {
        this.fLimitAnalyzer.addValue(XMLSecurityManager.Limit.MAX_ELEMENT_DEPTH_LIMIT, str, this.fElementStack.fDepth);
        if (this.fSecurityManager.isOverLimit(XMLSecurityManager.Limit.MAX_ELEMENT_DEPTH_LIMIT, this.fLimitAnalyzer)) {
            this.fSecurityManager.debugPrint(this.fLimitAnalyzer);
            reportFatalError("MaxElementDepthLimit", new Object[]{str, Integer.valueOf(this.fLimitAnalyzer.getTotalValue(XMLSecurityManager.Limit.MAX_ELEMENT_DEPTH_LIMIT)), Integer.valueOf(this.fSecurityManager.getLimit(XMLSecurityManager.Limit.MAX_ELEMENT_DEPTH_LIMIT)), "maxElementDepth"});
        }
    }

    private void handleCharacter(char c, String str, XMLStringBuffer xMLStringBuffer) throws XNIException {
        this.foundBuiltInRefs = true;
        checkEntityLimit(false, this.fEntityScanner.fCurrentEntity.name, 1);
        xMLStringBuffer.append(c);
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            this.fSingleChar[0] = c;
            if (this.fNotifyBuiltInRefs) {
                xMLDocumentHandler.startGeneralEntity(str, null, null, null);
            }
            this.fTempString.setValues(this.fSingleChar, 0, 1);
            if (this.fNotifyBuiltInRefs) {
                this.fDocumentHandler.endGeneralEntity(str, null);
            }
        }
    }

    /* access modifiers changed from: protected */
    public final void setScannerState(int i) {
        this.fScannerState = i;
    }

    /* access modifiers changed from: protected */
    public final void setDriver(Driver driver) {
        this.fDriver = driver;
    }

    /* access modifiers changed from: protected */
    public String getScannerStateName(int i) {
        switch (i) {
            case 21:
                return "SCANNER_STATE_START_OF_MARKUP";
            case 22:
                return "SCANNER_STATE_CONTENT";
            case 23:
                return "SCANNER_STATE_PI";
            case 24:
                return "SCANNER_STATE_DOCTYPE";
            case 25:
            case 31:
            case 32:
            default:
                return "??? (" + i + ')';
            case 26:
                return "SCANNER_STATE_ROOT_ELEMENT";
            case 27:
                return "SCANNER_STATE_COMMENT";
            case 28:
                return "SCANNER_STATE_REFERENCE";
            case 29:
                return "SCANNER_STATE_ATTRIBUTE";
            case 30:
                return "SCANNER_STATE_ATTRIBUTE_VALUE";
            case 33:
                return "SCANNER_STATE_END_OF_INPUT";
            case 34:
                return "SCANNER_STATE_TERMINATED";
            case 35:
                return "SCANNER_STATE_CDATA";
            case 36:
                return "SCANNER_STATE_TEXT_DECL";
            case 37:
                return "SCANNER_STATE_CHARACTER_DATA";
            case 38:
                return "SCANNER_STATE_START_ELEMENT_TAG";
            case 39:
                return "SCANNER_STATE_END_ELEMENT_TAG";
        }
    }

    public String getEntityName() {
        return this.fCurrentEntityName;
    }

    /* access modifiers changed from: package-private */
    public String checkAccess(String str, String str2) throws IOException {
        return SecuritySupport.checkAccess(XMLEntityManager.expandSystemId(str, this.fEntityScanner.getBaseSystemId(), this.fStrictURI), str2, "all");
    }

    protected static final class Element {
        public char[] fRawname;
        public Element next;
        public QName qname;

        public Element(QName qName, Element element) {
            this.qname.setValues(qName);
            this.fRawname = qName.rawname.toCharArray();
            this.next = element;
        }
    }

    protected class ElementStack2 {
        protected int fCount;
        protected int fDepth;
        protected int fLastDepth;
        protected int fMark;
        protected int fPosition;
        protected QName[] fQName = new QName[20];

        public ElementStack2() {
            int i = 0;
            while (true) {
                QName[] qNameArr = this.fQName;
                if (i < qNameArr.length) {
                    qNameArr[i] = new QName();
                    i++;
                } else {
                    this.fPosition = 1;
                    this.fMark = 1;
                    return;
                }
            }
        }

        public void resize() {
            QName[] qNameArr = this.fQName;
            int length = qNameArr.length;
            QName[] qNameArr2 = new QName[(length * 2)];
            System.arraycopy(qNameArr, 0, qNameArr2, 0, length);
            this.fQName = qNameArr2;
            while (true) {
                QName[] qNameArr3 = this.fQName;
                if (length < qNameArr3.length) {
                    qNameArr3[length] = new QName();
                    length++;
                } else {
                    return;
                }
            }
        }

        public boolean matchElement(QName qName) {
            int i = this.fLastDepth;
            int i2 = this.fDepth;
            boolean z = false;
            if (i > i2 && i2 <= 2) {
                if (qName.rawname == this.fQName[this.fDepth].rawname) {
                    XMLDocumentFragmentScannerImpl.this.fAdd = false;
                    this.fMark = this.fDepth - 1;
                    this.fPosition = this.fMark + 1;
                    this.fCount--;
                    z = true;
                } else {
                    XMLDocumentFragmentScannerImpl.this.fAdd = true;
                }
            }
            int i3 = this.fDepth;
            this.fDepth = i3 + 1;
            this.fLastDepth = i3;
            return z;
        }

        public QName nextElement() {
            int i = this.fCount;
            QName[] qNameArr = this.fQName;
            if (i == qNameArr.length) {
                XMLDocumentFragmentScannerImpl xMLDocumentFragmentScannerImpl = XMLDocumentFragmentScannerImpl.this;
                xMLDocumentFragmentScannerImpl.fShouldSkip = false;
                xMLDocumentFragmentScannerImpl.fAdd = false;
                int i2 = i - 1;
                this.fCount = i2;
                return qNameArr[i2];
            }
            this.fCount = i + 1;
            return qNameArr[i];
        }

        public QName getNext() {
            if (this.fPosition == this.fCount) {
                this.fPosition = this.fMark;
            }
            QName[] qNameArr = this.fQName;
            int i = this.fPosition;
            this.fPosition = i + 1;
            return qNameArr[i];
        }

        public int popElement() {
            int i = this.fDepth;
            this.fDepth = i - 1;
            return i;
        }

        public void clear() {
            this.fLastDepth = 0;
            this.fDepth = 0;
            this.fCount = 0;
            this.fMark = 1;
            this.fPosition = 1;
        }
    }

    /* access modifiers changed from: protected */
    public class ElementStack {
        protected int fCount;
        protected int fDepth;
        protected QName[] fElements = new QName[20];
        protected int[] fInt = new int[20];
        protected int fLastDepth;
        protected int fMark;
        protected int fPosition;

        public ElementStack() {
            int i = 0;
            while (true) {
                QName[] qNameArr = this.fElements;
                if (i < qNameArr.length) {
                    qNameArr[i] = new QName();
                    i++;
                } else {
                    return;
                }
            }
        }

        public QName pushElement(QName qName) {
            int i = this.fDepth;
            QName[] qNameArr = this.fElements;
            if (i == qNameArr.length) {
                QName[] qNameArr2 = new QName[(qNameArr.length * 2)];
                System.arraycopy(qNameArr, 0, qNameArr2, 0, i);
                this.fElements = qNameArr2;
                int i2 = this.fDepth;
                while (true) {
                    QName[] qNameArr3 = this.fElements;
                    if (i2 >= qNameArr3.length) {
                        break;
                    }
                    qNameArr3[i2] = new QName();
                    i2++;
                }
            }
            this.fElements[this.fDepth].setValues(qName);
            QName[] qNameArr4 = this.fElements;
            int i3 = this.fDepth;
            this.fDepth = i3 + 1;
            return qNameArr4[i3];
        }

        public QName getNext() {
            if (this.fPosition == this.fCount) {
                this.fPosition = this.fMark;
            }
            return this.fElements[this.fPosition];
        }

        public void push() {
            int[] iArr = this.fInt;
            int i = this.fDepth + 1;
            this.fDepth = i;
            int i2 = this.fPosition;
            this.fPosition = i2 + 1;
            iArr[i] = i2;
        }

        /* JADX WARNING: Removed duplicated region for block: B:10:0x0033  */
        /* JADX WARNING: Removed duplicated region for block: B:11:0x0040  */
        /* JADX WARNING: Removed duplicated region for block: B:14:0x0050  */
        /* JADX WARNING: Removed duplicated region for block: B:16:0x005a  */
        public boolean matchElement(QName qName) {
            boolean z;
            int i = this.fLastDepth;
            int i2 = this.fDepth;
            if (i > i2 && i2 <= 3) {
                if (qName.rawname == this.fElements[this.fDepth - 1].rawname) {
                    XMLDocumentFragmentScannerImpl.this.fAdd = false;
                    this.fMark = this.fDepth - 1;
                    this.fPosition = this.fMark;
                    this.fCount--;
                    z = true;
                    if (!z) {
                        int[] iArr = this.fInt;
                        int i3 = this.fDepth;
                        int i4 = this.fPosition;
                        this.fPosition = i4 + 1;
                        iArr[i3] = i4;
                    } else {
                        this.fInt[this.fDepth] = this.fCount - 1;
                    }
                    if (this.fCount != this.fElements.length) {
                        XMLDocumentFragmentScannerImpl xMLDocumentFragmentScannerImpl = XMLDocumentFragmentScannerImpl.this;
                        xMLDocumentFragmentScannerImpl.fSkip = false;
                        xMLDocumentFragmentScannerImpl.fAdd = false;
                        reposition();
                        return false;
                    }
                    this.fLastDepth = this.fDepth;
                    return z;
                }
                XMLDocumentFragmentScannerImpl.this.fAdd = true;
            }
            z = false;
            if (!z) {
            }
            if (this.fCount != this.fElements.length) {
            }
        }

        public QName nextElement() {
            if (XMLDocumentFragmentScannerImpl.this.fSkip) {
                this.fDepth++;
                QName[] qNameArr = this.fElements;
                int i = this.fCount;
                this.fCount = i + 1;
                return qNameArr[i];
            }
            int i2 = this.fDepth;
            QName[] qNameArr2 = this.fElements;
            if (i2 == qNameArr2.length) {
                QName[] qNameArr3 = new QName[(qNameArr2.length * 2)];
                System.arraycopy(qNameArr2, 0, qNameArr3, 0, i2);
                this.fElements = qNameArr3;
                int i3 = this.fDepth;
                while (true) {
                    QName[] qNameArr4 = this.fElements;
                    if (i3 >= qNameArr4.length) {
                        break;
                    }
                    qNameArr4[i3] = new QName();
                    i3++;
                }
            }
            QName[] qNameArr5 = this.fElements;
            int i4 = this.fDepth;
            this.fDepth = i4 + 1;
            return qNameArr5[i4];
        }

        public QName popElement() {
            if (XMLDocumentFragmentScannerImpl.this.fSkip || XMLDocumentFragmentScannerImpl.this.fAdd) {
                QName[] qNameArr = this.fElements;
                int[] iArr = this.fInt;
                int i = this.fDepth;
                this.fDepth = i - 1;
                return qNameArr[iArr[i]];
            }
            QName[] qNameArr2 = this.fElements;
            int i2 = this.fDepth - 1;
            this.fDepth = i2;
            return qNameArr2[i2];
        }

        public void reposition() {
            for (int i = 2; i <= this.fDepth; i++) {
                QName[] qNameArr = this.fElements;
                qNameArr[i - 1] = qNameArr[this.fInt[i]];
            }
        }

        public void clear() {
            this.fDepth = 0;
            this.fLastDepth = 0;
            this.fCount = 0;
            this.fMark = 1;
            this.fPosition = 1;
        }

        public QName getLastPoppedElement() {
            return this.fElements[this.fDepth];
        }
    }

    /* access modifiers changed from: protected */
    public class FragmentContentDriver implements Driver {
        /* access modifiers changed from: protected */
        public boolean elementDepthIsZeroHook() throws IOException, XNIException {
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean scanForDoctypeHook() throws IOException, XNIException {
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean scanRootElementHook() throws IOException, XNIException {
            return false;
        }

        protected FragmentContentDriver() {
        }

        private void startOfMarkup() throws IOException {
            XMLDocumentFragmentScannerImpl.this.fMarkupDepth++;
            int peekChar = XMLDocumentFragmentScannerImpl.this.fEntityScanner.peekChar();
            if (XMLDocumentFragmentScannerImpl.this.isValidNameStartChar(peekChar) || XMLDocumentFragmentScannerImpl.this.isValidNameStartHighSurrogate(peekChar)) {
                XMLDocumentFragmentScannerImpl.this.setScannerState(38);
            } else if (peekChar == 33) {
                XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(peekChar, null);
                if (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(45, null)) {
                    if (!XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(45, XMLScanner.NameType.COMMENT)) {
                        XMLDocumentFragmentScannerImpl.this.reportFatalError("InvalidCommentStart", null);
                    }
                    XMLDocumentFragmentScannerImpl.this.setScannerState(27);
                } else if (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipString(XMLDocumentFragmentScannerImpl.cdata)) {
                    XMLDocumentFragmentScannerImpl.this.setScannerState(35);
                } else if (!scanForDoctypeHook()) {
                    XMLDocumentFragmentScannerImpl.this.reportFatalError("MarkupNotRecognizedInContent", null);
                }
            } else if (peekChar == 47) {
                XMLDocumentFragmentScannerImpl.this.setScannerState(39);
                XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(peekChar, XMLScanner.NameType.ELEMENTEND);
            } else if (peekChar != 63) {
                XMLDocumentFragmentScannerImpl.this.reportFatalError("MarkupNotRecognizedInContent", null);
            } else {
                XMLDocumentFragmentScannerImpl.this.setScannerState(23);
                XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(peekChar, null);
            }
        }

        private void startOfContent() throws IOException {
            if (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(60, null)) {
                XMLDocumentFragmentScannerImpl.this.setScannerState(21);
            } else if (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(38, XMLScanner.NameType.REFERENCE)) {
                XMLDocumentFragmentScannerImpl.this.setScannerState(28);
            } else {
                XMLDocumentFragmentScannerImpl.this.setScannerState(37);
            }
        }

        public void decideSubState() throws IOException {
            while (true) {
                if (XMLDocumentFragmentScannerImpl.this.fScannerState == 22 || XMLDocumentFragmentScannerImpl.this.fScannerState == 21) {
                    int i = XMLDocumentFragmentScannerImpl.this.fScannerState;
                    if (i == 21) {
                        startOfMarkup();
                    } else if (i == 22) {
                        startOfContent();
                    }
                } else {
                    return;
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:104:0x0231 A[Catch:{ EOFException -> 0x0549 }] */
        /* JADX WARNING: Removed duplicated region for block: B:122:0x02c5 A[Catch:{ EOFException -> 0x0549 }] */
        /* JADX WARNING: Removed duplicated region for block: B:136:0x0329 A[Catch:{ EOFException -> 0x0549 }] */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0053 A[Catch:{ EOFException -> 0x0549 }] */
        /* JADX WARNING: Removed duplicated region for block: B:224:0x032f A[SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:228:0x0548 A[SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:252:0x02b8 A[SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:39:0x00a2 A[Catch:{ EOFException -> 0x0549 }] */
        /* JADX WARNING: Removed duplicated region for block: B:86:0x019d A[Catch:{ EOFException -> 0x0549 }] */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl.Driver
        public int next() throws IOException, XNIException {
            int i;
            boolean z;
            while (true) {
                try {
                    int i2 = XMLDocumentFragmentScannerImpl.this.fScannerState;
                    if (i2 != 21) {
                        if (i2 == 22) {
                            int peekChar = XMLDocumentFragmentScannerImpl.this.fEntityScanner.peekChar();
                            if (peekChar == 60) {
                                XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanChar(null);
                                XMLDocumentFragmentScannerImpl.this.setScannerState(21);
                            } else if (peekChar == 38) {
                                XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanChar(XMLScanner.NameType.REFERENCE);
                                XMLDocumentFragmentScannerImpl.this.setScannerState(28);
                            } else {
                                XMLDocumentFragmentScannerImpl.this.setScannerState(37);
                            }
                        }
                        if (XMLDocumentFragmentScannerImpl.this.fIsCoalesce) {
                            XMLDocumentFragmentScannerImpl.this.fUsebuffer = true;
                            if (XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData) {
                                if (!(XMLDocumentFragmentScannerImpl.this.fScannerState == 35 || XMLDocumentFragmentScannerImpl.this.fScannerState == 28 || XMLDocumentFragmentScannerImpl.this.fScannerState == 37)) {
                                    XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData = false;
                                    return 4;
                                }
                            } else if (!((!XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData && !XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference) || XMLDocumentFragmentScannerImpl.this.fScannerState == 35 || XMLDocumentFragmentScannerImpl.this.fScannerState == 28 || XMLDocumentFragmentScannerImpl.this.fScannerState == 37)) {
                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData = false;
                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference = false;
                                return 4;
                            }
                        }
                        i = XMLDocumentFragmentScannerImpl.this.fScannerState;
                        if (i != 7) {
                            return 7;
                        }
                        if (i != 23) {
                            switch (i) {
                                case 26:
                                    if (scanRootElementHook()) {
                                        XMLDocumentFragmentScannerImpl.this.fEmptyElement = true;
                                        return 1;
                                    }
                                    XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                                    return 1;
                                case 27:
                                    XMLDocumentFragmentScannerImpl.this.scanComment();
                                    XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                                    return 5;
                                case 28:
                                    XMLDocumentFragmentScannerImpl.this.fMarkupDepth++;
                                    XMLDocumentFragmentScannerImpl.this.foundBuiltInRefs = false;
                                    if (!XMLDocumentFragmentScannerImpl.this.fIsCoalesce || (!XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference && !XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData && !XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData)) {
                                        XMLDocumentFragmentScannerImpl.this.fContentBuffer.clear();
                                    } else {
                                        XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference = true;
                                        XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData = false;
                                        XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData = false;
                                    }
                                    XMLDocumentFragmentScannerImpl.this.fUsebuffer = true;
                                    if (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(35, XMLScanner.NameType.REFERENCE)) {
                                        XMLDocumentFragmentScannerImpl.this.scanCharReferenceValue(XMLDocumentFragmentScannerImpl.this.fContentBuffer, null);
                                        XMLDocumentFragmentScannerImpl.this.fMarkupDepth--;
                                        if (!XMLDocumentFragmentScannerImpl.this.fIsCoalesce) {
                                            XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                                            return 4;
                                        }
                                    } else {
                                        XMLDocumentFragmentScannerImpl.this.scanEntityReference(XMLDocumentFragmentScannerImpl.this.fContentBuffer);
                                        if (XMLDocumentFragmentScannerImpl.this.fScannerState != 41 || XMLDocumentFragmentScannerImpl.this.fIsCoalesce) {
                                            if (XMLDocumentFragmentScannerImpl.this.fScannerState != 36) {
                                                if (XMLDocumentFragmentScannerImpl.this.fScannerState == 28) {
                                                    XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                                                    if (XMLDocumentFragmentScannerImpl.this.fReplaceEntityReferences && XMLDocumentFragmentScannerImpl.this.fEntityStore.isDeclaredEntity(XMLDocumentFragmentScannerImpl.this.fCurrentEntityName)) {
                                                        break;
                                                    } else {
                                                        return 9;
                                                    }
                                                }
                                            } else {
                                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference = true;
                                                break;
                                            }
                                        } else {
                                            XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                                            return 4;
                                        }
                                    }
                                    XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                                    XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference = true;
                                    break;
                                default:
                                    switch (i) {
                                        case 35:
                                            if (!XMLDocumentFragmentScannerImpl.this.fIsCoalesce || (!XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference && !XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData && !XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData)) {
                                                XMLDocumentFragmentScannerImpl.this.fContentBuffer.clear();
                                            } else {
                                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData = true;
                                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference = false;
                                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData = false;
                                            }
                                            XMLDocumentFragmentScannerImpl.this.fUsebuffer = true;
                                            XMLDocumentFragmentScannerImpl.this.scanCDATASection(XMLDocumentFragmentScannerImpl.this.fContentBuffer, true);
                                            XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                                            if (XMLDocumentFragmentScannerImpl.this.fIsCoalesce) {
                                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData = true;
                                                continue;
                                            } else if (XMLDocumentFragmentScannerImpl.this.fReportCdataEvent) {
                                                return 12;
                                            } else {
                                                return 4;
                                            }
                                        case 36:
                                            if (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipString("<?xml")) {
                                                XMLDocumentFragmentScannerImpl.this.fMarkupDepth++;
                                                if (XMLDocumentFragmentScannerImpl.this.isValidNameChar(XMLDocumentFragmentScannerImpl.this.fEntityScanner.peekChar())) {
                                                    XMLDocumentFragmentScannerImpl.this.fStringBuffer.clear();
                                                    XMLDocumentFragmentScannerImpl.this.fStringBuffer.append("xml");
                                                    if (XMLDocumentFragmentScannerImpl.this.fNamespaces) {
                                                        while (XMLDocumentFragmentScannerImpl.this.isValidNCName(XMLDocumentFragmentScannerImpl.this.fEntityScanner.peekChar())) {
                                                            XMLDocumentFragmentScannerImpl.this.fStringBuffer.append((char) XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanChar(null));
                                                        }
                                                    } else {
                                                        while (XMLDocumentFragmentScannerImpl.this.isValidNameChar(XMLDocumentFragmentScannerImpl.this.fEntityScanner.peekChar())) {
                                                            XMLDocumentFragmentScannerImpl.this.fStringBuffer.append((char) XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanChar(null));
                                                        }
                                                    }
                                                    String addSymbol = XMLDocumentFragmentScannerImpl.this.fSymbolTable.addSymbol(XMLDocumentFragmentScannerImpl.this.fStringBuffer.ch, XMLDocumentFragmentScannerImpl.this.fStringBuffer.offset, XMLDocumentFragmentScannerImpl.this.fStringBuffer.length);
                                                    XMLDocumentFragmentScannerImpl.this.fContentBuffer.clear();
                                                    XMLDocumentFragmentScannerImpl.this.scanPIData(addSymbol, XMLDocumentFragmentScannerImpl.this.fContentBuffer);
                                                } else {
                                                    XMLDocumentFragmentScannerImpl.this.scanXMLDeclOrTextDecl(true);
                                                }
                                            }
                                            XMLDocumentFragmentScannerImpl.this.fEntityManager.fCurrentEntity.mayReadChunks = true;
                                            XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                                            continue;
                                        case 37:
                                            XMLDocumentFragmentScannerImpl xMLDocumentFragmentScannerImpl = XMLDocumentFragmentScannerImpl.this;
                                            if (!XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference && !XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData) {
                                                if (!XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData) {
                                                    z = false;
                                                    xMLDocumentFragmentScannerImpl.fUsebuffer = z;
                                                    if (XMLDocumentFragmentScannerImpl.this.fIsCoalesce || (!XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference && !XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData && !XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData)) {
                                                        XMLDocumentFragmentScannerImpl.this.fContentBuffer.clear();
                                                    } else {
                                                        XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference = false;
                                                        XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData = false;
                                                        XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData = true;
                                                        XMLDocumentFragmentScannerImpl.this.fUsebuffer = true;
                                                    }
                                                    XMLDocumentFragmentScannerImpl.this.fTempString.length = 0;
                                                    int scanContent = XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanContent(XMLDocumentFragmentScannerImpl.this.fTempString);
                                                    if (!XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(60, null)) {
                                                        if (!XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(47, XMLScanner.NameType.ELEMENTEND)) {
                                                            if (!XMLChar.isNameStart(XMLDocumentFragmentScannerImpl.this.fEntityScanner.peekChar())) {
                                                                XMLDocumentFragmentScannerImpl.this.setScannerState(21);
                                                                if (!XMLDocumentFragmentScannerImpl.this.fIsCoalesce) {
                                                                    break;
                                                                } else {
                                                                    XMLDocumentFragmentScannerImpl.this.fUsebuffer = true;
                                                                    XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData = true;
                                                                    XMLDocumentFragmentScannerImpl.this.fContentBuffer.append(XMLDocumentFragmentScannerImpl.this.fTempString);
                                                                    XMLDocumentFragmentScannerImpl.this.fTempString.length = 0;
                                                                    continue;
                                                                }
                                                            } else {
                                                                XMLDocumentFragmentScannerImpl.this.fMarkupDepth++;
                                                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData = false;
                                                                XMLDocumentFragmentScannerImpl.this.setScannerState(38);
                                                                break;
                                                            }
                                                        } else {
                                                            XMLDocumentFragmentScannerImpl.this.fMarkupDepth++;
                                                            XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData = false;
                                                            XMLDocumentFragmentScannerImpl.this.setScannerState(39);
                                                            break;
                                                        }
                                                    } else {
                                                        XMLDocumentFragmentScannerImpl.this.fUsebuffer = true;
                                                        XMLDocumentFragmentScannerImpl.this.fContentBuffer.append(XMLDocumentFragmentScannerImpl.this.fTempString);
                                                        XMLDocumentFragmentScannerImpl.this.fTempString.length = 0;
                                                        if (scanContent == 13) {
                                                            XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanChar(null);
                                                            XMLDocumentFragmentScannerImpl.this.fUsebuffer = true;
                                                            XMLDocumentFragmentScannerImpl.this.fContentBuffer.append((char) scanContent);
                                                        } else {
                                                            if (scanContent == 93) {
                                                                XMLDocumentFragmentScannerImpl.this.fUsebuffer = true;
                                                                XMLDocumentFragmentScannerImpl.this.fContentBuffer.append((char) XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanChar(null));
                                                                XMLDocumentFragmentScannerImpl.this.fInScanContent = true;
                                                                if (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(93, null)) {
                                                                    XMLDocumentFragmentScannerImpl.this.fContentBuffer.append(']');
                                                                    while (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(93, null)) {
                                                                        XMLDocumentFragmentScannerImpl.this.fContentBuffer.append(']');
                                                                    }
                                                                    if (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(62, null)) {
                                                                        XMLDocumentFragmentScannerImpl.this.reportFatalError("CDEndInContent", null);
                                                                    }
                                                                }
                                                                XMLDocumentFragmentScannerImpl.this.fInScanContent = false;
                                                            }
                                                            while (true) {
                                                                if (scanContent != 60) {
                                                                    XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanChar(null);
                                                                    XMLDocumentFragmentScannerImpl.this.setScannerState(21);
                                                                } else if (scanContent == 38) {
                                                                    XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanChar(XMLScanner.NameType.REFERENCE);
                                                                    XMLDocumentFragmentScannerImpl.this.setScannerState(28);
                                                                } else if (scanContent == -1 || !XMLDocumentFragmentScannerImpl.this.isInvalidLiteral(scanContent)) {
                                                                    scanContent = XMLDocumentFragmentScannerImpl.this.scanContent(XMLDocumentFragmentScannerImpl.this.fContentBuffer);
                                                                    if (!XMLDocumentFragmentScannerImpl.this.fIsCoalesce) {
                                                                        XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                                                                    }
                                                                } else if (XMLChar.isHighSurrogate(scanContent)) {
                                                                    XMLDocumentFragmentScannerImpl.this.scanSurrogates(XMLDocumentFragmentScannerImpl.this.fContentBuffer);
                                                                    XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                                                                } else {
                                                                    XMLDocumentFragmentScannerImpl.this.reportFatalError("InvalidCharInContent", new Object[]{Integer.toString(scanContent, 16)});
                                                                    XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanChar(null);
                                                                }
                                                            }
                                                            if (!XMLDocumentFragmentScannerImpl.this.fIsCoalesce) {
                                                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData = true;
                                                                break;
                                                            } else if (XMLDocumentFragmentScannerImpl.this.dtdGrammarUtil == null || !XMLDocumentFragmentScannerImpl.this.dtdGrammarUtil.isIgnorableWhiteSpace(XMLDocumentFragmentScannerImpl.this.fContentBuffer)) {
                                                                return 4;
                                                            } else {
                                                                return 6;
                                                            }
                                                        }
                                                        scanContent = -1;
                                                        while (true) {
                                                            if (scanContent != 60) {
                                                            }
                                                            

                                                            static void pr(String str) {
                                                                System.out.println(str);
                                                            }

                                                            /* access modifiers changed from: protected */
                                                            public XMLString getString() {
                                                                if (this.fAttributeCacheUsedCount < this.initialCacheCount || this.fAttributeCacheUsedCount < this.attributeValueCache.size()) {
                                                                    ArrayList arrayList = this.attributeValueCache;
                                                                    int i = this.fAttributeCacheUsedCount;
                                                                    this.fAttributeCacheUsedCount = i + 1;
                                                                    return (XMLString) arrayList.get(i);
                                                                }
                                                                XMLString xMLString = new XMLString();
                                                                this.fAttributeCacheUsedCount++;
                                                                this.attributeValueCache.add(xMLString);
                                                                return xMLString;
                                                            }

                                                            @Override // ohos.com.sun.xml.internal.stream.XMLBufferListener
                                                            public void refresh() {
                                                                refresh(0);
                                                            }

                                                            @Override // ohos.com.sun.xml.internal.stream.XMLBufferListener
                                                            public void refresh(int i) {
                                                                if (this.fReadingAttributes) {
                                                                    this.fAttributes.refresh();
                                                                }
                                                                if (this.fScannerState == 37) {
                                                                    this.fContentBuffer.append(this.fTempString);
                                                                    this.fTempString.length = 0;
                                                                    this.fUsebuffer = true;
                                                                }
                                                            }
                                                        }
