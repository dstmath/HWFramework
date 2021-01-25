package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;
import ohos.com.sun.org.apache.xml.internal.serializer.CharInfo;

public class TextViewAttrsConstants extends ViewAttrsConstants {
    public static final String AUTO_FONT_SIZE = "auto_font_size";
    public static final String CURSOR_VISIBLE = "cursor_visible";
    public static final String ELEMENT_BOTTOM = "element_bottom";
    public static final String ELEMENT_END = "element_end";
    public static final String ELEMENT_LEFT = "element_left";
    public static final String ELEMENT_PADDING = "element_padding";
    public static final String ELEMENT_RIGHT = "element_right";
    public static final String ELEMENT_START = "element_start";
    public static final String ELEMENT_TOP = "element_top";
    public static final String ELLIPSIZE = "ellipsize";
    public static final String HINT = "hint";
    public static final String HINT_COLOR = "hint_color";
    public static final String IME_OPTION = "ime_option";
    public static final String INPUT_TYPE = "input_type";
    public static final String ITALIC = "italic";
    public static final String LINE_SPACING_EXTRA = "line_spacing_extra";
    public static final String LINE_SPACING_MULTIPLIER = "line_spacing_multiplier";
    public static final String MAX_LINES = "max_lines";
    public static final String MULTIPLE_LINES = "multiple_lines";
    public static final String SCROLLABLE = "scrollable";
    public static final String TEXT = "text";
    public static final String TEXT_ALIGNMENT = "text_alignment";
    public static final String TEXT_COLOR = "text_color";
    public static final String TEXT_FONT = "text_font";
    public static final String TEXT_SIZE = "text_size";
    public static final String TEXT_STYLE = "text_style";
    public static final String TEXT_WEIGHT = "text_weight";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -2115337775:
                if (str.equals(TEXT_COLOR)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -2100400097:
                if (str.equals(TEXT_STYLE)) {
                    c = 11;
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
            case -1583029042:
                if (str.equals(ELEMENT_BOTTOM)) {
                    c = 24;
                    break;
                }
                c = 65535;
                break;
            case -1269570893:
                if (str.equals(IME_OPTION)) {
                    c = CharInfo.S_CARRIAGERETURN;
                    break;
                }
                c = 65535;
                break;
            case -1178781136:
                if (str.equals(ITALIC)) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case -1037978591:
                if (str.equals(TEXT_FONT)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1037596717:
                if (str.equals("text_size")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -590670055:
                if (str.equals(ELEMENT_RIGHT)) {
                    c = 23;
                    break;
                }
                c = 65535;
                break;
            case -589424289:
                if (str.equals(ELEMENT_START)) {
                    c = 25;
                    break;
                }
                c = 65535;
                break;
            case -587711030:
                if (str.equals(TEXT_WEIGHT)) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -573425814:
                if (str.equals(ELEMENT_LEFT)) {
                    c = 21;
                    break;
                }
                c = 65535;
                break;
            case -301224095:
                if (str.equals(AUTO_FONT_SIZE)) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case -241124252:
                if (str.equals(MAX_LINES)) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -37821431:
                if (str.equals(LINE_SPACING_EXTRA)) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case 3202695:
                if (str.equals(HINT)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 3556653:
                if (str.equals("text")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 66669991:
                if (str.equals(SCROLLABLE)) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case 674232600:
                if (str.equals(ELEMENT_END)) {
                    c = 26;
                    break;
                }
                c = 65535;
                break;
            case 674247058:
                if (str.equals(ELEMENT_TOP)) {
                    c = 22;
                    break;
                }
                c = 65535;
                break;
            case 764428648:
                if (str.equals(LINE_SPACING_MULTIPLIER)) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case 1160901769:
                if (str.equals(CURSOR_VISIBLE)) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case 1309117456:
                if (str.equals(MULTIPLE_LINES)) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case 1354706859:
                if (str.equals(HINT_COLOR)) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 1386692239:
                if (str.equals(INPUT_TYPE)) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 1554823821:
                if (str.equals(ELLIPSIZE)) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 1589790158:
                if (str.equals("element_padding")) {
                    c = 4;
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
                return Attr.AttrType.STRING;
            case 3:
            case 4:
                return Attr.AttrType.DIMENSION;
            case 5:
            case 6:
                return Attr.AttrType.COLOR;
            case 7:
            case '\b':
            case '\t':
            case '\n':
            case 11:
            case '\f':
            case '\r':
                return Attr.AttrType.INT;
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
                return Attr.AttrType.BOOLEAN;
            case 19:
            case 20:
                return Attr.AttrType.FLOAT;
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
                return Attr.AttrType.ELEMENT;
            default:
                return super.getType(str);
        }
    }
}
