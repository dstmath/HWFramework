package com.android.internal.telephony.protobuf.nano;

import java.io.IOException;
import java.util.Arrays;

final class UnknownFieldData {
    final byte[] bytes;
    final int tag;

    UnknownFieldData(int tag, byte[] bytes) {
        this.tag = tag;
        this.bytes = bytes;
    }

    int computeSerializedSize() {
        return (CodedOutputByteBufferNano.computeRawVarint32Size(this.tag) + 0) + this.bytes.length;
    }

    void writeTo(CodedOutputByteBufferNano output) throws IOException {
        output.writeRawVarint32(this.tag);
        output.writeRawBytes(this.bytes);
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == this) {
            return true;
        }
        if (!(o instanceof UnknownFieldData)) {
            return false;
        }
        UnknownFieldData other = (UnknownFieldData) o;
        if (this.tag == other.tag) {
            z = Arrays.equals(this.bytes, other.bytes);
        }
        return z;
    }

    public int hashCode() {
        return ((this.tag + 527) * 31) + Arrays.hashCode(this.bytes);
    }
}
