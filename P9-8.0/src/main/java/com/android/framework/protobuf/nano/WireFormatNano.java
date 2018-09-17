package com.android.framework.protobuf.nano;

import java.io.IOException;

public final class WireFormatNano {
    public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
    public static final byte[] EMPTY_BYTES = new byte[0];
    public static final byte[][] EMPTY_BYTES_ARRAY = new byte[0][];
    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
    public static final int[] EMPTY_INT_ARRAY = new int[0];
    public static final long[] EMPTY_LONG_ARRAY = new long[0];
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    static final int TAG_TYPE_BITS = 3;
    static final int TAG_TYPE_MASK = 7;
    static final int WIRETYPE_END_GROUP = 4;
    static final int WIRETYPE_FIXED32 = 5;
    static final int WIRETYPE_FIXED64 = 1;
    static final int WIRETYPE_LENGTH_DELIMITED = 2;
    static final int WIRETYPE_START_GROUP = 3;
    static final int WIRETYPE_VARINT = 0;

    private WireFormatNano() {
    }

    static int getTagWireType(int tag) {
        return tag & 7;
    }

    public static int getTagFieldNumber(int tag) {
        return tag >>> 3;
    }

    static int makeTag(int fieldNumber, int wireType) {
        return (fieldNumber << 3) | wireType;
    }

    public static boolean parseUnknownField(CodedInputByteBufferNano input, int tag) throws IOException {
        return input.skipField(tag);
    }

    public static final int getRepeatedFieldArrayLength(CodedInputByteBufferNano input, int tag) throws IOException {
        int arrayLength = 1;
        int startPos = input.getPosition();
        input.skipField(tag);
        while (input.readTag() == tag) {
            input.skipField(tag);
            arrayLength++;
        }
        input.rewindToPosition(startPos);
        return arrayLength;
    }
}
