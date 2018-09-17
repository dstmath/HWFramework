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
    public static final int RGBA_1010102 = 43;
    @Deprecated
    public static final int RGBA_4444 = 7;
    @Deprecated
    public static final int RGBA_5551 = 6;
    public static final int RGBA_8888 = 1;
    public static final int RGBA_F16 = 22;
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
            case 1:
            case 2:
            case 43:
                info.bitsPerPixel = 32;
                info.bytesPerPixel = 4;
                return;
            case 3:
                info.bitsPerPixel = 24;
                info.bytesPerPixel = 3;
                return;
            case 4:
            case 6:
            case 7:
            case 10:
                info.bitsPerPixel = 16;
                info.bytesPerPixel = 2;
                return;
            case 8:
            case 9:
            case 11:
                info.bitsPerPixel = 8;
                info.bytesPerPixel = 1;
                return;
            case 16:
            case 20:
                info.bitsPerPixel = 16;
                info.bytesPerPixel = 1;
                return;
            case 17:
                info.bitsPerPixel = 12;
                info.bytesPerPixel = 1;
                return;
            case 22:
                info.bitsPerPixel = 64;
                info.bytesPerPixel = 8;
                return;
            default:
                throw new IllegalArgumentException("unknown pixel format " + format);
        }
    }

    public static boolean formatHasAlpha(int format) {
        switch (format) {
            case -3:
            case -2:
            case 1:
            case 6:
            case 7:
            case 8:
            case 10:
            case 22:
            case 43:
                return true;
            default:
                return false;
        }
    }

    public static boolean isPublicFormat(int format) {
        switch (format) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 22:
            case 43:
                return true;
            default:
                return false;
        }
    }
}
