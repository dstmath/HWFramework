package com.android.dex;

import com.android.dex.util.ByteInput;
import dalvik.bytecode.Opcodes;
import java.io.UTFDataFormatException;
import org.w3c.dom.traversal.NodeFilter;

public final class Mutf8 {
    private Mutf8() {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String decode(ByteInput in, char[] out) throws UTFDataFormatException {
        int s = 0;
        while (true) {
            char a = (char) (in.readByte() & Opcodes.OP_CONST_CLASS_JUMBO);
            if (a != '\u0000') {
                out[s] = a;
                if (a >= '\u0080') {
                    int b;
                    int s2;
                    if ((a & Opcodes.OP_SHL_INT_LIT8) != Opcodes.OP_AND_LONG_2ADDR) {
                        if ((a & Opcodes.OP_INVOKE_DIRECT_EMPTY) != Opcodes.OP_SHL_INT_LIT8) {
                            break;
                        }
                        b = in.readByte() & Opcodes.OP_CONST_CLASS_JUMBO;
                        int c = in.readByte() & Opcodes.OP_CONST_CLASS_JUMBO;
                        if ((b & Opcodes.OP_AND_LONG_2ADDR) == NodeFilter.SHOW_COMMENT && (c & Opcodes.OP_AND_LONG_2ADDR) == NodeFilter.SHOW_COMMENT) {
                            s2 = s + 1;
                            out[s] = (char) ((((a & 15) << 12) | ((b & 63) << 6)) | (c & 63));
                            s = s2;
                        }
                    } else {
                        b = in.readByte() & Opcodes.OP_CONST_CLASS_JUMBO;
                        if ((b & Opcodes.OP_AND_LONG_2ADDR) != NodeFilter.SHOW_COMMENT) {
                            break;
                        }
                        s2 = s + 1;
                        out[s] = (char) (((a & 31) << 6) | (b & 63));
                        s = s2;
                    }
                } else {
                    s++;
                }
            } else {
                return new String(out, 0, s);
            }
        }
        throw new UTFDataFormatException("bad second or third byte");
    }

    private static long countBytes(String s, boolean shortLength) throws UTFDataFormatException {
        long result = 0;
        int length = s.length();
        int i = 0;
        while (i < length) {
            char ch = s.charAt(i);
            if (ch != '\u0000' && ch <= '\u007f') {
                result++;
            } else if (ch <= '\u07ff') {
                result += 2;
            } else {
                result += 3;
            }
            if (!shortLength || result <= 65535) {
                i++;
            } else {
                throw new UTFDataFormatException("String more than 65535 UTF bytes long");
            }
        }
        return result;
    }

    public static void encode(byte[] dst, int offset, String s) {
        int length = s.length();
        int i = 0;
        int offset2 = offset;
        while (i < length) {
            char ch = s.charAt(i);
            if (ch != '\u0000' && ch <= '\u007f') {
                offset = offset2 + 1;
                dst[offset2] = (byte) ch;
            } else if (ch <= '\u07ff') {
                offset = offset2 + 1;
                dst[offset2] = (byte) (((ch >> 6) & 31) | Opcodes.OP_AND_LONG_2ADDR);
                offset2 = offset + 1;
                dst[offset] = (byte) ((ch & 63) | NodeFilter.SHOW_COMMENT);
                offset = offset2;
            } else {
                offset = offset2 + 1;
                dst[offset2] = (byte) (((ch >> 12) & 15) | Opcodes.OP_SHL_INT_LIT8);
                offset2 = offset + 1;
                dst[offset] = (byte) (((ch >> 6) & 63) | NodeFilter.SHOW_COMMENT);
                offset = offset2 + 1;
                dst[offset2] = (byte) ((ch & 63) | NodeFilter.SHOW_COMMENT);
            }
            i++;
            offset2 = offset;
        }
    }

    public static byte[] encode(String s) throws UTFDataFormatException {
        byte[] result = new byte[((int) countBytes(s, true))];
        encode(result, 0, s);
        return result;
    }
}
