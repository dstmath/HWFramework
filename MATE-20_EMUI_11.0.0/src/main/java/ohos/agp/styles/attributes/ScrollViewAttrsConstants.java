package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class ScrollViewAttrsConstants extends StackLayoutAttrsConstants {
    public static final String FILL_VIEWPORT = "fill_viewport";
    public static final String REBOUND_EFFECT = "rebound_effect";

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0028  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0032  */
    @Override // ohos.agp.styles.attributes.StackLayoutAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        int hashCode = str.hashCode();
        if (hashCode != 1547810786) {
            if (hashCode == 1915014437 && str.equals("rebound_effect")) {
                c = 1;
                if (c == 0) {
                    return Attr.AttrType.BOOLEAN;
                }
                if (c != 1) {
                    return super.getType(str);
                }
                return Attr.AttrType.BOOLEAN;
            }
        } else if (str.equals(FILL_VIEWPORT)) {
            c = 0;
            if (c == 0) {
            }
        }
        c = 65535;
        if (c == 0) {
        }
    }
}
