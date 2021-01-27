package ohos.com.sun.org.apache.xerces.internal.impl;

import java.io.EOFException;
import java.io.IOException;
import java.util.NoSuchElementException;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDDescription;
import ohos.com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl;
import ohos.com.sun.org.apache.xerces.internal.util.XMLStringBuffer;
import ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDScanner;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.com.sun.xml.internal.stream.Entity;
import ohos.com.sun.xml.internal.stream.StaxXMLInputSource;
import ohos.com.sun.xml.internal.stream.dtd.DTDGrammarUtil;

public class XMLDocumentScannerImpl extends XMLDocumentFragmentScannerImpl {
    private static final char[] COMMENTSTRING = {LocaleUtility.IETF_SEPARATOR, LocaleUtility.IETF_SEPARATOR};
    protected static final String DISALLOW_DOCTYPE_DECL_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
    private static final char[] DOCTYPE = {'D', 'O', 'C', 'T', 'Y', 'P', 'E'};
    protected static final String DOCUMENT_SCANNER = "http://apache.org/xml/properties/internal/document-scanner";
    protected static final String DTD_SCANNER = "http://apache.org/xml/properties/internal/dtd-scanner";
    private static final Boolean[] FEATURE_DEFAULTS = {Boolean.TRUE, Boolean.FALSE};
    protected static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    protected static final String NAMESPACE_CONTEXT = "http://apache.org/xml/properties/internal/namespace-context";
    private static final Object[] PROPERTY_DEFAULTS = {null, null};
    private static final String[] RECOGNIZED_FEATURES = {LOAD_EXTERNAL_DTD, DISALLOW_DOCTYPE_DECL_FEATURE};
    private static final String[] RECOGNIZED_PROPERTIES = {DTD_SCANNER, VALIDATION_MANAGER};
    protected static final int SCANNER_STATE_DTD_EXTERNAL = 46;
    protected static final int SCANNER_STATE_DTD_EXTERNAL_DECLS = 47;
    protected static final int SCANNER_STATE_DTD_INTERNAL_DECLS = 45;
    protected static final int SCANNER_STATE_NO_SUCH_ELEMENT_EXCEPTION = 48;
    protected static final int SCANNER_STATE_PROLOG = 43;
    protected static final int SCANNER_STATE_TRAILING_MISC = 44;
    protected static final int SCANNER_STATE_XML_DECL = 42;
    protected static final String VALIDATION_MANAGER = "http://apache.org/xml/properties/internal/validation-manager";
    protected boolean fAddedListener = false;
    protected XMLStringBuffer fDTDDecl = null;
    private final XMLDTDDescription fDTDDescription = new XMLDTDDescription(null, null, null, null, null);
    protected XMLDocumentFragmentScannerImpl.Driver fDTDDriver = null;
    protected XMLDTDScanner fDTDScanner = null;
    protected String fDoctypeName;
    protected String fDoctypePublicId;
    protected String fDoctypeSystemId;
    protected int fEndPos = 0;
    private XMLInputSource fExternalSubsetSource = null;
    protected boolean fLoadExternalDTD = true;
    protected NamespaceContext fNamespaceContext = new NamespaceSupport();
    protected XMLDocumentFragmentScannerImpl.Driver fPrologDriver = new PrologDriver();
    protected boolean fReadingDTD = false;
    protected boolean fScanEndElement;
    protected boolean fSeenDoctypeDecl;
    protected boolean fSeenInternalSubset = false;
    protected int fStartPos = 0;
    private String[] fStrings = new String[3];
    protected XMLDocumentFragmentScannerImpl.Driver fTrailingMiscDriver = new TrailingMiscDriver();
    protected ValidationManager fValidationManager;
    protected XMLDocumentFragmentScannerImpl.Driver fXMLDeclDriver = new XMLDeclDriver();

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentScanner
    public void setInputSource(XMLInputSource xMLInputSource) throws IOException {
        this.fEntityManager.setEntityHandler(this);
        this.fEntityManager.startDocumentEntity(xMLInputSource);
        setScannerState(7);
    }

    public int getScannetState() {
        return this.fScannerState;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl, ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner
    public void reset(PropertyManager propertyManager) {
        super.reset(propertyManager);
        this.fDoctypeName = null;
        this.fDoctypePublicId = null;
        this.fDoctypeSystemId = null;
        this.fSeenDoctypeDecl = false;
        this.fNamespaceContext.reset();
        this.fSupportDTD = ((Boolean) propertyManager.getProperty("javax.xml.stream.supportDTD")).booleanValue();
        this.fLoadExternalDTD = !((Boolean) propertyManager.getProperty("http://java.sun.com/xml/stream/properties/ignore-external-dtd")).booleanValue();
        setScannerState(7);
        setDriver(this.fXMLDeclDriver);
        this.fSeenInternalSubset = false;
        XMLDTDScanner xMLDTDScanner = this.fDTDScanner;
        if (xMLDTDScanner != null) {
            ((XMLDTDScannerImpl) xMLDTDScanner).reset(propertyManager);
        }
        this.fEndPos = 0;
        this.fStartPos = 0;
        XMLStringBuffer xMLStringBuffer = this.fDTDDecl;
        if (xMLStringBuffer != null) {
            xMLStringBuffer.clear();
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl, ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void reset(XMLComponentManager xMLComponentManager) throws XMLConfigurationException {
        super.reset(xMLComponentManager);
        this.fDoctypeName = null;
        this.fDoctypePublicId = null;
        this.fDoctypeSystemId = null;
        this.fSeenDoctypeDecl = false;
        this.fExternalSubsetSource = null;
        this.fLoadExternalDTD = xMLComponentManager.getFeature(LOAD_EXTERNAL_DTD, true);
        this.fDisallowDoctype = xMLComponentManager.getFeature(DISALLOW_DOCTYPE_DECL_FEATURE, false);
        this.fNamespaces = xMLComponentManager.getFeature("http://xml.org/sax/features/namespaces", true);
        this.fSeenInternalSubset = false;
        this.fDTDScanner = (XMLDTDScanner) xMLComponentManager.getProperty(DTD_SCANNER);
        this.fValidationManager = (ValidationManager) xMLComponentManager.getProperty(VALIDATION_MANAGER, null);
        try {
            this.fNamespaceContext = (NamespaceContext) xMLComponentManager.getProperty(NAMESPACE_CONTEXT);
        } catch (XMLConfigurationException unused) {
        }
        if (this.fNamespaceContext == null) {
            this.fNamespaceContext = new NamespaceSupport();
        }
        this.fNamespaceContext.reset();
        this.fEndPos = 0;
        this.fStartPos = 0;
        XMLStringBuffer xMLStringBuffer = this.fDTDDecl;
        if (xMLStringBuffer != null) {
            xMLStringBuffer.clear();
        }
        setScannerState(42);
        setDriver(this.fXMLDeclDriver);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public String[] getRecognizedFeatures() {
        String[] recognizedFeatures = super.getRecognizedFeatures();
        int length = recognizedFeatures != null ? recognizedFeatures.length : 0;
        String[] strArr = new String[(RECOGNIZED_FEATURES.length + length)];
        if (recognizedFeatures != null) {
            System.arraycopy(recognizedFeatures, 0, strArr, 0, recognizedFeatures.length);
        }
        String[] strArr2 = RECOGNIZED_FEATURES;
        System.arraycopy(strArr2, 0, strArr, length, strArr2.length);
        return strArr;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl, ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setFeature(String str, boolean z) throws XMLConfigurationException {
        super.setFeature(str, z);
        if (str.startsWith(Constants.XERCES_FEATURE_PREFIX)) {
            int length = str.length() - 31;
            if (length == 31 && str.endsWith(Constants.LOAD_EXTERNAL_DTD_FEATURE)) {
                this.fLoadExternalDTD = z;
            } else if (length == 21 && str.endsWith(Constants.DISALLOW_DOCTYPE_DECL_FEATURE)) {
                this.fDisallowDoctype = z;
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public String[] getRecognizedProperties() {
        String[] recognizedProperties = super.getRecognizedProperties();
        int length = recognizedProperties != null ? recognizedProperties.length : 0;
        String[] strArr = new String[(RECOGNIZED_PROPERTIES.length + length)];
        if (recognizedProperties != null) {
            System.arraycopy(recognizedProperties, 0, strArr, 0, recognizedProperties.length);
        }
        String[] strArr2 = RECOGNIZED_PROPERTIES;
        System.arraycopy(strArr2, 0, strArr, length, strArr2.length);
        return strArr;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl, ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setProperty(String str, Object obj) throws XMLConfigurationException {
        super.setProperty(str, obj);
        if (str.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            int length = str.length() - 33;
            if (length == 20 && str.endsWith(Constants.DTD_SCANNER_PROPERTY)) {
                this.fDTDScanner = (XMLDTDScanner) obj;
            }
            if (length == 26 && str.endsWith(Constants.NAMESPACE_CONTEXT_PROPERTY) && obj != null) {
                this.fNamespaceContext = (NamespaceContext) obj;
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public Boolean getFeatureDefault(String str) {
        int i = 0;
        while (true) {
            String[] strArr = RECOGNIZED_FEATURES;
            if (i >= strArr.length) {
                return super.getFeatureDefault(str);
            }
            if (strArr[i].equals(str)) {
                return FEATURE_DEFAULTS[i];
            }
            i++;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public Object getPropertyDefault(String str) {
        int i = 0;
        while (true) {
            String[] strArr = RECOGNIZED_PROPERTIES;
            if (i >= strArr.length) {
                return super.getPropertyDefault(str);
            }
            if (strArr[i].equals(str)) {
                return PROPERTY_DEFAULTS[i];
            }
            i++;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl, ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner, ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityHandler
    public void startEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
        super.startEntity(str, xMLResourceIdentifier, str2, augmentations);
        this.fEntityScanner.registerListener(this);
        if (!str.equals("[xml]") && this.fEntityScanner.isExternal() && (augmentations == null || !((Boolean) augmentations.getItem(Constants.ENTITY_SKIPPED)).booleanValue())) {
            setScannerState(36);
        }
        if (this.fDocumentHandler != null && str.equals("[xml]")) {
            this.fDocumentHandler.startDocument(this.fEntityScanner, str2, this.fNamespaceContext, null);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl, ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner, ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityHandler
    public void endEntity(String str, Augmentations augmentations) throws IOException, XNIException {
        super.endEntity(str, augmentations);
        if (!str.equals("[xml]")) {
            return;
        }
        if (this.fMarkupDepth == 0 && this.fDriver == this.fTrailingMiscDriver) {
            setScannerState(34);
            return;
        }
        throw new EOFException();
    }

    public XMLStringBuffer getDTDDecl() {
        Entity.ScannedEntity currentEntity = this.fEntityScanner.getCurrentEntity();
        XMLStringBuffer xMLStringBuffer = this.fDTDDecl;
        char[] cArr = currentEntity.ch;
        int i = this.fStartPos;
        xMLStringBuffer.append(cArr, i, this.fEndPos - i);
        if (this.fSeenInternalSubset) {
            this.fDTDDecl.append("]>");
        }
        return this.fDTDDecl;
    }

    public String getCharacterEncodingScheme() {
        return this.fDeclaredEncoding;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentScanner
    public int next() throws IOException, XNIException {
        return this.fDriver.next();
    }

    public NamespaceContext getNamespaceContext() {
        return this.fNamespaceContext;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl
    public XMLDocumentFragmentScannerImpl.Driver createContentDriver() {
        return new ContentDriver();
    }

    /* access modifiers changed from: protected */
    public boolean scanDoctypeDecl(boolean z) throws IOException, XNIException {
        if (!this.fEntityScanner.skipSpaces()) {
            reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ROOT_ELEMENT_TYPE_IN_DOCTYPEDECL", null);
        }
        this.fDoctypeName = this.fEntityScanner.scanName(XMLScanner.NameType.DOCTYPE);
        if (this.fDoctypeName == null) {
            reportFatalError("MSG_ROOT_ELEMENT_TYPE_REQUIRED", null);
        }
        if (this.fEntityScanner.skipSpaces()) {
            scanExternalID(this.fStrings, false);
            String[] strArr = this.fStrings;
            this.fDoctypeSystemId = strArr[0];
            this.fDoctypePublicId = strArr[1];
            this.fEntityScanner.skipSpaces();
        }
        this.fHasExternalDTD = this.fDoctypeSystemId != null;
        if (z && !this.fHasExternalDTD && this.fExternalSubsetResolver != null) {
            this.fDTDDescription.setValues(null, null, this.fEntityManager.getCurrentResourceIdentifier().getExpandedSystemId(), null);
            this.fDTDDescription.setRootName(this.fDoctypeName);
            this.fExternalSubsetSource = this.fExternalSubsetResolver.getExternalSubset(this.fDTDDescription);
            this.fHasExternalDTD = this.fExternalSubsetSource != null;
        }
        if (z && this.fDocumentHandler != null) {
            if (this.fExternalSubsetSource == null) {
                this.fDocumentHandler.doctypeDecl(this.fDoctypeName, this.fDoctypePublicId, this.fDoctypeSystemId, null);
            } else {
                this.fDocumentHandler.doctypeDecl(this.fDoctypeName, this.fExternalSubsetSource.getPublicId(), this.fExternalSubsetSource.getSystemId(), null);
            }
        }
        if (this.fEntityScanner.skipChar(91, null)) {
            return true;
        }
        this.fEntityScanner.skipSpaces();
        if (!this.fEntityScanner.skipChar(62, null)) {
            reportFatalError("DoctypedeclUnterminated", new Object[]{this.fDoctypeName});
        }
        this.fMarkupDepth--;
        return false;
    }

    /* access modifiers changed from: protected */
    public void setEndDTDScanState() {
        setScannerState(43);
        setDriver(this.fPrologDriver);
        this.fEntityManager.setEntityHandler(this);
        this.fReadingDTD = false;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl
    public String getScannerStateName(int i) {
        switch (i) {
            case 42:
                return "SCANNER_STATE_XML_DECL";
            case 43:
                return "SCANNER_STATE_PROLOG";
            case 44:
                return "SCANNER_STATE_TRAILING_MISC";
            case 45:
                return "SCANNER_STATE_DTD_INTERNAL_DECLS";
            case 46:
                return "SCANNER_STATE_DTD_EXTERNAL";
            case 47:
                return "SCANNER_STATE_DTD_EXTERNAL_DECLS";
            default:
                return super.getScannerStateName(i);
        }
    }

    protected final class XMLDeclDriver implements XMLDocumentFragmentScannerImpl.Driver {
        protected XMLDeclDriver() {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl.Driver
        public int next() throws IOException, XNIException {
            XMLDocumentScannerImpl.this.setScannerState(43);
            XMLDocumentScannerImpl xMLDocumentScannerImpl = XMLDocumentScannerImpl.this;
            xMLDocumentScannerImpl.setDriver(xMLDocumentScannerImpl.fPrologDriver);
            try {
                if (XMLDocumentScannerImpl.this.fEntityScanner.skipString(XMLDocumentFragmentScannerImpl.xmlDecl)) {
                    XMLDocumentScannerImpl.this.fMarkupDepth++;
                    if (XMLChar.isName(XMLDocumentScannerImpl.this.fEntityScanner.peekChar())) {
                        XMLDocumentScannerImpl.this.fStringBuffer.clear();
                        XMLDocumentScannerImpl.this.fStringBuffer.append("xml");
                        while (XMLChar.isName(XMLDocumentScannerImpl.this.fEntityScanner.peekChar())) {
                            XMLDocumentScannerImpl.this.fStringBuffer.append((char) XMLDocumentScannerImpl.this.fEntityScanner.scanChar(null));
                        }
                        String addSymbol = XMLDocumentScannerImpl.this.fSymbolTable.addSymbol(XMLDocumentScannerImpl.this.fStringBuffer.ch, XMLDocumentScannerImpl.this.fStringBuffer.offset, XMLDocumentScannerImpl.this.fStringBuffer.length);
                        XMLDocumentScannerImpl.this.fContentBuffer.clear();
                        XMLDocumentScannerImpl.this.scanPIData(addSymbol, XMLDocumentScannerImpl.this.fContentBuffer);
                        XMLDocumentScannerImpl.this.fEntityManager.fCurrentEntity.mayReadChunks = true;
                        return 3;
                    }
                    XMLDocumentScannerImpl.this.scanXMLDeclOrTextDecl(false);
                    XMLDocumentScannerImpl.this.fEntityManager.fCurrentEntity.mayReadChunks = true;
                    return 7;
                }
                XMLDocumentScannerImpl.this.fEntityManager.fCurrentEntity.mayReadChunks = true;
                return 7;
            } catch (EOFException unused) {
                XMLDocumentScannerImpl.this.reportFatalError("PrematureEOF", null);
                return -1;
            }
        }
    }

    protected final class PrologDriver implements XMLDocumentFragmentScannerImpl.Driver {
        protected PrologDriver() {
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl.Driver
        public int next() throws IOException, XNIException {
            while (true) {
                try {
                    int i = XMLDocumentScannerImpl.this.fScannerState;
                    if (i == 21) {
                        XMLDocumentScannerImpl.this.fMarkupDepth++;
                        if (XMLDocumentScannerImpl.this.isValidNameStartChar(XMLDocumentScannerImpl.this.fEntityScanner.peekChar())) {
                            break;
                        } else if (XMLDocumentScannerImpl.this.isValidNameStartHighSurrogate(XMLDocumentScannerImpl.this.fEntityScanner.peekChar())) {
                            break;
                        } else if (XMLDocumentScannerImpl.this.fEntityScanner.skipChar(33, null)) {
                            if (XMLDocumentScannerImpl.this.fEntityScanner.skipChar(45, null)) {
                                if (!XMLDocumentScannerImpl.this.fEntityScanner.skipChar(45, null)) {
                                    XMLDocumentScannerImpl.this.reportFatalError("InvalidCommentStart", null);
                                }
                                XMLDocumentScannerImpl.this.setScannerState(27);
                            } else if (XMLDocumentScannerImpl.this.fEntityScanner.skipString(XMLDocumentScannerImpl.DOCTYPE)) {
                                XMLDocumentScannerImpl.this.setScannerState(24);
                                Entity.ScannedEntity currentEntity = XMLDocumentScannerImpl.this.fEntityScanner.getCurrentEntity();
                                if (currentEntity instanceof Entity.ScannedEntity) {
                                    XMLDocumentScannerImpl.this.fStartPos = currentEntity.position;
                                }
                                XMLDocumentScannerImpl.this.fReadingDTD = true;
                                if (XMLDocumentScannerImpl.this.fDTDDecl == null) {
                                    XMLDocumentScannerImpl.this.fDTDDecl = new XMLStringBuffer();
                                }
                                XMLDocumentScannerImpl.this.fDTDDecl.append("<!DOCTYPE");
                            } else {
                                XMLDocumentScannerImpl.this.reportFatalError("MarkupNotRecognizedInProlog", null);
                            }
                        } else if (XMLDocumentScannerImpl.this.fEntityScanner.skipChar(63, null)) {
                            XMLDocumentScannerImpl.this.setScannerState(23);
                        } else {
                            XMLDocumentScannerImpl.this.reportFatalError("MarkupNotRecognizedInProlog", null);
                        }
                    } else if (i == 43) {
                        XMLDocumentScannerImpl.this.fEntityScanner.skipSpaces();
                        if (XMLDocumentScannerImpl.this.fEntityScanner.skipChar(60, null)) {
                            XMLDocumentScannerImpl.this.setScannerState(21);
                        } else if (XMLDocumentScannerImpl.this.fEntityScanner.skipChar(38, XMLScanner.NameType.REFERENCE)) {
                            XMLDocumentScannerImpl.this.setScannerState(28);
                        } else {
                            XMLDocumentScannerImpl.this.setScannerState(22);
                        }
                    }
                    if (XMLDocumentScannerImpl.this.fScannerState != 43 && XMLDocumentScannerImpl.this.fScannerState != 21) {
                        switch (XMLDocumentScannerImpl.this.fScannerState) {
                            case 22:
                                XMLDocumentScannerImpl.this.reportFatalError("ContentIllegalInProlog", null);
                                XMLDocumentScannerImpl.this.fEntityScanner.scanChar(null);
                                XMLDocumentScannerImpl.this.reportFatalError("ReferenceIllegalInProlog", null);
                                break;
                            case 23:
                                XMLDocumentScannerImpl.this.fContentBuffer.clear();
                                XMLDocumentScannerImpl.this.scanPI(XMLDocumentScannerImpl.this.fContentBuffer);
                                XMLDocumentScannerImpl.this.setScannerState(43);
                                return 3;
                            case 24:
                                if (XMLDocumentScannerImpl.this.fDisallowDoctype) {
                                    XMLDocumentScannerImpl.this.reportFatalError("DoctypeNotAllowed", null);
                                }
                                if (XMLDocumentScannerImpl.this.fSeenDoctypeDecl) {
                                    XMLDocumentScannerImpl.this.reportFatalError("AlreadySeenDoctype", null);
                                }
                                XMLDocumentScannerImpl.this.fSeenDoctypeDecl = true;
                                if (XMLDocumentScannerImpl.this.scanDoctypeDecl(XMLDocumentScannerImpl.this.fSupportDTD)) {
                                    XMLDocumentScannerImpl.this.setScannerState(45);
                                    XMLDocumentScannerImpl.this.fSeenInternalSubset = true;
                                    if (XMLDocumentScannerImpl.this.fDTDDriver == null) {
                                        XMLDocumentScannerImpl.this.fDTDDriver = new DTDDriver();
                                    }
                                    XMLDocumentScannerImpl.this.setDriver(XMLDocumentScannerImpl.this.fContentDriver);
                                    return XMLDocumentScannerImpl.this.fDTDDriver.next();
                                }
                                if (XMLDocumentScannerImpl.this.fSeenDoctypeDecl) {
                                    Entity.ScannedEntity currentEntity2 = XMLDocumentScannerImpl.this.fEntityScanner.getCurrentEntity();
                                    if (currentEntity2 instanceof Entity.ScannedEntity) {
                                        XMLDocumentScannerImpl.this.fEndPos = currentEntity2.position;
                                    }
                                    XMLDocumentScannerImpl.this.fReadingDTD = false;
                                }
                                if (XMLDocumentScannerImpl.this.fDoctypeSystemId != null) {
                                    if ((XMLDocumentScannerImpl.this.fValidation || XMLDocumentScannerImpl.this.fLoadExternalDTD) && (XMLDocumentScannerImpl.this.fValidationManager == null || !XMLDocumentScannerImpl.this.fValidationManager.isCachedDTD())) {
                                        if (XMLDocumentScannerImpl.this.fSupportDTD) {
                                            XMLDocumentScannerImpl.this.setScannerState(46);
                                        } else {
                                            XMLDocumentScannerImpl.this.setScannerState(43);
                                        }
                                        XMLDocumentScannerImpl.this.setDriver(XMLDocumentScannerImpl.this.fContentDriver);
                                        if (XMLDocumentScannerImpl.this.fDTDDriver == null) {
                                            XMLDocumentScannerImpl.this.fDTDDriver = new DTDDriver();
                                        }
                                        return XMLDocumentScannerImpl.this.fDTDDriver.next();
                                    }
                                } else if (XMLDocumentScannerImpl.this.fExternalSubsetSource != null && ((XMLDocumentScannerImpl.this.fValidation || XMLDocumentScannerImpl.this.fLoadExternalDTD) && (XMLDocumentScannerImpl.this.fValidationManager == null || !XMLDocumentScannerImpl.this.fValidationManager.isCachedDTD()))) {
                                    XMLDocumentScannerImpl.this.fDTDScanner.setInputSource(XMLDocumentScannerImpl.this.fExternalSubsetSource);
                                    XMLDocumentScannerImpl.this.fExternalSubsetSource = null;
                                    if (XMLDocumentScannerImpl.this.fSupportDTD) {
                                        XMLDocumentScannerImpl.this.setScannerState(47);
                                    } else {
                                        XMLDocumentScannerImpl.this.setScannerState(43);
                                    }
                                    XMLDocumentScannerImpl.this.setDriver(XMLDocumentScannerImpl.this.fContentDriver);
                                    if (XMLDocumentScannerImpl.this.fDTDDriver == null) {
                                        XMLDocumentScannerImpl.this.fDTDDriver = new DTDDriver();
                                    }
                                    return XMLDocumentScannerImpl.this.fDTDDriver.next();
                                }
                                if (XMLDocumentScannerImpl.this.fDTDScanner != null) {
                                    XMLDocumentScannerImpl.this.fDTDScanner.setInputSource(null);
                                }
                                XMLDocumentScannerImpl.this.setScannerState(43);
                                return 11;
                            case 27:
                                XMLDocumentScannerImpl.this.scanComment();
                                XMLDocumentScannerImpl.this.setScannerState(43);
                                return 5;
                            case 28:
                                XMLDocumentScannerImpl.this.reportFatalError("ReferenceIllegalInProlog", null);
                                break;
                        }
                        return -1;
                    }
                } catch (EOFException unused) {
                    XMLDocumentScannerImpl.this.reportFatalError("PrematureEOF", null);
                    return -1;
                }
            }
            XMLDocumentScannerImpl.this.setScannerState(26);
            XMLDocumentScannerImpl.this.setDriver(XMLDocumentScannerImpl.this.fContentDriver);
            return XMLDocumentScannerImpl.this.fContentDriver.next();
        }
    }

    protected final class DTDDriver implements XMLDocumentFragmentScannerImpl.Driver {
        protected DTDDriver() {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl.Driver
        public int next() throws IOException, XNIException {
            dispatch(true);
            if (XMLDocumentScannerImpl.this.fPropertyManager == null) {
                return 11;
            }
            XMLDocumentScannerImpl xMLDocumentScannerImpl = XMLDocumentScannerImpl.this;
            xMLDocumentScannerImpl.dtdGrammarUtil = new DTDGrammarUtil(((XMLDTDScannerImpl) xMLDocumentScannerImpl.fDTDScanner).getGrammar(), XMLDocumentScannerImpl.this.fSymbolTable, XMLDocumentScannerImpl.this.fNamespaceContext);
            return 11;
        }

        public boolean dispatch(boolean z) throws IOException, XNIException {
            boolean z2;
            boolean z3;
            String checkAccess;
            XMLDocumentScannerImpl.this.fEntityManager.setEntityHandler(null);
            try {
                XMLResourceIdentifierImpl xMLResourceIdentifierImpl = new XMLResourceIdentifierImpl();
                if (XMLDocumentScannerImpl.this.fDTDScanner == null) {
                    if (XMLDocumentScannerImpl.this.fEntityManager.getEntityScanner() instanceof XML11EntityScanner) {
                        XMLDocumentScannerImpl.this.fDTDScanner = new XML11DTDScannerImpl();
                    } else {
                        XMLDocumentScannerImpl.this.fDTDScanner = new XMLDTDScannerImpl();
                    }
                    ((XMLDTDScannerImpl) XMLDocumentScannerImpl.this.fDTDScanner).reset(XMLDocumentScannerImpl.this.fPropertyManager);
                }
                XMLDocumentScannerImpl.this.fDTDScanner.setLimitAnalyzer(XMLDocumentScannerImpl.this.fLimitAnalyzer);
                while (true) {
                    switch (XMLDocumentScannerImpl.this.fScannerState) {
                        case 43:
                            XMLDocumentScannerImpl.this.setEndDTDScanState();
                            break;
                        case 44:
                        default:
                            throw new XNIException("DTDDriver#dispatch: scanner state=" + XMLDocumentScannerImpl.this.fScannerState + " (" + XMLDocumentScannerImpl.this.getScannerStateName(XMLDocumentScannerImpl.this.fScannerState) + ')');
                        case 45:
                            if (!XMLDocumentScannerImpl.this.fDTDScanner.skipDTD(XMLDocumentScannerImpl.this.fSupportDTD)) {
                                z3 = XMLDocumentScannerImpl.this.fDTDScanner.scanDTDInternalSubset(true, XMLDocumentScannerImpl.this.fStandalone, XMLDocumentScannerImpl.this.fHasExternalDTD && XMLDocumentScannerImpl.this.fLoadExternalDTD);
                            } else {
                                z3 = false;
                            }
                            Entity.ScannedEntity currentEntity = XMLDocumentScannerImpl.this.fEntityScanner.getCurrentEntity();
                            if (currentEntity instanceof Entity.ScannedEntity) {
                                XMLDocumentScannerImpl.this.fEndPos = currentEntity.position;
                            }
                            XMLDocumentScannerImpl.this.fReadingDTD = false;
                            if (!z3) {
                                if (!XMLDocumentScannerImpl.this.fEntityScanner.skipChar(93, null)) {
                                    XMLDocumentScannerImpl.this.reportFatalError("DoctypedeclNotClosed", new Object[]{XMLDocumentScannerImpl.this.fDoctypeName});
                                }
                                XMLDocumentScannerImpl.this.fEntityScanner.skipSpaces();
                                if (!XMLDocumentScannerImpl.this.fEntityScanner.skipChar(62, null)) {
                                    XMLDocumentScannerImpl.this.reportFatalError("DoctypedeclUnterminated", new Object[]{XMLDocumentScannerImpl.this.fDoctypeName});
                                }
                                XMLDocumentScannerImpl.this.fMarkupDepth--;
                                if (!XMLDocumentScannerImpl.this.fSupportDTD) {
                                    XMLDocumentScannerImpl.this.fEntityStore = XMLDocumentScannerImpl.this.fEntityManager.getEntityStore();
                                    XMLDocumentScannerImpl.this.fEntityStore.reset();
                                    break;
                                } else if (XMLDocumentScannerImpl.this.fDoctypeSystemId != null && (XMLDocumentScannerImpl.this.fValidation || XMLDocumentScannerImpl.this.fLoadExternalDTD)) {
                                    XMLDocumentScannerImpl.this.setScannerState(46);
                                }
                            }
                            z2 = false;
                            break;
                        case 46:
                            xMLResourceIdentifierImpl.setValues(XMLDocumentScannerImpl.this.fDoctypePublicId, XMLDocumentScannerImpl.this.fDoctypeSystemId, null, null);
                            StaxXMLInputSource resolveEntityAsPerStax = XMLDocumentScannerImpl.this.fEntityManager.resolveEntityAsPerStax(xMLResourceIdentifierImpl);
                            if (!resolveEntityAsPerStax.hasResolver() && (checkAccess = XMLDocumentScannerImpl.this.checkAccess(XMLDocumentScannerImpl.this.fDoctypeSystemId, XMLDocumentScannerImpl.this.fAccessExternalDTD)) != null) {
                                XMLDocumentScannerImpl.this.reportFatalError("AccessExternalDTD", new Object[]{SecuritySupport.sanitizePath(XMLDocumentScannerImpl.this.fDoctypeSystemId), checkAccess});
                            }
                            XMLDocumentScannerImpl.this.fDTDScanner.setInputSource(resolveEntityAsPerStax.getXMLInputSource());
                            if (XMLDocumentScannerImpl.this.fEntityScanner.fCurrentEntity != null) {
                                XMLDocumentScannerImpl.this.setScannerState(47);
                            } else {
                                XMLDocumentScannerImpl.this.setScannerState(43);
                            }
                            z2 = true;
                            break;
                        case 47:
                            if (!XMLDocumentScannerImpl.this.fDTDScanner.scanDTDExternalSubset(true)) {
                                XMLDocumentScannerImpl.this.setEndDTDScanState();
                                break;
                            }
                            z2 = false;
                            break;
                    }
                    if (!z && !z2) {
                        return true;
                    }
                }
                XMLDocumentScannerImpl.this.setEndDTDScanState();
                XMLDocumentScannerImpl.this.fEntityManager.setEntityHandler(XMLDocumentScannerImpl.this);
                return true;
            } catch (EOFException e) {
                e.printStackTrace();
                XMLDocumentScannerImpl.this.reportFatalError("PrematureEOF", null);
                return false;
            } finally {
                XMLDocumentScannerImpl.this.fEntityManager.setEntityHandler(XMLDocumentScannerImpl.this);
            }
        }
    }

    protected class ContentDriver extends XMLDocumentFragmentScannerImpl.FragmentContentDriver {
        protected ContentDriver() {
            super();
        }

        /* access modifiers changed from: protected */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl.FragmentContentDriver
        public boolean scanForDoctypeHook() throws IOException, XNIException {
            if (!XMLDocumentScannerImpl.this.fEntityScanner.skipString(XMLDocumentScannerImpl.DOCTYPE)) {
                return false;
            }
            XMLDocumentScannerImpl.this.setScannerState(24);
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl.FragmentContentDriver
        public boolean elementDepthIsZeroHook() throws IOException, XNIException {
            XMLDocumentScannerImpl.this.setScannerState(44);
            XMLDocumentScannerImpl xMLDocumentScannerImpl = XMLDocumentScannerImpl.this;
            xMLDocumentScannerImpl.setDriver(xMLDocumentScannerImpl.fTrailingMiscDriver);
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl.FragmentContentDriver
        public boolean scanRootElementHook() throws IOException, XNIException {
            if (!XMLDocumentScannerImpl.this.scanStartElement()) {
                return false;
            }
            XMLDocumentScannerImpl.this.setScannerState(44);
            XMLDocumentScannerImpl xMLDocumentScannerImpl = XMLDocumentScannerImpl.this;
            xMLDocumentScannerImpl.setDriver(xMLDocumentScannerImpl.fTrailingMiscDriver);
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl.FragmentContentDriver
        public void endOfFileHook(EOFException eOFException) throws IOException, XNIException {
            XMLDocumentScannerImpl.this.reportFatalError("PrematureEOF", null);
        }

        /* access modifiers changed from: protected */
        public void resolveExternalSubsetAndRead() throws IOException, XNIException {
            XMLDocumentScannerImpl.this.fDTDDescription.setValues(null, null, XMLDocumentScannerImpl.this.fEntityManager.getCurrentResourceIdentifier().getExpandedSystemId(), null);
            XMLDocumentScannerImpl.this.fDTDDescription.setRootName(XMLDocumentScannerImpl.this.fElementQName.rawname);
            XMLInputSource externalSubset = XMLDocumentScannerImpl.this.fExternalSubsetResolver.getExternalSubset(XMLDocumentScannerImpl.this.fDTDDescription);
            if (externalSubset != null) {
                XMLDocumentScannerImpl xMLDocumentScannerImpl = XMLDocumentScannerImpl.this;
                xMLDocumentScannerImpl.fDoctypeName = xMLDocumentScannerImpl.fElementQName.rawname;
                XMLDocumentScannerImpl.this.fDoctypePublicId = externalSubset.getPublicId();
                XMLDocumentScannerImpl.this.fDoctypeSystemId = externalSubset.getSystemId();
                if (XMLDocumentScannerImpl.this.fDocumentHandler != null) {
                    XMLDocumentScannerImpl.this.fDocumentHandler.doctypeDecl(XMLDocumentScannerImpl.this.fDoctypeName, XMLDocumentScannerImpl.this.fDoctypePublicId, XMLDocumentScannerImpl.this.fDoctypeSystemId, null);
                }
                try {
                    XMLDocumentScannerImpl.this.fDTDScanner.setInputSource(externalSubset);
                    do {
                    } while (XMLDocumentScannerImpl.this.fDTDScanner.scanDTDExternalSubset(true));
                } finally {
                    XMLDocumentScannerImpl.this.fEntityManager.setEntityHandler(XMLDocumentScannerImpl.this);
                }
            }
        }
    }

    protected final class TrailingMiscDriver implements XMLDocumentFragmentScannerImpl.Driver {
        protected TrailingMiscDriver() {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl.Driver
        public int next() throws IOException, XNIException {
            if (XMLDocumentScannerImpl.this.fEmptyElement) {
                XMLDocumentScannerImpl.this.fEmptyElement = false;
                return 2;
            }
            try {
                if (XMLDocumentScannerImpl.this.fScannerState == 34) {
                    return 8;
                }
                while (true) {
                    int i = XMLDocumentScannerImpl.this.fScannerState;
                    if (i == 21) {
                        XMLDocumentScannerImpl.this.fMarkupDepth++;
                        if (XMLDocumentScannerImpl.this.fEntityScanner.skipChar(63, null)) {
                            XMLDocumentScannerImpl.this.setScannerState(23);
                        } else if (XMLDocumentScannerImpl.this.fEntityScanner.skipChar(33, null)) {
                            XMLDocumentScannerImpl.this.setScannerState(27);
                        } else if (XMLDocumentScannerImpl.this.fEntityScanner.skipChar(47, null)) {
                            XMLDocumentScannerImpl.this.reportFatalError("MarkupNotRecognizedInMisc", null);
                        } else if (XMLDocumentScannerImpl.this.isValidNameStartChar(XMLDocumentScannerImpl.this.fEntityScanner.peekChar()) || XMLDocumentScannerImpl.this.isValidNameStartHighSurrogate(XMLDocumentScannerImpl.this.fEntityScanner.peekChar())) {
                            XMLDocumentScannerImpl.this.reportFatalError("MarkupNotRecognizedInMisc", null);
                            XMLDocumentScannerImpl.this.scanStartElement();
                            XMLDocumentScannerImpl.this.setScannerState(22);
                        } else {
                            XMLDocumentScannerImpl.this.reportFatalError("MarkupNotRecognizedInMisc", null);
                        }
                    } else if (i == 44) {
                        XMLDocumentScannerImpl.this.fEntityScanner.skipSpaces();
                        if (XMLDocumentScannerImpl.this.fScannerState == 34) {
                            return 8;
                        }
                        if (XMLDocumentScannerImpl.this.fEntityScanner.skipChar(60, null)) {
                            XMLDocumentScannerImpl.this.setScannerState(21);
                        } else {
                            XMLDocumentScannerImpl.this.setScannerState(22);
                        }
                    }
                    if (XMLDocumentScannerImpl.this.fScannerState != 21 && XMLDocumentScannerImpl.this.fScannerState != 44) {
                        int i2 = XMLDocumentScannerImpl.this.fScannerState;
                        if (i2 != 22) {
                            if (i2 == 23) {
                                XMLDocumentScannerImpl.this.fContentBuffer.clear();
                                XMLDocumentScannerImpl.this.scanPI(XMLDocumentScannerImpl.this.fContentBuffer);
                                XMLDocumentScannerImpl.this.setScannerState(44);
                                return 3;
                            } else if (i2 == 27) {
                                if (!XMLDocumentScannerImpl.this.fEntityScanner.skipString(XMLDocumentScannerImpl.COMMENTSTRING)) {
                                    XMLDocumentScannerImpl.this.reportFatalError("InvalidCommentStart", null);
                                }
                                XMLDocumentScannerImpl.this.scanComment();
                                XMLDocumentScannerImpl.this.setScannerState(44);
                                return 5;
                            } else if (i2 == 28) {
                                XMLDocumentScannerImpl.this.reportFatalError("ReferenceIllegalInTrailingMisc", null);
                                XMLDocumentScannerImpl.this.setScannerState(44);
                                return 9;
                            } else if (i2 == 34) {
                                XMLDocumentScannerImpl.this.setScannerState(48);
                                return 8;
                            } else if (i2 != 48) {
                                throw new XNIException("Scanner State " + XMLDocumentScannerImpl.this.fScannerState + " not Recognized ");
                            } else {
                                throw new NoSuchElementException("No more events to be parsed");
                            }
                        } else if (XMLDocumentScannerImpl.this.fEntityScanner.peekChar() == -1) {
                            XMLDocumentScannerImpl.this.setScannerState(34);
                            return 8;
                        } else {
                            XMLDocumentScannerImpl.this.reportFatalError("ContentIllegalInTrailingMisc", null);
                            XMLDocumentScannerImpl.this.fEntityScanner.scanChar(null);
                            XMLDocumentScannerImpl.this.setScannerState(44);
                            return 4;
                        }
                    }
                }
            } catch (EOFException unused) {
                if (XMLDocumentScannerImpl.this.fMarkupDepth != 0) {
                    XMLDocumentScannerImpl.this.reportFatalError("PrematureEOF", null);
                    return -1;
                }
                XMLDocumentScannerImpl.this.setScannerState(34);
                return 8;
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl, ohos.com.sun.xml.internal.stream.XMLBufferListener
    public void refresh(int i) {
        super.refresh(i);
        if (this.fReadingDTD) {
            Entity.ScannedEntity currentEntity = this.fEntityScanner.getCurrentEntity();
            if (currentEntity instanceof Entity.ScannedEntity) {
                this.fEndPos = currentEntity.position;
            }
            XMLStringBuffer xMLStringBuffer = this.fDTDDecl;
            char[] cArr = currentEntity.ch;
            int i2 = this.fStartPos;
            xMLStringBuffer.append(cArr, i2, this.fEndPos - i2);
            this.fStartPos = i;
        }
    }
}
