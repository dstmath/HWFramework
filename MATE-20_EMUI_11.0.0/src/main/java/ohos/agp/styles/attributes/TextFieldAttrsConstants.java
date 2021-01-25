package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class TextFieldAttrsConstants extends TextViewAttrsConstants {
    public static final String BASEMENT = "basement";

    @Override // ohos.agp.styles.attributes.TextViewAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        if (str.equals(BASEMENT)) {
            return Attr.AttrType.ELEMENT;
        }
        return super.getType(str);
    }
}
