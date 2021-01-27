package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class ClockAttrsConstants extends TextAttrsConstants {
    public static final String MODE_12_HOUR = "mode_12_hour";
    public static final String MODE_24_HOUR = "mode_24_hour";
    public static final String TIME = "time";
    public static final String TIME_ZONE = "time_zone";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.TextAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -925113690:
                if (str.equals(MODE_12_HOUR)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 3560141:
                if (str.equals("time")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 19648293:
                if (str.equals("mode_24_hour")) {
                    c = 1;
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
