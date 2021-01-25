package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class ListContainerAttrsConstants extends ViewAttrsConstants {
    public static final String ORIENTATION = "orientation";
    public static final String REBOUND_EFFECT = "rebound_effect";
    public static final String SHADER_COLOR = "shader_color";

    /* JADX WARNING: Removed duplicated region for block: B:17:0x003a  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0049  */
    @Override // ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        int hashCode = str.hashCode();
        if (hashCode != -1637496183) {
            if (hashCode != -1439500848) {
                if (hashCode == 1915014437 && str.equals("rebound_effect")) {
                    c = 0;
                    if (c != 0) {
                        return Attr.AttrType.BOOLEAN;
                    }
                    if (c == 1) {
                        return Attr.AttrType.COLOR;
                    }
                    if (c != 2) {
                        return super.getType(str);
                    }
                    return Attr.AttrType.INT;
                }
            } else if (str.equals("orientation")) {
                c = 2;
                if (c != 0) {
                }
            }
        } else if (str.equals("shader_color")) {
            c = 1;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }
}
