package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class ToggleButtonAttrsConstants extends AbsButtonAttrsConstants {
    public static final String TEXT_OFF = "text_off";
    public static final String TEXT_ON = "text_on";

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0029 A[ADDED_TO_REGION] */
    @Override // ohos.agp.styles.attributes.AbsButtonAttrsConstants, ohos.agp.styles.attributes.TextViewAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        int hashCode = str.hashCode();
        if (hashCode != -1417838031) {
            if (hashCode == -1003306147 && str.equals("text_off")) {
                c = 1;
                if (c != 0 || c == 1) {
                    return Attr.AttrType.STRING;
                }
                return super.getType(str);
            }
        } else if (str.equals("text_on")) {
            c = 0;
            if (c != 0) {
            }
            return Attr.AttrType.STRING;
        }
        c = 65535;
        if (c != 0) {
        }
        return Attr.AttrType.STRING;
    }
}
