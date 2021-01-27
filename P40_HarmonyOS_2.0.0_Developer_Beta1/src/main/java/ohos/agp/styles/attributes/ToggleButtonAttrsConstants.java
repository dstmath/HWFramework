package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class ToggleButtonAttrsConstants extends AbsButtonAttrsConstants {
    public static final String TEXT_STATE_OFF = "text_state_off";
    public static final String TEXT_STATE_ON = "text_state_on";

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0029 A[ADDED_TO_REGION] */
    @Override // ohos.agp.styles.attributes.AbsButtonAttrsConstants, ohos.agp.styles.attributes.TextAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        int hashCode = str.hashCode();
        if (hashCode != -1341563857) {
            if (hashCode == -320370913 && str.equals("text_state_on")) {
                c = 0;
                if (c != 0 || c == 1) {
                    return Attr.AttrType.STRING;
                }
                return super.getType(str);
            }
        } else if (str.equals("text_state_off")) {
            c = 1;
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
