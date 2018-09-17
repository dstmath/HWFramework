package com.android.framework.protobuf.nano;

import java.io.IOException;
import java.util.Arrays;

public abstract class MessageNano {
    protected volatile int cachedSize = -1;

    public abstract MessageNano mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException;

    public int getCachedSize() {
        if (this.cachedSize < 0) {
            getSerializedSize();
        }
        return this.cachedSize;
    }

    public int getSerializedSize() {
        int size = computeSerializedSize();
        this.cachedSize = size;
        return size;
    }

    protected int computeSerializedSize() {
        return 0;
    }

    public void writeTo(CodedOutputByteBufferNano output) throws IOException {
    }

    public static final byte[] toByteArray(MessageNano msg) {
        byte[] result = new byte[msg.getSerializedSize()];
        toByteArray(msg, result, 0, result.length);
        return result;
    }

    public static final void toByteArray(MessageNano msg, byte[] data, int offset, int length) {
        try {
            CodedOutputByteBufferNano output = CodedOutputByteBufferNano.newInstance(data, offset, length);
            msg.writeTo(output);
            output.checkNoSpaceLeft();
        } catch (IOException e) {
            throw new RuntimeException("Serializing to a byte array threw an IOException (should never happen).", e);
        }
    }

    public static final <T extends MessageNano> T mergeFrom(T msg, byte[] data) throws InvalidProtocolBufferNanoException {
        return mergeFrom(msg, data, 0, data.length);
    }

    public static final <T extends MessageNano> T mergeFrom(T msg, byte[] data, int off, int len) throws InvalidProtocolBufferNanoException {
        try {
            CodedInputByteBufferNano input = CodedInputByteBufferNano.newInstance(data, off, len);
            msg.mergeFrom(input);
            input.checkLastTagWas(0);
            return msg;
        } catch (InvalidProtocolBufferNanoException e) {
            throw e;
        } catch (IOException e2) {
            throw new RuntimeException("Reading from a byte array threw an IOException (should never happen).");
        }
    }

    /* JADX WARNING: Missing block: B:6:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static final boolean messageNanoEquals(MessageNano a, MessageNano b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null || a.getClass() != b.getClass()) {
            return false;
        }
        int serializedSize = a.getSerializedSize();
        if (b.getSerializedSize() != serializedSize) {
            return false;
        }
        byte[] aByteArray = new byte[serializedSize];
        byte[] bByteArray = new byte[serializedSize];
        toByteArray(a, aByteArray, 0, serializedSize);
        toByteArray(b, bByteArray, 0, serializedSize);
        return Arrays.equals(aByteArray, bByteArray);
    }

    public String toString() {
        return MessageNanoPrinter.print(this);
    }

    public MessageNano clone() throws CloneNotSupportedException {
        return (MessageNano) super.clone();
    }
}
