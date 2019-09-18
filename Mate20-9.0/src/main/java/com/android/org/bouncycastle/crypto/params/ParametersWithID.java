package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.crypto.CipherParameters;

public class ParametersWithID implements CipherParameters {
    private byte[] id;
    private CipherParameters parameters;

    public ParametersWithID(CipherParameters parameters2, byte[] id2) {
        this.parameters = parameters2;
        this.id = id2;
    }

    public byte[] getID() {
        return this.id;
    }

    public CipherParameters getParameters() {
        return this.parameters;
    }
}
