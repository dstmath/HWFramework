package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class PageSliderIndicatorAttrsConstants extends ViewAttrsConstants {
    public static final String ITEM_OFFSET = "item_offset";
    public static final String NORMAL_ELEMENT = "normal_element";
    public static final String SELECTED_ELEMENT = "selected_element";

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0039 A[ADDED_TO_REGION] */
    @Override // ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        int hashCode = str.hashCode();
        if (hashCode != -1866617948) {
            if (hashCode != -509879489) {
                if (hashCode == 299232600 && str.equals(SELECTED_ELEMENT)) {
                    c = 1;
                    if (c != 0 || c == 1) {
                        return Attr.AttrType.ELEMENT;
                    }
                    if (c != 2) {
                        return super.getType(str);
                    }
                    return Attr.AttrType.DIMENSION;
                }
            } else if (str.equals(ITEM_OFFSET)) {
                c = 2;
                if (c != 0) {
                }
                return Attr.AttrType.ELEMENT;
            }
        } else if (str.equals(NORMAL_ELEMENT)) {
            c = 0;
            if (c != 0) {
            }
            return Attr.AttrType.ELEMENT;
        }
        c = 65535;
        if (c != 0) {
        }
        return Attr.AttrType.ELEMENT;
    }
}
