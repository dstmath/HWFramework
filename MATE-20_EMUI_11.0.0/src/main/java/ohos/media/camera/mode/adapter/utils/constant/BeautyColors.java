package ohos.media.camera.mode.adapter.utils.constant;

public enum BeautyColors {
    COLORS_NONE(-1),
    COLORS_RGB_BF986C(0),
    COLORS_RGB_E9BB97(1),
    COLORS_RGB_EDCDA3(2),
    COLORS_RGB_F7D7B3(3),
    COLORS_RGB_FEE6CF(4),
    COLORS_RGB_F7E2D1(5),
    COLORS_RGB_FCDEDD(6),
    COLORS_RG_F6CBCF(7);
    
    private final int beautyColorValue;

    private BeautyColors(int i) {
        this.beautyColorValue = i;
    }

    public int getBeautyColorValue() {
        return this.beautyColorValue;
    }
}
