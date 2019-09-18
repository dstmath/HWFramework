package libcore.util;

public class HexEncoding {
    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    private HexEncoding() {
    }

    public static char[] encode(byte[] data) {
        return encode(data, 0, data.length);
    }

    public static char[] encode(byte[] data, int offset, int len) {
        char[] result = new char[(len * 2)];
        for (int i = 0; i < len; i++) {
            byte b = data[offset + i];
            int resultIndex = 2 * i;
            result[resultIndex] = HEX_DIGITS[(b >>> 4) & 15];
            result[resultIndex + 1] = HEX_DIGITS[b & 15];
        }
        return result;
    }

    public static String encodeToString(byte[] data) {
        return new String(encode(data));
    }

    public static byte[] decode(String encoded) throws IllegalArgumentException {
        return decode(encoded.toCharArray());
    }

    public static byte[] decode(String encoded, boolean allowSingleChar) throws IllegalArgumentException {
        return decode(encoded.toCharArray(), allowSingleChar);
    }

    public static byte[] decode(char[] encoded) throws IllegalArgumentException {
        return decode(encoded, false);
    }

    public static byte[] decode(char[] encoded, boolean allowSingleChar) throws IllegalArgumentException {
        byte[] result = new byte[((encoded.length + 1) / 2)];
        int resultOffset = 0;
        int i = 0;
        if (allowSingleChar) {
            if (encoded.length % 2 != 0) {
                result[0] = (byte) toDigit(encoded, 0);
                i = 0 + 1;
                resultOffset = 0 + 1;
            }
        } else if (encoded.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid input length: " + encoded.length);
        }
        int len = encoded.length;
        while (i < len) {
            result[resultOffset] = (byte) ((toDigit(encoded, i) << 4) | toDigit(encoded, i + 1));
            i += 2;
            resultOffset++;
        }
        return result;
    }

    private static int toDigit(char[] str, int offset) throws IllegalArgumentException {
        char pseudoCodePoint = str[offset];
        if ('0' <= pseudoCodePoint && pseudoCodePoint <= '9') {
            return pseudoCodePoint - '0';
        }
        if ('a' <= pseudoCodePoint && pseudoCodePoint <= 'f') {
            return 10 + (pseudoCodePoint - 'a');
        }
        if ('A' <= pseudoCodePoint && pseudoCodePoint <= 'F') {
            return 10 + (pseudoCodePoint - 'A');
        }
        throw new IllegalArgumentException("Illegal char: " + str[offset] + " at offset " + offset);
    }
}
