package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class TableLayoutAttrsConstants extends ViewAttrsConstants {
    public static final String ALIGNMENT_TYPE = "alignment_type";
    public static final String COLUMN_COUNT = "column_count";
    public static final String ORIENTATION = "orientation";
    public static final String ROW_COUNT = "row_count";
    public static final String USE_DEFAULT_MARGINS = "use_default_margins";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -1439500848:
                if (str.equals("orientation")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -552969194:
                if (str.equals(ALIGNMENT_TYPE)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -122671386:
                if (str.equals(COLUMN_COUNT)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 106891727:
                if (str.equals(USE_DEFAULT_MARGINS)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 1340416618:
                if (str.equals(ROW_COUNT)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0 || c == 1 || c == 2 || c == 3) {
            return Attr.AttrType.INT;
        }
        if (c != 4) {
            return super.getType(str);
        }
        return Attr.AttrType.BOOLEAN;
    }
}
