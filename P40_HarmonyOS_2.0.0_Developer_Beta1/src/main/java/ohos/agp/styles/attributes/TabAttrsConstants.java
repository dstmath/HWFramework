package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class TabAttrsConstants extends TextAttrsConstants {
    public static final String TAB_ICON_ELEMENT = "tab_icon_element";

    @Override // ohos.agp.styles.attributes.TextAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        if (((str.hashCode() == -1027416672 && str.equals(TAB_ICON_ELEMENT)) ? (char) 0 : 65535) != 0) {
            return super.getType(str);
        }
        return Attr.AttrType.ELEMENT;
    }
}
