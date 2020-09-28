package android.util;

public final class ByteStringUtils {
    private static final char[] HEX_LOWERCASE_ARRAY = "0123456789abcdef".toCharArray();
    private static final char[] HEX_UPPERCASE_ARRAY = "0123456789ABCDEF".toCharArray();

    private ByteStringUtils() {
    }

    public static String toHexString(byte[] bytes) {
        if (bytes == null || bytes.length == 0 || bytes.length % 2 != 0) {
            return null;
        }
        int byteLength = bytes.length;
        char[] chars = new char[(byteLength * 2)];
        for (int i = 0; i < byteLength; i++) {
            int byteHex = bytes[i] & 255;
            char[] cArr = HEX_UPPERCASE_ARRAY;
            chars[i * 2] = cArr[byteHex >>> 4];
            chars[(i * 2) + 1] = cArr[byteHex & 15];
        }
        return new String(chars);
    }

    public static byte[] fromHexToByteArray(String str) {
        if (str == null || str.length() == 0 || str.length() % 2 != 0) {
            return null;
        }
        char[] chars = str.toCharArray();
        byte[] bytes = new byte[(chars.length / 2)];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (((getIndex(chars[i * 2]) << 4) & 240) | (getIndex(chars[(i * 2) + 1]) & 15));
        }
        return bytes;
    }

    private static int getIndex(char c) {
        int i = 0;
        while (true) {
            char[] cArr = HEX_UPPERCASE_ARRAY;
            if (i >= cArr.length) {
                return -1;
            }
            if (cArr[i] == c || HEX_LOWERCASE_ARRAY[i] == c) {
                return i;
            }
            i++;
        }
        return i;
    }
}
