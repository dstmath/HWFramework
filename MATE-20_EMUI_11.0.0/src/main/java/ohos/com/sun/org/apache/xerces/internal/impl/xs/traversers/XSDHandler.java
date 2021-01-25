package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaNamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaException;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaLoader;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAttributeDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAttributeGroupDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSDDescription;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSDeclarationPool;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSElementDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSGrammarBucket;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSGroupDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSModelGroupImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSNotationDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSParticleDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.identity.IdentityConstraint;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.ElementImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.SchemaDOM;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.SchemaDOMParser;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.SchemaParsingConfig;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.SimpleLocator;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSInputSource;
import ohos.com.sun.org.apache.xerces.internal.parsers.SAXParser;
import ohos.com.sun.org.apache.xerces.internal.parsers.XML11Configuration;
import ohos.com.sun.org.apache.xerces.internal.util.DOMInputSource;
import ohos.com.sun.org.apache.xerces.internal.util.DOMUtil;
import ohos.com.sun.org.apache.xerces.internal.util.DefaultErrorHandler;
import ohos.com.sun.org.apache.xerces.internal.util.ErrorHandlerWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.SAXInputSource;
import ohos.com.sun.org.apache.xerces.internal.util.StAXInputSource;
import ohos.com.sun.org.apache.xerces.internal.util.StAXLocationWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolHash;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.URI;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLSchemaDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import ohos.com.sun.org.apache.xerces.internal.xs.StringList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeGroupDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeUse;
import ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration;
import ohos.com.sun.org.apache.xerces.internal.xs.XSModelGroup;
import ohos.com.sun.org.apache.xerces.internal.xs.XSModelGroupDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamedMap;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObject;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSParticle;
import ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTerm;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList;
import ohos.javax.xml.stream.XMLEventReader;
import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.XMLStreamReader;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXParseException;
import ohos.org.xml.sax.XMLReader;

public class XSDHandler {
    protected static final String ALLOW_JAVA_ENCODINGS = "http://apache.org/xml/features/allow-java-encodings";
    static final int ATTRIBUTEGROUP_TYPE = 2;
    static final int ATTRIBUTE_TYPE = 1;
    private static final String[] CIRCULAR_CODES = {"Internal-Error", "Internal-Error", "src-attribute_group.3", "e-props-correct.6", "mg-props-correct.2", "Internal-Error", "Internal-Error", "st-props-correct.2"};
    private static final String[] COMP_TYPE = {null, "attribute declaration", "attribute group", "element declaration", "group", "identity constraint", "notation", "type definition"};
    protected static final String CONTINUE_AFTER_FATAL_ERROR = "http://apache.org/xml/features/continue-after-fatal-error";
    protected static final boolean DEBUG_NODE_POOL = false;
    protected static final String DISALLOW_DOCTYPE = "http://apache.org/xml/features/disallow-doctype-decl";
    static final int ELEMENT_TYPE = 3;
    private static final String[] ELE_ERROR_CODES = {"src-include.1", "src-redefine.2", "src-import.2", "schema_reference.4", "schema_reference.4", "schema_reference.4", "schema_reference.4", "schema_reference.4"};
    protected static final String ENTITY_MANAGER = "http://apache.org/xml/properties/internal/entity-manager";
    public static final String ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
    protected static final String ERROR_HANDLER = "http://apache.org/xml/properties/internal/error-handler";
    public static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    protected static final String GENERATE_SYNTHETIC_ANNOTATIONS = "http://apache.org/xml/features/generate-synthetic-annotations";
    static final int GROUP_TYPE = 4;
    protected static final String HONOUR_ALL_SCHEMALOCATIONS = "http://apache.org/xml/features/honour-all-schemaLocations";
    static final int IDENTITYCONSTRAINT_TYPE = 5;
    private static final int INC_KEYREF_STACK_AMOUNT = 2;
    private static final int INC_STACK_SIZE = 10;
    private static final int INIT_KEYREF_STACK = 2;
    private static final int INIT_STACK_SIZE = 30;
    protected static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    protected static final String LOCALE = "http://apache.org/xml/properties/locale";
    protected static final String NAMESPACE_GROWTH = "http://apache.org/xml/features/namespace-growth";
    private static final String NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes";
    static final int NOTATION_TYPE = 6;
    private static final String[][] NS_ERROR_CODES = {new String[]{"src-include.2.1", "src-include.2.1"}, new String[]{"src-redefine.3.1", "src-redefine.3.1"}, new String[]{"src-import.3.1", "src-import.3.2"}, null, new String[]{"TargetNamespace.1", "TargetNamespace.2"}, new String[]{"TargetNamespace.1", "TargetNamespace.2"}, new String[]{"TargetNamespace.1", "TargetNamespace.2"}, new String[]{"TargetNamespace.1", "TargetNamespace.2"}};
    public static final String REDEF_IDENTIFIER = "_fn3dktizrknc9pi";
    protected static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    protected static final String STANDARD_URI_CONFORMANT_FEATURE = "http://apache.org/xml/features/standard-uri-conformant";
    protected static final String STRING_INTERNING = "http://xml.org/sax/features/string-interning";
    public static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    protected static final String TOLERATE_DUPLICATES = "http://apache.org/xml/features/internal/tolerate-duplicates";
    static final int TYPEDECL_TYPE = 7;
    protected static final String VALIDATE_ANNOTATIONS = "http://apache.org/xml/features/validate-annotations";
    protected static final String VALIDATION = "http://xml.org/sax/features/validation";
    public static final String XMLGRAMMAR_POOL = "http://apache.org/xml/properties/internal/grammar-pool";
    protected static final String XMLSCHEMA_VALIDATION = "http://apache.org/xml/features/validation/schema";
    private static final String XML_SECURITY_PROPERTY_MANAGER = "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager";
    private String fAccessExternalDTD;
    private String fAccessExternalSchema;
    private int[] fAllContext;
    private Vector<String> fAllTNSs;
    XML11Configuration fAnnotationValidator;
    private XSAttributeChecker fAttributeChecker;
    XSDAttributeGroupTraverser fAttributeGroupTraverser;
    XSDAttributeTraverser fAttributeTraverser;
    XSDComplexTypeTraverser fComplexTypeTraverser;
    SchemaDVFactory fDVFactory;
    protected XSDeclarationPool fDeclPool;
    private Map<XSDocumentInfo, Vector<XSDocumentInfo>> fDependencyMap;
    private Map<Element, String> fDoc2SystemId;
    private Map fDoc2XSDocumentMap;
    XSDElementTraverser fElementTraverser;
    private XMLEntityResolver fEntityManager;
    private XMLErrorHandler fErrorHandler;
    private XMLErrorReporter fErrorReporter;
    SymbolHash fGlobalAttrDecls;
    SymbolHash fGlobalAttrGrpDecls;
    SymbolHash fGlobalElemDecls;
    SymbolHash fGlobalGroupDecls;
    SymbolHash fGlobalIDConstraintDecls;
    SymbolHash fGlobalNotationDecls;
    SymbolHash fGlobalTypeDecls;
    private XSGrammarBucket fGrammarBucket;
    XSAnnotationGrammarPool fGrammarBucketAdapter;
    private XMLGrammarPool fGrammarPool;
    XSDGroupTraverser fGroupTraverser;
    Map<Node, String> fHiddenNodes;
    private boolean fHonourAllSchemaLocations;
    private Map<String, Vector> fImportMap;
    private XSElementDecl[] fKeyrefElems;
    private String[][] fKeyrefNamespaceContext;
    private int fKeyrefStackPos;
    XSDKeyrefTraverser fKeyrefTraverser;
    private Element[] fKeyrefs;
    private XSDocumentInfo[] fKeyrefsMapXSDocumentInfo;
    private boolean fLastSchemaWasDuplicate;
    private String[][] fLocalElemNamespaceContext;
    private int fLocalElemStackPos;
    private Element[] fLocalElementDecl;
    private XSDocumentInfo[] fLocalElementDecl_schema;
    private Locale fLocale;
    private Map<String, XMLSchemaLoader.LocationArray> fLocationPairs;
    boolean fNamespaceGrowth;
    XSDNotationTraverser fNotationTraverser;
    private boolean fOverrideDefaultParser;
    private XSObject[] fParent;
    private XSParticleDecl[] fParticle;
    private Map fRedefine2NSSupport;
    private Map fRedefine2XSDMap;
    private Map fRedefinedRestrictedAttributeGroupRegistry;
    private Map fRedefinedRestrictedGroupRegistry;
    private Vector fReportedTNS;
    private XSDocumentInfo fRoot;
    private XSDDescription fSchemaGrammarDescription;
    SchemaDOMParser fSchemaParser;
    protected XMLSecurityManager fSecurityManager;
    private XMLSecurityPropertyManager fSecurityPropertyMgr;
    XSDSimpleTypeTraverser fSimpleTypeTraverser;
    StAXSchemaParser fStAXSchemaParser;
    private SymbolTable fSymbolTable;
    boolean fTolerateDuplicates;
    private Map<XSDKey, Element> fTraversed;
    XSDUniqueOrKeyTraverser fUniqueOrKeyTraverser;
    private Map<String, Element> fUnparsedAttributeGroupRegistry;
    private Map<String, XSDocumentInfo> fUnparsedAttributeGroupRegistrySub;
    private Map<String, Element> fUnparsedAttributeRegistry;
    private Map<String, XSDocumentInfo> fUnparsedAttributeRegistrySub;
    private Map<String, Element> fUnparsedElementRegistry;
    private Map<String, XSDocumentInfo> fUnparsedElementRegistrySub;
    private Map<String, Element> fUnparsedGroupRegistry;
    private Map<String, XSDocumentInfo> fUnparsedGroupRegistrySub;
    private Map<String, Element> fUnparsedIdentityConstraintRegistry;
    private Map<String, XSDocumentInfo> fUnparsedIdentityConstraintRegistrySub;
    private Map<String, Element> fUnparsedNotationRegistry;
    private Map<String, XSDocumentInfo> fUnparsedNotationRegistrySub;
    private Map<String, XSDocumentInfo>[] fUnparsedRegistriesExt;
    private Map<String, Element> fUnparsedTypeRegistry;
    private Map<String, XSDocumentInfo> fUnparsedTypeRegistrySub;
    private boolean fValidateAnnotations;
    XSDWildcardTraverser fWildCardTraverser;
    SchemaContentHandler fXSContentHandler;
    private boolean registryEmpty;
    private SimpleLocator xl;

    private String null2EmptyString(String str) {
        return str == null ? XMLSymbols.EMPTY_STRING : str;
    }

    private String emptyString2Null(String str) {
        if (str == XMLSymbols.EMPTY_STRING) {
            return null;
        }
        return str;
    }

    private String doc2SystemId(Element element) {
        String documentURI = element.getOwnerDocument() instanceof SchemaDOM ? element.getOwnerDocument().getDocumentURI() : null;
        return documentURI != null ? documentURI : this.fDoc2SystemId.get(element);
    }

    public XSDHandler() {
        this.fDeclPool = null;
        this.fSecurityManager = null;
        this.registryEmpty = true;
        this.fUnparsedAttributeRegistry = new HashMap();
        this.fUnparsedAttributeGroupRegistry = new HashMap();
        this.fUnparsedElementRegistry = new HashMap();
        this.fUnparsedGroupRegistry = new HashMap();
        this.fUnparsedIdentityConstraintRegistry = new HashMap();
        this.fUnparsedNotationRegistry = new HashMap();
        this.fUnparsedTypeRegistry = new HashMap();
        this.fUnparsedAttributeRegistrySub = new HashMap();
        this.fUnparsedAttributeGroupRegistrySub = new HashMap();
        this.fUnparsedElementRegistrySub = new HashMap();
        this.fUnparsedGroupRegistrySub = new HashMap();
        this.fUnparsedIdentityConstraintRegistrySub = new HashMap();
        this.fUnparsedNotationRegistrySub = new HashMap();
        this.fUnparsedTypeRegistrySub = new HashMap();
        this.fUnparsedRegistriesExt = new HashMap[]{null, null, null, null, null, null, null, null};
        this.fDependencyMap = new HashMap();
        this.fImportMap = new HashMap();
        this.fAllTNSs = new Vector<>();
        this.fLocationPairs = null;
        this.fHiddenNodes = null;
        this.fTraversed = new HashMap();
        this.fDoc2SystemId = new HashMap();
        this.fRoot = null;
        this.fDoc2XSDocumentMap = new HashMap();
        this.fRedefine2XSDMap = null;
        this.fRedefine2NSSupport = null;
        this.fRedefinedRestrictedAttributeGroupRegistry = new HashMap();
        this.fRedefinedRestrictedGroupRegistry = new HashMap();
        this.fValidateAnnotations = false;
        this.fHonourAllSchemaLocations = false;
        this.fNamespaceGrowth = false;
        this.fTolerateDuplicates = false;
        this.fSecurityPropertyMgr = null;
        this.fLocalElemStackPos = 0;
        this.fParticle = new XSParticleDecl[30];
        this.fLocalElementDecl = new Element[30];
        this.fLocalElementDecl_schema = new XSDocumentInfo[30];
        this.fAllContext = new int[30];
        this.fParent = new XSObject[30];
        this.fLocalElemNamespaceContext = (String[][]) Array.newInstance(String.class, 30, 1);
        this.fKeyrefStackPos = 0;
        this.fKeyrefs = new Element[2];
        this.fKeyrefsMapXSDocumentInfo = new XSDocumentInfo[2];
        this.fKeyrefElems = new XSElementDecl[2];
        this.fKeyrefNamespaceContext = (String[][]) Array.newInstance(String.class, 2, 1);
        this.fGlobalAttrDecls = new SymbolHash(12);
        this.fGlobalAttrGrpDecls = new SymbolHash(5);
        this.fGlobalElemDecls = new SymbolHash(25);
        this.fGlobalGroupDecls = new SymbolHash(5);
        this.fGlobalNotationDecls = new SymbolHash(1);
        this.fGlobalIDConstraintDecls = new SymbolHash(3);
        this.fGlobalTypeDecls = new SymbolHash(25);
        this.fReportedTNS = null;
        this.xl = new SimpleLocator();
        this.fHiddenNodes = new HashMap();
        this.fSchemaParser = new SchemaDOMParser(new SchemaParsingConfig());
    }

    public XSDHandler(XSGrammarBucket xSGrammarBucket) {
        this();
        this.fGrammarBucket = xSGrammarBucket;
        this.fSchemaGrammarDescription = new XSDDescription();
    }

    public SchemaGrammar parseSchema(XMLInputSource xMLInputSource, XSDDescription xSDDescription, Map<String, XMLSchemaLoader.LocationArray> map) throws IOException {
        SchemaGrammar schemaGrammar;
        String str;
        Element element;
        String str2;
        SchemaGrammar schemaGrammar2;
        this.fLocationPairs = map;
        this.fSchemaParser.resetNodePool();
        short contextType = xSDDescription.getContextType();
        ArrayList arrayList = null;
        if (contextType != 3) {
            if (!this.fHonourAllSchemaLocations || contextType != 2 || !isExistingGrammar(xSDDescription, this.fNamespaceGrowth)) {
                schemaGrammar2 = findGrammar(xSDDescription, this.fNamespaceGrowth);
            } else {
                schemaGrammar2 = this.fGrammarBucket.getGrammar(xSDDescription.getTargetNamespace());
            }
            if (schemaGrammar2 != null) {
                if (!this.fNamespaceGrowth) {
                    return schemaGrammar2;
                }
                try {
                    if (schemaGrammar2.getDocumentLocations().contains(XMLEntityManager.expandSystemId(xMLInputSource.getSystemId(), xMLInputSource.getBaseSystemId(), false))) {
                        return schemaGrammar2;
                    }
                } catch (URI.MalformedURIException unused) {
                }
            }
            str = xSDDescription.getTargetNamespace();
            if (str != null) {
                str = this.fSymbolTable.addSymbol(str);
            }
            schemaGrammar = schemaGrammar2;
        } else {
            str = null;
            schemaGrammar = null;
        }
        prepareForParse();
        if (xMLInputSource instanceof DOMInputSource) {
            element = getSchemaDocument(str, (DOMInputSource) xMLInputSource, contextType == 3, contextType, (Element) null);
        } else if (xMLInputSource instanceof SAXInputSource) {
            element = getSchemaDocument(str, (SAXInputSource) xMLInputSource, contextType == 3, contextType, (Element) null);
        } else if (xMLInputSource instanceof StAXInputSource) {
            element = getSchemaDocument(str, (StAXInputSource) xMLInputSource, contextType == 3, contextType, (Element) null);
        } else if (xMLInputSource instanceof XSInputSource) {
            element = getSchemaDocument((XSInputSource) xMLInputSource, xSDDescription);
        } else {
            element = getSchemaDocument(str, xMLInputSource, contextType == 3, contextType, (Element) null);
        }
        if (element == null) {
            return xMLInputSource instanceof XSInputSource ? this.fGrammarBucket.getGrammar(xSDDescription.getTargetNamespace()) : schemaGrammar;
        }
        if (contextType == 3) {
            String attrValue = DOMUtil.getAttrValue(element, SchemaSymbols.ATT_TARGETNAMESPACE);
            if (attrValue == null || attrValue.length() <= 0) {
                str2 = null;
            } else {
                str2 = this.fSymbolTable.addSymbol(attrValue);
                xSDDescription.setTargetNamespace(str2);
            }
            schemaGrammar = findGrammar(xSDDescription, this.fNamespaceGrowth);
            String expandSystemId = XMLEntityManager.expandSystemId(xMLInputSource.getSystemId(), xMLInputSource.getBaseSystemId(), false);
            if (schemaGrammar != null && (!this.fNamespaceGrowth || (expandSystemId != null && schemaGrammar.getDocumentLocations().contains(expandSystemId)))) {
                return schemaGrammar;
            }
            this.fTraversed.put(new XSDKey(expandSystemId, contextType, str2), element);
            if (expandSystemId != null) {
                this.fDoc2SystemId.put(element, expandSystemId);
            }
        }
        prepareForTraverse();
        this.fRoot = constructTrees(element, xMLInputSource.getSystemId(), xSDDescription, schemaGrammar != null);
        if (this.fRoot == null) {
            return null;
        }
        buildGlobalNameRegistries();
        if (this.fValidateAnnotations) {
            arrayList = new ArrayList();
        }
        traverseSchemas(arrayList);
        traverseLocalElements();
        resolveKeyRefs();
        for (int size = this.fAllTNSs.size() - 1; size >= 0; size--) {
            String elementAt = this.fAllTNSs.elementAt(size);
            Vector vector = this.fImportMap.get(elementAt);
            SchemaGrammar grammar = this.fGrammarBucket.getGrammar(emptyString2Null(elementAt));
            if (grammar != null) {
                int i = 0;
                for (int i2 = 0; i2 < vector.size(); i2++) {
                    SchemaGrammar grammar2 = this.fGrammarBucket.getGrammar((String) vector.elementAt(i2));
                    if (grammar2 != null) {
                        vector.setElementAt(grammar2, i);
                        i++;
                    }
                }
                vector.setSize(i);
                grammar.setImportedGrammars(vector);
            }
        }
        if (this.fValidateAnnotations && arrayList.size() > 0) {
            validateAnnotations(arrayList);
        }
        return this.fGrammarBucket.getGrammar(this.fRoot.fTargetNamespace);
    }

    private void validateAnnotations(ArrayList arrayList) {
        if (this.fAnnotationValidator == null) {
            createAnnotationValidator();
        }
        int size = arrayList.size();
        XMLInputSource xMLInputSource = new XMLInputSource(null, null, null);
        this.fGrammarBucketAdapter.refreshGrammars(this.fGrammarBucket);
        for (int i = 0; i < size; i += 2) {
            xMLInputSource.setSystemId((String) arrayList.get(i));
            for (XSAnnotationInfo xSAnnotationInfo = (XSAnnotationInfo) arrayList.get(i + 1); xSAnnotationInfo != null; xSAnnotationInfo = xSAnnotationInfo.next) {
                xMLInputSource.setCharacterStream(new StringReader(xSAnnotationInfo.fAnnotation));
                try {
                    this.fAnnotationValidator.parse(xMLInputSource);
                } catch (IOException unused) {
                }
            }
        }
    }

    private void createAnnotationValidator() {
        this.fAnnotationValidator = new XML11Configuration();
        this.fGrammarBucketAdapter = new XSAnnotationGrammarPool();
        this.fAnnotationValidator.setFeature(VALIDATION, true);
        this.fAnnotationValidator.setFeature(XMLSCHEMA_VALIDATION, true);
        this.fAnnotationValidator.setProperty("http://apache.org/xml/properties/internal/grammar-pool", this.fGrammarBucketAdapter);
        XML11Configuration xML11Configuration = this.fAnnotationValidator;
        XMLSecurityManager xMLSecurityManager = this.fSecurityManager;
        if (xMLSecurityManager == null) {
            xMLSecurityManager = new XMLSecurityManager(true);
        }
        xML11Configuration.setProperty("http://apache.org/xml/properties/security-manager", xMLSecurityManager);
        this.fAnnotationValidator.setProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", this.fSecurityPropertyMgr);
        XML11Configuration xML11Configuration2 = this.fAnnotationValidator;
        Object obj = this.fErrorHandler;
        if (obj == null) {
            obj = new DefaultErrorHandler();
        }
        xML11Configuration2.setProperty(ERROR_HANDLER, obj);
        this.fAnnotationValidator.setProperty("http://apache.org/xml/properties/locale", this.fLocale);
    }

    /* access modifiers changed from: package-private */
    public SchemaGrammar getGrammar(String str) {
        return this.fGrammarBucket.getGrammar(str);
    }

    /* access modifiers changed from: protected */
    public SchemaGrammar findGrammar(XSDDescription xSDDescription, boolean z) {
        XMLGrammarPool xMLGrammarPool;
        SchemaGrammar grammar = this.fGrammarBucket.getGrammar(xSDDescription.getTargetNamespace());
        if (grammar != null || (xMLGrammarPool = this.fGrammarPool) == null) {
            return grammar;
        }
        SchemaGrammar schemaGrammar = (SchemaGrammar) xMLGrammarPool.retrieveGrammar(xSDDescription);
        if (schemaGrammar == null || this.fGrammarBucket.putGrammar(schemaGrammar, true, z)) {
            return schemaGrammar;
        }
        reportSchemaWarning("GrammarConflict", null, null);
        return null;
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:194:0x0454 */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x028c, code lost:
        if (isExistingGrammar(r21.fSchemaGrammarDescription, false) != false) goto L_0x028e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0200, code lost:
        if (r21.fNamespaceGrowth == false) goto L_0x028e;
     */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x0298  */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x029a  */
    /* JADX WARNING: Removed duplicated region for block: B:162:0x0406  */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x040f  */
    /* JADX WARNING: Removed duplicated region for block: B:191:0x0461 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x011b  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x01e0  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x01f4  */
    public XSDocumentInfo constructTrees(Element element, String str, XSDDescription xSDDescription, boolean z) {
        SchemaGrammar schemaGrammar;
        Vector<XSDocumentInfo> vector;
        Element firstChildElement;
        Vector<XSDocumentInfo> vector2;
        Vector<XSDocumentInfo> vector3;
        char c;
        Element element2;
        Vector<XSDocumentInfo> vector4;
        boolean z2;
        String str2;
        String str3;
        Element element3;
        XSDocumentInfo xSDocumentInfo;
        boolean z3;
        short s;
        boolean z4;
        Element element4;
        char c2;
        int i;
        char c3;
        Object[] objArr;
        char c4;
        Object[] objArr2;
        char c5;
        char c6;
        char c7;
        int i2;
        SchemaGrammar schemaGrammar2;
        if (element == null) {
            return null;
        }
        String targetNamespace = xSDDescription.getTargetNamespace();
        short contextType = xSDDescription.getContextType();
        boolean z5 = true;
        try {
            XSDocumentInfo xSDocumentInfo2 = new XSDocumentInfo(element, this.fAttributeChecker, this.fSymbolTable);
            if (xSDocumentInfo2.fTargetNamespace != null && xSDocumentInfo2.fTargetNamespace.length() == 0) {
                reportSchemaWarning("EmptyTargetNamespace", new Object[]{str}, element);
                xSDocumentInfo2.fTargetNamespace = null;
            }
            char c8 = 2;
            if (targetNamespace != null) {
                if (contextType == 0 || contextType == 1) {
                    if (xSDocumentInfo2.fTargetNamespace == null) {
                        xSDocumentInfo2.fTargetNamespace = targetNamespace;
                        xSDocumentInfo2.fIsChameleonSchema = true;
                    } else if (targetNamespace != xSDocumentInfo2.fTargetNamespace) {
                        reportSchemaError(NS_ERROR_CODES[contextType][0], new Object[]{targetNamespace, xSDocumentInfo2.fTargetNamespace}, element);
                        return null;
                    }
                } else if (!(contextType == 3 || targetNamespace == xSDocumentInfo2.fTargetNamespace)) {
                    reportSchemaError(NS_ERROR_CODES[contextType][0], new Object[]{targetNamespace, xSDocumentInfo2.fTargetNamespace}, element);
                    return null;
                }
            } else if (xSDocumentInfo2.fTargetNamespace != null) {
                if (contextType == 3) {
                    xSDDescription.setTargetNamespace(xSDocumentInfo2.fTargetNamespace);
                    targetNamespace = xSDocumentInfo2.fTargetNamespace;
                } else {
                    reportSchemaError(NS_ERROR_CODES[contextType][1], new Object[]{targetNamespace, xSDocumentInfo2.fTargetNamespace}, element);
                    return null;
                }
            }
            xSDocumentInfo2.addAllowedNS(xSDocumentInfo2.fTargetNamespace);
            if (z) {
                schemaGrammar2 = this.fGrammarBucket.getGrammar(xSDocumentInfo2.fTargetNamespace);
                if (schemaGrammar2.isImmutable()) {
                    SchemaGrammar schemaGrammar3 = new SchemaGrammar(schemaGrammar2);
                    this.fGrammarBucket.putGrammar(schemaGrammar3);
                    updateImportListWith(schemaGrammar3);
                    schemaGrammar2 = schemaGrammar3;
                }
                updateImportListFor(schemaGrammar2);
            } else if (contextType == 0 || contextType == 1) {
                schemaGrammar2 = this.fGrammarBucket.getGrammar(xSDocumentInfo2.fTargetNamespace);
            } else if (!this.fHonourAllSchemaLocations || contextType != 2) {
                schemaGrammar = new SchemaGrammar(xSDocumentInfo2.fTargetNamespace, xSDDescription.makeClone(), this.fSymbolTable);
                this.fGrammarBucket.putGrammar(schemaGrammar);
                schemaGrammar.addDocument(null, this.fDoc2SystemId.get(xSDocumentInfo2.fSchemaElement));
                this.fDoc2XSDocumentMap.put(element, xSDocumentInfo2);
                vector = new Vector<>();
                Element element5 = null;
                firstChildElement = DOMUtil.getFirstChildElement(element);
                while (true) {
                    if (firstChildElement == null) {
                        vector2 = vector;
                        break;
                    }
                    String localName = DOMUtil.getLocalName(firstChildElement);
                    if (localName.equals(SchemaSymbols.ELT_ANNOTATION)) {
                        vector4 = vector;
                        element2 = firstChildElement;
                        c = c8;
                    } else {
                        if (!localName.equals(SchemaSymbols.ELT_IMPORT)) {
                            vector4 = vector;
                            str2 = localName;
                            element2 = firstChildElement;
                            int i3 = c8;
                            if (!str2.equals(SchemaSymbols.ELT_INCLUDE) && !str2.equals(SchemaSymbols.ELT_REDEFINE)) {
                                vector2 = vector4;
                                break;
                            }
                            Object[] checkAttributes = this.fAttributeChecker.checkAttributes(element2, true, xSDocumentInfo2);
                            str3 = (String) checkAttributes[XSAttributeChecker.ATTIDX_SCHEMALOCATION];
                            if (str2.equals(SchemaSymbols.ELT_REDEFINE)) {
                                if (this.fRedefine2NSSupport == null) {
                                    this.fRedefine2NSSupport = new HashMap();
                                }
                                this.fRedefine2NSSupport.put(element2, new SchemaNamespaceSupport(xSDocumentInfo2.fNamespaceSupport));
                            }
                            if (str2.equals(SchemaSymbols.ELT_INCLUDE)) {
                                Element firstChildElement2 = DOMUtil.getFirstChildElement(element2);
                                if (firstChildElement2 != null) {
                                    String localName2 = DOMUtil.getLocalName(firstChildElement2);
                                    if (localName2.equals(SchemaSymbols.ELT_ANNOTATION)) {
                                        c3 = 1;
                                        schemaGrammar.addAnnotation(this.fElementTraverser.traverseAnnotationDecl(firstChildElement2, checkAttributes, true, xSDocumentInfo2));
                                        i = 3;
                                        c2 = 0;
                                    } else {
                                        c3 = 1;
                                        i = 3;
                                        Object[] objArr3 = new Object[3];
                                        c2 = 0;
                                        objArr3[0] = str2;
                                        objArr3[1] = "annotation?";
                                        objArr3[i3] = localName2;
                                        reportSchemaError("s4s-elt-must-match.1", objArr3, element2);
                                    }
                                    if (DOMUtil.getNextSiblingElement(firstChildElement2) != null) {
                                        Object[] objArr4 = new Object[i];
                                        objArr4[c2] = str2;
                                        objArr4[c3] = "annotation?";
                                        objArr4[i3] = DOMUtil.getLocalName(DOMUtil.getNextSiblingElement(firstChildElement2));
                                        reportSchemaError("s4s-elt-must-match.1", objArr4, element2);
                                    }
                                } else {
                                    String syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element2);
                                    if (syntheticAnnotation != null) {
                                        schemaGrammar.addAnnotation(this.fElementTraverser.traverseSyntheticAnnotation(element2, syntheticAnnotation, checkAttributes, true, xSDocumentInfo2));
                                    }
                                }
                            } else {
                                Element firstChildElement3 = DOMUtil.getFirstChildElement(element2);
                                while (firstChildElement3 != null) {
                                    if (DOMUtil.getLocalName(firstChildElement3).equals(SchemaSymbols.ELT_ANNOTATION)) {
                                        schemaGrammar.addAnnotation(this.fElementTraverser.traverseAnnotationDecl(firstChildElement3, checkAttributes, true, xSDocumentInfo2));
                                        DOMUtil.setHidden(firstChildElement3, this.fHiddenNodes);
                                    } else {
                                        String syntheticAnnotation2 = DOMUtil.getSyntheticAnnotation(element2);
                                        if (syntheticAnnotation2 != null) {
                                            element4 = firstChildElement3;
                                            schemaGrammar.addAnnotation(this.fElementTraverser.traverseSyntheticAnnotation(element2, syntheticAnnotation2, checkAttributes, true, xSDocumentInfo2));
                                            firstChildElement3 = DOMUtil.getNextSiblingElement(element4);
                                        }
                                    }
                                    element4 = firstChildElement3;
                                    firstChildElement3 = DOMUtil.getNextSiblingElement(element4);
                                }
                            }
                            this.fAttributeChecker.returnAttrArray(checkAttributes, xSDocumentInfo2);
                            if (str3 == null) {
                                Object[] objArr5 = new Object[i3];
                                objArr5[0] = "<include> or <redefine>";
                                objArr5[1] = "schemaLocation";
                                reportSchemaError("s4s-att-must-appear", objArr5, element2);
                            }
                            if (str2.equals(SchemaSymbols.ELT_REDEFINE)) {
                                z3 = nonAnnotationContent(element2);
                                s = 1;
                            } else {
                                s = 0;
                                z3 = false;
                            }
                            this.fSchemaGrammarDescription.reset();
                            this.fSchemaGrammarDescription.setContextType(s);
                            this.fSchemaGrammarDescription.setBaseSystemId(doc2SystemId(element));
                            this.fSchemaGrammarDescription.setLocationHints(new String[]{str3});
                            this.fSchemaGrammarDescription.setTargetNamespace(targetNamespace);
                            XMLInputSource resolveSchemaSource = resolveSchemaSource(this.fSchemaGrammarDescription, z3, element2, true);
                            if (this.fNamespaceGrowth && s == 0) {
                                try {
                                    z4 = schemaGrammar.getDocumentLocations().contains(XMLEntityManager.expandSystemId(resolveSchemaSource.getSystemId(), resolveSchemaSource.getBaseSystemId(), false));
                                } catch (URI.MalformedURIException unused) {
                                }
                                if (z4) {
                                    element5 = resolveSchema(resolveSchemaSource, this.fSchemaGrammarDescription, z3, element2);
                                    String str4 = xSDocumentInfo2.fTargetNamespace;
                                } else {
                                    this.fLastSchemaWasDuplicate = true;
                                }
                                element3 = element5;
                                z2 = false;
                                c = i3;
                            }
                            z4 = false;
                            if (z4) {
                            }
                            element3 = element5;
                            z2 = false;
                            c = i3;
                        } else {
                            Object[] checkAttributes2 = this.fAttributeChecker.checkAttributes(firstChildElement, z5, xSDocumentInfo2);
                            String str5 = (String) checkAttributes2[XSAttributeChecker.ATTIDX_SCHEMALOCATION];
                            String str6 = (String) checkAttributes2[XSAttributeChecker.ATTIDX_NAMESPACE];
                            if (str6 != null) {
                                str6 = this.fSymbolTable.addSymbol(str6);
                            }
                            Element firstChildElement4 = DOMUtil.getFirstChildElement(firstChildElement);
                            if (firstChildElement4 != null) {
                                String localName3 = DOMUtil.getLocalName(firstChildElement4);
                                if (localName3.equals(SchemaSymbols.ELT_ANNOTATION)) {
                                    schemaGrammar.addAnnotation(this.fElementTraverser.traverseAnnotationDecl(firstChildElement4, checkAttributes2, true, xSDocumentInfo2));
                                    objArr2 = checkAttributes2;
                                    c6 = 1;
                                    i2 = 3;
                                    c7 = 0;
                                    c5 = 2;
                                } else {
                                    objArr2 = checkAttributes2;
                                    i2 = 3;
                                    c6 = 1;
                                    c7 = 0;
                                    c5 = 2;
                                    reportSchemaError("s4s-elt-must-match.1", new Object[]{localName, "annotation?", localName3}, firstChildElement);
                                }
                                if (DOMUtil.getNextSiblingElement(firstChildElement4) != null) {
                                    Object[] objArr6 = new Object[i2];
                                    objArr6[c7] = localName;
                                    objArr6[c6] = "annotation?";
                                    objArr6[c5] = DOMUtil.getLocalName(DOMUtil.getNextSiblingElement(firstChildElement4));
                                    reportSchemaError("s4s-elt-must-match.1", objArr6, firstChildElement);
                                }
                            } else {
                                objArr2 = checkAttributes2;
                                c5 = 2;
                                String syntheticAnnotation3 = DOMUtil.getSyntheticAnnotation(firstChildElement);
                                if (syntheticAnnotation3 != null) {
                                    objArr = objArr2;
                                    element2 = firstChildElement;
                                    vector4 = vector;
                                    str2 = localName;
                                    c4 = 2;
                                    schemaGrammar.addAnnotation(this.fElementTraverser.traverseSyntheticAnnotation(firstChildElement, syntheticAnnotation3, objArr, true, xSDocumentInfo2));
                                    this.fAttributeChecker.returnAttrArray(objArr, xSDocumentInfo2);
                                    if (str6 != xSDocumentInfo2.fTargetNamespace) {
                                        reportSchemaError(str6 != null ? "src-import.1.1" : "src-import.1.2", new Object[]{str6}, element2);
                                        c = c4;
                                    } else {
                                        if (!xSDocumentInfo2.isAllowedNS(str6)) {
                                            xSDocumentInfo2.addAllowedNS(str6);
                                        } else if (!this.fHonourAllSchemaLocations) {
                                            c = c4;
                                        }
                                        String null2EmptyString = null2EmptyString(xSDocumentInfo2.fTargetNamespace);
                                        Vector vector5 = this.fImportMap.get(null2EmptyString);
                                        if (vector5 == null) {
                                            this.fAllTNSs.addElement(null2EmptyString);
                                            Vector vector6 = new Vector();
                                            this.fImportMap.put(null2EmptyString, vector6);
                                            vector6.addElement(str6);
                                        } else if (!vector5.contains(str6)) {
                                            vector5.addElement(str6);
                                        }
                                        this.fSchemaGrammarDescription.reset();
                                        this.fSchemaGrammarDescription.setContextType(c4);
                                        this.fSchemaGrammarDescription.setBaseSystemId(doc2SystemId(element));
                                        this.fSchemaGrammarDescription.setLiteralSystemId(str5);
                                        this.fSchemaGrammarDescription.setLocationHints(new String[]{str5});
                                        this.fSchemaGrammarDescription.setTargetNamespace(str6);
                                        SchemaGrammar findGrammar = findGrammar(this.fSchemaGrammarDescription, this.fNamespaceGrowth);
                                        if (findGrammar != null) {
                                            if (this.fNamespaceGrowth) {
                                                try {
                                                    c = c4;
                                                    if (!findGrammar.getDocumentLocations().contains(XMLEntityManager.expandSystemId(str5, this.fSchemaGrammarDescription.getBaseSystemId(), false))) {
                                                        z2 = true;
                                                        Element resolveSchema = resolveSchema(this.fSchemaGrammarDescription, false, element2, findGrammar != null);
                                                        str3 = str5;
                                                        element3 = resolveSchema;
                                                        c = c4;
                                                    }
                                                } catch (URI.MalformedURIException unused2) {
                                                }
                                            } else {
                                                c = c4;
                                                if (this.fHonourAllSchemaLocations) {
                                                    c = c4;
                                                }
                                            }
                                        }
                                        z2 = false;
                                        Element resolveSchema2 = resolveSchema(this.fSchemaGrammarDescription, false, element2, findGrammar != null);
                                        str3 = str5;
                                        element3 = resolveSchema2;
                                        c = c4;
                                    }
                                }
                            }
                            objArr = objArr2;
                            vector4 = vector;
                            str2 = localName;
                            element2 = firstChildElement;
                            c4 = c5;
                            this.fAttributeChecker.returnAttrArray(objArr, xSDocumentInfo2);
                            if (str6 != xSDocumentInfo2.fTargetNamespace) {
                            }
                        }
                        if (!this.fLastSchemaWasDuplicate) {
                            xSDocumentInfo = constructTrees(element3, str3, this.fSchemaGrammarDescription, z2);
                        } else if (element3 == null) {
                            xSDocumentInfo = null;
                        } else {
                            xSDocumentInfo = (XSDocumentInfo) this.fDoc2XSDocumentMap.get(element3);
                        }
                        if (str2.equals(SchemaSymbols.ELT_REDEFINE) && xSDocumentInfo != null) {
                            if (this.fRedefine2XSDMap == null) {
                                this.fRedefine2XSDMap = new HashMap();
                            }
                            this.fRedefine2XSDMap.put(element2, xSDocumentInfo);
                        }
                        vector3 = vector4;
                        if (element3 != null) {
                            if (xSDocumentInfo != null) {
                                vector3.addElement(xSDocumentInfo);
                            }
                            element5 = null;
                        } else {
                            element5 = element3;
                        }
                        firstChildElement = DOMUtil.getNextSiblingElement(element2);
                        char c9 = c == 1 ? 1 : 0;
                        boolean z6 = c == 1 ? 1 : 0;
                        boolean z7 = c == 1 ? 1 : 0;
                        c8 = c9;
                        vector = vector3;
                        z5 = true;
                    }
                    vector3 = vector4;
                    firstChildElement = DOMUtil.getNextSiblingElement(element2);
                    char c92 = c == 1 ? 1 : 0;
                    boolean z62 = c == 1 ? 1 : 0;
                    boolean z72 = c == 1 ? 1 : 0;
                    c8 = c92;
                    vector = vector3;
                    z5 = true;
                }
                this.fDependencyMap.put(xSDocumentInfo2, vector2);
                return xSDocumentInfo2;
            } else {
                schemaGrammar = findGrammar(xSDDescription, false);
                if (schemaGrammar == null) {
                    schemaGrammar = new SchemaGrammar(xSDocumentInfo2.fTargetNamespace, xSDDescription.makeClone(), this.fSymbolTable);
                    this.fGrammarBucket.putGrammar(schemaGrammar);
                }
                schemaGrammar.addDocument(null, this.fDoc2SystemId.get(xSDocumentInfo2.fSchemaElement));
                this.fDoc2XSDocumentMap.put(element, xSDocumentInfo2);
                vector = new Vector<>();
                Element element52 = null;
                firstChildElement = DOMUtil.getFirstChildElement(element);
                while (true) {
                    if (firstChildElement == null) {
                    }
                    firstChildElement = DOMUtil.getNextSiblingElement(element2);
                    char c922 = c == 1 ? 1 : 0;
                    boolean z622 = c == 1 ? 1 : 0;
                    boolean z722 = c == 1 ? 1 : 0;
                    c8 = c922;
                    vector = vector3;
                    z5 = true;
                }
                this.fDependencyMap.put(xSDocumentInfo2, vector2);
                return xSDocumentInfo2;
            }
            schemaGrammar = schemaGrammar2;
            schemaGrammar.addDocument(null, this.fDoc2SystemId.get(xSDocumentInfo2.fSchemaElement));
            this.fDoc2XSDocumentMap.put(element, xSDocumentInfo2);
            vector = new Vector<>();
            Element element522 = null;
            firstChildElement = DOMUtil.getFirstChildElement(element);
            while (true) {
                if (firstChildElement == null) {
                }
                firstChildElement = DOMUtil.getNextSiblingElement(element2);
                char c9222 = c == 1 ? 1 : 0;
                boolean z6222 = c == 1 ? 1 : 0;
                boolean z7222 = c == 1 ? 1 : 0;
                c8 = c9222;
                vector = vector3;
                z5 = true;
            }
            this.fDependencyMap.put(xSDocumentInfo2, vector2);
            return xSDocumentInfo2;
        } catch (XMLSchemaException unused3) {
            reportSchemaError(ELE_ERROR_CODES[contextType], new Object[]{str}, element);
            return null;
        }
    }

    private boolean isExistingGrammar(XSDDescription xSDDescription, boolean z) {
        SchemaGrammar grammar = this.fGrammarBucket.getGrammar(xSDDescription.getTargetNamespace());
        if (grammar == null) {
            return findGrammar(xSDDescription, z) != null;
        }
        if (grammar.isImmutable()) {
            return true;
        }
        try {
            return grammar.getDocumentLocations().contains(XMLEntityManager.expandSystemId(xSDDescription.getLiteralSystemId(), xSDDescription.getBaseSystemId(), false));
        } catch (URI.MalformedURIException unused) {
            return false;
        }
    }

    private void updateImportListFor(SchemaGrammar schemaGrammar) {
        Vector importedGrammars = schemaGrammar.getImportedGrammars();
        if (importedGrammars != null) {
            for (int i = 0; i < importedGrammars.size(); i++) {
                SchemaGrammar schemaGrammar2 = (SchemaGrammar) importedGrammars.elementAt(i);
                SchemaGrammar grammar = this.fGrammarBucket.getGrammar(schemaGrammar2.getTargetNamespace());
                if (!(grammar == null || schemaGrammar2 == grammar)) {
                    importedGrammars.set(i, grammar);
                }
            }
        }
    }

    private void updateImportListWith(SchemaGrammar schemaGrammar) {
        Vector importedGrammars;
        SchemaGrammar[] grammars = this.fGrammarBucket.getGrammars();
        for (SchemaGrammar schemaGrammar2 : grammars) {
            if (schemaGrammar2 != schemaGrammar && (importedGrammars = schemaGrammar2.getImportedGrammars()) != null) {
                int i = 0;
                while (true) {
                    if (i >= importedGrammars.size()) {
                        break;
                    }
                    SchemaGrammar schemaGrammar3 = (SchemaGrammar) importedGrammars.elementAt(i);
                    if (!null2EmptyString(schemaGrammar3.getTargetNamespace()).equals(null2EmptyString(schemaGrammar.getTargetNamespace()))) {
                        i++;
                    } else if (schemaGrammar3 != schemaGrammar) {
                        importedGrammars.set(i, schemaGrammar);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void buildGlobalNameRegistries() {
        Stack stack;
        int i;
        int i2;
        Stack stack2;
        Node node;
        int i3 = 0;
        this.registryEmpty = false;
        Stack stack3 = new Stack();
        stack3.push(this.fRoot);
        while (!stack3.empty()) {
            XSDocumentInfo xSDocumentInfo = (XSDocumentInfo) stack3.pop();
            Element element = xSDocumentInfo.fSchemaElement;
            if (!DOMUtil.isHidden(element, this.fHiddenNodes)) {
                int i4 = 1;
                Element firstChildElement = DOMUtil.getFirstChildElement(element);
                boolean z = true;
                while (firstChildElement != null) {
                    if (DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
                        i = i3;
                        stack = stack3;
                    } else if (DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_INCLUDE) || DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_IMPORT)) {
                        stack = stack3;
                        i2 = 1;
                        if (!z) {
                            i = 0;
                            reportSchemaError("s4s-elt-invalid-content.3", new Object[]{DOMUtil.getLocalName(firstChildElement)}, firstChildElement);
                        } else {
                            i = 0;
                        }
                        DOMUtil.setHidden(firstChildElement, this.fHiddenNodes);
                        firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
                        i4 = i2;
                        i3 = i;
                        stack3 = stack;
                    } else if (DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_REDEFINE)) {
                        if (!z) {
                            Object[] objArr = new Object[i4];
                            objArr[i3] = DOMUtil.getLocalName(firstChildElement);
                            reportSchemaError("s4s-elt-invalid-content.3", objArr, firstChildElement);
                        }
                        Node firstChildElement2 = DOMUtil.getFirstChildElement(firstChildElement);
                        while (firstChildElement2 != null) {
                            String attrValue = DOMUtil.getAttrValue(firstChildElement2, SchemaSymbols.ATT_NAME);
                            if (attrValue.length() == 0) {
                                node = firstChildElement2;
                            } else {
                                String str = xSDocumentInfo.fTargetNamespace == null ? "," + attrValue : xSDocumentInfo.fTargetNamespace + "," + attrValue;
                                String localName = DOMUtil.getLocalName(firstChildElement2);
                                if (localName.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP)) {
                                    node = firstChildElement2;
                                    checkForDuplicateNames(str, 2, this.fUnparsedAttributeGroupRegistry, this.fUnparsedAttributeGroupRegistrySub, firstChildElement2, xSDocumentInfo);
                                    renameRedefiningComponents(xSDocumentInfo, node, SchemaSymbols.ELT_ATTRIBUTEGROUP, attrValue, DOMUtil.getAttrValue(node, SchemaSymbols.ATT_NAME) + REDEF_IDENTIFIER);
                                } else {
                                    node = firstChildElement2;
                                    if (localName.equals(SchemaSymbols.ELT_COMPLEXTYPE) || localName.equals(SchemaSymbols.ELT_SIMPLETYPE)) {
                                        stack2 = stack3;
                                        checkForDuplicateNames(str, 7, this.fUnparsedTypeRegistry, this.fUnparsedTypeRegistrySub, node, xSDocumentInfo);
                                        String str2 = DOMUtil.getAttrValue(node, SchemaSymbols.ATT_NAME) + REDEF_IDENTIFIER;
                                        if (localName.equals(SchemaSymbols.ELT_COMPLEXTYPE)) {
                                            renameRedefiningComponents(xSDocumentInfo, node, SchemaSymbols.ELT_COMPLEXTYPE, attrValue, str2);
                                        } else {
                                            renameRedefiningComponents(xSDocumentInfo, node, SchemaSymbols.ELT_SIMPLETYPE, attrValue, str2);
                                        }
                                        firstChildElement2 = DOMUtil.getNextSiblingElement(node);
                                        stack3 = stack2;
                                        i3 = 0;
                                        i4 = 1;
                                    } else if (localName.equals(SchemaSymbols.ELT_GROUP)) {
                                        checkForDuplicateNames(str, 4, this.fUnparsedGroupRegistry, this.fUnparsedGroupRegistrySub, node, xSDocumentInfo);
                                        renameRedefiningComponents(xSDocumentInfo, node, SchemaSymbols.ELT_GROUP, attrValue, DOMUtil.getAttrValue(node, SchemaSymbols.ATT_NAME) + REDEF_IDENTIFIER);
                                    }
                                }
                            }
                            stack2 = stack3;
                            firstChildElement2 = DOMUtil.getNextSiblingElement(node);
                            stack3 = stack2;
                            i3 = 0;
                            i4 = 1;
                        }
                        stack = stack3;
                        i = i3;
                    } else {
                        stack = stack3;
                        String attrValue2 = DOMUtil.getAttrValue(firstChildElement, SchemaSymbols.ATT_NAME);
                        if (attrValue2.length() != 0) {
                            String str3 = xSDocumentInfo.fTargetNamespace == null ? "," + attrValue2 : xSDocumentInfo.fTargetNamespace + "," + attrValue2;
                            String localName2 = DOMUtil.getLocalName(firstChildElement);
                            if (localName2.equals(SchemaSymbols.ELT_ATTRIBUTE)) {
                                checkForDuplicateNames(str3, 1, this.fUnparsedAttributeRegistry, this.fUnparsedAttributeRegistrySub, firstChildElement, xSDocumentInfo);
                            } else if (localName2.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP)) {
                                checkForDuplicateNames(str3, 2, this.fUnparsedAttributeGroupRegistry, this.fUnparsedAttributeGroupRegistrySub, firstChildElement, xSDocumentInfo);
                            } else if (localName2.equals(SchemaSymbols.ELT_COMPLEXTYPE) || localName2.equals(SchemaSymbols.ELT_SIMPLETYPE)) {
                                checkForDuplicateNames(str3, 7, this.fUnparsedTypeRegistry, this.fUnparsedTypeRegistrySub, firstChildElement, xSDocumentInfo);
                            } else if (localName2.equals(SchemaSymbols.ELT_ELEMENT)) {
                                checkForDuplicateNames(str3, 3, this.fUnparsedElementRegistry, this.fUnparsedElementRegistrySub, firstChildElement, xSDocumentInfo);
                            } else if (localName2.equals(SchemaSymbols.ELT_GROUP)) {
                                checkForDuplicateNames(str3, 4, this.fUnparsedGroupRegistry, this.fUnparsedGroupRegistrySub, firstChildElement, xSDocumentInfo);
                            } else if (localName2.equals(SchemaSymbols.ELT_NOTATION)) {
                                checkForDuplicateNames(str3, 6, this.fUnparsedNotationRegistry, this.fUnparsedNotationRegistrySub, firstChildElement, xSDocumentInfo);
                            }
                        }
                        i2 = 1;
                        i = 0;
                        z = false;
                        firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
                        i4 = i2;
                        i3 = i;
                        stack3 = stack;
                    }
                    i2 = i4;
                    firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
                    i4 = i2;
                    i3 = i;
                    stack3 = stack;
                }
                DOMUtil.setHidden(element, this.fHiddenNodes);
                Vector<XSDocumentInfo> vector = this.fDependencyMap.get(xSDocumentInfo);
                for (int i5 = i3; i5 < vector.size(); i5++) {
                    stack3.push(vector.elementAt(i5));
                }
                i3 = i3;
                stack3 = stack3;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void traverseSchemas(ArrayList arrayList) {
        XSAnnotationInfo annotations;
        String syntheticAnnotation;
        setSchemasVisible(this.fRoot);
        Stack stack = new Stack();
        stack.push(this.fRoot);
        while (!stack.empty()) {
            XSDocumentInfo xSDocumentInfo = (XSDocumentInfo) stack.pop();
            Element element = xSDocumentInfo.fSchemaElement;
            SchemaGrammar grammar = this.fGrammarBucket.getGrammar(xSDocumentInfo.fTargetNamespace);
            if (!DOMUtil.isHidden(element, this.fHiddenNodes)) {
                Element firstVisibleChildElement = DOMUtil.getFirstVisibleChildElement(element, this.fHiddenNodes);
                boolean z = false;
                while (firstVisibleChildElement != null) {
                    DOMUtil.setHidden(firstVisibleChildElement, this.fHiddenNodes);
                    String localName = DOMUtil.getLocalName(firstVisibleChildElement);
                    if (DOMUtil.getLocalName(firstVisibleChildElement).equals(SchemaSymbols.ELT_REDEFINE)) {
                        Map map = this.fRedefine2NSSupport;
                        xSDocumentInfo.backupNSSupport(map != null ? (SchemaNamespaceSupport) map.get(firstVisibleChildElement) : null);
                        Element firstVisibleChildElement2 = DOMUtil.getFirstVisibleChildElement(firstVisibleChildElement, this.fHiddenNodes);
                        while (firstVisibleChildElement2 != null) {
                            String localName2 = DOMUtil.getLocalName(firstVisibleChildElement2);
                            DOMUtil.setHidden(firstVisibleChildElement2, this.fHiddenNodes);
                            if (localName2.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP)) {
                                this.fAttributeGroupTraverser.traverseGlobal(firstVisibleChildElement2, xSDocumentInfo, grammar);
                            } else if (localName2.equals(SchemaSymbols.ELT_COMPLEXTYPE)) {
                                this.fComplexTypeTraverser.traverseGlobal(firstVisibleChildElement2, xSDocumentInfo, grammar);
                            } else if (localName2.equals(SchemaSymbols.ELT_GROUP)) {
                                this.fGroupTraverser.traverseGlobal(firstVisibleChildElement2, xSDocumentInfo, grammar);
                            } else if (localName2.equals(SchemaSymbols.ELT_SIMPLETYPE)) {
                                this.fSimpleTypeTraverser.traverseGlobal(firstVisibleChildElement2, xSDocumentInfo, grammar);
                            } else {
                                reportSchemaError("s4s-elt-must-match.1", new Object[]{DOMUtil.getLocalName(firstVisibleChildElement), "(annotation | (simpleType | complexType | group | attributeGroup))*", localName2}, firstVisibleChildElement2);
                            }
                            firstVisibleChildElement2 = DOMUtil.getNextVisibleSiblingElement(firstVisibleChildElement2, this.fHiddenNodes);
                        }
                        xSDocumentInfo.restoreNSSupport();
                    } else if (localName.equals(SchemaSymbols.ELT_ATTRIBUTE)) {
                        this.fAttributeTraverser.traverseGlobal(firstVisibleChildElement, xSDocumentInfo, grammar);
                    } else if (localName.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP)) {
                        this.fAttributeGroupTraverser.traverseGlobal(firstVisibleChildElement, xSDocumentInfo, grammar);
                    } else if (localName.equals(SchemaSymbols.ELT_COMPLEXTYPE)) {
                        this.fComplexTypeTraverser.traverseGlobal(firstVisibleChildElement, xSDocumentInfo, grammar);
                    } else if (localName.equals(SchemaSymbols.ELT_ELEMENT)) {
                        this.fElementTraverser.traverseGlobal(firstVisibleChildElement, xSDocumentInfo, grammar);
                    } else if (localName.equals(SchemaSymbols.ELT_GROUP)) {
                        this.fGroupTraverser.traverseGlobal(firstVisibleChildElement, xSDocumentInfo, grammar);
                    } else if (localName.equals(SchemaSymbols.ELT_NOTATION)) {
                        this.fNotationTraverser.traverse(firstVisibleChildElement, xSDocumentInfo, grammar);
                    } else if (localName.equals(SchemaSymbols.ELT_SIMPLETYPE)) {
                        this.fSimpleTypeTraverser.traverseGlobal(firstVisibleChildElement, xSDocumentInfo, grammar);
                    } else if (localName.equals(SchemaSymbols.ELT_ANNOTATION)) {
                        grammar.addAnnotation(this.fElementTraverser.traverseAnnotationDecl(firstVisibleChildElement, xSDocumentInfo.getSchemaAttrs(), true, xSDocumentInfo));
                        z = true;
                    } else {
                        reportSchemaError("s4s-elt-invalid-content.1", new Object[]{SchemaSymbols.ELT_SCHEMA, DOMUtil.getLocalName(firstVisibleChildElement)}, firstVisibleChildElement);
                    }
                    firstVisibleChildElement = DOMUtil.getNextVisibleSiblingElement(firstVisibleChildElement, this.fHiddenNodes);
                }
                if (!z && (syntheticAnnotation = DOMUtil.getSyntheticAnnotation(element)) != null) {
                    grammar.addAnnotation(this.fElementTraverser.traverseSyntheticAnnotation(element, syntheticAnnotation, xSDocumentInfo.getSchemaAttrs(), true, xSDocumentInfo));
                }
                if (!(arrayList == null || (annotations = xSDocumentInfo.getAnnotations()) == null)) {
                    arrayList.add(doc2SystemId(element));
                    arrayList.add(annotations);
                }
                xSDocumentInfo.returnSchemaAttrs();
                DOMUtil.setHidden(element, this.fHiddenNodes);
                Vector<XSDocumentInfo> vector = this.fDependencyMap.get(xSDocumentInfo);
                for (int i = 0; i < vector.size(); i++) {
                    stack.push(vector.elementAt(i));
                }
            }
        }
    }

    private final boolean needReportTNSError(String str) {
        Vector vector = this.fReportedTNS;
        if (vector == null) {
            this.fReportedTNS = new Vector();
        } else if (vector.contains(str)) {
            return false;
        }
        this.fReportedTNS.addElement(str);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void addGlobalAttributeDecl(XSAttributeDecl xSAttributeDecl) {
        String str;
        String namespace = xSAttributeDecl.getNamespace();
        if (namespace == null || namespace.length() == 0) {
            str = "," + xSAttributeDecl.getName();
        } else {
            str = namespace + "," + xSAttributeDecl.getName();
        }
        if (this.fGlobalAttrDecls.get(str) == null) {
            this.fGlobalAttrDecls.put(str, xSAttributeDecl);
        }
    }

    /* access modifiers changed from: package-private */
    public void addGlobalAttributeGroupDecl(XSAttributeGroupDecl xSAttributeGroupDecl) {
        String str;
        String namespace = xSAttributeGroupDecl.getNamespace();
        if (namespace == null || namespace.length() == 0) {
            str = "," + xSAttributeGroupDecl.getName();
        } else {
            str = namespace + "," + xSAttributeGroupDecl.getName();
        }
        if (this.fGlobalAttrGrpDecls.get(str) == null) {
            this.fGlobalAttrGrpDecls.put(str, xSAttributeGroupDecl);
        }
    }

    /* access modifiers changed from: package-private */
    public void addGlobalElementDecl(XSElementDecl xSElementDecl) {
        String str;
        String namespace = xSElementDecl.getNamespace();
        if (namespace == null || namespace.length() == 0) {
            str = "," + xSElementDecl.getName();
        } else {
            str = namespace + "," + xSElementDecl.getName();
        }
        if (this.fGlobalElemDecls.get(str) == null) {
            this.fGlobalElemDecls.put(str, xSElementDecl);
        }
    }

    /* access modifiers changed from: package-private */
    public void addGlobalGroupDecl(XSGroupDecl xSGroupDecl) {
        String str;
        String namespace = xSGroupDecl.getNamespace();
        if (namespace == null || namespace.length() == 0) {
            str = "," + xSGroupDecl.getName();
        } else {
            str = namespace + "," + xSGroupDecl.getName();
        }
        if (this.fGlobalGroupDecls.get(str) == null) {
            this.fGlobalGroupDecls.put(str, xSGroupDecl);
        }
    }

    /* access modifiers changed from: package-private */
    public void addGlobalNotationDecl(XSNotationDecl xSNotationDecl) {
        String str;
        String namespace = xSNotationDecl.getNamespace();
        if (namespace == null || namespace.length() == 0) {
            str = "," + xSNotationDecl.getName();
        } else {
            str = namespace + "," + xSNotationDecl.getName();
        }
        if (this.fGlobalNotationDecls.get(str) == null) {
            this.fGlobalNotationDecls.put(str, xSNotationDecl);
        }
    }

    /* access modifiers changed from: package-private */
    public void addGlobalTypeDecl(XSTypeDefinition xSTypeDefinition) {
        String str;
        String namespace = xSTypeDefinition.getNamespace();
        if (namespace == null || namespace.length() == 0) {
            str = "," + xSTypeDefinition.getName();
        } else {
            str = namespace + "," + xSTypeDefinition.getName();
        }
        if (this.fGlobalTypeDecls.get(str) == null) {
            this.fGlobalTypeDecls.put(str, xSTypeDefinition);
        }
    }

    /* access modifiers changed from: package-private */
    public void addIDConstraintDecl(IdentityConstraint identityConstraint) {
        String str;
        String namespace = identityConstraint.getNamespace();
        if (namespace == null || namespace.length() == 0) {
            str = "," + identityConstraint.getIdentityConstraintName();
        } else {
            str = namespace + "," + identityConstraint.getIdentityConstraintName();
        }
        if (this.fGlobalIDConstraintDecls.get(str) == null) {
            this.fGlobalIDConstraintDecls.put(str, identityConstraint);
        }
    }

    private XSAttributeDecl getGlobalAttributeDecl(String str) {
        return (XSAttributeDecl) this.fGlobalAttrDecls.get(str);
    }

    private XSAttributeGroupDecl getGlobalAttributeGroupDecl(String str) {
        return (XSAttributeGroupDecl) this.fGlobalAttrGrpDecls.get(str);
    }

    private XSElementDecl getGlobalElementDecl(String str) {
        return (XSElementDecl) this.fGlobalElemDecls.get(str);
    }

    private XSGroupDecl getGlobalGroupDecl(String str) {
        return (XSGroupDecl) this.fGlobalGroupDecls.get(str);
    }

    private XSNotationDecl getGlobalNotationDecl(String str) {
        return (XSNotationDecl) this.fGlobalNotationDecls.get(str);
    }

    private XSTypeDefinition getGlobalTypeDecl(String str) {
        return (XSTypeDefinition) this.fGlobalTypeDecls.get(str);
    }

    private IdentityConstraint getIDConstraintDecl(String str) {
        return (IdentityConstraint) this.fGlobalIDConstraintDecls.get(str);
    }

    /* access modifiers changed from: protected */
    public Object getGlobalDecl(XSDocumentInfo xSDocumentInfo, int i, QName qName, Element element) {
        String str;
        Element element2;
        XSDocumentInfo xSDocumentInfo2;
        String str2;
        XSTypeDefinition globalTypeDecl;
        if (qName.uri != null && qName.uri == SchemaSymbols.URI_SCHEMAFORSCHEMA && i == 7 && (globalTypeDecl = SchemaGrammar.SG_SchemaNS.getGlobalTypeDecl(qName.localpart)) != null) {
            return globalTypeDecl;
        }
        String str3 = "src-resolve.4.1";
        if (!xSDocumentInfo.isAllowedNS(qName.uri) && xSDocumentInfo.needReportTNSError(qName.uri)) {
            if (qName.uri == null) {
                str2 = str3;
            } else {
                str2 = "src-resolve.4.2";
            }
            reportSchemaError(str2, new Object[]{this.fDoc2SystemId.get(xSDocumentInfo.fSchemaElement), qName.uri, qName.rawname}, element);
        }
        SchemaGrammar grammar = this.fGrammarBucket.getGrammar(qName.uri);
        if (grammar == null) {
            if (needReportTNSError(qName.uri)) {
                reportSchemaError("src-resolve", new Object[]{qName.rawname, COMP_TYPE[i]}, element);
            }
            return null;
        }
        Object globalDeclFromGrammar = getGlobalDeclFromGrammar(grammar, i, qName.localpart);
        if (qName.uri == null) {
            str = "," + qName.localpart;
        } else {
            str = qName.uri + "," + qName.localpart;
        }
        if (this.fTolerateDuplicates) {
            Object globalDecl = getGlobalDecl(str, i);
            if (globalDecl != null) {
                return globalDecl;
            }
        } else if (globalDeclFromGrammar != null) {
            return globalDeclFromGrammar;
        }
        switch (i) {
            case 1:
                element2 = getElementFromMap(this.fUnparsedAttributeRegistry, str);
                xSDocumentInfo2 = getDocInfoFromMap(this.fUnparsedAttributeRegistrySub, str);
                break;
            case 2:
                element2 = getElementFromMap(this.fUnparsedAttributeGroupRegistry, str);
                xSDocumentInfo2 = getDocInfoFromMap(this.fUnparsedAttributeGroupRegistrySub, str);
                break;
            case 3:
                element2 = getElementFromMap(this.fUnparsedElementRegistry, str);
                xSDocumentInfo2 = getDocInfoFromMap(this.fUnparsedElementRegistrySub, str);
                break;
            case 4:
                element2 = getElementFromMap(this.fUnparsedGroupRegistry, str);
                xSDocumentInfo2 = getDocInfoFromMap(this.fUnparsedGroupRegistrySub, str);
                break;
            case 5:
                element2 = getElementFromMap(this.fUnparsedIdentityConstraintRegistry, str);
                xSDocumentInfo2 = getDocInfoFromMap(this.fUnparsedIdentityConstraintRegistrySub, str);
                break;
            case 6:
                element2 = getElementFromMap(this.fUnparsedNotationRegistry, str);
                xSDocumentInfo2 = getDocInfoFromMap(this.fUnparsedNotationRegistrySub, str);
                break;
            case 7:
                element2 = getElementFromMap(this.fUnparsedTypeRegistry, str);
                xSDocumentInfo2 = getDocInfoFromMap(this.fUnparsedTypeRegistrySub, str);
                break;
            default:
                reportSchemaError("Internal-Error", new Object[]{"XSDHandler asked to locate component of type " + i + "; it does not recognize this type!"}, element);
                xSDocumentInfo2 = null;
                element2 = null;
                break;
        }
        if (element2 == null) {
            if (globalDeclFromGrammar == null) {
                reportSchemaError("src-resolve", new Object[]{qName.rawname, COMP_TYPE[i]}, element);
            }
            return globalDeclFromGrammar;
        }
        XSDocumentInfo findXSDocumentForDecl = findXSDocumentForDecl(xSDocumentInfo, element2, xSDocumentInfo2);
        if (findXSDocumentForDecl == null) {
            if (globalDeclFromGrammar == null) {
                if (qName.uri != null) {
                    str3 = "src-resolve.4.2";
                }
                reportSchemaError(str3, new Object[]{this.fDoc2SystemId.get(xSDocumentInfo.fSchemaElement), qName.uri, qName.rawname}, element);
            }
            return globalDeclFromGrammar;
        } else if (!DOMUtil.isHidden(element2, this.fHiddenNodes)) {
            return traverseGlobalDecl(i, element2, findXSDocumentForDecl, grammar);
        } else {
            if (globalDeclFromGrammar == null) {
                String str4 = CIRCULAR_CODES[i];
                if (i == 7 && SchemaSymbols.ELT_COMPLEXTYPE.equals(DOMUtil.getLocalName(element2))) {
                    str4 = "ct-props-correct.3";
                }
                reportSchemaError(str4, new Object[]{qName.prefix + ":" + qName.localpart}, element);
            }
            return globalDeclFromGrammar;
        }
    }

    /* access modifiers changed from: protected */
    public Object getGlobalDecl(String str, int i) {
        switch (i) {
            case 1:
                return getGlobalAttributeDecl(str);
            case 2:
                return getGlobalAttributeGroupDecl(str);
            case 3:
                return getGlobalElementDecl(str);
            case 4:
                return getGlobalGroupDecl(str);
            case 5:
                return getIDConstraintDecl(str);
            case 6:
                return getGlobalNotationDecl(str);
            case 7:
                return getGlobalTypeDecl(str);
            default:
                return null;
        }
    }

    /* access modifiers changed from: protected */
    public Object getGlobalDeclFromGrammar(SchemaGrammar schemaGrammar, int i, String str) {
        switch (i) {
            case 1:
                return schemaGrammar.getGlobalAttributeDecl(str);
            case 2:
                return schemaGrammar.getGlobalAttributeGroupDecl(str);
            case 3:
                return schemaGrammar.getGlobalElementDecl(str);
            case 4:
                return schemaGrammar.getGlobalGroupDecl(str);
            case 5:
                return schemaGrammar.getIDConstraintDecl(str);
            case 6:
                return schemaGrammar.getGlobalNotationDecl(str);
            case 7:
                return schemaGrammar.getGlobalTypeDecl(str);
            default:
                return null;
        }
    }

    /* access modifiers changed from: protected */
    public Object getGlobalDeclFromGrammar(SchemaGrammar schemaGrammar, int i, String str, String str2) {
        switch (i) {
            case 1:
                return schemaGrammar.getGlobalAttributeDecl(str, str2);
            case 2:
                return schemaGrammar.getGlobalAttributeGroupDecl(str, str2);
            case 3:
                return schemaGrammar.getGlobalElementDecl(str, str2);
            case 4:
                return schemaGrammar.getGlobalGroupDecl(str, str2);
            case 5:
                return schemaGrammar.getIDConstraintDecl(str, str2);
            case 6:
                return schemaGrammar.getGlobalNotationDecl(str, str2);
            case 7:
                return schemaGrammar.getGlobalTypeDecl(str, str2);
            default:
                return null;
        }
    }

    /* access modifiers changed from: protected */
    public Object traverseGlobalDecl(int i, Element element, XSDocumentInfo xSDocumentInfo, SchemaGrammar schemaGrammar) {
        Map map;
        DOMUtil.setHidden(element, this.fHiddenNodes);
        Element parent = DOMUtil.getParent(element);
        Object obj = null;
        xSDocumentInfo.backupNSSupport((!DOMUtil.getLocalName(parent).equals(SchemaSymbols.ELT_REDEFINE) || (map = this.fRedefine2NSSupport) == null) ? null : (SchemaNamespaceSupport) map.get(parent));
        if (i == 1) {
            obj = this.fAttributeTraverser.traverseGlobal(element, xSDocumentInfo, schemaGrammar);
        } else if (i == 2) {
            obj = this.fAttributeGroupTraverser.traverseGlobal(element, xSDocumentInfo, schemaGrammar);
        } else if (i == 3) {
            obj = this.fElementTraverser.traverseGlobal(element, xSDocumentInfo, schemaGrammar);
        } else if (i == 4) {
            obj = this.fGroupTraverser.traverseGlobal(element, xSDocumentInfo, schemaGrammar);
        } else if (i == 6) {
            obj = this.fNotationTraverser.traverse(element, xSDocumentInfo, schemaGrammar);
        } else if (i == 7) {
            obj = DOMUtil.getLocalName(element).equals(SchemaSymbols.ELT_COMPLEXTYPE) ? this.fComplexTypeTraverser.traverseGlobal(element, xSDocumentInfo, schemaGrammar) : this.fSimpleTypeTraverser.traverseGlobal(element, xSDocumentInfo, schemaGrammar);
        }
        xSDocumentInfo.restoreNSSupport();
        return obj;
    }

    public String schemaDocument2SystemId(XSDocumentInfo xSDocumentInfo) {
        return this.fDoc2SystemId.get(xSDocumentInfo.fSchemaElement);
    }

    /* access modifiers changed from: package-private */
    public Object getGrpOrAttrGrpRedefinedByRestriction(int i, QName qName, XSDocumentInfo xSDocumentInfo, Element element) {
        String str;
        String str2;
        if (qName.uri != null) {
            str = qName.uri + "," + qName.localpart;
        } else {
            str = "," + qName.localpart;
        }
        if (i == 2) {
            str2 = (String) this.fRedefinedRestrictedAttributeGroupRegistry.get(str);
        } else if (i != 4) {
            return null;
        } else {
            str2 = (String) this.fRedefinedRestrictedGroupRegistry.get(str);
        }
        if (str2 == null) {
            return null;
        }
        int indexOf = str2.indexOf(",");
        Object globalDecl = getGlobalDecl(xSDocumentInfo, i, new QName(XMLSymbols.EMPTY_STRING, str2.substring(indexOf + 1), str2.substring(indexOf), indexOf == 0 ? null : str2.substring(0, indexOf)), element);
        if (globalDecl != null) {
            return globalDecl;
        }
        if (i == 2) {
            reportSchemaError("src-redefine.7.2.1", new Object[]{qName.localpart}, element);
        } else if (i == 4) {
            reportSchemaError("src-redefine.6.2.1", new Object[]{qName.localpart}, element);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void resolveKeyRefs() {
        for (int i = 0; i < this.fKeyrefStackPos; i++) {
            XSDocumentInfo xSDocumentInfo = this.fKeyrefsMapXSDocumentInfo[i];
            xSDocumentInfo.fNamespaceSupport.makeGlobal();
            xSDocumentInfo.fNamespaceSupport.setEffectiveContext(this.fKeyrefNamespaceContext[i]);
            SchemaGrammar grammar = this.fGrammarBucket.getGrammar(xSDocumentInfo.fTargetNamespace);
            DOMUtil.setHidden(this.fKeyrefs[i], this.fHiddenNodes);
            this.fKeyrefTraverser.traverse(this.fKeyrefs[i], this.fKeyrefElems[i], xSDocumentInfo, grammar);
        }
    }

    /* access modifiers changed from: protected */
    public Map getIDRegistry() {
        return this.fUnparsedIdentityConstraintRegistry;
    }

    /* access modifiers changed from: protected */
    public Map getIDRegistry_sub() {
        return this.fUnparsedIdentityConstraintRegistrySub;
    }

    /* access modifiers changed from: protected */
    public void storeKeyRef(Element element, XSDocumentInfo xSDocumentInfo, XSElementDecl xSElementDecl) {
        StringBuilder sb;
        String attrValue = DOMUtil.getAttrValue(element, SchemaSymbols.ATT_NAME);
        if (attrValue.length() != 0) {
            if (xSDocumentInfo.fTargetNamespace == null) {
                sb = new StringBuilder();
            } else {
                sb = new StringBuilder();
                sb.append(xSDocumentInfo.fTargetNamespace);
            }
            sb.append(",");
            sb.append(attrValue);
            checkForDuplicateNames(sb.toString(), 5, this.fUnparsedIdentityConstraintRegistry, this.fUnparsedIdentityConstraintRegistrySub, element, xSDocumentInfo);
        }
        int i = this.fKeyrefStackPos;
        Element[] elementArr = this.fKeyrefs;
        if (i == elementArr.length) {
            Element[] elementArr2 = new Element[(i + 2)];
            System.arraycopy(elementArr, 0, elementArr2, 0, i);
            this.fKeyrefs = elementArr2;
            int i2 = this.fKeyrefStackPos;
            XSElementDecl[] xSElementDeclArr = new XSElementDecl[(i2 + 2)];
            System.arraycopy(this.fKeyrefElems, 0, xSElementDeclArr, 0, i2);
            this.fKeyrefElems = xSElementDeclArr;
            int i3 = this.fKeyrefStackPos;
            String[][] strArr = new String[(i3 + 2)][];
            System.arraycopy(this.fKeyrefNamespaceContext, 0, strArr, 0, i3);
            this.fKeyrefNamespaceContext = strArr;
            int i4 = this.fKeyrefStackPos;
            XSDocumentInfo[] xSDocumentInfoArr = new XSDocumentInfo[(i4 + 2)];
            System.arraycopy(this.fKeyrefsMapXSDocumentInfo, 0, xSDocumentInfoArr, 0, i4);
            this.fKeyrefsMapXSDocumentInfo = xSDocumentInfoArr;
        }
        Element[] elementArr3 = this.fKeyrefs;
        int i5 = this.fKeyrefStackPos;
        elementArr3[i5] = element;
        this.fKeyrefElems[i5] = xSElementDecl;
        this.fKeyrefNamespaceContext[i5] = xSDocumentInfo.fNamespaceSupport.getEffectiveLocalContext();
        XSDocumentInfo[] xSDocumentInfoArr2 = this.fKeyrefsMapXSDocumentInfo;
        int i6 = this.fKeyrefStackPos;
        this.fKeyrefStackPos = i6 + 1;
        xSDocumentInfoArr2[i6] = xSDocumentInfo;
    }

    private Element resolveSchema(XSDDescription xSDDescription, boolean z, Element element, boolean z2) {
        XMLInputSource xMLInputSource;
        Map<String, XMLSchemaLoader.LocationArray> map;
        if (z2) {
            try {
                map = this.fLocationPairs;
            } catch (IOException unused) {
                if (z) {
                    reportSchemaError("schema_reference.4", new Object[]{xSDDescription.getLocationHints()[0]}, element);
                } else {
                    reportSchemaWarning("schema_reference.4", new Object[]{xSDDescription.getLocationHints()[0]}, element);
                }
                xMLInputSource = null;
            }
        } else {
            map = Collections.emptyMap();
        }
        xMLInputSource = XMLSchemaLoader.resolveDocument(xSDDescription, map, this.fEntityManager);
        if (xMLInputSource instanceof DOMInputSource) {
            return getSchemaDocument(xSDDescription.getTargetNamespace(), (DOMInputSource) xMLInputSource, z, xSDDescription.getContextType(), element);
        }
        if (xMLInputSource instanceof SAXInputSource) {
            return getSchemaDocument(xSDDescription.getTargetNamespace(), (SAXInputSource) xMLInputSource, z, xSDDescription.getContextType(), element);
        }
        if (xMLInputSource instanceof StAXInputSource) {
            return getSchemaDocument(xSDDescription.getTargetNamespace(), (StAXInputSource) xMLInputSource, z, xSDDescription.getContextType(), element);
        }
        if (xMLInputSource instanceof XSInputSource) {
            return getSchemaDocument((XSInputSource) xMLInputSource, xSDDescription);
        }
        return getSchemaDocument(xSDDescription.getTargetNamespace(), xMLInputSource, z, xSDDescription.getContextType(), element);
    }

    private Element resolveSchema(XMLInputSource xMLInputSource, XSDDescription xSDDescription, boolean z, Element element) {
        if (xMLInputSource instanceof DOMInputSource) {
            return getSchemaDocument(xSDDescription.getTargetNamespace(), (DOMInputSource) xMLInputSource, z, xSDDescription.getContextType(), element);
        }
        if (xMLInputSource instanceof SAXInputSource) {
            return getSchemaDocument(xSDDescription.getTargetNamespace(), (SAXInputSource) xMLInputSource, z, xSDDescription.getContextType(), element);
        }
        if (xMLInputSource instanceof StAXInputSource) {
            return getSchemaDocument(xSDDescription.getTargetNamespace(), (StAXInputSource) xMLInputSource, z, xSDDescription.getContextType(), element);
        }
        if (xMLInputSource instanceof XSInputSource) {
            return getSchemaDocument((XSInputSource) xMLInputSource, xSDDescription);
        }
        return getSchemaDocument(xSDDescription.getTargetNamespace(), xMLInputSource, z, xSDDescription.getContextType(), element);
    }

    private XMLInputSource resolveSchemaSource(XSDDescription xSDDescription, boolean z, Element element, boolean z2) {
        Map<String, XMLSchemaLoader.LocationArray> map;
        if (z2) {
            try {
                map = this.fLocationPairs;
            } catch (IOException unused) {
                if (z) {
                    reportSchemaError("schema_reference.4", new Object[]{xSDDescription.getLocationHints()[0]}, element);
                } else {
                    reportSchemaWarning("schema_reference.4", new Object[]{xSDDescription.getLocationHints()[0]}, element);
                }
                return null;
            }
        } else {
            map = Collections.emptyMap();
        }
        return XMLSchemaLoader.resolveDocument(xSDDescription, map, this.fEntityManager);
    }

    private Element getSchemaDocument(String str, XMLInputSource xMLInputSource, boolean z, short s, Element element) {
        IOException iOException;
        boolean z2;
        XSDKey xSDKey;
        String str2;
        String checkAccess;
        Element element2 = null;
        if (xMLInputSource != null) {
            try {
                if (!(xMLInputSource.getSystemId() == null && xMLInputSource.getByteStream() == null && xMLInputSource.getCharacterStream() == null)) {
                    if (s != 3) {
                        str2 = XMLEntityManager.expandSystemId(xMLInputSource.getSystemId(), xMLInputSource.getBaseSystemId(), false);
                        xSDKey = new XSDKey(str2, s, str);
                        Element element3 = this.fTraversed.get(xSDKey);
                        if (element3 != null) {
                            this.fLastSchemaWasDuplicate = true;
                            return element3;
                        } else if ((s == 2 || s == 0 || s == 1) && (checkAccess = SecuritySupport.checkAccess(str2, this.fAccessExternalSchema, "all")) != null) {
                            reportSchemaFatalError("schema_reference.access", new Object[]{SecuritySupport.sanitizePath(str2), checkAccess}, element);
                        }
                    } else {
                        str2 = null;
                        xSDKey = null;
                    }
                    this.fSchemaParser.parse(xMLInputSource);
                    Document document = this.fSchemaParser.getDocument();
                    if (document != null) {
                        element2 = DOMUtil.getRoot(document);
                    }
                    return getSchemaDocument0(xSDKey, str2, element2);
                }
            } catch (IOException e) {
                iOException = e;
                z2 = true;
            }
        }
        z2 = false;
        iOException = null;
        return getSchemaDocument1(z, z2, xMLInputSource, element, iOException);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004d, code lost:
        r10 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x006f, code lost:
        r10 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00c4, code lost:
        r10 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00c5, code lost:
        r5 = r10;
        r2 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00c8, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00cd, code lost:
        throw ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers.XSDHandler.SAX2XNIUtil.createXNIException0(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00ce, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00d3, code lost:
        throw ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers.XSDHandler.SAX2XNIUtil.createXMLParseException0(r9);
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00c4 A[ExcHandler: IOException (r10v1 'e' java.io.IOException A[CUSTOM_DECLARE]), Splitter:B:2:0x000f] */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x00ce A[ExcHandler: SAXParseException (r9v2 'e' ohos.org.xml.sax.SAXParseException A[CUSTOM_DECLARE]), Splitter:B:2:0x000f] */
    private Element getSchemaDocument(String str, SAXInputSource sAXInputSource, boolean z, short s, Element element) {
        XSDKey xSDKey;
        String str2;
        boolean z2;
        XMLReader xMLReader = sAXInputSource.getXMLReader();
        InputSource inputSource = sAXInputSource.getInputSource();
        boolean z3 = false;
        IOException iOException = null;
        if (inputSource != null) {
            try {
                if (!(inputSource.getSystemId() == null && inputSource.getByteStream() == null && inputSource.getCharacterStream() == null)) {
                    if (s != 3) {
                        str2 = XMLEntityManager.expandSystemId(inputSource.getSystemId(), sAXInputSource.getBaseSystemId(), false);
                        xSDKey = new XSDKey(str2, s, str);
                        Element element2 = this.fTraversed.get(xSDKey);
                        if (element2 != null) {
                            this.fLastSchemaWasDuplicate = true;
                            return element2;
                        }
                    } else {
                        str2 = null;
                        xSDKey = null;
                    }
                    if (xMLReader != null) {
                        z2 = xMLReader.getFeature(NAMESPACE_PREFIXES);
                    } else {
                        xMLReader = JdkXmlUtils.getXMLReader(this.fOverrideDefaultParser, this.fSecurityManager.isSecureProcessing());
                        xMLReader.setFeature(NAMESPACE_PREFIXES, true);
                        if ((xMLReader instanceof SAXParser) && this.fSecurityManager != null) {
                            xMLReader.setProperty("http://apache.org/xml/properties/security-manager", this.fSecurityManager);
                        }
                        z2 = true;
                        try {
                            xMLReader.setProperty("http://ohos.javax.xml.XMLConstants/property/accessExternalDTD", this.fAccessExternalDTD);
                        } catch (SAXNotRecognizedException e) {
                            XMLSecurityManager.printWarning(xMLReader.getClass().getName(), "http://ohos.javax.xml.XMLConstants/property/accessExternalDTD", e);
                        }
                    }
                    z3 = xMLReader.getFeature(STRING_INTERNING);
                    if (this.fXSContentHandler == null) {
                        this.fXSContentHandler = new SchemaContentHandler();
                    }
                    this.fXSContentHandler.reset(this.fSchemaParser, this.fSymbolTable, z2, z3);
                    xMLReader.setContentHandler(this.fXSContentHandler);
                    xMLReader.setErrorHandler(this.fErrorReporter.getSAXErrorHandler());
                    xMLReader.parse(inputSource);
                    try {
                        xMLReader.setContentHandler((ContentHandler) null);
                        xMLReader.setErrorHandler((ErrorHandler) null);
                    } catch (Exception unused) {
                    }
                    Document document = this.fXSContentHandler.getDocument();
                    if (document != null) {
                        iOException = DOMUtil.getRoot(document);
                    }
                    return getSchemaDocument0(xSDKey, str2, iOException);
                }
            } catch (SAXException unused2) {
            } catch (SAXParseException e2) {
            } catch (IOException e3) {
            }
        }
        boolean z4 = false;
        return getSchemaDocument1(z, z4, sAXInputSource, element, iOException);
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0021  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0065  */
    private Element getSchemaDocument(String str, DOMInputSource dOMInputSource, boolean z, short s, Element element) {
        short s2;
        Element element2;
        IOException iOException;
        boolean z2;
        String str2;
        Element element3;
        Node parentNode;
        Document node = dOMInputSource.getNode();
        XSDKey xSDKey = null;
        if (node != null) {
            s2 = node.getNodeType();
            if (s2 == 9) {
                element2 = DOMUtil.getRoot(node);
            } else if (s2 == 1) {
                element2 = (Element) node;
            }
            if (element2 == null) {
                if (s != 3) {
                    try {
                        str2 = XMLEntityManager.expandSystemId(dOMInputSource.getSystemId(), dOMInputSource.getBaseSystemId(), false);
                        boolean z3 = s2 == 9;
                        if (!z3 && (parentNode = element2.getParentNode()) != null) {
                            z3 = parentNode.getNodeType() == 9;
                        }
                        if (z3 && (element3 = this.fTraversed.get((xSDKey = new XSDKey(str2, s, str)))) != null) {
                            this.fLastSchemaWasDuplicate = true;
                            return element3;
                        }
                    } catch (IOException e) {
                        iOException = e;
                        z2 = true;
                    }
                } else {
                    str2 = null;
                }
                return getSchemaDocument0(xSDKey, str2, element2);
            }
            iOException = null;
            z2 = false;
            return getSchemaDocument1(z, z2, dOMInputSource, element, iOException);
        }
        s2 = -1;
        element2 = null;
        if (element2 == null) {
        }
        return getSchemaDocument1(z, z2, dOMInputSource, element, iOException);
    }

    private Element getSchemaDocument(String str, StAXInputSource stAXInputSource, boolean z, short s, Element element) {
        XSDKey xSDKey;
        String str2;
        try {
            boolean shouldConsumeRemainingContent = stAXInputSource.shouldConsumeRemainingContent();
            XMLStreamReader xMLStreamReader = stAXInputSource.getXMLStreamReader();
            XMLEventReader xMLEventReader = stAXInputSource.getXMLEventReader();
            Element element2 = null;
            if (s != 3) {
                boolean z2 = false;
                str2 = XMLEntityManager.expandSystemId(stAXInputSource.getSystemId(), stAXInputSource.getBaseSystemId(), false);
                if (shouldConsumeRemainingContent) {
                    z2 = shouldConsumeRemainingContent;
                } else if (xMLStreamReader == null) {
                    z2 = xMLEventReader.peek().isStartDocument();
                } else if (xMLStreamReader.getEventType() == 7) {
                    z2 = true;
                }
                if (z2) {
                    xSDKey = new XSDKey(str2, s, str);
                    Element element3 = this.fTraversed.get(xSDKey);
                    if (element3 != null) {
                        this.fLastSchemaWasDuplicate = true;
                        return element3;
                    }
                } else {
                    xSDKey = null;
                }
            } else {
                str2 = null;
                xSDKey = null;
            }
            if (this.fStAXSchemaParser == null) {
                this.fStAXSchemaParser = new StAXSchemaParser();
            }
            this.fStAXSchemaParser.reset(this.fSchemaParser, this.fSymbolTable);
            if (xMLStreamReader != null) {
                this.fStAXSchemaParser.parse(xMLStreamReader);
                if (shouldConsumeRemainingContent) {
                    while (xMLStreamReader.hasNext()) {
                        xMLStreamReader.next();
                    }
                }
            } else {
                this.fStAXSchemaParser.parse(xMLEventReader);
                if (shouldConsumeRemainingContent) {
                    while (xMLEventReader.hasNext()) {
                        xMLEventReader.nextEvent();
                    }
                }
            }
            Document document = this.fStAXSchemaParser.getDocument();
            if (document != null) {
                element2 = DOMUtil.getRoot(document);
            }
            return getSchemaDocument0(xSDKey, str2, element2);
        } catch (XMLStreamException e) {
            StAXLocationWrapper stAXLocationWrapper = new StAXLocationWrapper();
            stAXLocationWrapper.setLocation(e.getLocation());
            throw new XMLParseException(stAXLocationWrapper, e.getMessage(), e);
        } catch (IOException e2) {
            return getSchemaDocument1(z, true, stAXInputSource, element, e2);
        }
    }

    private Element getSchemaDocument0(XSDKey xSDKey, String str, Element element) {
        if (xSDKey != null) {
            this.fTraversed.put(xSDKey, element);
        }
        if (str != null) {
            this.fDoc2SystemId.put(element, str);
        }
        this.fLastSchemaWasDuplicate = false;
        return element;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0020: APUT  (r4v2 java.lang.Object[]), (0 ??[int, short, byte, char]), (r5v2 java.lang.String) */
    private Element getSchemaDocument1(boolean z, boolean z2, XMLInputSource xMLInputSource, Element element, IOException iOException) {
        String str;
        if (z) {
            if (z2) {
                reportSchemaError("schema_reference.4", new Object[]{xMLInputSource.getSystemId()}, element, iOException);
            } else {
                Object[] objArr = new Object[1];
                if (xMLInputSource == null) {
                    str = "";
                } else {
                    str = xMLInputSource.getSystemId();
                }
                objArr[0] = str;
                reportSchemaError("schema_reference.4", objArr, element, iOException);
            }
        } else if (z2) {
            reportSchemaWarning("schema_reference.4", new Object[]{xMLInputSource.getSystemId()}, element, iOException);
        }
        this.fLastSchemaWasDuplicate = false;
        return null;
    }

    private Element getSchemaDocument(XSInputSource xSInputSource, XSDDescription xSDDescription) {
        SchemaGrammar[] grammars = xSInputSource.getGrammars();
        short contextType = xSDDescription.getContextType();
        if (grammars == null || grammars.length <= 0) {
            XSObject[] components = xSInputSource.getComponents();
            if (components == null || components.length <= 0) {
                return null;
            }
            HashMap hashMap = new HashMap();
            Vector expandComponents = expandComponents(components, hashMap);
            if (!this.fNamespaceGrowth && !canAddComponents(expandComponents)) {
                return null;
            }
            addGlobalComponents(expandComponents, hashMap);
            if (contextType != 3) {
                return null;
            }
            xSDDescription.setTargetNamespace(components[0].getNamespace());
            return null;
        }
        Vector expandGrammars = expandGrammars(grammars);
        if (!this.fNamespaceGrowth && existingGrammars(expandGrammars)) {
            return null;
        }
        addGrammars(expandGrammars);
        if (contextType != 3) {
            return null;
        }
        xSDDescription.setTargetNamespace(grammars[0].getTargetNamespace());
        return null;
    }

    private Vector expandGrammars(SchemaGrammar[] schemaGrammarArr) {
        Vector vector = new Vector();
        for (int i = 0; i < schemaGrammarArr.length; i++) {
            if (!vector.contains(schemaGrammarArr[i])) {
                vector.add(schemaGrammarArr[i]);
            }
        }
        for (int i2 = 0; i2 < vector.size(); i2++) {
            Vector importedGrammars = ((SchemaGrammar) vector.elementAt(i2)).getImportedGrammars();
            if (importedGrammars != null) {
                for (int size = importedGrammars.size() - 1; size >= 0; size--) {
                    SchemaGrammar schemaGrammar = (SchemaGrammar) importedGrammars.elementAt(size);
                    if (!vector.contains(schemaGrammar)) {
                        vector.addElement(schemaGrammar);
                    }
                }
            }
        }
        return vector;
    }

    private boolean existingGrammars(Vector vector) {
        int size = vector.size();
        XSDDescription xSDDescription = new XSDDescription();
        for (int i = 0; i < size; i++) {
            xSDDescription.setNamespace(((SchemaGrammar) vector.elementAt(i)).getTargetNamespace());
            if (findGrammar(xSDDescription, false) != null) {
                return true;
            }
        }
        return false;
    }

    private boolean canAddComponents(Vector vector) {
        int size = vector.size();
        XSDDescription xSDDescription = new XSDDescription();
        for (int i = 0; i < size; i++) {
            if (!canAddComponent((XSObject) vector.elementAt(i), xSDDescription)) {
                return false;
            }
        }
        return true;
    }

    private boolean canAddComponent(XSObject xSObject, XSDDescription xSDDescription) {
        xSDDescription.setNamespace(xSObject.getNamespace());
        SchemaGrammar findGrammar = findGrammar(xSDDescription, false);
        if (findGrammar == null) {
            return true;
        }
        if (findGrammar.isImmutable()) {
            return false;
        }
        short type = xSObject.getType();
        String name = xSObject.getName();
        if (type != 1) {
            if (type != 2) {
                if (type != 3) {
                    if (type != 5) {
                        if (type != 6) {
                            if (type != 11 || findGrammar.getGlobalNotationDecl(name) == xSObject) {
                                return true;
                            }
                        } else if (findGrammar.getGlobalGroupDecl(name) == xSObject) {
                            return true;
                        }
                    } else if (findGrammar.getGlobalAttributeDecl(name) == xSObject) {
                        return true;
                    }
                } else if (findGrammar.getGlobalTypeDecl(name) == xSObject) {
                    return true;
                }
            } else if (findGrammar.getGlobalElementDecl(name) == xSObject) {
                return true;
            }
        } else if (findGrammar.getGlobalAttributeDecl(name) == xSObject) {
            return true;
        }
        return false;
    }

    private void addGrammars(Vector vector) {
        int size = vector.size();
        XSDDescription xSDDescription = new XSDDescription();
        for (int i = 0; i < size; i++) {
            SchemaGrammar schemaGrammar = (SchemaGrammar) vector.elementAt(i);
            xSDDescription.setNamespace(schemaGrammar.getTargetNamespace());
            SchemaGrammar findGrammar = findGrammar(xSDDescription, this.fNamespaceGrowth);
            if (schemaGrammar != findGrammar) {
                addGrammarComponents(schemaGrammar, findGrammar);
            }
        }
    }

    private void addGrammarComponents(SchemaGrammar schemaGrammar, SchemaGrammar schemaGrammar2) {
        if (schemaGrammar2 == null) {
            createGrammarFrom(schemaGrammar);
            return;
        }
        if (schemaGrammar2.isImmutable()) {
            schemaGrammar2 = createGrammarFrom(schemaGrammar2);
        }
        addNewGrammarLocations(schemaGrammar, schemaGrammar2);
        addNewImportedGrammars(schemaGrammar, schemaGrammar2);
        addNewGrammarComponents(schemaGrammar, schemaGrammar2);
    }

    private SchemaGrammar createGrammarFrom(SchemaGrammar schemaGrammar) {
        SchemaGrammar schemaGrammar2 = new SchemaGrammar(schemaGrammar);
        this.fGrammarBucket.putGrammar(schemaGrammar2);
        updateImportListWith(schemaGrammar2);
        updateImportListFor(schemaGrammar2);
        return schemaGrammar2;
    }

    private void addNewGrammarLocations(SchemaGrammar schemaGrammar, SchemaGrammar schemaGrammar2) {
        StringList documentLocations = schemaGrammar.getDocumentLocations();
        int size = documentLocations.size();
        StringList documentLocations2 = schemaGrammar2.getDocumentLocations();
        for (int i = 0; i < size; i++) {
            String item = documentLocations.item(i);
            if (!documentLocations2.contains(item)) {
                schemaGrammar2.addDocument(null, item);
            }
        }
    }

    private void addNewImportedGrammars(SchemaGrammar schemaGrammar, SchemaGrammar schemaGrammar2) {
        Vector importedGrammars = schemaGrammar.getImportedGrammars();
        if (importedGrammars != null) {
            Vector importedGrammars2 = schemaGrammar2.getImportedGrammars();
            if (importedGrammars2 == null) {
                schemaGrammar2.setImportedGrammars((Vector) importedGrammars.clone());
            } else {
                updateImportList(importedGrammars, importedGrammars2);
            }
        }
    }

    private void updateImportList(Vector vector, Vector vector2) {
        int size = vector.size();
        for (int i = 0; i < size; i++) {
            SchemaGrammar schemaGrammar = (SchemaGrammar) vector.elementAt(i);
            if (!containedImportedGrammar(vector2, schemaGrammar)) {
                vector2.add(schemaGrammar);
            }
        }
    }

    private void addNewGrammarComponents(SchemaGrammar schemaGrammar, SchemaGrammar schemaGrammar2) {
        schemaGrammar2.resetComponents();
        addGlobalElementDecls(schemaGrammar, schemaGrammar2);
        addGlobalAttributeDecls(schemaGrammar, schemaGrammar2);
        addGlobalAttributeGroupDecls(schemaGrammar, schemaGrammar2);
        addGlobalGroupDecls(schemaGrammar, schemaGrammar2);
        addGlobalTypeDecls(schemaGrammar, schemaGrammar2);
        addGlobalNotationDecls(schemaGrammar, schemaGrammar2);
    }

    private void addGlobalElementDecls(SchemaGrammar schemaGrammar, SchemaGrammar schemaGrammar2) {
        XSNamedMap components = schemaGrammar.getComponents(2);
        int length = components.getLength();
        for (int i = 0; i < length; i++) {
            XSElementDecl xSElementDecl = (XSElementDecl) components.item(i);
            if (schemaGrammar2.getGlobalElementDecl(xSElementDecl.getName()) == null) {
                schemaGrammar2.addGlobalElementDecl(xSElementDecl);
            }
        }
        ObjectList componentsExt = schemaGrammar.getComponentsExt(2);
        int length2 = componentsExt.getLength();
        for (int i2 = 0; i2 < length2; i2 += 2) {
            String str = (String) componentsExt.item(i2);
            int indexOf = str.indexOf(44);
            String substring = str.substring(0, indexOf);
            String substring2 = str.substring(indexOf + 1, str.length());
            XSElementDecl xSElementDecl2 = (XSElementDecl) componentsExt.item(i2 + 1);
            if (schemaGrammar2.getGlobalElementDecl(substring2, substring) == null) {
                schemaGrammar2.addGlobalElementDecl(xSElementDecl2, substring);
            }
        }
    }

    private void addGlobalAttributeDecls(SchemaGrammar schemaGrammar, SchemaGrammar schemaGrammar2) {
        XSNamedMap components = schemaGrammar.getComponents(1);
        int length = components.getLength();
        for (int i = 0; i < length; i++) {
            XSAttributeDecl xSAttributeDecl = (XSAttributeDecl) components.item(i);
            XSAttributeDecl globalAttributeDecl = schemaGrammar2.getGlobalAttributeDecl(xSAttributeDecl.getName());
            if (globalAttributeDecl == null) {
                schemaGrammar2.addGlobalAttributeDecl(xSAttributeDecl);
            } else if (globalAttributeDecl != xSAttributeDecl && !this.fTolerateDuplicates) {
                reportSharingError(xSAttributeDecl.getNamespace(), xSAttributeDecl.getName());
            }
        }
        ObjectList componentsExt = schemaGrammar.getComponentsExt(1);
        int length2 = componentsExt.getLength();
        for (int i2 = 0; i2 < length2; i2 += 2) {
            String str = (String) componentsExt.item(i2);
            int indexOf = str.indexOf(44);
            String substring = str.substring(0, indexOf);
            String substring2 = str.substring(indexOf + 1, str.length());
            XSAttributeDecl xSAttributeDecl2 = (XSAttributeDecl) componentsExt.item(i2 + 1);
            if (schemaGrammar2.getGlobalAttributeDecl(substring2, substring) == null) {
                schemaGrammar2.addGlobalAttributeDecl(xSAttributeDecl2, substring);
            }
        }
    }

    private void addGlobalAttributeGroupDecls(SchemaGrammar schemaGrammar, SchemaGrammar schemaGrammar2) {
        XSNamedMap components = schemaGrammar.getComponents(5);
        int length = components.getLength();
        for (int i = 0; i < length; i++) {
            XSAttributeGroupDecl xSAttributeGroupDecl = (XSAttributeGroupDecl) components.item(i);
            XSAttributeGroupDecl globalAttributeGroupDecl = schemaGrammar2.getGlobalAttributeGroupDecl(xSAttributeGroupDecl.getName());
            if (globalAttributeGroupDecl == null) {
                schemaGrammar2.addGlobalAttributeGroupDecl(xSAttributeGroupDecl);
            } else if (globalAttributeGroupDecl != xSAttributeGroupDecl && !this.fTolerateDuplicates) {
                reportSharingError(xSAttributeGroupDecl.getNamespace(), xSAttributeGroupDecl.getName());
            }
        }
        ObjectList componentsExt = schemaGrammar.getComponentsExt(5);
        int length2 = componentsExt.getLength();
        for (int i2 = 0; i2 < length2; i2 += 2) {
            String str = (String) componentsExt.item(i2);
            int indexOf = str.indexOf(44);
            String substring = str.substring(0, indexOf);
            String substring2 = str.substring(indexOf + 1, str.length());
            XSAttributeGroupDecl xSAttributeGroupDecl2 = (XSAttributeGroupDecl) componentsExt.item(i2 + 1);
            if (schemaGrammar2.getGlobalAttributeGroupDecl(substring2, substring) == null) {
                schemaGrammar2.addGlobalAttributeGroupDecl(xSAttributeGroupDecl2, substring);
            }
        }
    }

    private void addGlobalNotationDecls(SchemaGrammar schemaGrammar, SchemaGrammar schemaGrammar2) {
        XSNamedMap components = schemaGrammar.getComponents(11);
        int length = components.getLength();
        for (int i = 0; i < length; i++) {
            XSNotationDecl xSNotationDecl = (XSNotationDecl) components.item(i);
            XSNotationDecl globalNotationDecl = schemaGrammar2.getGlobalNotationDecl(xSNotationDecl.getName());
            if (globalNotationDecl == null) {
                schemaGrammar2.addGlobalNotationDecl(xSNotationDecl);
            } else if (globalNotationDecl != xSNotationDecl && !this.fTolerateDuplicates) {
                reportSharingError(xSNotationDecl.getNamespace(), xSNotationDecl.getName());
            }
        }
        ObjectList componentsExt = schemaGrammar.getComponentsExt(11);
        int length2 = componentsExt.getLength();
        for (int i2 = 0; i2 < length2; i2 += 2) {
            String str = (String) componentsExt.item(i2);
            int indexOf = str.indexOf(44);
            String substring = str.substring(0, indexOf);
            String substring2 = str.substring(indexOf + 1, str.length());
            XSNotationDecl xSNotationDecl2 = (XSNotationDecl) componentsExt.item(i2 + 1);
            if (schemaGrammar2.getGlobalNotationDecl(substring2, substring) == null) {
                schemaGrammar2.addGlobalNotationDecl(xSNotationDecl2, substring);
            }
        }
    }

    private void addGlobalGroupDecls(SchemaGrammar schemaGrammar, SchemaGrammar schemaGrammar2) {
        XSNamedMap components = schemaGrammar.getComponents(6);
        int length = components.getLength();
        for (int i = 0; i < length; i++) {
            XSGroupDecl xSGroupDecl = (XSGroupDecl) components.item(i);
            XSGroupDecl globalGroupDecl = schemaGrammar2.getGlobalGroupDecl(xSGroupDecl.getName());
            if (globalGroupDecl == null) {
                schemaGrammar2.addGlobalGroupDecl(xSGroupDecl);
            } else if (xSGroupDecl != globalGroupDecl && !this.fTolerateDuplicates) {
                reportSharingError(xSGroupDecl.getNamespace(), xSGroupDecl.getName());
            }
        }
        ObjectList componentsExt = schemaGrammar.getComponentsExt(6);
        int length2 = componentsExt.getLength();
        for (int i2 = 0; i2 < length2; i2 += 2) {
            String str = (String) componentsExt.item(i2);
            int indexOf = str.indexOf(44);
            String substring = str.substring(0, indexOf);
            String substring2 = str.substring(indexOf + 1, str.length());
            XSGroupDecl xSGroupDecl2 = (XSGroupDecl) componentsExt.item(i2 + 1);
            if (schemaGrammar2.getGlobalGroupDecl(substring2, substring) == null) {
                schemaGrammar2.addGlobalGroupDecl(xSGroupDecl2, substring);
            }
        }
    }

    private void addGlobalTypeDecls(SchemaGrammar schemaGrammar, SchemaGrammar schemaGrammar2) {
        XSNamedMap components = schemaGrammar.getComponents(3);
        int length = components.getLength();
        for (int i = 0; i < length; i++) {
            XSTypeDefinition xSTypeDefinition = (XSTypeDefinition) components.item(i);
            XSTypeDefinition globalTypeDecl = schemaGrammar2.getGlobalTypeDecl(xSTypeDefinition.getName());
            if (globalTypeDecl == null) {
                schemaGrammar2.addGlobalTypeDecl(xSTypeDefinition);
            } else if (globalTypeDecl != xSTypeDefinition && !this.fTolerateDuplicates) {
                reportSharingError(xSTypeDefinition.getNamespace(), xSTypeDefinition.getName());
            }
        }
        ObjectList componentsExt = schemaGrammar.getComponentsExt(3);
        int length2 = componentsExt.getLength();
        for (int i2 = 0; i2 < length2; i2 += 2) {
            String str = (String) componentsExt.item(i2);
            int indexOf = str.indexOf(44);
            String substring = str.substring(0, indexOf);
            String substring2 = str.substring(indexOf + 1, str.length());
            XSTypeDefinition xSTypeDefinition2 = (XSTypeDefinition) componentsExt.item(i2 + 1);
            if (schemaGrammar2.getGlobalTypeDecl(substring2, substring) == null) {
                schemaGrammar2.addGlobalTypeDecl(xSTypeDefinition2, substring);
            }
        }
    }

    private Vector expandComponents(XSObject[] xSObjectArr, Map<String, Vector> map) {
        Vector vector = new Vector();
        for (int i = 0; i < xSObjectArr.length; i++) {
            if (!vector.contains(xSObjectArr[i])) {
                vector.add(xSObjectArr[i]);
            }
        }
        for (int i2 = 0; i2 < vector.size(); i2++) {
            expandRelatedComponents((XSObject) vector.elementAt(i2), vector, map);
        }
        return vector;
    }

    private void expandRelatedComponents(XSObject xSObject, Vector vector, Map<String, Vector> map) {
        short type = xSObject.getType();
        if (type != 1) {
            if (type != 2) {
                if (type == 3) {
                    expandRelatedTypeComponents((XSTypeDefinition) xSObject, vector, xSObject.getNamespace(), map);
                    return;
                } else if (type == 5) {
                    expandRelatedAttributeGroupComponents((XSAttributeGroupDefinition) xSObject, vector, xSObject.getNamespace(), map);
                } else if (type == 6) {
                    expandRelatedModelGroupDefinitionComponents((XSModelGroupDefinition) xSObject, vector, xSObject.getNamespace(), map);
                    return;
                } else {
                    return;
                }
            }
            expandRelatedElementComponents((XSElementDeclaration) xSObject, vector, xSObject.getNamespace(), map);
            return;
        }
        expandRelatedAttributeComponents((XSAttributeDeclaration) xSObject, vector, xSObject.getNamespace(), map);
    }

    private void expandRelatedAttributeComponents(XSAttributeDeclaration xSAttributeDeclaration, Vector vector, String str, Map<String, Vector> map) {
        addRelatedType(xSAttributeDeclaration.getTypeDefinition(), vector, str, map);
    }

    private void expandRelatedElementComponents(XSElementDeclaration xSElementDeclaration, Vector vector, String str, Map<String, Vector> map) {
        addRelatedType(xSElementDeclaration.getTypeDefinition(), vector, str, map);
        XSElementDeclaration substitutionGroupAffiliation = xSElementDeclaration.getSubstitutionGroupAffiliation();
        if (substitutionGroupAffiliation != null) {
            addRelatedElement(substitutionGroupAffiliation, vector, str, map);
        }
    }

    private void expandRelatedTypeComponents(XSTypeDefinition xSTypeDefinition, Vector vector, String str, Map<String, Vector> map) {
        if (xSTypeDefinition instanceof XSComplexTypeDecl) {
            expandRelatedComplexTypeComponents((XSComplexTypeDecl) xSTypeDefinition, vector, str, map);
        } else if (xSTypeDefinition instanceof XSSimpleTypeDecl) {
            expandRelatedSimpleTypeComponents((XSSimpleTypeDefinition) xSTypeDefinition, vector, str, map);
        }
    }

    private void expandRelatedModelGroupDefinitionComponents(XSModelGroupDefinition xSModelGroupDefinition, Vector vector, String str, Map<String, Vector> map) {
        expandRelatedModelGroupComponents(xSModelGroupDefinition.getModelGroup(), vector, str, map);
    }

    private void expandRelatedAttributeGroupComponents(XSAttributeGroupDefinition xSAttributeGroupDefinition, Vector vector, String str, Map<String, Vector> map) {
        expandRelatedAttributeUsesComponents(xSAttributeGroupDefinition.getAttributeUses(), vector, str, map);
    }

    private void expandRelatedComplexTypeComponents(XSComplexTypeDecl xSComplexTypeDecl, Vector vector, String str, Map<String, Vector> map) {
        addRelatedType(xSComplexTypeDecl.getBaseType(), vector, str, map);
        expandRelatedAttributeUsesComponents(xSComplexTypeDecl.getAttributeUses(), vector, str, map);
        XSParticle particle = xSComplexTypeDecl.getParticle();
        if (particle != null) {
            expandRelatedParticleComponents(particle, vector, str, map);
        }
    }

    private void expandRelatedSimpleTypeComponents(XSSimpleTypeDefinition xSSimpleTypeDefinition, Vector vector, String str, Map<String, Vector> map) {
        XSTypeDefinition baseType = xSSimpleTypeDefinition.getBaseType();
        if (baseType != null) {
            addRelatedType(baseType, vector, str, map);
        }
        XSSimpleTypeDefinition itemType = xSSimpleTypeDefinition.getItemType();
        if (itemType != null) {
            addRelatedType(itemType, vector, str, map);
        }
        XSSimpleTypeDefinition primitiveType = xSSimpleTypeDefinition.getPrimitiveType();
        if (primitiveType != null) {
            addRelatedType(primitiveType, vector, str, map);
        }
        XSObjectList memberTypes = xSSimpleTypeDefinition.getMemberTypes();
        if (memberTypes.size() > 0) {
            for (int i = 0; i < memberTypes.size(); i++) {
                addRelatedType((XSTypeDefinition) memberTypes.item(i), vector, str, map);
            }
        }
    }

    private void expandRelatedAttributeUsesComponents(XSObjectList xSObjectList, Vector vector, String str, Map<String, Vector> map) {
        int size = xSObjectList == null ? 0 : xSObjectList.size();
        for (int i = 0; i < size; i++) {
            expandRelatedAttributeUseComponents((XSAttributeUse) xSObjectList.item(i), vector, str, map);
        }
    }

    private void expandRelatedAttributeUseComponents(XSAttributeUse xSAttributeUse, Vector vector, String str, Map<String, Vector> map) {
        addRelatedAttribute(xSAttributeUse.getAttrDeclaration(), vector, str, map);
    }

    private void expandRelatedParticleComponents(XSParticle xSParticle, Vector vector, String str, Map<String, Vector> map) {
        XSTerm term = xSParticle.getTerm();
        short type = term.getType();
        if (type == 2) {
            addRelatedElement((XSElementDeclaration) term, vector, str, map);
        } else if (type == 7) {
            expandRelatedModelGroupComponents((XSModelGroup) term, vector, str, map);
        }
    }

    private void expandRelatedModelGroupComponents(XSModelGroup xSModelGroup, Vector vector, String str, Map<String, Vector> map) {
        XSObjectList particles = xSModelGroup.getParticles();
        int length = particles == null ? 0 : particles.getLength();
        for (int i = 0; i < length; i++) {
            expandRelatedParticleComponents((XSParticle) particles.item(i), vector, str, map);
        }
    }

    private void addRelatedType(XSTypeDefinition xSTypeDefinition, Vector vector, String str, Map<String, Vector> map) {
        if (xSTypeDefinition.getAnonymous()) {
            expandRelatedTypeComponents(xSTypeDefinition, vector, str, map);
        } else if (!xSTypeDefinition.getNamespace().equals(SchemaSymbols.URI_SCHEMAFORSCHEMA) && !vector.contains(xSTypeDefinition)) {
            addNamespaceDependency(str, xSTypeDefinition.getNamespace(), findDependentNamespaces(str, map));
            vector.add(xSTypeDefinition);
        }
    }

    private void addRelatedElement(XSElementDeclaration xSElementDeclaration, Vector vector, String str, Map<String, Vector> map) {
        if (xSElementDeclaration.getScope() != 1) {
            expandRelatedElementComponents(xSElementDeclaration, vector, str, map);
        } else if (!vector.contains(xSElementDeclaration)) {
            addNamespaceDependency(str, xSElementDeclaration.getNamespace(), findDependentNamespaces(str, map));
            vector.add(xSElementDeclaration);
        }
    }

    private void addRelatedAttribute(XSAttributeDeclaration xSAttributeDeclaration, Vector vector, String str, Map<String, Vector> map) {
        if (xSAttributeDeclaration.getScope() != 1) {
            expandRelatedAttributeComponents(xSAttributeDeclaration, vector, str, map);
        } else if (!vector.contains(xSAttributeDeclaration)) {
            addNamespaceDependency(str, xSAttributeDeclaration.getNamespace(), findDependentNamespaces(str, map));
            vector.add(xSAttributeDeclaration);
        }
    }

    private void addGlobalComponents(Vector vector, Map<String, Vector> map) {
        XSDDescription xSDDescription = new XSDDescription();
        int size = vector.size();
        for (int i = 0; i < size; i++) {
            addGlobalComponent((XSObject) vector.elementAt(i), xSDDescription);
        }
        updateImportDependencies(map);
    }

    private void addGlobalComponent(XSObject xSObject, XSDDescription xSDDescription) {
        xSDDescription.setNamespace(xSObject.getNamespace());
        SchemaGrammar schemaGrammar = getSchemaGrammar(xSDDescription);
        short type = xSObject.getType();
        String name = xSObject.getName();
        if (type == 1) {
            XSAttributeDecl xSAttributeDecl = (XSAttributeDecl) xSObject;
            if (xSAttributeDecl.getScope() == 1) {
                if (schemaGrammar.getGlobalAttributeDecl(name) == null) {
                    schemaGrammar.addGlobalAttributeDecl(xSAttributeDecl);
                }
                if (schemaGrammar.getGlobalAttributeDecl(name, "") == null) {
                    schemaGrammar.addGlobalAttributeDecl(xSAttributeDecl, "");
                }
            }
        } else if (type == 2) {
            XSElementDecl xSElementDecl = (XSElementDecl) xSObject;
            if (xSElementDecl.getScope() == 1) {
                schemaGrammar.addGlobalElementDeclAll(xSElementDecl);
                if (schemaGrammar.getGlobalElementDecl(name) == null) {
                    schemaGrammar.addGlobalElementDecl(xSElementDecl);
                }
                if (schemaGrammar.getGlobalElementDecl(name, "") == null) {
                    schemaGrammar.addGlobalElementDecl(xSElementDecl, "");
                }
            }
        } else if (type == 3) {
            XSTypeDefinition xSTypeDefinition = (XSTypeDefinition) xSObject;
            if (!xSTypeDefinition.getAnonymous()) {
                if (schemaGrammar.getGlobalTypeDecl(name) == null) {
                    schemaGrammar.addGlobalTypeDecl(xSTypeDefinition);
                }
                if (schemaGrammar.getGlobalTypeDecl(name, "") == null) {
                    schemaGrammar.addGlobalTypeDecl(xSTypeDefinition, "");
                }
            }
        } else if (type == 5) {
            if (schemaGrammar.getGlobalAttributeDecl(name) == null) {
                schemaGrammar.addGlobalAttributeGroupDecl((XSAttributeGroupDecl) xSObject);
            }
            if (schemaGrammar.getGlobalAttributeDecl(name, "") == null) {
                schemaGrammar.addGlobalAttributeGroupDecl((XSAttributeGroupDecl) xSObject, "");
            }
        } else if (type == 6) {
            if (schemaGrammar.getGlobalGroupDecl(name) == null) {
                schemaGrammar.addGlobalGroupDecl((XSGroupDecl) xSObject);
            }
            if (schemaGrammar.getGlobalGroupDecl(name, "") == null) {
                schemaGrammar.addGlobalGroupDecl((XSGroupDecl) xSObject, "");
            }
        } else if (type == 11) {
            if (schemaGrammar.getGlobalNotationDecl(name) == null) {
                schemaGrammar.addGlobalNotationDecl((XSNotationDecl) xSObject);
            }
            if (schemaGrammar.getGlobalNotationDecl(name, "") == null) {
                schemaGrammar.addGlobalNotationDecl((XSNotationDecl) xSObject, "");
            }
        }
    }

    private void updateImportDependencies(Map<String, Vector> map) {
        if (map != null) {
            for (Map.Entry<String, Vector> entry : map.entrySet()) {
                String key = entry.getKey();
                Vector value = entry.getValue();
                if (value.size() > 0) {
                    expandImportList(key, value);
                }
            }
        }
    }

    private void expandImportList(String str, Vector vector) {
        SchemaGrammar grammar = this.fGrammarBucket.getGrammar(str);
        if (grammar != null) {
            Vector importedGrammars = grammar.getImportedGrammars();
            if (importedGrammars == null) {
                Vector vector2 = new Vector();
                addImportList(grammar, vector2, vector);
                grammar.setImportedGrammars(vector2);
                return;
            }
            updateImportList(grammar, importedGrammars, vector);
        }
    }

    private void addImportList(SchemaGrammar schemaGrammar, Vector vector, Vector vector2) {
        int size = vector2.size();
        for (int i = 0; i < size; i++) {
            SchemaGrammar grammar = this.fGrammarBucket.getGrammar((String) vector2.elementAt(i));
            if (grammar != null) {
                vector.add(grammar);
            }
        }
    }

    private void updateImportList(SchemaGrammar schemaGrammar, Vector vector, Vector vector2) {
        int size = vector2.size();
        for (int i = 0; i < size; i++) {
            SchemaGrammar grammar = this.fGrammarBucket.getGrammar((String) vector2.elementAt(i));
            if (grammar != null && !containedImportedGrammar(vector, grammar)) {
                vector.add(grammar);
            }
        }
    }

    private boolean containedImportedGrammar(Vector vector, SchemaGrammar schemaGrammar) {
        int size = vector.size();
        for (int i = 0; i < size; i++) {
            if (null2EmptyString(((SchemaGrammar) vector.elementAt(i)).getTargetNamespace()).equals(null2EmptyString(schemaGrammar.getTargetNamespace()))) {
                return true;
            }
        }
        return false;
    }

    private SchemaGrammar getSchemaGrammar(XSDDescription xSDDescription) {
        SchemaGrammar findGrammar = findGrammar(xSDDescription, this.fNamespaceGrowth);
        if (findGrammar != null) {
            return findGrammar.isImmutable() ? createGrammarFrom(findGrammar) : findGrammar;
        }
        SchemaGrammar schemaGrammar = new SchemaGrammar(xSDDescription.getNamespace(), xSDDescription.makeClone(), this.fSymbolTable);
        this.fGrammarBucket.putGrammar(schemaGrammar);
        return schemaGrammar;
    }

    private Vector findDependentNamespaces(String str, Map map) {
        String null2EmptyString = null2EmptyString(str);
        Vector vector = (Vector) getFromMap(map, null2EmptyString);
        if (vector != null) {
            return vector;
        }
        Vector vector2 = new Vector();
        map.put(null2EmptyString, vector2);
        return vector2;
    }

    private void addNamespaceDependency(String str, String str2, Vector vector) {
        String null2EmptyString = null2EmptyString(str);
        String null2EmptyString2 = null2EmptyString(str2);
        if (!null2EmptyString.equals(null2EmptyString2) && !vector.contains(null2EmptyString2)) {
            vector.add(null2EmptyString2);
        }
    }

    private void reportSharingError(String str, String str2) {
        String str3;
        if (str == null) {
            str3 = "," + str2;
        } else {
            str3 = str + "," + str2;
        }
        reportSchemaError("sch-props-correct.2", new Object[]{str3}, null);
    }

    private void createTraversers() {
        this.fAttributeChecker = new XSAttributeChecker(this);
        this.fAttributeGroupTraverser = new XSDAttributeGroupTraverser(this, this.fAttributeChecker);
        this.fAttributeTraverser = new XSDAttributeTraverser(this, this.fAttributeChecker);
        this.fComplexTypeTraverser = new XSDComplexTypeTraverser(this, this.fAttributeChecker);
        this.fElementTraverser = new XSDElementTraverser(this, this.fAttributeChecker);
        this.fGroupTraverser = new XSDGroupTraverser(this, this.fAttributeChecker);
        this.fKeyrefTraverser = new XSDKeyrefTraverser(this, this.fAttributeChecker);
        this.fNotationTraverser = new XSDNotationTraverser(this, this.fAttributeChecker);
        this.fSimpleTypeTraverser = new XSDSimpleTypeTraverser(this, this.fAttributeChecker);
        this.fUniqueOrKeyTraverser = new XSDUniqueOrKeyTraverser(this, this.fAttributeChecker);
        this.fWildCardTraverser = new XSDWildcardTraverser(this, this.fAttributeChecker);
    }

    /* access modifiers changed from: package-private */
    public void prepareForParse() {
        this.fTraversed.clear();
        this.fDoc2SystemId.clear();
        this.fHiddenNodes.clear();
        this.fLastSchemaWasDuplicate = false;
    }

    /* access modifiers changed from: package-private */
    public void prepareForTraverse() {
        if (!this.registryEmpty) {
            this.fUnparsedAttributeRegistry.clear();
            this.fUnparsedAttributeGroupRegistry.clear();
            this.fUnparsedElementRegistry.clear();
            this.fUnparsedGroupRegistry.clear();
            this.fUnparsedIdentityConstraintRegistry.clear();
            this.fUnparsedNotationRegistry.clear();
            this.fUnparsedTypeRegistry.clear();
            this.fUnparsedAttributeRegistrySub.clear();
            this.fUnparsedAttributeGroupRegistrySub.clear();
            this.fUnparsedElementRegistrySub.clear();
            this.fUnparsedGroupRegistrySub.clear();
            this.fUnparsedIdentityConstraintRegistrySub.clear();
            this.fUnparsedNotationRegistrySub.clear();
            this.fUnparsedTypeRegistrySub.clear();
        }
        for (int i = 1; i <= 7; i++) {
            Map<String, XSDocumentInfo>[] mapArr = this.fUnparsedRegistriesExt;
            if (mapArr[i] != null) {
                mapArr[i].clear();
            }
        }
        this.fDependencyMap.clear();
        this.fDoc2XSDocumentMap.clear();
        Map map = this.fRedefine2XSDMap;
        if (map != null) {
            map.clear();
        }
        Map map2 = this.fRedefine2NSSupport;
        if (map2 != null) {
            map2.clear();
        }
        this.fAllTNSs.removeAllElements();
        this.fImportMap.clear();
        this.fRoot = null;
        for (int i2 = 0; i2 < this.fLocalElemStackPos; i2++) {
            this.fParticle[i2] = null;
            this.fLocalElementDecl[i2] = null;
            this.fLocalElementDecl_schema[i2] = null;
            this.fLocalElemNamespaceContext[i2] = null;
        }
        this.fLocalElemStackPos = 0;
        for (int i3 = 0; i3 < this.fKeyrefStackPos; i3++) {
            this.fKeyrefs[i3] = null;
            this.fKeyrefElems[i3] = null;
            this.fKeyrefNamespaceContext[i3] = null;
            this.fKeyrefsMapXSDocumentInfo[i3] = null;
        }
        this.fKeyrefStackPos = 0;
        if (this.fAttributeChecker == null) {
            createTraversers();
        }
        Locale locale = this.fErrorReporter.getLocale();
        this.fAttributeChecker.reset(this.fSymbolTable);
        this.fAttributeGroupTraverser.reset(this.fSymbolTable, this.fValidateAnnotations, locale);
        this.fAttributeTraverser.reset(this.fSymbolTable, this.fValidateAnnotations, locale);
        this.fComplexTypeTraverser.reset(this.fSymbolTable, this.fValidateAnnotations, locale);
        this.fElementTraverser.reset(this.fSymbolTable, this.fValidateAnnotations, locale);
        this.fGroupTraverser.reset(this.fSymbolTable, this.fValidateAnnotations, locale);
        this.fKeyrefTraverser.reset(this.fSymbolTable, this.fValidateAnnotations, locale);
        this.fNotationTraverser.reset(this.fSymbolTable, this.fValidateAnnotations, locale);
        this.fSimpleTypeTraverser.reset(this.fSymbolTable, this.fValidateAnnotations, locale);
        this.fUniqueOrKeyTraverser.reset(this.fSymbolTable, this.fValidateAnnotations, locale);
        this.fWildCardTraverser.reset(this.fSymbolTable, this.fValidateAnnotations, locale);
        this.fRedefinedRestrictedAttributeGroupRegistry.clear();
        this.fRedefinedRestrictedGroupRegistry.clear();
        this.fGlobalAttrDecls.clear();
        this.fGlobalAttrGrpDecls.clear();
        this.fGlobalElemDecls.clear();
        this.fGlobalGroupDecls.clear();
        this.fGlobalNotationDecls.clear();
        this.fGlobalIDConstraintDecls.clear();
        this.fGlobalTypeDecls.clear();
    }

    public void setDeclPool(XSDeclarationPool xSDeclarationPool) {
        this.fDeclPool = xSDeclarationPool;
    }

    public void setDVFactory(SchemaDVFactory schemaDVFactory) {
        this.fDVFactory = schemaDVFactory;
    }

    public SchemaDVFactory getDVFactory() {
        return this.fDVFactory;
    }

    public void reset(XMLComponentManager xMLComponentManager) {
        this.fSymbolTable = (SymbolTable) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/symbol-table");
        this.fSecurityManager = (XMLSecurityManager) xMLComponentManager.getProperty("http://apache.org/xml/properties/security-manager", null);
        this.fEntityManager = (XMLEntityResolver) xMLComponentManager.getProperty(ENTITY_MANAGER);
        XMLEntityResolver xMLEntityResolver = (XMLEntityResolver) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/entity-resolver");
        if (xMLEntityResolver != null) {
            this.fSchemaParser.setEntityResolver(xMLEntityResolver);
        }
        this.fErrorReporter = (XMLErrorReporter) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/error-reporter");
        this.fErrorHandler = this.fErrorReporter.getErrorHandler();
        this.fLocale = this.fErrorReporter.getLocale();
        this.fValidateAnnotations = xMLComponentManager.getFeature(VALIDATE_ANNOTATIONS, false);
        this.fHonourAllSchemaLocations = xMLComponentManager.getFeature(HONOUR_ALL_SCHEMALOCATIONS, false);
        this.fNamespaceGrowth = xMLComponentManager.getFeature(NAMESPACE_GROWTH, false);
        this.fTolerateDuplicates = xMLComponentManager.getFeature(TOLERATE_DUPLICATES, false);
        try {
            if (this.fErrorHandler != this.fSchemaParser.getProperty(ERROR_HANDLER)) {
                this.fSchemaParser.setProperty(ERROR_HANDLER, this.fErrorHandler != null ? this.fErrorHandler : new DefaultErrorHandler());
                if (this.fAnnotationValidator != null) {
                    this.fAnnotationValidator.setProperty(ERROR_HANDLER, this.fErrorHandler != null ? this.fErrorHandler : new DefaultErrorHandler());
                }
            }
            if (this.fLocale != this.fSchemaParser.getProperty("http://apache.org/xml/properties/locale")) {
                this.fSchemaParser.setProperty("http://apache.org/xml/properties/locale", this.fLocale);
                if (this.fAnnotationValidator != null) {
                    this.fAnnotationValidator.setProperty("http://apache.org/xml/properties/locale", this.fLocale);
                }
            }
        } catch (XMLConfigurationException unused) {
        }
        try {
            this.fSchemaParser.setFeature(CONTINUE_AFTER_FATAL_ERROR, this.fErrorReporter.getFeature(CONTINUE_AFTER_FATAL_ERROR));
        } catch (XMLConfigurationException unused2) {
        }
        try {
            if (xMLComponentManager.getFeature(ALLOW_JAVA_ENCODINGS, false)) {
                this.fSchemaParser.setFeature(ALLOW_JAVA_ENCODINGS, true);
            }
        } catch (XMLConfigurationException unused3) {
        }
        try {
            if (xMLComponentManager.getFeature(STANDARD_URI_CONFORMANT_FEATURE, false)) {
                this.fSchemaParser.setFeature(STANDARD_URI_CONFORMANT_FEATURE, true);
            }
        } catch (XMLConfigurationException unused4) {
        }
        try {
            this.fGrammarPool = (XMLGrammarPool) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/grammar-pool");
        } catch (XMLConfigurationException unused5) {
            this.fGrammarPool = null;
        }
        try {
            if (xMLComponentManager.getFeature(DISALLOW_DOCTYPE, false)) {
                this.fSchemaParser.setFeature(DISALLOW_DOCTYPE, true);
            }
        } catch (XMLConfigurationException unused6) {
        }
        try {
            if (this.fSecurityManager != null) {
                this.fSchemaParser.setProperty("http://apache.org/xml/properties/security-manager", this.fSecurityManager);
            }
        } catch (XMLConfigurationException unused7) {
        }
        this.fSecurityPropertyMgr = (XMLSecurityPropertyManager) xMLComponentManager.getProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager");
        this.fSchemaParser.setProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", this.fSecurityPropertyMgr);
        this.fAccessExternalDTD = this.fSecurityPropertyMgr.getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD);
        this.fAccessExternalSchema = this.fSecurityPropertyMgr.getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_SCHEMA);
        this.fOverrideDefaultParser = xMLComponentManager.getFeature("jdk.xml.overrideDefaultParser");
        this.fSchemaParser.setFeature("jdk.xml.overrideDefaultParser", this.fOverrideDefaultParser);
    }

    /* access modifiers changed from: package-private */
    public void traverseLocalElements() {
        this.fElementTraverser.fDeferTraversingLocalElements = false;
        for (int i = 0; i < this.fLocalElemStackPos; i++) {
            Element element = this.fLocalElementDecl[i];
            XSDocumentInfo xSDocumentInfo = this.fLocalElementDecl_schema[i];
            this.fElementTraverser.traverseLocal(this.fParticle[i], element, xSDocumentInfo, this.fGrammarBucket.getGrammar(xSDocumentInfo.fTargetNamespace), this.fAllContext[i], this.fParent[i], this.fLocalElemNamespaceContext[i]);
            if (this.fParticle[i].fType == 0) {
                XSModelGroupImpl xSModelGroupImpl = null;
                XSObject[] xSObjectArr = this.fParent;
                if (xSObjectArr[i] instanceof XSComplexTypeDecl) {
                    XSParticle particle = ((XSComplexTypeDecl) xSObjectArr[i]).getParticle();
                    if (particle != null) {
                        xSModelGroupImpl = (XSModelGroupImpl) particle.getTerm();
                    }
                } else {
                    xSModelGroupImpl = ((XSGroupDecl) xSObjectArr[i]).fModelGroup;
                }
                if (xSModelGroupImpl != null) {
                    removeParticle(xSModelGroupImpl, this.fParticle[i]);
                }
            }
        }
    }

    private boolean removeParticle(XSModelGroupImpl xSModelGroupImpl, XSParticleDecl xSParticleDecl) {
        int i = 0;
        while (i < xSModelGroupImpl.fParticleCount) {
            XSParticleDecl xSParticleDecl2 = xSModelGroupImpl.fParticles[i];
            if (xSParticleDecl2 == xSParticleDecl) {
                while (i < xSModelGroupImpl.fParticleCount - 1) {
                    int i2 = i + 1;
                    xSModelGroupImpl.fParticles[i] = xSModelGroupImpl.fParticles[i2];
                    i = i2;
                }
                xSModelGroupImpl.fParticleCount--;
                return true;
            } else if (xSParticleDecl2.fType == 3 && removeParticle((XSModelGroupImpl) xSParticleDecl2.fValue, xSParticleDecl)) {
                return true;
            } else {
                i++;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void fillInLocalElemInfo(Element element, XSDocumentInfo xSDocumentInfo, int i, XSObject xSObject, XSParticleDecl xSParticleDecl) {
        XSParticleDecl[] xSParticleDeclArr = this.fParticle;
        int length = xSParticleDeclArr.length;
        int i2 = this.fLocalElemStackPos;
        if (length == i2) {
            XSParticleDecl[] xSParticleDeclArr2 = new XSParticleDecl[(i2 + 10)];
            System.arraycopy(xSParticleDeclArr, 0, xSParticleDeclArr2, 0, i2);
            this.fParticle = xSParticleDeclArr2;
            int i3 = this.fLocalElemStackPos;
            Element[] elementArr = new Element[(i3 + 10)];
            System.arraycopy(this.fLocalElementDecl, 0, elementArr, 0, i3);
            this.fLocalElementDecl = elementArr;
            int i4 = this.fLocalElemStackPos;
            XSDocumentInfo[] xSDocumentInfoArr = new XSDocumentInfo[(i4 + 10)];
            System.arraycopy(this.fLocalElementDecl_schema, 0, xSDocumentInfoArr, 0, i4);
            this.fLocalElementDecl_schema = xSDocumentInfoArr;
            int i5 = this.fLocalElemStackPos;
            int[] iArr = new int[(i5 + 10)];
            System.arraycopy(this.fAllContext, 0, iArr, 0, i5);
            this.fAllContext = iArr;
            int i6 = this.fLocalElemStackPos;
            XSObject[] xSObjectArr = new XSObject[(i6 + 10)];
            System.arraycopy(this.fParent, 0, xSObjectArr, 0, i6);
            this.fParent = xSObjectArr;
            int i7 = this.fLocalElemStackPos;
            String[][] strArr = new String[(i7 + 10)][];
            System.arraycopy(this.fLocalElemNamespaceContext, 0, strArr, 0, i7);
            this.fLocalElemNamespaceContext = strArr;
        }
        XSParticleDecl[] xSParticleDeclArr3 = this.fParticle;
        int i8 = this.fLocalElemStackPos;
        xSParticleDeclArr3[i8] = xSParticleDecl;
        this.fLocalElementDecl[i8] = element;
        this.fLocalElementDecl_schema[i8] = xSDocumentInfo;
        this.fAllContext[i8] = i;
        this.fParent[i8] = xSObject;
        String[][] strArr2 = this.fLocalElemNamespaceContext;
        this.fLocalElemStackPos = i8 + 1;
        strArr2[i8] = xSDocumentInfo.fNamespaceSupport.getEffectiveLocalContext();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0184  */
    public void checkForDuplicateNames(String str, int i, Map<String, Element> map, Map<String, XSDocumentInfo> map2, Element element, XSDocumentInfo xSDocumentInfo) {
        boolean z;
        XSDocumentInfo xSDocumentInfo2;
        Element element2 = map.get(str);
        if (element2 == null) {
            if (this.fNamespaceGrowth && !this.fTolerateDuplicates) {
                checkForDuplicateNames(str, i, element);
            }
            map.put(str, element);
            map2.put(str, xSDocumentInfo);
        } else {
            Element element3 = element2;
            XSDocumentInfo xSDocumentInfo3 = map2.get(str);
            if (element3 != element) {
                Element parent = DOMUtil.getParent(element3);
                if (DOMUtil.getLocalName(parent).equals(SchemaSymbols.ELT_REDEFINE)) {
                    Map map3 = this.fRedefine2XSDMap;
                    if (map3 != null) {
                        xSDocumentInfo2 = (XSDocumentInfo) map3.get(parent);
                        z = true;
                        if (xSDocumentInfo2 != null) {
                            if (xSDocumentInfo3 == xSDocumentInfo) {
                                reportSchemaError("sch-props-correct.2", new Object[]{str}, element);
                                return;
                            }
                            String str2 = str.substring(str.lastIndexOf(44) + 1) + REDEF_IDENTIFIER;
                            if (xSDocumentInfo2 == xSDocumentInfo) {
                                element.setAttribute(SchemaSymbols.ATT_NAME, str2);
                                if (xSDocumentInfo.fTargetNamespace == null) {
                                    map.put("," + str2, element);
                                    map2.put("," + str2, xSDocumentInfo);
                                } else {
                                    map.put(xSDocumentInfo.fTargetNamespace + "," + str2, element);
                                    map2.put(xSDocumentInfo.fTargetNamespace + "," + str2, xSDocumentInfo);
                                }
                                if (xSDocumentInfo.fTargetNamespace == null) {
                                    checkForDuplicateNames("," + str2, i, map, map2, element, xSDocumentInfo);
                                } else {
                                    checkForDuplicateNames(xSDocumentInfo.fTargetNamespace + "," + str2, i, map, map2, element, xSDocumentInfo);
                                }
                            } else if (!z) {
                                reportSchemaError("sch-props-correct.2", new Object[]{str}, element);
                            } else if (xSDocumentInfo.fTargetNamespace == null) {
                                checkForDuplicateNames("," + str2, i, map, map2, element, xSDocumentInfo);
                            } else {
                                checkForDuplicateNames(xSDocumentInfo.fTargetNamespace + "," + str2, i, map, map2, element, xSDocumentInfo);
                            }
                        } else if (!this.fTolerateDuplicates) {
                            reportSchemaError("sch-props-correct.2", new Object[]{str}, element);
                        } else {
                            Map<String, XSDocumentInfo>[] mapArr = this.fUnparsedRegistriesExt;
                            if (mapArr[i] != null && mapArr[i].get(str) == xSDocumentInfo) {
                                reportSchemaError("sch-props-correct.2", new Object[]{str}, element);
                            }
                        }
                    }
                } else if (DOMUtil.getLocalName(DOMUtil.getParent(element)).equals(SchemaSymbols.ELT_REDEFINE)) {
                    xSDocumentInfo2 = xSDocumentInfo3;
                    z = false;
                    if (xSDocumentInfo2 != null) {
                    }
                }
                xSDocumentInfo2 = null;
                z = true;
                if (xSDocumentInfo2 != null) {
                }
            } else {
                return;
            }
        }
        if (this.fTolerateDuplicates) {
            Map<String, XSDocumentInfo>[] mapArr2 = this.fUnparsedRegistriesExt;
            if (mapArr2[i] == null) {
                mapArr2[i] = new HashMap();
            }
            this.fUnparsedRegistriesExt[i].put(str, xSDocumentInfo);
        }
    }

    /* access modifiers changed from: package-private */
    public void checkForDuplicateNames(String str, int i, Element element) {
        int indexOf = str.indexOf(44);
        SchemaGrammar grammar = this.fGrammarBucket.getGrammar(emptyString2Null(str.substring(0, indexOf)));
        if (grammar != null && getGlobalDeclFromGrammar(grammar, i, str.substring(indexOf + 1)) != null) {
            reportSchemaError("sch-props-correct.2", new Object[]{str}, element);
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x00c2: APUT  
      (r1v43 java.lang.Object[])
      (1 ??[boolean, int, float, short, byte, char])
      (wrap: java.lang.Object : 0x00bd: INVOKE  (r0v26 java.lang.Object) = (r2v13 java.lang.StringBuilder) type: VIRTUAL call: java.lang.StringBuilder.toString():java.lang.String)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x01ad: APUT  
      (r3v5 java.lang.Object[])
      (1 ??[boolean, int, float, short, byte, char])
      (wrap: java.lang.Object : 0x01a8: INVOKE  (r0v17 java.lang.Object) = (r1v35 java.lang.StringBuilder) type: VIRTUAL call: java.lang.StringBuilder.toString():java.lang.String)
     */
    private void renameRedefiningComponents(XSDocumentInfo xSDocumentInfo, Element element, String str, String str2, String str3) {
        StringBuilder sb;
        StringBuilder sb2;
        String str4 = "";
        if (str.equals(SchemaSymbols.ELT_SIMPLETYPE)) {
            Element firstChildElement = DOMUtil.getFirstChildElement(element);
            if (firstChildElement == null) {
                reportSchemaError("src-redefine.5.a.a", null, element);
                return;
            }
            if (DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
                firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement);
            }
            if (firstChildElement == null) {
                reportSchemaError("src-redefine.5.a.a", null, element);
                return;
            }
            String localName = DOMUtil.getLocalName(firstChildElement);
            if (!localName.equals(SchemaSymbols.ELT_RESTRICTION)) {
                reportSchemaError("src-redefine.5.a.b", new Object[]{localName}, element);
                return;
            }
            Object[] checkAttributes = this.fAttributeChecker.checkAttributes(firstChildElement, false, xSDocumentInfo);
            QName qName = (QName) checkAttributes[XSAttributeChecker.ATTIDX_BASE];
            if (qName == null || qName.uri != xSDocumentInfo.fTargetNamespace || !qName.localpart.equals(str2)) {
                Object[] objArr = new Object[2];
                objArr[0] = localName;
                StringBuilder sb3 = new StringBuilder();
                if (xSDocumentInfo.fTargetNamespace != null) {
                    str4 = xSDocumentInfo.fTargetNamespace;
                }
                sb3.append(str4);
                sb3.append(",");
                sb3.append(str2);
                objArr[1] = sb3.toString();
                reportSchemaError("src-redefine.5.a.c", objArr, element);
            } else if (qName.prefix == null || qName.prefix.length() <= 0) {
                firstChildElement.setAttribute(SchemaSymbols.ATT_BASE, str3);
            } else {
                String str5 = SchemaSymbols.ATT_BASE;
                firstChildElement.setAttribute(str5, qName.prefix + ":" + str3);
            }
            this.fAttributeChecker.returnAttrArray(checkAttributes, xSDocumentInfo);
        } else if (str.equals(SchemaSymbols.ELT_COMPLEXTYPE)) {
            Element firstChildElement2 = DOMUtil.getFirstChildElement(element);
            if (firstChildElement2 == null) {
                reportSchemaError("src-redefine.5.b.a", null, element);
                return;
            }
            if (DOMUtil.getLocalName(firstChildElement2).equals(SchemaSymbols.ELT_ANNOTATION)) {
                firstChildElement2 = DOMUtil.getNextSiblingElement(firstChildElement2);
            }
            if (firstChildElement2 == null) {
                reportSchemaError("src-redefine.5.b.a", null, element);
                return;
            }
            Element firstChildElement3 = DOMUtil.getFirstChildElement(firstChildElement2);
            if (firstChildElement3 == null) {
                reportSchemaError("src-redefine.5.b.b", null, firstChildElement2);
                return;
            }
            if (DOMUtil.getLocalName(firstChildElement3).equals(SchemaSymbols.ELT_ANNOTATION)) {
                firstChildElement3 = DOMUtil.getNextSiblingElement(firstChildElement3);
            }
            if (firstChildElement3 == null) {
                reportSchemaError("src-redefine.5.b.b", null, firstChildElement2);
                return;
            }
            String localName2 = DOMUtil.getLocalName(firstChildElement3);
            if (localName2.equals(SchemaSymbols.ELT_RESTRICTION) || localName2.equals(SchemaSymbols.ELT_EXTENSION)) {
                QName qName2 = (QName) this.fAttributeChecker.checkAttributes(firstChildElement3, false, xSDocumentInfo)[XSAttributeChecker.ATTIDX_BASE];
                if (qName2 == null || qName2.uri != xSDocumentInfo.fTargetNamespace || !qName2.localpart.equals(str2)) {
                    Object[] objArr2 = new Object[2];
                    objArr2[0] = localName2;
                    StringBuilder sb4 = new StringBuilder();
                    if (xSDocumentInfo.fTargetNamespace != null) {
                        str4 = xSDocumentInfo.fTargetNamespace;
                    }
                    sb4.append(str4);
                    sb4.append(",");
                    sb4.append(str2);
                    objArr2[1] = sb4.toString();
                    reportSchemaError("src-redefine.5.b.d", objArr2, firstChildElement3);
                } else if (qName2.prefix == null || qName2.prefix.length() <= 0) {
                    firstChildElement3.setAttribute(SchemaSymbols.ATT_BASE, str3);
                } else {
                    String str6 = SchemaSymbols.ATT_BASE;
                    firstChildElement3.setAttribute(str6, qName2.prefix + ":" + str3);
                }
            } else {
                reportSchemaError("src-redefine.5.b.c", new Object[]{localName2}, firstChildElement3);
            }
        } else if (str.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP)) {
            if (xSDocumentInfo.fTargetNamespace == null) {
                sb2 = new StringBuilder();
            } else {
                sb2 = new StringBuilder();
                sb2.append(xSDocumentInfo.fTargetNamespace);
            }
            sb2.append(",");
            sb2.append(str2);
            String sb5 = sb2.toString();
            int changeRedefineGroup = changeRedefineGroup(sb5, str, str3, element, xSDocumentInfo);
            if (changeRedefineGroup > 1) {
                reportSchemaError("src-redefine.7.1", new Object[]{new Integer(changeRedefineGroup)}, element);
            } else if (changeRedefineGroup != 1) {
                if (xSDocumentInfo.fTargetNamespace == null) {
                    Map map = this.fRedefinedRestrictedAttributeGroupRegistry;
                    map.put(sb5, "," + str3);
                    return;
                }
                Map map2 = this.fRedefinedRestrictedAttributeGroupRegistry;
                map2.put(sb5, xSDocumentInfo.fTargetNamespace + "," + str3);
            }
        } else if (str.equals(SchemaSymbols.ELT_GROUP)) {
            if (xSDocumentInfo.fTargetNamespace == null) {
                sb = new StringBuilder();
            } else {
                sb = new StringBuilder();
                sb.append(xSDocumentInfo.fTargetNamespace);
            }
            sb.append(",");
            sb.append(str2);
            String sb6 = sb.toString();
            int changeRedefineGroup2 = changeRedefineGroup(sb6, str, str3, element, xSDocumentInfo);
            if (changeRedefineGroup2 > 1) {
                reportSchemaError("src-redefine.6.1.1", new Object[]{new Integer(changeRedefineGroup2)}, element);
            } else if (changeRedefineGroup2 != 1) {
                if (xSDocumentInfo.fTargetNamespace == null) {
                    Map map3 = this.fRedefinedRestrictedGroupRegistry;
                    map3.put(sb6, "," + str3);
                    return;
                }
                Map map4 = this.fRedefinedRestrictedGroupRegistry;
                map4.put(sb6, xSDocumentInfo.fTargetNamespace + "," + str3);
            }
        } else {
            reportSchemaError("Internal-Error", new Object[]{"could not handle this particular <redefine>; please submit your schemas and instance document in a bug report!"}, element);
        }
    }

    private String findQName(String str, XSDocumentInfo xSDocumentInfo) {
        SchemaNamespaceSupport schemaNamespaceSupport = xSDocumentInfo.fNamespaceSupport;
        int indexOf = str.indexOf(58);
        String str2 = XMLSymbols.EMPTY_STRING;
        if (indexOf > 0) {
            str2 = str.substring(0, indexOf);
        }
        String uri = schemaNamespaceSupport.getURI(this.fSymbolTable.addSymbol(str2));
        if (indexOf != 0) {
            str = str.substring(indexOf + 1);
        }
        if (str2 == XMLSymbols.EMPTY_STRING && uri == null && xSDocumentInfo.fIsChameleonSchema) {
            uri = xSDocumentInfo.fTargetNamespace;
        }
        if (uri == null) {
            return "," + str;
        }
        return uri + "," + str;
    }

    private int changeRedefineGroup(String str, String str2, String str3, Element element, XSDocumentInfo xSDocumentInfo) {
        int i = 0;
        for (Element firstChildElement = DOMUtil.getFirstChildElement(element); firstChildElement != null; firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement)) {
            if (!DOMUtil.getLocalName(firstChildElement).equals(str2)) {
                i += changeRedefineGroup(str, str2, str3, firstChildElement, xSDocumentInfo);
            } else {
                String attribute = firstChildElement.getAttribute(SchemaSymbols.ATT_REF);
                if (attribute.length() != 0 && str.equals(findQName(attribute, xSDocumentInfo))) {
                    String str4 = XMLSymbols.EMPTY_STRING;
                    int indexOf = attribute.indexOf(":");
                    if (indexOf > 0) {
                        String substring = attribute.substring(0, indexOf);
                        firstChildElement.setAttribute(SchemaSymbols.ATT_REF, substring + ":" + str3);
                    } else {
                        firstChildElement.setAttribute(SchemaSymbols.ATT_REF, str3);
                    }
                    i++;
                    if (str2.equals(SchemaSymbols.ELT_GROUP)) {
                        String attribute2 = firstChildElement.getAttribute(SchemaSymbols.ATT_MINOCCURS);
                        String attribute3 = firstChildElement.getAttribute(SchemaSymbols.ATT_MAXOCCURS);
                        if ((attribute3.length() != 0 && !attribute3.equals("1")) || (attribute2.length() != 0 && !attribute2.equals("1"))) {
                            reportSchemaError("src-redefine.6.1.2", new Object[]{attribute}, firstChildElement);
                        }
                    }
                }
            }
        }
        return i;
    }

    private XSDocumentInfo findXSDocumentForDecl(XSDocumentInfo xSDocumentInfo, Element element, XSDocumentInfo xSDocumentInfo2) {
        if (xSDocumentInfo2 == null) {
            return null;
        }
        return xSDocumentInfo2;
    }

    private boolean nonAnnotationContent(Element element) {
        for (Element firstChildElement = DOMUtil.getFirstChildElement(element); firstChildElement != null; firstChildElement = DOMUtil.getNextSiblingElement(firstChildElement)) {
            if (!DOMUtil.getLocalName(firstChildElement).equals(SchemaSymbols.ELT_ANNOTATION)) {
                return true;
            }
        }
        return false;
    }

    private void setSchemasVisible(XSDocumentInfo xSDocumentInfo) {
        if (DOMUtil.isHidden(xSDocumentInfo.fSchemaElement, this.fHiddenNodes)) {
            DOMUtil.setVisible(xSDocumentInfo.fSchemaElement, this.fHiddenNodes);
            Vector<XSDocumentInfo> vector = this.fDependencyMap.get(xSDocumentInfo);
            for (int i = 0; i < vector.size(); i++) {
                setSchemasVisible(vector.elementAt(i));
            }
        }
    }

    public SimpleLocator element2Locator(Element element) {
        if (!(element instanceof ElementImpl)) {
            return null;
        }
        SimpleLocator simpleLocator = new SimpleLocator();
        if (element2Locator(element, simpleLocator)) {
            return simpleLocator;
        }
        return null;
    }

    public boolean element2Locator(Element element, SimpleLocator simpleLocator) {
        if (simpleLocator == null || !(element instanceof ElementImpl)) {
            return false;
        }
        ElementImpl elementImpl = (ElementImpl) element;
        String str = this.fDoc2SystemId.get(DOMUtil.getRoot(elementImpl.getOwnerDocument()));
        simpleLocator.setValues(str, str, elementImpl.getLineNumber(), elementImpl.getColumnNumber(), elementImpl.getCharacterOffset());
        return true;
    }

    private Element getElementFromMap(Map<String, Element> map, String str) {
        if (map == null) {
            return null;
        }
        return map.get(str);
    }

    private XSDocumentInfo getDocInfoFromMap(Map<String, XSDocumentInfo> map, String str) {
        if (map == null) {
            return null;
        }
        return map.get(str);
    }

    private Object getFromMap(Map map, String str) {
        if (map == null) {
            return null;
        }
        return map.get(str);
    }

    /* access modifiers changed from: package-private */
    public void reportSchemaFatalError(String str, Object[] objArr, Element element) {
        reportSchemaErr(str, objArr, element, 2, null);
    }

    /* access modifiers changed from: package-private */
    public void reportSchemaError(String str, Object[] objArr, Element element) {
        reportSchemaErr(str, objArr, element, 1, null);
    }

    /* access modifiers changed from: package-private */
    public void reportSchemaError(String str, Object[] objArr, Element element, Exception exc) {
        reportSchemaErr(str, objArr, element, 1, exc);
    }

    /* access modifiers changed from: package-private */
    public void reportSchemaWarning(String str, Object[] objArr, Element element) {
        reportSchemaErr(str, objArr, element, 0, null);
    }

    /* access modifiers changed from: package-private */
    public void reportSchemaWarning(String str, Object[] objArr, Element element, Exception exc) {
        reportSchemaErr(str, objArr, element, 0, exc);
    }

    /* access modifiers changed from: package-private */
    public void reportSchemaErr(String str, Object[] objArr, Element element, short s, Exception exc) {
        if (element2Locator(element, this.xl)) {
            this.fErrorReporter.reportError(this.xl, XSMessageFormatter.SCHEMA_DOMAIN, str, objArr, s, exc);
        } else {
            this.fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN, str, objArr, s, exc);
        }
    }

    /* access modifiers changed from: private */
    public static class XSAnnotationGrammarPool implements XMLGrammarPool {
        private XSGrammarBucket fGrammarBucket;
        private Grammar[] fInitialGrammarSet;

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public void cacheGrammars(String str, Grammar[] grammarArr) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public void clear() {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public void lockPool() {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public void unlockPool() {
        }

        private XSAnnotationGrammarPool() {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public Grammar[] retrieveInitialGrammarSet(String str) {
            if (str != "http://www.w3.org/2001/XMLSchema") {
                return new Grammar[0];
            }
            if (this.fInitialGrammarSet == null) {
                XSGrammarBucket xSGrammarBucket = this.fGrammarBucket;
                if (xSGrammarBucket == null) {
                    this.fInitialGrammarSet = new Grammar[]{SchemaGrammar.Schema4Annotations.INSTANCE};
                } else {
                    SchemaGrammar[] grammars = xSGrammarBucket.getGrammars();
                    for (SchemaGrammar schemaGrammar : grammars) {
                        if (SchemaSymbols.URI_SCHEMAFORSCHEMA.equals(schemaGrammar.getTargetNamespace())) {
                            this.fInitialGrammarSet = grammars;
                            return this.fInitialGrammarSet;
                        }
                    }
                    Grammar[] grammarArr = new Grammar[(grammars.length + 1)];
                    System.arraycopy(grammars, 0, grammarArr, 0, grammars.length);
                    grammarArr[grammarArr.length - 1] = SchemaGrammar.Schema4Annotations.INSTANCE;
                    this.fInitialGrammarSet = grammarArr;
                }
            }
            return this.fInitialGrammarSet;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public Grammar retrieveGrammar(XMLGrammarDescription xMLGrammarDescription) {
            SchemaGrammar grammar;
            if (xMLGrammarDescription.getGrammarType() != "http://www.w3.org/2001/XMLSchema") {
                return null;
            }
            String targetNamespace = ((XMLSchemaDescription) xMLGrammarDescription).getTargetNamespace();
            XSGrammarBucket xSGrammarBucket = this.fGrammarBucket;
            if (xSGrammarBucket != null && (grammar = xSGrammarBucket.getGrammar(targetNamespace)) != null) {
                return grammar;
            }
            if (SchemaSymbols.URI_SCHEMAFORSCHEMA.equals(targetNamespace)) {
                return SchemaGrammar.Schema4Annotations.INSTANCE;
            }
            return null;
        }

        public void refreshGrammars(XSGrammarBucket xSGrammarBucket) {
            this.fGrammarBucket = xSGrammarBucket;
            this.fInitialGrammarSet = null;
        }
    }

    /* access modifiers changed from: private */
    public static class XSDKey {
        String referNS;
        short referType;
        String systemId;

        XSDKey(String str, short s, String str2) {
            this.systemId = str;
            this.referType = s;
            this.referNS = str2;
        }

        public int hashCode() {
            String str = this.referNS;
            if (str == null) {
                return 0;
            }
            return str.hashCode();
        }

        public boolean equals(Object obj) {
            String str;
            if (!(obj instanceof XSDKey)) {
                return false;
            }
            XSDKey xSDKey = (XSDKey) obj;
            if (this.referNS == xSDKey.referNS && (str = this.systemId) != null && str.equals(xSDKey.systemId)) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static final class SAX2XNIUtil extends ErrorHandlerWrapper {
        private SAX2XNIUtil() {
        }

        public static XMLParseException createXMLParseException0(SAXParseException sAXParseException) {
            return createXMLParseException(sAXParseException);
        }

        public static XNIException createXNIException0(SAXException sAXException) {
            return createXNIException(sAXException);
        }
    }

    public void setGenerateSyntheticAnnotations(boolean z) {
        this.fSchemaParser.setFeature("http://apache.org/xml/features/generate-synthetic-annotations", z);
    }
}
