package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class AbsSeekBarAttrsConstants extends ProgressBarAttrsConstants {
    public static final String THUMB_ELEMENT = "thumb_element";

    @Override // ohos.agp.styles.attributes.ProgressBarAttrsConstants, ohos.agp.styles.attributes.ScrollViewAttrsConstants, ohos.agp.styles.attributes.StackLayoutAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        if (((str.hashCode() == 788152723 && str.equals("thumb_element")) ? (char) 0 : 65535) != 0) {
            return super.getType(str);
        }
        return Attr.AttrType.ELEMENT;
    }
}
