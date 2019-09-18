package android.graphics;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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

    @Retention(RetentionPolicy.SOURCE)
    public @interface Format {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Opacity {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x003e, code lost:
        r5.bitsPerPixel = 16;
        r5.bytesPerPixel = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        return;
     */
    public static void getPixelFormatInfo(int format, PixelFormat info) {
        if (format != 20) {
            if (format != 22) {
                if (format != 43) {
                    switch (format) {
                        case 1:
                        case 2:
                            break;
                        case 3:
                            info.bitsPerPixel = 24;
                            info.bytesPerPixel = 3;
                            return;
                        case 4:
                            break;
                        default:
                            switch (format) {
                                case 6:
                                case 7:
                                case 10:
                                    break;
                                case 8:
                                case 9:
                                case 11:
                                    info.bitsPerPixel = 8;
                                    info.bytesPerPixel = 1;
                                    return;
                                default:
                                    switch (format) {
                                        case 16:
                                            break;
                                        case 17:
                                            info.bitsPerPixel = 12;
                                            info.bytesPerPixel = 1;
                                            return;
                                        default:
                                            throw new IllegalArgumentException("unknown pixel format " + format);
                                    }
                            }
                    }
                }
                info.bitsPerPixel = 32;
                info.bytesPerPixel = 4;
                return;
            }
            info.bitsPerPixel = 64;
            info.bytesPerPixel = 8;
            return;
        }
        info.bitsPerPixel = 16;
        info.bytesPerPixel = 1;
    }

    public static boolean formatHasAlpha(int format) {
        if (!(format == 1 || format == 10 || format == 22 || format == 43)) {
            switch (format) {
                case -3:
                case -2:
                    break;
                default:
                    switch (format) {
                        case 6:
                        case 7:
                        case 8:
                            break;
                        default:
                            return false;
                    }
            }
        }
        return true;
    }

    public static boolean isPublicFormat(int format) {
        if (!(format == 22 || format == 43)) {
            switch (format) {
                case 1:
                case 2:
                case 3:
                case 4:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public static String formatToString(int format) {
        if (format == 20) {
            return "YCbCr_422_I";
        }
        if (format == 22) {
            return "RGBA_F16";
        }
        if (format == 43) {
            return "RGBA_1010102";
        }
        if (format == 256) {
            return "JPEG";
        }
        switch (format) {
            case -3:
                return "TRANSLUCENT";
            case -2:
                return "TRANSPARENT";
            default:
                switch (format) {
                    case 0:
                        return "UNKNOWN";
                    case 1:
                        return "RGBA_8888";
                    case 2:
                        return "RGBX_8888";
                    case 3:
                        return "RGB_888";
                    case 4:
                        return "RGB_565";
                    default:
                        switch (format) {
                            case 6:
                                return "RGBA_5551";
                            case 7:
                                return "RGBA_4444";
                            case 8:
                                return "A_8";
                            case 9:
                                return "L_8";
                            case 10:
                                return "LA_88";
                            case 11:
                                return "RGB_332";
                            default:
                                switch (format) {
                                    case 16:
                                        return "YCbCr_422_SP";
                                    case 17:
                                        return "YCbCr_420_SP";
                                    default:
                                        return Integer.toString(format);
                                }
                        }
                }
        }
    }
}
