package com.android.internal.telephony.protobuf.nano;

import com.android.internal.telephony.protobuf.nano.ExtendableMessageNano;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Extension<M extends ExtendableMessageNano<M>, T> {
    public static final int TYPE_BOOL = 8;
    public static final int TYPE_BYTES = 12;
    public static final int TYPE_DOUBLE = 1;
    public static final int TYPE_ENUM = 14;
    public static final int TYPE_FIXED32 = 7;
    public static final int TYPE_FIXED64 = 6;
    public static final int TYPE_FLOAT = 2;
    public static final int TYPE_GROUP = 10;
    public static final int TYPE_INT32 = 5;
    public static final int TYPE_INT64 = 3;
    public static final int TYPE_MESSAGE = 11;
    public static final int TYPE_SFIXED32 = 15;
    public static final int TYPE_SFIXED64 = 16;
    public static final int TYPE_SINT32 = 17;
    public static final int TYPE_SINT64 = 18;
    public static final int TYPE_STRING = 9;
    public static final int TYPE_UINT32 = 13;
    public static final int TYPE_UINT64 = 4;
    protected final Class<T> clazz;
    protected final boolean repeated;
    public final int tag;
    protected final int type;

    private static class PrimitiveExtension<M extends ExtendableMessageNano<M>, T> extends Extension<M, T> {
        private final int nonPackedTag;
        private final int packedTag;

        public PrimitiveExtension(int type, Class<T> clazz, int tag, boolean repeated, int nonPackedTag2, int packedTag2) {
            super(type, clazz, tag, repeated);
            this.nonPackedTag = nonPackedTag2;
            this.packedTag = packedTag2;
        }

        /* access modifiers changed from: protected */
        public Object readData(CodedInputByteBufferNano input) {
            try {
                return input.readPrimitiveField(this.type);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error reading extension field", e);
            }
        }

        /* access modifiers changed from: protected */
        public void readDataInto(UnknownFieldData data, List<Object> resultList) {
            if (data.tag == this.nonPackedTag) {
                resultList.add(readData(CodedInputByteBufferNano.newInstance(data.bytes)));
                return;
            }
            CodedInputByteBufferNano buffer = CodedInputByteBufferNano.newInstance(data.bytes);
            try {
                buffer.pushLimit(buffer.readRawVarint32());
                while (!buffer.isAtEnd()) {
                    resultList.add(readData(buffer));
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Error reading extension field", e);
            }
        }

        /* access modifiers changed from: protected */
        public final void writeSingularData(Object value, CodedOutputByteBufferNano output) {
            try {
                output.writeRawVarint32(this.tag);
                switch (this.type) {
                    case 1:
                        output.writeDoubleNoTag(((Double) value).doubleValue());
                        return;
                    case 2:
                        output.writeFloatNoTag(((Float) value).floatValue());
                        return;
                    case 3:
                        output.writeInt64NoTag(((Long) value).longValue());
                        return;
                    case 4:
                        output.writeUInt64NoTag(((Long) value).longValue());
                        return;
                    case 5:
                        output.writeInt32NoTag(((Integer) value).intValue());
                        return;
                    case 6:
                        output.writeFixed64NoTag(((Long) value).longValue());
                        return;
                    case 7:
                        output.writeFixed32NoTag(((Integer) value).intValue());
                        return;
                    case 8:
                        output.writeBoolNoTag(((Boolean) value).booleanValue());
                        return;
                    case 9:
                        output.writeStringNoTag((String) value);
                        return;
                    case 12:
                        output.writeBytesNoTag((byte[]) value);
                        return;
                    case 13:
                        output.writeUInt32NoTag(((Integer) value).intValue());
                        return;
                    case 14:
                        output.writeEnumNoTag(((Integer) value).intValue());
                        return;
                    case 15:
                        output.writeSFixed32NoTag(((Integer) value).intValue());
                        return;
                    case 16:
                        output.writeSFixed64NoTag(((Long) value).longValue());
                        return;
                    case 17:
                        output.writeSInt32NoTag(((Integer) value).intValue());
                        return;
                    case 18:
                        output.writeSInt64NoTag(((Long) value).longValue());
                        return;
                    default:
                        throw new IllegalArgumentException("Unknown type " + this.type);
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Code restructure failed: missing block: B:100:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:101:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0044, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0045, code lost:
            if (r2 >= r0) goto L_0x0051;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0047, code lost:
            r7.writeSInt64NoTag(java.lang.reflect.Array.getLong(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0054, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0055, code lost:
            if (r2 >= r0) goto L_0x0061;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0057, code lost:
            r7.writeSInt32NoTag(java.lang.reflect.Array.getInt(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0064, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0065, code lost:
            if (r2 >= r0) goto L_0x0071;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0067, code lost:
            r7.writeSFixed64NoTag(java.lang.reflect.Array.getLong(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0074, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x0075, code lost:
            if (r2 >= r0) goto L_0x0081;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x0077, code lost:
            r7.writeSFixed32NoTag(java.lang.reflect.Array.getInt(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:0x0084, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x0085, code lost:
            if (r2 >= r0) goto L_0x0091;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x0087, code lost:
            r7.writeEnumNoTag(java.lang.reflect.Array.getInt(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x0094, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x0095, code lost:
            if (r2 >= r0) goto L_0x00a1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x0097, code lost:
            r7.writeUInt32NoTag(java.lang.reflect.Array.getInt(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x00a4, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x00a5, code lost:
            if (r2 >= r0) goto L_0x00b1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x00a7, code lost:
            r7.writeBoolNoTag(java.lang.reflect.Array.getBoolean(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x00b4, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:0x00b5, code lost:
            if (r2 >= r0) goto L_0x00c1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x00b7, code lost:
            r7.writeFixed32NoTag(java.lang.reflect.Array.getInt(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:44:0x00c4, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:45:0x00c5, code lost:
            if (r2 >= r0) goto L_0x00d1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:46:0x00c7, code lost:
            r7.writeFixed64NoTag(java.lang.reflect.Array.getLong(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:48:0x00d3, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:49:0x00d4, code lost:
            if (r2 >= r0) goto L_0x00e0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:50:0x00d6, code lost:
            r7.writeInt32NoTag(java.lang.reflect.Array.getInt(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:52:0x00e2, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:53:0x00e3, code lost:
            if (r2 >= r0) goto L_0x00ef;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:54:0x00e5, code lost:
            r7.writeUInt64NoTag(java.lang.reflect.Array.getLong(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:56:0x00f1, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:57:0x00f2, code lost:
            if (r2 >= r0) goto L_0x00fe;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:58:0x00f4, code lost:
            r7.writeInt64NoTag(java.lang.reflect.Array.getLong(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:60:0x0100, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:61:0x0101, code lost:
            if (r2 >= r0) goto L_0x010d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:62:0x0103, code lost:
            r7.writeFloatNoTag(java.lang.reflect.Array.getFloat(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:64:0x010f, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:65:0x0110, code lost:
            if (r2 >= r0) goto L_0x011f;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:66:0x0112, code lost:
            r7.writeDoubleNoTag(java.lang.reflect.Array.getDouble(r6, r2));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:67:0x0119, code lost:
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:88:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:89:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:90:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:91:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:92:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:93:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:94:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:95:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:96:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:97:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:98:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:99:?, code lost:
            return;
         */
        public void writeRepeatedData(Object array, CodedOutputByteBufferNano output) {
            if (this.tag == this.nonPackedTag) {
                Extension.super.writeRepeatedData(array, output);
            } else if (this.tag == this.packedTag) {
                int arrayLength = Array.getLength(array);
                int dataSize = computePackedDataSize(array);
                try {
                    output.writeRawVarint32(this.tag);
                    output.writeRawVarint32(dataSize);
                    int i = this.type;
                    int i2 = 0;
                    switch (i) {
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                        case 4:
                            break;
                        case 5:
                            break;
                        case 6:
                            break;
                        case 7:
                            break;
                        case 8:
                            break;
                        default:
                            switch (i) {
                                case 13:
                                    break;
                                case 14:
                                    break;
                                case 15:
                                    break;
                                case 16:
                                    break;
                                case 17:
                                    break;
                                case 18:
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unpackable type " + this.type);
                            }
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            } else {
                throw new IllegalArgumentException("Unexpected repeated extension tag " + this.tag + ", unequal to both non-packed variant " + this.nonPackedTag + " and packed variant " + this.packedTag);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x003e, code lost:
            r0 = r0 + com.android.internal.telephony.protobuf.nano.CodedOutputByteBufferNano.computeSInt32SizeNoTag(java.lang.reflect.Array.getInt(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x004e, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x004f, code lost:
            if (r2 >= r1) goto L_0x00af;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0051, code lost:
            r0 = r0 + com.android.internal.telephony.protobuf.nano.CodedOutputByteBufferNano.computeEnumSizeNoTag(java.lang.reflect.Array.getInt(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0060, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0061, code lost:
            if (r2 >= r1) goto L_0x00af;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0063, code lost:
            r0 = r0 + com.android.internal.telephony.protobuf.nano.CodedOutputByteBufferNano.computeUInt32SizeNoTag(java.lang.reflect.Array.getInt(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0074, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0075, code lost:
            if (r2 >= r1) goto L_0x00af;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0077, code lost:
            r0 = r0 + com.android.internal.telephony.protobuf.nano.CodedOutputByteBufferNano.computeInt32SizeNoTag(java.lang.reflect.Array.getInt(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0086, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0087, code lost:
            if (r2 >= r1) goto L_0x00af;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x0089, code lost:
            r0 = r0 + com.android.internal.telephony.protobuf.nano.CodedOutputByteBufferNano.computeUInt64SizeNoTag(java.lang.reflect.Array.getLong(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0098, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x0099, code lost:
            if (r2 >= r1) goto L_0x00af;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x009b, code lost:
            r0 = r0 + com.android.internal.telephony.protobuf.nano.CodedOutputByteBufferNano.computeInt64SizeNoTag(java.lang.reflect.Array.getLong(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
            return r1 * 8;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
            return r1 * 4;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
            return r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
            return r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
            return r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
            return r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
            return r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:44:?, code lost:
            return r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
            return r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:5:0x0028, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:6:0x0029, code lost:
            if (r2 >= r1) goto L_0x00af;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:7:0x002b, code lost:
            r0 = r0 + com.android.internal.telephony.protobuf.nano.CodedOutputByteBufferNano.computeSInt64SizeNoTag(java.lang.reflect.Array.getLong(r6, r2));
            r3 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x003b, code lost:
            r2 = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x003c, code lost:
            if (r2 >= r1) goto L_0x00af;
         */
        private int computePackedDataSize(Object array) {
            int dataSize = 0;
            int arrayLength = Array.getLength(array);
            int i = this.type;
            int i2 = 0;
            switch (i) {
                case 1:
                case 6:
                    break;
                case 2:
                case 7:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 8:
                    return arrayLength;
                default:
                    switch (i) {
                        case 13:
                            break;
                        case 14:
                            break;
                        case 15:
                            break;
                        case 16:
                            break;
                        case 17:
                            break;
                        case 18:
                            break;
                        default:
                            throw new IllegalArgumentException("Unexpected non-packable type " + this.type);
                    }
            }
        }

        /* access modifiers changed from: protected */
        public int computeRepeatedSerializedSize(Object array) {
            if (this.tag == this.nonPackedTag) {
                return Extension.super.computeRepeatedSerializedSize(array);
            }
            if (this.tag == this.packedTag) {
                int dataSize = computePackedDataSize(array);
                return CodedOutputByteBufferNano.computeRawVarint32Size(this.tag) + CodedOutputByteBufferNano.computeRawVarint32Size(dataSize) + dataSize;
            }
            throw new IllegalArgumentException("Unexpected repeated extension tag " + this.tag + ", unequal to both non-packed variant " + this.nonPackedTag + " and packed variant " + this.packedTag);
        }

        /* access modifiers changed from: protected */
        public final int computeSingularSerializedSize(Object value) {
            int fieldNumber = WireFormatNano.getTagFieldNumber(this.tag);
            switch (this.type) {
                case 1:
                    return CodedOutputByteBufferNano.computeDoubleSize(fieldNumber, ((Double) value).doubleValue());
                case 2:
                    return CodedOutputByteBufferNano.computeFloatSize(fieldNumber, ((Float) value).floatValue());
                case 3:
                    return CodedOutputByteBufferNano.computeInt64Size(fieldNumber, ((Long) value).longValue());
                case 4:
                    return CodedOutputByteBufferNano.computeUInt64Size(fieldNumber, ((Long) value).longValue());
                case 5:
                    return CodedOutputByteBufferNano.computeInt32Size(fieldNumber, ((Integer) value).intValue());
                case 6:
                    return CodedOutputByteBufferNano.computeFixed64Size(fieldNumber, ((Long) value).longValue());
                case 7:
                    return CodedOutputByteBufferNano.computeFixed32Size(fieldNumber, ((Integer) value).intValue());
                case 8:
                    return CodedOutputByteBufferNano.computeBoolSize(fieldNumber, ((Boolean) value).booleanValue());
                case 9:
                    return CodedOutputByteBufferNano.computeStringSize(fieldNumber, (String) value);
                case 12:
                    return CodedOutputByteBufferNano.computeBytesSize(fieldNumber, (byte[]) value);
                case 13:
                    return CodedOutputByteBufferNano.computeUInt32Size(fieldNumber, ((Integer) value).intValue());
                case 14:
                    return CodedOutputByteBufferNano.computeEnumSize(fieldNumber, ((Integer) value).intValue());
                case 15:
                    return CodedOutputByteBufferNano.computeSFixed32Size(fieldNumber, ((Integer) value).intValue());
                case 16:
                    return CodedOutputByteBufferNano.computeSFixed64Size(fieldNumber, ((Long) value).longValue());
                case 17:
                    return CodedOutputByteBufferNano.computeSInt32Size(fieldNumber, ((Integer) value).intValue());
                case 18:
                    return CodedOutputByteBufferNano.computeSInt64Size(fieldNumber, ((Long) value).longValue());
                default:
                    throw new IllegalArgumentException("Unknown type " + this.type);
            }
        }
    }

    @Deprecated
    public static <M extends ExtendableMessageNano<M>, T extends MessageNano> Extension<M, T> createMessageTyped(int type2, Class<T> clazz2, int tag2) {
        return new Extension<>(type2, clazz2, tag2, false);
    }

    public static <M extends ExtendableMessageNano<M>, T extends MessageNano> Extension<M, T> createMessageTyped(int type2, Class<T> clazz2, long tag2) {
        return new Extension<>(type2, clazz2, (int) tag2, false);
    }

    public static <M extends ExtendableMessageNano<M>, T extends MessageNano> Extension<M, T[]> createRepeatedMessageTyped(int type2, Class<T[]> clazz2, long tag2) {
        return new Extension<>(type2, clazz2, (int) tag2, true);
    }

    public static <M extends ExtendableMessageNano<M>, T> Extension<M, T> createPrimitiveTyped(int type2, Class<T> clazz2, long tag2) {
        PrimitiveExtension primitiveExtension = new PrimitiveExtension(type2, clazz2, (int) tag2, false, 0, 0);
        return primitiveExtension;
    }

    public static <M extends ExtendableMessageNano<M>, T> Extension<M, T> createRepeatedPrimitiveTyped(int type2, Class<T> clazz2, long tag2, long nonPackedTag, long packedTag) {
        PrimitiveExtension primitiveExtension = new PrimitiveExtension(type2, clazz2, (int) tag2, true, (int) nonPackedTag, (int) packedTag);
        return primitiveExtension;
    }

    private Extension(int type2, Class<T> clazz2, int tag2, boolean repeated2) {
        this.type = type2;
        this.clazz = clazz2;
        this.tag = tag2;
        this.repeated = repeated2;
    }

    /* access modifiers changed from: package-private */
    public final T getValueFrom(List<UnknownFieldData> unknownFields) {
        if (unknownFields == null) {
            return null;
        }
        return this.repeated ? getRepeatedValueFrom(unknownFields) : getSingularValueFrom(unknownFields);
    }

    private T getRepeatedValueFrom(List<UnknownFieldData> unknownFields) {
        List<Object> resultList = new ArrayList<>();
        for (int i = 0; i < unknownFields.size(); i++) {
            UnknownFieldData data = unknownFields.get(i);
            if (data.bytes.length != 0) {
                readDataInto(data, resultList);
            }
        }
        int i2 = resultList.size();
        if (i2 == 0) {
            return null;
        }
        T result = this.clazz.cast(Array.newInstance(this.clazz.getComponentType(), i2));
        for (int i3 = 0; i3 < i2; i3++) {
            Array.set(result, i3, resultList.get(i3));
        }
        return result;
    }

    private T getSingularValueFrom(List<UnknownFieldData> unknownFields) {
        if (unknownFields.isEmpty()) {
            return null;
        }
        return this.clazz.cast(readData(CodedInputByteBufferNano.newInstance(unknownFields.get(unknownFields.size() - 1).bytes)));
    }

    /* access modifiers changed from: protected */
    public Object readData(CodedInputByteBufferNano input) {
        Class componentType = this.repeated ? this.clazz.getComponentType() : this.clazz;
        try {
            switch (this.type) {
                case 10:
                    MessageNano group = (MessageNano) componentType.newInstance();
                    input.readGroup(group, WireFormatNano.getTagFieldNumber(this.tag));
                    return group;
                case 11:
                    MessageNano message = (MessageNano) componentType.newInstance();
                    input.readMessage(message);
                    return message;
                default:
                    throw new IllegalArgumentException("Unknown type " + this.type);
            }
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Error creating instance of class " + componentType, e);
        } catch (IllegalAccessException e2) {
            throw new IllegalArgumentException("Error creating instance of class " + componentType, e2);
        } catch (IOException e3) {
            throw new IllegalArgumentException("Error reading extension field", e3);
        }
    }

    /* access modifiers changed from: protected */
    public void readDataInto(UnknownFieldData data, List<Object> resultList) {
        resultList.add(readData(CodedInputByteBufferNano.newInstance(data.bytes)));
    }

    /* access modifiers changed from: package-private */
    public void writeTo(Object value, CodedOutputByteBufferNano output) throws IOException {
        if (this.repeated) {
            writeRepeatedData(value, output);
        } else {
            writeSingularData(value, output);
        }
    }

    /* access modifiers changed from: protected */
    public void writeSingularData(Object value, CodedOutputByteBufferNano out) {
        try {
            out.writeRawVarint32(this.tag);
            switch (this.type) {
                case 10:
                    int fieldNumber = WireFormatNano.getTagFieldNumber(this.tag);
                    out.writeGroupNoTag((MessageNano) value);
                    out.writeTag(fieldNumber, 4);
                    return;
                case 11:
                    out.writeMessageNoTag((MessageNano) value);
                    return;
                default:
                    throw new IllegalArgumentException("Unknown type " + this.type);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /* access modifiers changed from: protected */
    public void writeRepeatedData(Object array, CodedOutputByteBufferNano output) {
        int arrayLength = Array.getLength(array);
        for (int i = 0; i < arrayLength; i++) {
            Object element = Array.get(array, i);
            if (element != null) {
                writeSingularData(element, output);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int computeSerializedSize(Object value) {
        if (this.repeated) {
            return computeRepeatedSerializedSize(value);
        }
        return computeSingularSerializedSize(value);
    }

    /* access modifiers changed from: protected */
    public int computeRepeatedSerializedSize(Object array) {
        int size = 0;
        int arrayLength = Array.getLength(array);
        for (int i = 0; i < arrayLength; i++) {
            if (Array.get(array, i) != null) {
                size += computeSingularSerializedSize(Array.get(array, i));
            }
        }
        return size;
    }

    /* access modifiers changed from: protected */
    public int computeSingularSerializedSize(Object value) {
        int fieldNumber = WireFormatNano.getTagFieldNumber(this.tag);
        switch (this.type) {
            case 10:
                return CodedOutputByteBufferNano.computeGroupSize(fieldNumber, (MessageNano) value);
            case 11:
                return CodedOutputByteBufferNano.computeMessageSize(fieldNumber, (MessageNano) value);
            default:
                throw new IllegalArgumentException("Unknown type " + this.type);
        }
    }
}
