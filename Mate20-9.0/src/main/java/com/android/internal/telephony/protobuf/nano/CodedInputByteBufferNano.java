package com.android.internal.telephony.protobuf.nano;

import com.android.internal.telephony.dataconnection.KeepaliveStatus;
import java.io.IOException;

public final class CodedInputByteBufferNano {
    private static final int DEFAULT_RECURSION_LIMIT = 64;
    private static final int DEFAULT_SIZE_LIMIT = 67108864;
    private final byte[] buffer;
    private int bufferPos;
    private int bufferSize;
    private int bufferSizeAfterLimit;
    private int bufferStart;
    private int currentLimit = KeepaliveStatus.INVALID_HANDLE;
    private int lastTag;
    private int recursionDepth;
    private int recursionLimit = 64;
    private int sizeLimit = DEFAULT_SIZE_LIMIT;

    public static CodedInputByteBufferNano newInstance(byte[] buf) {
        return newInstance(buf, 0, buf.length);
    }

    public static CodedInputByteBufferNano newInstance(byte[] buf, int off, int len) {
        return new CodedInputByteBufferNano(buf, off, len);
    }

    public int readTag() throws IOException {
        if (isAtEnd()) {
            this.lastTag = 0;
            return 0;
        }
        this.lastTag = readRawVarint32();
        if (this.lastTag != 0) {
            return this.lastTag;
        }
        throw InvalidProtocolBufferNanoException.invalidTag();
    }

    public void checkLastTagWas(int value) throws InvalidProtocolBufferNanoException {
        if (this.lastTag != value) {
            throw InvalidProtocolBufferNanoException.invalidEndTag();
        }
    }

    public boolean skipField(int tag) throws IOException {
        switch (WireFormatNano.getTagWireType(tag)) {
            case 0:
                readInt32();
                return true;
            case 1:
                readRawLittleEndian64();
                return true;
            case 2:
                skipRawBytes(readRawVarint32());
                return true;
            case 3:
                skipMessage();
                checkLastTagWas(WireFormatNano.makeTag(WireFormatNano.getTagFieldNumber(tag), 4));
                return true;
            case 4:
                return false;
            case 5:
                readRawLittleEndian32();
                return true;
            default:
                throw InvalidProtocolBufferNanoException.invalidWireType();
        }
    }

    public void skipMessage() throws IOException {
        int tag;
        do {
            tag = readTag();
            if (tag == 0) {
                return;
            }
        } while (skipField(tag));
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readRawLittleEndian64());
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readRawLittleEndian32());
    }

    public long readUInt64() throws IOException {
        return readRawVarint64();
    }

    public long readInt64() throws IOException {
        return readRawVarint64();
    }

    public int readInt32() throws IOException {
        return readRawVarint32();
    }

    public long readFixed64() throws IOException {
        return readRawLittleEndian64();
    }

    public int readFixed32() throws IOException {
        return readRawLittleEndian32();
    }

    public boolean readBool() throws IOException {
        return readRawVarint32() != 0;
    }

    public String readString() throws IOException {
        int size = readRawVarint32();
        if (size > this.bufferSize - this.bufferPos || size <= 0) {
            return new String(readRawBytes(size), InternalNano.UTF_8);
        }
        String result = new String(this.buffer, this.bufferPos, size, InternalNano.UTF_8);
        this.bufferPos += size;
        return result;
    }

    public void readGroup(MessageNano msg, int fieldNumber) throws IOException {
        if (this.recursionDepth < this.recursionLimit) {
            this.recursionDepth++;
            msg.mergeFrom(this);
            checkLastTagWas(WireFormatNano.makeTag(fieldNumber, 4));
            this.recursionDepth--;
            return;
        }
        throw InvalidProtocolBufferNanoException.recursionLimitExceeded();
    }

    public void readMessage(MessageNano msg) throws IOException {
        int length = readRawVarint32();
        if (this.recursionDepth < this.recursionLimit) {
            int oldLimit = pushLimit(length);
            this.recursionDepth++;
            msg.mergeFrom(this);
            checkLastTagWas(0);
            this.recursionDepth--;
            popLimit(oldLimit);
            return;
        }
        throw InvalidProtocolBufferNanoException.recursionLimitExceeded();
    }

    public byte[] readBytes() throws IOException {
        int size = readRawVarint32();
        if (size <= this.bufferSize - this.bufferPos && size > 0) {
            byte[] result = new byte[size];
            System.arraycopy(this.buffer, this.bufferPos, result, 0, size);
            this.bufferPos += size;
            return result;
        } else if (size == 0) {
            return WireFormatNano.EMPTY_BYTES;
        } else {
            return readRawBytes(size);
        }
    }

    public int readUInt32() throws IOException {
        return readRawVarint32();
    }

    public int readEnum() throws IOException {
        return readRawVarint32();
    }

    public int readSFixed32() throws IOException {
        return readRawLittleEndian32();
    }

    public long readSFixed64() throws IOException {
        return readRawLittleEndian64();
    }

    public int readSInt32() throws IOException {
        return decodeZigZag32(readRawVarint32());
    }

    public long readSInt64() throws IOException {
        return decodeZigZag64(readRawVarint64());
    }

    public int readRawVarint32() throws IOException {
        int result;
        int tmp = readRawByte();
        if (tmp >= 0) {
            return tmp;
        }
        int result2 = tmp & 127;
        byte readRawByte = readRawByte();
        byte tmp2 = readRawByte;
        if (readRawByte >= 0) {
            result = result2 | (tmp2 << 7);
        } else {
            int result3 = result2 | ((tmp2 & Byte.MAX_VALUE) << 7);
            byte readRawByte2 = readRawByte();
            byte tmp3 = readRawByte2;
            if (readRawByte2 >= 0) {
                result = result3 | (tmp3 << 14);
            } else {
                int result4 = result3 | ((tmp3 & Byte.MAX_VALUE) << 14);
                byte readRawByte3 = readRawByte();
                byte tmp4 = readRawByte3;
                if (readRawByte3 >= 0) {
                    result = result4 | (tmp4 << 21);
                } else {
                    byte tmp5 = readRawByte();
                    result = result4 | ((tmp4 & Byte.MAX_VALUE) << 21) | (tmp5 << 28);
                    if (tmp5 < 0) {
                        for (int i = 0; i < 5; i++) {
                            if (readRawByte() >= 0) {
                                return result;
                            }
                        }
                        throw InvalidProtocolBufferNanoException.malformedVarint();
                    }
                }
            }
        }
        return result;
    }

    public long readRawVarint64() throws IOException {
        long result = 0;
        for (int shift = 0; shift < 64; shift += 7) {
            byte b = readRawByte();
            result |= ((long) (b & Byte.MAX_VALUE)) << shift;
            if ((b & 128) == 0) {
                return result;
            }
        }
        throw InvalidProtocolBufferNanoException.malformedVarint();
    }

    public int readRawLittleEndian32() throws IOException {
        return (readRawByte() & 255) | ((readRawByte() & 255) << 8) | ((readRawByte() & 255) << 16) | ((readRawByte() & 255) << 24);
    }

    public long readRawLittleEndian64() throws IOException {
        return (((long) readRawByte()) & 255) | ((((long) readRawByte()) & 255) << 8) | ((((long) readRawByte()) & 255) << 16) | ((((long) readRawByte()) & 255) << 24) | ((((long) readRawByte()) & 255) << 32) | ((((long) readRawByte()) & 255) << 40) | ((((long) readRawByte()) & 255) << 48) | ((255 & ((long) readRawByte())) << 56);
    }

    public static int decodeZigZag32(int n) {
        return (n >>> 1) ^ (-(n & 1));
    }

    public static long decodeZigZag64(long n) {
        return (n >>> 1) ^ (-(1 & n));
    }

    private CodedInputByteBufferNano(byte[] buffer2, int off, int len) {
        this.buffer = buffer2;
        this.bufferStart = off;
        this.bufferSize = off + len;
        this.bufferPos = off;
    }

    public int setRecursionLimit(int limit) {
        if (limit >= 0) {
            int oldLimit = this.recursionLimit;
            this.recursionLimit = limit;
            return oldLimit;
        }
        throw new IllegalArgumentException("Recursion limit cannot be negative: " + limit);
    }

    public int setSizeLimit(int limit) {
        if (limit >= 0) {
            int oldLimit = this.sizeLimit;
            this.sizeLimit = limit;
            return oldLimit;
        }
        throw new IllegalArgumentException("Size limit cannot be negative: " + limit);
    }

    public void resetSizeCounter() {
    }

    public int pushLimit(int byteLimit) throws InvalidProtocolBufferNanoException {
        if (byteLimit >= 0) {
            int byteLimit2 = byteLimit + this.bufferPos;
            int oldLimit = this.currentLimit;
            if (byteLimit2 <= oldLimit) {
                this.currentLimit = byteLimit2;
                recomputeBufferSizeAfterLimit();
                return oldLimit;
            }
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        }
        throw InvalidProtocolBufferNanoException.negativeSize();
    }

    private void recomputeBufferSizeAfterLimit() {
        this.bufferSize += this.bufferSizeAfterLimit;
        int bufferEnd = this.bufferSize;
        if (bufferEnd > this.currentLimit) {
            this.bufferSizeAfterLimit = bufferEnd - this.currentLimit;
            this.bufferSize -= this.bufferSizeAfterLimit;
            return;
        }
        this.bufferSizeAfterLimit = 0;
    }

    public void popLimit(int oldLimit) {
        this.currentLimit = oldLimit;
        recomputeBufferSizeAfterLimit();
    }

    public int getBytesUntilLimit() {
        if (this.currentLimit == Integer.MAX_VALUE) {
            return -1;
        }
        return this.currentLimit - this.bufferPos;
    }

    public boolean isAtEnd() {
        return this.bufferPos == this.bufferSize;
    }

    public int getPosition() {
        return this.bufferPos - this.bufferStart;
    }

    public int getAbsolutePosition() {
        return this.bufferPos;
    }

    public byte[] getBuffer() {
        return this.buffer;
    }

    public byte[] getData(int offset, int length) {
        if (length == 0) {
            return WireFormatNano.EMPTY_BYTES;
        }
        byte[] copy = new byte[length];
        System.arraycopy(this.buffer, this.bufferStart + offset, copy, 0, length);
        return copy;
    }

    public void rewindToPosition(int position) {
        if (position > this.bufferPos - this.bufferStart) {
            throw new IllegalArgumentException("Position " + position + " is beyond current " + (this.bufferPos - this.bufferStart));
        } else if (position >= 0) {
            this.bufferPos = this.bufferStart + position;
        } else {
            throw new IllegalArgumentException("Bad position " + position);
        }
    }

    public byte readRawByte() throws IOException {
        if (this.bufferPos != this.bufferSize) {
            byte[] bArr = this.buffer;
            int i = this.bufferPos;
            this.bufferPos = i + 1;
            return bArr[i];
        }
        throw InvalidProtocolBufferNanoException.truncatedMessage();
    }

    public byte[] readRawBytes(int size) throws IOException {
        if (size < 0) {
            throw InvalidProtocolBufferNanoException.negativeSize();
        } else if (this.bufferPos + size > this.currentLimit) {
            skipRawBytes(this.currentLimit - this.bufferPos);
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        } else if (size <= this.bufferSize - this.bufferPos) {
            byte[] bytes = new byte[size];
            System.arraycopy(this.buffer, this.bufferPos, bytes, 0, size);
            this.bufferPos += size;
            return bytes;
        } else {
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        }
    }

    public void skipRawBytes(int size) throws IOException {
        if (size < 0) {
            throw InvalidProtocolBufferNanoException.negativeSize();
        } else if (this.bufferPos + size > this.currentLimit) {
            skipRawBytes(this.currentLimit - this.bufferPos);
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        } else if (size <= this.bufferSize - this.bufferPos) {
            this.bufferPos += size;
        } else {
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        }
    }

    /* access modifiers changed from: package-private */
    public Object readPrimitiveField(int type) throws IOException {
        switch (type) {
            case 1:
                return Double.valueOf(readDouble());
            case 2:
                return Float.valueOf(readFloat());
            case 3:
                return Long.valueOf(readInt64());
            case 4:
                return Long.valueOf(readUInt64());
            case 5:
                return Integer.valueOf(readInt32());
            case 6:
                return Long.valueOf(readFixed64());
            case 7:
                return Integer.valueOf(readFixed32());
            case 8:
                return Boolean.valueOf(readBool());
            case 9:
                return readString();
            case 12:
                return readBytes();
            case 13:
                return Integer.valueOf(readUInt32());
            case 14:
                return Integer.valueOf(readEnum());
            case 15:
                return Integer.valueOf(readSFixed32());
            case 16:
                return Long.valueOf(readSFixed64());
            case 17:
                return Integer.valueOf(readSInt32());
            case 18:
                return Long.valueOf(readSInt64());
            default:
                throw new IllegalArgumentException("Unknown type " + type);
        }
    }
}
