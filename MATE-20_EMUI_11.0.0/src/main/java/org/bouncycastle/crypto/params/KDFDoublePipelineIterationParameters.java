package org.bouncycastle.crypto.params;

import org.bouncycastle.crypto.DerivationParameters;
import org.bouncycastle.util.Arrays;

public final class KDFDoublePipelineIterationParameters implements DerivationParameters {
    private static final int UNUSED_R = 32;
    private final byte[] fixedInputData;
    private final byte[] ki;
    private final int r;
    private final boolean useCounter;

    private KDFDoublePipelineIterationParameters(byte[] bArr, byte[] bArr2, int i, boolean z) {
        if (bArr != null) {
            this.ki = Arrays.clone(bArr);
            this.fixedInputData = bArr2 == null ? new byte[0] : Arrays.clone(bArr2);
            if (i == 8 || i == 16 || i == 24 || i == 32) {
                this.r = i;
                this.useCounter = z;
                return;
            }
            throw new IllegalArgumentException("Length of counter should be 8, 16, 24 or 32");
        }
        throw new IllegalArgumentException("A KDF requires Ki (a seed) as input");
    }

    public static KDFDoublePipelineIterationParameters createWithCounter(byte[] bArr, byte[] bArr2, int i) {
        return new KDFDoublePipelineIterationParameters(bArr, bArr2, i, true);
    }

    public static KDFDoublePipelineIterationParameters createWithoutCounter(byte[] bArr, byte[] bArr2) {
        return new KDFDoublePipelineIterationParameters(bArr, bArr2, 32, false);
    }

    public byte[] getFixedInputData() {
        return Arrays.clone(this.fixedInputData);
    }

    public byte[] getKI() {
        return this.ki;
    }

    public int getR() {
        return this.r;
    }

    public boolean useCounter() {
        return this.useCounter;
    }
}
