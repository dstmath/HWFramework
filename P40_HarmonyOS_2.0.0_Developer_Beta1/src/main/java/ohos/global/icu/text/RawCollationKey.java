package ohos.global.icu.text;

import ohos.global.icu.util.ByteArrayWrapper;

public final class RawCollationKey extends ByteArrayWrapper {
    public RawCollationKey() {
    }

    public RawCollationKey(int i) {
        this.bytes = new byte[i];
    }

    public RawCollationKey(byte[] bArr) {
        this.bytes = bArr;
    }

    public RawCollationKey(byte[] bArr, int i) {
        super(bArr, i);
    }

    public int compareTo(RawCollationKey rawCollationKey) {
        int compareTo = super.compareTo((ByteArrayWrapper) rawCollationKey);
        if (compareTo < 0) {
            return -1;
        }
        return compareTo == 0 ? 0 : 1;
    }
}
