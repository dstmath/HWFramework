package android.icu.text;

import android.icu.util.ByteArrayWrapper;

public final class RawCollationKey extends ByteArrayWrapper {
    public RawCollationKey(int capacity) {
        this.bytes = new byte[capacity];
    }

    public RawCollationKey(byte[] bytes) {
        this.bytes = bytes;
    }

    public RawCollationKey(byte[] bytesToAdopt, int size) {
        super(bytesToAdopt, size);
    }

    public int compareTo(RawCollationKey rhs) {
        int result = super.compareTo((ByteArrayWrapper) rhs);
        if (result < 0) {
            return -1;
        }
        if (result != 0) {
            return 1;
        }
        return 0;
    }
}
