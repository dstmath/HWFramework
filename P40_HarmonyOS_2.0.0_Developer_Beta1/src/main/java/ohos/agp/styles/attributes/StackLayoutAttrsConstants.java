package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class StackLayoutAttrsConstants extends ViewAttrsConstants {
    public static final String MEASURE_ALL_CHILDREN = "measure_all_children";

    public static class LayoutParamsAttrsConstants {
        public static final String LAYOUT_ALIGNMENT = "layout_alignment";
    }

    @Override // ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        if (((str.hashCode() == 1749829662 && str.equals(MEASURE_ALL_CHILDREN)) ? (char) 0 : 65535) != 0) {
            return super.getType(str);
        }
        return Attr.AttrType.BOOLEAN;
    }
}
