package com.android.internal.telephony.protobuf.nano;

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

        public PrimitiveExtension(int type, Class<T> clazz, int tag, boolean repeated, int nonPackedTag, int packedTag) {
            super(type, clazz, tag, repeated, null);
            this.nonPackedTag = nonPackedTag;
            this.packedTag = packedTag;
        }

        protected Object readData(CodedInputByteBufferNano input) {
            try {
                return input.readPrimitiveField(this.type);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error reading extension field", e);
            }
        }

        protected void readDataInto(UnknownFieldData data, List<Object> resultList) {
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

        protected final void writeSingularData(Object value, CodedOutputByteBufferNano output) {
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
            throw new IllegalStateException(e);
        }

        protected void writeRepeatedData(Object array, CodedOutputByteBufferNano output) {
            if (this.tag == this.nonPackedTag) {
                super.writeRepeatedData(array, output);
            } else if (this.tag == this.packedTag) {
                int arrayLength = Array.getLength(array);
                int dataSize = computePackedDataSize(array);
                try {
                    output.writeRawVarint32(this.tag);
                    output.writeRawVarint32(dataSize);
                    int i;
                    switch (this.type) {
                        case 1:
                            for (i = 0; i < arrayLength; i++) {
                                output.writeDoubleNoTag(Array.getDouble(array, i));
                            }
                            return;
                        case 2:
                            for (i = 0; i < arrayLength; i++) {
                                output.writeFloatNoTag(Array.getFloat(array, i));
                            }
                            return;
                        case 3:
                            for (i = 0; i < arrayLength; i++) {
                                output.writeInt64NoTag(Array.getLong(array, i));
                            }
                            return;
                        case 4:
                            for (i = 0; i < arrayLength; i++) {
                                output.writeUInt64NoTag(Array.getLong(array, i));
                            }
                            return;
                        case 5:
                            for (i = 0; i < arrayLength; i++) {
                                output.writeInt32NoTag(Array.getInt(array, i));
                            }
                            return;
                        case 6:
                            for (i = 0; i < arrayLength; i++) {
                                output.writeFixed64NoTag(Array.getLong(array, i));
                            }
                            return;
                        case 7:
                            for (i = 0; i < arrayLength; i++) {
                                output.writeFixed32NoTag(Array.getInt(array, i));
                            }
                            return;
                        case 8:
                            for (i = 0; i < arrayLength; i++) {
                                output.writeBoolNoTag(Array.getBoolean(array, i));
                            }
                            return;
                        case 13:
                            for (i = 0; i < arrayLength; i++) {
                                output.writeUInt32NoTag(Array.getInt(array, i));
                            }
                            return;
                        case 14:
                            for (i = 0; i < arrayLength; i++) {
                                output.writeEnumNoTag(Array.getInt(array, i));
                            }
                            return;
                        case 15:
                            for (i = 0; i < arrayLength; i++) {
                                output.writeSFixed32NoTag(Array.getInt(array, i));
                            }
                            return;
                        case 16:
                            for (i = 0; i < arrayLength; i++) {
                                output.writeSFixed64NoTag(Array.getLong(array, i));
                            }
                            return;
                        case 17:
                            for (i = 0; i < arrayLength; i++) {
                                output.writeSInt32NoTag(Array.getInt(array, i));
                            }
                            return;
                        case 18:
                            for (i = 0; i < arrayLength; i++) {
                                output.writeSInt64NoTag(Array.getLong(array, i));
                            }
                            return;
                        default:
                            throw new IllegalArgumentException("Unpackable type " + this.type);
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
                throw new IllegalStateException(e);
            } else {
                throw new IllegalArgumentException("Unexpected repeated extension tag " + this.tag + ", unequal to both non-packed variant " + this.nonPackedTag + " and packed variant " + this.packedTag);
            }
        }

        private int computePackedDataSize(Object array) {
            int dataSize = 0;
            int arrayLength = Array.getLength(array);
            int i;
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
                    for (i = 0; i < arrayLength; i++) {
                        dataSize += CodedOutputByteBufferNano.computeInt64SizeNoTag(Array.getLong(array, i));
                    }
                    return dataSize;
                case 4:
                    for (i = 0; i < arrayLength; i++) {
                        dataSize += CodedOutputByteBufferNano.computeUInt64SizeNoTag(Array.getLong(array, i));
                    }
                    return dataSize;
                case 5:
                    for (i = 0; i < arrayLength; i++) {
                        dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(Array.getInt(array, i));
                    }
                    return dataSize;
                case 8:
                    return arrayLength;
                case 13:
                    for (i = 0; i < arrayLength; i++) {
                        dataSize += CodedOutputByteBufferNano.computeUInt32SizeNoTag(Array.getInt(array, i));
                    }
                    return dataSize;
                case 14:
                    for (i = 0; i < arrayLength; i++) {
                        dataSize += CodedOutputByteBufferNano.computeEnumSizeNoTag(Array.getInt(array, i));
                    }
                    return dataSize;
                case 17:
                    for (i = 0; i < arrayLength; i++) {
                        dataSize += CodedOutputByteBufferNano.computeSInt32SizeNoTag(Array.getInt(array, i));
                    }
                    return dataSize;
                case 18:
                    for (i = 0; i < arrayLength; i++) {
                        dataSize += CodedOutputByteBufferNano.computeSInt64SizeNoTag(Array.getLong(array, i));
                    }
                    return dataSize;
                default:
                    throw new IllegalArgumentException("Unexpected non-packable type " + this.type);
            }
        }

        protected int computeRepeatedSerializedSize(Object array) {
            if (this.tag == this.nonPackedTag) {
                return super.computeRepeatedSerializedSize(array);
            }
            if (this.tag == this.packedTag) {
                int dataSize = computePackedDataSize(array);
                return CodedOutputByteBufferNano.computeRawVarint32Size(this.tag) + (dataSize + CodedOutputByteBufferNano.computeRawVarint32Size(dataSize));
            }
            throw new IllegalArgumentException("Unexpected repeated extension tag " + this.tag + ", unequal to both non-packed variant " + this.nonPackedTag + " and packed variant " + this.packedTag);
        }

        protected final int computeSingularSerializedSize(Object value) {
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

    /* synthetic */ Extension(int type, Class clazz, int tag, boolean repeated, Extension -this4) {
        this(type, clazz, tag, repeated);
    }

    @Deprecated
    public static <M extends ExtendableMessageNano<M>, T extends MessageNano> Extension<M, T> createMessageTyped(int type, Class<T> clazz, int tag) {
        return new Extension(type, clazz, tag, false);
    }

    public static <M extends ExtendableMessageNano<M>, T extends MessageNano> Extension<M, T> createMessageTyped(int type, Class<T> clazz, long tag) {
        return new Extension(type, clazz, (int) tag, false);
    }

    public static <M extends ExtendableMessageNano<M>, T extends MessageNano> Extension<M, T[]> createRepeatedMessageTyped(int type, Class<T[]> clazz, long tag) {
        return new Extension(type, clazz, (int) tag, true);
    }

    public static <M extends ExtendableMessageNano<M>, T> Extension<M, T> createPrimitiveTyped(int type, Class<T> clazz, long tag) {
        return new PrimitiveExtension(type, clazz, (int) tag, false, 0, 0);
    }

    public static <M extends ExtendableMessageNano<M>, T> Extension<M, T> createRepeatedPrimitiveTyped(int type, Class<T> clazz, long tag, long nonPackedTag, long packedTag) {
        return new PrimitiveExtension(type, clazz, (int) tag, true, (int) nonPackedTag, (int) packedTag);
    }

    private Extension(int type, Class<T> clazz, int tag, boolean repeated) {
        this.type = type;
        this.clazz = clazz;
        this.tag = tag;
        this.repeated = repeated;
    }

    final T getValueFrom(List<UnknownFieldData> unknownFields) {
        if (unknownFields == null) {
            return null;
        }
        return this.repeated ? getRepeatedValueFrom(unknownFields) : getSingularValueFrom(unknownFields);
    }

    private T getRepeatedValueFrom(List<UnknownFieldData> unknownFields) {
        int i;
        List<Object> resultList = new ArrayList();
        for (i = 0; i < unknownFields.size(); i++) {
            UnknownFieldData data = (UnknownFieldData) unknownFields.get(i);
            if (data.bytes.length != 0) {
                readDataInto(data, resultList);
            }
        }
        int resultSize = resultList.size();
        if (resultSize == 0) {
            return null;
        }
        T result = this.clazz.cast(Array.newInstance(this.clazz.getComponentType(), resultSize));
        for (i = 0; i < resultSize; i++) {
            Array.set(result, i, resultList.get(i));
        }
        return result;
    }

    private T getSingularValueFrom(List<UnknownFieldData> unknownFields) {
        if (unknownFields.isEmpty()) {
            return null;
        }
        return this.clazz.cast(readData(CodedInputByteBufferNano.newInstance(((UnknownFieldData) unknownFields.get(unknownFields.size() - 1)).bytes)));
    }

    protected Object readData(CodedInputByteBufferNano input) {
        Class<?> messageType = this.repeated ? this.clazz.getComponentType() : this.clazz;
        try {
            switch (this.type) {
                case 10:
                    MessageNano group = (MessageNano) messageType.newInstance();
                    input.readGroup(group, WireFormatNano.getTagFieldNumber(this.tag));
                    return group;
                case 11:
                    MessageNano message = (MessageNano) messageType.newInstance();
                    input.readMessage(message);
                    return message;
                default:
                    throw new IllegalArgumentException("Unknown type " + this.type);
            }
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Error creating instance of class " + messageType, e);
        } catch (IllegalAccessException e2) {
            throw new IllegalArgumentException("Error creating instance of class " + messageType, e2);
        } catch (IOException e3) {
            throw new IllegalArgumentException("Error reading extension field", e3);
        }
    }

    protected void readDataInto(UnknownFieldData data, List<Object> resultList) {
        resultList.add(readData(CodedInputByteBufferNano.newInstance(data.bytes)));
    }

    void writeTo(Object value, CodedOutputByteBufferNano output) throws IOException {
        if (this.repeated) {
            writeRepeatedData(value, output);
        } else {
            writeSingularData(value, output);
        }
    }

    protected void writeSingularData(Object value, CodedOutputByteBufferNano out) {
        try {
            out.writeRawVarint32(this.tag);
            switch (this.type) {
                case 10:
                    MessageNano groupValue = (MessageNano) value;
                    int fieldNumber = WireFormatNano.getTagFieldNumber(this.tag);
                    out.writeGroupNoTag(groupValue);
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
        throw new IllegalStateException(e);
    }

    protected void writeRepeatedData(Object array, CodedOutputByteBufferNano output) {
        int arrayLength = Array.getLength(array);
        for (int i = 0; i < arrayLength; i++) {
            Object element = Array.get(array, i);
            if (element != null) {
                writeSingularData(element, output);
            }
        }
    }

    int computeSerializedSize(Object value) {
        if (this.repeated) {
            return computeRepeatedSerializedSize(value);
        }
        return computeSingularSerializedSize(value);
    }

    protected int computeRepeatedSerializedSize(Object array) {
        int size = 0;
        int arrayLength = Array.getLength(array);
        for (int i = 0; i < arrayLength; i++) {
            if (Array.get(array, i) != null) {
                size += computeSingularSerializedSize(Array.get(array, i));
            }
        }
        return size;
    }

    protected int computeSingularSerializedSize(Object value) {
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
