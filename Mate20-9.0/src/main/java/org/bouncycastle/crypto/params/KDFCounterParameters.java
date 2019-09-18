package org.bouncycastle.crypto.params;

import org.bouncycastle.crypto.DerivationParameters;
import org.bouncycastle.util.Arrays;

public final class KDFCounterParameters implements DerivationParameters {
    private byte[] fixedInputDataCounterPrefix;
    private byte[] fixedInputDataCounterSuffix;
    private byte[] ki;
    private int r;

    public KDFCounterParameters(byte[] bArr, byte[] bArr2, int i) {
        this(bArr, null, bArr2, i);
    }

    public KDFCounterParameters(byte[] bArr, byte[] bArr2, byte[] bArr3, int i) {
        if (bArr != null) {
            this.ki = Arrays.clone(bArr);
            this.fixedInputDataCounterPrefix = bArr2 == null ? new byte[0] : Arrays.clone(bArr2);
            this.fixedInputDataCounterSuffix = bArr3 == null ? new byte[0] : Arrays.clone(bArr3);
            if (i == 8 || i == 16 || i == 24 || i == 32) {
                this.r = i;
                return;
            }
            throw new IllegalArgumentException("Length of counter should be 8, 16, 24 or 32");
        }
        throw new IllegalArgumentException("A KDF requires Ki (a seed) as input");
    }

    public byte[] getFixedInputData() {
        return Arrays.clone(this.fixedInputDataCounterSuffix);
    }

    public byte[] getFixedInputDataCounterPrefix() {
        return Arrays.clone(this.fixedInputDataCounterPrefix);
    }

    public byte[] getFixedInputDataCounterSuffix() {
        return Arrays.clone(this.fixedInputDataCounterSuffix);
    }

    public byte[] getKI() {
        return this.ki;
    }

    public int getR() {
        return this.r;
    }
}
