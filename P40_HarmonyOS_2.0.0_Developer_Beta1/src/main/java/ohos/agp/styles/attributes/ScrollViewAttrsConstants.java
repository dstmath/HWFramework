package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class ScrollViewAttrsConstants extends StackLayoutAttrsConstants {
    public static final String MATCH_VIEWPORT = "match_viewport";
    public static final String REBOUND_EFFECT = "rebound_effect";

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0028 A[ADDED_TO_REGION] */
    @Override // ohos.agp.styles.attributes.StackLayoutAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        int hashCode = str.hashCode();
        if (hashCode != 647200224) {
            if (hashCode == 1915014437 && str.equals("rebound_effect")) {
                c = 1;
                if (c != 0 || c == 1) {
                    return Attr.AttrType.BOOLEAN;
                }
                return super.getType(str);
            }
        } else if (str.equals(MATCH_VIEWPORT)) {
            c = 0;
            if (c != 0) {
            }
            return Attr.AttrType.BOOLEAN;
        }
        c = 65535;
        if (c != 0) {
        }
        return Attr.AttrType.BOOLEAN;
    }
}
