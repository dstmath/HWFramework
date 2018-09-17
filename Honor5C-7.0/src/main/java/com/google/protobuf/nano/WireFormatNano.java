package com.google.protobuf.nano;

import java.io.IOException;

public final class WireFormatNano {
    public static final boolean[] EMPTY_BOOLEAN_ARRAY = null;
    public static final byte[] EMPTY_BYTES = null;
    public static final byte[][] EMPTY_BYTES_ARRAY = null;
    public static final double[] EMPTY_DOUBLE_ARRAY = null;
    public static final float[] EMPTY_FLOAT_ARRAY = null;
    public static final int[] EMPTY_INT_ARRAY = null;
    public static final long[] EMPTY_LONG_ARRAY = null;
    public static final String[] EMPTY_STRING_ARRAY = null;
    static final int TAG_TYPE_BITS = 3;
    static final int TAG_TYPE_MASK = 7;
    static final int WIRETYPE_END_GROUP = 4;
    static final int WIRETYPE_FIXED32 = 5;
    static final int WIRETYPE_FIXED64 = 1;
    static final int WIRETYPE_LENGTH_DELIMITED = 2;
    static final int WIRETYPE_START_GROUP = 3;
    static final int WIRETYPE_VARINT = 0;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.google.protobuf.nano.WireFormatNano.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.google.protobuf.nano.WireFormatNano.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.protobuf.nano.WireFormatNano.<clinit>():void");
    }

    private WireFormatNano() {
    }

    static int getTagWireType(int tag) {
        return tag & TAG_TYPE_MASK;
    }

    public static int getTagFieldNumber(int tag) {
        return tag >>> WIRETYPE_START_GROUP;
    }

    static int makeTag(int fieldNumber, int wireType) {
        return (fieldNumber << WIRETYPE_START_GROUP) | wireType;
    }

    public static boolean parseUnknownField(CodedInputByteBufferNano input, int tag) throws IOException {
        return input.skipField(tag);
    }

    public static final int getRepeatedFieldArrayLength(CodedInputByteBufferNano input, int tag) throws IOException {
        int arrayLength = WIRETYPE_FIXED64;
        int startPos = input.getPosition();
        input.skipField(tag);
        while (input.readTag() == tag) {
            input.skipField(tag);
            arrayLength += WIRETYPE_FIXED64;
        }
        input.rewindToPosition(startPos);
        return arrayLength;
    }
}
