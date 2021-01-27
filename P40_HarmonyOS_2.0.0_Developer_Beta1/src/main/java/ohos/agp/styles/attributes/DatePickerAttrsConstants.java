package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;
import ohos.com.sun.org.apache.xml.internal.serializer.CharInfo;

public class DatePickerAttrsConstants extends StackLayoutAttrsConstants {
    public static final String BOTTOM_LINE_ELEMENT = "bottom_line_element";
    public static final String DATE_ORDER = "date_order";
    public static final String DAY_FIXED = "day_fixed";
    public static final String MAX_DATE = "max_date";
    public static final String MIN_DATE = "min_date";
    public static final String MONTH_FIXED = "month_fixed";
    public static final String NORMAL_TEXT_COLOR = "normal_text_color";
    public static final String NORMAL_TEXT_SIZE = "normal_text_size";
    public static final String OPERATED_TEXT_BACKGROUND = "operated_text_background";
    public static final String OPERATED_TEXT_COLOR = "operated_text_color";
    public static final String SELECTED_NORMAL_TEXT_MARGIN_RATIO = "selected_normal_text_margin_ratio";
    public static final String SELECTED_TEXT_BACKGROUND = "selected_text_background";
    public static final String SELECTED_TEXT_COLOR = "selected_text_color";
    public static final String SELECTED_TEXT_SIZE = "selected_text_size";
    public static final String SELECTOR_ITEM_NUM = "selector_item_num";
    public static final String SHADER_COLOR = "shader_color";
    public static final String TEXT_SIZE = "text_size";
    public static final String TOP_LINE_ELEMENT = "top_line_element";
    public static final String WHEEL_MODE_ENABLED = "wheel_mode_enabled";
    public static final String YEAR_FIXED = "year_fixed";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.StackLayoutAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -2005274382:
                if (str.equals(YEAR_FIXED)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -1668367174:
                if (str.equals("selector_item_num")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1637496183:
                if (str.equals("shader_color")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -1368578693:
                if (str.equals(MIN_DATE)) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -1363115048:
                if (str.equals("selected_normal_text_margin_ratio")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case -1255255024:
                if (str.equals("operated_text_color")) {
                    c = CharInfo.S_CARRIAGERETURN;
                    break;
                }
                c = 65535;
                break;
            case -1037596717:
                if (str.equals("text_size")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case -979357905:
                if (str.equals("selected_text_size")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -869660196:
                if (str.equals("selected_text_background")) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case -309934603:
                if (str.equals("selected_text_color")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -271931119:
                if (str.equals(DAY_FIXED)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 130077737:
                if (str.equals("normal_text_color")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 407617961:
                if (str.equals(MAX_DATE)) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 697404027:
                if (str.equals("normal_text_size")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 857609377:
                if (str.equals("operated_text_background")) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case 870965117:
                if (str.equals(DATE_ORDER)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1041992949:
                if (str.equals(MONTH_FIXED)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1316826949:
                if (str.equals("bottom_line_element")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case 1362907913:
                if (str.equals("wheel_mode_enabled")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 1889762395:
                if (str.equals("top_line_element")) {
                    c = 16;
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
            case 4:
            case 5:
                return Attr.AttrType.BOOLEAN;
            case 6:
            case 7:
                return Attr.AttrType.LONG;
            case '\b':
            case '\t':
            case '\n':
                return Attr.AttrType.DIMENSION;
            case 11:
            case '\f':
            case '\r':
            case 14:
                return Attr.AttrType.COLOR;
            case 15:
                return Attr.AttrType.FLOAT;
            case 16:
            case 17:
            case 18:
            case 19:
                return Attr.AttrType.ELEMENT;
            default:
                return super.getType(str);
        }
    }
}
