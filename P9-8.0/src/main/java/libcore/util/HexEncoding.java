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
            int resultIndex = i * 2;
            result[resultIndex] = HEX_DIGITS[(b >>> 4) & 15];
            result[resultIndex + 1] = HEX_DIGITS[b & 15];
        }
        return result;
    }

    public static byte[] decode(char[] encoded, boolean allowSingleChar) throws IllegalArgumentException {
        byte[] result = new byte[((encoded.length + 1) / 2)];
        int resultOffset = 0;
        int i = 0;
        if (allowSingleChar) {
            if (encoded.length % 2 != 0) {
                resultOffset = 1;
                result[0] = (byte) toDigit(encoded, 0);
                i = 1;
            }
        } else if (encoded.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid input length: " + encoded.length);
        }
        int len = encoded.length;
        int resultOffset2 = resultOffset;
        while (i < len) {
            resultOffset = resultOffset2 + 1;
            result[resultOffset2] = (byte) ((toDigit(encoded, i) << 4) | toDigit(encoded, i + 1));
            i += 2;
            resultOffset2 = resultOffset;
        }
        return result;
    }

    private static int toDigit(char[] str, int offset) throws IllegalArgumentException {
        int pseudoCodePoint = str[offset];
        if (48 <= pseudoCodePoint && pseudoCodePoint <= 57) {
            return pseudoCodePoint - 48;
        }
        if (97 <= pseudoCodePoint && pseudoCodePoint <= 102) {
            return (pseudoCodePoint - 97) + 10;
        }
        if (65 <= pseudoCodePoint && pseudoCodePoint <= 70) {
            return (pseudoCodePoint - 65) + 10;
        }
        throw new IllegalArgumentException("Illegal char: " + str[offset] + " at offset " + offset);
    }
}
