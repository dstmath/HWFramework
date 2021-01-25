package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;
import ohos.agp.styles.attributes.DirectionalLayoutAttrsConstants;
import ohos.com.sun.org.apache.xml.internal.serializer.CharInfo;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.global.icu.impl.PatternTokenizer;
import ohos.global.icu.text.SymbolTable;

public class ViewAttrsConstants {
    public static final String ALPHA = "alpha";
    public static final String BACKGROUND_ELEMENT = "background_element";
    public static final String BOTTOM_MARGIN = "bottom_margin";
    public static final String BOTTOM_PADDING = "bottom_padding";
    public static final String CLICKABLE = "clickable";
    public static final String CONTENT_DESCRIPTION = "content_description";
    public static final String ENABLED = "enable";
    public static final String END_MARGIN = "end_margin";
    public static final String END_PADDING = "end_padding";
    public static final String FOCUSABLE = "focusable";
    public static final String FOCUSABLE_IN_TOUCH_MODE = "focusable_in_touch_mode";
    public static final String FOREGROUND_ELEMENT = "foreground_element";
    public static final String FOREGROUND_GRAVITY = "foreground_gravity";
    public static final String HEIGHT = "height";
    public static final String ID = "id";
    public static final String LAYOUT_DIRECTION = "layout_direction";
    public static final String LEFT_MARGIN = "left_margin";
    public static final String LEFT_PADDING = "left_padding";
    public static final String LONG_CLICKABLE = "long_clickable";
    public static final String MARGIN = "margin";
    public static final String MINIMUM_HEIGHT = "minimum_height";
    public static final String MINIMUM_WIDTH = "minimum_width";
    public static final String PADDING = "padding";
    public static final String PIVOT_X = "pivot_x";
    public static final String PIVOT_Y = "pivot_y";
    public static final String POSITION_X = "position_x";
    public static final String POSITION_Y = "position_y";
    public static final String RIGHT_MARGIN = "right_margin";
    public static final String RIGHT_PADDING = "right_padding";
    public static final String ROTATION = "rotation";
    public static final String ROTATION_X = "rotation_x";
    public static final String ROTATION_Y = "rotation_y";
    public static final String SCALE_X = "scale_x";
    public static final String SCALE_Y = "scale_y";
    public static final String SCROLLBAR_BACKGROUND_COLOR = "scrollbar_background_color";
    public static final String SCROLLBAR_FADING = "scrollbar_fading";
    public static final String SCROLLBAR_FADING_DELAY = "scrollbar_fading_delay";
    public static final String SCROLLBAR_FADING_DURATION = "scrollbar_fading_duration";
    public static final String SCROLLBAR_START_ANGLE = "scrollbar_start_angle";
    public static final String SCROLLBAR_SWEEP_ANGLE = "scrollbar_sweep_angle";
    public static final String SCROLLBAR_THICKNESS = "scrollbar_thickness";
    public static final String SCROLLBAR_THUMB_COLOR = "scrollbar_thumb_color";
    public static final String START_MARGIN = "start_margin";
    public static final String START_PADDING = "start_padding";
    public static final String TOP_MARGIN = "top_margin";
    public static final String TOP_PADDING = "top_padding";
    public static final String TRANSLATION_X = "translation_x";
    public static final String TRANSLATION_Y = "translation_y";
    public static final String VISIBILITY = "visibility";
    public static final String WIDTH = "width";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -1964681502:
                if (str.equals(CLICKABLE)) {
                    c = 28;
                    break;
                }
                c = 65535;
                break;
            case -1670432063:
                if (str.equals(SCROLLBAR_FADING_DURATION)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1645641945:
                if (str.equals(TOP_PADDING)) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -1416025802:
                if (str.equals(CONTENT_DESCRIPTION)) {
                    c = ' ';
                    break;
                }
                c = 65535;
                break;
            case -1409720747:
                if (str.equals(MINIMUM_WIDTH)) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case -1298848381:
                if (str.equals(ENABLED)) {
                    c = 27;
                    break;
                }
                c = 65535;
                break;
            case -1298124222:
                if (str.equals(BOTTOM_MARGIN)) {
                    c = 23;
                    break;
                }
                c = 65535;
                break;
            case -1221903360:
                if (str.equals(FOREGROUND_ELEMENT)) {
                    c = '/';
                    break;
                }
                c = 65535;
                break;
            case -1221029593:
                if (str.equals(HEIGHT)) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -1212277530:
                if (str.equals(LEFT_MARGIN)) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case -1184664968:
                if (str.equals(MINIMUM_HEIGHT)) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -1081309778:
                if (str.equals(MARGIN)) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case -872181590:
                if (str.equals(LAYOUT_DIRECTION)) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -831289384:
                if (str.equals(TOP_MARGIN)) {
                    c = 21;
                    break;
                }
                c = 65535;
                break;
            case -806339567:
                if (str.equals(PADDING)) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -791592328:
                if (str.equals(DirectionalLayoutAttrsConstants.LayoutParamsAttrsConstants.LAYOUT_WEIGHT)) {
                    c = SymbolTable.SYMBOL_REF;
                    break;
                }
                c = 65535;
                break;
            case -774266192:
                if (str.equals(FOCUSABLE_IN_TOUCH_MODE)) {
                    c = 30;
                    break;
                }
                c = 65535;
                break;
            case -571372583:
                if (str.equals(LEFT_PADDING)) {
                    c = CharInfo.S_CARRIAGERETURN;
                    break;
                }
                c = 65535;
                break;
            case -560345157:
                if (str.equals(PIVOT_X)) {
                    c = '%';
                    break;
                }
                c = 65535;
                break;
            case -560345156:
                if (str.equals(PIVOT_Y)) {
                    c = '&';
                    break;
                }
                c = 65535;
                break;
            case -428775647:
                if (str.equals(SCROLLBAR_THUMB_COLOR)) {
                    c = '0';
                    break;
                }
                c = 65535;
                break;
            case -283203043:
                if (str.equals(SCROLLBAR_START_ANGLE)) {
                    c = '\"';
                    break;
                }
                c = 65535;
                break;
            case -211977717:
                if (str.equals(SCROLLBAR_BACKGROUND_COLOR)) {
                    c = '1';
                    break;
                }
                c = 65535;
                break;
            case -171938773:
                if (str.equals(START_MARGIN)) {
                    c = 24;
                    break;
                }
                c = 65535;
                break;
            case -74238985:
                if (str.equals(ROTATION_X)) {
                    c = '(';
                    break;
                }
                c = 65535;
                break;
            case -74238984:
                if (str.equals(ROTATION_Y)) {
                    c = ')';
                    break;
                }
                c = 65535;
                break;
            case -50044755:
                if (str.equals(END_PADDING)) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case -40300674:
                if (str.equals(ROTATION)) {
                    c = PatternTokenizer.SINGLE_QUOTE;
                    break;
                }
                c = 65535;
                break;
            case 3355:
                if (str.equals("id")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 50627879:
                if (str.equals(SCROLLBAR_SWEEP_ANGLE)) {
                    c = '#';
                    break;
                }
                c = 65535;
                break;
            case 92909918:
                if (str.equals(ALPHA)) {
                    c = '!';
                    break;
                }
                c = 65535;
                break;
            case 113126854:
                if (str.equals(WIDTH)) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 190012818:
                if (str.equals(END_MARGIN)) {
                    c = 25;
                    break;
                }
                c = 65535;
                break;
            case 261212398:
                if (str.equals(RIGHT_PADDING)) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 484453599:
                if (str.equals(LONG_CLICKABLE)) {
                    c = 29;
                    break;
                }
                c = 65535;
                break;
            case 523373803:
                if (str.equals(BACKGROUND_ELEMENT)) {
                    c = '.';
                    break;
                }
                c = 65535;
                break;
            case 664278634:
                if (str.equals(TRANSLATION_X)) {
                    c = ',';
                    break;
                }
                c = 65535;
                break;
            case 664278635:
                if (str.equals(TRANSLATION_Y)) {
                    c = LocaleUtility.IETF_SEPARATOR;
                    break;
                }
                c = 65535;
                break;
            case 721456978:
                if (str.equals(FOREGROUND_GRAVITY)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 1062347261:
                if (str.equals(BOTTOM_PADDING)) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case 1165820059:
                if (str.equals(SCROLLBAR_THICKNESS)) {
                    c = 26;
                    break;
                }
                c = 65535;
                break;
            case 1579131766:
                if (str.equals(SCROLLBAR_FADING_DELAY)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1614357812:
                if (str.equals(START_PADDING)) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case 1629011506:
                if (str.equals(FOCUSABLE)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 1823941746:
                if (str.equals(SCROLLBAR_FADING)) {
                    c = 31;
                    break;
                }
                c = 65535;
                break;
            case 1858284302:
                if (str.equals("layout_alignment")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1910893251:
                if (str.equals(SCALE_X)) {
                    c = '*';
                    break;
                }
                c = 65535;
                break;
            case 1910893252:
                if (str.equals(SCALE_Y)) {
                    c = '+';
                    break;
                }
                c = 65535;
                break;
            case 1941332754:
                if (str.equals(VISIBILITY)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 2001168689:
                if (str.equals(RIGHT_MARGIN)) {
                    c = 22;
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
            case 4:
            case 5:
            case 6:
            case 7:
                return Attr.AttrType.INT;
            case '\b':
            case '\t':
            case '\n':
            case 11:
            case '\f':
            case '\r':
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
                return Attr.AttrType.DIMENSION;
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
                return Attr.AttrType.BOOLEAN;
            case ' ':
                return Attr.AttrType.STRING;
            case '!':
            case '\"':
            case '#':
            case '$':
            case '%':
            case '&':
            case '\'':
            case '(':
            case ')':
            case '*':
            case '+':
            case ',':
            case '-':
                return Attr.AttrType.FLOAT;
            case '.':
            case '/':
                return Attr.AttrType.ELEMENT;
            case '0':
            case '1':
                return Attr.AttrType.COLOR;
            default:
                return Attr.AttrType.NONE;
        }
    }
}
