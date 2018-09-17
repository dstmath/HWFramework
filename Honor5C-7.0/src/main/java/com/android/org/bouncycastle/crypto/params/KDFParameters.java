package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.crypto.DerivationParameters;

public class KDFParameters implements DerivationParameters {
    byte[] iv;
    byte[] shared;

    public KDFParameters(byte[] shared, byte[] iv) {
        this.shared = shared;
        this.iv = iv;
    }

    public byte[] getSharedSecret() {
        return this.shared;
    }

    public byte[] getIV() {
        return this.iv;
    }
}
