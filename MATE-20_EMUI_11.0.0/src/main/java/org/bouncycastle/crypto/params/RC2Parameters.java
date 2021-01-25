package org.bouncycastle.crypto.params;

public class RC2Parameters extends KeyParameter {
    private int bits;

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public RC2Parameters(byte[] bArr) {
        this(bArr, bArr.length > 128 ? 1024 : bArr.length * 8);
    }

    public RC2Parameters(byte[] bArr, int i) {
        super(bArr);
        this.bits = i;
    }

    public int getEffectiveKeyBits() {
        return this.bits;
    }
}
