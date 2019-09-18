package java.nio.charset;

import android.icu.text.Bidi;
import java.io.UTFDataFormatException;

public class ModifiedUtf8 {
    public static long countBytes(String s, boolean shortLength) throws UTFDataFormatException {
        long counter = 0;
        int strLen = s.length();
        for (int i = 0; i < strLen; i++) {
            char c = s.charAt(i);
            if (c < 128) {
                counter++;
                if (c == 0) {
                    counter++;
                }
            } else if (c < 2048) {
                counter += 2;
            } else {
                counter += 3;
            }
        }
        if (!shortLength || counter <= 65535) {
            return counter;
        }
        throw new UTFDataFormatException("Size of the encoded string doesn't fit in two bytes");
    }

    public static void encode(byte[] dst, int offset, String s) {
        int strLen = s.length();
        for (int i = 0; i < strLen; i++) {
            char c = s.charAt(i);
            if (c < 128) {
                if (c == 0) {
                    int offset2 = offset + 1;
                    dst[offset] = -64;
                    offset = offset2 + 1;
                    dst[offset2] = Bidi.LEVEL_OVERRIDE;
                } else {
                    dst[offset] = (byte) c;
                    offset++;
                }
            } else if (c < 2048) {
                int offset3 = offset + 1;
                dst[offset] = (byte) ((c >>> 6) | 192);
                offset = offset3 + 1;
                dst[offset3] = (byte) (128 | (c & '?'));
            } else {
                int offset4 = offset + 1;
                dst[offset] = (byte) ((c >>> 12) | 224);
                int offset5 = offset4 + 1;
                dst[offset4] = (byte) (((c >>> 6) & 63) | 128);
                dst[offset5] = (byte) (128 | (c & '?'));
                offset = offset5 + 1;
            }
        }
    }

    public static byte[] encode(String s) throws UTFDataFormatException {
        long size = countBytes(s, true);
        byte[] output = new byte[(((int) size) + 2)];
        encode(output, 2, s);
        output[0] = (byte) ((int) (size >>> 8));
        output[1] = (byte) ((int) size);
        return output;
    }

    public static String decode(byte[] in, char[] out, int offset, int length) throws UTFDataFormatException {
        if (offset < 0 || length < 0) {
            throw new IllegalArgumentException("Illegal arguments: offset " + offset + ". Length: " + length);
        }
        int outputIndex = 0;
        int limitIndex = offset + length;
        while (offset < limitIndex) {
            int i = in[offset] & 255;
            offset++;
            if (i < 128) {
                out[outputIndex] = (char) i;
                outputIndex++;
            } else if (192 <= i && i < 224) {
                int i2 = (i & 31) << 6;
                if (offset == limitIndex) {
                    throw new UTFDataFormatException("unexpected end of input");
                } else if ((192 & in[offset]) == 128) {
                    out[outputIndex] = (char) ((in[offset] & 63) | i2);
                    offset++;
                    outputIndex++;
                } else {
                    throw new UTFDataFormatException("bad second byte at " + offset);
                }
            } else if (i < 240) {
                int i3 = (i & 31) << 12;
                if (offset + 1 >= limitIndex) {
                    throw new UTFDataFormatException("unexpected end of input");
                } else if ((in[offset] & 192) == 128) {
                    int i4 = i3 | ((in[offset] & 63) << 6);
                    int offset2 = offset + 1;
                    if ((192 & in[offset2]) == 128) {
                        out[outputIndex] = (char) ((in[offset2] & 63) | i4);
                        offset = offset2 + 1;
                        outputIndex++;
                    } else {
                        throw new UTFDataFormatException("bad third byte at " + offset2);
                    }
                } else {
                    throw new UTFDataFormatException("bad second byte at " + offset);
                }
            } else {
                throw new UTFDataFormatException("Invalid UTF8 byte " + i + " at position " + (offset - 1));
            }
        }
        return String.valueOf(out, 0, outputIndex);
    }
}
