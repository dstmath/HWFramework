package android_maps_conflict_avoidance.com.google.common.util;

public final class MathUtil {
    private static final byte[] sinArray = new byte[]{(byte) 0, (byte) 4, (byte) 9, (byte) 13, (byte) 17, (byte) 22, (byte) 26, (byte) 30, (byte) 35, (byte) 39, (byte) 43, (byte) 48, (byte) 52, (byte) 56, (byte) 60, (byte) 65, (byte) 69, (byte) 73, (byte) 77, (byte) 81, (byte) 86, (byte) 90, (byte) 94, (byte) 98, (byte) 102, (byte) 106, (byte) 110, (byte) 113, (byte) 117, (byte) 121, (byte) 125, (byte) -127, (byte) -124, (byte) -120, (byte) -116, (byte) -113, (byte) -109, (byte) -106, (byte) -102, (byte) -99, (byte) -95, (byte) -92, (byte) -89, (byte) -86, (byte) -82, (byte) -79, (byte) -76, (byte) -73, (byte) -70, (byte) -67, (byte) -64, (byte) -62, (byte) -59, (byte) -56, (byte) -54, (byte) -51, (byte) -49, (byte) -46, (byte) -44, (byte) -42, (byte) -39, (byte) -37, (byte) -35, (byte) -33, (byte) -31, (byte) -29, (byte) -28, (byte) -26, (byte) -24, (byte) -23, (byte) -21, (byte) -20, (byte) -18, (byte) -17, (byte) -16, (byte) -15, (byte) -13, (byte) -12, (byte) -11, (byte) -11, (byte) -10, (byte) -9, (byte) -8, (byte) -8, (byte) -7, (byte) -7, (byte) -7, (byte) -6, (byte) -6, (byte) -6, (byte) -6};

    public static int ceiledDivision(int dividend, int divisor) {
        if (dividend < 0) {
            return dividend / divisor;
        }
        return ((dividend + divisor) - 1) / divisor;
    }
}
