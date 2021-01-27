package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class ListContainerAttrsConstants extends ViewAttrsConstants {
    public static final String BOUNDARY = "boundary";
    public static final String BOUNDARY_COLOR = "boundary_color";
    public static final String BOUNDARY_SWITCH = "boundary_switch";
    public static final String BOUNDARY_THICKNESS = "boundary_thickness";
    public static final String FOOTER_BOUNDARY_SWITCH = "footer_boundary_switch";
    public static final String HEADER_BOUNDARY_SWITCH = "header_boundary_switch";
    public static final String ORIENTATION = "orientation";
    public static final String REBOUND_EFFECT = "rebound_effect";
    public static final String SHADER_COLOR = "shader_color";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -2115311574:
                if (str.equals(BOUNDARY)) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -1637496183:
                if (str.equals("shader_color")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -1439500848:
                if (str.equals("orientation")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -1071567457:
                if (str.equals(BOUNDARY_THICKNESS)) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -919822779:
                if (str.equals(FOOTER_BOUNDARY_SWITCH)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -759257289:
                if (str.equals(HEADER_BOUNDARY_SWITCH)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -442092786:
                if (str.equals(BOUNDARY_COLOR)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -354604823:
                if (str.equals(BOUNDARY_SWITCH)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1915014437:
                if (str.equals("rebound_effect")) {
                    c = 0;
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
                return Attr.AttrType.BOOLEAN;
            case 4:
            case 5:
                return Attr.AttrType.COLOR;
            case 6:
                return Attr.AttrType.INT;
            case 7:
                return Attr.AttrType.DIMENSION;
            case '\b':
                return Attr.AttrType.ELEMENT;
            default:
                return super.getType(str);
        }
    }
}
