package ohos.com.sun.xml.internal.stream.dtd.nonvalidating;

public class XMLSimpleType {
    public static final short DEFAULT_TYPE_DEFAULT = 3;
    public static final short DEFAULT_TYPE_FIXED = 1;
    public static final short DEFAULT_TYPE_IMPLIED = 0;
    public static final short DEFAULT_TYPE_REQUIRED = 2;
    public static final short TYPE_CDATA = 0;
    public static final short TYPE_ENTITY = 1;
    public static final short TYPE_ENUMERATION = 2;
    public static final short TYPE_ID = 3;
    public static final short TYPE_IDREF = 4;
    public static final short TYPE_NAMED = 7;
    public static final short TYPE_NMTOKEN = 5;
    public static final short TYPE_NOTATION = 6;
    public short defaultType;
    public String defaultValue;
    public String[] enumeration;
    public boolean list;
    public String name;
    public String nonNormalizedDefaultValue;
    public short type;

    public void setValues(short s, String str, String[] strArr, boolean z, short s2, String str2, String str3) {
        this.type = s;
        this.name = str;
        if (strArr == null || strArr.length <= 0) {
            this.enumeration = null;
        } else {
            this.enumeration = new String[strArr.length];
            String[] strArr2 = this.enumeration;
            System.arraycopy(strArr, 0, strArr2, 0, strArr2.length);
        }
        this.list = z;
        this.defaultType = s2;
        this.defaultValue = str2;
        this.nonNormalizedDefaultValue = str3;
    }

    public void setValues(XMLSimpleType xMLSimpleType) {
        this.type = xMLSimpleType.type;
        this.name = xMLSimpleType.name;
        String[] strArr = xMLSimpleType.enumeration;
        if (strArr == null || strArr.length <= 0) {
            this.enumeration = null;
        } else {
            this.enumeration = new String[strArr.length];
            String[] strArr2 = xMLSimpleType.enumeration;
            String[] strArr3 = this.enumeration;
            System.arraycopy(strArr2, 0, strArr3, 0, strArr3.length);
        }
        this.list = xMLSimpleType.list;
        this.defaultType = xMLSimpleType.defaultType;
        this.defaultValue = xMLSimpleType.defaultValue;
        this.nonNormalizedDefaultValue = xMLSimpleType.nonNormalizedDefaultValue;
    }

    public void clear() {
        this.type = -1;
        this.name = null;
        this.enumeration = null;
        this.list = false;
        this.defaultType = -1;
        this.defaultValue = null;
        this.nonNormalizedDefaultValue = null;
    }
}
