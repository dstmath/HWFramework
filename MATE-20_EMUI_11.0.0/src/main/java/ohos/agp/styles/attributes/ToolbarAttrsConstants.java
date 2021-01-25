package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class ToolbarAttrsConstants extends ViewAttrsConstants {
    public static final String LOGO = "logo";
    public static final String LOGO_DESCRIPTION = "logo_description";
    public static final String SUBTITLE = "subtitle";
    public static final String SUBTITLE_COLOR = "subtitle_color";
    public static final String TITLE = "title";
    public static final String TITLE_COLOR = "title_color";
    public static final String TITLE_MARGIN_BOTTOM = "title_margin_bottom";
    public static final String TITLE_MARGIN_END = "title_margin_end";
    public static final String TITLE_MARGIN_START = "title_margin_start";
    public static final String TITLE_MARGIN_TOP = "title_margin_top";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -2060497896:
                if (str.equals(SUBTITLE)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1663079300:
                if (str.equals(SUBTITLE_COLOR)) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -1648825291:
                if (str.equals(TITLE_MARGIN_BOTTOM)) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -933985519:
                if (str.equals(TITLE_MARGIN_END)) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -933971061:
                if (str.equals(TITLE_MARGIN_TOP)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -556105880:
                if (str.equals(LOGO_DESCRIPTION)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 3327403:
                if (str.equals(LOGO)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 101189912:
                if (str.equals(TITLE_MARGIN_START)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 110371416:
                if (str.equals(TITLE)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 844796604:
                if (str.equals(TITLE_COLOR)) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
            case 2:
            case 3:
                return Attr.AttrType.STRING;
            case 4:
            case 5:
            case 6:
            case 7:
                return Attr.AttrType.DIMENSION;
            case '\b':
            case '\t':
                return Attr.AttrType.COLOR;
            default:
                return super.getType(str);
        }
    }
}
