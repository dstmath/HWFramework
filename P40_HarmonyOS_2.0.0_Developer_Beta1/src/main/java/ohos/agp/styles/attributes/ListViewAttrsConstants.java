package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class ListViewAttrsConstants extends ViewAttrsConstants {
    public static final String ORIENTATION = "orientation";
    public static final String REBOUND_EFFECT = "rebound_effect";

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0029  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0033  */
    @Override // ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        int hashCode = str.hashCode();
        if (hashCode != -1439500848) {
            if (hashCode == 1915014437 && str.equals("rebound_effect")) {
                c = 1;
                if (c == 0) {
                    return Attr.AttrType.INT;
                }
                if (c != 1) {
                    return super.getType(str);
                }
                return Attr.AttrType.BOOLEAN;
            }
        } else if (str.equals("orientation")) {
            c = 0;
            if (c == 0) {
            }
        }
        c = 65535;
        if (c == 0) {
        }
    }
}
