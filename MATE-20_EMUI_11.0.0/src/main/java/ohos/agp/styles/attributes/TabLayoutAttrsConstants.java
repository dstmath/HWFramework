package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class TabLayoutAttrsConstants extends ScrollViewAttrsConstants {
    public static final String FIXED_MODE = "fixed_mode";
    public static final String NORMAL_TEXT_COLOR = "normal_text_color";
    public static final String ORIENTATION = "orientation";
    public static final String SELECTED_TAB_INDICATOR_COLOR = "selected_tab_indicator_color";
    public static final String SELECTED_TAB_INDICATOR_HEIGHT = "selected_tab_indicator_height";
    public static final String SELECTED_TEXT_COLOR = "selected_text_color";
    public static final String TAB_INDICATOR_TYPE = "tab_indicator_type";
    public static final String TAB_LENGTH = "tab_length";
    public static final String TAB_MARGIN = "tab_margin";
    public static final String TEXT_ALIGNMENT = "text_alignment";
    public static final String TEXT_SIZE = "text_size";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.ScrollViewAttrsConstants, ohos.agp.styles.attributes.StackLayoutAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -2084620315:
                if (str.equals("selected_tab_indicator_height")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -1783100079:
                if (str.equals("text_alignment")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -1439500848:
                if (str.equals("orientation")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -1037596717:
                if (str.equals("text_size")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -764299035:
                if (str.equals("selected_tab_indicator_color")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -309934603:
                if (str.equals("selected_text_color")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -128193196:
                if (str.equals("tab_indicator_type")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 48674512:
                if (str.equals("tab_length")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 73728408:
                if (str.equals("tab_margin")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 130077737:
                if (str.equals("normal_text_color")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1707709326:
                if (str.equals("fixed_mode")) {
                    c = '\n';
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
                return Attr.AttrType.COLOR;
            case 3:
            case 4:
            case 5:
            case 6:
                return Attr.AttrType.DIMENSION;
            case 7:
            case '\b':
            case '\t':
                return Attr.AttrType.INT;
            case '\n':
                return Attr.AttrType.BOOLEAN;
            default:
                return super.getType(str);
        }
    }
}
