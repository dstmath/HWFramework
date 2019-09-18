package com.android.org.bouncycastle.crypto.params;

public class RC2Parameters extends KeyParameter {
    private int bits;

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public RC2Parameters(byte[] key) {
        this(key, key.length > 128 ? 1024 : key.length * 8);
    }

    public RC2Parameters(byte[] key, int bits2) {
        super(key);
        this.bits = bits2;
    }

    public int getEffectiveKeyBits() {
        return this.bits;
    }
}
