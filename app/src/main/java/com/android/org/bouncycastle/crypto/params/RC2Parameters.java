package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.asn1.x509.ReasonFlags;

public class RC2Parameters extends KeyParameter {
    private int bits;

    public RC2Parameters(byte[] key) {
        this(key, key.length > ReasonFlags.unused ? 1024 : key.length * 8);
    }

    public RC2Parameters(byte[] key, int bits) {
        super(key);
        this.bits = bits;
    }

    public int getEffectiveKeyBits() {
        return this.bits;
    }
}
