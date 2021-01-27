package ohos.com.sun.org.apache.xerces.internal.xs;

public interface XSTypeDefinition extends XSObject {
    public static final short COMPLEX_TYPE = 15;
    public static final short SIMPLE_TYPE = 16;

    boolean derivedFrom(String str, String str2, short s);

    boolean derivedFromType(XSTypeDefinition xSTypeDefinition, short s);

    boolean getAnonymous();

    XSTypeDefinition getBaseType();

    short getFinal();

    short getTypeCategory();

    boolean isFinal(short s);
}
