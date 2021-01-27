package ohos.com.sun.org.apache.xerces.internal.xs;

public interface XSSimpleTypeDefinition extends XSTypeDefinition {
    public static final short FACET_ENUMERATION = 2048;
    public static final short FACET_FRACTIONDIGITS = 1024;
    public static final short FACET_LENGTH = 1;
    public static final short FACET_MAXEXCLUSIVE = 64;
    public static final short FACET_MAXINCLUSIVE = 32;
    public static final short FACET_MAXLENGTH = 4;
    public static final short FACET_MINEXCLUSIVE = 128;
    public static final short FACET_MININCLUSIVE = 256;
    public static final short FACET_MINLENGTH = 2;
    public static final short FACET_NONE = 0;
    public static final short FACET_PATTERN = 8;
    public static final short FACET_TOTALDIGITS = 512;
    public static final short FACET_WHITESPACE = 16;
    public static final short ORDERED_FALSE = 0;
    public static final short ORDERED_PARTIAL = 1;
    public static final short ORDERED_TOTAL = 2;
    public static final short VARIETY_ABSENT = 0;
    public static final short VARIETY_ATOMIC = 1;
    public static final short VARIETY_LIST = 2;
    public static final short VARIETY_UNION = 3;

    XSObjectList getAnnotations();

    boolean getBounded();

    short getBuiltInKind();

    short getDefinedFacets();

    XSObjectList getFacets();

    boolean getFinite();

    short getFixedFacets();

    XSSimpleTypeDefinition getItemType();

    StringList getLexicalEnumeration();

    String getLexicalFacetValue(short s);

    StringList getLexicalPattern();

    XSObjectList getMemberTypes();

    XSObjectList getMultiValueFacets();

    boolean getNumeric();

    short getOrdered();

    XSSimpleTypeDefinition getPrimitiveType();

    short getVariety();

    boolean isDefinedFacet(short s);

    boolean isFixedFacet(short s);
}
