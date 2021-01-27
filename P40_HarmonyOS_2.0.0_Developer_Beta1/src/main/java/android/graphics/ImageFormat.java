package android.graphics;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ImageFormat {
    public static final int DEPTH16 = 1144402265;
    public static final int DEPTH_JPEG = 1768253795;
    public static final int DEPTH_POINT_CLOUD = 257;
    public static final int FLEX_RGBA_8888 = 42;
    public static final int FLEX_RGB_888 = 41;
    public static final int H264 = 40961;
    public static final int H265 = 40962;
    public static final int HEIC = 1212500294;
    public static final int JPEG = 256;
    public static final int NV16 = 16;
    public static final int NV21 = 17;
    public static final int PRIVATE = 34;
    public static final int RAW10 = 37;
    public static final int RAW12 = 38;
    public static final int RAW_DEPTH = 4098;
    public static final int RAW_PRIVATE = 36;
    public static final int RAW_SENSOR = 32;
    public static final int RGB_565 = 4;
    public static final int UNKNOWN = 0;
    public static final int Y16 = 540422489;
    public static final int Y8 = 538982489;
    public static final int YUV_420_888 = 35;
    public static final int YUV_422_888 = 39;
    public static final int YUV_444_888 = 40;
    public static final int YUY2 = 20;
    public static final int YV12 = 842094169;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Format {
    }

    public static int getBitsPerPixel(int format) {
        if (!(format == 4 || format == 20 || format == 32)) {
            if (format == 35) {
                return 12;
            }
            if (format != 4098) {
                if (format == 538982489) {
                    return 8;
                }
                if (format != 540422489) {
                    if (format == 842094169) {
                        return 12;
                    }
                    if (format == 1144402265 || format == 16) {
                        return 16;
                    }
                    if (format == 17) {
                        return 12;
                    }
                    switch (format) {
                        case 37:
                            return 10;
                        case 38:
                            return 12;
                        case 39:
                            return 16;
                        case 40:
                            return 24;
                        case 41:
                            return 24;
                        case 42:
                            return 32;
                        default:
                            return -1;
                    }
                }
                return 16;
            }
        }
        return 16;
    }

    public static boolean isPublicFormat(int format) {
        if (format == 16 || format == 17 || format == 256 || format == 257) {
            return true;
        }
        switch (format) {
            case 4:
            case 20:
            case 32:
            case 4098:
            case Y8 /* 538982489 */:
            case YV12 /* 842094169 */:
            case DEPTH16 /* 1144402265 */:
            case HEIC /* 1212500294 */:
            case DEPTH_JPEG /* 1768253795 */:
                return true;
            default:
                switch (format) {
                    case 34:
                    case 35:
                    case 36:
                    case 37:
                    case 38:
                    case 39:
                    case 40:
                    case 41:
                    case 42:
                        return true;
                    default:
                        switch (format) {
                            case H264 /* 40961 */:
                            case H265 /* 40962 */:
                                return true;
                            default:
                                return false;
                        }
                }
        }
    }
}
