package android.util;

public final class ByteStringUtils {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

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
            chars[i * 2] = HEX_ARRAY[byteHex >>> 4];
            chars[(i * 2) + 1] = HEX_ARRAY[byteHex & 15];
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
        for (int i = 0; i < HEX_ARRAY.length; i++) {
            if (HEX_ARRAY[i] == c) {
                return i;
            }
        }
        return -1;
    }
}
