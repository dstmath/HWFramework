package com.android.org.conscrypt;

import java.util.Arrays;

final class ByteArray {
    private final byte[] bytes;
    private final int hashCode;

    ByteArray(byte[] bytes) {
        this.bytes = bytes;
        this.hashCode = Arrays.hashCode(bytes);
    }

    public int hashCode() {
        return this.hashCode;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ByteArray)) {
            return false;
        }
        return Arrays.equals(this.bytes, ((ByteArray) o).bytes);
    }
}
