package org.bouncycastle.jce.spec;

import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.util.Arrays;

public class IESParameterSpec implements AlgorithmParameterSpec {
    private int cipherKeySize;
    private byte[] derivation;
    private byte[] encoding;
    private int macKeySize;
    private byte[] nonce;
    private boolean usePointCompression;

    public IESParameterSpec(byte[] bArr, byte[] bArr2, int i) {
        this(bArr, bArr2, i, -1, null, false);
    }

    public IESParameterSpec(byte[] bArr, byte[] bArr2, int i, int i2, byte[] bArr3) {
        this(bArr, bArr2, i, i2, bArr3, false);
    }

    public IESParameterSpec(byte[] bArr, byte[] bArr2, int i, int i2, byte[] bArr3, boolean z) {
        if (bArr != null) {
            this.derivation = new byte[bArr.length];
            System.arraycopy(bArr, 0, this.derivation, 0, bArr.length);
        } else {
            this.derivation = null;
        }
        if (bArr2 != null) {
            this.encoding = new byte[bArr2.length];
            System.arraycopy(bArr2, 0, this.encoding, 0, bArr2.length);
        } else {
            this.encoding = null;
        }
        this.macKeySize = i;
        this.cipherKeySize = i2;
        this.nonce = Arrays.clone(bArr3);
        this.usePointCompression = z;
    }

    public int getCipherKeySize() {
        return this.cipherKeySize;
    }

    public byte[] getDerivationV() {
        return Arrays.clone(this.derivation);
    }

    public byte[] getEncodingV() {
        return Arrays.clone(this.encoding);
    }

    public int getMacKeySize() {
        return this.macKeySize;
    }

    public byte[] getNonce() {
        return Arrays.clone(this.nonce);
    }

    public boolean getPointCompression() {
        return this.usePointCompression;
    }

    public void setPointCompression(boolean z) {
        this.usePointCompression = z;
    }
}
