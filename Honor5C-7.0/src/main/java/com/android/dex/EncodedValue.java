package com.android.dex;

import com.android.dex.Dex.Section;
import com.android.dex.util.ByteArrayByteInput;
import com.android.dex.util.ByteInput;
import dalvik.bytecode.Opcodes;

public final class EncodedValue implements Comparable<EncodedValue> {
    private final byte[] data;

    public EncodedValue(byte[] data) {
        this.data = data;
    }

    public ByteInput asByteInput() {
        return new ByteArrayByteInput(this.data);
    }

    public byte[] getBytes() {
        return this.data;
    }

    public void writeTo(Section out) {
        out.write(this.data);
    }

    public int compareTo(EncodedValue other) {
        int size = Math.min(this.data.length, other.data.length);
        for (int i = 0; i < size; i++) {
            if (this.data[i] != other.data[i]) {
                return (this.data[i] & Opcodes.OP_CONST_CLASS_JUMBO) - (other.data[i] & Opcodes.OP_CONST_CLASS_JUMBO);
            }
        }
        return this.data.length - other.data.length;
    }

    public String toString() {
        return Integer.toHexString(this.data[0] & Opcodes.OP_CONST_CLASS_JUMBO) + "...(" + this.data.length + ")";
    }
}
