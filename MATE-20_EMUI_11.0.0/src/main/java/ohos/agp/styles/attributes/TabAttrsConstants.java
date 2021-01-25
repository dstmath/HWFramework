package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class TabAttrsConstants extends TextViewAttrsConstants {
    public static final String TAB_ICON = "tab_icon";

    @Override // ohos.agp.styles.attributes.TextViewAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        if (((str.hashCode() == -907302173 && str.equals(TAB_ICON)) ? (char) 0 : 65535) != 0) {
            return super.getType(str);
        }
        return Attr.AttrType.ELEMENT;
    }
}
