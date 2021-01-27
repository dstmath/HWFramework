package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;
import ohos.agp.styles.attributes.DirectionalLayoutAttrsConstants;
import ohos.com.sun.org.apache.xml.internal.serializer.CharInfo;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.global.icu.impl.PatternTokenizer;

public class ViewAttrsConstants {
    public static final String ALPHA = "alpha";
    public static final String BACKGROUND_ELEMENT = "background_element";
    public static final String BOTTOM_MARGIN = "bottom_margin";
    public static final String BOTTOM_PADDING = "bottom_padding";
    public static final String CLICKABLE = "clickable";
    public static final String COMPONENT_DESCRIPTION = "component_description";
    public static final String ENABLED = "enabled";
    public static final String END_MARGIN = "end_margin";
    public static final String END_PADDING = "end_padding";
    public static final String FOCUSABLE = "focusable";
    public static final String FOCUSABLE_IN_TOUCH = "focusable_in_touch";
    public static final String FOCUS_BORDER_ENABLE = "focus_border_enable";
    public static final String FOCUS_BORDER_PADDING = "focus_border_padding";
    public static final String FOCUS_BORDER_RADIUS = "focus_border_radius";
    public static final String FOCUS_BORDER_WIDTH = "focus_border_width";
    public static final String FOREGROUND_ALIGNMENT = "foreground_alignment";
    public static final String FOREGROUND_ELEMENT = "foreground_element";
    public static final String HEIGHT = "height";
    public static final String ID = "id";
    public static final String LAYOUT_DIRECTION = "layout_direction";
    public static final String LEFT_MARGIN = "left_margin";
    public static final String LEFT_PADDING = "left_padding";
    public static final String LONG_CLICK_ENABLED = "long_click_enabled";
    public static final String MARGIN = "margin";
    public static final String MIN_HEIGHT = "min_height";
    public static final String MIN_WIDTH = "min_width";
    public static final String PADDING = "padding";
    public static final String PIVOT_X = "pivot_x";
    public static final String PIVOT_Y = "pivot_y";
    public static final String POSITION_X = "position_x";
    public static final String POSITION_Y = "position_y";
    public static final String RIGHT_MARGIN = "right_margin";
    public static final String RIGHT_PADDING = "right_padding";
    public static final String ROTATE = "rotate";
    public static final String SCALE_X = "scale_x";
    public static final String SCALE_Y = "scale_y";
    public static final String SCROLLBAR_BACKGROUND_COLOR = "scrollbar_background_color";
    public static final String SCROLLBAR_COLOR = "scrollbar_color";
    public static final String SCROLLBAR_FADING_DELAY = "scrollbar_fading_delay";
    public static final String SCROLLBAR_FADING_DURATION = "scrollbar_fading_duration";
    public static final String SCROLLBAR_FADING_ENABLED = "scrollbar_fading_enabled";
    public static final String SCROLLBAR_OVERLAP_ENABLED = "scrollbar_overlap_enabled";
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
                    c = ' ';
                    break;
                }
                c = 65535;
                break;
            case -1768527086:
                if (str.equals(FOCUSABLE_IN_TOUCH)) {
                    c = '\"';
                    break;
                }
                c = 65535;
                break;
            case -1737767820:
                if (str.equals(SCROLLBAR_FADING_ENABLED)) {
                    c = '#';
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
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case -1609594047:
                if (str.equals(ENABLED)) {
                    c = 31;
                    break;
                }
                c = 65535;
                break;
            case -1298124222:
                if (str.equals(BOTTOM_MARGIN)) {
                    c = 27;
                    break;
                }
                c = 65535;
                break;
            case -1221903360:
                if (str.equals(FOREGROUND_ELEMENT)) {
                    c = '4';
                    break;
                }
                c = 65535;
                break;
            case -1221029593:
                if (str.equals(HEIGHT)) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -1212277530:
                if (str.equals(LEFT_MARGIN)) {
                    c = 24;
                    break;
                }
                c = 65535;
                break;
            case -1081309778:
                if (str.equals(MARGIN)) {
                    c = 23;
                    break;
                }
                c = 65535;
                break;
            case -925180581:
                if (str.equals(ROTATE)) {
                    c = LocaleUtility.IETF_SEPARATOR;
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
            case -832279622:
                if (str.equals(COMPONENT_DESCRIPTION)) {
                    c = '&';
                    break;
                }
                c = 65535;
                break;
            case -831289384:
                if (str.equals(TOP_MARGIN)) {
                    c = 25;
                    break;
                }
                c = 65535;
                break;
            case -826243148:
                if (str.equals("min_height")) {
                    c = CharInfo.S_CARRIAGERETURN;
                    break;
                }
                c = 65535;
                break;
            case -806339567:
                if (str.equals(PADDING)) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -791592328:
                if (str.equals(DirectionalLayoutAttrsConstants.LayoutParamsAttrsConstants.LAYOUT_WEIGHT)) {
                    c = '*';
                    break;
                }
                c = 65535;
                break;
            case -571372583:
                if (str.equals(LEFT_PADDING)) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case -560345157:
                if (str.equals(PIVOT_X)) {
                    c = '+';
                    break;
                }
                c = 65535;
                break;
            case -560345156:
                if (str.equals(PIVOT_Y)) {
                    c = ',';
                    break;
                }
                c = 65535;
                break;
            case -428775647:
                if (str.equals(SCROLLBAR_THUMB_COLOR)) {
                    c = '5';
                    break;
                }
                c = 65535;
                break;
            case -283203043:
                if (str.equals(SCROLLBAR_START_ANGLE)) {
                    c = '(';
                    break;
                }
                c = 65535;
                break;
            case -211977717:
                if (str.equals(SCROLLBAR_BACKGROUND_COLOR)) {
                    c = '6';
                    break;
                }
                c = 65535;
                break;
            case -171938773:
                if (str.equals(START_MARGIN)) {
                    c = 28;
                    break;
                }
                c = 65535;
                break;
            case -142573817:
                if (str.equals(LONG_CLICK_ENABLED)) {
                    c = '!';
                    break;
                }
                c = 65535;
                break;
            case -108337158:
                if (str.equals(FOCUS_BORDER_WIDTH)) {
                    c = 21;
                    break;
                }
                c = 65535;
                break;
            case -50044755:
                if (str.equals(END_PADDING)) {
                    c = 20;
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
                    c = ')';
                    break;
                }
                c = 65535;
                break;
            case 92909918:
                if (str.equals(ALPHA)) {
                    c = PatternTokenizer.SINGLE_QUOTE;
                    break;
                }
                c = 65535;
                break;
            case 113126854:
                if (str.equals(WIDTH)) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 190012818:
                if (str.equals(END_MARGIN)) {
                    c = 29;
                    break;
                }
                c = 65535;
                break;
            case 261212398:
                if (str.equals(RIGHT_PADDING)) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case 303336935:
                if (str.equals(FOREGROUND_ALIGNMENT)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 410881840:
                if (str.equals(SCROLLBAR_OVERLAP_ENABLED)) {
                    c = '$';
                    break;
                }
                c = 65535;
                break;
            case 425701839:
                if (str.equals(FOCUS_BORDER_ENABLE)) {
                    c = '%';
                    break;
                }
                c = 65535;
                break;
            case 523373803:
                if (str.equals(BACKGROUND_ELEMENT)) {
                    c = '3';
                    break;
                }
                c = 65535;
                break;
            case 541503897:
                if (str.equals("min_width")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 664278634:
                if (str.equals(TRANSLATION_X)) {
                    c = '0';
                    break;
                }
                c = 65535;
                break;
            case 664278635:
                if (str.equals(TRANSLATION_Y)) {
                    c = '1';
                    break;
                }
                c = 65535;
                break;
            case 785971422:
                if (str.equals(FOCUS_BORDER_RADIUS)) {
                    c = '2';
                    break;
                }
                c = 65535;
                break;
            case 1062347261:
                if (str.equals(BOTTOM_PADDING)) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case 1115109701:
                if (str.equals(FOCUS_BORDER_PADDING)) {
                    c = 22;
                    break;
                }
                c = 65535;
                break;
            case 1165820059:
                if (str.equals(SCROLLBAR_THICKNESS)) {
                    c = 30;
                    break;
                }
                c = 65535;
                break;
            case 1381039842:
                if (str.equals(POSITION_X)) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 1381039843:
                if (str.equals(POSITION_Y)) {
                    c = '\t';
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
                    c = 19;
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
            case 1858284302:
                if (str.equals("layout_alignment")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1910893251:
                if (str.equals(SCALE_X)) {
                    c = '.';
                    break;
                }
                c = 65535;
                break;
            case 1910893252:
                if (str.equals(SCALE_Y)) {
                    c = '/';
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
            case 1996153866:
                if (str.equals(SCROLLBAR_COLOR)) {
                    c = '7';
                    break;
                }
                c = 65535;
                break;
            case 2001168689:
                if (str.equals(RIGHT_MARGIN)) {
                    c = 26;
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
            case 27:
            case 28:
            case 29:
            case 30:
                return Attr.AttrType.DIMENSION;
            case 31:
            case ' ':
            case '!':
            case '\"':
            case '#':
            case '$':
            case '%':
                return Attr.AttrType.BOOLEAN;
            case '&':
                return Attr.AttrType.STRING;
            case '\'':
            case '(':
            case ')':
            case '*':
            case '+':
            case ',':
            case '-':
            case '.':
            case '/':
            case '0':
            case '1':
            case '2':
                return Attr.AttrType.FLOAT;
            case '3':
            case '4':
                return Attr.AttrType.ELEMENT;
            case '5':
            case '6':
            case '7':
                return Attr.AttrType.COLOR;
            default:
                return Attr.AttrType.NONE;
        }
    }
}
