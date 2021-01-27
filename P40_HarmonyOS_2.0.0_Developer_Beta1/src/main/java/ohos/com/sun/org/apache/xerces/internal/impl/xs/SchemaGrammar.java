package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import java.lang.ref.SoftReference;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidatedInfo;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.identity.IdentityConstraint;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.ObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.SimpleLocator;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.StringListImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSNamedMap4Types;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSNamedMapImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.parsers.DOMParser;
import ohos.com.sun.org.apache.xerces.internal.parsers.SAXParser;
import ohos.com.sun.org.apache.xerces.internal.parsers.XML11Configuration;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolHash;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XSGrammar;
import ohos.com.sun.org.apache.xerces.internal.xs.StringList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAnnotation;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeGroupDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSElementDeclaration;
import ohos.com.sun.org.apache.xerces.internal.xs.XSModel;
import ohos.com.sun.org.apache.xerces.internal.xs.XSModelGroupDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamedMap;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNotationDeclaration;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSParticle;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSWildcard;
import ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList;
import ohos.org.xml.sax.SAXException;

public class SchemaGrammar implements XSGrammar, XSNamespaceItem {
    private static final int BASICSET_COUNT = 29;
    private static final int FULLSET_COUNT = 46;
    private static final boolean[] GLOBAL_COMP = {false, true, true, true, false, true, true, false, false, false, false, true, false, false, false, true, true};
    private static final int GRAMMAR_XS = 1;
    private static final int GRAMMAR_XSI = 2;
    private static final int INC_SIZE = 16;
    private static final int INITIAL_SIZE = 16;
    private static final short MAX_COMP_IDX = 16;
    private static final int REDEFINED_GROUP_INIT_SIZE = 2;
    public static final BuiltinSchemaGrammar SG_SchemaNS = new BuiltinSchemaGrammar(1, 1);
    private static final BuiltinSchemaGrammar SG_SchemaNSExtended = new BuiltinSchemaGrammar(1, 2);
    public static final BuiltinSchemaGrammar SG_XSI = new BuiltinSchemaGrammar(2, 1);
    public static final XSSimpleType fAnySimpleType = ((XSSimpleType) SG_SchemaNS.getGlobalTypeDecl(SchemaSymbols.ATTVAL_ANYSIMPLETYPE));
    public static final XSComplexTypeDecl fAnyType = new XSAnyType();
    SymbolHash fAllGlobalElemDecls;
    XSAnnotationImpl[] fAnnotations;
    private int fCTCount;
    private SimpleLocator[] fCTLocators;
    private XSComplexTypeDecl[] fComplexTypeDecls;
    private XSNamedMap[] fComponents;
    private ObjectList[] fComponentsExt;
    private SoftReference fDOMParser;
    private Vector fDocuments;
    boolean fFullChecked;
    SymbolHash fGlobalAttrDecls;
    SymbolHash fGlobalAttrDeclsExt;
    SymbolHash fGlobalAttrGrpDecls;
    SymbolHash fGlobalAttrGrpDeclsExt;
    SymbolHash fGlobalElemDecls;
    SymbolHash fGlobalElemDeclsExt;
    SymbolHash fGlobalGroupDecls;
    SymbolHash fGlobalGroupDeclsExt;
    SymbolHash fGlobalIDConstraintDecls;
    SymbolHash fGlobalIDConstraintDeclsExt;
    SymbolHash fGlobalNotationDecls;
    SymbolHash fGlobalNotationDeclsExt;
    SymbolHash fGlobalTypeDecls;
    SymbolHash fGlobalTypeDeclsExt;
    XSDDescription fGrammarDescription;
    Vector fImported;
    private boolean fIsImmutable;
    private Vector fLocations;
    int fNumAnnotations;
    private int fRGCount;
    private SimpleLocator[] fRGLocators;
    private XSGroupDecl[] fRedefinedGroupDecls;
    private SoftReference fSAXParser;
    private int fSubGroupCount;
    private XSElementDecl[] fSubGroups;
    private SymbolTable fSymbolTable;
    String fTargetNamespace;

    public boolean isNamespaceAware() {
        return true;
    }

    protected SchemaGrammar() {
        this.fGrammarDescription = null;
        this.fAnnotations = null;
        this.fSymbolTable = null;
        this.fSAXParser = null;
        this.fDOMParser = null;
        this.fIsImmutable = false;
        this.fImported = null;
        this.fCTCount = 0;
        this.fComplexTypeDecls = new XSComplexTypeDecl[16];
        this.fCTLocators = new SimpleLocator[16];
        this.fRGCount = 0;
        this.fRedefinedGroupDecls = new XSGroupDecl[2];
        this.fRGLocators = new SimpleLocator[1];
        this.fFullChecked = false;
        this.fSubGroupCount = 0;
        this.fSubGroups = new XSElementDecl[16];
        this.fComponents = null;
        this.fComponentsExt = null;
        this.fDocuments = null;
        this.fLocations = null;
    }

    public SchemaGrammar(String str, XSDDescription xSDDescription, SymbolTable symbolTable) {
        this.fGrammarDescription = null;
        this.fAnnotations = null;
        this.fSymbolTable = null;
        this.fSAXParser = null;
        this.fDOMParser = null;
        this.fIsImmutable = false;
        this.fImported = null;
        this.fCTCount = 0;
        this.fComplexTypeDecls = new XSComplexTypeDecl[16];
        this.fCTLocators = new SimpleLocator[16];
        this.fRGCount = 0;
        this.fRedefinedGroupDecls = new XSGroupDecl[2];
        this.fRGLocators = new SimpleLocator[1];
        this.fFullChecked = false;
        this.fSubGroupCount = 0;
        this.fSubGroups = new XSElementDecl[16];
        this.fComponents = null;
        this.fComponentsExt = null;
        this.fDocuments = null;
        this.fLocations = null;
        this.fTargetNamespace = str;
        this.fGrammarDescription = xSDDescription;
        this.fSymbolTable = symbolTable;
        this.fGlobalAttrDecls = new SymbolHash();
        this.fGlobalAttrGrpDecls = new SymbolHash();
        this.fGlobalElemDecls = new SymbolHash();
        this.fGlobalGroupDecls = new SymbolHash();
        this.fGlobalNotationDecls = new SymbolHash();
        this.fGlobalIDConstraintDecls = new SymbolHash();
        this.fGlobalAttrDeclsExt = new SymbolHash();
        this.fGlobalAttrGrpDeclsExt = new SymbolHash();
        this.fGlobalElemDeclsExt = new SymbolHash();
        this.fGlobalGroupDeclsExt = new SymbolHash();
        this.fGlobalNotationDeclsExt = new SymbolHash();
        this.fGlobalIDConstraintDeclsExt = new SymbolHash();
        this.fGlobalTypeDeclsExt = new SymbolHash();
        this.fAllGlobalElemDecls = new SymbolHash();
        if (this.fTargetNamespace == SchemaSymbols.URI_SCHEMAFORSCHEMA) {
            this.fGlobalTypeDecls = SG_SchemaNS.fGlobalTypeDecls.makeClone();
        } else {
            this.fGlobalTypeDecls = new SymbolHash();
        }
    }

    public SchemaGrammar(SchemaGrammar schemaGrammar) {
        this.fGrammarDescription = null;
        this.fAnnotations = null;
        this.fSymbolTable = null;
        this.fSAXParser = null;
        this.fDOMParser = null;
        this.fIsImmutable = false;
        this.fImported = null;
        this.fCTCount = 0;
        this.fComplexTypeDecls = new XSComplexTypeDecl[16];
        this.fCTLocators = new SimpleLocator[16];
        this.fRGCount = 0;
        this.fRedefinedGroupDecls = new XSGroupDecl[2];
        this.fRGLocators = new SimpleLocator[1];
        this.fFullChecked = false;
        this.fSubGroupCount = 0;
        this.fSubGroups = new XSElementDecl[16];
        this.fComponents = null;
        this.fComponentsExt = null;
        this.fDocuments = null;
        this.fLocations = null;
        this.fTargetNamespace = schemaGrammar.fTargetNamespace;
        this.fGrammarDescription = schemaGrammar.fGrammarDescription.makeClone();
        this.fSymbolTable = schemaGrammar.fSymbolTable;
        this.fGlobalAttrDecls = schemaGrammar.fGlobalAttrDecls.makeClone();
        this.fGlobalAttrGrpDecls = schemaGrammar.fGlobalAttrGrpDecls.makeClone();
        this.fGlobalElemDecls = schemaGrammar.fGlobalElemDecls.makeClone();
        this.fGlobalGroupDecls = schemaGrammar.fGlobalGroupDecls.makeClone();
        this.fGlobalNotationDecls = schemaGrammar.fGlobalNotationDecls.makeClone();
        this.fGlobalIDConstraintDecls = schemaGrammar.fGlobalIDConstraintDecls.makeClone();
        this.fGlobalTypeDecls = schemaGrammar.fGlobalTypeDecls.makeClone();
        this.fGlobalAttrDeclsExt = schemaGrammar.fGlobalAttrDeclsExt.makeClone();
        this.fGlobalAttrGrpDeclsExt = schemaGrammar.fGlobalAttrGrpDeclsExt.makeClone();
        this.fGlobalElemDeclsExt = schemaGrammar.fGlobalElemDeclsExt.makeClone();
        this.fGlobalGroupDeclsExt = schemaGrammar.fGlobalGroupDeclsExt.makeClone();
        this.fGlobalNotationDeclsExt = schemaGrammar.fGlobalNotationDeclsExt.makeClone();
        this.fGlobalIDConstraintDeclsExt = schemaGrammar.fGlobalIDConstraintDeclsExt.makeClone();
        this.fGlobalTypeDeclsExt = schemaGrammar.fGlobalTypeDeclsExt.makeClone();
        this.fAllGlobalElemDecls = schemaGrammar.fAllGlobalElemDecls.makeClone();
        this.fNumAnnotations = schemaGrammar.fNumAnnotations;
        int i = this.fNumAnnotations;
        if (i > 0) {
            this.fAnnotations = new XSAnnotationImpl[schemaGrammar.fAnnotations.length];
            System.arraycopy(schemaGrammar.fAnnotations, 0, this.fAnnotations, 0, i);
        }
        this.fSubGroupCount = schemaGrammar.fSubGroupCount;
        int i2 = this.fSubGroupCount;
        if (i2 > 0) {
            this.fSubGroups = new XSElementDecl[schemaGrammar.fSubGroups.length];
            System.arraycopy(schemaGrammar.fSubGroups, 0, this.fSubGroups, 0, i2);
        }
        this.fCTCount = schemaGrammar.fCTCount;
        int i3 = this.fCTCount;
        if (i3 > 0) {
            this.fComplexTypeDecls = new XSComplexTypeDecl[schemaGrammar.fComplexTypeDecls.length];
            this.fCTLocators = new SimpleLocator[schemaGrammar.fCTLocators.length];
            System.arraycopy(schemaGrammar.fComplexTypeDecls, 0, this.fComplexTypeDecls, 0, i3);
            System.arraycopy(schemaGrammar.fCTLocators, 0, this.fCTLocators, 0, this.fCTCount);
        }
        this.fRGCount = schemaGrammar.fRGCount;
        int i4 = this.fRGCount;
        if (i4 > 0) {
            this.fRedefinedGroupDecls = new XSGroupDecl[schemaGrammar.fRedefinedGroupDecls.length];
            this.fRGLocators = new SimpleLocator[schemaGrammar.fRGLocators.length];
            System.arraycopy(schemaGrammar.fRedefinedGroupDecls, 0, this.fRedefinedGroupDecls, 0, i4);
            System.arraycopy(schemaGrammar.fRGLocators, 0, this.fRGLocators, 0, this.fRGCount);
        }
        if (schemaGrammar.fImported != null) {
            this.fImported = new Vector();
            for (int i5 = 0; i5 < schemaGrammar.fImported.size(); i5++) {
                this.fImported.add(schemaGrammar.fImported.elementAt(i5));
            }
        }
        if (schemaGrammar.fLocations != null) {
            for (int i6 = 0; i6 < schemaGrammar.fLocations.size(); i6++) {
                addDocument(null, (String) schemaGrammar.fLocations.elementAt(i6));
            }
        }
    }

    public static class BuiltinSchemaGrammar extends SchemaGrammar {
        private static final String EXTENDED_SCHEMA_FACTORY_CLASS = "ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.ExtendedSchemaDVFactoryImpl";

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addComplexTypeDecl(XSComplexTypeDecl xSComplexTypeDecl, SimpleLocator simpleLocator) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalAttributeDecl(XSAttributeDecl xSAttributeDecl) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalAttributeDecl(XSAttributeDecl xSAttributeDecl, String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalAttributeGroupDecl(XSAttributeGroupDecl xSAttributeGroupDecl) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalAttributeGroupDecl(XSAttributeGroupDecl xSAttributeGroupDecl, String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalComplexTypeDecl(XSComplexTypeDecl xSComplexTypeDecl) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalComplexTypeDecl(XSComplexTypeDecl xSComplexTypeDecl, String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalElementDecl(XSElementDecl xSElementDecl) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalElementDecl(XSElementDecl xSElementDecl, String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalElementDeclAll(XSElementDecl xSElementDecl) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalGroupDecl(XSGroupDecl xSGroupDecl) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalGroupDecl(XSGroupDecl xSGroupDecl, String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalNotationDecl(XSNotationDecl xSNotationDecl) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalNotationDecl(XSNotationDecl xSNotationDecl, String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalSimpleTypeDecl(XSSimpleType xSSimpleType) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalSimpleTypeDecl(XSSimpleType xSSimpleType, String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalTypeDecl(XSTypeDefinition xSTypeDefinition) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalTypeDecl(XSTypeDefinition xSTypeDefinition, String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addRedefinedGroupDecl(XSGroupDecl xSGroupDecl, XSGroupDecl xSGroupDecl2, SimpleLocator simpleLocator) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void setImportedGrammars(Vector vector) {
        }

        public BuiltinSchemaGrammar(int i, short s) {
            SchemaDVFactory schemaDVFactory;
            if (s == 1) {
                schemaDVFactory = SchemaDVFactory.getInstance();
            } else {
                schemaDVFactory = SchemaDVFactory.getInstance(EXTENDED_SCHEMA_FACTORY_CLASS);
            }
            if (i == 1) {
                this.fTargetNamespace = SchemaSymbols.URI_SCHEMAFORSCHEMA;
                this.fGrammarDescription = new XSDDescription();
                this.fGrammarDescription.fContextType = 3;
                this.fGrammarDescription.setNamespace(SchemaSymbols.URI_SCHEMAFORSCHEMA);
                this.fGlobalAttrDecls = new SymbolHash(1);
                this.fGlobalAttrGrpDecls = new SymbolHash(1);
                this.fGlobalElemDecls = new SymbolHash(1);
                this.fGlobalGroupDecls = new SymbolHash(1);
                this.fGlobalNotationDecls = new SymbolHash(1);
                this.fGlobalIDConstraintDecls = new SymbolHash(1);
                this.fGlobalAttrDeclsExt = new SymbolHash(1);
                this.fGlobalAttrGrpDeclsExt = new SymbolHash(1);
                this.fGlobalElemDeclsExt = new SymbolHash(1);
                this.fGlobalGroupDeclsExt = new SymbolHash(1);
                this.fGlobalNotationDeclsExt = new SymbolHash(1);
                this.fGlobalIDConstraintDeclsExt = new SymbolHash(1);
                this.fGlobalTypeDeclsExt = new SymbolHash(1);
                this.fAllGlobalElemDecls = new SymbolHash(1);
                this.fGlobalTypeDecls = schemaDVFactory.getBuiltInTypes();
                int length = this.fGlobalTypeDecls.getLength();
                XSTypeDefinition[] xSTypeDefinitionArr = new XSTypeDefinition[length];
                this.fGlobalTypeDecls.getValues(xSTypeDefinitionArr, 0);
                for (int i2 = 0; i2 < length; i2++) {
                    XSTypeDefinition xSTypeDefinition = xSTypeDefinitionArr[i2];
                    if (xSTypeDefinition instanceof XSSimpleTypeDecl) {
                        ((XSSimpleTypeDecl) xSTypeDefinition).setNamespaceItem(this);
                    }
                }
                this.fGlobalTypeDecls.put(fAnyType.getName(), fAnyType);
            } else if (i == 2) {
                this.fTargetNamespace = SchemaSymbols.URI_XSI;
                this.fGrammarDescription = new XSDDescription();
                this.fGrammarDescription.fContextType = 3;
                this.fGrammarDescription.setNamespace(SchemaSymbols.URI_XSI);
                this.fGlobalAttrGrpDecls = new SymbolHash(1);
                this.fGlobalElemDecls = new SymbolHash(1);
                this.fGlobalGroupDecls = new SymbolHash(1);
                this.fGlobalNotationDecls = new SymbolHash(1);
                this.fGlobalIDConstraintDecls = new SymbolHash(1);
                this.fGlobalTypeDecls = new SymbolHash(1);
                this.fGlobalAttrDeclsExt = new SymbolHash(1);
                this.fGlobalAttrGrpDeclsExt = new SymbolHash(1);
                this.fGlobalElemDeclsExt = new SymbolHash(1);
                this.fGlobalGroupDeclsExt = new SymbolHash(1);
                this.fGlobalNotationDeclsExt = new SymbolHash(1);
                this.fGlobalIDConstraintDeclsExt = new SymbolHash(1);
                this.fGlobalTypeDeclsExt = new SymbolHash(1);
                this.fAllGlobalElemDecls = new SymbolHash(1);
                this.fGlobalAttrDecls = new SymbolHash(8);
                String str = SchemaSymbols.XSI_TYPE;
                this.fGlobalAttrDecls.put(str, new BuiltinAttrDecl(str, SchemaSymbols.URI_XSI, schemaDVFactory.getBuiltInType(SchemaSymbols.ATTVAL_QNAME), 1));
                String str2 = SchemaSymbols.XSI_NIL;
                this.fGlobalAttrDecls.put(str2, new BuiltinAttrDecl(str2, SchemaSymbols.URI_XSI, schemaDVFactory.getBuiltInType("boolean"), 1));
                XSSimpleType builtInType = schemaDVFactory.getBuiltInType(SchemaSymbols.ATTVAL_ANYURI);
                String str3 = SchemaSymbols.XSI_SCHEMALOCATION;
                String str4 = SchemaSymbols.URI_XSI;
                XSSimpleType createTypeList = schemaDVFactory.createTypeList("#AnonType_schemaLocation", SchemaSymbols.URI_XSI, 0, builtInType, null);
                if (createTypeList instanceof XSSimpleTypeDecl) {
                    ((XSSimpleTypeDecl) createTypeList).setAnonymous(true);
                }
                this.fGlobalAttrDecls.put(str3, new BuiltinAttrDecl(str3, str4, createTypeList, 1));
                String str5 = SchemaSymbols.XSI_NONAMESPACESCHEMALOCATION;
                this.fGlobalAttrDecls.put(str5, new BuiltinAttrDecl(str5, SchemaSymbols.URI_XSI, builtInType, 1));
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar, ohos.com.sun.org.apache.xerces.internal.xni.grammars.Grammar
        public XMLGrammarDescription getGrammarDescription() {
            return this.fGrammarDescription.makeClone();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public synchronized void addDocument(Object obj, String str) {
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public synchronized DOMParser getDOMParser() {
            return null;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public synchronized SAXParser getSAXParser() {
            return null;
        }
    }

    public static final class Schema4Annotations extends SchemaGrammar {
        public static final Schema4Annotations INSTANCE = new Schema4Annotations();

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addComplexTypeDecl(XSComplexTypeDecl xSComplexTypeDecl, SimpleLocator simpleLocator) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalAttributeDecl(XSAttributeDecl xSAttributeDecl) {
        }

        public void addGlobalAttributeDecl(XSAttributeGroupDecl xSAttributeGroupDecl, String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalAttributeGroupDecl(XSAttributeGroupDecl xSAttributeGroupDecl) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalAttributeGroupDecl(XSAttributeGroupDecl xSAttributeGroupDecl, String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalComplexTypeDecl(XSComplexTypeDecl xSComplexTypeDecl) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalComplexTypeDecl(XSComplexTypeDecl xSComplexTypeDecl, String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalElementDecl(XSElementDecl xSElementDecl) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalElementDecl(XSElementDecl xSElementDecl, String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalElementDeclAll(XSElementDecl xSElementDecl) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalGroupDecl(XSGroupDecl xSGroupDecl) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalGroupDecl(XSGroupDecl xSGroupDecl, String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalNotationDecl(XSNotationDecl xSNotationDecl) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalNotationDecl(XSNotationDecl xSNotationDecl, String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalSimpleTypeDecl(XSSimpleType xSSimpleType) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalSimpleTypeDecl(XSSimpleType xSSimpleType, String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalTypeDecl(XSTypeDefinition xSTypeDefinition) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addGlobalTypeDecl(XSTypeDefinition xSTypeDefinition, String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void addRedefinedGroupDecl(XSGroupDecl xSGroupDecl, XSGroupDecl xSGroupDecl2, SimpleLocator simpleLocator) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public void setImportedGrammars(Vector vector) {
        }

        private Schema4Annotations() {
            this.fTargetNamespace = SchemaSymbols.URI_SCHEMAFORSCHEMA;
            this.fGrammarDescription = new XSDDescription();
            this.fGrammarDescription.fContextType = 3;
            this.fGrammarDescription.setNamespace(SchemaSymbols.URI_SCHEMAFORSCHEMA);
            this.fGlobalAttrDecls = new SymbolHash(1);
            this.fGlobalAttrGrpDecls = new SymbolHash(1);
            this.fGlobalElemDecls = new SymbolHash(6);
            this.fGlobalGroupDecls = new SymbolHash(1);
            this.fGlobalNotationDecls = new SymbolHash(1);
            this.fGlobalIDConstraintDecls = new SymbolHash(1);
            this.fGlobalAttrDeclsExt = new SymbolHash(1);
            this.fGlobalAttrGrpDeclsExt = new SymbolHash(1);
            this.fGlobalElemDeclsExt = new SymbolHash(6);
            this.fGlobalGroupDeclsExt = new SymbolHash(1);
            this.fGlobalNotationDeclsExt = new SymbolHash(1);
            this.fGlobalIDConstraintDeclsExt = new SymbolHash(1);
            this.fGlobalTypeDeclsExt = new SymbolHash(1);
            this.fAllGlobalElemDecls = new SymbolHash(6);
            this.fGlobalTypeDecls = SG_SchemaNS.fGlobalTypeDecls;
            XSElementDecl createAnnotationElementDecl = createAnnotationElementDecl(SchemaSymbols.ELT_ANNOTATION);
            XSElementDecl createAnnotationElementDecl2 = createAnnotationElementDecl(SchemaSymbols.ELT_DOCUMENTATION);
            XSElementDecl createAnnotationElementDecl3 = createAnnotationElementDecl(SchemaSymbols.ELT_APPINFO);
            this.fGlobalElemDecls.put(createAnnotationElementDecl.fName, createAnnotationElementDecl);
            this.fGlobalElemDecls.put(createAnnotationElementDecl2.fName, createAnnotationElementDecl2);
            this.fGlobalElemDecls.put(createAnnotationElementDecl3.fName, createAnnotationElementDecl3);
            SymbolHash symbolHash = this.fGlobalElemDeclsExt;
            symbolHash.put("," + createAnnotationElementDecl.fName, createAnnotationElementDecl);
            SymbolHash symbolHash2 = this.fGlobalElemDeclsExt;
            symbolHash2.put("," + createAnnotationElementDecl2.fName, createAnnotationElementDecl2);
            SymbolHash symbolHash3 = this.fGlobalElemDeclsExt;
            symbolHash3.put("," + createAnnotationElementDecl3.fName, createAnnotationElementDecl3);
            this.fAllGlobalElemDecls.put(createAnnotationElementDecl, createAnnotationElementDecl);
            this.fAllGlobalElemDecls.put(createAnnotationElementDecl2, createAnnotationElementDecl2);
            this.fAllGlobalElemDecls.put(createAnnotationElementDecl3, createAnnotationElementDecl3);
            XSComplexTypeDecl xSComplexTypeDecl = new XSComplexTypeDecl();
            XSComplexTypeDecl xSComplexTypeDecl2 = new XSComplexTypeDecl();
            XSComplexTypeDecl xSComplexTypeDecl3 = new XSComplexTypeDecl();
            createAnnotationElementDecl.fType = xSComplexTypeDecl;
            createAnnotationElementDecl2.fType = xSComplexTypeDecl2;
            createAnnotationElementDecl3.fType = xSComplexTypeDecl3;
            XSAttributeGroupDecl xSAttributeGroupDecl = new XSAttributeGroupDecl();
            XSAttributeGroupDecl xSAttributeGroupDecl2 = new XSAttributeGroupDecl();
            XSAttributeGroupDecl xSAttributeGroupDecl3 = new XSAttributeGroupDecl();
            XSAttributeUseImpl xSAttributeUseImpl = new XSAttributeUseImpl();
            xSAttributeUseImpl.fAttrDecl = new XSAttributeDecl();
            xSAttributeUseImpl.fAttrDecl.setValues(SchemaSymbols.ATT_ID, null, (XSSimpleType) this.fGlobalTypeDecls.get(SchemaSymbols.ATTVAL_ID), 0, 2, null, xSComplexTypeDecl, null);
            xSAttributeUseImpl.fUse = 0;
            xSAttributeUseImpl.fConstraintType = 0;
            XSAttributeUseImpl xSAttributeUseImpl2 = new XSAttributeUseImpl();
            xSAttributeUseImpl2.fAttrDecl = new XSAttributeDecl();
            xSAttributeUseImpl2.fAttrDecl.setValues(SchemaSymbols.ATT_SOURCE, null, (XSSimpleType) this.fGlobalTypeDecls.get(SchemaSymbols.ATTVAL_ANYURI), 0, 2, null, xSComplexTypeDecl2, null);
            xSAttributeUseImpl2.fUse = 0;
            xSAttributeUseImpl2.fConstraintType = 0;
            XSAttributeUseImpl xSAttributeUseImpl3 = new XSAttributeUseImpl();
            xSAttributeUseImpl3.fAttrDecl = new XSAttributeDecl();
            xSAttributeUseImpl3.fAttrDecl.setValues("lang".intern(), NamespaceContext.XML_URI, (XSSimpleType) this.fGlobalTypeDecls.get("language"), 0, 2, null, xSComplexTypeDecl2, null);
            xSAttributeUseImpl3.fUse = 0;
            xSAttributeUseImpl3.fConstraintType = 0;
            XSAttributeUseImpl xSAttributeUseImpl4 = new XSAttributeUseImpl();
            xSAttributeUseImpl4.fAttrDecl = new XSAttributeDecl();
            xSAttributeUseImpl4.fAttrDecl.setValues(SchemaSymbols.ATT_SOURCE, null, (XSSimpleType) this.fGlobalTypeDecls.get(SchemaSymbols.ATTVAL_ANYURI), 0, 2, null, xSComplexTypeDecl3, null);
            xSAttributeUseImpl4.fUse = 0;
            xSAttributeUseImpl4.fConstraintType = 0;
            XSWildcardDecl xSWildcardDecl = new XSWildcardDecl();
            xSWildcardDecl.fNamespaceList = new String[]{this.fTargetNamespace, null};
            xSWildcardDecl.fType = 2;
            xSWildcardDecl.fProcessContents = 3;
            xSAttributeGroupDecl.addAttributeUse(xSAttributeUseImpl);
            xSAttributeGroupDecl.fAttributeWC = xSWildcardDecl;
            xSAttributeGroupDecl2.addAttributeUse(xSAttributeUseImpl2);
            xSAttributeGroupDecl2.addAttributeUse(xSAttributeUseImpl3);
            xSAttributeGroupDecl2.fAttributeWC = xSWildcardDecl;
            xSAttributeGroupDecl3.addAttributeUse(xSAttributeUseImpl4);
            xSAttributeGroupDecl3.fAttributeWC = xSWildcardDecl;
            XSParticleDecl createUnboundedModelGroupParticle = createUnboundedModelGroupParticle();
            XSModelGroupImpl xSModelGroupImpl = new XSModelGroupImpl();
            xSModelGroupImpl.fCompositor = 101;
            xSModelGroupImpl.fParticleCount = 2;
            xSModelGroupImpl.fParticles = new XSParticleDecl[2];
            xSModelGroupImpl.fParticles[0] = createChoiceElementParticle(createAnnotationElementDecl3);
            xSModelGroupImpl.fParticles[1] = createChoiceElementParticle(createAnnotationElementDecl2);
            createUnboundedModelGroupParticle.fValue = xSModelGroupImpl;
            XSParticleDecl createUnboundedAnyWildcardSequenceParticle = createUnboundedAnyWildcardSequenceParticle();
            xSComplexTypeDecl.setValues("#AnonType_" + SchemaSymbols.ELT_ANNOTATION, this.fTargetNamespace, SchemaGrammar.fAnyType, 2, 0, 3, 2, false, xSAttributeGroupDecl, null, createUnboundedModelGroupParticle, new XSObjectListImpl(null, 0));
            xSComplexTypeDecl.setName("#AnonType_" + SchemaSymbols.ELT_ANNOTATION);
            xSComplexTypeDecl.setIsAnonymous();
            xSComplexTypeDecl2.setValues("#AnonType_" + SchemaSymbols.ELT_DOCUMENTATION, this.fTargetNamespace, SchemaGrammar.fAnyType, 2, 0, 3, 3, false, xSAttributeGroupDecl2, null, createUnboundedAnyWildcardSequenceParticle, new XSObjectListImpl(null, 0));
            xSComplexTypeDecl2.setName("#AnonType_" + SchemaSymbols.ELT_DOCUMENTATION);
            xSComplexTypeDecl2.setIsAnonymous();
            xSComplexTypeDecl3.setValues("#AnonType_" + SchemaSymbols.ELT_APPINFO, this.fTargetNamespace, SchemaGrammar.fAnyType, 2, 0, 3, 3, false, xSAttributeGroupDecl3, null, createUnboundedAnyWildcardSequenceParticle, new XSObjectListImpl(null, 0));
            xSComplexTypeDecl3.setName("#AnonType_" + SchemaSymbols.ELT_APPINFO);
            xSComplexTypeDecl3.setIsAnonymous();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar, ohos.com.sun.org.apache.xerces.internal.xni.grammars.Grammar
        public XMLGrammarDescription getGrammarDescription() {
            return this.fGrammarDescription.makeClone();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public synchronized void addDocument(Object obj, String str) {
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public synchronized DOMParser getDOMParser() {
            return null;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar
        public synchronized SAXParser getSAXParser() {
            return null;
        }

        private XSElementDecl createAnnotationElementDecl(String str) {
            XSElementDecl xSElementDecl = new XSElementDecl();
            xSElementDecl.fName = str;
            xSElementDecl.fTargetNamespace = this.fTargetNamespace;
            xSElementDecl.setIsGlobal();
            xSElementDecl.fBlock = 7;
            xSElementDecl.setConstraintType(0);
            return xSElementDecl;
        }

        private XSParticleDecl createUnboundedModelGroupParticle() {
            XSParticleDecl xSParticleDecl = new XSParticleDecl();
            xSParticleDecl.fMinOccurs = 0;
            xSParticleDecl.fMaxOccurs = -1;
            xSParticleDecl.fType = 3;
            return xSParticleDecl;
        }

        private XSParticleDecl createChoiceElementParticle(XSElementDecl xSElementDecl) {
            XSParticleDecl xSParticleDecl = new XSParticleDecl();
            xSParticleDecl.fMinOccurs = 1;
            xSParticleDecl.fMaxOccurs = 1;
            xSParticleDecl.fType = 1;
            xSParticleDecl.fValue = xSElementDecl;
            return xSParticleDecl;
        }

        private XSParticleDecl createUnboundedAnyWildcardSequenceParticle() {
            XSParticleDecl createUnboundedModelGroupParticle = createUnboundedModelGroupParticle();
            XSModelGroupImpl xSModelGroupImpl = new XSModelGroupImpl();
            xSModelGroupImpl.fCompositor = 102;
            xSModelGroupImpl.fParticleCount = 1;
            xSModelGroupImpl.fParticles = new XSParticleDecl[1];
            xSModelGroupImpl.fParticles[0] = createAnyLaxWildcardParticle();
            createUnboundedModelGroupParticle.fValue = xSModelGroupImpl;
            return createUnboundedModelGroupParticle;
        }

        private XSParticleDecl createAnyLaxWildcardParticle() {
            XSParticleDecl xSParticleDecl = new XSParticleDecl();
            xSParticleDecl.fMinOccurs = 1;
            xSParticleDecl.fMaxOccurs = 1;
            xSParticleDecl.fType = 2;
            XSWildcardDecl xSWildcardDecl = new XSWildcardDecl();
            xSWildcardDecl.fNamespaceList = null;
            xSWildcardDecl.fType = 1;
            xSWildcardDecl.fProcessContents = 3;
            xSParticleDecl.fValue = xSWildcardDecl;
            return xSParticleDecl;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.Grammar
    public XMLGrammarDescription getGrammarDescription() {
        return this.fGrammarDescription;
    }

    public void setImportedGrammars(Vector vector) {
        this.fImported = vector;
    }

    public Vector getImportedGrammars() {
        return this.fImported;
    }

    public final String getTargetNamespace() {
        return this.fTargetNamespace;
    }

    public void addGlobalAttributeDecl(XSAttributeDecl xSAttributeDecl) {
        this.fGlobalAttrDecls.put(xSAttributeDecl.fName, xSAttributeDecl);
        xSAttributeDecl.setNamespaceItem(this);
    }

    public void addGlobalAttributeDecl(XSAttributeDecl xSAttributeDecl, String str) {
        SymbolHash symbolHash = this.fGlobalAttrDeclsExt;
        StringBuilder sb = new StringBuilder();
        if (str == null) {
            str = "";
        }
        sb.append(str);
        sb.append(",");
        sb.append(xSAttributeDecl.fName);
        symbolHash.put(sb.toString(), xSAttributeDecl);
        if (xSAttributeDecl.getNamespaceItem() == null) {
            xSAttributeDecl.setNamespaceItem(this);
        }
    }

    public void addGlobalAttributeGroupDecl(XSAttributeGroupDecl xSAttributeGroupDecl) {
        this.fGlobalAttrGrpDecls.put(xSAttributeGroupDecl.fName, xSAttributeGroupDecl);
        xSAttributeGroupDecl.setNamespaceItem(this);
    }

    public void addGlobalAttributeGroupDecl(XSAttributeGroupDecl xSAttributeGroupDecl, String str) {
        SymbolHash symbolHash = this.fGlobalAttrGrpDeclsExt;
        StringBuilder sb = new StringBuilder();
        if (str == null) {
            str = "";
        }
        sb.append(str);
        sb.append(",");
        sb.append(xSAttributeGroupDecl.fName);
        symbolHash.put(sb.toString(), xSAttributeGroupDecl);
        if (xSAttributeGroupDecl.getNamespaceItem() == null) {
            xSAttributeGroupDecl.setNamespaceItem(this);
        }
    }

    public void addGlobalElementDeclAll(XSElementDecl xSElementDecl) {
        if (this.fAllGlobalElemDecls.get(xSElementDecl) == null) {
            this.fAllGlobalElemDecls.put(xSElementDecl, xSElementDecl);
            if (xSElementDecl.fSubGroup != null) {
                int i = this.fSubGroupCount;
                XSElementDecl[] xSElementDeclArr = this.fSubGroups;
                if (i == xSElementDeclArr.length) {
                    this.fSubGroups = resize(xSElementDeclArr, i + 16);
                }
                XSElementDecl[] xSElementDeclArr2 = this.fSubGroups;
                int i2 = this.fSubGroupCount;
                this.fSubGroupCount = i2 + 1;
                xSElementDeclArr2[i2] = xSElementDecl;
            }
        }
    }

    public void addGlobalElementDecl(XSElementDecl xSElementDecl) {
        this.fGlobalElemDecls.put(xSElementDecl.fName, xSElementDecl);
        xSElementDecl.setNamespaceItem(this);
    }

    public void addGlobalElementDecl(XSElementDecl xSElementDecl, String str) {
        SymbolHash symbolHash = this.fGlobalElemDeclsExt;
        StringBuilder sb = new StringBuilder();
        if (str == null) {
            str = "";
        }
        sb.append(str);
        sb.append(",");
        sb.append(xSElementDecl.fName);
        symbolHash.put(sb.toString(), xSElementDecl);
        if (xSElementDecl.getNamespaceItem() == null) {
            xSElementDecl.setNamespaceItem(this);
        }
    }

    public void addGlobalGroupDecl(XSGroupDecl xSGroupDecl) {
        this.fGlobalGroupDecls.put(xSGroupDecl.fName, xSGroupDecl);
        xSGroupDecl.setNamespaceItem(this);
    }

    public void addGlobalGroupDecl(XSGroupDecl xSGroupDecl, String str) {
        SymbolHash symbolHash = this.fGlobalGroupDeclsExt;
        StringBuilder sb = new StringBuilder();
        if (str == null) {
            str = "";
        }
        sb.append(str);
        sb.append(",");
        sb.append(xSGroupDecl.fName);
        symbolHash.put(sb.toString(), xSGroupDecl);
        if (xSGroupDecl.getNamespaceItem() == null) {
            xSGroupDecl.setNamespaceItem(this);
        }
    }

    public void addGlobalNotationDecl(XSNotationDecl xSNotationDecl) {
        this.fGlobalNotationDecls.put(xSNotationDecl.fName, xSNotationDecl);
        xSNotationDecl.setNamespaceItem(this);
    }

    public void addGlobalNotationDecl(XSNotationDecl xSNotationDecl, String str) {
        SymbolHash symbolHash = this.fGlobalNotationDeclsExt;
        StringBuilder sb = new StringBuilder();
        if (str == null) {
            str = "";
        }
        sb.append(str);
        sb.append(",");
        sb.append(xSNotationDecl.fName);
        symbolHash.put(sb.toString(), xSNotationDecl);
        if (xSNotationDecl.getNamespaceItem() == null) {
            xSNotationDecl.setNamespaceItem(this);
        }
    }

    public void addGlobalTypeDecl(XSTypeDefinition xSTypeDefinition) {
        this.fGlobalTypeDecls.put(xSTypeDefinition.getName(), xSTypeDefinition);
        if (xSTypeDefinition instanceof XSComplexTypeDecl) {
            ((XSComplexTypeDecl) xSTypeDefinition).setNamespaceItem(this);
        } else if (xSTypeDefinition instanceof XSSimpleTypeDecl) {
            ((XSSimpleTypeDecl) xSTypeDefinition).setNamespaceItem(this);
        }
    }

    public void addGlobalTypeDecl(XSTypeDefinition xSTypeDefinition, String str) {
        SymbolHash symbolHash = this.fGlobalTypeDeclsExt;
        StringBuilder sb = new StringBuilder();
        if (str == null) {
            str = "";
        }
        sb.append(str);
        sb.append(",");
        sb.append(xSTypeDefinition.getName());
        symbolHash.put(sb.toString(), xSTypeDefinition);
        if (xSTypeDefinition.getNamespaceItem() != null) {
            return;
        }
        if (xSTypeDefinition instanceof XSComplexTypeDecl) {
            ((XSComplexTypeDecl) xSTypeDefinition).setNamespaceItem(this);
        } else if (xSTypeDefinition instanceof XSSimpleTypeDecl) {
            ((XSSimpleTypeDecl) xSTypeDefinition).setNamespaceItem(this);
        }
    }

    public void addGlobalComplexTypeDecl(XSComplexTypeDecl xSComplexTypeDecl) {
        this.fGlobalTypeDecls.put(xSComplexTypeDecl.getName(), xSComplexTypeDecl);
        xSComplexTypeDecl.setNamespaceItem(this);
    }

    public void addGlobalComplexTypeDecl(XSComplexTypeDecl xSComplexTypeDecl, String str) {
        SymbolHash symbolHash = this.fGlobalTypeDeclsExt;
        StringBuilder sb = new StringBuilder();
        if (str == null) {
            str = "";
        }
        sb.append(str);
        sb.append(",");
        sb.append(xSComplexTypeDecl.getName());
        symbolHash.put(sb.toString(), xSComplexTypeDecl);
        if (xSComplexTypeDecl.getNamespaceItem() == null) {
            xSComplexTypeDecl.setNamespaceItem(this);
        }
    }

    public void addGlobalSimpleTypeDecl(XSSimpleType xSSimpleType) {
        this.fGlobalTypeDecls.put(xSSimpleType.getName(), xSSimpleType);
        if (xSSimpleType instanceof XSSimpleTypeDecl) {
            ((XSSimpleTypeDecl) xSSimpleType).setNamespaceItem(this);
        }
    }

    public void addGlobalSimpleTypeDecl(XSSimpleType xSSimpleType, String str) {
        SymbolHash symbolHash = this.fGlobalTypeDeclsExt;
        StringBuilder sb = new StringBuilder();
        if (str == null) {
            str = "";
        }
        sb.append(str);
        sb.append(",");
        sb.append(xSSimpleType.getName());
        symbolHash.put(sb.toString(), xSSimpleType);
        if (xSSimpleType.getNamespaceItem() == null && (xSSimpleType instanceof XSSimpleTypeDecl)) {
            ((XSSimpleTypeDecl) xSSimpleType).setNamespaceItem(this);
        }
    }

    public final void addIDConstraintDecl(XSElementDecl xSElementDecl, IdentityConstraint identityConstraint) {
        xSElementDecl.addIDConstraint(identityConstraint);
        this.fGlobalIDConstraintDecls.put(identityConstraint.getIdentityConstraintName(), identityConstraint);
    }

    public final void addIDConstraintDecl(XSElementDecl xSElementDecl, IdentityConstraint identityConstraint, String str) {
        SymbolHash symbolHash = this.fGlobalIDConstraintDeclsExt;
        StringBuilder sb = new StringBuilder();
        if (str == null) {
            str = "";
        }
        sb.append(str);
        sb.append(",");
        sb.append(identityConstraint.getIdentityConstraintName());
        symbolHash.put(sb.toString(), identityConstraint);
    }

    public final XSAttributeDecl getGlobalAttributeDecl(String str) {
        return (XSAttributeDecl) this.fGlobalAttrDecls.get(str);
    }

    public final XSAttributeDecl getGlobalAttributeDecl(String str, String str2) {
        SymbolHash symbolHash = this.fGlobalAttrDeclsExt;
        StringBuilder sb = new StringBuilder();
        if (str2 == null) {
            str2 = "";
        }
        sb.append(str2);
        sb.append(",");
        sb.append(str);
        return (XSAttributeDecl) symbolHash.get(sb.toString());
    }

    public final XSAttributeGroupDecl getGlobalAttributeGroupDecl(String str) {
        return (XSAttributeGroupDecl) this.fGlobalAttrGrpDecls.get(str);
    }

    public final XSAttributeGroupDecl getGlobalAttributeGroupDecl(String str, String str2) {
        SymbolHash symbolHash = this.fGlobalAttrGrpDeclsExt;
        StringBuilder sb = new StringBuilder();
        if (str2 == null) {
            str2 = "";
        }
        sb.append(str2);
        sb.append(",");
        sb.append(str);
        return (XSAttributeGroupDecl) symbolHash.get(sb.toString());
    }

    public final XSElementDecl getGlobalElementDecl(String str) {
        return (XSElementDecl) this.fGlobalElemDecls.get(str);
    }

    public final XSElementDecl getGlobalElementDecl(String str, String str2) {
        SymbolHash symbolHash = this.fGlobalElemDeclsExt;
        StringBuilder sb = new StringBuilder();
        if (str2 == null) {
            str2 = "";
        }
        sb.append(str2);
        sb.append(",");
        sb.append(str);
        return (XSElementDecl) symbolHash.get(sb.toString());
    }

    public final XSGroupDecl getGlobalGroupDecl(String str) {
        return (XSGroupDecl) this.fGlobalGroupDecls.get(str);
    }

    public final XSGroupDecl getGlobalGroupDecl(String str, String str2) {
        SymbolHash symbolHash = this.fGlobalGroupDeclsExt;
        StringBuilder sb = new StringBuilder();
        if (str2 == null) {
            str2 = "";
        }
        sb.append(str2);
        sb.append(",");
        sb.append(str);
        return (XSGroupDecl) symbolHash.get(sb.toString());
    }

    public final XSNotationDecl getGlobalNotationDecl(String str) {
        return (XSNotationDecl) this.fGlobalNotationDecls.get(str);
    }

    public final XSNotationDecl getGlobalNotationDecl(String str, String str2) {
        SymbolHash symbolHash = this.fGlobalNotationDeclsExt;
        StringBuilder sb = new StringBuilder();
        if (str2 == null) {
            str2 = "";
        }
        sb.append(str2);
        sb.append(",");
        sb.append(str);
        return (XSNotationDecl) symbolHash.get(sb.toString());
    }

    public final XSTypeDefinition getGlobalTypeDecl(String str) {
        return (XSTypeDefinition) this.fGlobalTypeDecls.get(str);
    }

    public final XSTypeDefinition getGlobalTypeDecl(String str, String str2) {
        SymbolHash symbolHash = this.fGlobalTypeDeclsExt;
        StringBuilder sb = new StringBuilder();
        if (str2 == null) {
            str2 = "";
        }
        sb.append(str2);
        sb.append(",");
        sb.append(str);
        return (XSTypeDefinition) symbolHash.get(sb.toString());
    }

    public final IdentityConstraint getIDConstraintDecl(String str) {
        return (IdentityConstraint) this.fGlobalIDConstraintDecls.get(str);
    }

    public final IdentityConstraint getIDConstraintDecl(String str, String str2) {
        SymbolHash symbolHash = this.fGlobalIDConstraintDeclsExt;
        StringBuilder sb = new StringBuilder();
        if (str2 == null) {
            str2 = "";
        }
        sb.append(str2);
        sb.append(",");
        sb.append(str);
        return (IdentityConstraint) symbolHash.get(sb.toString());
    }

    public final boolean hasIDConstraints() {
        return this.fGlobalIDConstraintDecls.getLength() > 0;
    }

    public void addComplexTypeDecl(XSComplexTypeDecl xSComplexTypeDecl, SimpleLocator simpleLocator) {
        int i = this.fCTCount;
        XSComplexTypeDecl[] xSComplexTypeDeclArr = this.fComplexTypeDecls;
        if (i == xSComplexTypeDeclArr.length) {
            this.fComplexTypeDecls = resize(xSComplexTypeDeclArr, i + 16);
            this.fCTLocators = resize(this.fCTLocators, this.fCTCount + 16);
        }
        SimpleLocator[] simpleLocatorArr = this.fCTLocators;
        int i2 = this.fCTCount;
        simpleLocatorArr[i2] = simpleLocator;
        XSComplexTypeDecl[] xSComplexTypeDeclArr2 = this.fComplexTypeDecls;
        this.fCTCount = i2 + 1;
        xSComplexTypeDeclArr2[i2] = xSComplexTypeDecl;
    }

    public void addRedefinedGroupDecl(XSGroupDecl xSGroupDecl, XSGroupDecl xSGroupDecl2, SimpleLocator simpleLocator) {
        int i = this.fRGCount;
        XSGroupDecl[] xSGroupDeclArr = this.fRedefinedGroupDecls;
        if (i == xSGroupDeclArr.length) {
            this.fRedefinedGroupDecls = resize(xSGroupDeclArr, i << 1);
            this.fRGLocators = resize(this.fRGLocators, this.fRGCount);
        }
        SimpleLocator[] simpleLocatorArr = this.fRGLocators;
        int i2 = this.fRGCount;
        simpleLocatorArr[i2 / 2] = simpleLocator;
        XSGroupDecl[] xSGroupDeclArr2 = this.fRedefinedGroupDecls;
        this.fRGCount = i2 + 1;
        xSGroupDeclArr2[i2] = xSGroupDecl;
        int i3 = this.fRGCount;
        this.fRGCount = i3 + 1;
        xSGroupDeclArr2[i3] = xSGroupDecl2;
    }

    /* access modifiers changed from: package-private */
    public final XSComplexTypeDecl[] getUncheckedComplexTypeDecls() {
        int i = this.fCTCount;
        XSComplexTypeDecl[] xSComplexTypeDeclArr = this.fComplexTypeDecls;
        if (i < xSComplexTypeDeclArr.length) {
            this.fComplexTypeDecls = resize(xSComplexTypeDeclArr, i);
            this.fCTLocators = resize(this.fCTLocators, this.fCTCount);
        }
        return this.fComplexTypeDecls;
    }

    /* access modifiers changed from: package-private */
    public final SimpleLocator[] getUncheckedCTLocators() {
        int i = this.fCTCount;
        if (i < this.fCTLocators.length) {
            this.fComplexTypeDecls = resize(this.fComplexTypeDecls, i);
            this.fCTLocators = resize(this.fCTLocators, this.fCTCount);
        }
        return this.fCTLocators;
    }

    /* access modifiers changed from: package-private */
    public final XSGroupDecl[] getRedefinedGroupDecls() {
        int i = this.fRGCount;
        XSGroupDecl[] xSGroupDeclArr = this.fRedefinedGroupDecls;
        if (i < xSGroupDeclArr.length) {
            this.fRedefinedGroupDecls = resize(xSGroupDeclArr, i);
            this.fRGLocators = resize(this.fRGLocators, this.fRGCount / 2);
        }
        return this.fRedefinedGroupDecls;
    }

    /* access modifiers changed from: package-private */
    public final SimpleLocator[] getRGLocators() {
        int i = this.fRGCount;
        XSGroupDecl[] xSGroupDeclArr = this.fRedefinedGroupDecls;
        if (i < xSGroupDeclArr.length) {
            this.fRedefinedGroupDecls = resize(xSGroupDeclArr, i);
            this.fRGLocators = resize(this.fRGLocators, this.fRGCount / 2);
        }
        return this.fRGLocators;
    }

    /* access modifiers changed from: package-private */
    public final void setUncheckedTypeNum(int i) {
        this.fCTCount = i;
        this.fComplexTypeDecls = resize(this.fComplexTypeDecls, this.fCTCount);
        this.fCTLocators = resize(this.fCTLocators, this.fCTCount);
    }

    /* access modifiers changed from: package-private */
    public final XSElementDecl[] getSubstitutionGroups() {
        int i = this.fSubGroupCount;
        XSElementDecl[] xSElementDeclArr = this.fSubGroups;
        if (i < xSElementDeclArr.length) {
            this.fSubGroups = resize(xSElementDeclArr, i);
        }
        return this.fSubGroups;
    }

    private static class XSAnyType extends XSComplexTypeDecl {
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl
        public void reset() {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl
        public void setContainsTypeID() {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl
        public void setIsAbstractType() {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl
        public void setIsAnonymous() {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl
        public void setName(String str) {
        }

        public void setValues(String str, String str2, XSTypeDefinition xSTypeDefinition, short s, short s2, short s3, short s4, boolean z, XSAttributeGroupDecl xSAttributeGroupDecl, XSSimpleType xSSimpleType, XSParticleDecl xSParticleDecl) {
        }

        public XSAnyType() {
            this.fName = SchemaSymbols.ATTVAL_ANYTYPE;
            this.fTargetNamespace = SchemaSymbols.URI_SCHEMAFORSCHEMA;
            this.fBaseType = this;
            this.fDerivedBy = 2;
            this.fContentType = 3;
            this.fParticle = null;
            this.fAttrGrp = null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl, ohos.com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition
        public XSObjectList getAttributeUses() {
            return XSObjectListImpl.EMPTY_LIST;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl
        public XSAttributeGroupDecl getAttrGrp() {
            XSWildcardDecl xSWildcardDecl = new XSWildcardDecl();
            xSWildcardDecl.fProcessContents = 3;
            XSAttributeGroupDecl xSAttributeGroupDecl = new XSAttributeGroupDecl();
            xSAttributeGroupDecl.fAttributeWC = xSWildcardDecl;
            return xSAttributeGroupDecl;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl, ohos.com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition
        public XSWildcard getAttributeWildcard() {
            XSWildcardDecl xSWildcardDecl = new XSWildcardDecl();
            xSWildcardDecl.fProcessContents = 3;
            return xSWildcardDecl;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl, ohos.com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition
        public XSParticle getParticle() {
            XSWildcardDecl xSWildcardDecl = new XSWildcardDecl();
            xSWildcardDecl.fProcessContents = 3;
            XSParticleDecl xSParticleDecl = new XSParticleDecl();
            xSParticleDecl.fMinOccurs = 0;
            xSParticleDecl.fMaxOccurs = -1;
            xSParticleDecl.fType = 2;
            xSParticleDecl.fValue = xSWildcardDecl;
            XSModelGroupImpl xSModelGroupImpl = new XSModelGroupImpl();
            xSModelGroupImpl.fCompositor = 102;
            xSModelGroupImpl.fParticleCount = 1;
            xSModelGroupImpl.fParticles = new XSParticleDecl[1];
            xSModelGroupImpl.fParticles[0] = xSParticleDecl;
            XSParticleDecl xSParticleDecl2 = new XSParticleDecl();
            xSParticleDecl2.fType = 3;
            xSParticleDecl2.fValue = xSModelGroupImpl;
            return xSParticleDecl2;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl, ohos.com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition
        public XSObjectList getAnnotations() {
            return XSObjectListImpl.EMPTY_LIST;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl, ohos.com.sun.org.apache.xerces.internal.xs.XSObject
        public XSNamespaceItem getNamespaceItem() {
            return SchemaGrammar.SG_SchemaNS;
        }
    }

    private static class BuiltinAttrDecl extends XSAttributeDecl {
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAttributeDecl, ohos.com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration
        public XSAnnotation getAnnotation() {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAttributeDecl
        public void reset() {
        }

        public void setValues(String str, String str2, XSSimpleType xSSimpleType, short s, short s2, ValidatedInfo validatedInfo, XSComplexTypeDecl xSComplexTypeDecl) {
        }

        public BuiltinAttrDecl(String str, String str2, XSSimpleType xSSimpleType, short s) {
            this.fName = str;
            this.fTargetNamespace = str2;
            this.fType = xSSimpleType;
            this.fScope = s;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAttributeDecl, ohos.com.sun.org.apache.xerces.internal.xs.XSObject
        public XSNamespaceItem getNamespaceItem() {
            return SchemaGrammar.SG_XSI;
        }
    }

    public static SchemaGrammar getS4SGrammar(short s) {
        if (s == 1) {
            return SG_SchemaNS;
        }
        return SG_SchemaNSExtended;
    }

    static final XSComplexTypeDecl[] resize(XSComplexTypeDecl[] xSComplexTypeDeclArr, int i) {
        XSComplexTypeDecl[] xSComplexTypeDeclArr2 = new XSComplexTypeDecl[i];
        System.arraycopy(xSComplexTypeDeclArr, 0, xSComplexTypeDeclArr2, 0, Math.min(xSComplexTypeDeclArr.length, i));
        return xSComplexTypeDeclArr2;
    }

    static final XSGroupDecl[] resize(XSGroupDecl[] xSGroupDeclArr, int i) {
        XSGroupDecl[] xSGroupDeclArr2 = new XSGroupDecl[i];
        System.arraycopy(xSGroupDeclArr, 0, xSGroupDeclArr2, 0, Math.min(xSGroupDeclArr.length, i));
        return xSGroupDeclArr2;
    }

    static final XSElementDecl[] resize(XSElementDecl[] xSElementDeclArr, int i) {
        XSElementDecl[] xSElementDeclArr2 = new XSElementDecl[i];
        System.arraycopy(xSElementDeclArr, 0, xSElementDeclArr2, 0, Math.min(xSElementDeclArr.length, i));
        return xSElementDeclArr2;
    }

    static final SimpleLocator[] resize(SimpleLocator[] simpleLocatorArr, int i) {
        SimpleLocator[] simpleLocatorArr2 = new SimpleLocator[i];
        System.arraycopy(simpleLocatorArr, 0, simpleLocatorArr2, 0, Math.min(simpleLocatorArr.length, i));
        return simpleLocatorArr2;
    }

    public synchronized void addDocument(Object obj, String str) {
        if (this.fDocuments == null) {
            this.fDocuments = new Vector();
            this.fLocations = new Vector();
        }
        this.fDocuments.addElement(obj);
        this.fLocations.addElement(str);
    }

    public synchronized void removeDocument(int i) {
        if (this.fDocuments != null && i >= 0 && i < this.fDocuments.size()) {
            this.fDocuments.removeElementAt(i);
            this.fLocations.removeElementAt(i);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem
    public String getSchemaNamespace() {
        return this.fTargetNamespace;
    }

    /* access modifiers changed from: package-private */
    public synchronized DOMParser getDOMParser() {
        DOMParser dOMParser;
        if (this.fDOMParser != null && (dOMParser = (DOMParser) this.fDOMParser.get()) != null) {
            return dOMParser;
        }
        XML11Configuration xML11Configuration = new XML11Configuration(this.fSymbolTable);
        xML11Configuration.setFeature("http://xml.org/sax/features/namespaces", true);
        xML11Configuration.setFeature("http://xml.org/sax/features/validation", false);
        DOMParser dOMParser2 = new DOMParser(xML11Configuration);
        try {
            dOMParser2.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
        } catch (SAXException unused) {
        }
        this.fDOMParser = new SoftReference(dOMParser2);
        return dOMParser2;
    }

    /* access modifiers changed from: package-private */
    public synchronized SAXParser getSAXParser() {
        SAXParser sAXParser;
        if (this.fSAXParser != null && (sAXParser = (SAXParser) this.fSAXParser.get()) != null) {
            return sAXParser;
        }
        XML11Configuration xML11Configuration = new XML11Configuration(this.fSymbolTable);
        xML11Configuration.setFeature("http://xml.org/sax/features/namespaces", true);
        xML11Configuration.setFeature("http://xml.org/sax/features/validation", false);
        SAXParser sAXParser2 = new SAXParser(xML11Configuration);
        this.fSAXParser = new SoftReference(sAXParser2);
        return sAXParser2;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem
    public synchronized XSNamedMap getComponents(short s) {
        if (s > 0 && s <= 16) {
            if (GLOBAL_COMP[s]) {
                if (this.fComponents == null) {
                    this.fComponents = new XSNamedMap[17];
                }
                if (this.fComponents[s] == null) {
                    SymbolHash symbolHash = null;
                    if (s == 1) {
                        symbolHash = this.fGlobalAttrDecls;
                    } else if (s != 2) {
                        if (s != 3) {
                            if (s == 5) {
                                symbolHash = this.fGlobalAttrGrpDecls;
                            } else if (s == 6) {
                                symbolHash = this.fGlobalGroupDecls;
                            } else if (s == 11) {
                                symbolHash = this.fGlobalNotationDecls;
                            } else if (!(s == 15 || s == 16)) {
                            }
                        }
                        symbolHash = this.fGlobalTypeDecls;
                    } else {
                        symbolHash = this.fGlobalElemDecls;
                    }
                    if (s != 15) {
                        if (s != 16) {
                            this.fComponents[s] = new XSNamedMapImpl(this.fTargetNamespace, symbolHash);
                        }
                    }
                    this.fComponents[s] = new XSNamedMap4Types(this.fTargetNamespace, symbolHash, s);
                }
                return this.fComponents[s];
            }
        }
        return XSNamedMapImpl.EMPTY_MAP;
    }

    public synchronized ObjectList getComponentsExt(short s) {
        if (s > 0 && s <= 16) {
            if (GLOBAL_COMP[s]) {
                if (this.fComponentsExt == null) {
                    this.fComponentsExt = new ObjectList[17];
                }
                if (this.fComponentsExt[s] == null) {
                    SymbolHash symbolHash = null;
                    if (s == 1) {
                        symbolHash = this.fGlobalAttrDeclsExt;
                    } else if (s != 2) {
                        if (s != 3) {
                            if (s == 5) {
                                symbolHash = this.fGlobalAttrGrpDeclsExt;
                            } else if (s == 6) {
                                symbolHash = this.fGlobalGroupDeclsExt;
                            } else if (s == 11) {
                                symbolHash = this.fGlobalNotationDeclsExt;
                            } else if (!(s == 15 || s == 16)) {
                            }
                        }
                        symbolHash = this.fGlobalTypeDeclsExt;
                    } else {
                        symbolHash = this.fGlobalElemDeclsExt;
                    }
                    Object[] entries = symbolHash.getEntries();
                    this.fComponentsExt[s] = new ObjectListImpl(entries, entries.length);
                }
                return this.fComponentsExt[s];
            }
        }
        return ObjectListImpl.EMPTY_LIST;
    }

    public synchronized void resetComponents() {
        this.fComponents = null;
        this.fComponentsExt = null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem
    public XSTypeDefinition getTypeDefinition(String str) {
        return getGlobalTypeDecl(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem
    public XSAttributeDeclaration getAttributeDeclaration(String str) {
        return getGlobalAttributeDecl(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem
    public XSElementDeclaration getElementDeclaration(String str) {
        return getGlobalElementDecl(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem
    public XSAttributeGroupDefinition getAttributeGroup(String str) {
        return getGlobalAttributeGroupDecl(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem
    public XSModelGroupDefinition getModelGroupDefinition(String str) {
        return getGlobalGroupDecl(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem
    public XSNotationDeclaration getNotationDeclaration(String str) {
        return getGlobalNotationDecl(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem
    public StringList getDocumentLocations() {
        return new StringListImpl(this.fLocations);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XSGrammar
    public XSModel toXSModel() {
        return new XSModelImpl(new SchemaGrammar[]{this});
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XSGrammar
    public XSModel toXSModel(XSGrammar[] xSGrammarArr) {
        boolean z;
        if (xSGrammarArr == null || xSGrammarArr.length == 0) {
            return toXSModel();
        }
        int length = xSGrammarArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                z = false;
                break;
            } else if (xSGrammarArr[i] == this) {
                z = true;
                break;
            } else {
                i++;
            }
        }
        SchemaGrammar[] schemaGrammarArr = new SchemaGrammar[(z ? length : length + 1)];
        for (int i2 = 0; i2 < length; i2++) {
            schemaGrammarArr[i2] = (SchemaGrammar) xSGrammarArr[i2];
        }
        if (!z) {
            schemaGrammarArr[length] = this;
        }
        return new XSModelImpl(schemaGrammarArr);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem
    public XSObjectList getAnnotations() {
        int i = this.fNumAnnotations;
        if (i == 0) {
            return XSObjectListImpl.EMPTY_LIST;
        }
        return new XSObjectListImpl(this.fAnnotations, i);
    }

    public void addAnnotation(XSAnnotationImpl xSAnnotationImpl) {
        if (xSAnnotationImpl != null) {
            XSAnnotationImpl[] xSAnnotationImplArr = this.fAnnotations;
            if (xSAnnotationImplArr == null) {
                this.fAnnotations = new XSAnnotationImpl[2];
            } else {
                int i = this.fNumAnnotations;
                if (i == xSAnnotationImplArr.length) {
                    XSAnnotationImpl[] xSAnnotationImplArr2 = new XSAnnotationImpl[(i << 1)];
                    System.arraycopy(xSAnnotationImplArr, 0, xSAnnotationImplArr2, 0, i);
                    this.fAnnotations = xSAnnotationImplArr2;
                }
            }
            XSAnnotationImpl[] xSAnnotationImplArr3 = this.fAnnotations;
            int i2 = this.fNumAnnotations;
            this.fNumAnnotations = i2 + 1;
            xSAnnotationImplArr3[i2] = xSAnnotationImpl;
        }
    }

    public void setImmutable(boolean z) {
        this.fIsImmutable = z;
    }

    public boolean isImmutable() {
        return this.fIsImmutable;
    }
}
