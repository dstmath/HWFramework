package ohos.media.image.common;

public enum ColorSpace {
    UNKNOWN(0),
    DISPLAY_P3(1),
    SRGB(2),
    LINEAR_SRGB(3),
    EXTENDED_SRGB(4),
    LINEAR_EXTENDED_SRGB(5),
    GENERIC_XYZ(6),
    GENERIC_LAB(7),
    ACES(8),
    ACES_CG(9),
    ADOBE_RGB_1998(10),
    DCI_P3(11),
    ITU_709(12),
    ITU_2020(13),
    ROMM_RGB(14),
    NTSC_1953(15),
    SMPTE_C(16);
    
    private final int colorValue;

    private ColorSpace(int i) {
        this.colorValue = i;
    }

    public int getValue() {
        return this.colorValue;
    }
}
