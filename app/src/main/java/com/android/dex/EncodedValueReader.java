package com.android.dex;

import com.android.dex.util.ByteInput;
import dalvik.bytecode.Opcodes;

public final class EncodedValueReader {
    public static final int ENCODED_ANNOTATION = 29;
    public static final int ENCODED_ARRAY = 28;
    public static final int ENCODED_BOOLEAN = 31;
    public static final int ENCODED_BYTE = 0;
    public static final int ENCODED_CHAR = 3;
    public static final int ENCODED_DOUBLE = 17;
    public static final int ENCODED_ENUM = 27;
    public static final int ENCODED_FIELD = 25;
    public static final int ENCODED_FLOAT = 16;
    public static final int ENCODED_INT = 4;
    public static final int ENCODED_LONG = 6;
    public static final int ENCODED_METHOD = 26;
    public static final int ENCODED_NULL = 30;
    public static final int ENCODED_SHORT = 2;
    public static final int ENCODED_STRING = 23;
    public static final int ENCODED_TYPE = 24;
    private static final int MUST_READ = -1;
    private int annotationType;
    private int arg;
    protected final ByteInput in;
    private int type;

    public EncodedValueReader(ByteInput in) {
        this.type = MUST_READ;
        this.in = in;
    }

    public EncodedValueReader(EncodedValue in) {
        this(in.asByteInput());
    }

    public EncodedValueReader(ByteInput in, int knownType) {
        this.type = MUST_READ;
        this.in = in;
        this.type = knownType;
    }

    public EncodedValueReader(EncodedValue in, int knownType) {
        this(in.asByteInput(), knownType);
    }

    public int peek() {
        if (this.type == MUST_READ) {
            int argAndType = this.in.readByte() & Opcodes.OP_CONST_CLASS_JUMBO;
            this.type = argAndType & ENCODED_BOOLEAN;
            this.arg = (argAndType & Opcodes.OP_SHL_INT_LIT8) >> 5;
        }
        return this.type;
    }

    public int readArray() {
        checkType(ENCODED_ARRAY);
        this.type = MUST_READ;
        return Leb128.readUnsignedLeb128(this.in);
    }

    public int readAnnotation() {
        checkType(ENCODED_ANNOTATION);
        this.type = MUST_READ;
        this.annotationType = Leb128.readUnsignedLeb128(this.in);
        return Leb128.readUnsignedLeb128(this.in);
    }

    public int getAnnotationType() {
        return this.annotationType;
    }

    public int readAnnotationName() {
        return Leb128.readUnsignedLeb128(this.in);
    }

    public byte readByte() {
        checkType(ENCODED_BYTE);
        this.type = MUST_READ;
        return (byte) EncodedValueCodec.readSignedInt(this.in, this.arg);
    }

    public short readShort() {
        checkType(ENCODED_SHORT);
        this.type = MUST_READ;
        return (short) EncodedValueCodec.readSignedInt(this.in, this.arg);
    }

    public char readChar() {
        checkType(ENCODED_CHAR);
        this.type = MUST_READ;
        return (char) EncodedValueCodec.readUnsignedInt(this.in, this.arg, false);
    }

    public int readInt() {
        checkType(ENCODED_INT);
        this.type = MUST_READ;
        return EncodedValueCodec.readSignedInt(this.in, this.arg);
    }

    public long readLong() {
        checkType(ENCODED_LONG);
        this.type = MUST_READ;
        return EncodedValueCodec.readSignedLong(this.in, this.arg);
    }

    public float readFloat() {
        checkType(ENCODED_FLOAT);
        this.type = MUST_READ;
        return Float.intBitsToFloat(EncodedValueCodec.readUnsignedInt(this.in, this.arg, true));
    }

    public double readDouble() {
        checkType(ENCODED_DOUBLE);
        this.type = MUST_READ;
        return Double.longBitsToDouble(EncodedValueCodec.readUnsignedLong(this.in, this.arg, true));
    }

    public int readString() {
        checkType(ENCODED_STRING);
        this.type = MUST_READ;
        return EncodedValueCodec.readUnsignedInt(this.in, this.arg, false);
    }

    public int readType() {
        checkType(ENCODED_TYPE);
        this.type = MUST_READ;
        return EncodedValueCodec.readUnsignedInt(this.in, this.arg, false);
    }

    public int readField() {
        checkType(ENCODED_FIELD);
        this.type = MUST_READ;
        return EncodedValueCodec.readUnsignedInt(this.in, this.arg, false);
    }

    public int readEnum() {
        checkType(ENCODED_ENUM);
        this.type = MUST_READ;
        return EncodedValueCodec.readUnsignedInt(this.in, this.arg, false);
    }

    public int readMethod() {
        checkType(ENCODED_METHOD);
        this.type = MUST_READ;
        return EncodedValueCodec.readUnsignedInt(this.in, this.arg, false);
    }

    public void readNull() {
        checkType(ENCODED_NULL);
        this.type = MUST_READ;
    }

    public boolean readBoolean() {
        checkType(ENCODED_BOOLEAN);
        this.type = MUST_READ;
        if (this.arg != 0) {
            return true;
        }
        return false;
    }

    public void skipValue() {
        int size;
        int i;
        switch (peek()) {
            case ENCODED_BYTE /*0*/:
                readByte();
            case ENCODED_SHORT /*2*/:
                readShort();
            case ENCODED_CHAR /*3*/:
                readChar();
            case ENCODED_INT /*4*/:
                readInt();
            case ENCODED_LONG /*6*/:
                readLong();
            case ENCODED_FLOAT /*16*/:
                readFloat();
            case ENCODED_DOUBLE /*17*/:
                readDouble();
            case ENCODED_STRING /*23*/:
                readString();
            case ENCODED_TYPE /*24*/:
                readType();
            case ENCODED_FIELD /*25*/:
                readField();
            case ENCODED_METHOD /*26*/:
                readMethod();
            case ENCODED_ENUM /*27*/:
                readEnum();
            case ENCODED_ARRAY /*28*/:
                size = readArray();
                for (i = ENCODED_BYTE; i < size; i++) {
                    skipValue();
                }
            case ENCODED_ANNOTATION /*29*/:
                size = readAnnotation();
                for (i = ENCODED_BYTE; i < size; i++) {
                    readAnnotationName();
                    skipValue();
                }
            case ENCODED_NULL /*30*/:
                readNull();
            case ENCODED_BOOLEAN /*31*/:
                readBoolean();
            default:
                throw new DexException("Unexpected type: " + Integer.toHexString(this.type));
        }
    }

    private void checkType(int expected) {
        if (peek() != expected) {
            Object[] objArr = new Object[ENCODED_SHORT];
            objArr[ENCODED_BYTE] = Integer.valueOf(expected);
            objArr[1] = Integer.valueOf(peek());
            throw new IllegalStateException(String.format("Expected %x but was %x", objArr));
        }
    }
}
