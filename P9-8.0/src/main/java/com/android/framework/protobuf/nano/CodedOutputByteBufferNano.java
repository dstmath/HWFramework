package com.android.framework.protobuf.nano;

import android.util.proto.ProtoOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ReadOnlyBufferException;

public final class CodedOutputByteBufferNano {
    public static final int LITTLE_ENDIAN_32_SIZE = 4;
    public static final int LITTLE_ENDIAN_64_SIZE = 8;
    private static final int MAX_UTF8_EXPANSION = 3;
    private final ByteBuffer buffer;

    public static class OutOfSpaceException extends IOException {
        private static final long serialVersionUID = -6947486886997889499L;

        OutOfSpaceException(int position, int limit) {
            super("CodedOutputStream was writing to a flat byte array and ran out of space (pos " + position + " limit " + limit + ").");
        }
    }

    private CodedOutputByteBufferNano(byte[] buffer, int offset, int length) {
        this(ByteBuffer.wrap(buffer, offset, length));
    }

    private CodedOutputByteBufferNano(ByteBuffer buffer) {
        this.buffer = buffer;
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public static CodedOutputByteBufferNano newInstance(byte[] flatArray) {
        return newInstance(flatArray, 0, flatArray.length);
    }

    public static CodedOutputByteBufferNano newInstance(byte[] flatArray, int offset, int length) {
        return new CodedOutputByteBufferNano(flatArray, offset, length);
    }

    public void writeDouble(int fieldNumber, double value) throws IOException {
        writeTag(fieldNumber, 1);
        writeDoubleNoTag(value);
    }

    public void writeFloat(int fieldNumber, float value) throws IOException {
        writeTag(fieldNumber, 5);
        writeFloatNoTag(value);
    }

    public void writeUInt64(int fieldNumber, long value) throws IOException {
        writeTag(fieldNumber, 0);
        writeUInt64NoTag(value);
    }

    public void writeInt64(int fieldNumber, long value) throws IOException {
        writeTag(fieldNumber, 0);
        writeInt64NoTag(value);
    }

    public void writeInt32(int fieldNumber, int value) throws IOException {
        writeTag(fieldNumber, 0);
        writeInt32NoTag(value);
    }

    public void writeFixed64(int fieldNumber, long value) throws IOException {
        writeTag(fieldNumber, 1);
        writeFixed64NoTag(value);
    }

    public void writeFixed32(int fieldNumber, int value) throws IOException {
        writeTag(fieldNumber, 5);
        writeFixed32NoTag(value);
    }

    public void writeBool(int fieldNumber, boolean value) throws IOException {
        writeTag(fieldNumber, 0);
        writeBoolNoTag(value);
    }

    public void writeString(int fieldNumber, String value) throws IOException {
        writeTag(fieldNumber, 2);
        writeStringNoTag(value);
    }

    public void writeGroup(int fieldNumber, MessageNano value) throws IOException {
        writeTag(fieldNumber, 3);
        writeGroupNoTag(value);
        writeTag(fieldNumber, 4);
    }

    public void writeMessage(int fieldNumber, MessageNano value) throws IOException {
        writeTag(fieldNumber, 2);
        writeMessageNoTag(value);
    }

    public void writeBytes(int fieldNumber, byte[] value) throws IOException {
        writeTag(fieldNumber, 2);
        writeBytesNoTag(value);
    }

    public void writeBytes(int fieldNumber, byte[] value, int offset, int length) throws IOException {
        writeTag(fieldNumber, 2);
        writeBytesNoTag(value, offset, length);
    }

    public void writeUInt32(int fieldNumber, int value) throws IOException {
        writeTag(fieldNumber, 0);
        writeUInt32NoTag(value);
    }

    public void writeEnum(int fieldNumber, int value) throws IOException {
        writeTag(fieldNumber, 0);
        writeEnumNoTag(value);
    }

    public void writeSFixed32(int fieldNumber, int value) throws IOException {
        writeTag(fieldNumber, 5);
        writeSFixed32NoTag(value);
    }

    public void writeSFixed64(int fieldNumber, long value) throws IOException {
        writeTag(fieldNumber, 1);
        writeSFixed64NoTag(value);
    }

    public void writeSInt32(int fieldNumber, int value) throws IOException {
        writeTag(fieldNumber, 0);
        writeSInt32NoTag(value);
    }

    public void writeSInt64(int fieldNumber, long value) throws IOException {
        writeTag(fieldNumber, 0);
        writeSInt64NoTag(value);
    }

    public void writeDoubleNoTag(double value) throws IOException {
        writeRawLittleEndian64(Double.doubleToLongBits(value));
    }

    public void writeFloatNoTag(float value) throws IOException {
        writeRawLittleEndian32(Float.floatToIntBits(value));
    }

    public void writeUInt64NoTag(long value) throws IOException {
        writeRawVarint64(value);
    }

    public void writeInt64NoTag(long value) throws IOException {
        writeRawVarint64(value);
    }

    public void writeInt32NoTag(int value) throws IOException {
        if (value >= 0) {
            writeRawVarint32(value);
        } else {
            writeRawVarint64((long) value);
        }
    }

    public void writeFixed64NoTag(long value) throws IOException {
        writeRawLittleEndian64(value);
    }

    public void writeFixed32NoTag(int value) throws IOException {
        writeRawLittleEndian32(value);
    }

    public void writeBoolNoTag(boolean value) throws IOException {
        writeRawByte(value ? 1 : 0);
    }

    public void writeStringNoTag(String value) throws IOException {
        try {
            int minLengthVarIntSize = computeRawVarint32Size(value.length());
            if (minLengthVarIntSize == computeRawVarint32Size(value.length() * 3)) {
                int oldPosition = this.buffer.position();
                if (this.buffer.remaining() < minLengthVarIntSize) {
                    throw new OutOfSpaceException(oldPosition + minLengthVarIntSize, this.buffer.limit());
                }
                this.buffer.position(oldPosition + minLengthVarIntSize);
                encode(value, this.buffer);
                int newPosition = this.buffer.position();
                this.buffer.position(oldPosition);
                writeRawVarint32((newPosition - oldPosition) - minLengthVarIntSize);
                this.buffer.position(newPosition);
                return;
            }
            writeRawVarint32(encodedLength(value));
            encode(value, this.buffer);
        } catch (BufferOverflowException e) {
            OutOfSpaceException outOfSpaceException = new OutOfSpaceException(this.buffer.position(), this.buffer.limit());
            outOfSpaceException.initCause(e);
            throw outOfSpaceException;
        }
    }

    private static int encodedLength(CharSequence sequence) {
        int utf16Length = sequence.length();
        int utf8Length = utf16Length;
        int i = 0;
        while (i < utf16Length && sequence.charAt(i) < 128) {
            i++;
        }
        while (i < utf16Length) {
            char c = sequence.charAt(i);
            if (c >= 2048) {
                utf8Length += encodedLengthGeneral(sequence, i);
                break;
            }
            utf8Length += (127 - c) >>> 31;
            i++;
        }
        if (utf8Length >= utf16Length) {
            return utf8Length;
        }
        throw new IllegalArgumentException("UTF-8 length does not fit in int: " + (((long) utf8Length) + ProtoOutputStream.FIELD_TYPE_DOUBLE));
    }

    private static int encodedLengthGeneral(CharSequence sequence, int start) {
        int utf16Length = sequence.length();
        int utf8Length = 0;
        int i = start;
        while (i < utf16Length) {
            char c = sequence.charAt(i);
            if (c < 2048) {
                utf8Length += (127 - c) >>> 31;
            } else {
                utf8Length += 2;
                if (55296 <= c && c <= 57343) {
                    if (Character.codePointAt(sequence, i) < 65536) {
                        throw new IllegalArgumentException("Unpaired surrogate at index " + i);
                    }
                    i++;
                }
            }
            i++;
        }
        return utf8Length;
    }

    private static void encode(CharSequence sequence, ByteBuffer byteBuffer) {
        if (byteBuffer.isReadOnly()) {
            throw new ReadOnlyBufferException();
        } else if (byteBuffer.hasArray()) {
            try {
                byteBuffer.position(encode(sequence, byteBuffer.array(), byteBuffer.arrayOffset() + byteBuffer.position(), byteBuffer.remaining()) - byteBuffer.arrayOffset());
            } catch (ArrayIndexOutOfBoundsException e) {
                BufferOverflowException boe = new BufferOverflowException();
                boe.initCause(e);
                throw boe;
            }
        } else {
            encodeDirect(sequence, byteBuffer);
        }
    }

    private static void encodeDirect(CharSequence sequence, ByteBuffer byteBuffer) {
        int utf16Length = sequence.length();
        int i = 0;
        while (i < utf16Length) {
            char c = sequence.charAt(i);
            if (c < 128) {
                byteBuffer.put((byte) c);
            } else if (c < 2048) {
                byteBuffer.put((byte) ((c >>> 6) | 960));
                byteBuffer.put((byte) ((c & 63) | 128));
            } else if (c < 55296 || 57343 < c) {
                byteBuffer.put((byte) ((c >>> 12) | 480));
                byteBuffer.put((byte) (((c >>> 6) & 63) | 128));
                byteBuffer.put((byte) ((c & 63) | 128));
            } else {
                if (i + 1 != sequence.length()) {
                    i++;
                    char low = sequence.charAt(i);
                    if ((Character.isSurrogatePair(c, low) ^ 1) == 0) {
                        int codePoint = Character.toCodePoint(c, low);
                        byteBuffer.put((byte) ((codePoint >>> 18) | 240));
                        byteBuffer.put((byte) (((codePoint >>> 12) & 63) | 128));
                        byteBuffer.put((byte) (((codePoint >>> 6) & 63) | 128));
                        byteBuffer.put((byte) ((codePoint & 63) | 128));
                    }
                }
                throw new IllegalArgumentException("Unpaired surrogate at index " + (i - 1));
            }
            i++;
        }
    }

    private static int encode(CharSequence sequence, byte[] bytes, int offset, int length) {
        char c;
        int utf16Length = sequence.length();
        int j = offset;
        int i = 0;
        int limit = offset + length;
        while (i < utf16Length && i + offset < limit) {
            c = sequence.charAt(i);
            if (c >= 128) {
                break;
            }
            bytes[offset + i] = (byte) c;
            i++;
        }
        if (i == utf16Length) {
            return offset + utf16Length;
        }
        int j2 = offset + i;
        while (i < utf16Length) {
            c = sequence.charAt(i);
            if (c < 128 && j2 < limit) {
                j = j2 + 1;
                bytes[j2] = (byte) c;
            } else if (c < 2048 && j2 <= limit - 2) {
                j = j2 + 1;
                bytes[j2] = (byte) ((c >>> 6) | 960);
                j2 = j + 1;
                bytes[j] = (byte) ((c & 63) | 128);
                j = j2;
            } else if ((c < 55296 || 57343 < c) && j2 <= limit - 3) {
                j = j2 + 1;
                bytes[j2] = (byte) ((c >>> 12) | 480);
                j2 = j + 1;
                bytes[j] = (byte) (((c >>> 6) & 63) | 128);
                j = j2 + 1;
                bytes[j2] = (byte) ((c & 63) | 128);
            } else if (j2 <= limit - 4) {
                if (i + 1 != sequence.length()) {
                    i++;
                    char low = sequence.charAt(i);
                    if ((Character.isSurrogatePair(c, low) ^ 1) == 0) {
                        int codePoint = Character.toCodePoint(c, low);
                        j = j2 + 1;
                        bytes[j2] = (byte) ((codePoint >>> 18) | 240);
                        j2 = j + 1;
                        bytes[j] = (byte) (((codePoint >>> 12) & 63) | 128);
                        j = j2 + 1;
                        bytes[j2] = (byte) (((codePoint >>> 6) & 63) | 128);
                        j2 = j + 1;
                        bytes[j] = (byte) ((codePoint & 63) | 128);
                        j = j2;
                    }
                }
                throw new IllegalArgumentException("Unpaired surrogate at index " + (i - 1));
            } else {
                throw new ArrayIndexOutOfBoundsException("Failed writing " + c + " at index " + j2);
            }
            i++;
            j2 = j;
        }
        return j2;
    }

    public void writeGroupNoTag(MessageNano value) throws IOException {
        value.writeTo(this);
    }

    public void writeMessageNoTag(MessageNano value) throws IOException {
        writeRawVarint32(value.getCachedSize());
        value.writeTo(this);
    }

    public void writeBytesNoTag(byte[] value) throws IOException {
        writeRawVarint32(value.length);
        writeRawBytes(value);
    }

    public void writeBytesNoTag(byte[] value, int offset, int length) throws IOException {
        writeRawVarint32(length);
        writeRawBytes(value, offset, length);
    }

    public void writeUInt32NoTag(int value) throws IOException {
        writeRawVarint32(value);
    }

    public void writeEnumNoTag(int value) throws IOException {
        writeRawVarint32(value);
    }

    public void writeSFixed32NoTag(int value) throws IOException {
        writeRawLittleEndian32(value);
    }

    public void writeSFixed64NoTag(long value) throws IOException {
        writeRawLittleEndian64(value);
    }

    public void writeSInt32NoTag(int value) throws IOException {
        writeRawVarint32(encodeZigZag32(value));
    }

    public void writeSInt64NoTag(long value) throws IOException {
        writeRawVarint64(encodeZigZag64(value));
    }

    public static int computeDoubleSize(int fieldNumber, double value) {
        return computeTagSize(fieldNumber) + computeDoubleSizeNoTag(value);
    }

    public static int computeFloatSize(int fieldNumber, float value) {
        return computeTagSize(fieldNumber) + computeFloatSizeNoTag(value);
    }

    public static int computeUInt64Size(int fieldNumber, long value) {
        return computeTagSize(fieldNumber) + computeUInt64SizeNoTag(value);
    }

    public static int computeInt64Size(int fieldNumber, long value) {
        return computeTagSize(fieldNumber) + computeInt64SizeNoTag(value);
    }

    public static int computeInt32Size(int fieldNumber, int value) {
        return computeTagSize(fieldNumber) + computeInt32SizeNoTag(value);
    }

    public static int computeFixed64Size(int fieldNumber, long value) {
        return computeTagSize(fieldNumber) + computeFixed64SizeNoTag(value);
    }

    public static int computeFixed32Size(int fieldNumber, int value) {
        return computeTagSize(fieldNumber) + computeFixed32SizeNoTag(value);
    }

    public static int computeBoolSize(int fieldNumber, boolean value) {
        return computeTagSize(fieldNumber) + computeBoolSizeNoTag(value);
    }

    public static int computeStringSize(int fieldNumber, String value) {
        return computeTagSize(fieldNumber) + computeStringSizeNoTag(value);
    }

    public static int computeGroupSize(int fieldNumber, MessageNano value) {
        return (computeTagSize(fieldNumber) * 2) + computeGroupSizeNoTag(value);
    }

    public static int computeMessageSize(int fieldNumber, MessageNano value) {
        return computeTagSize(fieldNumber) + computeMessageSizeNoTag(value);
    }

    public static int computeBytesSize(int fieldNumber, byte[] value) {
        return computeTagSize(fieldNumber) + computeBytesSizeNoTag(value);
    }

    public static int computeBytesSize(int fieldNumber, int length) {
        return computeTagSize(fieldNumber) + computeBytesSizeNoTag(length);
    }

    public static int computeUInt32Size(int fieldNumber, int value) {
        return computeTagSize(fieldNumber) + computeUInt32SizeNoTag(value);
    }

    public static int computeEnumSize(int fieldNumber, int value) {
        return computeTagSize(fieldNumber) + computeEnumSizeNoTag(value);
    }

    public static int computeSFixed32Size(int fieldNumber, int value) {
        return computeTagSize(fieldNumber) + computeSFixed32SizeNoTag(value);
    }

    public static int computeSFixed64Size(int fieldNumber, long value) {
        return computeTagSize(fieldNumber) + computeSFixed64SizeNoTag(value);
    }

    public static int computeSInt32Size(int fieldNumber, int value) {
        return computeTagSize(fieldNumber) + computeSInt32SizeNoTag(value);
    }

    public static int computeSInt64Size(int fieldNumber, long value) {
        return computeTagSize(fieldNumber) + computeSInt64SizeNoTag(value);
    }

    public static int computeDoubleSizeNoTag(double value) {
        return 8;
    }

    public static int computeFloatSizeNoTag(float value) {
        return 4;
    }

    public static int computeUInt64SizeNoTag(long value) {
        return computeRawVarint64Size(value);
    }

    public static int computeInt64SizeNoTag(long value) {
        return computeRawVarint64Size(value);
    }

    public static int computeInt32SizeNoTag(int value) {
        if (value >= 0) {
            return computeRawVarint32Size(value);
        }
        return 10;
    }

    public static int computeFixed64SizeNoTag(long value) {
        return 8;
    }

    public static int computeFixed32SizeNoTag(int value) {
        return 4;
    }

    public static int computeBoolSizeNoTag(boolean value) {
        return 1;
    }

    public static int computeStringSizeNoTag(String value) {
        int length = encodedLength(value);
        return computeRawVarint32Size(length) + length;
    }

    public static int computeGroupSizeNoTag(MessageNano value) {
        return value.getSerializedSize();
    }

    public static int computeMessageSizeNoTag(MessageNano value) {
        int size = value.getSerializedSize();
        return computeRawVarint32Size(size) + size;
    }

    public static int computeBytesSizeNoTag(byte[] value) {
        return computeRawVarint32Size(value.length) + value.length;
    }

    public static int computeBytesSizeNoTag(int length) {
        return computeRawVarint32Size(length) + length;
    }

    public static int computeUInt32SizeNoTag(int value) {
        return computeRawVarint32Size(value);
    }

    public static int computeEnumSizeNoTag(int value) {
        return computeRawVarint32Size(value);
    }

    public static int computeSFixed32SizeNoTag(int value) {
        return 4;
    }

    public static int computeSFixed64SizeNoTag(long value) {
        return 8;
    }

    public static int computeSInt32SizeNoTag(int value) {
        return computeRawVarint32Size(encodeZigZag32(value));
    }

    public static int computeSInt64SizeNoTag(long value) {
        return computeRawVarint64Size(encodeZigZag64(value));
    }

    public int spaceLeft() {
        return this.buffer.remaining();
    }

    public void checkNoSpaceLeft() {
        if (spaceLeft() != 0) {
            throw new IllegalStateException("Did not write as much data as expected.");
        }
    }

    public int position() {
        return this.buffer.position();
    }

    public void reset() {
        this.buffer.clear();
    }

    public void writeRawByte(byte value) throws IOException {
        if (this.buffer.hasRemaining()) {
            this.buffer.put(value);
            return;
        }
        throw new OutOfSpaceException(this.buffer.position(), this.buffer.limit());
    }

    public void writeRawByte(int value) throws IOException {
        writeRawByte((byte) value);
    }

    public void writeRawBytes(byte[] value) throws IOException {
        writeRawBytes(value, 0, value.length);
    }

    public void writeRawBytes(byte[] value, int offset, int length) throws IOException {
        if (this.buffer.remaining() >= length) {
            this.buffer.put(value, offset, length);
            return;
        }
        throw new OutOfSpaceException(this.buffer.position(), this.buffer.limit());
    }

    public void writeTag(int fieldNumber, int wireType) throws IOException {
        writeRawVarint32(WireFormatNano.makeTag(fieldNumber, wireType));
    }

    public static int computeTagSize(int fieldNumber) {
        return computeRawVarint32Size(WireFormatNano.makeTag(fieldNumber, 0));
    }

    public void writeRawVarint32(int value) throws IOException {
        while ((value & -128) != 0) {
            writeRawByte((value & 127) | 128);
            value >>>= 7;
        }
        writeRawByte(value);
    }

    public static int computeRawVarint32Size(int value) {
        if ((value & -128) == 0) {
            return 1;
        }
        if ((value & -16384) == 0) {
            return 2;
        }
        if ((-2097152 & value) == 0) {
            return 3;
        }
        if ((-268435456 & value) == 0) {
            return 4;
        }
        return 5;
    }

    public void writeRawVarint64(long value) throws IOException {
        while ((-128 & value) != 0) {
            writeRawByte((((int) value) & 127) | 128);
            value >>>= 7;
        }
        writeRawByte((int) value);
    }

    public static int computeRawVarint64Size(long value) {
        if ((-128 & value) == 0) {
            return 1;
        }
        if ((-16384 & value) == 0) {
            return 2;
        }
        if ((-2097152 & value) == 0) {
            return 3;
        }
        if ((-268435456 & value) == 0) {
            return 4;
        }
        if ((-34359738368L & value) == 0) {
            return 5;
        }
        if ((-4398046511104L & value) == 0) {
            return 6;
        }
        if ((-562949953421312L & value) == 0) {
            return 7;
        }
        if ((-72057594037927936L & value) == 0) {
            return 8;
        }
        if ((Long.MIN_VALUE & value) == 0) {
            return 9;
        }
        return 10;
    }

    public void writeRawLittleEndian32(int value) throws IOException {
        if (this.buffer.remaining() < 4) {
            throw new OutOfSpaceException(this.buffer.position(), this.buffer.limit());
        }
        this.buffer.putInt(value);
    }

    public void writeRawLittleEndian64(long value) throws IOException {
        if (this.buffer.remaining() < 8) {
            throw new OutOfSpaceException(this.buffer.position(), this.buffer.limit());
        }
        this.buffer.putLong(value);
    }

    public static int encodeZigZag32(int n) {
        return (n << 1) ^ (n >> 31);
    }

    public static long encodeZigZag64(long n) {
        return (n << 1) ^ (n >> 63);
    }

    static int computeFieldSize(int number, int type, Object object) {
        switch (type) {
            case 1:
                return computeDoubleSize(number, ((Double) object).doubleValue());
            case 2:
                return computeFloatSize(number, ((Float) object).floatValue());
            case 3:
                return computeInt64Size(number, ((Long) object).longValue());
            case 4:
                return computeUInt64Size(number, ((Long) object).longValue());
            case 5:
                return computeInt32Size(number, ((Integer) object).intValue());
            case 6:
                return computeFixed64Size(number, ((Long) object).longValue());
            case 7:
                return computeFixed32Size(number, ((Integer) object).intValue());
            case 8:
                return computeBoolSize(number, ((Boolean) object).booleanValue());
            case 9:
                return computeStringSize(number, (String) object);
            case 10:
                return computeGroupSize(number, (MessageNano) object);
            case 11:
                return computeMessageSize(number, (MessageNano) object);
            case 12:
                return computeBytesSize(number, (byte[]) object);
            case 13:
                return computeUInt32Size(number, ((Integer) object).intValue());
            case 14:
                return computeEnumSize(number, ((Integer) object).intValue());
            case 15:
                return computeSFixed32Size(number, ((Integer) object).intValue());
            case 16:
                return computeSFixed64Size(number, ((Long) object).longValue());
            case 17:
                return computeSInt32Size(number, ((Integer) object).intValue());
            case 18:
                return computeSInt64Size(number, ((Long) object).longValue());
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    void writeField(int number, int type, Object value) throws IOException {
        switch (type) {
            case 1:
                writeDouble(number, ((Double) value).doubleValue());
                return;
            case 2:
                writeFloat(number, ((Float) value).floatValue());
                return;
            case 3:
                writeInt64(number, ((Long) value).longValue());
                return;
            case 4:
                writeUInt64(number, ((Long) value).longValue());
                return;
            case 5:
                writeInt32(number, ((Integer) value).intValue());
                return;
            case 6:
                writeFixed64(number, ((Long) value).longValue());
                return;
            case 7:
                writeFixed32(number, ((Integer) value).intValue());
                return;
            case 8:
                writeBool(number, ((Boolean) value).booleanValue());
                return;
            case 9:
                writeString(number, (String) value);
                return;
            case 10:
                writeGroup(number, (MessageNano) value);
                return;
            case 11:
                writeMessage(number, (MessageNano) value);
                return;
            case 12:
                writeBytes(number, (byte[]) value);
                return;
            case 13:
                writeUInt32(number, ((Integer) value).intValue());
                return;
            case 14:
                writeEnum(number, ((Integer) value).intValue());
                return;
            case 15:
                writeSFixed32(number, ((Integer) value).intValue());
                return;
            case 16:
                writeSFixed64(number, ((Long) value).longValue());
                return;
            case 17:
                writeSInt32(number, ((Integer) value).intValue());
                return;
            case 18:
                writeSInt64(number, ((Long) value).longValue());
                return;
            default:
                throw new IOException("Unknown type: " + type);
        }
    }
}
