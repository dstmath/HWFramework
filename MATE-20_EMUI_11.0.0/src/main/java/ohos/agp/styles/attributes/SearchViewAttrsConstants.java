package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class SearchViewAttrsConstants extends DirectionalLayoutAttrsConstants {
    public static final String CLOSE_BUTTON_URI = "close_button_uri";
    public static final String ICONIFIED_BY_DEFAULT = "iconified_by_default";
    public static final String MAX_WIDTH = "max_width";
    public static final String QUERY_HINT = "query_hint";
    public static final String QUERY_TEXT_COLOR = "query_text_color";
    public static final String SEARCH_BUTTON_URI = "search_button_uri";
    public static final String SEARCH_ICON_URI = "search_icon_uri";
    public static final String SUBMIT_BUTTON_ENABLED = "submit_button_enabled";
    public static final String SUBMIT_BUTTON_URI = "submit_button_uri";
    public static final String TEXT_SIZE = "text_size";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.DirectionalLayoutAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -1947874970:
                if (str.equals(SUBMIT_BUTTON_URI)) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -1898214954:
                if (str.equals(SEARCH_BUTTON_URI)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -1755791482:
                if (str.equals(CLOSE_BUTTON_URI)) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -1037596717:
                if (str.equals("text_size")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -771947973:
                if (str.equals(SUBMIT_BUTTON_ENABLED)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -230974677:
                if (str.equals("max_width")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -225970851:
                if (str.equals(SEARCH_ICON_URI)) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -168510562:
                if (str.equals(QUERY_HINT)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 1103688966:
                if (str.equals(ICONIFIED_BY_DEFAULT)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1531179432:
                if (str.equals(QUERY_TEXT_COLOR)) {
                    c = '\t';
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
                return Attr.AttrType.INT;
            case 2:
            case 3:
                return Attr.AttrType.BOOLEAN;
            case 4:
            case 5:
            case 6:
            case 7:
            case '\b':
                return Attr.AttrType.STRING;
            case '\t':
                return Attr.AttrType.COLOR;
            default:
                return super.getType(str);
        }
    }
}
