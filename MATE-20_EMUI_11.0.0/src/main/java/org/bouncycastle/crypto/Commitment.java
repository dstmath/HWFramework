package org.bouncycastle.crypto;

public class Commitment {
    private final byte[] commitment;
    private final byte[] secret;

    public Commitment(byte[] bArr, byte[] bArr2) {
        this.secret = bArr;
        this.commitment = bArr2;
    }

    public byte[] getCommitment() {
        return this.commitment;
    }

    public byte[] getSecret() {
        return this.secret;
    }
}
