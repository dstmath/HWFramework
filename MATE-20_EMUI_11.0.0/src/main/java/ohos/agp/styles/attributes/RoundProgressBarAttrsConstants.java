package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class RoundProgressBarAttrsConstants extends ProgressBarAttrsConstants {
    public static final String MAX_ANGLE = "max_angle";
    public static final String START_ANGLE = "start_angle";

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0028 A[ADDED_TO_REGION] */
    @Override // ohos.agp.styles.attributes.ProgressBarAttrsConstants, ohos.agp.styles.attributes.ScrollViewAttrsConstants, ohos.agp.styles.attributes.StackLayoutAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        int hashCode = str.hashCode();
        if (hashCode != -1540272458) {
            if (hashCode == -251140552 && str.equals(MAX_ANGLE)) {
                c = 1;
                if (c != 0 || c == 1) {
                    return Attr.AttrType.FLOAT;
                }
                return super.getType(str);
            }
        } else if (str.equals(START_ANGLE)) {
            c = 0;
            if (c != 0) {
            }
            return Attr.AttrType.FLOAT;
        }
        c = 65535;
        if (c != 0) {
        }
        return Attr.AttrType.FLOAT;
    }
}
