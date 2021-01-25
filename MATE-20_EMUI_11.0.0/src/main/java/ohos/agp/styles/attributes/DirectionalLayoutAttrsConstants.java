package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class DirectionalLayoutAttrsConstants extends ViewAttrsConstants {
    public static final String GRAVITY = "gravity";
    public static final String MEASURE_WITH_LARGEST_CHILD = "measure_with_largest_child";
    public static final String ORIENTATION = "orientation";
    public static final String WEIGHT_SUM = "weight_sum";

    public static class LayoutParamsAttrsConstants {
        public static final String LAYOUT_GRAVITY = "layout_alignment";
        public static final String LAYOUT_WEIGHT = "weight";
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -1456983036:
                if (str.equals(WEIGHT_SUM)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1439500848:
                if (str.equals("orientation")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 280523342:
                if (str.equals("gravity")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1979683521:
                if (str.equals(MEASURE_WITH_LARGEST_CHILD)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            return Attr.AttrType.BOOLEAN;
        }
        if (c == 1) {
            return Attr.AttrType.FLOAT;
        }
        if (c == 2 || c == 3) {
            return Attr.AttrType.INT;
        }
        return super.getType(str);
    }
}
