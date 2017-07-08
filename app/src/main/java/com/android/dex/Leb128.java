package com.android.dex;

import com.android.dex.util.ByteInput;
import com.android.dex.util.ByteOutput;
import dalvik.bytecode.Opcodes;
import javax.xml.datatype.DatatypeConstants;
import org.w3c.dom.traversal.NodeFilter;

public final class Leb128 {
    private Leb128() {
    }

    public static int unsignedLeb128Size(int value) {
        int remaining = value >> 7;
        int count = 0;
        while (remaining != 0) {
            remaining >>= 7;
            count++;
        }
        return count + 1;
    }

    public static int signedLeb128Size(int value) {
        int remaining = value >> 7;
        int count = 0;
        boolean hasMore = true;
        int end = (DatatypeConstants.FIELD_UNDEFINED & value) == 0 ? 0 : -1;
        while (hasMore) {
            hasMore = remaining == end ? (remaining & 1) != ((value >> 6) & 1) : true;
            value = remaining;
            remaining >>= 7;
            count++;
        }
        return count;
    }

    public static int readSignedLeb128(ByteInput in) {
        int result = 0;
        int count = 0;
        int signBits = -1;
        do {
            int cur = in.readByte() & Opcodes.OP_CONST_CLASS_JUMBO;
            result |= (cur & Opcodes.OP_NEG_FLOAT) << (count * 7);
            signBits <<= 7;
            count++;
            if ((cur & NodeFilter.SHOW_COMMENT) != NodeFilter.SHOW_COMMENT) {
                break;
            }
        } while (count < 5);
        if ((cur & NodeFilter.SHOW_COMMENT) == NodeFilter.SHOW_COMMENT) {
            throw new DexException("invalid LEB128 sequence");
        } else if (((signBits >> 1) & result) != 0) {
            return result | signBits;
        } else {
            return result;
        }
    }

    public static int readUnsignedLeb128(ByteInput in) {
        int result = 0;
        int count = 0;
        do {
            int cur = in.readByte() & Opcodes.OP_CONST_CLASS_JUMBO;
            result |= (cur & Opcodes.OP_NEG_FLOAT) << (count * 7);
            count++;
            if ((cur & NodeFilter.SHOW_COMMENT) != NodeFilter.SHOW_COMMENT) {
                break;
            }
        } while (count < 5);
        if ((cur & NodeFilter.SHOW_COMMENT) != NodeFilter.SHOW_COMMENT) {
            return result;
        }
        throw new DexException("invalid LEB128 sequence");
    }

    public static void writeUnsignedLeb128(ByteOutput out, int value) {
        for (int remaining = value >>> 7; remaining != 0; remaining >>>= 7) {
            out.writeByte((byte) ((value & Opcodes.OP_NEG_FLOAT) | NodeFilter.SHOW_COMMENT));
            value = remaining;
        }
        out.writeByte((byte) (value & Opcodes.OP_NEG_FLOAT));
    }

    public static void writeSignedLeb128(ByteOutput out, int value) {
        int remaining = value >> 7;
        boolean hasMore = true;
        int end = (DatatypeConstants.FIELD_UNDEFINED & value) == 0 ? 0 : -1;
        while (hasMore) {
            int i;
            if (remaining != end) {
                hasMore = true;
            } else if ((remaining & 1) != ((value >> 6) & 1)) {
                hasMore = true;
            } else {
                hasMore = false;
            }
            int i2 = value & Opcodes.OP_NEG_FLOAT;
            if (hasMore) {
                i = NodeFilter.SHOW_COMMENT;
            } else {
                i = 0;
            }
            out.writeByte((byte) (i | i2));
            value = remaining;
            remaining >>= 7;
        }
    }
}
