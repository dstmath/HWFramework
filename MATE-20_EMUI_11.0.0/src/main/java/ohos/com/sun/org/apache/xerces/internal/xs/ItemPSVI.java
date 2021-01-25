package ohos.com.sun.org.apache.xerces.internal.xs;

public interface ItemPSVI {
    public static final short VALIDATION_FULL = 2;
    public static final short VALIDATION_NONE = 0;
    public static final short VALIDATION_PARTIAL = 1;
    public static final short VALIDITY_INVALID = 1;
    public static final short VALIDITY_NOTKNOWN = 0;
    public static final short VALIDITY_VALID = 2;

    Object getActualNormalizedValue() throws XSException;

    short getActualNormalizedValueType() throws XSException;

    StringList getErrorCodes();

    boolean getIsSchemaSpecified();

    ShortList getItemValueTypes() throws XSException;

    XSSimpleTypeDefinition getMemberTypeDefinition();

    String getSchemaDefault();

    String getSchemaNormalizedValue();

    XSTypeDefinition getTypeDefinition();

    short getValidationAttempted();

    String getValidationContext();

    short getValidity();
}
