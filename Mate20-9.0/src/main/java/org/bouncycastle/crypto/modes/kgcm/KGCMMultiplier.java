package org.bouncycastle.crypto.modes.kgcm;

public interface KGCMMultiplier {
    void init(long[] jArr);

    void multiplyH(long[] jArr);
}
