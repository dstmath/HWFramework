package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidatedInfo;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaNamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSAttributeDecl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSGrammarBucket;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XInt;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XIntPool;
import ohos.com.sun.org.apache.xerces.internal.util.DOMUtil;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.Element;

public class XSAttributeChecker {
    public static final int ATTIDX_ABSTRACT;
    public static final int ATTIDX_AFORMDEFAULT;
    public static final int ATTIDX_BASE;
    public static final int ATTIDX_BLOCK;
    public static final int ATTIDX_BLOCKDEFAULT;
    private static int ATTIDX_COUNT = 0;
    public static final int ATTIDX_DEFAULT;
    public static final int ATTIDX_EFORMDEFAULT;
    public static final int ATTIDX_ENUMNSDECLS;
    public static final int ATTIDX_FINAL;
    public static final int ATTIDX_FINALDEFAULT;
    public static final int ATTIDX_FIXED;
    public static final int ATTIDX_FORM;
    public static final int ATTIDX_FROMDEFAULT;
    public static final int ATTIDX_ID;
    public static final int ATTIDX_ISRETURNED;
    public static final int ATTIDX_ITEMTYPE;
    public static final int ATTIDX_MAXOCCURS;
    public static final int ATTIDX_MEMBERTYPES;
    public static final int ATTIDX_MINOCCURS;
    public static final int ATTIDX_MIXED;
    public static final int ATTIDX_NAME;
    public static final int ATTIDX_NAMESPACE;
    public static final int ATTIDX_NAMESPACE_LIST;
    public static final int ATTIDX_NILLABLE;
    public static final int ATTIDX_NONSCHEMA;
    public static final int ATTIDX_PROCESSCONTENTS;
    public static final int ATTIDX_PUBLIC;
    public static final int ATTIDX_REF;
    public static final int ATTIDX_REFER;
    public static final int ATTIDX_SCHEMALOCATION;
    public static final int ATTIDX_SOURCE;
    public static final int ATTIDX_SUBSGROUP;
    public static final int ATTIDX_SYSTEM;
    public static final int ATTIDX_TARGETNAMESPACE;
    public static final int ATTIDX_TYPE;
    public static final int ATTIDX_USE;
    public static final int ATTIDX_VALUE;
    public static final int ATTIDX_VERSION;
    public static final int ATTIDX_XML_LANG;
    public static final int ATTIDX_XPATH;
    private static final String ATTRIBUTE_N = "attribute_n";
    private static final String ATTRIBUTE_R = "attribute_r";
    protected static final int DT_ANYURI = 0;
    protected static final int DT_BLOCK = -1;
    protected static final int DT_BLOCK1 = -2;
    protected static final int DT_BOOLEAN = -15;
    protected static final int DT_COUNT = 9;
    protected static final int DT_FINAL = -3;
    protected static final int DT_FINAL1 = -4;
    protected static final int DT_FINAL2 = -5;
    protected static final int DT_FORM = -6;
    protected static final int DT_ID = 1;
    protected static final int DT_LANGUAGE = 8;
    protected static final int DT_MAXOCCURS = -7;
    protected static final int DT_MAXOCCURS1 = -8;
    protected static final int DT_MEMBERTYPES = -9;
    protected static final int DT_MINOCCURS1 = -10;
    protected static final int DT_NAMESPACE = -11;
    protected static final int DT_NCNAME = 5;
    protected static final int DT_NONNEGINT = -16;
    protected static final int DT_POSINT = -17;
    protected static final int DT_PROCESSCONTENTS = -12;
    protected static final int DT_QNAME = 2;
    protected static final int DT_STRING = 3;
    protected static final int DT_TOKEN = 4;
    protected static final int DT_USE = -13;
    protected static final int DT_WHITESPACE = -14;
    protected static final int DT_XPATH = 6;
    protected static final int DT_XPATH1 = 7;
    private static final String ELEMENT_N = "element_n";
    private static final String ELEMENT_R = "element_r";
    static final int INC_POOL_SIZE = 10;
    static final int INIT_POOL_SIZE = 10;
    private static final XInt INT_ANY_ANY = fXIntPool.getXInt(1);
    private static final XInt INT_ANY_LAX = fXIntPool.getXInt(3);
    private static final XInt INT_ANY_LIST = fXIntPool.getXInt(3);
    private static final XInt INT_ANY_NOT = fXIntPool.getXInt(2);
    private static final XInt INT_ANY_SKIP = fXIntPool.getXInt(2);
    private static final XInt INT_ANY_STRICT = fXIntPool.getXInt(1);
    private static final XInt INT_EMPTY_SET = fXIntPool.getXInt(0);
    private static final XInt INT_QUALIFIED = fXIntPool.getXInt(1);
    private static final XInt INT_UNBOUNDED = fXIntPool.getXInt(-1);
    private static final XInt INT_UNQUALIFIED = fXIntPool.getXInt(0);
    private static final XInt INT_USE_OPTIONAL = fXIntPool.getXInt(0);
    private static final XInt INT_USE_PROHIBITED = fXIntPool.getXInt(2);
    private static final XInt INT_USE_REQUIRED = fXIntPool.getXInt(1);
    private static final XInt INT_WS_COLLAPSE = fXIntPool.getXInt(2);
    private static final XInt INT_WS_PRESERVE = fXIntPool.getXInt(0);
    private static final XInt INT_WS_REPLACE = fXIntPool.getXInt(1);
    private static final Map fEleAttrsMapG = new HashMap(29);
    private static final Map fEleAttrsMapL = new HashMap(79);
    private static final XSSimpleType[] fExtraDVs = new XSSimpleType[9];
    private static boolean[] fSeenTemp;
    private static Object[] fTempArray;
    private static final XIntPool fXIntPool = new XIntPool();
    Object[][] fArrayPool;
    protected Vector fNamespaceList = new Vector();
    protected Map fNonSchemaAttrs = new HashMap();
    int fPoolPos;
    protected XSDHandler fSchemaHandler = null;
    protected boolean[] fSeen;
    protected SymbolTable fSymbolTable = null;

    static {
        int i = ATTIDX_COUNT;
        ATTIDX_COUNT = i + 1;
        ATTIDX_ABSTRACT = i;
        int i2 = ATTIDX_COUNT;
        ATTIDX_COUNT = i2 + 1;
        ATTIDX_AFORMDEFAULT = i2;
        int i3 = ATTIDX_COUNT;
        ATTIDX_COUNT = i3 + 1;
        ATTIDX_BASE = i3;
        int i4 = ATTIDX_COUNT;
        ATTIDX_COUNT = i4 + 1;
        ATTIDX_BLOCK = i4;
        int i5 = ATTIDX_COUNT;
        ATTIDX_COUNT = i5 + 1;
        ATTIDX_BLOCKDEFAULT = i5;
        int i6 = ATTIDX_COUNT;
        ATTIDX_COUNT = i6 + 1;
        ATTIDX_DEFAULT = i6;
        int i7 = ATTIDX_COUNT;
        ATTIDX_COUNT = i7 + 1;
        ATTIDX_EFORMDEFAULT = i7;
        int i8 = ATTIDX_COUNT;
        ATTIDX_COUNT = i8 + 1;
        ATTIDX_FINAL = i8;
        int i9 = ATTIDX_COUNT;
        ATTIDX_COUNT = i9 + 1;
        ATTIDX_FINALDEFAULT = i9;
        int i10 = ATTIDX_COUNT;
        ATTIDX_COUNT = i10 + 1;
        ATTIDX_FIXED = i10;
        int i11 = ATTIDX_COUNT;
        ATTIDX_COUNT = i11 + 1;
        ATTIDX_FORM = i11;
        int i12 = ATTIDX_COUNT;
        ATTIDX_COUNT = i12 + 1;
        ATTIDX_ID = i12;
        int i13 = ATTIDX_COUNT;
        ATTIDX_COUNT = i13 + 1;
        ATTIDX_ITEMTYPE = i13;
        int i14 = ATTIDX_COUNT;
        ATTIDX_COUNT = i14 + 1;
        ATTIDX_MAXOCCURS = i14;
        int i15 = ATTIDX_COUNT;
        ATTIDX_COUNT = i15 + 1;
        ATTIDX_MEMBERTYPES = i15;
        int i16 = ATTIDX_COUNT;
        ATTIDX_COUNT = i16 + 1;
        ATTIDX_MINOCCURS = i16;
        int i17 = ATTIDX_COUNT;
        ATTIDX_COUNT = i17 + 1;
        ATTIDX_MIXED = i17;
        int i18 = ATTIDX_COUNT;
        ATTIDX_COUNT = i18 + 1;
        ATTIDX_NAME = i18;
        int i19 = ATTIDX_COUNT;
        ATTIDX_COUNT = i19 + 1;
        ATTIDX_NAMESPACE = i19;
        int i20 = ATTIDX_COUNT;
        ATTIDX_COUNT = i20 + 1;
        ATTIDX_NAMESPACE_LIST = i20;
        int i21 = ATTIDX_COUNT;
        ATTIDX_COUNT = i21 + 1;
        ATTIDX_NILLABLE = i21;
        int i22 = ATTIDX_COUNT;
        ATTIDX_COUNT = i22 + 1;
        ATTIDX_NONSCHEMA = i22;
        int i23 = ATTIDX_COUNT;
        ATTIDX_COUNT = i23 + 1;
        ATTIDX_PROCESSCONTENTS = i23;
        int i24 = ATTIDX_COUNT;
        ATTIDX_COUNT = i24 + 1;
        ATTIDX_PUBLIC = i24;
        int i25 = ATTIDX_COUNT;
        ATTIDX_COUNT = i25 + 1;
        ATTIDX_REF = i25;
        int i26 = ATTIDX_COUNT;
        ATTIDX_COUNT = i26 + 1;
        ATTIDX_REFER = i26;
        int i27 = ATTIDX_COUNT;
        ATTIDX_COUNT = i27 + 1;
        ATTIDX_SCHEMALOCATION = i27;
        int i28 = ATTIDX_COUNT;
        ATTIDX_COUNT = i28 + 1;
        ATTIDX_SOURCE = i28;
        int i29 = ATTIDX_COUNT;
        ATTIDX_COUNT = i29 + 1;
        ATTIDX_SUBSGROUP = i29;
        int i30 = ATTIDX_COUNT;
        ATTIDX_COUNT = i30 + 1;
        ATTIDX_SYSTEM = i30;
        int i31 = ATTIDX_COUNT;
        ATTIDX_COUNT = i31 + 1;
        ATTIDX_TARGETNAMESPACE = i31;
        int i32 = ATTIDX_COUNT;
        ATTIDX_COUNT = i32 + 1;
        ATTIDX_TYPE = i32;
        int i33 = ATTIDX_COUNT;
        ATTIDX_COUNT = i33 + 1;
        ATTIDX_USE = i33;
        int i34 = ATTIDX_COUNT;
        ATTIDX_COUNT = i34 + 1;
        ATTIDX_VALUE = i34;
        int i35 = ATTIDX_COUNT;
        ATTIDX_COUNT = i35 + 1;
        ATTIDX_ENUMNSDECLS = i35;
        int i36 = ATTIDX_COUNT;
        ATTIDX_COUNT = i36 + 1;
        ATTIDX_VERSION = i36;
        int i37 = ATTIDX_COUNT;
        ATTIDX_COUNT = i37 + 1;
        ATTIDX_XML_LANG = i37;
        int i38 = ATTIDX_COUNT;
        ATTIDX_COUNT = i38 + 1;
        ATTIDX_XPATH = i38;
        int i39 = ATTIDX_COUNT;
        ATTIDX_COUNT = i39 + 1;
        ATTIDX_FROMDEFAULT = i39;
        int i40 = ATTIDX_COUNT;
        ATTIDX_COUNT = i40 + 1;
        ATTIDX_ISRETURNED = i40;
        SchemaGrammar.BuiltinSchemaGrammar builtinSchemaGrammar = SchemaGrammar.SG_SchemaNS;
        fExtraDVs[0] = (XSSimpleType) builtinSchemaGrammar.getGlobalTypeDecl(SchemaSymbols.ATTVAL_ANYURI);
        fExtraDVs[1] = (XSSimpleType) builtinSchemaGrammar.getGlobalTypeDecl(SchemaSymbols.ATTVAL_ID);
        fExtraDVs[2] = (XSSimpleType) builtinSchemaGrammar.getGlobalTypeDecl(SchemaSymbols.ATTVAL_QNAME);
        fExtraDVs[3] = (XSSimpleType) builtinSchemaGrammar.getGlobalTypeDecl("string");
        fExtraDVs[4] = (XSSimpleType) builtinSchemaGrammar.getGlobalTypeDecl(SchemaSymbols.ATTVAL_TOKEN);
        fExtraDVs[5] = (XSSimpleType) builtinSchemaGrammar.getGlobalTypeDecl(SchemaSymbols.ATTVAL_NCNAME);
        XSSimpleType[] xSSimpleTypeArr = fExtraDVs;
        xSSimpleTypeArr[6] = xSSimpleTypeArr[3];
        xSSimpleTypeArr[6] = xSSimpleTypeArr[3];
        xSSimpleTypeArr[8] = (XSSimpleType) builtinSchemaGrammar.getGlobalTypeDecl("language");
        OneAttr[] oneAttrArr = {new OneAttr(SchemaSymbols.ATT_ABSTRACT, -15, ATTIDX_ABSTRACT, Boolean.FALSE), new OneAttr(SchemaSymbols.ATT_ATTRIBUTEFORMDEFAULT, -6, ATTIDX_AFORMDEFAULT, INT_UNQUALIFIED), new OneAttr(SchemaSymbols.ATT_BASE, 2, ATTIDX_BASE, null), new OneAttr(SchemaSymbols.ATT_BASE, 2, ATTIDX_BASE, null), new OneAttr(SchemaSymbols.ATT_BLOCK, -1, ATTIDX_BLOCK, null), new OneAttr(SchemaSymbols.ATT_BLOCK, -2, ATTIDX_BLOCK, null), new OneAttr(SchemaSymbols.ATT_BLOCKDEFAULT, -1, ATTIDX_BLOCKDEFAULT, INT_EMPTY_SET), new OneAttr(SchemaSymbols.ATT_DEFAULT, 3, ATTIDX_DEFAULT, null), new OneAttr(SchemaSymbols.ATT_ELEMENTFORMDEFAULT, -6, ATTIDX_EFORMDEFAULT, INT_UNQUALIFIED), new OneAttr(SchemaSymbols.ATT_FINAL, -3, ATTIDX_FINAL, null), new OneAttr(SchemaSymbols.ATT_FINAL, -4, ATTIDX_FINAL, null), new OneAttr(SchemaSymbols.ATT_FINALDEFAULT, -5, ATTIDX_FINALDEFAULT, INT_EMPTY_SET), new OneAttr(SchemaSymbols.ATT_FIXED, 3, ATTIDX_FIXED, null), new OneAttr(SchemaSymbols.ATT_FIXED, -15, ATTIDX_FIXED, Boolean.FALSE), new OneAttr(SchemaSymbols.ATT_FORM, -6, ATTIDX_FORM, null), new OneAttr(SchemaSymbols.ATT_ID, 1, ATTIDX_ID, null), new OneAttr(SchemaSymbols.ATT_ITEMTYPE, 2, ATTIDX_ITEMTYPE, null), new OneAttr(SchemaSymbols.ATT_MAXOCCURS, -7, ATTIDX_MAXOCCURS, fXIntPool.getXInt(1)), new OneAttr(SchemaSymbols.ATT_MAXOCCURS, -8, ATTIDX_MAXOCCURS, fXIntPool.getXInt(1)), new OneAttr(SchemaSymbols.ATT_MEMBERTYPES, -9, ATTIDX_MEMBERTYPES, null), new OneAttr(SchemaSymbols.ATT_MINOCCURS, -16, ATTIDX_MINOCCURS, fXIntPool.getXInt(1)), new OneAttr(SchemaSymbols.ATT_MINOCCURS, -10, ATTIDX_MINOCCURS, fXIntPool.getXInt(1)), new OneAttr(SchemaSymbols.ATT_MIXED, -15, ATTIDX_MIXED, Boolean.FALSE), new OneAttr(SchemaSymbols.ATT_MIXED, -15, ATTIDX_MIXED, null), new OneAttr(SchemaSymbols.ATT_NAME, 5, ATTIDX_NAME, null), new OneAttr(SchemaSymbols.ATT_NAMESPACE, -11, ATTIDX_NAMESPACE, INT_ANY_ANY), new OneAttr(SchemaSymbols.ATT_NAMESPACE, 0, ATTIDX_NAMESPACE, null), new OneAttr(SchemaSymbols.ATT_NILLABLE, -15, ATTIDX_NILLABLE, Boolean.FALSE), new OneAttr(SchemaSymbols.ATT_PROCESSCONTENTS, -12, ATTIDX_PROCESSCONTENTS, INT_ANY_STRICT), new OneAttr(SchemaSymbols.ATT_PUBLIC, 4, ATTIDX_PUBLIC, null), new OneAttr(SchemaSymbols.ATT_REF, 2, ATTIDX_REF, null), new OneAttr(SchemaSymbols.ATT_REFER, 2, ATTIDX_REFER, null), new OneAttr(SchemaSymbols.ATT_SCHEMALOCATION, 0, ATTIDX_SCHEMALOCATION, null), new OneAttr(SchemaSymbols.ATT_SCHEMALOCATION, 0, ATTIDX_SCHEMALOCATION, null), new OneAttr(SchemaSymbols.ATT_SOURCE, 0, ATTIDX_SOURCE, null), new OneAttr(SchemaSymbols.ATT_SUBSTITUTIONGROUP, 2, ATTIDX_SUBSGROUP, null), new OneAttr(SchemaSymbols.ATT_SYSTEM, 0, ATTIDX_SYSTEM, null), new OneAttr(SchemaSymbols.ATT_TARGETNAMESPACE, 0, ATTIDX_TARGETNAMESPACE, null), new OneAttr(SchemaSymbols.ATT_TYPE, 2, ATTIDX_TYPE, null), new OneAttr(SchemaSymbols.ATT_USE, -13, ATTIDX_USE, INT_USE_OPTIONAL), new OneAttr(SchemaSymbols.ATT_VALUE, -16, ATTIDX_VALUE, null), new OneAttr(SchemaSymbols.ATT_VALUE, -17, ATTIDX_VALUE, null), new OneAttr(SchemaSymbols.ATT_VALUE, 3, ATTIDX_VALUE, null), new OneAttr(SchemaSymbols.ATT_VALUE, -14, ATTIDX_VALUE, null), new OneAttr(SchemaSymbols.ATT_VERSION, 4, ATTIDX_VERSION, null), new OneAttr(SchemaSymbols.ATT_XML_LANG, 8, ATTIDX_XML_LANG, null), new OneAttr(SchemaSymbols.ATT_XPATH, 6, ATTIDX_XPATH, null), new OneAttr(SchemaSymbols.ATT_XPATH, 7, ATTIDX_XPATH, null)};
        Container container = Container.getContainer(5);
        container.put(SchemaSymbols.ATT_DEFAULT, oneAttrArr[7]);
        container.put(SchemaSymbols.ATT_FIXED, oneAttrArr[12]);
        container.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container.put(SchemaSymbols.ATT_NAME, oneAttrArr[24]);
        container.put(SchemaSymbols.ATT_TYPE, oneAttrArr[38]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_ATTRIBUTE, container);
        Container container2 = Container.getContainer(7);
        container2.put(SchemaSymbols.ATT_DEFAULT, oneAttrArr[7]);
        container2.put(SchemaSymbols.ATT_FIXED, oneAttrArr[12]);
        container2.put(SchemaSymbols.ATT_FORM, oneAttrArr[14]);
        container2.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container2.put(SchemaSymbols.ATT_NAME, oneAttrArr[24]);
        container2.put(SchemaSymbols.ATT_TYPE, oneAttrArr[38]);
        container2.put(SchemaSymbols.ATT_USE, oneAttrArr[39]);
        fEleAttrsMapL.put(ATTRIBUTE_N, container2);
        Container container3 = Container.getContainer(5);
        container3.put(SchemaSymbols.ATT_DEFAULT, oneAttrArr[7]);
        container3.put(SchemaSymbols.ATT_FIXED, oneAttrArr[12]);
        container3.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container3.put(SchemaSymbols.ATT_REF, oneAttrArr[30]);
        container3.put(SchemaSymbols.ATT_USE, oneAttrArr[39]);
        fEleAttrsMapL.put(ATTRIBUTE_R, container3);
        Container container4 = Container.getContainer(10);
        container4.put(SchemaSymbols.ATT_ABSTRACT, oneAttrArr[0]);
        container4.put(SchemaSymbols.ATT_BLOCK, oneAttrArr[4]);
        container4.put(SchemaSymbols.ATT_DEFAULT, oneAttrArr[7]);
        container4.put(SchemaSymbols.ATT_FINAL, oneAttrArr[9]);
        container4.put(SchemaSymbols.ATT_FIXED, oneAttrArr[12]);
        container4.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container4.put(SchemaSymbols.ATT_NAME, oneAttrArr[24]);
        container4.put(SchemaSymbols.ATT_NILLABLE, oneAttrArr[27]);
        container4.put(SchemaSymbols.ATT_SUBSTITUTIONGROUP, oneAttrArr[35]);
        container4.put(SchemaSymbols.ATT_TYPE, oneAttrArr[38]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_ELEMENT, container4);
        Container container5 = Container.getContainer(10);
        container5.put(SchemaSymbols.ATT_BLOCK, oneAttrArr[4]);
        container5.put(SchemaSymbols.ATT_DEFAULT, oneAttrArr[7]);
        container5.put(SchemaSymbols.ATT_FIXED, oneAttrArr[12]);
        container5.put(SchemaSymbols.ATT_FORM, oneAttrArr[14]);
        container5.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container5.put(SchemaSymbols.ATT_MAXOCCURS, oneAttrArr[17]);
        container5.put(SchemaSymbols.ATT_MINOCCURS, oneAttrArr[20]);
        container5.put(SchemaSymbols.ATT_NAME, oneAttrArr[24]);
        container5.put(SchemaSymbols.ATT_NILLABLE, oneAttrArr[27]);
        container5.put(SchemaSymbols.ATT_TYPE, oneAttrArr[38]);
        fEleAttrsMapL.put(ELEMENT_N, container5);
        Container container6 = Container.getContainer(4);
        container6.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container6.put(SchemaSymbols.ATT_MAXOCCURS, oneAttrArr[17]);
        container6.put(SchemaSymbols.ATT_MINOCCURS, oneAttrArr[20]);
        container6.put(SchemaSymbols.ATT_REF, oneAttrArr[30]);
        fEleAttrsMapL.put(ELEMENT_R, container6);
        Container container7 = Container.getContainer(6);
        container7.put(SchemaSymbols.ATT_ABSTRACT, oneAttrArr[0]);
        container7.put(SchemaSymbols.ATT_BLOCK, oneAttrArr[5]);
        container7.put(SchemaSymbols.ATT_FINAL, oneAttrArr[9]);
        container7.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container7.put(SchemaSymbols.ATT_MIXED, oneAttrArr[22]);
        container7.put(SchemaSymbols.ATT_NAME, oneAttrArr[24]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_COMPLEXTYPE, container7);
        Container container8 = Container.getContainer(4);
        container8.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container8.put(SchemaSymbols.ATT_NAME, oneAttrArr[24]);
        container8.put(SchemaSymbols.ATT_PUBLIC, oneAttrArr[29]);
        container8.put(SchemaSymbols.ATT_SYSTEM, oneAttrArr[36]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_NOTATION, container8);
        Container container9 = Container.getContainer(2);
        container9.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container9.put(SchemaSymbols.ATT_MIXED, oneAttrArr[22]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_COMPLEXTYPE, container9);
        Container container10 = Container.getContainer(1);
        container10.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_SIMPLECONTENT, container10);
        Container container11 = Container.getContainer(2);
        container11.put(SchemaSymbols.ATT_BASE, oneAttrArr[3]);
        container11.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_RESTRICTION, container11);
        Container container12 = Container.getContainer(2);
        container12.put(SchemaSymbols.ATT_BASE, oneAttrArr[2]);
        container12.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_EXTENSION, container12);
        Container container13 = Container.getContainer(2);
        container13.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container13.put(SchemaSymbols.ATT_REF, oneAttrArr[30]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_ATTRIBUTEGROUP, container13);
        Container container14 = Container.getContainer(3);
        container14.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container14.put(SchemaSymbols.ATT_NAMESPACE, oneAttrArr[25]);
        container14.put(SchemaSymbols.ATT_PROCESSCONTENTS, oneAttrArr[28]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_ANYATTRIBUTE, container14);
        Container container15 = Container.getContainer(2);
        container15.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container15.put(SchemaSymbols.ATT_MIXED, oneAttrArr[23]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_COMPLEXCONTENT, container15);
        Container container16 = Container.getContainer(2);
        container16.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container16.put(SchemaSymbols.ATT_NAME, oneAttrArr[24]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_ATTRIBUTEGROUP, container16);
        Container container17 = Container.getContainer(2);
        container17.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container17.put(SchemaSymbols.ATT_NAME, oneAttrArr[24]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_GROUP, container17);
        Container container18 = Container.getContainer(4);
        container18.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container18.put(SchemaSymbols.ATT_MAXOCCURS, oneAttrArr[17]);
        container18.put(SchemaSymbols.ATT_MINOCCURS, oneAttrArr[20]);
        container18.put(SchemaSymbols.ATT_REF, oneAttrArr[30]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_GROUP, container18);
        Container container19 = Container.getContainer(3);
        container19.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container19.put(SchemaSymbols.ATT_MAXOCCURS, oneAttrArr[18]);
        container19.put(SchemaSymbols.ATT_MINOCCURS, oneAttrArr[21]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_ALL, container19);
        Container container20 = Container.getContainer(3);
        container20.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container20.put(SchemaSymbols.ATT_MAXOCCURS, oneAttrArr[17]);
        container20.put(SchemaSymbols.ATT_MINOCCURS, oneAttrArr[20]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_CHOICE, container20);
        fEleAttrsMapL.put(SchemaSymbols.ELT_SEQUENCE, container20);
        Container container21 = Container.getContainer(5);
        container21.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container21.put(SchemaSymbols.ATT_MAXOCCURS, oneAttrArr[17]);
        container21.put(SchemaSymbols.ATT_MINOCCURS, oneAttrArr[20]);
        container21.put(SchemaSymbols.ATT_NAMESPACE, oneAttrArr[25]);
        container21.put(SchemaSymbols.ATT_PROCESSCONTENTS, oneAttrArr[28]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_ANY, container21);
        Container container22 = Container.getContainer(2);
        container22.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container22.put(SchemaSymbols.ATT_NAME, oneAttrArr[24]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_UNIQUE, container22);
        fEleAttrsMapL.put(SchemaSymbols.ELT_KEY, container22);
        Container container23 = Container.getContainer(3);
        container23.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container23.put(SchemaSymbols.ATT_NAME, oneAttrArr[24]);
        container23.put(SchemaSymbols.ATT_REFER, oneAttrArr[31]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_KEYREF, container23);
        Container container24 = Container.getContainer(2);
        container24.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container24.put(SchemaSymbols.ATT_XPATH, oneAttrArr[46]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_SELECTOR, container24);
        Container container25 = Container.getContainer(2);
        container25.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container25.put(SchemaSymbols.ATT_XPATH, oneAttrArr[47]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_FIELD, container25);
        Container container26 = Container.getContainer(1);
        container26.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_ANNOTATION, container26);
        fEleAttrsMapL.put(SchemaSymbols.ELT_ANNOTATION, container26);
        Container container27 = Container.getContainer(1);
        container27.put(SchemaSymbols.ATT_SOURCE, oneAttrArr[34]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_APPINFO, container27);
        fEleAttrsMapL.put(SchemaSymbols.ELT_APPINFO, container27);
        Container container28 = Container.getContainer(2);
        container28.put(SchemaSymbols.ATT_SOURCE, oneAttrArr[34]);
        container28.put(SchemaSymbols.ATT_XML_LANG, oneAttrArr[45]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_DOCUMENTATION, container28);
        fEleAttrsMapL.put(SchemaSymbols.ELT_DOCUMENTATION, container28);
        Container container29 = Container.getContainer(3);
        container29.put(SchemaSymbols.ATT_FINAL, oneAttrArr[10]);
        container29.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container29.put(SchemaSymbols.ATT_NAME, oneAttrArr[24]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_SIMPLETYPE, container29);
        Container container30 = Container.getContainer(2);
        container30.put(SchemaSymbols.ATT_FINAL, oneAttrArr[10]);
        container30.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_SIMPLETYPE, container30);
        Container container31 = Container.getContainer(2);
        container31.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container31.put(SchemaSymbols.ATT_ITEMTYPE, oneAttrArr[16]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_LIST, container31);
        Container container32 = Container.getContainer(2);
        container32.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container32.put(SchemaSymbols.ATT_MEMBERTYPES, oneAttrArr[19]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_UNION, container32);
        Container container33 = Container.getContainer(8);
        container33.put(SchemaSymbols.ATT_ATTRIBUTEFORMDEFAULT, oneAttrArr[1]);
        container33.put(SchemaSymbols.ATT_BLOCKDEFAULT, oneAttrArr[6]);
        container33.put(SchemaSymbols.ATT_ELEMENTFORMDEFAULT, oneAttrArr[8]);
        container33.put(SchemaSymbols.ATT_FINALDEFAULT, oneAttrArr[11]);
        container33.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container33.put(SchemaSymbols.ATT_TARGETNAMESPACE, oneAttrArr[37]);
        container33.put(SchemaSymbols.ATT_VERSION, oneAttrArr[44]);
        container33.put(SchemaSymbols.ATT_XML_LANG, oneAttrArr[45]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_SCHEMA, container33);
        Container container34 = Container.getContainer(2);
        container34.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container34.put(SchemaSymbols.ATT_SCHEMALOCATION, oneAttrArr[32]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_INCLUDE, container34);
        fEleAttrsMapG.put(SchemaSymbols.ELT_REDEFINE, container34);
        Container container35 = Container.getContainer(3);
        container35.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container35.put(SchemaSymbols.ATT_NAMESPACE, oneAttrArr[26]);
        container35.put(SchemaSymbols.ATT_SCHEMALOCATION, oneAttrArr[33]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_IMPORT, container35);
        Container container36 = Container.getContainer(3);
        container36.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container36.put(SchemaSymbols.ATT_VALUE, oneAttrArr[40]);
        container36.put(SchemaSymbols.ATT_FIXED, oneAttrArr[13]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_LENGTH, container36);
        fEleAttrsMapL.put(SchemaSymbols.ELT_MINLENGTH, container36);
        fEleAttrsMapL.put(SchemaSymbols.ELT_MAXLENGTH, container36);
        fEleAttrsMapL.put(SchemaSymbols.ELT_FRACTIONDIGITS, container36);
        Container container37 = Container.getContainer(3);
        container37.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container37.put(SchemaSymbols.ATT_VALUE, oneAttrArr[41]);
        container37.put(SchemaSymbols.ATT_FIXED, oneAttrArr[13]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_TOTALDIGITS, container37);
        Container container38 = Container.getContainer(2);
        container38.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container38.put(SchemaSymbols.ATT_VALUE, oneAttrArr[42]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_PATTERN, container38);
        Container container39 = Container.getContainer(2);
        container39.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container39.put(SchemaSymbols.ATT_VALUE, oneAttrArr[42]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_ENUMERATION, container39);
        Container container40 = Container.getContainer(3);
        container40.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container40.put(SchemaSymbols.ATT_VALUE, oneAttrArr[43]);
        container40.put(SchemaSymbols.ATT_FIXED, oneAttrArr[13]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_WHITESPACE, container40);
        Container container41 = Container.getContainer(3);
        container41.put(SchemaSymbols.ATT_ID, oneAttrArr[15]);
        container41.put(SchemaSymbols.ATT_VALUE, oneAttrArr[42]);
        container41.put(SchemaSymbols.ATT_FIXED, oneAttrArr[13]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_MAXINCLUSIVE, container41);
        fEleAttrsMapL.put(SchemaSymbols.ELT_MAXEXCLUSIVE, container41);
        fEleAttrsMapL.put(SchemaSymbols.ELT_MININCLUSIVE, container41);
        fEleAttrsMapL.put(SchemaSymbols.ELT_MINEXCLUSIVE, container41);
        int i41 = ATTIDX_COUNT;
        fSeenTemp = new boolean[i41];
        fTempArray = new Object[i41];
    }

    public XSAttributeChecker(XSDHandler xSDHandler) {
        int i = ATTIDX_COUNT;
        this.fSeen = new boolean[i];
        this.fArrayPool = (Object[][]) Array.newInstance(Object.class, 10, i);
        this.fPoolPos = 0;
        this.fSchemaHandler = xSDHandler;
    }

    public void reset(SymbolTable symbolTable) {
        this.fSymbolTable = symbolTable;
        this.fNonSchemaAttrs.clear();
    }

    public Object[] checkAttributes(Element element, boolean z, XSDocumentInfo xSDocumentInfo) {
        return checkAttributes(element, z, xSDocumentInfo, false);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r14v1, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r0v10, resolved type: java.lang.Object[] */
    /* JADX DEBUG: Multi-variable search result rejected for r0v12, resolved type: java.lang.Object[] */
    /* JADX DEBUG: Multi-variable search result rejected for r1v7, resolved type: java.lang.Object[] */
    /* JADX DEBUG: Multi-variable search result rejected for r1v13, resolved type: java.lang.Object[] */
    /* JADX DEBUG: Multi-variable search result rejected for r1v15, resolved type: java.lang.Object[] */
    /* JADX DEBUG: Multi-variable search result rejected for r1v18, resolved type: java.lang.Object[] */
    /* JADX DEBUG: Multi-variable search result rejected for r0v19, resolved type: boolean[] */
    /* JADX DEBUG: Multi-variable search result rejected for r1v42, resolved type: java.lang.Object[] */
    /* JADX DEBUG: Multi-variable search result rejected for r0v45, resolved type: java.lang.Object[] */
    /* JADX DEBUG: Multi-variable search result rejected for r0v48, resolved type: java.lang.Object[] */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r14v0 */
    /* JADX WARN: Type inference failed for: r14v2 */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x006b  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0076  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x01d3  */
    public Object[] checkAttributes(Element element, boolean z, XSDocumentInfo xSDocumentInfo, boolean z2) {
        String str;
        Container container;
        int limit;
        int i;
        int i2;
        char c;
        String str2;
        OneAttr oneAttr;
        int i3;
        InvalidDatatypeValueException e;
        if (element == null) {
            return null;
        }
        Attr[] attrs = DOMUtil.getAttrs(element);
        resolveNamespace(element, attrs, xSDocumentInfo.fNamespaceSupport);
        String namespaceURI = DOMUtil.getNamespaceURI(element);
        String localName = DOMUtil.getLocalName(element);
        char c2 = 0;
        boolean z3 = 1;
        if (!SchemaSymbols.URI_SCHEMAFORSCHEMA.equals(namespaceURI)) {
            reportSchemaError("s4s-elt-schema-ns", new Object[]{localName}, element);
        }
        Map map = fEleAttrsMapG;
        if (!z) {
            map = fEleAttrsMapL;
            if (localName.equals(SchemaSymbols.ELT_ELEMENT)) {
                str = DOMUtil.getAttr(element, SchemaSymbols.ATT_REF) != null ? ELEMENT_R : ELEMENT_N;
            } else if (localName.equals(SchemaSymbols.ELT_ATTRIBUTE)) {
                str = DOMUtil.getAttr(element, SchemaSymbols.ATT_REF) != null ? ATTRIBUTE_R : ATTRIBUTE_N;
            }
            container = (Container) map.get(str);
            if (container != null) {
                reportSchemaError("s4s-elt-invalid", new Object[]{localName}, element);
                return null;
            }
            Object[] availableArray = getAvailableArray();
            System.arraycopy(fSeenTemp, 0, this.fSeen, 0, ATTIDX_COUNT);
            int length = attrs.length;
            int i4 = 0;
            while (i4 < length) {
                Attr attr = attrs[i4];
                String name = attr.getName();
                String namespaceURI2 = DOMUtil.getNamespaceURI(attr);
                String value = DOMUtil.getValue(attr);
                if (name.startsWith("xml")) {
                    if (!"xmlns".equals(DOMUtil.getPrefix(attr)) && !"xmlns".equals(name)) {
                        if (SchemaSymbols.ATT_XML_LANG.equals(name) && (SchemaSymbols.ELT_SCHEMA.equals(localName) || SchemaSymbols.ELT_DOCUMENTATION.equals(localName))) {
                            namespaceURI2 = null;
                        }
                    }
                    i2 = i4;
                    i = length;
                    i4 = i2 + 1;
                    length = i;
                    c2 = 0;
                    z3 = 1;
                }
                if (namespaceURI2 == null || namespaceURI2.length() == 0) {
                    OneAttr oneAttr2 = container.get(name);
                    if (oneAttr2 == null) {
                        Object[] objArr = new Object[2];
                        objArr[c2] = localName;
                        char c3 = z3 ? 1 : 0;
                        char c4 = z3 ? 1 : 0;
                        char c5 = z3 ? 1 : 0;
                        objArr[c3] = name;
                        reportSchemaError("s4s-att-not-allowed", objArr, element);
                        i2 = i4;
                        i = length;
                        i4 = i2 + 1;
                        length = i;
                        c2 = 0;
                        z3 = 1;
                    } else {
                        this.fSeen[oneAttr2.valueIndex] = z3;
                        try {
                            if (oneAttr2.dvIndex >= 0) {
                                try {
                                    if (oneAttr2.dvIndex != 3) {
                                        try {
                                            if (!(oneAttr2.dvIndex == 6 || oneAttr2.dvIndex == 7)) {
                                                Object validate = fExtraDVs[oneAttr2.dvIndex].validate(value, (ValidationContext) xSDocumentInfo.fValidationContext, (ValidatedInfo) null);
                                                if (oneAttr2.dvIndex == 2) {
                                                    QName qName = (QName) validate;
                                                    try {
                                                        if (qName.prefix == XMLSymbols.EMPTY_STRING && qName.uri == null && xSDocumentInfo.fIsChameleonSchema) {
                                                            qName.uri = xSDocumentInfo.fTargetNamespace;
                                                        }
                                                    } catch (InvalidDatatypeValueException e2) {
                                                        e = e2;
                                                        str2 = name;
                                                        oneAttr = oneAttr2;
                                                        i2 = i4;
                                                        i = length;
                                                        i3 = 3;
                                                        c = 2;
                                                        Object[] objArr2 = new Object[i3];
                                                        objArr2[0] = localName;
                                                        objArr2[1] = str2;
                                                        objArr2[c] = e.getMessage();
                                                        reportSchemaError("s4s-att-invalid-value", objArr2, element);
                                                        if (oneAttr.dfltValue != null) {
                                                        }
                                                        availableArray[ATTIDX_ENUMNSDECLS] = new SchemaNamespaceSupport(xSDocumentInfo.fNamespaceSupport);
                                                        i4 = i2 + 1;
                                                        length = i;
                                                        c2 = 0;
                                                        z3 = 1;
                                                    }
                                                }
                                                availableArray[oneAttr2.valueIndex] = validate;
                                                i2 = i4;
                                                i = length;
                                                if (localName.equals(SchemaSymbols.ELT_ENUMERATION) && z2) {
                                                    availableArray[ATTIDX_ENUMNSDECLS] = new SchemaNamespaceSupport(xSDocumentInfo.fNamespaceSupport);
                                                }
                                                i4 = i2 + 1;
                                                length = i;
                                                c2 = 0;
                                                z3 = 1;
                                            }
                                        } catch (InvalidDatatypeValueException e3) {
                                            e = e3;
                                            i3 = 3;
                                            str2 = name;
                                            c = 2;
                                            oneAttr = oneAttr2;
                                            i2 = i4;
                                            i = length;
                                            Object[] objArr22 = new Object[i3];
                                            objArr22[0] = localName;
                                            objArr22[1] = str2;
                                            objArr22[c] = e.getMessage();
                                            reportSchemaError("s4s-att-invalid-value", objArr22, element);
                                            if (oneAttr.dfltValue != null) {
                                                availableArray[oneAttr.valueIndex] = oneAttr.dfltValue;
                                            }
                                            availableArray[ATTIDX_ENUMNSDECLS] = new SchemaNamespaceSupport(xSDocumentInfo.fNamespaceSupport);
                                            i4 = i2 + 1;
                                            length = i;
                                            c2 = 0;
                                            z3 = 1;
                                        }
                                    }
                                    availableArray[oneAttr2.valueIndex] = value;
                                    i2 = i4;
                                    i = length;
                                } catch (InvalidDatatypeValueException e4) {
                                    e = e4;
                                    str2 = name;
                                    c = 2;
                                    oneAttr = oneAttr2;
                                    i2 = i4;
                                    i = length;
                                    i3 = 3;
                                    Object[] objArr222 = new Object[i3];
                                    objArr222[0] = localName;
                                    objArr222[1] = str2;
                                    objArr222[c] = e.getMessage();
                                    reportSchemaError("s4s-att-invalid-value", objArr222, element);
                                    if (oneAttr.dfltValue != null) {
                                    }
                                    availableArray[ATTIDX_ENUMNSDECLS] = new SchemaNamespaceSupport(xSDocumentInfo.fNamespaceSupport);
                                    i4 = i2 + 1;
                                    length = i;
                                    c2 = 0;
                                    z3 = 1;
                                }
                                availableArray[ATTIDX_ENUMNSDECLS] = new SchemaNamespaceSupport(xSDocumentInfo.fNamespaceSupport);
                                i4 = i2 + 1;
                                length = i;
                                c2 = 0;
                                z3 = 1;
                            } else {
                                str2 = name;
                                c = 2;
                                oneAttr = oneAttr2;
                                i3 = 3;
                                i2 = i4;
                                i = length;
                                try {
                                    availableArray[oneAttr2.valueIndex] = validate(availableArray, str2, value, oneAttr2.dvIndex, xSDocumentInfo);
                                } catch (InvalidDatatypeValueException e5) {
                                    e = e5;
                                }
                                availableArray[ATTIDX_ENUMNSDECLS] = new SchemaNamespaceSupport(xSDocumentInfo.fNamespaceSupport);
                                i4 = i2 + 1;
                                length = i;
                                c2 = 0;
                                z3 = 1;
                            }
                        } catch (InvalidDatatypeValueException e6) {
                            e = e6;
                            str2 = name;
                            c = 2;
                            oneAttr = oneAttr2;
                            i2 = i4;
                            i = length;
                            i3 = 3;
                            Object[] objArr2222 = new Object[i3];
                            objArr2222[0] = localName;
                            objArr2222[1] = str2;
                            objArr2222[c] = e.getMessage();
                            reportSchemaError("s4s-att-invalid-value", objArr2222, element);
                            if (oneAttr.dfltValue != null) {
                            }
                            availableArray[ATTIDX_ENUMNSDECLS] = new SchemaNamespaceSupport(xSDocumentInfo.fNamespaceSupport);
                            i4 = i2 + 1;
                            length = i;
                            c2 = 0;
                            z3 = 1;
                        }
                    }
                } else {
                    if (namespaceURI2.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA)) {
                        Object[] objArr3 = new Object[2];
                        objArr3[c2] = localName;
                        objArr3[z3] = name;
                        reportSchemaError("s4s-att-not-allowed", objArr3, element);
                    } else {
                        int i5 = ATTIDX_NONSCHEMA;
                        if (availableArray[i5] == null) {
                            availableArray[i5] = new Vector(4, 2);
                        }
                        ((Vector) availableArray[ATTIDX_NONSCHEMA]).addElement(name);
                        ((Vector) availableArray[ATTIDX_NONSCHEMA]).addElement(value);
                    }
                    i2 = i4;
                    i = length;
                    i4 = i2 + 1;
                    length = i;
                    c2 = 0;
                    z3 = 1;
                }
            }
            OneAttr[] oneAttrArr = container.values;
            long j = 0;
            for (OneAttr oneAttr3 : oneAttrArr) {
                if (oneAttr3.dfltValue != null && !this.fSeen[oneAttr3.valueIndex]) {
                    availableArray[oneAttr3.valueIndex] = oneAttr3.dfltValue;
                    j |= (long) (1 << oneAttr3.valueIndex);
                }
            }
            availableArray[ATTIDX_FROMDEFAULT] = new Long(j);
            if (availableArray[ATTIDX_MAXOCCURS] != null) {
                int intValue = ((XInt) availableArray[ATTIDX_MINOCCURS]).intValue();
                int intValue2 = ((XInt) availableArray[ATTIDX_MAXOCCURS]).intValue();
                if (intValue2 != -1) {
                    if (this.fSchemaHandler.fSecurityManager != null) {
                        String localName2 = element.getLocalName();
                        if (!((localName2.equals("element") || localName2.equals("any")) && element.getNextSibling() == null && element.getPreviousSibling() == null && element.getParentNode().getLocalName().equals("sequence")) && intValue2 > (limit = this.fSchemaHandler.fSecurityManager.getLimit(XMLSecurityManager.Limit.MAX_OCCUR_NODE_LIMIT)) && !this.fSchemaHandler.fSecurityManager.isNoLimit(limit)) {
                            reportSchemaFatalError("MaxOccurLimit", new Object[]{new Integer(limit)}, element);
                            availableArray[ATTIDX_MAXOCCURS] = fXIntPool.getXInt(limit);
                            intValue2 = limit;
                        }
                    }
                    if (intValue > intValue2) {
                        reportSchemaError("p-props-correct.2.1", new Object[]{localName, availableArray[ATTIDX_MINOCCURS], availableArray[ATTIDX_MAXOCCURS]}, element);
                        availableArray[ATTIDX_MINOCCURS] = availableArray[ATTIDX_MAXOCCURS];
                    }
                }
            }
            return availableArray;
        }
        str = localName;
        container = (Container) map.get(str);
        if (container != null) {
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:257:0x026b */
    /* JADX DEBUG: Multi-variable search result rejected for r0v47, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v51, types: [ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XInt] */
    /* JADX WARN: Type inference failed for: r0v57, types: [java.util.Vector] */
    private Object validate(Object[] objArr, String str, String str2, int i, XSDocumentInfo xSDocumentInfo) throws InvalidDatatypeValueException {
        char c;
        Object obj;
        String str3;
        int i2;
        int i3;
        int i4;
        int i5;
        if (str2 == null) {
            return null;
        }
        String trim = XMLChar.trim(str2);
        switch (i) {
            case -17:
                char c2 = 1;
                try {
                    if (trim.length() > 0 && trim.charAt(0) == '+') {
                        trim = trim.substring(1);
                    }
                    try {
                        XInt xInt = fXIntPool.getXInt(Integer.parseInt(trim));
                        if (xInt.intValue() > 0) {
                            return xInt;
                        }
                        throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{trim, SchemaSymbols.ATTVAL_POSITIVEINTEGER});
                    } catch (NumberFormatException unused) {
                        c2 = 1;
                        Object[] objArr2 = new Object[2];
                        objArr2[0] = trim;
                        objArr2[c2] = SchemaSymbols.ATTVAL_POSITIVEINTEGER;
                        throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", objArr2);
                    }
                } catch (NumberFormatException unused2) {
                    Object[] objArr22 = new Object[2];
                    objArr22[0] = trim;
                    objArr22[c2] = SchemaSymbols.ATTVAL_POSITIVEINTEGER;
                    throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", objArr22);
                }
            case -16:
                try {
                    if (trim.length() > 0 && trim.charAt(0) == '+') {
                        try {
                            trim = trim.substring(1);
                        } catch (NumberFormatException unused3) {
                            c = 1;
                            Object[] objArr3 = new Object[2];
                            objArr3[0] = trim;
                            objArr3[c] = SchemaSymbols.ATTVAL_NONNEGATIVEINTEGER;
                            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", objArr3);
                        }
                    }
                    XInt xInt2 = fXIntPool.getXInt(Integer.parseInt(trim));
                    if (xInt2.intValue() >= 0) {
                        return xInt2;
                    }
                    throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{trim, SchemaSymbols.ATTVAL_NONNEGATIVEINTEGER});
                } catch (NumberFormatException unused4) {
                    c = 1;
                    Object[] objArr32 = new Object[2];
                    objArr32[0] = trim;
                    objArr32[c] = SchemaSymbols.ATTVAL_NONNEGATIVEINTEGER;
                    throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", objArr32);
                }
            case -15:
                if (trim.equals("false") || trim.equals("0")) {
                    return Boolean.FALSE;
                }
                if (trim.equals("true") || trim.equals("1")) {
                    return Boolean.TRUE;
                }
                throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{trim, "boolean"});
            case -14:
                if (trim.equals(SchemaSymbols.ATTVAL_PRESERVE)) {
                    return INT_WS_PRESERVE;
                }
                if (trim.equals(SchemaSymbols.ATTVAL_REPLACE)) {
                    return INT_WS_REPLACE;
                }
                if (trim.equals(SchemaSymbols.ATTVAL_COLLAPSE)) {
                    return INT_WS_COLLAPSE;
                }
                throw new InvalidDatatypeValueException("cvc-enumeration-valid", new Object[]{trim, "(preserve | replace | collapse)"});
            case -13:
                if (trim.equals(SchemaSymbols.ATTVAL_OPTIONAL)) {
                    return INT_USE_OPTIONAL;
                }
                if (trim.equals(SchemaSymbols.ATTVAL_REQUIRED)) {
                    return INT_USE_REQUIRED;
                }
                if (trim.equals(SchemaSymbols.ATTVAL_PROHIBITED)) {
                    return INT_USE_PROHIBITED;
                }
                throw new InvalidDatatypeValueException("cvc-enumeration-valid", new Object[]{trim, "(optional | prohibited | required)"});
            case -12:
                if (trim.equals(SchemaSymbols.ATTVAL_STRICT)) {
                    return INT_ANY_STRICT;
                }
                if (trim.equals(SchemaSymbols.ATTVAL_LAX)) {
                    return INT_ANY_LAX;
                }
                if (trim.equals(SchemaSymbols.ATTVAL_SKIP)) {
                    return INT_ANY_SKIP;
                }
                throw new InvalidDatatypeValueException("cvc-enumeration-valid", new Object[]{trim, "(lax | skip | strict)"});
            case -11:
                if (trim.equals(SchemaSymbols.ATTVAL_TWOPOUNDANY)) {
                    return INT_ANY_ANY;
                }
                if (trim.equals(SchemaSymbols.ATTVAL_TWOPOUNDOTHER)) {
                    obj = INT_ANY_NOT;
                    objArr[ATTIDX_NAMESPACE_LIST] = new String[]{xSDocumentInfo.fTargetNamespace, null};
                    break;
                } else {
                    XInt xInt3 = INT_ANY_LIST;
                    this.fNamespaceList.removeAllElements();
                    StringTokenizer stringTokenizer = new StringTokenizer(trim, " \n\t\r");
                    while (stringTokenizer.hasMoreTokens()) {
                        try {
                            String nextToken = stringTokenizer.nextToken();
                            if (nextToken.equals(SchemaSymbols.ATTVAL_TWOPOUNDLOCAL)) {
                                str3 = null;
                            } else if (nextToken.equals(SchemaSymbols.ATTVAL_TWOPOUNDTARGETNS)) {
                                str3 = xSDocumentInfo.fTargetNamespace;
                            } else {
                                fExtraDVs[0].validate(nextToken, (ValidationContext) xSDocumentInfo.fValidationContext, (ValidatedInfo) null);
                                str3 = this.fSymbolTable.addSymbol(nextToken);
                            }
                            if (!this.fNamespaceList.contains(str3)) {
                                this.fNamespaceList.addElement(str3);
                            }
                        } catch (InvalidDatatypeValueException unused5) {
                            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.3", new Object[]{trim, "((##any | ##other) | List of (anyURI | (##targetNamespace | ##local)) )"});
                        }
                    }
                    String[] strArr = new String[this.fNamespaceList.size()];
                    this.fNamespaceList.copyInto(strArr);
                    objArr[ATTIDX_NAMESPACE_LIST] = strArr;
                    return xInt3;
                }
            case -10:
                if (trim.equals("0")) {
                    return fXIntPool.getXInt(0);
                }
                if (trim.equals("1")) {
                    return fXIntPool.getXInt(1);
                }
                throw new InvalidDatatypeValueException("cvc-enumeration-valid", new Object[]{trim, "(0 | 1)"});
            case -9:
                obj = new Vector();
                try {
                    StringTokenizer stringTokenizer2 = new StringTokenizer(trim, " \n\t\r");
                    while (stringTokenizer2.hasMoreTokens()) {
                        QName qName = (QName) fExtraDVs[2].validate(stringTokenizer2.nextToken(), (ValidationContext) xSDocumentInfo.fValidationContext, (ValidatedInfo) null);
                        if (qName.prefix == XMLSymbols.EMPTY_STRING && qName.uri == null && xSDocumentInfo.fIsChameleonSchema) {
                            qName.uri = xSDocumentInfo.fTargetNamespace;
                        }
                        obj.addElement(qName);
                    }
                    break;
                } catch (InvalidDatatypeValueException unused6) {
                    throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.2", new Object[]{trim, "(List of QName)"});
                }
            case -8:
                if (trim.equals("1")) {
                    return fXIntPool.getXInt(1);
                }
                throw new InvalidDatatypeValueException("cvc-enumeration-valid", new Object[]{trim, "(1)"});
            case -7:
                if (trim.equals(SchemaSymbols.ATTVAL_UNBOUNDED)) {
                    return INT_UNBOUNDED;
                }
                try {
                    return validate(objArr, str, trim, -16, xSDocumentInfo);
                } catch (NumberFormatException unused7) {
                    throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.3", new Object[]{trim, "(nonNegativeInteger | unbounded)"});
                }
            case -6:
                if (trim.equals(SchemaSymbols.ATTVAL_QUALIFIED)) {
                    return INT_QUALIFIED;
                }
                if (trim.equals(SchemaSymbols.ATTVAL_UNQUALIFIED)) {
                    return INT_UNQUALIFIED;
                }
                throw new InvalidDatatypeValueException("cvc-enumeration-valid", new Object[]{trim, "(qualified | unqualified)"});
            case -5:
                if (trim.equals(SchemaSymbols.ATTVAL_POUNDALL)) {
                    i2 = 31;
                } else {
                    StringTokenizer stringTokenizer3 = new StringTokenizer(trim, " \n\t\r");
                    i2 = 0;
                    while (stringTokenizer3.hasMoreTokens()) {
                        String nextToken2 = stringTokenizer3.nextToken();
                        if (nextToken2.equals(SchemaSymbols.ATTVAL_EXTENSION)) {
                            i2 |= 1;
                        } else if (nextToken2.equals(SchemaSymbols.ATTVAL_RESTRICTION)) {
                            i2 |= 2;
                        } else if (nextToken2.equals(SchemaSymbols.ATTVAL_LIST)) {
                            i2 |= 16;
                        } else if (nextToken2.equals(SchemaSymbols.ATTVAL_UNION)) {
                            i2 |= 8;
                        } else {
                            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.3", new Object[]{trim, "(#all | List of (extension | restriction | list | union))"});
                        }
                    }
                }
                return fXIntPool.getXInt(i2);
            case -4:
                if (trim.equals(SchemaSymbols.ATTVAL_POUNDALL)) {
                    i3 = 31;
                } else {
                    StringTokenizer stringTokenizer4 = new StringTokenizer(trim, " \n\t\r");
                    i3 = 0;
                    while (stringTokenizer4.hasMoreTokens()) {
                        String nextToken3 = stringTokenizer4.nextToken();
                        if (nextToken3.equals(SchemaSymbols.ATTVAL_LIST)) {
                            i3 |= 16;
                        } else if (nextToken3.equals(SchemaSymbols.ATTVAL_UNION)) {
                            i3 |= 8;
                        } else if (nextToken3.equals(SchemaSymbols.ATTVAL_RESTRICTION)) {
                            i3 |= 2;
                        } else {
                            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.3", new Object[]{trim, "(#all | List of (list | union | restriction))"});
                        }
                    }
                }
                return fXIntPool.getXInt(i3);
            case -3:
            case -2:
                if (trim.equals(SchemaSymbols.ATTVAL_POUNDALL)) {
                    i4 = 31;
                } else {
                    StringTokenizer stringTokenizer5 = new StringTokenizer(trim, " \n\t\r");
                    i4 = 0;
                    while (stringTokenizer5.hasMoreTokens()) {
                        String nextToken4 = stringTokenizer5.nextToken();
                        if (nextToken4.equals(SchemaSymbols.ATTVAL_EXTENSION)) {
                            i4 |= 1;
                        } else if (nextToken4.equals(SchemaSymbols.ATTVAL_RESTRICTION)) {
                            i4 |= 2;
                        } else {
                            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.3", new Object[]{trim, "(#all | List of (extension | restriction))"});
                        }
                    }
                }
                return fXIntPool.getXInt(i4);
            case -1:
                if (trim.equals(SchemaSymbols.ATTVAL_POUNDALL)) {
                    i5 = 7;
                } else {
                    StringTokenizer stringTokenizer6 = new StringTokenizer(trim, " \n\t\r");
                    int i6 = 0;
                    while (stringTokenizer6.hasMoreTokens()) {
                        String nextToken5 = stringTokenizer6.nextToken();
                        if (nextToken5.equals(SchemaSymbols.ATTVAL_EXTENSION)) {
                            i6 |= 1;
                        } else if (nextToken5.equals(SchemaSymbols.ATTVAL_RESTRICTION)) {
                            i6 |= 2;
                        } else if (nextToken5.equals(SchemaSymbols.ATTVAL_SUBSTITUTION)) {
                            i6 |= 4;
                        } else {
                            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.3", new Object[]{trim, "(#all | List of (extension | restriction | substitution))"});
                        }
                    }
                    i5 = i6;
                }
                return fXIntPool.getXInt(i5);
            default:
                return null;
        }
        return obj;
    }

    /* access modifiers changed from: package-private */
    public void reportSchemaFatalError(String str, Object[] objArr, Element element) {
        this.fSchemaHandler.reportSchemaFatalError(str, objArr, element);
    }

    /* access modifiers changed from: package-private */
    public void reportSchemaError(String str, Object[] objArr, Element element) {
        this.fSchemaHandler.reportSchemaError(str, objArr, element);
    }

    public void checkNonSchemaAttributes(XSGrammarBucket xSGrammarBucket) {
        XSAttributeDecl globalAttributeDecl;
        XSSimpleType xSSimpleType;
        for (Map.Entry entry : this.fNonSchemaAttrs.entrySet()) {
            String str = (String) entry.getKey();
            String substring = str.substring(0, str.indexOf(44));
            String substring2 = str.substring(str.indexOf(44) + 1);
            SchemaGrammar grammar = xSGrammarBucket.getGrammar(substring);
            if (!(grammar == null || (globalAttributeDecl = grammar.getGlobalAttributeDecl(substring2)) == null || (xSSimpleType = (XSSimpleType) globalAttributeDecl.getTypeDefinition()) == null)) {
                Vector vector = (Vector) entry.getValue();
                String str2 = (String) vector.elementAt(0);
                int size = vector.size();
                for (int i = 1; i < size; i += 2) {
                    String str3 = (String) vector.elementAt(i);
                    try {
                        xSSimpleType.validate((String) vector.elementAt(i + 1), (ValidationContext) null, (ValidatedInfo) null);
                    } catch (InvalidDatatypeValueException e) {
                        reportSchemaError("s4s-att-invalid-value", new Object[]{str3, str2, e.getMessage()}, null);
                    }
                }
            }
        }
    }

    public static String normalize(String str, short s) {
        int i;
        int i2;
        char charAt;
        int length = str == null ? 0 : str.length();
        if (length == 0 || s == 0) {
            return str;
        }
        StringBuffer stringBuffer = new StringBuffer();
        if (s == 1) {
            for (int i3 = 0; i3 < length; i3++) {
                char charAt2 = str.charAt(i3);
                if (charAt2 == '\t' || charAt2 == '\n' || charAt2 == '\r') {
                    stringBuffer.append(' ');
                } else {
                    stringBuffer.append(charAt2);
                }
            }
        } else {
            int i4 = 0;
            boolean z = true;
            while (i4 < length) {
                char charAt3 = str.charAt(i4);
                if (charAt3 == '\t' || charAt3 == '\n' || charAt3 == '\r' || charAt3 == ' ') {
                    while (true) {
                        i = length - 1;
                        if (i4 >= i || !((charAt = str.charAt((i2 = i4 + 1))) == '\t' || charAt == '\n' || charAt == '\r' || charAt == ' ')) {
                            break;
                        }
                        i4 = i2;
                    }
                    if (i4 < i && !z) {
                        stringBuffer.append(' ');
                    }
                } else {
                    stringBuffer.append(charAt3);
                    z = false;
                }
                i4++;
            }
        }
        return stringBuffer.toString();
    }

    /* access modifiers changed from: protected */
    public Object[] getAvailableArray() {
        int length = this.fArrayPool.length;
        int i = this.fPoolPos;
        if (length == i) {
            this.fArrayPool = new Object[(i + 10)][];
            while (true) {
                Object[][] objArr = this.fArrayPool;
                if (i >= objArr.length) {
                    break;
                }
                objArr[i] = new Object[ATTIDX_COUNT];
                i++;
            }
        }
        Object[][] objArr2 = this.fArrayPool;
        int i2 = this.fPoolPos;
        Object[] objArr3 = objArr2[i2];
        this.fPoolPos = i2 + 1;
        objArr2[i2] = null;
        System.arraycopy(fTempArray, 0, objArr3, 0, ATTIDX_COUNT - 1);
        objArr3[ATTIDX_ISRETURNED] = Boolean.FALSE;
        return objArr3;
    }

    public void returnAttrArray(Object[] objArr, XSDocumentInfo xSDocumentInfo) {
        if (xSDocumentInfo != null) {
            xSDocumentInfo.fNamespaceSupport.popContext();
        }
        if (this.fPoolPos != 0 && objArr != null && objArr.length == ATTIDX_COUNT && !((Boolean) objArr[ATTIDX_ISRETURNED]).booleanValue()) {
            objArr[ATTIDX_ISRETURNED] = Boolean.TRUE;
            int i = ATTIDX_NONSCHEMA;
            if (objArr[i] != null) {
                ((Vector) objArr[i]).clear();
            }
            Object[][] objArr2 = this.fArrayPool;
            int i2 = this.fPoolPos - 1;
            this.fPoolPos = i2;
            objArr2[i2] = objArr;
        }
    }

    public void resolveNamespace(Element element, Attr[] attrArr, SchemaNamespaceSupport schemaNamespaceSupport) {
        String str;
        schemaNamespaceSupport.pushContext();
        for (Attr attr : attrArr) {
            String name = DOMUtil.getName(attr);
            if (name.equals(XMLSymbols.PREFIX_XMLNS)) {
                str = XMLSymbols.EMPTY_STRING;
            } else {
                str = name.startsWith("xmlns:") ? this.fSymbolTable.addSymbol(DOMUtil.getLocalName(attr)) : null;
            }
            if (str != null) {
                String addSymbol = this.fSymbolTable.addSymbol(DOMUtil.getValue(attr));
                if (addSymbol.length() == 0) {
                    addSymbol = null;
                }
                schemaNamespaceSupport.declarePrefix(str, addSymbol);
            }
        }
    }
}
