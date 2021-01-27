package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class ChronometerAttrsConstants extends TextAttrsConstants {
    public static final String COUNT_DOWN = "count_down";
    public static final String FORMAT = "format";

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0027  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0031  */
    @Override // ohos.agp.styles.attributes.TextAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        int hashCode = str.hashCode();
        if (hashCode != -1268779017) {
            if (hashCode == -1035608622 && str.equals(COUNT_DOWN)) {
                c = 1;
                if (c == 0) {
                    return Attr.AttrType.STRING;
                }
                if (c != 1) {
                    return super.getType(str);
                }
                return Attr.AttrType.BOOLEAN;
            }
        } else if (str.equals("format")) {
            c = 0;
            if (c == 0) {
            }
        }
        c = 65535;
        if (c == 0) {
        }
    }
}
