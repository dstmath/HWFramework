package org.bouncycastle.crypto.modes.gcm;

public interface GCMExponentiator {
    void exponentiateX(long j, byte[] bArr);

    void init(byte[] bArr);
}
