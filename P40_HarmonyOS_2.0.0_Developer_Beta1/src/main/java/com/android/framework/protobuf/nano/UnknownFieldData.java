package com.android.framework.protobuf.nano;

import java.io.IOException;
import java.util.Arrays;

final class UnknownFieldData {
    final byte[] bytes;
    final int tag;

    UnknownFieldData(int tag2, byte[] bytes2) {
        this.tag = tag2;
        this.bytes = bytes2;
    }

    /* access modifiers changed from: package-private */
    public int computeSerializedSize() {
        return 0 + CodedOutputByteBufferNano.computeRawVarint32Size(this.tag) + this.bytes.length;
    }

    /* access modifiers changed from: package-private */
    public void writeTo(CodedOutputByteBufferNano output) throws IOException {
        output.writeRawVarint32(this.tag);
        output.writeRawBytes(this.bytes);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof UnknownFieldData)) {
            return false;
        }
        UnknownFieldData other = (UnknownFieldData) o;
        if (this.tag != other.tag || !Arrays.equals(this.bytes, other.bytes)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (((17 * 31) + this.tag) * 31) + Arrays.hashCode(this.bytes);
    }
}
