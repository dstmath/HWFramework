package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import java.util.AbstractList;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.DatatypeException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeFacetException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidatedInfo;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSFacets;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.ListDV;
import ohos.com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.ShortListImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.StringListImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xs.ShortList;
import ohos.com.sun.org.apache.xerces.internal.xs.StringList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSAnnotation;
import ohos.com.sun.org.apache.xerces.internal.xs.XSFacet;
import ohos.com.sun.org.apache.xerces.internal.xs.XSMultiValueFacet;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamespaceItem;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList;
import ohos.org.w3c.dom.TypeInfo;

public class XSSimpleTypeDecl implements XSSimpleType, TypeInfo {
    public static final short ANYATOMICTYPE_DT = 49;
    static final String ANY_TYPE = "anyType";
    public static final short DAYTIMEDURATION_DT = 47;
    static final int DERIVATION_ANY = 0;
    static final int DERIVATION_EXTENSION = 2;
    static final int DERIVATION_LIST = 8;
    static final int DERIVATION_RESTRICTION = 1;
    static final int DERIVATION_UNION = 4;
    protected static final short DV_ANYATOMICTYPE = 29;
    protected static final short DV_ANYSIMPLETYPE = 0;
    protected static final short DV_ANYURI = 17;
    protected static final short DV_BASE64BINARY = 16;
    protected static final short DV_BOOLEAN = 2;
    protected static final short DV_DATE = 9;
    protected static final short DV_DATETIME = 7;
    protected static final short DV_DAYTIMEDURATION = 28;
    protected static final short DV_DECIMAL = 3;
    protected static final short DV_DOUBLE = 5;
    protected static final short DV_DURATION = 6;
    protected static final short DV_ENTITY = 23;
    protected static final short DV_FLOAT = 4;
    protected static final short DV_GDAY = 13;
    protected static final short DV_GMONTH = 14;
    protected static final short DV_GMONTHDAY = 12;
    protected static final short DV_GYEAR = 11;
    protected static final short DV_GYEARMONTH = 10;
    protected static final short DV_HEXBINARY = 15;
    protected static final short DV_ID = 21;
    protected static final short DV_IDREF = 22;
    protected static final short DV_INTEGER = 24;
    protected static final short DV_LIST = 25;
    protected static final short DV_NOTATION = 20;
    protected static final short DV_PRECISIONDECIMAL = 19;
    protected static final short DV_QNAME = 18;
    protected static final short DV_STRING = 1;
    protected static final short DV_TIME = 8;
    protected static final short DV_UNION = 26;
    protected static final short DV_YEARMONTHDURATION = 27;
    static final short NORMALIZE_FULL = 2;
    static final short NORMALIZE_NONE = 0;
    static final short NORMALIZE_TRIM = 1;
    public static final short PRECISIONDECIMAL_DT = 48;
    static final short SPECIAL_PATTERN_NAME = 2;
    static final short SPECIAL_PATTERN_NCNAME = 3;
    static final short SPECIAL_PATTERN_NMTOKEN = 1;
    static final short SPECIAL_PATTERN_NONE = 0;
    static final String[] SPECIAL_PATTERN_STRING = {"NONE", SchemaSymbols.ATTVAL_NMTOKEN, SchemaSymbols.ATTVAL_NAME, SchemaSymbols.ATTVAL_NCNAME};
    static final String URI_SCHEMAFORSCHEMA = "http://www.w3.org/2001/XMLSchema";
    static final String[] WS_FACET_STRING = {SchemaSymbols.ATTVAL_PRESERVE, SchemaSymbols.ATTVAL_REPLACE, SchemaSymbols.ATTVAL_COLLAPSE};
    public static final short YEARMONTHDURATION_DT = 46;
    static final XSSimpleTypeDecl fAnyAtomicType = new XSSimpleTypeDecl(fAnySimpleType, "anyAtomicType", 29, 0, false, true, false, true, 49);
    static final XSSimpleTypeDecl fAnySimpleType = new XSSimpleTypeDecl(null, SchemaSymbols.ATTVAL_ANYSIMPLETYPE, 0, 0, false, true, false, true, 1);
    static final short[] fDVNormalizeType = {0, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 0, 1, 1, 0};
    static final ValidationContext fDummyContext = new ValidationContext() {
        /* class ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDecl.AnonymousClass4 */

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public void addId(String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public void addIdRef(String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public String getURI(String str) {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean isEntityDeclared(String str) {
            return false;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean isEntityUnparsed(String str) {
            return false;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean isIdDeclared(String str) {
            return false;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean needExtraChecking() {
            return false;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean needFacetChecking() {
            return true;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean needToNormalize() {
            return false;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean useNamespaces() {
            return true;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public String getSymbol(String str) {
            return str.intern();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public Locale getLocale() {
            return Locale.getDefault();
        }
    };
    static final ValidationContext fEmptyContext = new ValidationContext() {
        /* class ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDecl.AnonymousClass1 */

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public void addId(String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public void addIdRef(String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public String getURI(String str) {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean isEntityDeclared(String str) {
            return false;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean isEntityUnparsed(String str) {
            return false;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean isIdDeclared(String str) {
            return false;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean needExtraChecking() {
            return false;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean needFacetChecking() {
            return true;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean needToNormalize() {
            return true;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean useNamespaces() {
            return true;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public String getSymbol(String str) {
            return str.intern();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public Locale getLocale() {
            return Locale.getDefault();
        }
    };
    private static final TypeValidator[] gDVs = {new AnySimpleDV(), new StringDV(), new BooleanDV(), new DecimalDV(), new FloatDV(), new DoubleDV(), new DurationDV(), new DateTimeDV(), new TimeDV(), new DateDV(), new YearMonthDV(), new YearDV(), new MonthDayDV(), new DayDV(), new MonthDV(), new HexBinaryDV(), new Base64BinaryDV(), new AnyURIDV(), new QNameDV(), new PrecisionDecimalDV(), new QNameDV(), new IDDV(), new IDREFDV(), new EntityDV(), new IntegerDV(), new ListDV(), new UnionDV(), new YearMonthDurationDV(), new DayTimeDurationDV(), new AnyAtomicDV()};
    public XSObjectList enumerationAnnotations;
    private ObjectList fActualEnumeration;
    private XSObjectList fAnnotations;
    private boolean fAnonymous;
    private XSSimpleTypeDecl fBase;
    private boolean fBounded;
    private short fBuiltInKind;
    private TypeValidator[] fDVs;
    private Vector fEnumeration;
    private ShortList[] fEnumerationItemType;
    private ObjectList fEnumerationItemTypeList;
    private short[] fEnumerationType;
    private ShortList fEnumerationTypeList;
    private XSObjectListImpl fFacets;
    private short fFacetsDefined;
    private short fFinalSet;
    private boolean fFinite;
    private short fFixedFacet;
    private int fFractionDigits;
    private boolean fIsImmutable;
    private XSSimpleTypeDecl fItemType;
    private int fLength;
    private StringList fLexicalEnumeration;
    private StringList fLexicalPattern;
    private Object fMaxExclusive;
    private Object fMaxInclusive;
    private int fMaxLength;
    private XSSimpleTypeDecl[] fMemberTypes;
    private Object fMinExclusive;
    private Object fMinInclusive;
    private int fMinLength;
    private XSObjectListImpl fMultiValueFacets;
    private XSNamespaceItem fNamespaceItem;
    private boolean fNumeric;
    private short fOrdered;
    private Vector fPattern;
    private Vector fPatternStr;
    private short fPatternType;
    private String fTargetNamespace;
    private int fTotalDigits;
    private String fTypeName;
    private short fValidationDV;
    private short fVariety;
    private short fWhiteSpace;
    public XSAnnotation fractionDigitsAnnotation;
    public XSAnnotation lengthAnnotation;
    public XSAnnotation maxExclusiveAnnotation;
    public XSAnnotation maxInclusiveAnnotation;
    public XSAnnotation maxLengthAnnotation;
    public XSAnnotation minExclusiveAnnotation;
    public XSAnnotation minInclusiveAnnotation;
    public XSAnnotation minLengthAnnotation;
    public XSObjectListImpl patternAnnotations;
    public XSAnnotation totalDigitsAnnotation;
    public XSAnnotation whiteSpaceAnnotation;

    private short convertToPrimitiveKind(short s) {
        if (s <= 20) {
            return s;
        }
        if (s <= 29) {
            return 2;
        }
        if (s <= 42) {
            return 4;
        }
        return s;
    }

    private short getPrimitiveDV(short s) {
        if (s == 21 || s == 22 || s == 23) {
            return 1;
        }
        if (s == 24) {
            return 3;
        }
        return s;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public short getType() {
        return 3;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public short getTypeCategory() {
        return 16;
    }

    protected static TypeValidator[] getGDVs() {
        return (TypeValidator[]) gDVs.clone();
    }

    /* access modifiers changed from: protected */
    public void setDVs(TypeValidator[] typeValidatorArr) {
        this.fDVs = typeValidatorArr;
    }

    public XSSimpleTypeDecl() {
        this.fDVs = gDVs;
        this.fIsImmutable = false;
        this.fFinalSet = 0;
        this.fVariety = -1;
        this.fValidationDV = -1;
        this.fFacetsDefined = 0;
        this.fFixedFacet = 0;
        this.fWhiteSpace = 0;
        this.fLength = -1;
        this.fMinLength = -1;
        this.fMaxLength = -1;
        this.fTotalDigits = -1;
        this.fFractionDigits = -1;
        this.fAnnotations = null;
        this.fPatternType = 0;
        this.fNamespaceItem = null;
        this.fAnonymous = false;
    }

    protected XSSimpleTypeDecl(XSSimpleTypeDecl xSSimpleTypeDecl, String str, short s, short s2, boolean z, boolean z2, boolean z3, boolean z4, short s3) {
        this.fDVs = gDVs;
        this.fIsImmutable = false;
        this.fFinalSet = 0;
        this.fVariety = -1;
        this.fValidationDV = -1;
        this.fFacetsDefined = 0;
        this.fFixedFacet = 0;
        this.fWhiteSpace = 0;
        this.fLength = -1;
        this.fMinLength = -1;
        this.fMaxLength = -1;
        this.fTotalDigits = -1;
        this.fFractionDigits = -1;
        this.fAnnotations = null;
        this.fPatternType = 0;
        this.fNamespaceItem = null;
        this.fAnonymous = false;
        this.fIsImmutable = z4;
        this.fBase = xSSimpleTypeDecl;
        this.fTypeName = str;
        this.fTargetNamespace = "http://www.w3.org/2001/XMLSchema";
        this.fVariety = 1;
        this.fValidationDV = s;
        this.fFacetsDefined = 16;
        if (s == 0 || s == 29 || s == 1) {
            this.fWhiteSpace = 0;
        } else {
            this.fWhiteSpace = 2;
            this.fFixedFacet = 16;
        }
        this.fOrdered = s2;
        this.fBounded = z;
        this.fFinite = z2;
        this.fNumeric = z3;
        this.fAnnotations = null;
        this.fBuiltInKind = s3;
    }

    protected XSSimpleTypeDecl(XSSimpleTypeDecl xSSimpleTypeDecl, String str, String str2, short s, boolean z, XSObjectList xSObjectList, short s2) {
        this(xSSimpleTypeDecl, str, str2, s, z, xSObjectList);
        this.fBuiltInKind = s2;
    }

    protected XSSimpleTypeDecl(XSSimpleTypeDecl xSSimpleTypeDecl, String str, String str2, short s, boolean z, XSObjectList xSObjectList) {
        this.fDVs = gDVs;
        this.fIsImmutable = false;
        this.fFinalSet = 0;
        this.fVariety = -1;
        this.fValidationDV = -1;
        this.fFacetsDefined = 0;
        this.fFixedFacet = 0;
        this.fWhiteSpace = 0;
        this.fLength = -1;
        this.fMinLength = -1;
        this.fMaxLength = -1;
        this.fTotalDigits = -1;
        this.fFractionDigits = -1;
        this.fAnnotations = null;
        this.fPatternType = 0;
        this.fNamespaceItem = null;
        this.fAnonymous = false;
        this.fBase = xSSimpleTypeDecl;
        this.fTypeName = str;
        this.fTargetNamespace = str2;
        this.fFinalSet = s;
        this.fAnnotations = xSObjectList;
        XSSimpleTypeDecl xSSimpleTypeDecl2 = this.fBase;
        this.fVariety = xSSimpleTypeDecl2.fVariety;
        this.fValidationDV = xSSimpleTypeDecl2.fValidationDV;
        short s2 = this.fVariety;
        if (s2 != 1) {
            if (s2 == 2) {
                this.fItemType = xSSimpleTypeDecl2.fItemType;
            } else if (s2 == 3) {
                this.fMemberTypes = xSSimpleTypeDecl2.fMemberTypes;
            }
        }
        XSSimpleTypeDecl xSSimpleTypeDecl3 = this.fBase;
        this.fLength = xSSimpleTypeDecl3.fLength;
        this.fMinLength = xSSimpleTypeDecl3.fMinLength;
        this.fMaxLength = xSSimpleTypeDecl3.fMaxLength;
        this.fPattern = xSSimpleTypeDecl3.fPattern;
        this.fPatternStr = xSSimpleTypeDecl3.fPatternStr;
        this.fEnumeration = xSSimpleTypeDecl3.fEnumeration;
        this.fEnumerationType = xSSimpleTypeDecl3.fEnumerationType;
        this.fEnumerationItemType = xSSimpleTypeDecl3.fEnumerationItemType;
        this.fWhiteSpace = xSSimpleTypeDecl3.fWhiteSpace;
        this.fMaxExclusive = xSSimpleTypeDecl3.fMaxExclusive;
        this.fMaxInclusive = xSSimpleTypeDecl3.fMaxInclusive;
        this.fMinExclusive = xSSimpleTypeDecl3.fMinExclusive;
        this.fMinInclusive = xSSimpleTypeDecl3.fMinInclusive;
        this.fTotalDigits = xSSimpleTypeDecl3.fTotalDigits;
        this.fFractionDigits = xSSimpleTypeDecl3.fFractionDigits;
        this.fPatternType = xSSimpleTypeDecl3.fPatternType;
        this.fFixedFacet = xSSimpleTypeDecl3.fFixedFacet;
        this.fFacetsDefined = xSSimpleTypeDecl3.fFacetsDefined;
        this.lengthAnnotation = xSSimpleTypeDecl3.lengthAnnotation;
        this.minLengthAnnotation = xSSimpleTypeDecl3.minLengthAnnotation;
        this.maxLengthAnnotation = xSSimpleTypeDecl3.maxLengthAnnotation;
        this.patternAnnotations = xSSimpleTypeDecl3.patternAnnotations;
        this.enumerationAnnotations = xSSimpleTypeDecl3.enumerationAnnotations;
        this.whiteSpaceAnnotation = xSSimpleTypeDecl3.whiteSpaceAnnotation;
        this.maxExclusiveAnnotation = xSSimpleTypeDecl3.maxExclusiveAnnotation;
        this.maxInclusiveAnnotation = xSSimpleTypeDecl3.maxInclusiveAnnotation;
        this.minExclusiveAnnotation = xSSimpleTypeDecl3.minExclusiveAnnotation;
        this.minInclusiveAnnotation = xSSimpleTypeDecl3.minInclusiveAnnotation;
        this.totalDigitsAnnotation = xSSimpleTypeDecl3.totalDigitsAnnotation;
        this.fractionDigitsAnnotation = xSSimpleTypeDecl3.fractionDigitsAnnotation;
        calcFundamentalFacets();
        this.fIsImmutable = z;
        this.fBuiltInKind = xSSimpleTypeDecl.fBuiltInKind;
    }

    protected XSSimpleTypeDecl(String str, String str2, short s, XSSimpleTypeDecl xSSimpleTypeDecl, boolean z, XSObjectList xSObjectList) {
        this.fDVs = gDVs;
        this.fIsImmutable = false;
        this.fFinalSet = 0;
        this.fVariety = -1;
        this.fValidationDV = -1;
        this.fFacetsDefined = 0;
        this.fFixedFacet = 0;
        this.fWhiteSpace = 0;
        this.fLength = -1;
        this.fMinLength = -1;
        this.fMaxLength = -1;
        this.fTotalDigits = -1;
        this.fFractionDigits = -1;
        this.fAnnotations = null;
        this.fPatternType = 0;
        this.fNamespaceItem = null;
        this.fAnonymous = false;
        this.fBase = fAnySimpleType;
        this.fTypeName = str;
        this.fTargetNamespace = str2;
        this.fFinalSet = s;
        this.fAnnotations = xSObjectList;
        this.fVariety = 2;
        this.fItemType = xSSimpleTypeDecl;
        this.fValidationDV = 25;
        this.fFacetsDefined = 16;
        this.fFixedFacet = 16;
        this.fWhiteSpace = 2;
        calcFundamentalFacets();
        this.fIsImmutable = z;
        this.fBuiltInKind = 44;
    }

    protected XSSimpleTypeDecl(String str, String str2, short s, XSSimpleTypeDecl[] xSSimpleTypeDeclArr, XSObjectList xSObjectList) {
        this.fDVs = gDVs;
        this.fIsImmutable = false;
        this.fFinalSet = 0;
        this.fVariety = -1;
        this.fValidationDV = -1;
        this.fFacetsDefined = 0;
        this.fFixedFacet = 0;
        this.fWhiteSpace = 0;
        this.fLength = -1;
        this.fMinLength = -1;
        this.fMaxLength = -1;
        this.fTotalDigits = -1;
        this.fFractionDigits = -1;
        this.fAnnotations = null;
        this.fPatternType = 0;
        this.fNamespaceItem = null;
        this.fAnonymous = false;
        this.fBase = fAnySimpleType;
        this.fTypeName = str;
        this.fTargetNamespace = str2;
        this.fFinalSet = s;
        this.fAnnotations = xSObjectList;
        this.fVariety = 3;
        this.fMemberTypes = xSSimpleTypeDeclArr;
        this.fValidationDV = 26;
        this.fFacetsDefined = 16;
        this.fWhiteSpace = 2;
        calcFundamentalFacets();
        this.fIsImmutable = false;
        this.fBuiltInKind = 45;
    }

    /* access modifiers changed from: protected */
    public XSSimpleTypeDecl setRestrictionValues(XSSimpleTypeDecl xSSimpleTypeDecl, String str, String str2, short s, XSObjectList xSObjectList) {
        if (this.fIsImmutable) {
            return null;
        }
        this.fBase = xSSimpleTypeDecl;
        this.fAnonymous = false;
        this.fTypeName = str;
        this.fTargetNamespace = str2;
        this.fFinalSet = s;
        this.fAnnotations = xSObjectList;
        XSSimpleTypeDecl xSSimpleTypeDecl2 = this.fBase;
        this.fVariety = xSSimpleTypeDecl2.fVariety;
        this.fValidationDV = xSSimpleTypeDecl2.fValidationDV;
        short s2 = this.fVariety;
        if (s2 != 1) {
            if (s2 == 2) {
                this.fItemType = xSSimpleTypeDecl2.fItemType;
            } else if (s2 == 3) {
                this.fMemberTypes = xSSimpleTypeDecl2.fMemberTypes;
            }
        }
        XSSimpleTypeDecl xSSimpleTypeDecl3 = this.fBase;
        this.fLength = xSSimpleTypeDecl3.fLength;
        this.fMinLength = xSSimpleTypeDecl3.fMinLength;
        this.fMaxLength = xSSimpleTypeDecl3.fMaxLength;
        this.fPattern = xSSimpleTypeDecl3.fPattern;
        this.fPatternStr = xSSimpleTypeDecl3.fPatternStr;
        this.fEnumeration = xSSimpleTypeDecl3.fEnumeration;
        this.fEnumerationType = xSSimpleTypeDecl3.fEnumerationType;
        this.fEnumerationItemType = xSSimpleTypeDecl3.fEnumerationItemType;
        this.fWhiteSpace = xSSimpleTypeDecl3.fWhiteSpace;
        this.fMaxExclusive = xSSimpleTypeDecl3.fMaxExclusive;
        this.fMaxInclusive = xSSimpleTypeDecl3.fMaxInclusive;
        this.fMinExclusive = xSSimpleTypeDecl3.fMinExclusive;
        this.fMinInclusive = xSSimpleTypeDecl3.fMinInclusive;
        this.fTotalDigits = xSSimpleTypeDecl3.fTotalDigits;
        this.fFractionDigits = xSSimpleTypeDecl3.fFractionDigits;
        this.fPatternType = xSSimpleTypeDecl3.fPatternType;
        this.fFixedFacet = xSSimpleTypeDecl3.fFixedFacet;
        this.fFacetsDefined = xSSimpleTypeDecl3.fFacetsDefined;
        calcFundamentalFacets();
        this.fBuiltInKind = xSSimpleTypeDecl.fBuiltInKind;
        return this;
    }

    /* access modifiers changed from: protected */
    public XSSimpleTypeDecl setListValues(String str, String str2, short s, XSSimpleTypeDecl xSSimpleTypeDecl, XSObjectList xSObjectList) {
        if (this.fIsImmutable) {
            return null;
        }
        this.fBase = fAnySimpleType;
        this.fAnonymous = false;
        this.fTypeName = str;
        this.fTargetNamespace = str2;
        this.fFinalSet = s;
        this.fAnnotations = xSObjectList;
        this.fVariety = 2;
        this.fItemType = xSSimpleTypeDecl;
        this.fValidationDV = 25;
        this.fFacetsDefined = 16;
        this.fFixedFacet = 16;
        this.fWhiteSpace = 2;
        calcFundamentalFacets();
        this.fBuiltInKind = 44;
        return this;
    }

    /* access modifiers changed from: protected */
    public XSSimpleTypeDecl setUnionValues(String str, String str2, short s, XSSimpleTypeDecl[] xSSimpleTypeDeclArr, XSObjectList xSObjectList) {
        if (this.fIsImmutable) {
            return null;
        }
        this.fBase = fAnySimpleType;
        this.fAnonymous = false;
        this.fTypeName = str;
        this.fTargetNamespace = str2;
        this.fFinalSet = s;
        this.fAnnotations = xSObjectList;
        this.fVariety = 3;
        this.fMemberTypes = xSSimpleTypeDeclArr;
        this.fValidationDV = 26;
        this.fFacetsDefined = 16;
        this.fWhiteSpace = 2;
        calcFundamentalFacets();
        this.fBuiltInKind = 45;
        return this;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getName() {
        if (getAnonymous()) {
            return null;
        }
        return this.fTypeName;
    }

    public String getTypeName() {
        return this.fTypeName;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public String getNamespace() {
        return this.fTargetNamespace;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public short getFinal() {
        return this.fFinalSet;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public boolean isFinal(short s) {
        return (this.fFinalSet & s) != 0;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public XSTypeDefinition getBaseType() {
        return this.fBase;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public boolean getAnonymous() {
        return this.fAnonymous || this.fTypeName == null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public short getVariety() {
        if (this.fValidationDV == 0) {
            return 0;
        }
        return this.fVariety;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType
    public boolean isIDType() {
        short s = this.fVariety;
        if (s == 1) {
            return this.fValidationDV == 21;
        }
        if (s == 2) {
            return this.fItemType.isIDType();
        }
        if (s == 3) {
            int i = 0;
            while (true) {
                XSSimpleTypeDecl[] xSSimpleTypeDeclArr = this.fMemberTypes;
                if (i >= xSSimpleTypeDeclArr.length) {
                    break;
                } else if (xSSimpleTypeDeclArr[i].isIDType()) {
                    return true;
                } else {
                    i++;
                }
            }
        }
        return false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType
    public short getWhitespace() throws DatatypeException {
        if (this.fVariety != 3) {
            return this.fWhiteSpace;
        }
        throw new DatatypeException("dt-whitespace", new Object[]{this.fTypeName});
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType
    public short getPrimitiveKind() {
        short s;
        if (this.fVariety != 1 || (s = this.fValidationDV) == 0) {
            return 0;
        }
        if (s == 21 || s == 22 || s == 23) {
            return 1;
        }
        if (s == 24) {
            return 3;
        }
        return s;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public short getBuiltInKind() {
        return this.fBuiltInKind;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public XSSimpleTypeDefinition getPrimitiveType() {
        if (this.fVariety != 1 || this.fValidationDV == 0) {
            return null;
        }
        while (true) {
            XSSimpleTypeDecl xSSimpleTypeDecl = this.fBase;
            if (xSSimpleTypeDecl == fAnySimpleType) {
                return this;
            }
            this = xSSimpleTypeDecl;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public XSSimpleTypeDefinition getItemType() {
        if (this.fVariety == 2) {
            return this.fItemType;
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public XSObjectList getMemberTypes() {
        if (this.fVariety != 3) {
            return XSObjectListImpl.EMPTY_LIST;
        }
        XSSimpleTypeDecl[] xSSimpleTypeDeclArr = this.fMemberTypes;
        return new XSObjectListImpl(xSSimpleTypeDeclArr, xSSimpleTypeDeclArr.length);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType
    public void applyFacets(XSFacets xSFacets, short s, short s2, ValidationContext validationContext) throws InvalidDatatypeFacetException {
        if (validationContext == null) {
            validationContext = fEmptyContext;
        }
        applyFacets(xSFacets, s, s2, 0, validationContext);
    }

    /* access modifiers changed from: package-private */
    public void applyFacets1(XSFacets xSFacets, short s, short s2) {
        try {
            applyFacets(xSFacets, s, s2, 0, fDummyContext);
        } catch (InvalidDatatypeFacetException unused) {
        }
        this.fIsImmutable = true;
    }

    /* access modifiers changed from: package-private */
    public void applyFacets1(XSFacets xSFacets, short s, short s2, short s3) {
        try {
            applyFacets(xSFacets, s, s2, s3, fDummyContext);
        } catch (InvalidDatatypeFacetException unused) {
        }
        this.fIsImmutable = true;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x036a A[SYNTHETIC, Splitter:B:117:0x036a] */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x039c  */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x0471 A[SYNTHETIC, Splitter:B:148:0x0471] */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x04a3  */
    /* JADX WARNING: Removed duplicated region for block: B:305:0x08dd  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0966  */
    /* JADX WARNING: Removed duplicated region for block: B:329:0x0991  */
    /* JADX WARNING: Removed duplicated region for block: B:341:0x09ef  */
    /* JADX WARNING: Removed duplicated region for block: B:353:0x0a52  */
    /* JADX WARNING: Removed duplicated region for block: B:360:0x0a82  */
    /* JADX WARNING: Removed duplicated region for block: B:382:0x0b18  */
    public void applyFacets(XSFacets xSFacets, short s, short s2, short s3, ValidationContext validationContext) throws InvalidDatatypeFacetException {
        short s4;
        short s5;
        short s6;
        short s7;
        int i;
        int i2;
        short s8;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        char c;
        short s9;
        char c2;
        int i9;
        int i10;
        int i11;
        int i12;
        int compare;
        int compare2;
        int i13;
        boolean z;
        boolean z2;
        int i14;
        RegularExpression regularExpression;
        if (!this.fIsImmutable) {
            ValidatedInfo validatedInfo = new ValidatedInfo();
            this.fFacetsDefined = 0;
            this.fFixedFacet = 0;
            short allowedFacets = this.fDVs[this.fValidationDV].getAllowedFacets();
            int i15 = 2;
            if ((s & 1) != 0) {
                if ((allowedFacets & 1) == 0) {
                    reportError("cos-applicable-facets", new Object[]{"length", this.fTypeName});
                } else {
                    this.fLength = xSFacets.length;
                    this.lengthAnnotation = xSFacets.lengthAnnotation;
                    this.fFacetsDefined = (short) (this.fFacetsDefined | 1);
                    if ((s2 & 1) != 0) {
                        this.fFixedFacet = (short) (this.fFixedFacet | 1);
                    }
                }
            }
            if ((s & 2) != 0) {
                if ((allowedFacets & 2) == 0) {
                    reportError("cos-applicable-facets", new Object[]{"minLength", this.fTypeName});
                } else {
                    this.fMinLength = xSFacets.minLength;
                    this.minLengthAnnotation = xSFacets.minLengthAnnotation;
                    this.fFacetsDefined = (short) (this.fFacetsDefined | 2);
                    if ((s2 & 2) != 0) {
                        this.fFixedFacet = (short) (this.fFixedFacet | 2);
                    }
                }
            }
            if ((s & 4) != 0) {
                if ((allowedFacets & 4) == 0) {
                    reportError("cos-applicable-facets", new Object[]{"maxLength", this.fTypeName});
                } else {
                    this.fMaxLength = xSFacets.maxLength;
                    this.maxLengthAnnotation = xSFacets.maxLengthAnnotation;
                    this.fFacetsDefined = (short) (this.fFacetsDefined | 4);
                    if ((s2 & 4) != 0) {
                        this.fFixedFacet = (short) (this.fFixedFacet | 4);
                    }
                }
            }
            if ((s & 8) != 0) {
                if ((allowedFacets & 8) == 0) {
                    reportError("cos-applicable-facets", new Object[]{"pattern", this.fTypeName});
                } else {
                    this.patternAnnotations = xSFacets.patternAnnotations;
                    try {
                        regularExpression = new RegularExpression(xSFacets.pattern, "X", validationContext.getLocale());
                    } catch (Exception e) {
                        reportError("InvalidRegex", new Object[]{xSFacets.pattern, e.getLocalizedMessage()});
                        regularExpression = null;
                    }
                    if (regularExpression != null) {
                        this.fPattern = new Vector();
                        this.fPattern.addElement(regularExpression);
                        this.fPatternStr = new Vector();
                        this.fPatternStr.addElement(xSFacets.pattern);
                        this.fFacetsDefined = (short) (this.fFacetsDefined | 8);
                        if ((s2 & 8) != 0) {
                            this.fFixedFacet = (short) (this.fFixedFacet | 8);
                        }
                    }
                }
            }
            if ((s & 16) != 0) {
                if ((allowedFacets & 16) == 0) {
                    reportError("cos-applicable-facets", new Object[]{"whiteSpace", this.fTypeName});
                } else {
                    this.fWhiteSpace = xSFacets.whiteSpace;
                    this.whiteSpaceAnnotation = xSFacets.whiteSpaceAnnotation;
                    this.fFacetsDefined = (short) (this.fFacetsDefined | 16);
                    if ((s2 & 16) != 0) {
                        this.fFixedFacet = (short) (this.fFixedFacet | 16);
                    }
                }
            }
            if ((s & 2048) != 0) {
                if ((allowedFacets & 2048) == 0) {
                    reportError("cos-applicable-facets", new Object[]{"enumeration", this.fTypeName});
                } else {
                    this.fEnumeration = new Vector();
                    Vector vector = xSFacets.enumeration;
                    this.fEnumerationType = new short[vector.size()];
                    this.fEnumerationItemType = new ShortList[vector.size()];
                    Vector vector2 = xSFacets.enumNSDecls;
                    ValidationContextImpl validationContextImpl = new ValidationContextImpl(validationContext);
                    this.enumerationAnnotations = xSFacets.enumAnnotations;
                    int i16 = 0;
                    while (i16 < vector.size()) {
                        if (vector2 != null) {
                            validationContextImpl.setNSContext((NamespaceContext) vector2.elementAt(i16));
                        }
                        try {
                            ValidatedInfo actualEnumValue = getActualEnumValue((String) vector.elementAt(i16), validationContextImpl, validatedInfo);
                            try {
                                this.fEnumeration.addElement(actualEnumValue.actualValue);
                                this.fEnumerationType[i16] = actualEnumValue.actualValueType;
                                this.fEnumerationItemType[i16] = actualEnumValue.itemValueTypes;
                            } catch (InvalidDatatypeValueException unused) {
                                i14 = 2;
                            }
                        } catch (InvalidDatatypeValueException unused2) {
                            i14 = i15;
                            Object[] objArr = new Object[i14];
                            objArr[0] = vector.elementAt(i16);
                            objArr[1] = getBaseType().getName();
                            reportError("enumeration-valid-restriction", objArr);
                            i16++;
                            i15 = 2;
                        }
                        i16++;
                        i15 = 2;
                    }
                    this.fFacetsDefined = (short) (this.fFacetsDefined | 2048);
                    if ((s2 & 2048) != 0) {
                        this.fFixedFacet = (short) (this.fFixedFacet | 2048);
                    }
                }
            }
            if ((s & 32) != 0) {
                if ((allowedFacets & 32) == 0) {
                    reportError("cos-applicable-facets", new Object[]{"maxInclusive", this.fTypeName});
                } else {
                    this.maxInclusiveAnnotation = xSFacets.maxInclusiveAnnotation;
                    try {
                        this.fMaxInclusive = this.fBase.getActualValue(xSFacets.maxInclusive, validationContext, validatedInfo, true);
                        this.fFacetsDefined = (short) (this.fFacetsDefined | 32);
                        if ((s2 & 32) != 0) {
                            this.fFixedFacet = (short) (this.fFixedFacet | 32);
                        }
                    } catch (InvalidDatatypeValueException e2) {
                        reportError(e2.getKey(), e2.getArgs());
                        reportError("FacetValueFromBase", new Object[]{this.fTypeName, xSFacets.maxInclusive, "maxInclusive", this.fBase.getName()});
                    }
                    XSSimpleTypeDecl xSSimpleTypeDecl = this.fBase;
                    if (!((xSSimpleTypeDecl.fFacetsDefined & 32) == 0 || (xSSimpleTypeDecl.fFixedFacet & 32) == 0 || this.fDVs[this.fValidationDV].compare(this.fMaxInclusive, xSSimpleTypeDecl.fMaxInclusive) == 0)) {
                        reportError("FixedFacetValue", new Object[]{"maxInclusive", this.fMaxInclusive, this.fBase.fMaxInclusive, this.fTypeName});
                    }
                    try {
                        this.fBase.validate(validationContext, validatedInfo);
                    } catch (InvalidDatatypeValueException e3) {
                        reportError(e3.getKey(), e3.getArgs());
                        reportError("FacetValueFromBase", new Object[]{this.fTypeName, xSFacets.maxInclusive, "maxInclusive", this.fBase.getName()});
                    }
                }
            }
            if ((s & 64) != 0) {
                if ((allowedFacets & 64) == 0) {
                    reportError("cos-applicable-facets", new Object[]{"maxExclusive", this.fTypeName});
                } else {
                    this.maxExclusiveAnnotation = xSFacets.maxExclusiveAnnotation;
                    try {
                        this.fMaxExclusive = this.fBase.getActualValue(xSFacets.maxExclusive, validationContext, validatedInfo, true);
                        this.fFacetsDefined = (short) (this.fFacetsDefined | 64);
                        if ((s2 & 64) != 0) {
                            this.fFixedFacet = (short) (this.fFixedFacet | 64);
                        }
                    } catch (InvalidDatatypeValueException e4) {
                        reportError(e4.getKey(), e4.getArgs());
                        reportError("FacetValueFromBase", new Object[]{this.fTypeName, xSFacets.maxExclusive, "maxExclusive", this.fBase.getName()});
                    }
                    XSSimpleTypeDecl xSSimpleTypeDecl2 = this.fBase;
                    if ((xSSimpleTypeDecl2.fFacetsDefined & 64) != 0) {
                        int compare3 = this.fDVs[this.fValidationDV].compare(this.fMaxExclusive, xSSimpleTypeDecl2.fMaxExclusive);
                        if (!((this.fBase.fFixedFacet & 64) == 0 || compare3 == 0)) {
                            reportError("FixedFacetValue", new Object[]{"maxExclusive", xSFacets.maxExclusive, this.fBase.fMaxExclusive, this.fTypeName});
                        }
                        if (compare3 == 0) {
                            z2 = false;
                            if (!z2) {
                                try {
                                    this.fBase.validate(validationContext, validatedInfo);
                                } catch (InvalidDatatypeValueException e5) {
                                    reportError(e5.getKey(), e5.getArgs());
                                    reportError("FacetValueFromBase", new Object[]{this.fTypeName, xSFacets.maxExclusive, "maxExclusive", this.fBase.getName()});
                                }
                            } else {
                                XSSimpleTypeDecl xSSimpleTypeDecl3 = this.fBase;
                                if ((xSSimpleTypeDecl3.fFacetsDefined & 32) != 0 && this.fDVs[this.fValidationDV].compare(this.fMaxExclusive, xSSimpleTypeDecl3.fMaxInclusive) > 0) {
                                    reportError("maxExclusive-valid-restriction.2", new Object[]{xSFacets.maxExclusive, this.fBase.fMaxInclusive});
                                }
                            }
                        }
                    }
                    z2 = true;
                    if (!z2) {
                    }
                }
            }
            if ((s & 128) != 0) {
                if ((allowedFacets & 128) == 0) {
                    reportError("cos-applicable-facets", new Object[]{"minExclusive", this.fTypeName});
                } else {
                    this.minExclusiveAnnotation = xSFacets.minExclusiveAnnotation;
                    try {
                        this.fMinExclusive = this.fBase.getActualValue(xSFacets.minExclusive, validationContext, validatedInfo, true);
                        this.fFacetsDefined = (short) (this.fFacetsDefined | 128);
                        if ((s2 & 128) != 0) {
                            this.fFixedFacet = (short) (this.fFixedFacet | 128);
                        }
                    } catch (InvalidDatatypeValueException e6) {
                        reportError(e6.getKey(), e6.getArgs());
                        reportError("FacetValueFromBase", new Object[]{this.fTypeName, xSFacets.minExclusive, "minExclusive", this.fBase.getName()});
                    }
                    XSSimpleTypeDecl xSSimpleTypeDecl4 = this.fBase;
                    if ((xSSimpleTypeDecl4.fFacetsDefined & 128) != 0) {
                        int compare4 = this.fDVs[this.fValidationDV].compare(this.fMinExclusive, xSSimpleTypeDecl4.fMinExclusive);
                        if (!((this.fBase.fFixedFacet & 128) == 0 || compare4 == 0)) {
                            reportError("FixedFacetValue", new Object[]{"minExclusive", xSFacets.minExclusive, this.fBase.fMinExclusive, this.fTypeName});
                        }
                        if (compare4 == 0) {
                            z = false;
                            if (!z) {
                                try {
                                    this.fBase.validate(validationContext, validatedInfo);
                                } catch (InvalidDatatypeValueException e7) {
                                    reportError(e7.getKey(), e7.getArgs());
                                    reportError("FacetValueFromBase", new Object[]{this.fTypeName, xSFacets.minExclusive, "minExclusive", this.fBase.getName()});
                                }
                            } else {
                                XSSimpleTypeDecl xSSimpleTypeDecl5 = this.fBase;
                                if ((xSSimpleTypeDecl5.fFacetsDefined & 256) != 0 && this.fDVs[this.fValidationDV].compare(this.fMinExclusive, xSSimpleTypeDecl5.fMinInclusive) < 0) {
                                    reportError("minExclusive-valid-restriction.3", new Object[]{xSFacets.minExclusive, this.fBase.fMinInclusive});
                                }
                            }
                        }
                    }
                    z = true;
                    if (!z) {
                    }
                }
            }
            if ((s & 256) != 0) {
                if ((allowedFacets & 256) == 0) {
                    reportError("cos-applicable-facets", new Object[]{"minInclusive", this.fTypeName});
                } else {
                    this.minInclusiveAnnotation = xSFacets.minInclusiveAnnotation;
                    try {
                        this.fMinInclusive = this.fBase.getActualValue(xSFacets.minInclusive, validationContext, validatedInfo, true);
                        this.fFacetsDefined = (short) (this.fFacetsDefined | 256);
                        if ((s2 & 256) != 0) {
                            this.fFixedFacet = (short) (this.fFixedFacet | 256);
                        }
                    } catch (InvalidDatatypeValueException e8) {
                        reportError(e8.getKey(), e8.getArgs());
                        reportError("FacetValueFromBase", new Object[]{this.fTypeName, xSFacets.minInclusive, "minInclusive", this.fBase.getName()});
                    }
                    XSSimpleTypeDecl xSSimpleTypeDecl6 = this.fBase;
                    if (!((xSSimpleTypeDecl6.fFacetsDefined & 256) == 0 || (xSSimpleTypeDecl6.fFixedFacet & 256) == 0 || this.fDVs[this.fValidationDV].compare(this.fMinInclusive, xSSimpleTypeDecl6.fMinInclusive) == 0)) {
                        reportError("FixedFacetValue", new Object[]{"minInclusive", xSFacets.minInclusive, this.fBase.fMinInclusive, this.fTypeName});
                    }
                    try {
                        this.fBase.validate(validationContext, validatedInfo);
                    } catch (InvalidDatatypeValueException e9) {
                        reportError(e9.getKey(), e9.getArgs());
                        reportError("FacetValueFromBase", new Object[]{this.fTypeName, xSFacets.minInclusive, "minInclusive", this.fBase.getName()});
                    }
                }
            }
            if ((s & 512) != 0) {
                if ((allowedFacets & 512) == 0) {
                    reportError("cos-applicable-facets", new Object[]{"totalDigits", this.fTypeName});
                } else {
                    this.totalDigitsAnnotation = xSFacets.totalDigitsAnnotation;
                    this.fTotalDigits = xSFacets.totalDigits;
                    this.fFacetsDefined = (short) (this.fFacetsDefined | 512);
                    if ((s2 & 512) != 0) {
                        this.fFixedFacet = (short) (this.fFixedFacet | 512);
                    }
                }
            }
            if ((s & 1024) != 0) {
                if ((allowedFacets & 1024) == 0) {
                    reportError("cos-applicable-facets", new Object[]{"fractionDigits", this.fTypeName});
                } else {
                    this.fFractionDigits = xSFacets.fractionDigits;
                    this.fractionDigitsAnnotation = xSFacets.fractionDigitsAnnotation;
                    this.fFacetsDefined = (short) (this.fFacetsDefined | 1024);
                    if ((s2 & 1024) != 0) {
                        this.fFixedFacet = (short) (this.fFixedFacet | 1024);
                    }
                }
            }
            if (s3 != 0) {
                this.fPatternType = s3;
            }
            short s10 = this.fFacetsDefined;
            if (s10 != 0) {
                if (!((s10 & 2) == 0 || (s10 & 4) == 0 || (i13 = this.fMinLength) <= this.fMaxLength)) {
                    reportError("minLength-less-than-equal-to-maxLength", new Object[]{Integer.toString(i13), Integer.toString(this.fMaxLength), this.fTypeName});
                }
                short s11 = this.fFacetsDefined;
                if (!((s11 & 64) == 0 || (s11 & 32) == 0)) {
                    reportError("maxInclusive-maxExclusive", new Object[]{this.fMaxInclusive, this.fMaxExclusive, this.fTypeName});
                }
                short s12 = this.fFacetsDefined;
                if (!((s12 & 128) == 0 || (s12 & 256) == 0)) {
                    reportError("minInclusive-minExclusive", new Object[]{this.fMinInclusive, this.fMinExclusive, this.fTypeName});
                }
                short s13 = this.fFacetsDefined;
                if (!((s13 & 32) == 0 || (s13 & 256) == 0 || (compare2 = this.fDVs[this.fValidationDV].compare(this.fMinInclusive, this.fMaxInclusive)) == -1 || compare2 == 0)) {
                    reportError("minInclusive-less-than-equal-to-maxInclusive", new Object[]{this.fMinInclusive, this.fMaxInclusive, this.fTypeName});
                }
                short s14 = this.fFacetsDefined;
                if (!((s14 & 64) == 0 || (s14 & 128) == 0 || (compare = this.fDVs[this.fValidationDV].compare(this.fMinExclusive, this.fMaxExclusive)) == -1 || compare == 0)) {
                    reportError("minExclusive-less-than-equal-to-maxExclusive", new Object[]{this.fMinExclusive, this.fMaxExclusive, this.fTypeName});
                }
                short s15 = this.fFacetsDefined;
                if (!((s15 & 32) == 0 || (s15 & 128) == 0 || this.fDVs[this.fValidationDV].compare(this.fMinExclusive, this.fMaxInclusive) == -1)) {
                    reportError("minExclusive-less-than-maxInclusive", new Object[]{this.fMinExclusive, this.fMaxInclusive, this.fTypeName});
                }
                short s16 = this.fFacetsDefined;
                if (!((s16 & 64) == 0 || (s16 & 256) == 0 || this.fDVs[this.fValidationDV].compare(this.fMinInclusive, this.fMaxExclusive) == -1)) {
                    reportError("minInclusive-less-than-maxExclusive", new Object[]{this.fMinInclusive, this.fMaxExclusive, this.fTypeName});
                }
                short s17 = this.fFacetsDefined;
                if ((s17 & 1024) == 0 || (s17 & 512) == 0 || (i12 = this.fFractionDigits) <= this.fTotalDigits) {
                    s5 = 1;
                } else {
                    s5 = 1;
                    reportError("fractionDigits-totalDigits", new Object[]{Integer.toString(i12), Integer.toString(this.fTotalDigits), this.fTypeName});
                }
                if ((this.fFacetsDefined & s5) != 0) {
                    XSSimpleTypeDecl xSSimpleTypeDecl7 = this.fBase;
                    if ((xSSimpleTypeDecl7.fFacetsDefined & 2) != 0 && (i11 = this.fLength) < xSSimpleTypeDecl7.fMinLength) {
                        reportError("length-minLength-maxLength.1.1", new Object[]{this.fTypeName, Integer.toString(i11), Integer.toString(this.fBase.fMinLength)});
                    }
                    XSSimpleTypeDecl xSSimpleTypeDecl8 = this.fBase;
                    if ((xSSimpleTypeDecl8.fFacetsDefined & 4) != 0 && (i10 = this.fLength) > xSSimpleTypeDecl8.fMaxLength) {
                        reportError("length-minLength-maxLength.2.1", new Object[]{this.fTypeName, Integer.toString(i10), Integer.toString(this.fBase.fMaxLength)});
                    }
                    XSSimpleTypeDecl xSSimpleTypeDecl9 = this.fBase;
                    s6 = 1;
                    if (!((xSSimpleTypeDecl9.fFacetsDefined & 1) == 0 || (i9 = this.fLength) == xSSimpleTypeDecl9.fLength)) {
                        reportError("length-valid-restriction", new Object[]{Integer.toString(i9), Integer.toString(this.fBase.fLength), this.fTypeName});
                    }
                } else {
                    s6 = 1;
                }
                if (!((this.fBase.fFacetsDefined & s6) == 0 && (this.fFacetsDefined & s6) == 0)) {
                    if ((this.fFacetsDefined & 2) != 0) {
                        int i17 = this.fBase.fLength;
                        if (i17 < this.fMinLength) {
                            s9 = 2;
                            reportError("length-minLength-maxLength.1.1", new Object[]{this.fTypeName, Integer.toString(i17), Integer.toString(this.fMinLength)});
                        } else {
                            s9 = 2;
                        }
                        if ((this.fBase.fFacetsDefined & s9) == 0) {
                            c2 = 0;
                            reportError("length-minLength-maxLength.1.2.a", new Object[]{this.fTypeName});
                        } else {
                            c2 = 0;
                        }
                        int i18 = this.fMinLength;
                        if (i18 != this.fBase.fMinLength) {
                            Object[] objArr2 = new Object[3];
                            objArr2[c2] = this.fTypeName;
                            objArr2[1] = Integer.toString(i18);
                            objArr2[2] = Integer.toString(this.fBase.fMinLength);
                            reportError("length-minLength-maxLength.1.2.b", objArr2);
                        }
                    }
                    if ((this.fFacetsDefined & 4) != 0) {
                        int i19 = this.fBase.fLength;
                        if (i19 > this.fMaxLength) {
                            reportError("length-minLength-maxLength.2.1", new Object[]{this.fTypeName, Integer.toString(i19), Integer.toString(this.fMaxLength)});
                        }
                        if ((this.fBase.fFacetsDefined & 4) == 0) {
                            c = 0;
                            reportError("length-minLength-maxLength.2.2.a", new Object[]{this.fTypeName});
                        } else {
                            c = 0;
                        }
                        int i20 = this.fMaxLength;
                        if (i20 != this.fBase.fMaxLength) {
                            Object[] objArr3 = new Object[3];
                            objArr3[c] = this.fTypeName;
                            objArr3[1] = Integer.toString(i20);
                            s7 = 2;
                            objArr3[2] = Integer.toString(this.fBase.fBase.fMaxLength);
                            reportError("length-minLength-maxLength.2.2.b", objArr3);
                            if ((this.fFacetsDefined & s7) != 0) {
                                XSSimpleTypeDecl xSSimpleTypeDecl10 = this.fBase;
                                short s18 = xSSimpleTypeDecl10.fFacetsDefined;
                                if ((s18 & 4) != 0) {
                                    int i21 = this.fMinLength;
                                    if (i21 > xSSimpleTypeDecl10.fMaxLength) {
                                        reportError("minLength-less-than-equal-to-maxLength", new Object[]{Integer.toString(i21), Integer.toString(this.fBase.fMaxLength), this.fTypeName});
                                    }
                                } else if ((s18 & 2) != 0) {
                                    if (!((xSSimpleTypeDecl10.fFixedFacet & 2) == 0 || (i8 = this.fMinLength) == xSSimpleTypeDecl10.fMinLength)) {
                                        reportError("FixedFacetValue", new Object[]{"minLength", Integer.toString(i8), Integer.toString(this.fBase.fMinLength), this.fTypeName});
                                    }
                                    int i22 = this.fMinLength;
                                    if (i22 < this.fBase.fMinLength) {
                                        reportError("minLength-valid-restriction", new Object[]{Integer.toString(i22), Integer.toString(this.fBase.fMinLength), this.fTypeName});
                                    }
                                }
                            }
                            if ((this.fFacetsDefined & 4) != 0) {
                                XSSimpleTypeDecl xSSimpleTypeDecl11 = this.fBase;
                                if ((xSSimpleTypeDecl11.fFacetsDefined & 2) != 0 && this.fMaxLength < (i7 = xSSimpleTypeDecl11.fMinLength)) {
                                    reportError("minLength-less-than-equal-to-maxLength", new Object[]{Integer.toString(i7), Integer.toString(this.fMaxLength)});
                                }
                            }
                            if ((this.fFacetsDefined & 4) != 0) {
                                XSSimpleTypeDecl xSSimpleTypeDecl12 = this.fBase;
                                if ((xSSimpleTypeDecl12.fFacetsDefined & 4) != 0) {
                                    if (!((xSSimpleTypeDecl12.fFixedFacet & 4) == 0 || (i6 = this.fMaxLength) == xSSimpleTypeDecl12.fMaxLength)) {
                                        reportError("FixedFacetValue", new Object[]{"maxLength", Integer.toString(i6), Integer.toString(this.fBase.fMaxLength), this.fTypeName});
                                    }
                                    int i23 = this.fMaxLength;
                                    if (i23 > this.fBase.fMaxLength) {
                                        reportError("maxLength-valid-restriction", new Object[]{Integer.toString(i23), Integer.toString(this.fBase.fMaxLength), this.fTypeName});
                                    }
                                }
                            }
                            if ((this.fFacetsDefined & 512) != 0) {
                                XSSimpleTypeDecl xSSimpleTypeDecl13 = this.fBase;
                                if ((xSSimpleTypeDecl13.fFacetsDefined & 512) != 0) {
                                    if (!((xSSimpleTypeDecl13.fFixedFacet & 512) == 0 || (i5 = this.fTotalDigits) == xSSimpleTypeDecl13.fTotalDigits)) {
                                        reportError("FixedFacetValue", new Object[]{"totalDigits", Integer.toString(i5), Integer.toString(this.fBase.fTotalDigits), this.fTypeName});
                                    }
                                    int i24 = this.fTotalDigits;
                                    if (i24 > this.fBase.fTotalDigits) {
                                        reportError("totalDigits-valid-restriction", new Object[]{Integer.toString(i24), Integer.toString(this.fBase.fTotalDigits), this.fTypeName});
                                    }
                                }
                            }
                            if ((this.fFacetsDefined & 1024) != 0) {
                                XSSimpleTypeDecl xSSimpleTypeDecl14 = this.fBase;
                                if ((xSSimpleTypeDecl14.fFacetsDefined & 512) != 0 && (i4 = this.fFractionDigits) > xSSimpleTypeDecl14.fTotalDigits) {
                                    reportError("fractionDigits-totalDigits", new Object[]{Integer.toString(i4), Integer.toString(this.fTotalDigits), this.fTypeName});
                                }
                            }
                            if ((this.fFacetsDefined & 1024) != 0) {
                                XSSimpleTypeDecl xSSimpleTypeDecl15 = this.fBase;
                                if ((xSSimpleTypeDecl15.fFacetsDefined & 1024) != 0) {
                                    if (!((xSSimpleTypeDecl15.fFixedFacet & 1024) == 0 || this.fFractionDigits == xSSimpleTypeDecl15.fFractionDigits) || (this.fValidationDV == 24 && this.fFractionDigits != 0)) {
                                        reportError("FixedFacetValue", new Object[]{"fractionDigits", Integer.toString(this.fFractionDigits), Integer.toString(this.fBase.fFractionDigits), this.fTypeName});
                                    }
                                    int i25 = this.fFractionDigits;
                                    if (i25 > this.fBase.fFractionDigits) {
                                        reportError("fractionDigits-valid-restriction", new Object[]{Integer.toString(i25), Integer.toString(this.fBase.fFractionDigits), this.fTypeName});
                                    }
                                } else if (this.fValidationDV == 24 && (i3 = this.fFractionDigits) != 0) {
                                    reportError("FixedFacetValue", new Object[]{"fractionDigits", Integer.toString(i3), "0", this.fTypeName});
                                }
                            }
                            if ((this.fFacetsDefined & 16) != 0) {
                                XSSimpleTypeDecl xSSimpleTypeDecl16 = this.fBase;
                                if ((xSSimpleTypeDecl16.fFacetsDefined & 16) != 0) {
                                    if ((xSSimpleTypeDecl16.fFixedFacet & 16) == 0 || (s8 = this.fWhiteSpace) == xSSimpleTypeDecl16.fWhiteSpace) {
                                        i = 2;
                                    } else {
                                        i = 2;
                                        reportError("FixedFacetValue", new Object[]{"whiteSpace", whiteSpaceValue(s8), whiteSpaceValue(this.fBase.fWhiteSpace), this.fTypeName});
                                    }
                                    if (this.fWhiteSpace == 0 && this.fBase.fWhiteSpace == i) {
                                        Object[] objArr4 = new Object[i];
                                        objArr4[0] = this.fTypeName;
                                        i2 = 1;
                                        objArr4[1] = SchemaSymbols.ATTVAL_PRESERVE;
                                        reportError("whiteSpace-valid-restriction.1", objArr4);
                                    } else {
                                        i2 = 1;
                                    }
                                    if (this.fWhiteSpace == i2 && this.fBase.fWhiteSpace == 2) {
                                        Object[] objArr5 = new Object[2];
                                        objArr5[0] = this.fTypeName;
                                        objArr5[i2] = SchemaSymbols.ATTVAL_REPLACE;
                                        reportError("whiteSpace-valid-restriction.1", objArr5);
                                    }
                                    if (this.fWhiteSpace == 0 && this.fBase.fWhiteSpace == i2) {
                                        Object[] objArr6 = new Object[i2];
                                        objArr6[0] = this.fTypeName;
                                        reportError("whiteSpace-valid-restriction.2", objArr6);
                                    }
                                }
                            }
                        }
                    }
                }
                s7 = 2;
                if ((this.fFacetsDefined & s7) != 0) {
                }
                if ((this.fFacetsDefined & 4) != 0) {
                }
                if ((this.fFacetsDefined & 4) != 0) {
                }
                if ((this.fFacetsDefined & 512) != 0) {
                }
                if ((this.fFacetsDefined & 1024) != 0) {
                }
                if ((this.fFacetsDefined & 1024) != 0) {
                }
                if ((this.fFacetsDefined & 16) != 0) {
                }
            }
            short s19 = this.fFacetsDefined;
            if ((s19 & 1) == 0) {
                XSSimpleTypeDecl xSSimpleTypeDecl17 = this.fBase;
                if ((xSSimpleTypeDecl17.fFacetsDefined & 1) != 0) {
                    this.fFacetsDefined = (short) (s19 | 1);
                    this.fLength = xSSimpleTypeDecl17.fLength;
                    this.lengthAnnotation = xSSimpleTypeDecl17.lengthAnnotation;
                }
            }
            short s20 = this.fFacetsDefined;
            if ((s20 & 2) == 0) {
                XSSimpleTypeDecl xSSimpleTypeDecl18 = this.fBase;
                if ((xSSimpleTypeDecl18.fFacetsDefined & 2) != 0) {
                    this.fFacetsDefined = (short) (s20 | 2);
                    this.fMinLength = xSSimpleTypeDecl18.fMinLength;
                    this.minLengthAnnotation = xSSimpleTypeDecl18.minLengthAnnotation;
                }
            }
            short s21 = this.fFacetsDefined;
            if ((s21 & 4) == 0) {
                XSSimpleTypeDecl xSSimpleTypeDecl19 = this.fBase;
                if ((xSSimpleTypeDecl19.fFacetsDefined & 4) != 0) {
                    this.fFacetsDefined = (short) (s21 | 4);
                    this.fMaxLength = xSSimpleTypeDecl19.fMaxLength;
                    this.maxLengthAnnotation = xSSimpleTypeDecl19.maxLengthAnnotation;
                }
            }
            XSSimpleTypeDecl xSSimpleTypeDecl20 = this.fBase;
            if ((xSSimpleTypeDecl20.fFacetsDefined & 8) != 0) {
                short s22 = this.fFacetsDefined;
                if ((s22 & 8) == 0) {
                    this.fFacetsDefined = (short) (s22 | 8);
                    this.fPattern = xSSimpleTypeDecl20.fPattern;
                    this.fPatternStr = xSSimpleTypeDecl20.fPatternStr;
                    this.patternAnnotations = xSSimpleTypeDecl20.patternAnnotations;
                } else {
                    for (int size = xSSimpleTypeDecl20.fPattern.size() - 1; size >= 0; size--) {
                        this.fPattern.addElement(this.fBase.fPattern.elementAt(size));
                        this.fPatternStr.addElement(this.fBase.fPatternStr.elementAt(size));
                    }
                    XSObjectListImpl xSObjectListImpl = this.fBase.patternAnnotations;
                    if (xSObjectListImpl != null) {
                        if (this.patternAnnotations != null) {
                            for (int length = xSObjectListImpl.getLength() - 1; length >= 0; length--) {
                                this.patternAnnotations.addXSObject(this.fBase.patternAnnotations.item(length));
                            }
                        } else {
                            this.patternAnnotations = xSObjectListImpl;
                        }
                    }
                }
            }
            short s23 = this.fFacetsDefined;
            if ((s23 & 16) == 0) {
                XSSimpleTypeDecl xSSimpleTypeDecl21 = this.fBase;
                if ((xSSimpleTypeDecl21.fFacetsDefined & 16) != 0) {
                    this.fFacetsDefined = (short) (s23 | 16);
                    this.fWhiteSpace = xSSimpleTypeDecl21.fWhiteSpace;
                    this.whiteSpaceAnnotation = xSSimpleTypeDecl21.whiteSpaceAnnotation;
                }
            }
            short s24 = this.fFacetsDefined;
            if ((s24 & 2048) == 0) {
                XSSimpleTypeDecl xSSimpleTypeDecl22 = this.fBase;
                if ((xSSimpleTypeDecl22.fFacetsDefined & 2048) != 0) {
                    this.fFacetsDefined = (short) (s24 | 2048);
                    this.fEnumeration = xSSimpleTypeDecl22.fEnumeration;
                    this.enumerationAnnotations = xSSimpleTypeDecl22.enumerationAnnotations;
                }
            }
            XSSimpleTypeDecl xSSimpleTypeDecl23 = this.fBase;
            if ((xSSimpleTypeDecl23.fFacetsDefined & 64) != 0) {
                short s25 = this.fFacetsDefined;
                if ((s25 & 64) == 0 && (s25 & 32) == 0) {
                    this.fFacetsDefined = (short) (s25 | 64);
                    this.fMaxExclusive = xSSimpleTypeDecl23.fMaxExclusive;
                    this.maxExclusiveAnnotation = xSSimpleTypeDecl23.maxExclusiveAnnotation;
                }
            }
            XSSimpleTypeDecl xSSimpleTypeDecl24 = this.fBase;
            if ((xSSimpleTypeDecl24.fFacetsDefined & 32) != 0) {
                short s26 = this.fFacetsDefined;
                if ((s26 & 64) == 0 && (s26 & 32) == 0) {
                    this.fFacetsDefined = (short) (s26 | 32);
                    this.fMaxInclusive = xSSimpleTypeDecl24.fMaxInclusive;
                    this.maxInclusiveAnnotation = xSSimpleTypeDecl24.maxInclusiveAnnotation;
                }
            }
            XSSimpleTypeDecl xSSimpleTypeDecl25 = this.fBase;
            if ((xSSimpleTypeDecl25.fFacetsDefined & 128) != 0) {
                short s27 = this.fFacetsDefined;
                if ((s27 & 128) == 0 && (s27 & 256) == 0) {
                    this.fFacetsDefined = (short) (s27 | 128);
                    this.fMinExclusive = xSSimpleTypeDecl25.fMinExclusive;
                    this.minExclusiveAnnotation = xSSimpleTypeDecl25.minExclusiveAnnotation;
                }
            }
            XSSimpleTypeDecl xSSimpleTypeDecl26 = this.fBase;
            if ((xSSimpleTypeDecl26.fFacetsDefined & 256) != 0) {
                short s28 = this.fFacetsDefined;
                if ((s28 & 128) == 0 && (s28 & 256) == 0) {
                    this.fFacetsDefined = (short) (s28 | 256);
                    this.fMinInclusive = xSSimpleTypeDecl26.fMinInclusive;
                    this.minInclusiveAnnotation = xSSimpleTypeDecl26.minInclusiveAnnotation;
                }
            }
            XSSimpleTypeDecl xSSimpleTypeDecl27 = this.fBase;
            if ((xSSimpleTypeDecl27.fFacetsDefined & 512) != 0) {
                short s29 = this.fFacetsDefined;
                if ((s29 & 512) == 0) {
                    this.fFacetsDefined = (short) (s29 | 512);
                    this.fTotalDigits = xSSimpleTypeDecl27.fTotalDigits;
                    this.totalDigitsAnnotation = xSSimpleTypeDecl27.totalDigitsAnnotation;
                }
            }
            XSSimpleTypeDecl xSSimpleTypeDecl28 = this.fBase;
            if ((xSSimpleTypeDecl28.fFacetsDefined & 1024) != 0) {
                short s30 = this.fFacetsDefined;
                if ((s30 & 1024) == 0) {
                    this.fFacetsDefined = (short) (s30 | 1024);
                    this.fFractionDigits = xSSimpleTypeDecl28.fFractionDigits;
                    this.fractionDigitsAnnotation = xSSimpleTypeDecl28.fractionDigitsAnnotation;
                }
            }
            if (this.fPatternType == 0 && (s4 = this.fBase.fPatternType) != 0) {
                this.fPatternType = s4;
            }
            this.fFixedFacet = (short) (this.fFixedFacet | this.fBase.fFixedFacet);
            calcFundamentalFacets();
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType
    public Object validate(String str, ValidationContext validationContext, ValidatedInfo validatedInfo) throws InvalidDatatypeValueException {
        if (validationContext == null) {
            validationContext = fEmptyContext;
        }
        if (validatedInfo == null) {
            validatedInfo = new ValidatedInfo();
        } else {
            validatedInfo.memberType = null;
        }
        Object actualValue = getActualValue(str, validationContext, validatedInfo, validationContext == null || validationContext.needToNormalize());
        validate(validationContext, validatedInfo);
        return actualValue;
    }

    /* access modifiers changed from: protected */
    public ValidatedInfo getActualEnumValue(String str, ValidationContext validationContext, ValidatedInfo validatedInfo) throws InvalidDatatypeValueException {
        return this.fBase.validateWithInfo(str, validationContext, validatedInfo);
    }

    public ValidatedInfo validateWithInfo(String str, ValidationContext validationContext, ValidatedInfo validatedInfo) throws InvalidDatatypeValueException {
        if (validationContext == null) {
            validationContext = fEmptyContext;
        }
        if (validatedInfo == null) {
            validatedInfo = new ValidatedInfo();
        } else {
            validatedInfo.memberType = null;
        }
        getActualValue(str, validationContext, validatedInfo, validationContext == null || validationContext.needToNormalize());
        validate(validationContext, validatedInfo);
        return validatedInfo;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType
    public Object validate(Object obj, ValidationContext validationContext, ValidatedInfo validatedInfo) throws InvalidDatatypeValueException {
        if (validationContext == null) {
            validationContext = fEmptyContext;
        }
        if (validatedInfo == null) {
            validatedInfo = new ValidatedInfo();
        } else {
            validatedInfo.memberType = null;
        }
        Object actualValue = getActualValue(obj, validationContext, validatedInfo, validationContext == null || validationContext.needToNormalize());
        validate(validationContext, validatedInfo);
        return actualValue;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType
    public void validate(ValidationContext validationContext, ValidatedInfo validatedInfo) throws InvalidDatatypeValueException {
        short s;
        if (validationContext == null) {
            validationContext = fEmptyContext;
        }
        if (!(!validationContext.needFacetChecking() || (s = this.fFacetsDefined) == 0 || s == 16)) {
            checkFacets(validatedInfo);
        }
        if (validationContext.needExtraChecking()) {
            checkExtraRules(validationContext, validatedInfo);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00d1, code lost:
        r15 = true;
     */
    private void checkFacets(ValidatedInfo validatedInfo) throws InvalidDatatypeValueException {
        int compare;
        int compare2;
        int totalDigits;
        int fractionDigits;
        boolean z;
        Object obj = validatedInfo.actualValue;
        String str = validatedInfo.normalizedValue;
        short s = validatedInfo.actualValueType;
        ShortList shortList = validatedInfo.itemValueTypes;
        short s2 = this.fValidationDV;
        if (!(s2 == 18 || s2 == 20)) {
            int dataLength = this.fDVs[s2].getDataLength(obj);
            if ((this.fFacetsDefined & 4) != 0 && dataLength > this.fMaxLength) {
                throw new InvalidDatatypeValueException("cvc-maxLength-valid", new Object[]{str, Integer.toString(dataLength), Integer.toString(this.fMaxLength), this.fTypeName});
            } else if ((this.fFacetsDefined & 2) != 0 && dataLength < this.fMinLength) {
                throw new InvalidDatatypeValueException("cvc-minLength-valid", new Object[]{str, Integer.toString(dataLength), Integer.toString(this.fMinLength), this.fTypeName});
            } else if (!((this.fFacetsDefined & 1) == 0 || dataLength == this.fLength)) {
                throw new InvalidDatatypeValueException("cvc-length-valid", new Object[]{str, Integer.toString(dataLength), Integer.toString(this.fLength), this.fTypeName});
            }
        }
        if ((this.fFacetsDefined & 2048) != 0) {
            int size = this.fEnumeration.size();
            short convertToPrimitiveKind = convertToPrimitiveKind(s);
            int i = 0;
            while (true) {
                if (i >= size) {
                    z = false;
                    break;
                }
                short convertToPrimitiveKind2 = convertToPrimitiveKind(this.fEnumerationType[i]);
                if ((convertToPrimitiveKind == convertToPrimitiveKind2 || ((convertToPrimitiveKind == 1 && convertToPrimitiveKind2 == 2) || (convertToPrimitiveKind == 2 && convertToPrimitiveKind2 == 1))) && this.fEnumeration.elementAt(i).equals(obj)) {
                    if (convertToPrimitiveKind != 44 && convertToPrimitiveKind != 43) {
                        break;
                    }
                    ShortList shortList2 = this.fEnumerationItemType[i];
                    int length = shortList != null ? shortList.getLength() : 0;
                    if (length == (shortList2 != null ? shortList2.getLength() : 0)) {
                        int i2 = 0;
                        while (i2 < length) {
                            short convertToPrimitiveKind3 = convertToPrimitiveKind(shortList.item(i2));
                            short convertToPrimitiveKind4 = convertToPrimitiveKind(shortList2.item(i2));
                            if (convertToPrimitiveKind3 != convertToPrimitiveKind4 && ((convertToPrimitiveKind3 != 1 || convertToPrimitiveKind4 != 2) && (convertToPrimitiveKind3 != 2 || convertToPrimitiveKind4 != 1))) {
                                break;
                            }
                            i2++;
                        }
                        if (i2 == length) {
                            break;
                        }
                    } else {
                        continue;
                    }
                }
                i++;
            }
            if (!z) {
                throw new InvalidDatatypeValueException("cvc-enumeration-valid", new Object[]{str, this.fEnumeration.toString()});
            }
        }
        if ((this.fFacetsDefined & 1024) != 0 && (fractionDigits = this.fDVs[this.fValidationDV].getFractionDigits(obj)) > this.fFractionDigits) {
            throw new InvalidDatatypeValueException("cvc-fractionDigits-valid", new Object[]{str, Integer.toString(fractionDigits), Integer.toString(this.fFractionDigits)});
        } else if ((this.fFacetsDefined & 512) != 0 && (totalDigits = this.fDVs[this.fValidationDV].getTotalDigits(obj)) > this.fTotalDigits) {
            throw new InvalidDatatypeValueException("cvc-totalDigits-valid", new Object[]{str, Integer.toString(totalDigits), Integer.toString(this.fTotalDigits)});
        } else if ((this.fFacetsDefined & 32) != 0 && (compare2 = this.fDVs[this.fValidationDV].compare(obj, this.fMaxInclusive)) != -1 && compare2 != 0) {
            throw new InvalidDatatypeValueException("cvc-maxInclusive-valid", new Object[]{str, this.fMaxInclusive, this.fTypeName});
        } else if ((this.fFacetsDefined & 64) != 0 && this.fDVs[this.fValidationDV].compare(obj, this.fMaxExclusive) != -1) {
            throw new InvalidDatatypeValueException("cvc-maxExclusive-valid", new Object[]{str, this.fMaxExclusive, this.fTypeName});
        } else if ((this.fFacetsDefined & 256) != 0 && (compare = this.fDVs[this.fValidationDV].compare(obj, this.fMinInclusive)) != 1 && compare != 0) {
            throw new InvalidDatatypeValueException("cvc-minInclusive-valid", new Object[]{str, this.fMinInclusive, this.fTypeName});
        } else if ((this.fFacetsDefined & 128) != 0 && this.fDVs[this.fValidationDV].compare(obj, this.fMinExclusive) != 1) {
            throw new InvalidDatatypeValueException("cvc-minExclusive-valid", new Object[]{str, this.fMinExclusive, this.fTypeName});
        }
    }

    private void checkExtraRules(ValidationContext validationContext, ValidatedInfo validatedInfo) throws InvalidDatatypeValueException {
        Object obj = validatedInfo.actualValue;
        short s = this.fVariety;
        if (s == 1) {
            this.fDVs[this.fValidationDV].checkExtraRules(obj, validationContext);
        } else if (s == 2) {
            ListDV.ListData listData = (ListDV.ListData) obj;
            XSSimpleType xSSimpleType = validatedInfo.memberType;
            int length = listData.getLength();
            try {
                if (this.fItemType.fVariety == 3) {
                    XSSimpleTypeDecl[] xSSimpleTypeDeclArr = (XSSimpleTypeDecl[]) validatedInfo.memberTypes;
                    for (int i = length - 1; i >= 0; i--) {
                        validatedInfo.actualValue = listData.item(i);
                        validatedInfo.memberType = xSSimpleTypeDeclArr[i];
                        this.fItemType.checkExtraRules(validationContext, validatedInfo);
                    }
                } else {
                    for (int i2 = length - 1; i2 >= 0; i2--) {
                        validatedInfo.actualValue = listData.item(i2);
                        this.fItemType.checkExtraRules(validationContext, validatedInfo);
                    }
                }
            } finally {
                validatedInfo.actualValue = listData;
                validatedInfo.memberType = xSSimpleType;
            }
        } else {
            ((XSSimpleTypeDecl) validatedInfo.memberType).checkExtraRules(validationContext, validatedInfo);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x008c  */
    private Object getActualValue(Object obj, ValidationContext validationContext, ValidatedInfo validatedInfo, boolean z) throws InvalidDatatypeValueException {
        String str;
        XSSimpleTypeDecl xSSimpleTypeDecl;
        short s;
        boolean z2;
        boolean isValidNCName;
        if (z) {
            str = normalize(obj, this.fWhiteSpace);
        } else {
            str = obj.toString();
        }
        int i = 1;
        if ((this.fFacetsDefined & 8) != 0) {
            if (this.fPattern.size() != 0 || str.length() <= 0) {
                for (int size = this.fPattern.size() - 1; size >= 0; size--) {
                    if (!((RegularExpression) this.fPattern.elementAt(size)).matches(str)) {
                        throw new InvalidDatatypeValueException("cvc-pattern-valid", new Object[]{obj, this.fPatternStr.elementAt(size), this.fTypeName});
                    }
                }
            } else {
                throw new InvalidDatatypeValueException("cvc-pattern-valid", new Object[]{obj, "(empty string)", this.fTypeName});
            }
        }
        short s2 = this.fVariety;
        if (s2 == 1) {
            short s3 = this.fPatternType;
            if (s3 != 0) {
                if (s3 == 1) {
                    isValidNCName = XMLChar.isValidNmtoken(str);
                } else if (s3 == 2) {
                    isValidNCName = XMLChar.isValidName(str);
                } else if (s3 == 3) {
                    isValidNCName = XMLChar.isValidNCName(str);
                } else {
                    z2 = false;
                    if (z2) {
                        throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{str, SPECIAL_PATTERN_STRING[this.fPatternType]});
                    }
                }
                z2 = !isValidNCName;
                if (z2) {
                }
            }
            validatedInfo.normalizedValue = str;
            Object actualValue = this.fDVs[this.fValidationDV].getActualValue(str, validationContext);
            validatedInfo.actualValue = actualValue;
            validatedInfo.actualValueType = this.fBuiltInKind;
            return actualValue;
        }
        short s4 = 44;
        if (s2 == 2) {
            StringTokenizer stringTokenizer = new StringTokenizer(str, " ");
            int countTokens = stringTokenizer.countTokens();
            Object[] objArr = new Object[countTokens];
            boolean z3 = this.fItemType.getVariety() == 3;
            if (z3) {
                i = countTokens;
            }
            short[] sArr = new short[i];
            if (!z3) {
                sArr[0] = this.fItemType.fBuiltInKind;
            }
            XSSimpleTypeDecl[] xSSimpleTypeDeclArr = new XSSimpleTypeDecl[countTokens];
            for (int i2 = 0; i2 < countTokens; i2++) {
                objArr[i2] = this.fItemType.getActualValue(stringTokenizer.nextToken(), validationContext, validatedInfo, false);
                if (!(!validationContext.needFacetChecking() || (s = (xSSimpleTypeDecl = this.fItemType).fFacetsDefined) == 0 || s == 16)) {
                    xSSimpleTypeDecl.checkFacets(validatedInfo);
                }
                xSSimpleTypeDeclArr[i2] = (XSSimpleTypeDecl) validatedInfo.memberType;
                if (z3) {
                    sArr[i2] = xSSimpleTypeDeclArr[i2].fBuiltInKind;
                }
            }
            ListDV.ListData listData = new ListDV.ListData(objArr);
            validatedInfo.actualValue = listData;
            if (z3) {
                s4 = 43;
            }
            validatedInfo.actualValueType = s4;
            validatedInfo.memberType = null;
            validatedInfo.memberTypes = xSSimpleTypeDeclArr;
            validatedInfo.itemValueTypes = new ShortListImpl(sArr, sArr.length);
            validatedInfo.normalizedValue = str;
            return listData;
        }
        String obj2 = (this.fMemberTypes.length <= 1 || obj == null) ? obj : obj.toString();
        int i3 = 0;
        while (true) {
            XSSimpleTypeDecl[] xSSimpleTypeDeclArr2 = this.fMemberTypes;
            if (i3 < xSSimpleTypeDeclArr2.length) {
                try {
                    Object actualValue2 = xSSimpleTypeDeclArr2[i3].getActualValue(obj2, validationContext, validatedInfo, true);
                    if (!(!validationContext.needFacetChecking() || this.fMemberTypes[i3].fFacetsDefined == 0 || this.fMemberTypes[i3].fFacetsDefined == 16)) {
                        this.fMemberTypes[i3].checkFacets(validatedInfo);
                    }
                    validatedInfo.memberType = this.fMemberTypes[i3];
                    return actualValue2;
                } catch (InvalidDatatypeValueException unused) {
                    i3++;
                }
            } else {
                StringBuffer stringBuffer = new StringBuffer();
                for (int i4 = 0; i4 < this.fMemberTypes.length; i4++) {
                    if (i4 != 0) {
                        stringBuffer.append(" | ");
                    }
                    XSSimpleTypeDecl xSSimpleTypeDecl2 = this.fMemberTypes[i4];
                    if (xSSimpleTypeDecl2.fTargetNamespace != null) {
                        stringBuffer.append('{');
                        stringBuffer.append(xSSimpleTypeDecl2.fTargetNamespace);
                        stringBuffer.append('}');
                    }
                    stringBuffer.append(xSSimpleTypeDecl2.fTypeName);
                    Vector vector = xSSimpleTypeDecl2.fEnumeration;
                    if (vector != null) {
                        stringBuffer.append(" : [");
                        for (int i5 = 0; i5 < vector.size(); i5++) {
                            if (i5 != 0) {
                                stringBuffer.append(',');
                            }
                            stringBuffer.append(vector.elementAt(i5));
                        }
                        stringBuffer.append(']');
                    }
                }
                throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.3", new Object[]{obj, this.fTypeName, stringBuffer.toString()});
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType
    public boolean isEqual(Object obj, Object obj2) {
        if (obj == null) {
            return false;
        }
        return obj.equals(obj2);
    }

    public boolean isIdentical(Object obj, Object obj2) {
        if (obj == null) {
            return false;
        }
        return this.fDVs[this.fValidationDV].isIdentical(obj, obj2);
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
    public String normalize(Object obj, short s) {
        int i;
        int i2;
        char charAt;
        if (obj == null) {
            return null;
        }
        if ((this.fFacetsDefined & 8) == 0) {
            short s2 = fDVNormalizeType[this.fValidationDV];
            if (s2 == 0) {
                return obj.toString();
            }
            if (s2 == 1) {
                return XMLChar.trim(obj.toString());
            }
        }
        if (!(obj instanceof StringBuffer)) {
            return normalize(obj.toString(), s);
        }
        StringBuffer stringBuffer = (StringBuffer) obj;
        int length = stringBuffer.length();
        if (length == 0) {
            return "";
        }
        if (s == 0) {
            return stringBuffer.toString();
        }
        if (s == 1) {
            for (int i3 = 0; i3 < length; i3++) {
                char charAt2 = stringBuffer.charAt(i3);
                if (charAt2 == '\t' || charAt2 == '\n' || charAt2 == '\r') {
                    stringBuffer.setCharAt(i3, ' ');
                }
            }
        } else {
            boolean z = true;
            int i4 = 0;
            int i5 = 0;
            while (i4 < length) {
                char charAt3 = stringBuffer.charAt(i4);
                if (charAt3 == '\t' || charAt3 == '\n' || charAt3 == '\r' || charAt3 == ' ') {
                    while (true) {
                        i = length - 1;
                        if (i4 >= i || !((charAt = stringBuffer.charAt((i2 = i4 + 1))) == '\t' || charAt == '\n' || charAt == '\r' || charAt == ' ')) {
                            break;
                        }
                        i4 = i2;
                    }
                    if (i4 < i && !z) {
                        stringBuffer.setCharAt(i5, ' ');
                        i5++;
                    }
                } else {
                    stringBuffer.setCharAt(i5, charAt3);
                    i5++;
                    z = false;
                }
                i4++;
            }
            stringBuffer.setLength(i5);
        }
        return stringBuffer.toString();
    }

    /* access modifiers changed from: package-private */
    public void reportError(String str, Object[] objArr) throws InvalidDatatypeFacetException {
        throw new InvalidDatatypeFacetException(str, objArr);
    }

    private String whiteSpaceValue(short s) {
        return WS_FACET_STRING[s];
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public short getOrdered() {
        return this.fOrdered;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public boolean getBounded() {
        return this.fBounded;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public boolean getFinite() {
        return this.fFinite;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public boolean getNumeric() {
        return this.fNumeric;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public boolean isDefinedFacet(short s) {
        short s2 = this.fValidationDV;
        if (s2 == 0 || s2 == 29) {
            return false;
        }
        if ((this.fFacetsDefined & s) != 0) {
            return true;
        }
        if (this.fPatternType != 0) {
            if (s == 8) {
                return true;
            }
            return false;
        } else if (s2 != 24) {
            return false;
        } else {
            if (s == 8 || s == 1024) {
                return true;
            }
            return false;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public short getDefinedFacets() {
        int i;
        short s = this.fValidationDV;
        if (s == 0 || s == 29) {
            return 0;
        }
        if (this.fPatternType != 0) {
            i = this.fFacetsDefined | 8;
        } else if (s != 24) {
            return this.fFacetsDefined;
        } else {
            i = this.fFacetsDefined | 8 | 1024;
        }
        return (short) i;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public boolean isFixedFacet(short s) {
        if ((this.fFixedFacet & s) != 0) {
            return true;
        }
        if (this.fValidationDV != 24) {
            return false;
        }
        if (s == 1024) {
            return true;
        }
        return false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public short getFixedFacets() {
        if (this.fValidationDV == 24) {
            return (short) (this.fFixedFacet | 1024);
        }
        return this.fFixedFacet;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public String getLexicalFacetValue(short s) {
        if (s == 1) {
            int i = this.fLength;
            if (i == -1) {
                return null;
            }
            return Integer.toString(i);
        } else if (s == 2) {
            int i2 = this.fMinLength;
            if (i2 == -1) {
                return null;
            }
            return Integer.toString(i2);
        } else if (s == 4) {
            int i3 = this.fMaxLength;
            if (i3 == -1) {
                return null;
            }
            return Integer.toString(i3);
        } else if (s == 16) {
            short s2 = this.fValidationDV;
            if (s2 == 0 || s2 == 29) {
                return null;
            }
            return WS_FACET_STRING[this.fWhiteSpace];
        } else if (s == 32) {
            Object obj = this.fMaxInclusive;
            if (obj == null) {
                return null;
            }
            return obj.toString();
        } else if (s == 64) {
            Object obj2 = this.fMaxExclusive;
            if (obj2 == null) {
                return null;
            }
            return obj2.toString();
        } else if (s == 128) {
            Object obj3 = this.fMinExclusive;
            if (obj3 == null) {
                return null;
            }
            return obj3.toString();
        } else if (s == 256) {
            Object obj4 = this.fMinInclusive;
            if (obj4 == null) {
                return null;
            }
            return obj4.toString();
        } else if (s == 512) {
            int i4 = this.fTotalDigits;
            if (i4 == -1) {
                return null;
            }
            return Integer.toString(i4);
        } else if (s != 1024) {
            return null;
        } else {
            if (this.fValidationDV == 24) {
                return "0";
            }
            int i5 = this.fFractionDigits;
            if (i5 == -1) {
                return null;
            }
            return Integer.toString(i5);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public StringList getLexicalEnumeration() {
        if (this.fLexicalEnumeration == null) {
            Vector vector = this.fEnumeration;
            if (vector == null) {
                return StringListImpl.EMPTY_LIST;
            }
            int size = vector.size();
            String[] strArr = new String[size];
            for (int i = 0; i < size; i++) {
                strArr[i] = this.fEnumeration.elementAt(i).toString();
            }
            this.fLexicalEnumeration = new StringListImpl(strArr, size);
        }
        return this.fLexicalEnumeration;
    }

    public ObjectList getActualEnumeration() {
        if (this.fActualEnumeration == null) {
            this.fActualEnumeration = new AbstractObjectList() {
                /* class ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDecl.AnonymousClass2 */

                @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList
                public int getLength() {
                    if (XSSimpleTypeDecl.this.fEnumeration != null) {
                        return XSSimpleTypeDecl.this.fEnumeration.size();
                    }
                    return 0;
                }

                @Override // java.util.AbstractCollection, java.util.List, java.util.Collection, ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList
                public boolean contains(Object obj) {
                    return XSSimpleTypeDecl.this.fEnumeration != null && XSSimpleTypeDecl.this.fEnumeration.contains(obj);
                }

                @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList
                public Object item(int i) {
                    if (i < 0 || i >= getLength()) {
                        return null;
                    }
                    return XSSimpleTypeDecl.this.fEnumeration.elementAt(i);
                }
            };
        }
        return this.fActualEnumeration;
    }

    public ObjectList getEnumerationItemTypeList() {
        if (this.fEnumerationItemTypeList == null) {
            if (this.fEnumerationItemType == null) {
                return null;
            }
            this.fEnumerationItemTypeList = new AbstractObjectList() {
                /* class ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDecl.AnonymousClass3 */

                @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList
                public int getLength() {
                    if (XSSimpleTypeDecl.this.fEnumerationItemType != null) {
                        return XSSimpleTypeDecl.this.fEnumerationItemType.length;
                    }
                    return 0;
                }

                @Override // java.util.AbstractCollection, java.util.List, java.util.Collection, ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList
                public boolean contains(Object obj) {
                    if (XSSimpleTypeDecl.this.fEnumerationItemType != null && (obj instanceof ShortList)) {
                        for (int i = 0; i < XSSimpleTypeDecl.this.fEnumerationItemType.length; i++) {
                            if (XSSimpleTypeDecl.this.fEnumerationItemType[i] == obj) {
                                return true;
                            }
                        }
                    }
                    return false;
                }

                @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList
                public Object item(int i) {
                    if (i < 0 || i >= getLength()) {
                        return null;
                    }
                    return XSSimpleTypeDecl.this.fEnumerationItemType[i];
                }
            };
        }
        return this.fEnumerationItemTypeList;
    }

    public ShortList getEnumerationTypeList() {
        if (this.fEnumerationTypeList == null) {
            short[] sArr = this.fEnumerationType;
            if (sArr == null) {
                return ShortListImpl.EMPTY_LIST;
            }
            this.fEnumerationTypeList = new ShortListImpl(sArr, sArr.length);
        }
        return this.fEnumerationTypeList;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public StringList getLexicalPattern() {
        String[] strArr;
        if (this.fPatternType == 0 && this.fValidationDV != 24 && this.fPatternStr == null) {
            return StringListImpl.EMPTY_LIST;
        }
        if (this.fLexicalPattern == null) {
            Vector vector = this.fPatternStr;
            int size = vector == null ? 0 : vector.size();
            short s = this.fPatternType;
            if (s == 1) {
                strArr = new String[(size + 1)];
                strArr[size] = "\\c+";
            } else if (s == 2) {
                strArr = new String[(size + 1)];
                strArr[size] = "\\i\\c*";
            } else if (s == 3) {
                strArr = new String[(size + 2)];
                strArr[size] = "\\i\\c*";
                strArr[size + 1] = "[\\i-[:]][\\c-[:]]*";
            } else if (this.fValidationDV == 24) {
                strArr = new String[(size + 1)];
                strArr[size] = "[\\-+]?[0-9]+";
            } else {
                strArr = new String[size];
            }
            for (int i = 0; i < size; i++) {
                strArr[i] = (String) this.fPatternStr.elementAt(i);
            }
            this.fLexicalPattern = new StringListImpl(strArr, strArr.length);
        }
        return this.fLexicalPattern;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public XSObjectList getAnnotations() {
        XSObjectList xSObjectList = this.fAnnotations;
        return xSObjectList != null ? xSObjectList : XSObjectListImpl.EMPTY_LIST;
    }

    private void calcFundamentalFacets() {
        setOrdered();
        setNumeric();
        setBounded();
        setCardinality();
    }

    private void setOrdered() {
        short s = this.fVariety;
        if (s == 1) {
            this.fOrdered = this.fBase.fOrdered;
        } else if (s == 2) {
            this.fOrdered = 0;
        } else if (s == 3) {
            XSSimpleTypeDecl[] xSSimpleTypeDeclArr = this.fMemberTypes;
            if (xSSimpleTypeDeclArr.length == 0) {
                this.fOrdered = 1;
                return;
            }
            short primitiveDV = getPrimitiveDV(xSSimpleTypeDeclArr[0].fValidationDV);
            boolean z = primitiveDV != 0;
            boolean z2 = this.fMemberTypes[0].fOrdered == 0;
            boolean z3 = z;
            for (int i = 1; i < this.fMemberTypes.length && (z3 || z2); i++) {
                if (z3) {
                    z3 = primitiveDV == getPrimitiveDV(this.fMemberTypes[i].fValidationDV);
                }
                if (z2) {
                    z2 = this.fMemberTypes[i].fOrdered == 0;
                }
            }
            if (z3) {
                this.fOrdered = this.fMemberTypes[0].fOrdered;
            } else if (z2) {
                this.fOrdered = 0;
            } else {
                this.fOrdered = 1;
            }
        }
    }

    private void setNumeric() {
        XSSimpleTypeDecl[] xSSimpleTypeDeclArr;
        short s = this.fVariety;
        if (s == 1) {
            this.fNumeric = this.fBase.fNumeric;
        } else if (s == 2) {
            this.fNumeric = false;
        } else if (s == 3) {
            for (XSSimpleTypeDecl xSSimpleTypeDecl : this.fMemberTypes) {
                if (!xSSimpleTypeDecl.getNumeric()) {
                    this.fNumeric = false;
                    return;
                }
            }
            this.fNumeric = true;
        }
    }

    private void setBounded() {
        short s = this.fVariety;
        if (s == 1) {
            short s2 = this.fFacetsDefined;
            if (!((s2 & 256) == 0 && (s2 & 128) == 0)) {
                short s3 = this.fFacetsDefined;
                if (!((s3 & 32) == 0 && (s3 & 64) == 0)) {
                    this.fBounded = true;
                    return;
                }
            }
            this.fBounded = false;
        } else if (s == 2) {
            short s4 = this.fFacetsDefined;
            if ((s4 & 1) == 0 && ((s4 & 2) == 0 || (s4 & 4) == 0)) {
                this.fBounded = false;
            } else {
                this.fBounded = true;
            }
        } else if (s == 3) {
            XSSimpleTypeDecl[] xSSimpleTypeDeclArr = this.fMemberTypes;
            short primitiveDV = xSSimpleTypeDeclArr.length > 0 ? getPrimitiveDV(xSSimpleTypeDeclArr[0].fValidationDV) : 0;
            for (int i = 0; i < xSSimpleTypeDeclArr.length; i++) {
                if (!xSSimpleTypeDeclArr[i].getBounded() || primitiveDV != getPrimitiveDV(xSSimpleTypeDeclArr[i].fValidationDV)) {
                    this.fBounded = false;
                    return;
                }
            }
            this.fBounded = true;
        }
    }

    private boolean specialCardinalityCheck() {
        short s = this.fBase.fValidationDV;
        return s == 9 || s == 10 || s == 11 || s == 12 || s == 13 || s == 14;
    }

    private void setCardinality() {
        XSSimpleTypeDecl[] xSSimpleTypeDeclArr;
        short s = this.fVariety;
        if (s == 1) {
            if (this.fBase.fFinite) {
                this.fFinite = true;
                return;
            }
            short s2 = this.fFacetsDefined;
            if ((s2 & 1) == 0 && (s2 & 4) == 0 && (s2 & 512) == 0) {
                if (!((s2 & 256) == 0 && (s2 & 128) == 0)) {
                    short s3 = this.fFacetsDefined;
                    if (!((s3 & 32) == 0 && (s3 & 64) == 0)) {
                        if ((this.fFacetsDefined & 1024) != 0 || specialCardinalityCheck()) {
                            this.fFinite = true;
                            return;
                        } else {
                            this.fFinite = false;
                            return;
                        }
                    }
                }
                this.fFinite = false;
                return;
            }
            this.fFinite = true;
        } else if (s == 2) {
            short s4 = this.fFacetsDefined;
            if ((s4 & 1) == 0 && ((s4 & 2) == 0 || (s4 & 4) == 0)) {
                this.fFinite = false;
            } else {
                this.fFinite = true;
            }
        } else if (s == 3) {
            for (XSSimpleTypeDecl xSSimpleTypeDecl : this.fMemberTypes) {
                if (!xSSimpleTypeDecl.getFinite()) {
                    this.fFinite = false;
                    return;
                }
            }
            this.fFinite = true;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v1, types: [ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition] */
    /* JADX WARN: Type inference failed for: r2v3 */
    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public boolean derivedFromType(XSTypeDefinition xSTypeDefinition, short s) {
        if (xSTypeDefinition == null) {
            return false;
        }
        while (xSTypeDefinition instanceof XSSimpleTypeDelegate) {
            xSTypeDefinition = ((XSSimpleTypeDelegate) xSTypeDefinition).type;
        }
        ?? r2 = this;
        if (xSTypeDefinition.getBaseType() == xSTypeDefinition) {
            return true;
        }
        while (r2 != xSTypeDefinition && r2 != fAnySimpleType) {
            r2 = r2.getBaseType();
        }
        if (r2 == xSTypeDefinition) {
            return true;
        }
        return false;
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:25:0x0016 */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v0, types: [ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDecl] */
    /* JADX WARN: Type inference failed for: r2v1, types: [ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition] */
    /* JADX WARN: Type inference failed for: r2v2, types: [ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition] */
    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition
    public boolean derivedFrom(String str, String str2, short s) {
        if (str2 == null) {
            return false;
        }
        if ("http://www.w3.org/2001/XMLSchema".equals(str) && "anyType".equals(str2)) {
            return true;
        }
        while (true) {
            if ((!str2.equals(this.getName()) || (!(str == null && this.getNamespace() == null) && (str == null || !str.equals(this.getNamespace())))) && this != fAnySimpleType) {
                this = this.getBaseType();
            }
        }
        return this != fAnySimpleType;
    }

    public boolean isDOMDerivedFrom(String str, String str2, int i) {
        if (str2 == null) {
            return false;
        }
        if (SchemaSymbols.URI_SCHEMAFORSCHEMA.equals(str) && "anyType".equals(str2) && ((i & 1) != 0 || i == 0)) {
            return true;
        }
        int i2 = i & 1;
        if (i2 != 0 && isDerivedByRestriction(str, str2, this)) {
            return true;
        }
        int i3 = i & 8;
        if (i3 != 0 && isDerivedByList(str, str2, this)) {
            return true;
        }
        int i4 = i & 4;
        if (i4 != 0 && isDerivedByUnion(str, str2, this)) {
            return true;
        }
        int i5 = i & 2;
        if ((i5 == 0 || i2 != 0 || i3 != 0 || i4 != 0) && i5 == 0 && i2 == 0 && i3 == 0 && i4 == 0) {
            return isDerivedByAny(str, str2, this);
        }
        return false;
    }

    private boolean isDerivedByAny(String str, String str2, XSTypeDefinition xSTypeDefinition) {
        XSTypeDefinition xSTypeDefinition2;
        XSTypeDefinition xSTypeDefinition3 = null;
        while (xSTypeDefinition != null && xSTypeDefinition != xSTypeDefinition3) {
            if (str2.equals(xSTypeDefinition.getName()) && ((str == null && xSTypeDefinition.getNamespace() == null) || (str != null && str.equals(xSTypeDefinition.getNamespace())))) {
                return true;
            }
            if (isDerivedByRestriction(str, str2, xSTypeDefinition) || isDerivedByList(str, str2, xSTypeDefinition) || isDerivedByUnion(str, str2, xSTypeDefinition)) {
                return true;
            }
            XSSimpleTypeDecl xSSimpleTypeDecl = (XSSimpleTypeDecl) xSTypeDefinition;
            if (xSSimpleTypeDecl.getVariety() == 0 || xSSimpleTypeDecl.getVariety() == 1) {
                xSTypeDefinition2 = xSTypeDefinition.getBaseType();
            } else {
                if (xSSimpleTypeDecl.getVariety() == 3) {
                    if (xSSimpleTypeDecl.getMemberTypes().getLength() > 0) {
                        return isDerivedByAny(str, str2, (XSTypeDefinition) xSSimpleTypeDecl.getMemberTypes().item(0));
                    }
                } else if (xSSimpleTypeDecl.getVariety() == 2) {
                    xSTypeDefinition2 = xSSimpleTypeDecl.getItemType();
                }
                xSTypeDefinition3 = xSTypeDefinition;
            }
            xSTypeDefinition3 = xSTypeDefinition;
            xSTypeDefinition = xSTypeDefinition2;
        }
        return false;
    }

    private boolean isDerivedByRestriction(String str, String str2, XSTypeDefinition xSTypeDefinition) {
        XSTypeDefinition xSTypeDefinition2 = null;
        while (true) {
            xSTypeDefinition2 = xSTypeDefinition;
            if (xSTypeDefinition2 == null || xSTypeDefinition2 == xSTypeDefinition2) {
                return false;
            }
            if (str2.equals(xSTypeDefinition2.getName())) {
                if (str != null && str.equals(xSTypeDefinition2.getNamespace())) {
                    return true;
                }
                if (xSTypeDefinition2.getNamespace() == null && str == null) {
                    return true;
                }
            }
            xSTypeDefinition = xSTypeDefinition2.getBaseType();
        }
    }

    private boolean isDerivedByList(String str, String str2, XSTypeDefinition xSTypeDefinition) {
        XSSimpleTypeDefinition itemType;
        if (xSTypeDefinition == null) {
            return false;
        }
        XSSimpleTypeDefinition xSSimpleTypeDefinition = (XSSimpleTypeDefinition) xSTypeDefinition;
        return xSSimpleTypeDefinition.getVariety() == 2 && (itemType = xSSimpleTypeDefinition.getItemType()) != null && isDerivedByRestriction(str, str2, itemType);
    }

    private boolean isDerivedByUnion(String str, String str2, XSTypeDefinition xSTypeDefinition) {
        if (xSTypeDefinition != null) {
            XSSimpleTypeDefinition xSSimpleTypeDefinition = (XSSimpleTypeDefinition) xSTypeDefinition;
            if (xSSimpleTypeDefinition.getVariety() == 3) {
                XSObjectList memberTypes = xSSimpleTypeDefinition.getMemberTypes();
                for (int i = 0; i < memberTypes.getLength(); i++) {
                    if (memberTypes.item(i) != null && isDerivedByRestriction(str, str2, (XSSimpleTypeDefinition) memberTypes.item(i))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public static final class ValidationContextImpl implements ValidationContext {
        final ValidationContext fExternal;
        NamespaceContext fNSContext;

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean useNamespaces() {
            return true;
        }

        ValidationContextImpl(ValidationContext validationContext) {
            this.fExternal = validationContext;
        }

        /* access modifiers changed from: package-private */
        public void setNSContext(NamespaceContext namespaceContext) {
            this.fNSContext = namespaceContext;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean needFacetChecking() {
            return this.fExternal.needFacetChecking();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean needExtraChecking() {
            return this.fExternal.needExtraChecking();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean needToNormalize() {
            return this.fExternal.needToNormalize();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean isEntityDeclared(String str) {
            return this.fExternal.isEntityDeclared(str);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean isEntityUnparsed(String str) {
            return this.fExternal.isEntityUnparsed(str);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public boolean isIdDeclared(String str) {
            return this.fExternal.isIdDeclared(str);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public void addId(String str) {
            this.fExternal.addId(str);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public void addIdRef(String str) {
            this.fExternal.addIdRef(str);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public String getSymbol(String str) {
            return this.fExternal.getSymbol(str);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public String getURI(String str) {
            NamespaceContext namespaceContext = this.fNSContext;
            if (namespaceContext == null) {
                return this.fExternal.getURI(str);
            }
            return namespaceContext.getURI(str);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
        public Locale getLocale() {
            return this.fExternal.getLocale();
        }
    }

    public void reset() {
        if (!this.fIsImmutable) {
            this.fItemType = null;
            this.fMemberTypes = null;
            this.fTypeName = null;
            this.fTargetNamespace = null;
            this.fFinalSet = 0;
            this.fBase = null;
            this.fVariety = -1;
            this.fValidationDV = -1;
            this.fFacetsDefined = 0;
            this.fFixedFacet = 0;
            this.fWhiteSpace = 0;
            this.fLength = -1;
            this.fMinLength = -1;
            this.fMaxLength = -1;
            this.fTotalDigits = -1;
            this.fFractionDigits = -1;
            this.fPattern = null;
            this.fPatternStr = null;
            this.fEnumeration = null;
            this.fEnumerationType = null;
            this.fEnumerationItemType = null;
            this.fLexicalPattern = null;
            this.fLexicalEnumeration = null;
            this.fMaxInclusive = null;
            this.fMaxExclusive = null;
            this.fMinExclusive = null;
            this.fMinInclusive = null;
            this.lengthAnnotation = null;
            this.minLengthAnnotation = null;
            this.maxLengthAnnotation = null;
            this.whiteSpaceAnnotation = null;
            this.totalDigitsAnnotation = null;
            this.fractionDigitsAnnotation = null;
            this.patternAnnotations = null;
            this.enumerationAnnotations = null;
            this.maxInclusiveAnnotation = null;
            this.maxExclusiveAnnotation = null;
            this.minInclusiveAnnotation = null;
            this.minExclusiveAnnotation = null;
            this.fPatternType = 0;
            this.fAnnotations = null;
            this.fFacets = null;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
    public XSNamespaceItem getNamespaceItem() {
        return this.fNamespaceItem;
    }

    public void setNamespaceItem(XSNamespaceItem xSNamespaceItem) {
        this.fNamespaceItem = xSNamespaceItem;
    }

    public String toString() {
        return this.fTargetNamespace + "," + this.fTypeName;
    }

    /* JADX WARNING: Removed duplicated region for block: B:59:0x00e0  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x00fd  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x011a  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0137  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0150  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x0156  */
    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public XSObjectList getFacets() {
        int i;
        Object obj;
        Object obj2;
        Object obj3;
        Object obj4;
        short s;
        if (this.fFacets == null && (this.fFacetsDefined != 0 || this.fValidationDV == 24)) {
            XSFacetImpl[] xSFacetImplArr = new XSFacetImpl[10];
            boolean z = false;
            if ((this.fFacetsDefined & 16) == 0 || (s = this.fValidationDV) == 0 || s == 29) {
                i = 0;
            } else {
                xSFacetImplArr[0] = new XSFacetImpl(16, WS_FACET_STRING[this.fWhiteSpace], (this.fFixedFacet & 16) != 0, this.whiteSpaceAnnotation);
                i = 1;
            }
            int i2 = this.fLength;
            if (i2 != -1) {
                xSFacetImplArr[i] = new XSFacetImpl(1, Integer.toString(i2), (this.fFixedFacet & 1) != 0, this.lengthAnnotation);
                i++;
            }
            int i3 = this.fMinLength;
            if (i3 != -1) {
                xSFacetImplArr[i] = new XSFacetImpl(2, Integer.toString(i3), (this.fFixedFacet & 2) != 0, this.minLengthAnnotation);
                i++;
            }
            int i4 = this.fMaxLength;
            if (i4 != -1) {
                xSFacetImplArr[i] = new XSFacetImpl(4, Integer.toString(i4), (this.fFixedFacet & 4) != 0, this.maxLengthAnnotation);
                i++;
            }
            int i5 = this.fTotalDigits;
            if (i5 != -1) {
                xSFacetImplArr[i] = new XSFacetImpl(512, Integer.toString(i5), (this.fFixedFacet & 512) != 0, this.totalDigitsAnnotation);
                i++;
            }
            if (this.fValidationDV == 24) {
                xSFacetImplArr[i] = new XSFacetImpl(1024, "0", true, this.fractionDigitsAnnotation);
            } else {
                int i6 = this.fFractionDigits;
                if (i6 != -1) {
                    xSFacetImplArr[i] = new XSFacetImpl(1024, Integer.toString(i6), (this.fFixedFacet & 1024) != 0, this.fractionDigitsAnnotation);
                }
                obj = this.fMaxInclusive;
                if (obj != null) {
                    xSFacetImplArr[i] = new XSFacetImpl(32, obj.toString(), (this.fFixedFacet & 32) != 0, this.maxInclusiveAnnotation);
                    i++;
                }
                obj2 = this.fMaxExclusive;
                if (obj2 != null) {
                    xSFacetImplArr[i] = new XSFacetImpl(64, obj2.toString(), (this.fFixedFacet & 64) != 0, this.maxExclusiveAnnotation);
                    i++;
                }
                obj3 = this.fMinExclusive;
                if (obj3 != null) {
                    xSFacetImplArr[i] = new XSFacetImpl(128, obj3.toString(), (this.fFixedFacet & 128) != 0, this.minExclusiveAnnotation);
                    i++;
                }
                obj4 = this.fMinInclusive;
                if (obj4 != null) {
                    String obj5 = obj4.toString();
                    if ((this.fFixedFacet & 256) != 0) {
                        z = true;
                    }
                    xSFacetImplArr[i] = new XSFacetImpl(256, obj5, z, this.minInclusiveAnnotation);
                    i++;
                }
                this.fFacets = i <= 0 ? new XSObjectListImpl(xSFacetImplArr, i) : XSObjectListImpl.EMPTY_LIST;
            }
            i++;
            obj = this.fMaxInclusive;
            if (obj != null) {
            }
            obj2 = this.fMaxExclusive;
            if (obj2 != null) {
            }
            obj3 = this.fMinExclusive;
            if (obj3 != null) {
            }
            obj4 = this.fMinInclusive;
            if (obj4 != null) {
            }
            this.fFacets = i <= 0 ? new XSObjectListImpl(xSFacetImplArr, i) : XSObjectListImpl.EMPTY_LIST;
        }
        XSObjectListImpl xSObjectListImpl = this.fFacets;
        return xSObjectListImpl != null ? xSObjectListImpl : XSObjectListImpl.EMPTY_LIST;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
    public XSObjectList getMultiValueFacets() {
        if (this.fMultiValueFacets == null) {
            short s = this.fFacetsDefined;
            if (!((s & 2048) == 0 && (s & 8) == 0 && this.fPatternType == 0 && this.fValidationDV != 24)) {
                XSMVFacetImpl[] xSMVFacetImplArr = new XSMVFacetImpl[2];
                int i = 0;
                if (!((this.fFacetsDefined & 8) == 0 && this.fPatternType == 0 && this.fValidationDV != 24)) {
                    xSMVFacetImplArr[0] = new XSMVFacetImpl(8, getLexicalPattern(), this.patternAnnotations);
                    i = 1;
                }
                if (this.fEnumeration != null) {
                    xSMVFacetImplArr[i] = new XSMVFacetImpl(2048, getLexicalEnumeration(), this.enumerationAnnotations);
                    i++;
                }
                this.fMultiValueFacets = new XSObjectListImpl(xSMVFacetImplArr, i);
            }
        }
        XSObjectListImpl xSObjectListImpl = this.fMultiValueFacets;
        return xSObjectListImpl != null ? xSObjectListImpl : XSObjectListImpl.EMPTY_LIST;
    }

    public Object getMinInclusiveValue() {
        return this.fMinInclusive;
    }

    public Object getMinExclusiveValue() {
        return this.fMinExclusive;
    }

    public Object getMaxInclusiveValue() {
        return this.fMaxInclusive;
    }

    public Object getMaxExclusiveValue() {
        return this.fMaxExclusive;
    }

    public void setAnonymous(boolean z) {
        this.fAnonymous = z;
    }

    private static final class XSFacetImpl implements XSFacet {
        final XSObjectList annotations;
        final boolean fixed;
        final short kind;
        final String value;

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
        public String getName() {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
        public String getNamespace() {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
        public XSNamespaceItem getNamespaceItem() {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
        public short getType() {
            return 13;
        }

        public XSFacetImpl(short s, String str, boolean z, XSAnnotation xSAnnotation) {
            this.kind = s;
            this.value = str;
            this.fixed = z;
            if (xSAnnotation != null) {
                this.annotations = new XSObjectListImpl();
                ((XSObjectListImpl) this.annotations).addXSObject(xSAnnotation);
                return;
            }
            this.annotations = XSObjectListImpl.EMPTY_LIST;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSFacet
        public XSAnnotation getAnnotation() {
            return (XSAnnotation) this.annotations.item(0);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSFacet
        public XSObjectList getAnnotations() {
            return this.annotations;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSFacet
        public short getFacetKind() {
            return this.kind;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSFacet
        public String getLexicalFacetValue() {
            return this.value;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSFacet
        public boolean getFixed() {
            return this.fixed;
        }
    }

    private static final class XSMVFacetImpl implements XSMultiValueFacet {
        final XSObjectList annotations;
        final short kind;
        final StringList values;

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
        public String getName() {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
        public String getNamespace() {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
        public XSNamespaceItem getNamespaceItem() {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSObject
        public short getType() {
            return 14;
        }

        public XSMVFacetImpl(short s, StringList stringList, XSObjectList xSObjectList) {
            this.kind = s;
            this.values = stringList;
            this.annotations = xSObjectList == null ? XSObjectListImpl.EMPTY_LIST : xSObjectList;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSMultiValueFacet
        public short getFacetKind() {
            return this.kind;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSMultiValueFacet
        public XSObjectList getAnnotations() {
            return this.annotations;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSMultiValueFacet
        public StringList getLexicalFacetValues() {
            return this.values;
        }
    }

    private static abstract class AbstractObjectList extends AbstractList implements ObjectList {
        private AbstractObjectList() {
        }

        @Override // java.util.AbstractList, java.util.List
        public Object get(int i) {
            if (i >= 0 && i < getLength()) {
                return item(i);
            }
            throw new IndexOutOfBoundsException("Index: " + i);
        }

        @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
        public int size() {
            return getLength();
        }
    }

    public String getTypeNamespace() {
        return getNamespace();
    }

    public boolean isDerivedFrom(String str, String str2, int i) {
        return isDOMDerivedFrom(str, str2, i);
    }
}
