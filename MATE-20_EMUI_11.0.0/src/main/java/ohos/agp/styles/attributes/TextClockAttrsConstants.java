package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class TextClockAttrsConstants extends TextViewAttrsConstants {
    public static final String FORMAT_12_HOUR = "format_12_hour";
    public static final String FORMAT_24_HOUR = "format_24_hour";
    public static final String TIME = "time";
    public static final String TIME_ZONE = "time_zone";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.TextViewAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case 3560141:
                if (str.equals("time")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 31395066:
                if (str.equals(FORMAT_12_HOUR)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 36848094:
                if (str.equals(TIME_ZONE)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 976157049:
                if (str.equals(FORMAT_24_HOUR)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0 || c == 1 || c == 2) {
            return Attr.AttrType.STRING;
        }
        if (c != 3) {
            return super.getType(str);
        }
        return Attr.AttrType.LONG;
    }
}
