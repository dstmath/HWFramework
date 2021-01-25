package org.bouncycastle.crypto.params;

import org.bouncycastle.crypto.CipherParameters;

public class ParametersWithID implements CipherParameters {
    private byte[] id;
    private CipherParameters parameters;

    public ParametersWithID(CipherParameters cipherParameters, byte[] bArr) {
        this.parameters = cipherParameters;
        this.id = bArr;
    }

    public byte[] getID() {
        return this.id;
    }

    public CipherParameters getParameters() {
        return this.parameters;
    }
}
