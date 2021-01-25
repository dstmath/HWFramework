package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;
import ohos.com.sun.org.apache.xml.internal.serializer.CharInfo;

public class TimePickerAttrsConstants extends StackLayoutAttrsConstants {
    public static final String AM_PM_ORDER = "am_pm_order";
    public static final String BOTTOM_LINE_ELEMENT = "bottom_line_element";
    public static final String ENABLE_24_HOUR_MODE = "24_hour_mode";
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";
    public static final String NORMAL_TEXT_COLOR = "normal_text_color";
    public static final String NORMAL_TEXT_SIZE = "normal_text_size";
    public static final String OPERATED_TEXT_COLOR = "operated_text_color";
    public static final String SECOND = "second";
    public static final String SELECTED_NORMAL_TEXT_MARGIN_RATIO = "selected_normal_text_margin_ratio";
    public static final String SELECTED_TEXT_COLOR = "selected_text_color";
    public static final String SELECTED_TEXT_SIZE = "selected_text_size";
    public static final String SELECTOR_WHEEL_ITEM_NUM = "selector_wheel_item_num";
    public static final String SHADER_COLOR = "shader_color";
    public static final String TEXT_AM = "text_am";
    public static final String TEXT_PM = "text_pm";
    public static final String TOP_LINE_ELEMENT = "top_line_element";
    public static final String WRAP_SELECTOR_WHEEL = "wrap_selector_wheel";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.StackLayoutAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -1637496183:
                if (str.equals("shader_color")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -1576623775:
                if (str.equals(ENABLE_24_HOUR_MODE)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -1417838466:
                if (str.equals(TEXT_AM)) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case -1417838001:
                if (str.equals(TEXT_PM)) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case -1363115048:
                if (str.equals("selected_normal_text_margin_ratio")) {
                    c = CharInfo.S_CARRIAGERETURN;
                    break;
                }
                c = 65535;
                break;
            case -1255255024:
                if (str.equals("operated_text_color")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -1074026988:
                if (str.equals(MINUTE)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -979357905:
                if (str.equals("selected_text_size")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -906279820:
                if (str.equals(SECOND)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -309934603:
                if (str.equals("selected_text_color")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 3208676:
                if (str.equals(HOUR)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 130077737:
                if (str.equals("normal_text_color")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 340450623:
                if (str.equals(AM_PM_ORDER)) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 697404027:
                if (str.equals("normal_text_size")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 850407312:
                if (str.equals("wrap_selector_wheel")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1316826949:
                if (str.equals("bottom_line_element")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 1889762395:
                if (str.equals("top_line_element")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case 2141978910:
                if (str.equals("selector_wheel_item_num")) {
                    c = 5;
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
                return Attr.AttrType.BOOLEAN;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return Attr.AttrType.INT;
            case 7:
            case '\b':
                return Attr.AttrType.DIMENSION;
            case '\t':
            case '\n':
            case 11:
            case '\f':
                return Attr.AttrType.COLOR;
            case '\r':
                return Attr.AttrType.FLOAT;
            case 14:
            case 15:
                return Attr.AttrType.ELEMENT;
            case 16:
            case 17:
                return Attr.AttrType.STRING;
            default:
                return super.getType(str);
        }
    }
}
