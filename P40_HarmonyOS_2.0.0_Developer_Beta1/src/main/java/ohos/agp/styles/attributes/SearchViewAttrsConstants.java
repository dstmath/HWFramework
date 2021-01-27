package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class SearchViewAttrsConstants extends DirectionalLayoutAttrsConstants {
    public static final String CLOSE_BUTTON = "close_button";
    public static final String ICONIFIED_BY_DEFAULT = "iconified_by_default";
    public static final String MAX_WIDTH = "max_width";
    public static final String QUERY_HINT = "query_hint";
    public static final String QUERY_TEXT_COLOR = "query_text_color";
    public static final String SEARCH_BUTTON = "search_button";
    public static final String SEARCH_BUTTON_ENABLED = "search_button_enabled";
    public static final String SEARCH_ICON = "search_icon";
    public static final String SUBMIT_BUTTON = "submit_button";
    public static final String TEXT_SIZE = "text_size";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.DirectionalLayoutAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -1678958759:
                if (str.equals(CLOSE_BUTTON)) {
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
            case -582346439:
                if (str.equals(SUBMIT_BUTTON)) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -539567952:
                if (str.equals(SEARCH_ICON)) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -365098325:
                if (str.equals(SEARCH_BUTTON_ENABLED)) {
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
            case -168510562:
                if (str.equals(QUERY_HINT)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 982618537:
                if (str.equals(SEARCH_BUTTON)) {
                    c = 5;
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
                return Attr.AttrType.STRING;
            case 5:
            case 6:
            case 7:
            case '\b':
                return Attr.AttrType.ELEMENT;
            case '\t':
                return Attr.AttrType.COLOR;
            default:
                return super.getType(str);
        }
    }
}
