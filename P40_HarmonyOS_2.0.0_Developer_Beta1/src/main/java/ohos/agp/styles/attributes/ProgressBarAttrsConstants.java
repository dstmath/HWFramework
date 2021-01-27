package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;
import ohos.com.sun.org.apache.xml.internal.serializer.CharInfo;

public class ProgressBarAttrsConstants extends ScrollViewAttrsConstants {
    public static final String BACKGROUND_INSTRUCT_ELEMENT = "background_instruct_element";
    public static final String DIVIDER_LINES_ENABLED = "divider_lines_enabled";
    public static final String DIVIDER_LINES_NUMBER = "divider_lines_number";
    public static final String INFINITE = "infinite";
    public static final String INFINITE_ELEMENT = "infinite_element";
    public static final String MAX = "max";
    public static final String MAX_HEIGHT = "max_height";
    public static final String MAX_WIDTH = "max_width";
    public static final String MIN = "min";
    public static final String ORIENTATION = "orientation";
    public static final String PROGRESS = "progress";
    public static final String PROGRESS_COLOR = "progress_color";
    public static final String PROGRESS_ELEMENT = "progress_element";
    public static final String PROGRESS_HINT_TEXT = "progress_hint_text";
    public static final String PROGRESS_HINT_TEXT_ALIGNMENT = "progress_hint_text_alignment";
    public static final String PROGRESS_HINT_TEXT_COLOR = "progress_hint_text_color";
    public static final String PROGRESS_HINT_TEXT_SIZE = "progress_hint_text_size";
    public static final String PROGRESS_WIDTH = "progress_width";
    public static final String STEP = "step";
    public static final String VICE_PROGRESS = "vice_progress";
    public static final String VICE_PROGRESS_ELEMENT = "vice_progress_element";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.ScrollViewAttrsConstants, ohos.agp.styles.attributes.StackLayoutAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -1725360293:
                if (str.equals(DIVIDER_LINES_ENABLED)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1569243439:
                if (str.equals(PROGRESS_COLOR)) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case -1550959308:
                if (str.equals(PROGRESS_WIDTH)) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case -1439500848:
                if (str.equals("orientation")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -1391207471:
                if (str.equals(INFINITE_ELEMENT)) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case -1001078227:
                if (str.equals("progress")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -681146153:
                if (str.equals(VICE_PROGRESS)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -678064019:
                if (str.equals(PROGRESS_HINT_TEXT_SIZE)) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case -340278189:
                if (str.equals(PROGRESS_HINT_TEXT)) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -230974677:
                if (str.equals("max_width")) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case 107876:
                if (str.equals(MAX)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 108114:
                if (str.equals(MIN)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 3540684:
                if (str.equals(STEP)) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 70280175:
                if (str.equals(DIVIDER_LINES_NUMBER)) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 173173268:
                if (str.equals(INFINITE)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 440241271:
                if (str.equals(PROGRESS_HINT_TEXT_COLOR)) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 457869204:
                if (str.equals(VICE_PROGRESS_ELEMENT)) {
                    c = CharInfo.S_CARRIAGERETURN;
                    break;
                }
                c = 65535;
                break;
            case 961913832:
                if (str.equals(BACKGROUND_INSTRUCT_ELEMENT)) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 996724834:
                if (str.equals("max_height")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case 1173162730:
                if (str.equals(PROGRESS_ELEMENT)) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 1654215927:
                if (str.equals(PROGRESS_HINT_TEXT_ALIGNMENT)) {
                    c = 7;
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
            case 7:
            case '\b':
            case '\t':
                return Attr.AttrType.INT;
            case '\n':
            case 11:
            case '\f':
            case '\r':
                return Attr.AttrType.ELEMENT;
            case 14:
                return Attr.AttrType.STRING;
            case 15:
            case 16:
                return Attr.AttrType.COLOR;
            case 17:
            case 18:
            case 19:
            case 20:
                return Attr.AttrType.DIMENSION;
            default:
                return super.getType(str);
        }
    }
}
