package libcore.util;

public final class CharsetUtils {
    public static native void asciiBytesToChars(byte[] bArr, int i, int i2, char[] cArr);

    public static native void isoLatin1BytesToChars(byte[] bArr, int i, int i2, char[] cArr);

    public static native byte[] toAsciiBytes(String str, int i, int i2);

    public static native byte[] toIsoLatin1Bytes(String str, int i, int i2);

    public static native byte[] toUtf8Bytes(String str, int i, int i2);

    public static byte[] toBigEndianUtf16Bytes(String s, int offset, int length) {
        byte[] result = new byte[(length * 2)];
        int end = offset + length;
        int resultIndex = 0;
        for (int i = offset; i < end; i++) {
            char ch = s.charAt(i);
            int i2 = resultIndex + 1;
            result[resultIndex] = (byte) (ch >> 8);
            resultIndex = i2 + 1;
            result[i2] = (byte) ch;
        }
        return result;
    }

    private CharsetUtils() {
    }
}
