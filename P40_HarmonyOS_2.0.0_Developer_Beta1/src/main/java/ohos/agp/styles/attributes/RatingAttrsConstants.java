package ohos.agp.styles.attributes;

import ohos.agp.components.Attr;

public class RatingAttrsConstants extends AbsSliderAttrsConstants {
    public static final String GRAIN_SIZE = "grain_size";
    public static final String IS_OPERABLE = "operable";
    public static final String RATING_ITEMS_NUMBER = "rating_items";
    public static final String SCORE = "score";

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // ohos.agp.styles.attributes.AbsSeekBarAttrsConstants, ohos.agp.styles.attributes.ProgressBarAttrsConstants, ohos.agp.styles.attributes.ScrollViewAttrsConstants, ohos.agp.styles.attributes.StackLayoutAttrsConstants, ohos.agp.styles.attributes.ViewAttrsConstants
    public Attr.AttrType getType(String str) {
        char c;
        switch (str.hashCode()) {
            case -500570968:
                if (str.equals(IS_OPERABLE)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -325479778:
                if (str.equals(RATING_ITEMS_NUMBER)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 109264530:
                if (str.equals(SCORE)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 535555397:
                if (str.equals(GRAIN_SIZE)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            return Attr.AttrType.INT;
        }
        if (c == 1) {
            return Attr.AttrType.BOOLEAN;
        }
        if (c == 2 || c == 3) {
            return Attr.AttrType.FLOAT;
        }
        return super.getType(str);
    }
}
