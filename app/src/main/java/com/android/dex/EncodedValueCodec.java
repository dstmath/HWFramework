package com.android.dex;

import com.android.dex.util.ByteInput;
import com.android.dex.util.ByteOutput;
import dalvik.bytecode.Opcodes;

public final class EncodedValueCodec {
    private EncodedValueCodec() {
    }

    public static void writeSignedIntegralValue(ByteOutput out, int type, long value) {
        int requiredBytes = ((65 - Long.numberOfLeadingZeros((value >> 63) ^ value)) + 7) >> 3;
        out.writeByte(((requiredBytes - 1) << 5) | type);
        while (requiredBytes > 0) {
            out.writeByte((byte) ((int) value));
            value >>= 8;
            requiredBytes--;
        }
    }

    public static void writeUnsignedIntegralValue(ByteOutput out, int type, long value) {
        int requiredBits = 64 - Long.numberOfLeadingZeros(value);
        if (requiredBits == 0) {
            requiredBits = 1;
        }
        int requiredBytes = (requiredBits + 7) >> 3;
        out.writeByte(((requiredBytes - 1) << 5) | type);
        while (requiredBytes > 0) {
            out.writeByte((byte) ((int) value));
            value >>= 8;
            requiredBytes--;
        }
    }

    public static void writeRightZeroExtendedValue(ByteOutput out, int type, long value) {
        int requiredBits = 64 - Long.numberOfTrailingZeros(value);
        if (requiredBits == 0) {
            requiredBits = 1;
        }
        int requiredBytes = (requiredBits + 7) >> 3;
        value >>= 64 - (requiredBytes * 8);
        out.writeByte(((requiredBytes - 1) << 5) | type);
        while (requiredBytes > 0) {
            out.writeByte((byte) ((int) value));
            value >>= 8;
            requiredBytes--;
        }
    }

    public static int readSignedInt(ByteInput in, int zwidth) {
        int result = 0;
        for (int i = zwidth; i >= 0; i--) {
            result = (result >>> 8) | ((in.readByte() & Opcodes.OP_CONST_CLASS_JUMBO) << 24);
        }
        return result >> ((3 - zwidth) * 8);
    }

    public static int readUnsignedInt(ByteInput in, int zwidth, boolean fillOnRight) {
        int result = 0;
        int i;
        if (fillOnRight) {
            for (i = zwidth; i >= 0; i--) {
                result = (result >>> 8) | ((in.readByte() & Opcodes.OP_CONST_CLASS_JUMBO) << 24);
            }
            return result;
        }
        for (i = zwidth; i >= 0; i--) {
            result = (result >>> 8) | ((in.readByte() & Opcodes.OP_CONST_CLASS_JUMBO) << 24);
        }
        return result >>> ((3 - zwidth) * 8);
    }

    public static long readSignedLong(ByteInput in, int zwidth) {
        long result = 0;
        for (int i = zwidth; i >= 0; i--) {
            result = (result >>> 8) | ((((long) in.readByte()) & 255) << 56);
        }
        return result >> ((7 - zwidth) * 8);
    }

    public static long readUnsignedLong(ByteInput in, int zwidth, boolean fillOnRight) {
        long result = 0;
        int i;
        if (fillOnRight) {
            for (i = zwidth; i >= 0; i--) {
                result = (result >>> 8) | ((((long) in.readByte()) & 255) << 56);
            }
            return result;
        }
        for (i = zwidth; i >= 0; i--) {
            result = (result >>> 8) | ((((long) in.readByte()) & 255) << 56);
        }
        return result >>> ((7 - zwidth) * 8);
    }
}
