package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;
import ohos.com.sun.org.apache.xml.internal.serializer.CharInfo;

public class PickerAttrsConstants extends DirectionalLayoutAttrsConstants {
    public static final String BOTTOM_LINE_ELEMENT = "bottom_line_element";
    public static final String ELEMENT_PADDING = "element_padding";
    public static final String MAX_VALUE = "max_value";
    public static final String MIN_VALUE = "min_value";
    public static final String NORMAL_TEXT_COLOR = "normal_text_color";
    public static final String NORMAL_TEXT_SIZE = "normal_text_size";
    public static final String SELECTED_NORMAL_TEXT_MARGIN_RATIO = "selected_normal_text_margin_ratio";
    public static final String SELECTED_TEXT_COLOR = "selected_text_color";
    public static final String SELECTED_TEXT_SIZE = "selected_text_size";
    public static final String SELECTOR_WHEEL_ITEM_NUM = "selector_wheel_item_num";
    public static final String SHADER_COLOR = "shader_color";
    public static final String TOP_LINE_ELEMENT = "top_line_element";
    public static final String VALUE = "value";
    public static final String WRAP_SELECTOR_WHEEL = "wrap_selector_wheel";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.DirectionalLayoutAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -1637496183:
                if (str.equals("shader_color")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case -1363115048:
                if (str.equals("selected_normal_text_margin_ratio")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -979357905:
                if (str.equals("selected_text_size")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -309934603:
                if (str.equals("selected_text_color")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -232128810:
                if (str.equals(MAX_VALUE)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 111972721:
                if (str.equals("value")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 130077737:
                if (str.equals("normal_text_color")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 540349764:
                if (str.equals(MIN_VALUE)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 697404027:
                if (str.equals("normal_text_size")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 850407312:
                if (str.equals("wrap_selector_wheel")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1316826949:
                if (str.equals("bottom_line_element")) {
                    c = CharInfo.S_CARRIAGERETURN;
                    break;
                }
                c = 65535;
                break;
            case 1589790158:
                if (str.equals("element_padding")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1889762395:
                if (str.equals("top_line_element")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 2141978910:
                if (str.equals("selector_wheel_item_num")) {
                    c = 6;
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
                return Attr.AttrType.DIMENSION;
            case 3:
            case 4:
            case 5:
            case 6:
                return Attr.AttrType.INT;
            case 7:
                return Attr.AttrType.BOOLEAN;
            case '\b':
            case '\t':
            case '\n':
                return Attr.AttrType.COLOR;
            case 11:
                return Attr.AttrType.FLOAT;
            case '\f':
            case '\r':
                return Attr.AttrType.ELEMENT;
            default:
                return super.getType(str);
        }
    }
}
