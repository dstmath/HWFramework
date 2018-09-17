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
        int i = 0;
        int offset2 = offset;
        while (i < strLen) {
            char c = s.charAt(i);
            if (c < 128) {
                if (c == 0) {
                    offset = offset2 + 1;
                    dst[offset2] = (byte) -64;
                    offset2 = offset + 1;
                    dst[offset] = Bidi.LEVEL_OVERRIDE;
                    offset = offset2;
                } else {
                    offset = offset2 + 1;
                    dst[offset2] = (byte) c;
                }
            } else if (c < 2048) {
                offset = offset2 + 1;
                dst[offset2] = (byte) ((c >>> 6) | 192);
                offset2 = offset + 1;
                dst[offset] = (byte) ((c & 63) | 128);
                offset = offset2;
            } else {
                offset = offset2 + 1;
                dst[offset2] = (byte) ((c >>> 12) | 224);
                offset2 = offset + 1;
                dst[offset] = (byte) (((c >>> 6) & 63) | 128);
                offset = offset2 + 1;
                dst[offset2] = (byte) ((c & 63) | 128);
            }
            i++;
            offset2 = offset;
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
                i = (i & 31) << 6;
                if (offset == limitIndex) {
                    throw new UTFDataFormatException("unexpected end of input");
                } else if ((in[offset] & 192) != 128) {
                    throw new UTFDataFormatException("bad second byte at " + offset);
                } else {
                    out[outputIndex] = (char) ((in[offset] & 63) | i);
                    offset++;
                    outputIndex++;
                }
            } else if (i < 240) {
                i = (i & 31) << 12;
                if (offset + 1 >= limitIndex) {
                    throw new UTFDataFormatException("unexpected end of input");
                } else if ((in[offset] & 192) != 128) {
                    throw new UTFDataFormatException("bad second byte at " + offset);
                } else {
                    i |= (in[offset] & 63) << 6;
                    offset++;
                    if ((in[offset] & 192) != 128) {
                        throw new UTFDataFormatException("bad third byte at " + offset);
                    }
                    out[outputIndex] = (char) ((in[offset] & 63) | i);
                    offset++;
                    outputIndex++;
                }
            } else {
                throw new UTFDataFormatException("Invalid UTF8 byte " + i + " at position " + (offset - 1));
            }
        }
        return String.valueOf(out, 0, outputIndex);
    }
}
