package org.apache.commons.codec.binary;

import org.apache.commons.codec.BinaryDecoder;
import org.apache.commons.codec.BinaryEncoder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

@Deprecated
public class BinaryCodec implements BinaryDecoder, BinaryEncoder {
    private static final int[] BITS = new int[]{1, 2, 4, BIT_3, 16, 32, BIT_6, BIT_7};
    private static final int BIT_0 = 1;
    private static final int BIT_1 = 2;
    private static final int BIT_2 = 4;
    private static final int BIT_3 = 8;
    private static final int BIT_4 = 16;
    private static final int BIT_5 = 32;
    private static final int BIT_6 = 64;
    private static final int BIT_7 = 128;
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];

    public byte[] encode(byte[] raw) {
        return toAsciiBytes(raw);
    }

    public Object encode(Object raw) throws EncoderException {
        if (raw instanceof byte[]) {
            return toAsciiChars((byte[]) raw);
        }
        throw new EncoderException("argument not a byte array");
    }

    public Object decode(Object ascii) throws DecoderException {
        if (ascii == null) {
            return EMPTY_BYTE_ARRAY;
        }
        if (ascii instanceof byte[]) {
            return fromAscii((byte[]) ascii);
        }
        if (ascii instanceof char[]) {
            return fromAscii((char[]) ascii);
        }
        if (ascii instanceof String) {
            return fromAscii(((String) ascii).toCharArray());
        }
        throw new DecoderException("argument not a byte array");
    }

    public byte[] decode(byte[] ascii) {
        return fromAscii(ascii);
    }

    public byte[] toByteArray(String ascii) {
        if (ascii == null) {
            return EMPTY_BYTE_ARRAY;
        }
        return fromAscii(ascii.toCharArray());
    }

    public static byte[] fromAscii(char[] ascii) {
        if (ascii == null || ascii.length == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] l_raw = new byte[(ascii.length >> 3)];
        int ii = 0;
        int jj = ascii.length - 1;
        while (ii < l_raw.length) {
            for (int bits = 0; bits < BITS.length; bits++) {
                if (ascii[jj - bits] == '1') {
                    l_raw[ii] = (byte) (l_raw[ii] | BITS[bits]);
                }
            }
            ii++;
            jj -= 8;
        }
        return l_raw;
    }

    public static byte[] fromAscii(byte[] ascii) {
        if (ascii == null || ascii.length == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] l_raw = new byte[(ascii.length >> 3)];
        int ii = 0;
        int jj = ascii.length - 1;
        while (ii < l_raw.length) {
            for (int bits = 0; bits < BITS.length; bits++) {
                if (ascii[jj - bits] == (byte) 49) {
                    l_raw[ii] = (byte) (l_raw[ii] | BITS[bits]);
                }
            }
            ii++;
            jj -= 8;
        }
        return l_raw;
    }

    public static byte[] toAsciiBytes(byte[] raw) {
        if (raw == null || raw.length == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] l_ascii = new byte[(raw.length << 3)];
        int ii = 0;
        int jj = l_ascii.length - 1;
        while (ii < raw.length) {
            for (int bits = 0; bits < BITS.length; bits++) {
                if ((raw[ii] & BITS[bits]) == 0) {
                    l_ascii[jj - bits] = (byte) 48;
                } else {
                    l_ascii[jj - bits] = (byte) 49;
                }
            }
            ii++;
            jj -= 8;
        }
        return l_ascii;
    }

    public static char[] toAsciiChars(byte[] raw) {
        if (raw == null || raw.length == 0) {
            return EMPTY_CHAR_ARRAY;
        }
        char[] l_ascii = new char[(raw.length << 3)];
        int ii = 0;
        int jj = l_ascii.length - 1;
        while (ii < raw.length) {
            for (int bits = 0; bits < BITS.length; bits++) {
                if ((raw[ii] & BITS[bits]) == 0) {
                    l_ascii[jj - bits] = '0';
                } else {
                    l_ascii[jj - bits] = '1';
                }
            }
            ii++;
            jj -= 8;
        }
        return l_ascii;
    }

    public static String toAsciiString(byte[] raw) {
        return new String(toAsciiChars(raw));
    }
}
