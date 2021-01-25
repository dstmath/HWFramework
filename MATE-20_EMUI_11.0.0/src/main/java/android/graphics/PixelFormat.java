package android.graphics;

import com.android.internal.telephony.IccCardConstants;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PixelFormat {
    @Deprecated
    public static final int A_8 = 8;
    public static final int HSV_888 = 55;
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

    public static void getPixelFormatInfo(int format, PixelFormat info) {
        if (!(format == 1 || format == 2)) {
            if (format != 3) {
                if (format != 4) {
                    if (format != 16) {
                        if (format == 17) {
                            info.bitsPerPixel = 12;
                            info.bytesPerPixel = 1;
                            return;
                        } else if (format != 20) {
                            if (format == 22) {
                                info.bitsPerPixel = 64;
                                info.bytesPerPixel = 8;
                                return;
                            } else if (format != 43) {
                                if (format != 55) {
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
                                            throw new IllegalArgumentException("unknown pixel format " + format);
                                    }
                                }
                            }
                        }
                    }
                    info.bitsPerPixel = 16;
                    info.bytesPerPixel = 1;
                    return;
                }
                info.bitsPerPixel = 16;
                info.bytesPerPixel = 2;
                return;
            }
            info.bitsPerPixel = 24;
            info.bytesPerPixel = 3;
            return;
        }
        info.bitsPerPixel = 32;
        info.bytesPerPixel = 4;
    }

    public static boolean formatHasAlpha(int format) {
        return format == -3 || format == -2 || format == 1 || format == 10 || format == 22 || format == 43 || format == 6 || format == 7 || format == 8;
    }

    public static boolean isPublicFormat(int format) {
        if (format == 1 || format == 2 || format == 3 || format == 4 || format == 22 || format == 43) {
            return true;
        }
        return false;
    }

    public static String formatToString(int format) {
        if (format == -3) {
            return "TRANSLUCENT";
        }
        if (format == -2) {
            return "TRANSPARENT";
        }
        if (format == 0) {
            return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
        if (format == 1) {
            return "RGBA_8888";
        }
        if (format == 2) {
            return "RGBX_8888";
        }
        if (format == 3) {
            return "RGB_888";
        }
        if (format == 4) {
            return "RGB_565";
        }
        if (format == 16) {
            return "YCbCr_422_SP";
        }
        if (format == 17) {
            return "YCbCr_420_SP";
        }
        if (format == 20) {
            return "YCbCr_422_I";
        }
        if (format == 22) {
            return "RGBA_F16";
        }
        if (format == 43) {
            return "RGBA_1010102";
        }
        if (format == 55) {
            return "HSV_888";
        }
        if (format == 256) {
            return "JPEG";
        }
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
                return Integer.toString(format);
        }
    }
}
