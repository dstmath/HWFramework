package android.graphics;

public class PixelFormat {
    @Deprecated
    public static final int A_8 = 8;
    @Deprecated
    public static final int JPEG = 256;
    @Deprecated
    public static final int LA_88 = 10;
    @Deprecated
    public static final int L_8 = 9;
    public static final int OPAQUE = -1;
    @Deprecated
    public static final int RGBA_4444 = 7;
    @Deprecated
    public static final int RGBA_5551 = 6;
    public static final int RGBA_8888 = 1;
    public static final int RGBX_8888 = 2;
    @Deprecated
    public static final int RGB_332 = 11;
    public static final int RGB_565 = 4;
    public static final int RGB_888 = 3;
    public static final int TRANSLUCENT = -3;
    public static final int TRANSPARENT = -2;
    public static final int UNKNOWN = 0;
    @Deprecated
    public static final int YCbCr_420_SP = 17;
    @Deprecated
    public static final int YCbCr_422_I = 20;
    @Deprecated
    public static final int YCbCr_422_SP = 16;
    public int bitsPerPixel;
    public int bytesPerPixel;

    public static void getPixelFormatInfo(int format, PixelFormat info) {
        switch (format) {
            case RGBA_8888 /*1*/:
            case RGBX_8888 /*2*/:
                info.bitsPerPixel = 32;
                info.bytesPerPixel = RGB_565;
            case RGB_888 /*3*/:
                info.bitsPerPixel = 24;
                info.bytesPerPixel = RGB_888;
            case RGB_565 /*4*/:
            case RGBA_5551 /*6*/:
            case RGBA_4444 /*7*/:
            case LA_88 /*10*/:
                info.bitsPerPixel = YCbCr_422_SP;
                info.bytesPerPixel = RGBX_8888;
            case A_8 /*8*/:
            case L_8 /*9*/:
            case RGB_332 /*11*/:
                info.bitsPerPixel = A_8;
                info.bytesPerPixel = RGBA_8888;
            case YCbCr_422_SP /*16*/:
            case YCbCr_422_I /*20*/:
                info.bitsPerPixel = YCbCr_422_SP;
                info.bytesPerPixel = RGBA_8888;
            case YCbCr_420_SP /*17*/:
                info.bitsPerPixel = 12;
                info.bytesPerPixel = RGBA_8888;
            default:
                throw new IllegalArgumentException("unknown pixel format " + format);
        }
    }

    public static boolean formatHasAlpha(int format) {
        switch (format) {
            case TRANSLUCENT /*-3*/:
            case TRANSPARENT /*-2*/:
            case RGBA_8888 /*1*/:
            case RGBA_5551 /*6*/:
            case RGBA_4444 /*7*/:
            case A_8 /*8*/:
            case LA_88 /*10*/:
                return true;
            default:
                return false;
        }
    }

    public static boolean isPublicFormat(int format) {
        switch (format) {
            case RGBA_8888 /*1*/:
            case RGBX_8888 /*2*/:
            case RGB_888 /*3*/:
            case RGB_565 /*4*/:
                return true;
            default:
                return false;
        }
    }
}
