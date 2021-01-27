package android.util;

public interface AttributeSet {
    boolean getAttributeBooleanValue(int i, boolean z);

    boolean getAttributeBooleanValue(String str, String str2, boolean z);

    @Override // android.util.AttributeSet
    int getAttributeCount();

    float getAttributeFloatValue(int i, float f);

    float getAttributeFloatValue(String str, String str2, float f);

    int getAttributeIntValue(int i, int i2);

    int getAttributeIntValue(String str, String str2, int i);

    int getAttributeListValue(int i, String[] strArr, int i2);

    int getAttributeListValue(String str, String str2, String[] strArr, int i);

    @Override // android.util.AttributeSet
    String getAttributeName(int i);

    int getAttributeNameResource(int i);

    int getAttributeResourceValue(int i, int i2);

    int getAttributeResourceValue(String str, String str2, int i);

    int getAttributeUnsignedIntValue(int i, int i2);

    int getAttributeUnsignedIntValue(String str, String str2, int i);

    @Override // android.util.AttributeSet
    String getAttributeValue(int i);

    @Override // android.util.AttributeSet
    String getAttributeValue(String str, String str2);

    String getClassAttribute();

    String getIdAttribute();

    int getIdAttributeResourceValue(int i);

    @Override // android.util.AttributeSet
    String getPositionDescription();

    int getStyleAttribute();

    @Override // android.util.AttributeSet
    default String getAttributeNamespace(int index) {
        return null;
    }
}
