package org.apache.commons.codec.binary;

import org.apache.commons.codec.BinaryDecoder;
import org.apache.commons.codec.BinaryEncoder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

@Deprecated
public class Hex implements BinaryEncoder, BinaryDecoder {
    private static final char[] DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static byte[] decodeHex(char[] data) throws DecoderException {
        int len = data.length;
        if ((len & 1) != 0) {
            throw new DecoderException("Odd number of characters.");
        }
        byte[] out = new byte[(len >> 1)];
        int i = 0;
        int j = 0;
        while (j < len) {
            j++;
            j++;
            out[i] = (byte) (((toDigit(data[j], j) << 4) | toDigit(data[j], j)) & 255);
            i++;
        }
        return out;
    }

    protected static int toDigit(char ch, int index) throws DecoderException {
        int digit = Character.digit(ch, 16);
        if (digit != -1) {
            return digit;
        }
        throw new DecoderException("Illegal hexadecimal charcter " + ch + " at index " + index);
    }

    public static char[] encodeHex(byte[] data) {
        int l = data.length;
        char[] out = new char[(l << 1)];
        int j = 0;
        for (int i = 0; i < l; i++) {
            int i2 = j + 1;
            out[j] = DIGITS[(data[i] & 240) >>> 4];
            j = i2 + 1;
            out[i2] = DIGITS[data[i] & 15];
        }
        return out;
    }

    public byte[] decode(byte[] array) throws DecoderException {
        return decodeHex(new String(array).toCharArray());
    }

    public Object decode(Object object) throws DecoderException {
        try {
            return decodeHex(object instanceof String ? ((String) object).toCharArray() : (char[]) object);
        } catch (ClassCastException e) {
            throw new DecoderException(e.getMessage());
        }
    }

    public byte[] encode(byte[] array) {
        return new String(encodeHex(array)).getBytes();
    }

    public Object encode(Object object) throws EncoderException {
        try {
            return encodeHex(object instanceof String ? ((String) object).getBytes() : (byte[]) object);
        } catch (ClassCastException e) {
            throw new EncoderException(e.getMessage());
        }
    }
}
