package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class SwitchAttrsConstants extends AbsButtonAttrsConstants {
    public static final String TEXT_OFF = "text_off";
    public static final String TEXT_ON = "text_on";
    public static final String THUMB_ELEMENT = "thumb_element";
    public static final String TRACK_ELEMENT = "track_element";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.AbsButtonAttrsConstants, ohos.agp.styles.attributes.TextViewAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -2053751800:
                if (str.equals(TRACK_ELEMENT)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1417838031:
                if (str.equals("text_on")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -1003306147:
                if (str.equals("text_off")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 788152723:
                if (str.equals("thumb_element")) {
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
            return Attr.AttrType.STRING;
        }
        if (c == 2 || c == 3) {
            return Attr.AttrType.ELEMENT;
        }
        return super.getType(str);
    }
}
