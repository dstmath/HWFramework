package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;
import ohos.com.sun.org.apache.xml.internal.serializer.CharInfo;

public class DependentLayoutAttrsConstants extends ViewAttrsConstants {
    public static final String ALIGNMENT = "alignment";

    public static class LayoutConfigAttrsConstants {
        public static final String ABOVE = "above";
        public static final String ALIGN_BASELINE = "align_baseline";
        public static final String ALIGN_BOTTOM = "align_bottom";
        public static final String ALIGN_END = "align_end";
        public static final String ALIGN_LEFT = "align_left";
        public static final String ALIGN_PARENT_BOTTOM = "align_parent_bottom";
        public static final String ALIGN_PARENT_END = "align_parent_end";
        public static final String ALIGN_PARENT_LEFT = "align_parent_left";
        public static final String ALIGN_PARENT_RIGHT = "align_parent_right";
        public static final String ALIGN_PARENT_START = "align_parent_start";
        public static final String ALIGN_PARENT_TOP = "align_parent_top";
        public static final String ALIGN_RIGHT = "align_right";
        public static final String ALIGN_START = "align_start";
        public static final String ALIGN_TOP = "align_top";
        public static final String BELOW = "below";
        public static final String CENTER_IN_PARENT = "center_in_parent";
        public static final String END_OF = "end_of";
        public static final String HORIZONTAL_CENTER = "horizontal_center";
        public static final String LEFT_OF = "left_of";
        public static final String RIGHT_OF = "right_of";
        public static final String START_OF = "start_of";
        public static final String VERTICAL_CENTER = "vertical_center";
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -2043543387:
                if (str.equals(LayoutConfigAttrsConstants.ALIGN_BOTTOM)) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -1497238118:
                if (str.equals(LayoutConfigAttrsConstants.CENTER_IN_PARENT)) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case -1436079398:
                if (str.equals(LayoutConfigAttrsConstants.RIGHT_OF)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1298761797:
                if (str.equals(LayoutConfigAttrsConstants.END_OF)) {
                    c = CharInfo.S_CARRIAGERETURN;
                    break;
                }
                c = 65535;
                break;
            case -1043178239:
                if (str.equals(LayoutConfigAttrsConstants.ALIGN_LEFT)) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -627861953:
                if (str.equals(LayoutConfigAttrsConstants.ALIGN_BASELINE)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 55443727:
                if (str.equals(LayoutConfigAttrsConstants.LEFT_OF)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 92611485:
                if (str.equals(LayoutConfigAttrsConstants.ABOVE)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 93621297:
                if (str.equals(LayoutConfigAttrsConstants.BELOW)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 148488258:
                if (str.equals(LayoutConfigAttrsConstants.ALIGN_PARENT_LEFT)) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case 313829697:
                if (str.equals(LayoutConfigAttrsConstants.ALIGN_PARENT_RIGHT)) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case 315075463:
                if (str.equals(LayoutConfigAttrsConstants.ALIGN_PARENT_START)) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case 364851518:
                if (str.equals(LayoutConfigAttrsConstants.VERTICAL_CENTER)) {
                    c = 22;
                    break;
                }
                c = 65535;
                break;
            case 686659494:
                if (str.equals(LayoutConfigAttrsConstants.ALIGN_PARENT_BOTTOM)) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case 939658448:
                if (str.equals(LayoutConfigAttrsConstants.HORIZONTAL_CENTER)) {
                    c = 21;
                    break;
                }
                c = 65535;
                break;
            case 1316797140:
                if (str.equals(LayoutConfigAttrsConstants.START_OF)) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 1767457953:
                if (str.equals(LayoutConfigAttrsConstants.ALIGN_END)) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 1767472411:
                if (str.equals(LayoutConfigAttrsConstants.ALIGN_TOP)) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1767875043:
                if (str.equals("alignment")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 2026873954:
                if (str.equals(LayoutConfigAttrsConstants.ALIGN_RIGHT)) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 2028119720:
                if (str.equals(LayoutConfigAttrsConstants.ALIGN_START)) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 2082993472:
                if (str.equals(LayoutConfigAttrsConstants.ALIGN_PARENT_END)) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case 2083007930:
                if (str.equals(LayoutConfigAttrsConstants.ALIGN_PARENT_TOP)) {
                    c = 15;
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
            case 19:
            case 20:
            case 21:
            case 22:
                return Attr.AttrType.BOOLEAN;
            default:
                return super.getType(str);
        }
    }
}
