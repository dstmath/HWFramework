package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class AbsButtonAttrsConstants extends TextAttrsConstants {
    public static final String CHECK_ELEMENT = "check_element";
    public static final String MARKED = "marked";
    public static final String TEXT_COLOR_OFF = "text_color_off";
    public static final String TEXT_COLOR_ON = "text_color_on";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.TextAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -1569714687:
                if (str.equals(TEXT_COLOR_OFF)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1249566011:
                if (str.equals(CHECK_ELEMENT)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -1081306068:
                if (str.equals(MARKED)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 2027574029:
                if (str.equals(TEXT_COLOR_ON)) {
                    c = 1;
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
        if (c == 1 || c == 2) {
            return Attr.AttrType.COLOR;
        }
        if (c != 3) {
            return super.getType(str);
        }
        return Attr.AttrType.ELEMENT;
    }
}
