package java.nio.charset;

import android.icu.text.Bidi;
import dalvik.bytecode.Opcodes;
import java.io.UTFDataFormatException;
import org.w3c.dom.traversal.NodeFilter;

public class ModifiedUtf8 {
    public static long countBytes(String s, boolean shortLength) throws UTFDataFormatException {
        long counter = 0;
        int strLen = s.length();
        for (int i = 0; i < strLen; i++) {
            char c = s.charAt(i);
            if (c < '\u0080') {
                counter++;
                if (c == '\u0000') {
                    counter++;
                }
            } else if (c < '\u0800') {
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
            if (c < '\u0080') {
                if (c == '\u0000') {
                    offset = offset2 + 1;
                    dst[offset2] = (byte) -64;
                    offset2 = offset + 1;
                    dst[offset] = Bidi.LEVEL_OVERRIDE;
                    offset = offset2;
                } else {
                    offset = offset2 + 1;
                    dst[offset2] = (byte) c;
                }
            } else if (c < '\u0800') {
                offset = offset2 + 1;
                dst[offset2] = (byte) ((c >>> 6) | Opcodes.OP_AND_LONG_2ADDR);
                offset2 = offset + 1;
                dst[offset] = (byte) ((c & 63) | NodeFilter.SHOW_COMMENT);
                offset = offset2;
            } else {
                offset = offset2 + 1;
                dst[offset2] = (byte) ((c >>> 12) | Opcodes.OP_SHL_INT_LIT8);
                offset2 = offset + 1;
                dst[offset] = (byte) (((c >>> 6) & 63) | NodeFilter.SHOW_COMMENT);
                offset = offset2 + 1;
                dst[offset2] = (byte) ((c & 63) | NodeFilter.SHOW_COMMENT);
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
            int i = in[offset] & Opcodes.OP_CONST_CLASS_JUMBO;
            offset++;
            if (i < NodeFilter.SHOW_COMMENT) {
                out[outputIndex] = (char) i;
                outputIndex++;
            } else if (Opcodes.OP_AND_LONG_2ADDR <= i && i < Opcodes.OP_SHL_INT_LIT8) {
                i = (i & 31) << 6;
                if (offset == limitIndex) {
                    throw new UTFDataFormatException("unexpected end of input");
                } else if ((in[offset] & Opcodes.OP_AND_LONG_2ADDR) != NodeFilter.SHOW_COMMENT) {
                    throw new UTFDataFormatException("bad second byte at " + offset);
                } else {
                    out[outputIndex] = (char) ((in[offset] & 63) | i);
                    offset++;
                    outputIndex++;
                }
            } else if (i < Opcodes.OP_INVOKE_DIRECT_EMPTY) {
                i = (i & 31) << 12;
                if (offset + 1 >= limitIndex) {
                    throw new UTFDataFormatException("unexpected end of input");
                } else if ((in[offset] & Opcodes.OP_AND_LONG_2ADDR) != NodeFilter.SHOW_COMMENT) {
                    throw new UTFDataFormatException("bad second byte at " + offset);
                } else {
                    i |= (in[offset] & 63) << 6;
                    offset++;
                    if ((in[offset] & Opcodes.OP_AND_LONG_2ADDR) != NodeFilter.SHOW_COMMENT) {
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
