package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class PageSliderAttrsConstants extends StackLayoutAttrsConstants {
    public static final String ORIENTATION = "orientation";
    public static final String PAGE_CACHE_SIZE = "page_cache_size";
    public static final String REBOUND_EFFECT = "rebound_effect";
    public static final String SLIDING_ENABLE = "sliding_enable";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.StackLayoutAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -1439500848:
                if (str.equals("orientation")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 661610062:
                if (str.equals(PAGE_CACHE_SIZE)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1849704628:
                if (str.equals(SLIDING_ENABLE)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1915014437:
                if (str.equals("rebound_effect")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0 || c == 1) {
            return Attr.AttrType.INT;
        }
        if (c == 2 || c == 3) {
            return Attr.AttrType.BOOLEAN;
        }
        return super.getType(str);
    }
}
