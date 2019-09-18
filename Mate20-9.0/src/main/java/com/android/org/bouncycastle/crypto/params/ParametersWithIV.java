package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.crypto.CipherParameters;

public class ParametersWithIV implements CipherParameters {
    private byte[] iv;
    private CipherParameters parameters;

    public ParametersWithIV(CipherParameters parameters2, byte[] iv2) {
        this(parameters2, iv2, 0, iv2.length);
    }

    public ParametersWithIV(CipherParameters parameters2, byte[] iv2, int ivOff, int ivLen) {
        this.iv = new byte[ivLen];
        this.parameters = parameters2;
        System.arraycopy(iv2, ivOff, this.iv, 0, ivLen);
    }

    public byte[] getIV() {
        return this.iv;
    }

    public CipherParameters getParameters() {
        return this.parameters;
    }
}
