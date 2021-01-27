package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;
import ohos.com.sun.org.apache.xml.internal.serializer.CharInfo;
import ohos.global.icu.impl.PatternTokenizer;

public class TextAttrsConstants extends ViewAttrsConstants {
    public static final String ADDITIONAL_LINE_SPACING = "additional_line_spacing";
    public static final String AUTO_FONT_SIZE = "auto_font_size";
    public static final String AUTO_SCROLLING_COUNT = "auto_scrolling_count";
    public static final String AUTO_SCROLLING_DURATION = "auto_scrolling_duration";
    public static final String BUBBLE_HEIGHT = "bubble_height";
    public static final String BUBBLE_LEFT_HEIGHT = "bubble_left_height";
    public static final String BUBBLE_LEFT_WIDTH = "bubble_left_width";
    public static final String BUBBLE_RIGHT_HEIGHT = "bubble_right_height";
    public static final String BUBBLE_RIGHT_WIDTH = "bubble_right_width";
    public static final String BUBBLE_WIDTH = "bubble_width";
    public static final String ELEMENT_BOTTOM = "element_bottom";
    public static final String ELEMENT_CURSOR_BUBBLE = "element_cursor_bubble";
    public static final String ELEMENT_END = "element_end";
    public static final String ELEMENT_LEFT = "element_left";
    public static final String ELEMENT_PADDING = "element_padding";
    public static final String ELEMENT_RIGHT = "element_right";
    public static final String ELEMENT_SELECTION_LEFT_BUBBLE = "element_selection_left_bubble";
    public static final String ELEMENT_SELECTION_RIGHT_BUBBLE = "element_selection_right_bubble";
    public static final String ELEMENT_START = "element_start";
    public static final String ELEMENT_TOP = "element_top";
    public static final String ELLIPSIZE = "ellipsize";
    public static final String HINT = "hint";
    public static final String HINT_COLOR = "hint_color";
    public static final String INPUT_ENTER_KEY_TYPE = "input_enter_key_type";
    public static final String ITALIC = "italic";
    public static final String LINE_HEIGHT_NUM = "line_height_num";
    public static final String MAX_TEXT_LINES = "max_text_lines";
    public static final String MULTIPLE_LINES = "multiple_lines";
    public static final String PADDING_FOR_TEXT = "padding_for_text";
    public static final String SCROLLABLE = "scrollable";
    public static final String SELECTION_COLOR = "selection_color";
    public static final String TEXT = "text";
    public static final String TEXT_ALIGNMENT = "text_alignment";
    public static final String TEXT_COLOR = "text_color";
    public static final String TEXT_CURSOR_VISIBLE = "text_cursor_visible";
    public static final String TEXT_FONT = "text_font";
    public static final String TEXT_INPUT_TYPE = "text_input_type";
    public static final String TEXT_SIZE = "text_size";
    public static final String TEXT_STYLE = "text_style";
    public static final String TEXT_WEIGHT = "text_weight";
    public static final String TRUNCATION_MODE = "truncation_mode";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -2115337775:
                if (str.equals(TEXT_COLOR)) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -2100400097:
                if (str.equals(TEXT_STYLE)) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case -1945078858:
                if (str.equals(INPUT_ENTER_KEY_TYPE)) {
                    c = 21;
                    break;
                }
                c = 65535;
                break;
            case -1783100079:
                if (str.equals("text_alignment")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case -1618629549:
                if (str.equals(BUBBLE_WIDTH)) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -1590617979:
                if (str.equals(ELEMENT_SELECTION_RIGHT_BUBBLE)) {
                    c = '(';
                    break;
                }
                c = 65535;
                break;
            case -1583029042:
                if (str.equals(ELEMENT_BOTTOM)) {
                    c = '#';
                    break;
                }
                c = 65535;
                break;
            case -1517573392:
                if (str.equals(SELECTION_COLOR)) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -1415289447:
                if (str.equals(LINE_HEIGHT_NUM)) {
                    c = 31;
                    break;
                }
                c = 65535;
                break;
            case -1369571503:
                if (str.equals(PADDING_FOR_TEXT)) {
                    c = 29;
                    break;
                }
                c = 65535;
                break;
            case -1319354143:
                if (str.equals(TEXT_INPUT_TYPE)) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case -1178781136:
                if (str.equals(ITALIC)) {
                    c = 28;
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
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -1016599171:
                if (str.equals(BUBBLE_RIGHT_HEIGHT)) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -890648276:
                if (str.equals(BUBBLE_LEFT_HEIGHT)) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -784210802:
                if (str.equals(ELEMENT_SELECTION_LEFT_BUBBLE)) {
                    c = PatternTokenizer.SINGLE_QUOTE;
                    break;
                }
                c = 65535;
                break;
            case -635464402:
                if (str.equals(AUTO_SCROLLING_DURATION)) {
                    c = 22;
                    break;
                }
                c = 65535;
                break;
            case -590670055:
                if (str.equals(ELEMENT_RIGHT)) {
                    c = '\"';
                    break;
                }
                c = 65535;
                break;
            case -589424289:
                if (str.equals(ELEMENT_START)) {
                    c = '$';
                    break;
                }
                c = 65535;
                break;
            case -587711030:
                if (str.equals(TEXT_WEIGHT)) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case -573425814:
                if (str.equals(ELEMENT_LEFT)) {
                    c = ' ';
                    break;
                }
                c = 65535;
                break;
            case -375469272:
                if (str.equals(MAX_TEXT_LINES)) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case -301224095:
                if (str.equals(AUTO_FONT_SIZE)) {
                    c = 25;
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
            case 29962256:
                if (str.equals(ADDITIONAL_LINE_SPACING)) {
                    c = 30;
                    break;
                }
                c = 65535;
                break;
            case 66669991:
                if (str.equals(SCROLLABLE)) {
                    c = 26;
                    break;
                }
                c = 65535;
                break;
            case 158908341:
                if (str.equals(AUTO_SCROLLING_COUNT)) {
                    c = 23;
                    break;
                }
                c = 65535;
                break;
            case 396816048:
                if (str.equals(BUBBLE_RIGHT_WIDTH)) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 674232600:
                if (str.equals(ELEMENT_END)) {
                    c = '%';
                    break;
                }
                c = 65535;
                break;
            case 674247058:
                if (str.equals(ELEMENT_TOP)) {
                    c = '!';
                    break;
                }
                c = 65535;
                break;
            case 929096762:
                if (str.equals(BUBBLE_HEIGHT)) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1006657369:
                if (str.equals(TRUNCATION_MODE)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1309117456:
                if (str.equals(MULTIPLE_LINES)) {
                    c = 24;
                    break;
                }
                c = 65535;
                break;
            case 1354706859:
                if (str.equals(HINT_COLOR)) {
                    c = CharInfo.S_CARRIAGERETURN;
                    break;
                }
                c = 65535;
                break;
            case 1370710305:
                if (str.equals(BUBBLE_LEFT_WIDTH)) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 1554823821:
                if (str.equals(ELLIPSIZE)) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case 1589790158:
                if (str.equals("element_padding")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 1645837787:
                if (str.equals(TEXT_CURSOR_VISIBLE)) {
                    c = 27;
                    break;
                }
                c = 65535;
                break;
            case 1984846226:
                if (str.equals(ELEMENT_CURSOR_BUBBLE)) {
                    c = '&';
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
            case '\b':
            case '\t':
            case '\n':
            case 11:
                return Attr.AttrType.DIMENSION;
            case '\f':
            case '\r':
            case 14:
                return Attr.AttrType.COLOR;
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
                return Attr.AttrType.INT;
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
                return Attr.AttrType.BOOLEAN;
            case 30:
            case 31:
                return Attr.AttrType.FLOAT;
            case ' ':
            case '!':
            case '\"':
            case '#':
            case '$':
            case '%':
            case '&':
            case '\'':
            case '(':
                return Attr.AttrType.ELEMENT;
            default:
                return super.getType(str);
        }
    }
}
