package ohos.com.sun.org.apache.xerces.internal.impl.dtd;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.RevalidationHandler;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.DTDDVFactory;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import ohos.com.sun.org.apache.xerces.internal.impl.validation.ValidationState;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.jaxp.JAXPConstants;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;

public class XMLDTDValidator implements XMLComponent, XMLDocumentFilter, XMLDTDValidatorFilter, RevalidationHandler {
    protected static final String BALANCE_SYNTAX_TREES = "http://apache.org/xml/features/validation/balance-syntax-trees";
    protected static final String DATATYPE_VALIDATOR_FACTORY = "http://apache.org/xml/properties/internal/datatype-validator-factory";
    private static final boolean DEBUG_ATTRIBUTES = false;
    private static final boolean DEBUG_ELEMENT_CHILDREN = false;
    protected static final String DYNAMIC_VALIDATION = "http://apache.org/xml/features/validation/dynamic";
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    private static final Boolean[] FEATURE_DEFAULTS = {null, null, Boolean.FALSE, Boolean.FALSE};
    protected static final String GRAMMAR_POOL = "http://apache.org/xml/properties/internal/grammar-pool";
    protected static final String NAMESPACES = "http://xml.org/sax/features/namespaces";
    protected static final String PARSER_SETTINGS = "http://apache.org/xml/features/internal/parser-settings";
    private static final Object[] PROPERTY_DEFAULTS = {null, null, null, null, null};
    private static final String[] RECOGNIZED_FEATURES = {"http://xml.org/sax/features/namespaces", VALIDATION, DYNAMIC_VALIDATION, BALANCE_SYNTAX_TREES};
    private static final String[] RECOGNIZED_PROPERTIES = {"http://apache.org/xml/properties/internal/symbol-table", "http://apache.org/xml/properties/internal/error-reporter", "http://apache.org/xml/properties/internal/grammar-pool", DATATYPE_VALIDATOR_FACTORY, VALIDATION_MANAGER};
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    private static final int TOP_LEVEL_SCOPE = -1;
    protected static final String VALIDATION = "http://xml.org/sax/features/validation";
    protected static final String VALIDATION_MANAGER = "http://apache.org/xml/properties/internal/validation-manager";
    protected static final String WARN_ON_DUPLICATE_ATTDEF = "http://apache.org/xml/features/validation/warn-on-duplicate-attdef";
    protected boolean fBalanceSyntaxTrees;
    private final StringBuffer fBuffer;
    private int[] fContentSpecTypeStack;
    private int fCurrentContentSpecType;
    private final QName fCurrentElement;
    private int fCurrentElementIndex;
    protected DTDGrammar fDTDGrammar;
    protected boolean fDTDValidation;
    protected DTDDVFactory fDatatypeValidatorFactory;
    protected XMLLocator fDocLocation;
    protected XMLDocumentHandler fDocumentHandler;
    protected XMLDocumentSource fDocumentSource;
    protected boolean fDynamicValidation;
    private QName[] fElementChildren;
    private int fElementChildrenLength;
    private int[] fElementChildrenOffsetStack;
    private int fElementDepth;
    private int[] fElementIndexStack;
    private QName[] fElementQNamePartsStack;
    private final XMLEntityDecl fEntityDecl;
    protected XMLErrorReporter fErrorReporter;
    protected DTDGrammarBucket fGrammarBucket;
    protected XMLGrammarPool fGrammarPool;
    private boolean fInCDATASection;
    private boolean fInElementContent;
    protected NamespaceContext fNamespaceContext = null;
    protected boolean fNamespaces;
    private boolean fPerformValidation;
    private final QName fRootElement;
    private String fSchemaType;
    protected boolean fSeenDoctypeDecl;
    private boolean fSeenRootElement;
    protected SymbolTable fSymbolTable;
    private final XMLAttributeDecl fTempAttDecl;
    private XMLElementDecl fTempElementDecl;
    private final QName fTempQName;
    protected DatatypeValidator fValENTITIES;
    protected DatatypeValidator fValENTITY;
    protected DatatypeValidator fValID;
    protected DatatypeValidator fValIDRef;
    protected DatatypeValidator fValIDRefs;
    protected DatatypeValidator fValNMTOKEN;
    protected DatatypeValidator fValNMTOKENS;
    protected DatatypeValidator fValNOTATION;
    protected boolean fValidation;
    protected ValidationManager fValidationManager = null;
    protected final ValidationState fValidationState = new ValidationState();
    protected boolean fWarnDuplicateAttdef;

    /* access modifiers changed from: protected */
    public boolean invalidStandaloneAttDef(QName qName, QName qName2) {
        return true;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setFeature(String str, boolean z) throws XMLConfigurationException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setProperty(String str, Object obj) throws XMLConfigurationException {
    }

    /* access modifiers changed from: protected */
    public void startNamespaceScope(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) {
    }

    public XMLDTDValidator() {
        int i = 0;
        this.fSeenDoctypeDecl = false;
        this.fCurrentElement = new QName();
        this.fCurrentElementIndex = -1;
        this.fCurrentContentSpecType = -1;
        this.fRootElement = new QName();
        this.fInCDATASection = false;
        this.fElementIndexStack = new int[8];
        this.fContentSpecTypeStack = new int[8];
        this.fElementQNamePartsStack = new QName[8];
        this.fElementChildren = new QName[32];
        this.fElementChildrenLength = 0;
        this.fElementChildrenOffsetStack = new int[32];
        this.fElementDepth = -1;
        this.fSeenRootElement = false;
        this.fInElementContent = false;
        this.fTempElementDecl = new XMLElementDecl();
        this.fTempAttDecl = new XMLAttributeDecl();
        this.fEntityDecl = new XMLEntityDecl();
        this.fTempQName = new QName();
        this.fBuffer = new StringBuffer();
        while (true) {
            QName[] qNameArr = this.fElementQNamePartsStack;
            if (i < qNameArr.length) {
                qNameArr[i] = new QName();
                i++;
            } else {
                this.fGrammarBucket = new DTDGrammarBucket();
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public DTDGrammarBucket getGrammarBucket() {
        return this.fGrammarBucket;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void reset(XMLComponentManager xMLComponentManager) throws XMLConfigurationException {
        this.fDTDGrammar = null;
        this.fSeenDoctypeDecl = false;
        this.fInCDATASection = false;
        this.fSeenRootElement = false;
        this.fInElementContent = false;
        this.fCurrentElementIndex = -1;
        this.fCurrentContentSpecType = -1;
        this.fRootElement.clear();
        this.fValidationState.resetIDTables();
        this.fGrammarBucket.clear();
        this.fElementDepth = -1;
        this.fElementChildrenLength = 0;
        if (!xMLComponentManager.getFeature(PARSER_SETTINGS, true)) {
            this.fValidationManager.addValidationState(this.fValidationState);
            return;
        }
        this.fNamespaces = xMLComponentManager.getFeature("http://xml.org/sax/features/namespaces", true);
        this.fValidation = xMLComponentManager.getFeature(VALIDATION, false);
        this.fDTDValidation = true ^ xMLComponentManager.getFeature("http://apache.org/xml/features/validation/schema", false);
        this.fDynamicValidation = xMLComponentManager.getFeature(DYNAMIC_VALIDATION, false);
        this.fBalanceSyntaxTrees = xMLComponentManager.getFeature(BALANCE_SYNTAX_TREES, false);
        this.fWarnDuplicateAttdef = xMLComponentManager.getFeature(WARN_ON_DUPLICATE_ATTDEF, false);
        this.fSchemaType = (String) xMLComponentManager.getProperty(JAXPConstants.JAXP_SCHEMA_LANGUAGE, null);
        this.fValidationManager = (ValidationManager) xMLComponentManager.getProperty(VALIDATION_MANAGER);
        this.fValidationManager.addValidationState(this.fValidationState);
        this.fValidationState.setUsingNamespaces(this.fNamespaces);
        this.fErrorReporter = (XMLErrorReporter) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/error-reporter");
        this.fSymbolTable = (SymbolTable) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/symbol-table");
        this.fGrammarPool = (XMLGrammarPool) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/grammar-pool", null);
        this.fDatatypeValidatorFactory = (DTDDVFactory) xMLComponentManager.getProperty(DATATYPE_VALIDATOR_FACTORY);
        init();
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

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource
    public void setDocumentHandler(XMLDocumentHandler xMLDocumentHandler) {
        this.fDocumentHandler = xMLDocumentHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource
    public XMLDocumentHandler getDocumentHandler() {
        return this.fDocumentHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void setDocumentSource(XMLDocumentSource xMLDocumentSource) {
        this.fDocumentSource = xMLDocumentSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public XMLDocumentSource getDocumentSource() {
        return this.fDocumentSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startDocument(XMLLocator xMLLocator, String str, NamespaceContext namespaceContext, Augmentations augmentations) throws XNIException {
        XMLGrammarPool xMLGrammarPool = this.fGrammarPool;
        if (xMLGrammarPool != null) {
            Grammar[] retrieveInitialGrammarSet = xMLGrammarPool.retrieveInitialGrammarSet(XMLGrammarDescription.XML_DTD);
            int length = retrieveInitialGrammarSet != null ? retrieveInitialGrammarSet.length : 0;
            for (int i = 0; i < length; i++) {
                this.fGrammarBucket.putGrammar((DTDGrammar) retrieveInitialGrammarSet[i]);
            }
        }
        this.fDocLocation = xMLLocator;
        this.fNamespaceContext = namespaceContext;
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            xMLDocumentHandler.startDocument(xMLLocator, str, namespaceContext, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void xmlDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
        this.fGrammarBucket.setStandalone(str3 != null && str3.equals("yes"));
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            xMLDocumentHandler.xmlDecl(str, str2, str3, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void doctypeDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
        this.fSeenDoctypeDecl = true;
        String str4 = null;
        this.fRootElement.setValues(null, str, str, null);
        try {
            str4 = XMLEntityManager.expandSystemId(str3, this.fDocLocation.getExpandedSystemId(), false);
        } catch (IOException unused) {
        }
        XMLDTDDescription xMLDTDDescription = new XMLDTDDescription(str2, str3, this.fDocLocation.getExpandedSystemId(), str4, str);
        this.fDTDGrammar = this.fGrammarBucket.getGrammar(xMLDTDDescription);
        if (!(this.fDTDGrammar != null || this.fGrammarPool == null || (str3 == null && str2 == null))) {
            this.fDTDGrammar = (DTDGrammar) this.fGrammarPool.retrieveGrammar(xMLDTDDescription);
        }
        if (this.fDTDGrammar != null) {
            this.fValidationManager.setCachedDTD(true);
        } else if (!this.fBalanceSyntaxTrees) {
            this.fDTDGrammar = new DTDGrammar(this.fSymbolTable, xMLDTDDescription);
        } else {
            this.fDTDGrammar = new BalancedDTDGrammar(this.fSymbolTable, xMLDTDDescription);
        }
        this.fGrammarBucket.setActiveGrammar(this.fDTDGrammar);
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            xMLDocumentHandler.doctypeDecl(str, str2, str3, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        handleStartElement(qName, xMLAttributes, augmentations);
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            xMLDocumentHandler.startElement(qName, xMLAttributes, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void emptyElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        boolean handleStartElement = handleStartElement(qName, xMLAttributes, augmentations);
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            xMLDocumentHandler.emptyElement(qName, xMLAttributes, augmentations);
        }
        if (!handleStartElement) {
            handleEndElement(qName, augmentations, true);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void characters(XMLString xMLString, Augmentations augmentations) throws XNIException {
        boolean z;
        boolean z2;
        XMLDocumentHandler xMLDocumentHandler;
        XMLDocumentHandler xMLDocumentHandler2;
        int i = xMLString.offset;
        while (true) {
            if (i >= xMLString.offset + xMLString.length) {
                z = true;
                break;
            } else if (!isSpace(xMLString.ch[i])) {
                z = false;
                break;
            } else {
                i++;
            }
        }
        if (!this.fInElementContent || !z || this.fInCDATASection || (xMLDocumentHandler2 = this.fDocumentHandler) == null) {
            z2 = true;
        } else {
            xMLDocumentHandler2.ignorableWhitespace(xMLString, augmentations);
            z2 = false;
        }
        if (this.fPerformValidation) {
            if (this.fInElementContent) {
                if (this.fGrammarBucket.getStandalone() && this.fDTDGrammar.getElementDeclIsExternal(this.fCurrentElementIndex) && z) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_WHITE_SPACE_IN_ELEMENT_CONTENT_WHEN_STANDALONE", null, 1);
                }
                if (!z) {
                    charDataInContent();
                }
                if (augmentations != null && augmentations.getItem(Constants.CHAR_REF_PROBABLE_WS) == Boolean.TRUE) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_CONTENT_INVALID_SPECIFIED", new Object[]{this.fCurrentElement.rawname, this.fDTDGrammar.getContentSpecAsString(this.fElementDepth), "character reference"}, 1);
                }
            }
            if (this.fCurrentContentSpecType == 1) {
                charDataInContent();
            }
        }
        if (z2 && (xMLDocumentHandler = this.fDocumentHandler) != null) {
            xMLDocumentHandler.characters(xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void ignorableWhitespace(XMLString xMLString, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            xMLDocumentHandler.ignorableWhitespace(xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endElement(QName qName, Augmentations augmentations) throws XNIException {
        handleEndElement(qName, augmentations, false);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startCDATA(Augmentations augmentations) throws XNIException {
        if (this.fPerformValidation && this.fInElementContent) {
            charDataInContent();
        }
        this.fInCDATASection = true;
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            xMLDocumentHandler.startCDATA(augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endCDATA(Augmentations augmentations) throws XNIException {
        this.fInCDATASection = false;
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            xMLDocumentHandler.endCDATA(augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endDocument(Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            xMLDocumentHandler.endDocument(augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void comment(XMLString xMLString, Augmentations augmentations) throws XNIException {
        DTDGrammar dTDGrammar;
        if (this.fPerformValidation && this.fElementDepth >= 0 && (dTDGrammar = this.fDTDGrammar) != null) {
            dTDGrammar.getElementDecl(this.fCurrentElementIndex, this.fTempElementDecl);
            if (this.fTempElementDecl.type == 1) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_CONTENT_INVALID_SPECIFIED", new Object[]{this.fCurrentElement.rawname, "EMPTY", ohos.com.sun.org.apache.xalan.internal.templates.Constants.ELEMNAME_COMMENT_STRING}, 1);
            }
        }
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            xMLDocumentHandler.comment(xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void processingInstruction(String str, XMLString xMLString, Augmentations augmentations) throws XNIException {
        DTDGrammar dTDGrammar;
        if (this.fPerformValidation && this.fElementDepth >= 0 && (dTDGrammar = this.fDTDGrammar) != null) {
            dTDGrammar.getElementDecl(this.fCurrentElementIndex, this.fTempElementDecl);
            if (this.fTempElementDecl.type == 1) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_CONTENT_INVALID_SPECIFIED", new Object[]{this.fCurrentElement.rawname, "EMPTY", "processing instruction"}, 1);
            }
        }
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            xMLDocumentHandler.processingInstruction(str, xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startGeneralEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
        DTDGrammar dTDGrammar;
        if (this.fPerformValidation && this.fElementDepth >= 0 && (dTDGrammar = this.fDTDGrammar) != null) {
            dTDGrammar.getElementDecl(this.fCurrentElementIndex, this.fTempElementDecl);
            if (this.fTempElementDecl.type == 1) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_CONTENT_INVALID_SPECIFIED", new Object[]{this.fCurrentElement.rawname, "EMPTY", SchemaSymbols.ATTVAL_ENTITY}, 1);
            }
            if (this.fGrammarBucket.getStandalone()) {
                XMLDTDLoader.checkStandaloneEntityRef(str, this.fDTDGrammar, this.fEntityDecl, this.fErrorReporter);
            }
        }
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            xMLDocumentHandler.startGeneralEntity(str, xMLResourceIdentifier, str2, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endGeneralEntity(String str, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            xMLDocumentHandler.endGeneralEntity(str, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void textDecl(String str, String str2, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            xMLDocumentHandler.textDecl(str, str2, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidatorFilter
    public final boolean hasGrammar() {
        return this.fDTDGrammar != null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidatorFilter
    public final boolean validate() {
        return this.fSchemaType != Constants.NS_XMLSCHEMA && ((!this.fDynamicValidation && this.fValidation) || (this.fDynamicValidation && this.fSeenDoctypeDecl)) && (this.fDTDValidation || this.fSeenDoctypeDecl);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x00e9 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0076  */
    public void addDTDDefaultAttrsAndValidate(QName qName, int i, XMLAttributes xMLAttributes) throws XNIException {
        DTDGrammar dTDGrammar;
        boolean z;
        String nonNormalizedValue;
        String externalEntityRefInAttrValue;
        boolean z2;
        int indexOf;
        if (!(i == -1 || (dTDGrammar = this.fDTDGrammar) == null)) {
            int firstAttributeDeclIndex = dTDGrammar.getFirstAttributeDeclIndex(i);
            for (int i2 = -1; firstAttributeDeclIndex != i2; i2 = -1) {
                this.fDTDGrammar.getAttributeDecl(firstAttributeDeclIndex, this.fTempAttDecl);
                String str = this.fTempAttDecl.name.prefix;
                String str2 = this.fTempAttDecl.name.localpart;
                String str3 = this.fTempAttDecl.name.rawname;
                String attributeTypeName = getAttributeTypeName(this.fTempAttDecl);
                short s = this.fTempAttDecl.simpleType.defaultType;
                String str4 = this.fTempAttDecl.simpleType.defaultValue != null ? this.fTempAttDecl.simpleType.defaultValue : null;
                boolean z3 = s == 2;
                if (!(attributeTypeName == XMLSymbols.fCDATASymbol) || z3 || str4 != null) {
                    int length = xMLAttributes.getLength();
                    int i3 = 0;
                    while (true) {
                        if (i3 >= length) {
                            break;
                        } else if (xMLAttributes.getQName(i3) == str3) {
                            z2 = true;
                            break;
                        } else {
                            i3++;
                        }
                    }
                    if (!z2) {
                        if (z3) {
                            if (this.fPerformValidation) {
                                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_REQUIRED_ATTRIBUTE_NOT_SPECIFIED", new Object[]{qName.localpart, str3}, 1);
                            }
                        } else if (str4 != null) {
                            if (this.fPerformValidation && this.fGrammarBucket.getStandalone() && this.fDTDGrammar.getAttributeDeclIsExternal(firstAttributeDeclIndex)) {
                                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DEFAULTED_ATTRIBUTE_NOT_SPECIFIED", new Object[]{qName.localpart, str3}, 1);
                            }
                            if (this.fNamespaces && (indexOf = str3.indexOf(58)) != -1) {
                                str = this.fSymbolTable.addSymbol(str3.substring(0, indexOf));
                                str2 = this.fSymbolTable.addSymbol(str3.substring(indexOf + 1));
                            }
                            this.fTempQName.setValues(str, str2, str3, this.fTempAttDecl.name.uri);
                            xMLAttributes.addAttribute(this.fTempQName, attributeTypeName, str4);
                        }
                    }
                    firstAttributeDeclIndex = this.fDTDGrammar.getNextAttributeDeclIndex(firstAttributeDeclIndex);
                }
                z2 = false;
                if (!z2) {
                }
                firstAttributeDeclIndex = this.fDTDGrammar.getNextAttributeDeclIndex(firstAttributeDeclIndex);
            }
            int length2 = xMLAttributes.getLength();
            for (int i4 = 0; i4 < length2; i4++) {
                String qName2 = xMLAttributes.getQName(i4);
                if (this.fPerformValidation && this.fGrammarBucket.getStandalone() && (nonNormalizedValue = xMLAttributes.getNonNormalizedValue(i4)) != null && (externalEntityRefInAttrValue = getExternalEntityRefInAttrValue(nonNormalizedValue)) != null) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_REFERENCE_TO_EXTERNALLY_DECLARED_ENTITY_WHEN_STANDALONE", new Object[]{externalEntityRefInAttrValue}, 1);
                }
                int firstAttributeDeclIndex2 = this.fDTDGrammar.getFirstAttributeDeclIndex(i);
                while (true) {
                    if (firstAttributeDeclIndex2 == -1) {
                        z = false;
                        break;
                    }
                    this.fDTDGrammar.getAttributeDecl(firstAttributeDeclIndex2, this.fTempAttDecl);
                    if (this.fTempAttDecl.name.rawname == qName2) {
                        z = true;
                        break;
                    }
                    firstAttributeDeclIndex2 = this.fDTDGrammar.getNextAttributeDeclIndex(firstAttributeDeclIndex2);
                }
                if (z) {
                    String attributeTypeName2 = getAttributeTypeName(this.fTempAttDecl);
                    xMLAttributes.setType(i4, attributeTypeName2);
                    xMLAttributes.getAugmentations(i4).putItem(Constants.ATTRIBUTE_DECLARED, Boolean.TRUE);
                    String value = xMLAttributes.getValue(i4);
                    if (xMLAttributes.isSpecified(i4) && attributeTypeName2 != XMLSymbols.fCDATASymbol) {
                        boolean normalizeAttrValue = normalizeAttrValue(xMLAttributes, i4);
                        String value2 = xMLAttributes.getValue(i4);
                        if (this.fPerformValidation && this.fGrammarBucket.getStandalone() && normalizeAttrValue && this.fDTDGrammar.getAttributeDeclIsExternal(firstAttributeDeclIndex2)) {
                            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE", new Object[]{qName2, value, value2}, 1);
                        }
                        value = value2;
                    }
                    if (this.fPerformValidation) {
                        if (this.fTempAttDecl.simpleType.defaultType == 1) {
                            String str5 = this.fTempAttDecl.simpleType.defaultValue;
                            if (!value.equals(str5)) {
                                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_FIXED_ATTVALUE_INVALID", new Object[]{qName.localpart, qName2, value, str5}, 1);
                                if (this.fTempAttDecl.simpleType.type != 1 || this.fTempAttDecl.simpleType.type == 2 || this.fTempAttDecl.simpleType.type == 3 || this.fTempAttDecl.simpleType.type == 4 || this.fTempAttDecl.simpleType.type == 5 || this.fTempAttDecl.simpleType.type == 6) {
                                    validateDTDattribute(qName, value, this.fTempAttDecl);
                                }
                            }
                        }
                        if (this.fTempAttDecl.simpleType.type != 1) {
                        }
                        validateDTDattribute(qName, value, this.fTempAttDecl);
                    }
                } else if (this.fPerformValidation) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_ATTRIBUTE_NOT_DECLARED", new Object[]{qName.rawname, qName2}, 1);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public String getExternalEntityRefInAttrValue(String str) {
        String addSymbol;
        int entityDeclIndex;
        int length = str.length();
        int indexOf = str.indexOf(38);
        while (indexOf != -1) {
            int i = indexOf + 1;
            if (i < length && str.charAt(i) != '#' && (entityDeclIndex = this.fDTDGrammar.getEntityDeclIndex((addSymbol = this.fSymbolTable.addSymbol(str.substring(i, str.indexOf(59, i)))))) > -1) {
                this.fDTDGrammar.getEntityDecl(entityDeclIndex, this.fEntityDecl);
                if (this.fEntityDecl.inExternal || (addSymbol = getExternalEntityRefInAttrValue(this.fEntityDecl.value)) != null) {
                    return addSymbol;
                }
            }
            indexOf = str.indexOf(38, i);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00b0  */
    /* JADX WARNING: Removed duplicated region for block: B:63:? A[RETURN, SYNTHETIC] */
    public void validateDTDattribute(QName qName, String str, XMLAttributeDecl xMLAttributeDecl) throws XNIException {
        boolean z;
        switch (xMLAttributeDecl.simpleType.type) {
            case 1:
                if (xMLAttributeDecl.simpleType.list) {
                    try {
                        this.fValENTITIES.validate(str, this.fValidationState);
                        return;
                    } catch (InvalidDatatypeValueException e) {
                        this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", e.getKey(), e.getArgs(), 1);
                        return;
                    }
                } else {
                    this.fValENTITY.validate(str, this.fValidationState);
                    return;
                }
            case 2:
            case 6:
                String[] strArr = xMLAttributeDecl.simpleType.enumeration;
                if (strArr != null) {
                    int i = 0;
                    while (true) {
                        if (i < strArr.length) {
                            if (str != strArr[i] && !str.equals(strArr[i])) {
                                i++;
                            }
                        }
                    }
                    z = true;
                    if (z) {
                        StringBuffer stringBuffer = new StringBuffer();
                        if (strArr != null) {
                            for (int i2 = 0; i2 < strArr.length; i2++) {
                                stringBuffer.append(strArr[i2] + " ");
                            }
                        }
                        this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_ATTRIBUTE_VALUE_NOT_IN_LIST", new Object[]{xMLAttributeDecl.name.rawname, str, stringBuffer}, 1);
                        return;
                    }
                    return;
                }
                z = false;
                if (z) {
                }
                break;
            case 3:
                try {
                    this.fValID.validate(str, this.fValidationState);
                    return;
                } catch (InvalidDatatypeValueException e2) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", e2.getKey(), e2.getArgs(), 1);
                    return;
                }
            case 4:
                boolean z2 = xMLAttributeDecl.simpleType.list;
                if (z2) {
                    try {
                        this.fValIDRefs.validate(str, this.fValidationState);
                        return;
                    } catch (InvalidDatatypeValueException e3) {
                        if (z2) {
                            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "IDREFSInvalid", new Object[]{str}, 1);
                            return;
                        } else {
                            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", e3.getKey(), e3.getArgs(), 1);
                            return;
                        }
                    }
                } else {
                    this.fValIDRef.validate(str, this.fValidationState);
                    return;
                }
            case 5:
                boolean z3 = xMLAttributeDecl.simpleType.list;
                if (z3) {
                    try {
                        this.fValNMTOKENS.validate(str, this.fValidationState);
                        return;
                    } catch (InvalidDatatypeValueException unused) {
                        if (z3) {
                            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "NMTOKENSInvalid", new Object[]{str}, 1);
                            return;
                        } else {
                            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "NMTOKENInvalid", new Object[]{str}, 1);
                            return;
                        }
                    }
                } else {
                    this.fValNMTOKEN.validate(str, this.fValidationState);
                    return;
                }
            default:
                return;
        }
    }

    private boolean normalizeAttrValue(XMLAttributes xMLAttributes, int i) {
        String value = xMLAttributes.getValue(i);
        char[] cArr = new char[value.length()];
        this.fBuffer.setLength(0);
        value.getChars(0, value.length(), cArr, 0);
        boolean z = true;
        int i2 = 0;
        boolean z2 = false;
        boolean z3 = false;
        for (int i3 = 0; i3 < cArr.length; i3++) {
            if (cArr[i3] == ' ') {
                if (z2) {
                    z3 = true;
                    z2 = false;
                }
                if (z3 && !z) {
                    this.fBuffer.append(cArr[i3]);
                    i2++;
                    z3 = false;
                }
            } else {
                this.fBuffer.append(cArr[i3]);
                i2++;
                z2 = true;
                z3 = false;
                z = false;
            }
        }
        if (i2 > 0) {
            int i4 = i2 - 1;
            if (this.fBuffer.charAt(i4) == ' ') {
                this.fBuffer.setLength(i4);
            }
        }
        String stringBuffer = this.fBuffer.toString();
        xMLAttributes.setValue(i, stringBuffer);
        return !value.equals(stringBuffer);
    }

    private final void rootElementSpecified(QName qName) throws XNIException {
        if (this.fPerformValidation) {
            String str = this.fRootElement.rawname;
            String str2 = qName.rawname;
            if (str == null || !str.equals(str2)) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "RootElementTypeMustMatchDoctypedecl", new Object[]{str, str2}, 1);
            }
        }
    }

    private int checkContent(int i, QName[] qNameArr, int i2, int i3) throws XNIException {
        this.fDTDGrammar.getElementDecl(i, this.fTempElementDecl);
        String str = this.fCurrentElement.rawname;
        int i4 = this.fCurrentContentSpecType;
        if (i4 == 1) {
            if (i3 != 0) {
                return 0;
            }
        } else if (i4 != 0 && (i4 == 2 || i4 == 3)) {
            return this.fTempElementDecl.contentModelValidator.validate(qNameArr, i2, i3);
        }
        return -1;
    }

    private int getContentSpecType(int i) {
        if (i <= -1 || !this.fDTDGrammar.getElementDecl(i, this.fTempElementDecl)) {
            return -1;
        }
        return this.fTempElementDecl.type;
    }

    private void charDataInContent() {
        QName[] qNameArr;
        QName[] qNameArr2 = this.fElementChildren;
        if (qNameArr2.length <= this.fElementChildrenLength) {
            QName[] qNameArr3 = new QName[(qNameArr2.length * 2)];
            System.arraycopy(qNameArr2, 0, qNameArr3, 0, qNameArr2.length);
            this.fElementChildren = qNameArr3;
        }
        QName[] qNameArr4 = this.fElementChildren;
        int i = this.fElementChildrenLength;
        QName qName = qNameArr4[i];
        if (qName == null) {
            while (true) {
                qNameArr = this.fElementChildren;
                if (i >= qNameArr.length) {
                    break;
                }
                qNameArr[i] = new QName();
                i++;
            }
            qName = qNameArr[this.fElementChildrenLength];
        }
        qName.clear();
        this.fElementChildrenLength++;
    }

    private String getAttributeTypeName(XMLAttributeDecl xMLAttributeDecl) {
        switch (xMLAttributeDecl.simpleType.type) {
            case 1:
                return xMLAttributeDecl.simpleType.list ? XMLSymbols.fENTITIESSymbol : XMLSymbols.fENTITYSymbol;
            case 2:
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append('(');
                for (int i = 0; i < xMLAttributeDecl.simpleType.enumeration.length; i++) {
                    if (i > 0) {
                        stringBuffer.append('|');
                    }
                    stringBuffer.append(xMLAttributeDecl.simpleType.enumeration[i]);
                }
                stringBuffer.append(')');
                return this.fSymbolTable.addSymbol(stringBuffer.toString());
            case 3:
                return XMLSymbols.fIDSymbol;
            case 4:
                return xMLAttributeDecl.simpleType.list ? XMLSymbols.fIDREFSSymbol : XMLSymbols.fIDREFSymbol;
            case 5:
                return xMLAttributeDecl.simpleType.list ? XMLSymbols.fNMTOKENSSymbol : XMLSymbols.fNMTOKENSymbol;
            case 6:
                return XMLSymbols.fNOTATIONSymbol;
            default:
                return XMLSymbols.fCDATASymbol;
        }
    }

    /* access modifiers changed from: protected */
    public void init() {
        if (this.fValidation || this.fDynamicValidation) {
            try {
                this.fValID = this.fDatatypeValidatorFactory.getBuiltInDV(XMLSymbols.fIDSymbol);
                this.fValIDRef = this.fDatatypeValidatorFactory.getBuiltInDV(XMLSymbols.fIDREFSymbol);
                this.fValIDRefs = this.fDatatypeValidatorFactory.getBuiltInDV(XMLSymbols.fIDREFSSymbol);
                this.fValENTITY = this.fDatatypeValidatorFactory.getBuiltInDV(XMLSymbols.fENTITYSymbol);
                this.fValENTITIES = this.fDatatypeValidatorFactory.getBuiltInDV(XMLSymbols.fENTITIESSymbol);
                this.fValNMTOKEN = this.fDatatypeValidatorFactory.getBuiltInDV(XMLSymbols.fNMTOKENSymbol);
                this.fValNMTOKENS = this.fDatatypeValidatorFactory.getBuiltInDV(XMLSymbols.fNMTOKENSSymbol);
                this.fValNOTATION = this.fDatatypeValidatorFactory.getBuiltInDV(XMLSymbols.fNOTATIONSymbol);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

    private void ensureStackCapacity(int i) {
        QName[] qNameArr = this.fElementQNamePartsStack;
        if (i == qNameArr.length) {
            int i2 = i * 2;
            QName[] qNameArr2 = new QName[i2];
            System.arraycopy(qNameArr, 0, qNameArr2, 0, i);
            this.fElementQNamePartsStack = qNameArr2;
            if (this.fElementQNamePartsStack[i] == null) {
                int i3 = i;
                while (true) {
                    QName[] qNameArr3 = this.fElementQNamePartsStack;
                    if (i3 >= qNameArr3.length) {
                        break;
                    }
                    qNameArr3[i3] = new QName();
                    i3++;
                }
            }
            int[] iArr = new int[i2];
            System.arraycopy(this.fElementIndexStack, 0, iArr, 0, i);
            this.fElementIndexStack = iArr;
            int[] iArr2 = new int[i2];
            System.arraycopy(this.fContentSpecTypeStack, 0, iArr2, 0, i);
            this.fContentSpecTypeStack = iArr2;
        }
    }

    /* access modifiers changed from: protected */
    public boolean handleStartElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        QName[] qNameArr;
        if (!this.fSeenRootElement) {
            this.fPerformValidation = validate();
            this.fSeenRootElement = true;
            this.fValidationManager.setEntityState(this.fDTDGrammar);
            this.fValidationManager.setGrammarFound(this.fSeenDoctypeDecl);
            rootElementSpecified(qName);
        }
        DTDGrammar dTDGrammar = this.fDTDGrammar;
        if (dTDGrammar == null) {
            if (!this.fPerformValidation) {
                this.fCurrentElementIndex = -1;
                this.fCurrentContentSpecType = -1;
                this.fInElementContent = false;
            }
            if (this.fPerformValidation) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_GRAMMAR_NOT_FOUND", new Object[]{qName.rawname}, 1);
            }
            XMLDocumentSource xMLDocumentSource = this.fDocumentSource;
            if (xMLDocumentSource != null) {
                xMLDocumentSource.setDocumentHandler(this.fDocumentHandler);
                XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
                if (xMLDocumentHandler != null) {
                    xMLDocumentHandler.setDocumentSource(this.fDocumentSource);
                }
                return true;
            }
        } else {
            this.fCurrentElementIndex = dTDGrammar.getElementDeclIndex(qName);
            this.fCurrentContentSpecType = this.fDTDGrammar.getContentSpecType(this.fCurrentElementIndex);
            if (this.fCurrentContentSpecType == -1 && this.fPerformValidation) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_ELEMENT_NOT_DECLARED", new Object[]{qName.rawname}, 1);
            }
            addDTDDefaultAttrsAndValidate(qName, this.fCurrentElementIndex, xMLAttributes);
        }
        this.fInElementContent = this.fCurrentContentSpecType == 3;
        this.fElementDepth++;
        if (this.fPerformValidation) {
            int[] iArr = this.fElementChildrenOffsetStack;
            if (iArr.length <= this.fElementDepth) {
                int[] iArr2 = new int[(iArr.length * 2)];
                System.arraycopy(iArr, 0, iArr2, 0, iArr.length);
                this.fElementChildrenOffsetStack = iArr2;
            }
            int[] iArr3 = this.fElementChildrenOffsetStack;
            int i = this.fElementDepth;
            int i2 = this.fElementChildrenLength;
            iArr3[i] = i2;
            QName[] qNameArr2 = this.fElementChildren;
            if (qNameArr2.length <= i2) {
                QName[] qNameArr3 = new QName[(i2 * 2)];
                System.arraycopy(qNameArr2, 0, qNameArr3, 0, qNameArr2.length);
                this.fElementChildren = qNameArr3;
            }
            QName[] qNameArr4 = this.fElementChildren;
            int i3 = this.fElementChildrenLength;
            QName qName2 = qNameArr4[i3];
            if (qName2 == null) {
                while (true) {
                    qNameArr = this.fElementChildren;
                    if (i3 >= qNameArr.length) {
                        break;
                    }
                    qNameArr[i3] = new QName();
                    i3++;
                }
                qName2 = qNameArr[this.fElementChildrenLength];
            }
            qName2.setValues(qName);
            this.fElementChildrenLength++;
        }
        this.fCurrentElement.setValues(qName);
        ensureStackCapacity(this.fElementDepth);
        this.fElementQNamePartsStack[this.fElementDepth].setValues(this.fCurrentElement);
        int[] iArr4 = this.fElementIndexStack;
        int i4 = this.fElementDepth;
        iArr4[i4] = this.fCurrentElementIndex;
        this.fContentSpecTypeStack[i4] = this.fCurrentContentSpecType;
        startNamespaceScope(qName, xMLAttributes, augmentations);
        return false;
    }

    /* access modifiers changed from: protected */
    public void handleEndElement(QName qName, Augmentations augmentations, boolean z) throws XNIException {
        String checkIDRefID;
        int i;
        int i2;
        int checkContent;
        boolean z2 = true;
        this.fElementDepth--;
        if (this.fPerformValidation) {
            int i3 = this.fCurrentElementIndex;
            if (!(i3 == -1 || this.fCurrentContentSpecType == -1 || (checkContent = checkContent(i3, this.fElementChildren, i, (i2 = this.fElementChildrenLength - (i = this.fElementChildrenOffsetStack[this.fElementDepth + 1] + 1)))) == -1)) {
                this.fDTDGrammar.getElementDecl(i3, this.fTempElementDecl);
                String str = "MSG_CONTENT_INVALID";
                if (this.fTempElementDecl.type == 1) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", str, new Object[]{qName.rawname, "EMPTY"}, 1);
                } else {
                    if (checkContent == i2) {
                        str = "MSG_CONTENT_INCOMPLETE";
                    }
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", str, new Object[]{qName.rawname, this.fDTDGrammar.getContentSpecAsString(i3)}, 1);
                }
            }
            this.fElementChildrenLength = this.fElementChildrenOffsetStack[this.fElementDepth + 1] + 1;
        }
        endNamespaceScope(this.fCurrentElement, augmentations, z);
        int i4 = this.fElementDepth;
        if (i4 < -1) {
            throw new RuntimeException("FWK008 Element stack underflow");
        } else if (i4 < 0) {
            this.fCurrentElement.clear();
            this.fCurrentElementIndex = -1;
            this.fCurrentContentSpecType = -1;
            this.fInElementContent = false;
            if (this.fPerformValidation && (checkIDRefID = this.fValidationState.checkIDRefID()) != null) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_ELEMENT_WITH_ID_REQUIRED", new Object[]{checkIDRefID}, 1);
            }
        } else {
            this.fCurrentElement.setValues(this.fElementQNamePartsStack[i4]);
            int[] iArr = this.fElementIndexStack;
            int i5 = this.fElementDepth;
            this.fCurrentElementIndex = iArr[i5];
            this.fCurrentContentSpecType = this.fContentSpecTypeStack[i5];
            if (this.fCurrentContentSpecType != 3) {
                z2 = false;
            }
            this.fInElementContent = z2;
        }
    }

    /* access modifiers changed from: protected */
    public void endNamespaceScope(QName qName, Augmentations augmentations, boolean z) {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && !z) {
            xMLDocumentHandler.endElement(this.fCurrentElement, augmentations);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isSpace(int i) {
        return XMLChar.isSpace(i);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.RevalidationHandler
    public boolean characterData(String str, Augmentations augmentations) {
        characters(new XMLString(str.toCharArray(), 0, str.length()), augmentations);
        return true;
    }
}
