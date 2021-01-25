package com.android.phone.ecc.nano;

import com.android.phone.ecc.nano.ExtendableMessageNano;
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
        return new PrimitiveExtension(type2, clazz2, (int) tag2, false, 0, 0);
    }

    public static <M extends ExtendableMessageNano<M>, T> Extension<M, T> createRepeatedPrimitiveTyped(int type2, Class<T> clazz2, long tag2, long nonPackedTag, long packedTag) {
        return new PrimitiveExtension(type2, clazz2, (int) tag2, true, (int) nonPackedTag, (int) packedTag);
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
        int resultSize = resultList.size();
        if (resultSize == 0) {
            return null;
        }
        Class<T> cls = this.clazz;
        T result = cls.cast(Array.newInstance(cls.getComponentType(), resultSize));
        for (int i2 = 0; i2 < resultSize; i2++) {
            Array.set(result, i2, resultList.get(i2));
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
            int i = this.type;
            if (i == 10) {
                MessageNano group = (MessageNano) componentType.newInstance();
                input.readGroup(group, WireFormatNano.getTagFieldNumber(this.tag));
                return group;
            } else if (i == 11) {
                MessageNano message = (MessageNano) componentType.newInstance();
                input.readMessage(message);
                return message;
            } else {
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
            int i = this.type;
            if (i == 10) {
                int fieldNumber = WireFormatNano.getTagFieldNumber(this.tag);
                out.writeGroupNoTag((MessageNano) value);
                out.writeTag(fieldNumber, 4);
            } else if (i == 11) {
                out.writeMessageNoTag((MessageNano) value);
            } else {
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
        int i = this.type;
        if (i == 10) {
            return CodedOutputByteBufferNano.computeGroupSize(fieldNumber, (MessageNano) value);
        }
        if (i == 11) {
            return CodedOutputByteBufferNano.computeMessageSize(fieldNumber, (MessageNano) value);
        }
        throw new IllegalArgumentException("Unknown type " + this.type);
    }

    private static class PrimitiveExtension<M extends ExtendableMessageNano<M>, T> extends Extension<M, T> {
        private final int nonPackedTag;
        private final int packedTag;

        public PrimitiveExtension(int type, Class<T> clazz, int tag, boolean repeated, int nonPackedTag2, int packedTag2) {
            super(type, clazz, tag, repeated);
            this.nonPackedTag = nonPackedTag2;
            this.packedTag = packedTag2;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.phone.ecc.nano.Extension
        public Object readData(CodedInputByteBufferNano input) {
            try {
                return input.readPrimitiveField(this.type);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error reading extension field", e);
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.phone.ecc.nano.Extension
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
        @Override // com.android.phone.ecc.nano.Extension
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
                    case 10:
                    case 11:
                    default:
                        throw new IllegalArgumentException("Unknown type " + this.type);
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
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.phone.ecc.nano.Extension
        public void writeRepeatedData(Object array, CodedOutputByteBufferNano output) {
            if (this.tag == this.nonPackedTag) {
                Extension.super.writeRepeatedData(array, output);
            } else if (this.tag == this.packedTag) {
                int arrayLength = Array.getLength(array);
                int dataSize = computePackedDataSize(array);
                try {
                    output.writeRawVarint32(this.tag);
                    output.writeRawVarint32(dataSize);
                    switch (this.type) {
                        case 1:
                            for (int i = 0; i < arrayLength; i++) {
                                output.writeDoubleNoTag(Array.getDouble(array, i));
                            }
                            return;
                        case 2:
                            for (int i2 = 0; i2 < arrayLength; i2++) {
                                output.writeFloatNoTag(Array.getFloat(array, i2));
                            }
                            return;
                        case 3:
                            for (int i3 = 0; i3 < arrayLength; i3++) {
                                output.writeInt64NoTag(Array.getLong(array, i3));
                            }
                            return;
                        case 4:
                            for (int i4 = 0; i4 < arrayLength; i4++) {
                                output.writeUInt64NoTag(Array.getLong(array, i4));
                            }
                            return;
                        case 5:
                            for (int i5 = 0; i5 < arrayLength; i5++) {
                                output.writeInt32NoTag(Array.getInt(array, i5));
                            }
                            return;
                        case 6:
                            for (int i6 = 0; i6 < arrayLength; i6++) {
                                output.writeFixed64NoTag(Array.getLong(array, i6));
                            }
                            return;
                        case 7:
                            for (int i7 = 0; i7 < arrayLength; i7++) {
                                output.writeFixed32NoTag(Array.getInt(array, i7));
                            }
                            return;
                        case 8:
                            for (int i8 = 0; i8 < arrayLength; i8++) {
                                output.writeBoolNoTag(Array.getBoolean(array, i8));
                            }
                            return;
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        default:
                            throw new IllegalArgumentException("Unpackable type " + this.type);
                        case 13:
                            for (int i9 = 0; i9 < arrayLength; i9++) {
                                output.writeUInt32NoTag(Array.getInt(array, i9));
                            }
                            return;
                        case 14:
                            for (int i10 = 0; i10 < arrayLength; i10++) {
                                output.writeEnumNoTag(Array.getInt(array, i10));
                            }
                            return;
                        case 15:
                            for (int i11 = 0; i11 < arrayLength; i11++) {
                                output.writeSFixed32NoTag(Array.getInt(array, i11));
                            }
                            return;
                        case 16:
                            for (int i12 = 0; i12 < arrayLength; i12++) {
                                output.writeSFixed64NoTag(Array.getLong(array, i12));
                            }
                            return;
                        case 17:
                            for (int i13 = 0; i13 < arrayLength; i13++) {
                                output.writeSInt32NoTag(Array.getInt(array, i13));
                            }
                            return;
                        case 18:
                            for (int i14 = 0; i14 < arrayLength; i14++) {
                                output.writeSInt64NoTag(Array.getLong(array, i14));
                            }
                            return;
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            } else {
                throw new IllegalArgumentException("Unexpected repeated extension tag " + this.tag + ", unequal to both non-packed variant " + this.nonPackedTag + " and packed variant " + this.packedTag);
            }
        }

        private int computePackedDataSize(Object array) {
            int dataSize = 0;
            int arrayLength = Array.getLength(array);
            switch (this.type) {
                case 1:
                case 6:
                case 16:
                    return arrayLength * 8;
                case 2:
                case 7:
                case 15:
                    return arrayLength * 4;
                case 3:
                    for (int i = 0; i < arrayLength; i++) {
                        dataSize += CodedOutputByteBufferNano.computeInt64SizeNoTag(Array.getLong(array, i));
                    }
                    return dataSize;
                case 4:
                    for (int i2 = 0; i2 < arrayLength; i2++) {
                        dataSize += CodedOutputByteBufferNano.computeUInt64SizeNoTag(Array.getLong(array, i2));
                    }
                    return dataSize;
                case 5:
                    for (int i3 = 0; i3 < arrayLength; i3++) {
                        dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(Array.getInt(array, i3));
                    }
                    return dataSize;
                case 8:
                    return arrayLength;
                case 9:
                case 10:
                case 11:
                case 12:
                default:
                    throw new IllegalArgumentException("Unexpected non-packable type " + this.type);
                case 13:
                    for (int i4 = 0; i4 < arrayLength; i4++) {
                        dataSize += CodedOutputByteBufferNano.computeUInt32SizeNoTag(Array.getInt(array, i4));
                    }
                    return dataSize;
                case 14:
                    for (int i5 = 0; i5 < arrayLength; i5++) {
                        dataSize += CodedOutputByteBufferNano.computeEnumSizeNoTag(Array.getInt(array, i5));
                    }
                    return dataSize;
                case 17:
                    for (int i6 = 0; i6 < arrayLength; i6++) {
                        dataSize += CodedOutputByteBufferNano.computeSInt32SizeNoTag(Array.getInt(array, i6));
                    }
                    return dataSize;
                case 18:
                    for (int i7 = 0; i7 < arrayLength; i7++) {
                        dataSize += CodedOutputByteBufferNano.computeSInt64SizeNoTag(Array.getLong(array, i7));
                    }
                    return dataSize;
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.phone.ecc.nano.Extension
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
        @Override // com.android.phone.ecc.nano.Extension
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
                case 10:
                case 11:
                default:
                    throw new IllegalArgumentException("Unknown type " + this.type);
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
            }
        }
    }
}
