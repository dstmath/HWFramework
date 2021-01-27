package android.util.proto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public final class ProtoInputStream extends ProtoStream {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    public static final int NO_MORE_FIELDS = -1;
    private static final byte STATE_FIELD_MISS = 4;
    private static final byte STATE_READING_PACKED = 2;
    private static final byte STATE_STARTED_FIELD_READ = 1;
    private byte[] mBuffer;
    private final int mBufferSize;
    private int mDepth;
    private int mDiscardedBytes;
    private int mEnd;
    private ArrayList<Long> mExpectedObjectTokenStack;
    private int mFieldNumber;
    private int mOffset;
    private int mPackedEnd;
    private byte mState;
    private InputStream mStream;
    private int mWireType;

    public ProtoInputStream(InputStream stream, int bufferSize) {
        this.mState = 0;
        this.mExpectedObjectTokenStack = null;
        this.mDepth = -1;
        this.mDiscardedBytes = 0;
        this.mOffset = 0;
        this.mEnd = 0;
        this.mPackedEnd = 0;
        this.mStream = stream;
        if (bufferSize > 0) {
            this.mBufferSize = bufferSize;
        } else {
            this.mBufferSize = 8192;
        }
        this.mBuffer = new byte[this.mBufferSize];
    }

    public ProtoInputStream(InputStream stream) {
        this(stream, 8192);
    }

    public ProtoInputStream(byte[] buffer) {
        this.mState = 0;
        this.mExpectedObjectTokenStack = null;
        this.mDepth = -1;
        this.mDiscardedBytes = 0;
        this.mOffset = 0;
        this.mEnd = 0;
        this.mPackedEnd = 0;
        this.mBufferSize = buffer.length;
        this.mEnd = buffer.length;
        this.mBuffer = buffer;
        this.mStream = null;
    }

    public int getFieldNumber() {
        return this.mFieldNumber;
    }

    public int getWireType() {
        if ((this.mState & 2) == 2) {
            return 2;
        }
        return this.mWireType;
    }

    public int getOffset() {
        return this.mOffset + this.mDiscardedBytes;
    }

    public int nextField() throws IOException {
        byte b = this.mState;
        if ((b & 4) == 4) {
            this.mState = (byte) (b & -5);
            return this.mFieldNumber;
        }
        if ((b & 1) == 1) {
            skip();
            this.mState = (byte) (this.mState & -2);
        }
        if ((this.mState & 2) == 2) {
            if (getOffset() < this.mPackedEnd) {
                this.mState = (byte) (this.mState | 1);
                return this.mFieldNumber;
            } else if (getOffset() == this.mPackedEnd) {
                this.mState = (byte) (this.mState & -3);
            } else {
                throw new ProtoParseException("Unexpectedly reached end of packed field at offset 0x" + Integer.toHexString(this.mPackedEnd) + dumpDebugData());
            }
        }
        if (this.mDepth < 0 || getOffset() != getOffsetFromToken(this.mExpectedObjectTokenStack.get(this.mDepth).longValue())) {
            readTag();
        } else {
            this.mFieldNumber = -1;
        }
        return this.mFieldNumber;
    }

    public boolean isNextField(long fieldId) throws IOException {
        if (nextField() == ((int) fieldId)) {
            return true;
        }
        this.mState = (byte) (this.mState | 4);
        return false;
    }

    public double readDouble(long fieldId) throws IOException {
        assertFreshData();
        assertFieldNumber(fieldId);
        checkPacked(fieldId);
        if (((int) ((ProtoStream.FIELD_TYPE_MASK & fieldId) >>> 32)) == 1) {
            assertWireType(1);
            double value = Double.longBitsToDouble(readFixed64());
            this.mState = (byte) (this.mState & -2);
            return value;
        }
        throw new IllegalArgumentException("Requested field id (" + getFieldIdString(fieldId) + ") cannot be read as a double" + dumpDebugData());
    }

    public float readFloat(long fieldId) throws IOException {
        assertFreshData();
        assertFieldNumber(fieldId);
        checkPacked(fieldId);
        if (((int) ((ProtoStream.FIELD_TYPE_MASK & fieldId) >>> 32)) == 2) {
            assertWireType(5);
            float value = Float.intBitsToFloat(readFixed32());
            this.mState = (byte) (this.mState & -2);
            return value;
        }
        throw new IllegalArgumentException("Requested field id (" + getFieldIdString(fieldId) + ") is not a float" + dumpDebugData());
    }

    public int readInt(long fieldId) throws IOException {
        int value;
        assertFreshData();
        assertFieldNumber(fieldId);
        checkPacked(fieldId);
        int i = (int) ((ProtoStream.FIELD_TYPE_MASK & fieldId) >>> 32);
        if (i != 5) {
            if (i != 7) {
                if (i != 17) {
                    switch (i) {
                        case 13:
                        case 14:
                            break;
                        case 15:
                            break;
                        default:
                            throw new IllegalArgumentException("Requested field id (" + getFieldIdString(fieldId) + ") is not an int" + dumpDebugData());
                    }
                    this.mState = (byte) (this.mState & -2);
                    return value;
                }
                assertWireType(0);
                value = decodeZigZag32((int) readVarint());
                this.mState = (byte) (this.mState & -2);
                return value;
            }
            assertWireType(5);
            value = readFixed32();
            this.mState = (byte) (this.mState & -2);
            return value;
        }
        assertWireType(0);
        value = (int) readVarint();
        this.mState = (byte) (this.mState & -2);
        return value;
    }

    public long readLong(long fieldId) throws IOException {
        long value;
        assertFreshData();
        assertFieldNumber(fieldId);
        checkPacked(fieldId);
        int i = (int) ((ProtoStream.FIELD_TYPE_MASK & fieldId) >>> 32);
        if (i == 3 || i == 4) {
            assertWireType(0);
            value = readVarint();
        } else if (i == 6 || i == 16) {
            assertWireType(1);
            value = readFixed64();
        } else if (i == 18) {
            assertWireType(0);
            value = decodeZigZag64(readVarint());
        } else {
            throw new IllegalArgumentException("Requested field id (" + getFieldIdString(fieldId) + ") is not an long" + dumpDebugData());
        }
        this.mState = (byte) (this.mState & -2);
        return value;
    }

    public boolean readBoolean(long fieldId) throws IOException {
        assertFreshData();
        assertFieldNumber(fieldId);
        checkPacked(fieldId);
        if (((int) ((ProtoStream.FIELD_TYPE_MASK & fieldId) >>> 32)) == 8) {
            boolean value = false;
            assertWireType(0);
            if (readVarint() != 0) {
                value = true;
            }
            this.mState = (byte) (this.mState & -2);
            return value;
        }
        throw new IllegalArgumentException("Requested field id (" + getFieldIdString(fieldId) + ") is not an boolean" + dumpDebugData());
    }

    public String readString(long fieldId) throws IOException {
        assertFreshData();
        assertFieldNumber(fieldId);
        if (((int) ((ProtoStream.FIELD_TYPE_MASK & fieldId) >>> 32)) == 9) {
            assertWireType(2);
            String value = readRawString((int) readVarint());
            this.mState = (byte) (this.mState & -2);
            return value;
        }
        throw new IllegalArgumentException("Requested field id(" + getFieldIdString(fieldId) + ") is not an string" + dumpDebugData());
    }

    public byte[] readBytes(long fieldId) throws IOException {
        assertFreshData();
        assertFieldNumber(fieldId);
        int i = (int) ((ProtoStream.FIELD_TYPE_MASK & fieldId) >>> 32);
        if (i == 11 || i == 12) {
            assertWireType(2);
            byte[] value = readRawBytes((int) readVarint());
            this.mState = (byte) (this.mState & -2);
            return value;
        }
        throw new IllegalArgumentException("Requested field type (" + getFieldIdString(fieldId) + ") cannot be read as raw bytes" + dumpDebugData());
    }

    public long start(long fieldId) throws IOException {
        assertFreshData();
        assertFieldNumber(fieldId);
        assertWireType(2);
        int messageSize = (int) readVarint();
        if (this.mExpectedObjectTokenStack == null) {
            this.mExpectedObjectTokenStack = new ArrayList<>();
        }
        int i = this.mDepth + 1;
        this.mDepth = i;
        if (i == this.mExpectedObjectTokenStack.size()) {
            this.mExpectedObjectTokenStack.add(Long.valueOf(makeToken(0, (fieldId & ProtoStream.FIELD_COUNT_REPEATED) == ProtoStream.FIELD_COUNT_REPEATED, this.mDepth, (int) fieldId, getOffset() + messageSize)));
        } else {
            this.mExpectedObjectTokenStack.set(this.mDepth, Long.valueOf(makeToken(0, (fieldId & ProtoStream.FIELD_COUNT_REPEATED) == ProtoStream.FIELD_COUNT_REPEATED, this.mDepth, (int) fieldId, getOffset() + messageSize)));
        }
        int i2 = this.mDepth;
        if (i2 <= 0 || getOffsetFromToken(this.mExpectedObjectTokenStack.get(i2).longValue()) <= getOffsetFromToken(this.mExpectedObjectTokenStack.get(this.mDepth - 1).longValue())) {
            this.mState = (byte) (this.mState & -2);
            return this.mExpectedObjectTokenStack.get(this.mDepth).longValue();
        }
        throw new ProtoParseException("Embedded Object (" + token2String(this.mExpectedObjectTokenStack.get(this.mDepth).longValue()) + ") ends after of parent Objects's (" + token2String(this.mExpectedObjectTokenStack.get(this.mDepth - 1).longValue()) + ") end" + dumpDebugData());
    }

    public void end(long token) {
        if (this.mExpectedObjectTokenStack.get(this.mDepth).longValue() == token) {
            if (getOffsetFromToken(this.mExpectedObjectTokenStack.get(this.mDepth).longValue()) > getOffset()) {
                incOffset(getOffsetFromToken(this.mExpectedObjectTokenStack.get(this.mDepth).longValue()) - getOffset());
            }
            this.mDepth--;
            this.mState = (byte) (this.mState & -2);
            return;
        }
        throw new ProtoParseException("end token " + token + " does not match current message token " + this.mExpectedObjectTokenStack.get(this.mDepth) + dumpDebugData());
    }

    private void readTag() throws IOException {
        fillBuffer();
        if (this.mOffset >= this.mEnd) {
            this.mFieldNumber = -1;
            return;
        }
        int tag = (int) readVarint();
        this.mFieldNumber = tag >>> 3;
        this.mWireType = tag & 7;
        this.mState = (byte) (this.mState | 1);
    }

    public int decodeZigZag32(int n) {
        return (n >>> 1) ^ (-(n & 1));
    }

    public long decodeZigZag64(long n) {
        return (n >>> 1) ^ (-(1 & n));
    }

    private long readVarint() throws IOException {
        long value = 0;
        int shift = 0;
        while (true) {
            fillBuffer();
            int fragment = this.mEnd - this.mOffset;
            for (int i = 0; i < fragment; i++) {
                byte b = this.mBuffer[this.mOffset + i];
                value |= (((long) b) & 127) << shift;
                if ((b & 128) == 0) {
                    incOffset(i + 1);
                    return value;
                }
                shift += 7;
                if (shift > 63) {
                    throw new ProtoParseException("Varint is too large at offset 0x" + Integer.toHexString(getOffset() + i) + dumpDebugData());
                }
            }
            incOffset(fragment);
        }
    }

    private int readFixed32() throws IOException {
        if (this.mOffset + 4 <= this.mEnd) {
            incOffset(4);
            byte[] bArr = this.mBuffer;
            int i = this.mOffset;
            return ((bArr[i - 1] & 255) << 24) | (bArr[i - 4] & 255) | ((bArr[i - 3] & 255) << 8) | ((bArr[i - 2] & 255) << 16);
        }
        int value = 0;
        int shift = 0;
        int bytesLeft = 4;
        while (bytesLeft > 0) {
            fillBuffer();
            int i2 = this.mEnd;
            int i3 = this.mOffset;
            int fragment = i2 - i3 < bytesLeft ? i2 - i3 : bytesLeft;
            incOffset(fragment);
            bytesLeft -= fragment;
            while (fragment > 0) {
                value |= (this.mBuffer[this.mOffset - fragment] & 255) << shift;
                fragment--;
                shift += 8;
            }
        }
        return value;
    }

    private long readFixed64() throws IOException {
        if (this.mOffset + 8 <= this.mEnd) {
            incOffset(8);
            byte[] bArr = this.mBuffer;
            int i = this.mOffset;
            return ((((long) bArr[i - 1]) & 255) << 56) | (((long) bArr[i - 8]) & 255) | ((((long) bArr[i - 7]) & 255) << 8) | ((((long) bArr[i - 6]) & 255) << 16) | ((((long) bArr[i - 5]) & 255) << 24) | ((((long) bArr[i - 4]) & 255) << 32) | ((((long) bArr[i - 3]) & 255) << 40) | ((((long) bArr[i - 2]) & 255) << 48);
        }
        long value = 0;
        int shift = 0;
        int bytesLeft = 8;
        while (bytesLeft > 0) {
            fillBuffer();
            int i2 = this.mEnd;
            int i3 = this.mOffset;
            int fragment = i2 - i3 < bytesLeft ? i2 - i3 : bytesLeft;
            incOffset(fragment);
            bytesLeft -= fragment;
            while (fragment > 0) {
                value |= (((long) this.mBuffer[this.mOffset - fragment]) & 255) << shift;
                fragment--;
                shift += 8;
            }
        }
        return value;
    }

    private byte[] readRawBytes(int n) throws IOException {
        byte[] buffer = new byte[n];
        int pos = 0;
        do {
            int i = this.mOffset;
            int i2 = (i + n) - pos;
            int i3 = this.mEnd;
            if (i2 > i3) {
                int fragment = i3 - i;
                if (fragment > 0) {
                    System.arraycopy(this.mBuffer, i, buffer, pos, fragment);
                    incOffset(fragment);
                    pos += fragment;
                }
                fillBuffer();
            } else {
                System.arraycopy(this.mBuffer, i, buffer, pos, n - pos);
                incOffset(n - pos);
                return buffer;
            }
        } while (this.mOffset < this.mEnd);
        throw new ProtoParseException("Unexpectedly reached end of the InputStream at offset 0x" + Integer.toHexString(this.mEnd) + dumpDebugData());
    }

    private String readRawString(int n) throws IOException {
        fillBuffer();
        int i = this.mOffset;
        int i2 = i + n;
        int i3 = this.mEnd;
        if (i2 <= i3) {
            String value = new String(this.mBuffer, i, n, StandardCharsets.UTF_8);
            incOffset(n);
            return value;
        } else if (n > this.mBufferSize) {
            return new String(readRawBytes(n), 0, n, StandardCharsets.UTF_8);
        } else {
            int stringHead = i3 - i;
            byte[] bArr = this.mBuffer;
            System.arraycopy(bArr, i, bArr, 0, stringHead);
            this.mEnd = this.mStream.read(this.mBuffer, stringHead, n - stringHead) + stringHead;
            this.mDiscardedBytes += this.mOffset;
            this.mOffset = 0;
            String value2 = new String(this.mBuffer, this.mOffset, n, StandardCharsets.UTF_8);
            incOffset(n);
            return value2;
        }
    }

    private void fillBuffer() throws IOException {
        InputStream inputStream;
        int i = this.mOffset;
        int i2 = this.mEnd;
        if (i >= i2 && (inputStream = this.mStream) != null) {
            this.mOffset = i - i2;
            this.mDiscardedBytes += i2;
            int i3 = this.mOffset;
            int i4 = this.mBufferSize;
            if (i3 >= i4) {
                int skipped = (int) inputStream.skip((long) ((i3 / i4) * i4));
                this.mDiscardedBytes += skipped;
                this.mOffset -= skipped;
            }
            this.mEnd = this.mStream.read(this.mBuffer);
        }
    }

    public void skip() throws IOException {
        byte b;
        if ((this.mState & 2) == 2) {
            incOffset(this.mPackedEnd - getOffset());
        } else {
            int i = this.mWireType;
            if (i == 0) {
                do {
                    fillBuffer();
                    b = this.mBuffer[this.mOffset];
                    incOffset(1);
                } while ((b & 128) != 0);
            } else if (i == 1) {
                incOffset(8);
            } else if (i == 2) {
                fillBuffer();
                incOffset((int) readVarint());
            } else if (i == 5) {
                incOffset(4);
            } else {
                throw new ProtoParseException("Unexpected wire type: " + this.mWireType + " at offset 0x" + Integer.toHexString(this.mOffset) + dumpDebugData());
            }
        }
        this.mState = (byte) (this.mState & -2);
    }

    private void incOffset(int n) {
        this.mOffset += n;
        if (this.mDepth >= 0 && getOffset() > getOffsetFromToken(this.mExpectedObjectTokenStack.get(this.mDepth).longValue())) {
            throw new ProtoParseException("Unexpectedly reached end of embedded object.  " + token2String(this.mExpectedObjectTokenStack.get(this.mDepth).longValue()) + dumpDebugData());
        }
    }

    private void checkPacked(long fieldId) throws IOException {
        if (this.mWireType == 2) {
            int length = (int) readVarint();
            this.mPackedEnd = getOffset() + length;
            this.mState = (byte) (2 | this.mState);
            switch ((int) ((ProtoStream.FIELD_TYPE_MASK & fieldId) >>> 32)) {
                case 1:
                case 6:
                case 16:
                    if (length % 8 == 0) {
                        this.mWireType = 1;
                        return;
                    }
                    throw new IllegalArgumentException("Requested field id (" + getFieldIdString(fieldId) + ") packed length " + length + " is not aligned for fixed64" + dumpDebugData());
                case 2:
                case 7:
                case 15:
                    if (length % 4 == 0) {
                        this.mWireType = 5;
                        return;
                    }
                    throw new IllegalArgumentException("Requested field id (" + getFieldIdString(fieldId) + ") packed length " + length + " is not aligned for fixed32" + dumpDebugData());
                case 3:
                case 4:
                case 5:
                case 8:
                case 13:
                case 14:
                case 17:
                case 18:
                    this.mWireType = 0;
                    return;
                case 9:
                case 10:
                case 11:
                case 12:
                default:
                    throw new IllegalArgumentException("Requested field id (" + getFieldIdString(fieldId) + ") is not a packable field" + dumpDebugData());
            }
        }
    }

    private void assertFieldNumber(long fieldId) {
        if (((int) fieldId) != this.mFieldNumber) {
            throw new IllegalArgumentException("Requested field id (" + getFieldIdString(fieldId) + ") does not match current field number (0x" + Integer.toHexString(this.mFieldNumber) + ") at offset 0x" + Integer.toHexString(getOffset()) + dumpDebugData());
        }
    }

    private void assertWireType(int wireType) {
        if (wireType != this.mWireType) {
            throw new WireTypeMismatchException("Current wire type " + getWireTypeString(this.mWireType) + " does not match expected wire type " + getWireTypeString(wireType) + " at offset 0x" + Integer.toHexString(getOffset()) + dumpDebugData());
        }
    }

    private void assertFreshData() {
        if ((this.mState & 1) != 1) {
            throw new ProtoParseException("Attempting to read already read field at offset 0x" + Integer.toHexString(getOffset()) + dumpDebugData());
        }
    }

    public String dumpDebugData() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nmFieldNumber : 0x" + Integer.toHexString(this.mFieldNumber));
        sb.append("\nmWireType : 0x" + Integer.toHexString(this.mWireType));
        sb.append("\nmState : 0x" + Integer.toHexString(this.mState));
        sb.append("\nmDiscardedBytes : 0x" + Integer.toHexString(this.mDiscardedBytes));
        sb.append("\nmOffset : 0x" + Integer.toHexString(this.mOffset));
        sb.append("\nmExpectedObjectTokenStack : ");
        ArrayList<Long> arrayList = this.mExpectedObjectTokenStack;
        if (arrayList == null) {
            sb.append("null");
        } else {
            sb.append(arrayList);
        }
        sb.append("\nmDepth : 0x" + Integer.toHexString(this.mDepth));
        sb.append("\nmBuffer : ");
        byte[] bArr = this.mBuffer;
        if (bArr == null) {
            sb.append("null");
        } else {
            sb.append(bArr);
        }
        sb.append("\nmBufferSize : 0x" + Integer.toHexString(this.mBufferSize));
        sb.append("\nmEnd : 0x" + Integer.toHexString(this.mEnd));
        return sb.toString();
    }
}
