package android_maps_conflict_avoidance.com.google.common.util;

public class ConversionUtil {
    public static final int byteArrayToInt(byte[] b) {
        if (b.length == 4) {
            return ((((b[0] & 255) << 24) | ((b[1] & 255) << 16)) | ((b[2] & 255) << 8)) | (b[3] & 255);
        }
        throw new IllegalArgumentException("byte[] must be size 4, there are 4 bytes to an int");
    }
}
