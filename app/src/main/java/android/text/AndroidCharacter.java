package android.text;

public class AndroidCharacter {
    public static final int EAST_ASIAN_WIDTH_AMBIGUOUS = 1;
    public static final int EAST_ASIAN_WIDTH_FULL_WIDTH = 3;
    public static final int EAST_ASIAN_WIDTH_HALF_WIDTH = 2;
    public static final int EAST_ASIAN_WIDTH_NARROW = 4;
    public static final int EAST_ASIAN_WIDTH_NEUTRAL = 0;
    public static final int EAST_ASIAN_WIDTH_WIDE = 5;

    public static native void getDirectionalities(char[] cArr, byte[] bArr, int i);

    public static native int getEastAsianWidth(char c);

    public static native void getEastAsianWidths(char[] cArr, int i, int i2, byte[] bArr);

    public static native char getMirror(char c);

    public static native boolean mirror(char[] cArr, int i, int i2);
}
