package com.google.protobuf;

import java.io.IOException;

public final class WireFormat {
    static final int MESSAGE_SET_ITEM = 1;
    static final int MESSAGE_SET_ITEM_END_TAG = makeTag(1, 4);
    static final int MESSAGE_SET_ITEM_TAG = makeTag(1, 3);
    static final int MESSAGE_SET_MESSAGE = 3;
    static final int MESSAGE_SET_MESSAGE_TAG = makeTag(3, 2);
    static final int MESSAGE_SET_TYPE_ID = 2;
    static final int MESSAGE_SET_TYPE_ID_TAG = makeTag(2, 0);
    static final int TAG_TYPE_BITS = 3;
    static final int TAG_TYPE_MASK = 7;
    public static final int WIRETYPE_END_GROUP = 4;
    public static final int WIRETYPE_FIXED32 = 5;
    public static final int WIRETYPE_FIXED64 = 1;
    public static final int WIRETYPE_LENGTH_DELIMITED = 2;
    public static final int WIRETYPE_START_GROUP = 3;
    public static final int WIRETYPE_VARINT = 0;

    /* access modifiers changed from: package-private */
    public enum Utf8Validation {
        LOOSE {
            /* access modifiers changed from: package-private */
            @Override // com.google.protobuf.WireFormat.Utf8Validation
            public Object readString(CodedInputStream input) throws IOException {
                return input.readString();
            }
        },
        STRICT {
            /* access modifiers changed from: package-private */
            @Override // com.google.protobuf.WireFormat.Utf8Validation
            public Object readString(CodedInputStream input) throws IOException {
                return input.readStringRequireUtf8();
            }
        },
        LAZY {
            /* access modifiers changed from: package-private */
            @Override // com.google.protobuf.WireFormat.Utf8Validation
            public Object readString(CodedInputStream input) throws IOException {
                return input.readBytes();
            }
        };

        /* access modifiers changed from: package-private */
        public abstract Object readString(CodedInputStream codedInputStream) throws IOException;
    }

    private WireFormat() {
    }

    public static int getTagWireType(int tag) {
        return tag & 7;
    }

    public static int getTagFieldNumber(int tag) {
        return tag >>> 3;
    }

    static int makeTag(int fieldNumber, int wireType) {
        return (fieldNumber << 3) | wireType;
    }

    public enum JavaType {
        INT(0),
        LONG(0L),
        FLOAT(Float.valueOf(0.0f)),
        DOUBLE(Double.valueOf(0.0d)),
        BOOLEAN(false),
        STRING(""),
        BYTE_STRING(ByteString.EMPTY),
        ENUM(null),
        MESSAGE(null);
        
        private final Object defaultDefault;

        private JavaType(Object defaultDefault2) {
            this.defaultDefault = defaultDefault2;
        }

        /* access modifiers changed from: package-private */
        public Object getDefaultDefault() {
            return this.defaultDefault;
        }
    }

    public enum FieldType {
        DOUBLE(JavaType.DOUBLE, 1),
        FLOAT(JavaType.FLOAT, 5),
        INT64(JavaType.LONG, 0),
        UINT64(JavaType.LONG, 0),
        INT32(JavaType.INT, 0),
        FIXED64(JavaType.LONG, 1),
        FIXED32(JavaType.INT, 5),
        BOOL(JavaType.BOOLEAN, 0),
        STRING(JavaType.STRING, 2) {
            @Override // com.google.protobuf.WireFormat.FieldType
            public boolean isPackable() {
                return false;
            }
        },
        GROUP(JavaType.MESSAGE, 3) {
            @Override // com.google.protobuf.WireFormat.FieldType
            public boolean isPackable() {
                return false;
            }
        },
        MESSAGE(JavaType.MESSAGE, 2) {
            @Override // com.google.protobuf.WireFormat.FieldType
            public boolean isPackable() {
                return false;
            }
        },
        BYTES(JavaType.BYTE_STRING, 2) {
            @Override // com.google.protobuf.WireFormat.FieldType
            public boolean isPackable() {
                return false;
            }
        },
        UINT32(JavaType.INT, 0),
        ENUM(JavaType.ENUM, 0),
        SFIXED32(JavaType.INT, 5),
        SFIXED64(JavaType.LONG, 1),
        SINT32(JavaType.INT, 0),
        SINT64(JavaType.LONG, 0);
        
        private final JavaType javaType;
        private final int wireType;

        private FieldType(JavaType javaType2, int wireType2) {
            this.javaType = javaType2;
            this.wireType = wireType2;
        }

        public JavaType getJavaType() {
            return this.javaType;
        }

        public int getWireType() {
            return this.wireType;
        }

        public boolean isPackable() {
            return true;
        }
    }

    static Object readPrimitiveField(CodedInputStream input, FieldType type, Utf8Validation utf8Validation) throws IOException {
        switch (type) {
            case DOUBLE:
                return Double.valueOf(input.readDouble());
            case FLOAT:
                return Float.valueOf(input.readFloat());
            case INT64:
                return Long.valueOf(input.readInt64());
            case UINT64:
                return Long.valueOf(input.readUInt64());
            case INT32:
                return Integer.valueOf(input.readInt32());
            case FIXED64:
                return Long.valueOf(input.readFixed64());
            case FIXED32:
                return Integer.valueOf(input.readFixed32());
            case BOOL:
                return Boolean.valueOf(input.readBool());
            case BYTES:
                return input.readBytes();
            case UINT32:
                return Integer.valueOf(input.readUInt32());
            case SFIXED32:
                return Integer.valueOf(input.readSFixed32());
            case SFIXED64:
                return Long.valueOf(input.readSFixed64());
            case SINT32:
                return Integer.valueOf(input.readSInt32());
            case SINT64:
                return Long.valueOf(input.readSInt64());
            case STRING:
                return utf8Validation.readString(input);
            case GROUP:
                throw new IllegalArgumentException("readPrimitiveField() cannot handle nested groups.");
            case MESSAGE:
                throw new IllegalArgumentException("readPrimitiveField() cannot handle embedded messages.");
            case ENUM:
                throw new IllegalArgumentException("readPrimitiveField() cannot handle enums.");
            default:
                throw new RuntimeException("There is no way to get here, but the compiler thinks otherwise.");
        }
    }
}
