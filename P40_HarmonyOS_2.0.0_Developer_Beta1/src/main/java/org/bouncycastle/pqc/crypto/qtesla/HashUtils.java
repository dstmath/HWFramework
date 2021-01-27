package org.bouncycastle.pqc.crypto.qtesla;

import org.bouncycastle.crypto.digests.CSHAKEDigest;
import org.bouncycastle.crypto.digests.SHAKEDigest;

class HashUtils {
    static final int SECURE_HASH_ALGORITHM_KECCAK_128_RATE = 168;
    static final int SECURE_HASH_ALGORITHM_KECCAK_256_RATE = 136;

    HashUtils() {
    }

    static void customizableSecureHashAlgorithmKECCAK128Simple(byte[] bArr, int i, int i2, short s, byte[] bArr2, int i3, int i4) {
        CSHAKEDigest cSHAKEDigest = new CSHAKEDigest(128, null, new byte[]{(byte) s, (byte) (s >> 8)});
        cSHAKEDigest.update(bArr2, i3, i4);
        cSHAKEDigest.doFinal(bArr, i, i2);
    }

    static void customizableSecureHashAlgorithmKECCAK256Simple(byte[] bArr, int i, int i2, short s, byte[] bArr2, int i3, int i4) {
        CSHAKEDigest cSHAKEDigest = new CSHAKEDigest(256, null, new byte[]{(byte) s, (byte) (s >> 8)});
        cSHAKEDigest.update(bArr2, i3, i4);
        cSHAKEDigest.doFinal(bArr, i, i2);
    }

    static void secureHashAlgorithmKECCAK128(byte[] bArr, int i, int i2, byte[] bArr2, int i3, int i4) {
        SHAKEDigest sHAKEDigest = new SHAKEDigest(128);
        sHAKEDigest.update(bArr2, i3, i4);
        sHAKEDigest.doFinal(bArr, i, i2);
    }

    static void secureHashAlgorithmKECCAK256(byte[] bArr, int i, int i2, byte[] bArr2, int i3, int i4) {
        SHAKEDigest sHAKEDigest = new SHAKEDigest(256);
        sHAKEDigest.update(bArr2, i3, i4);
        sHAKEDigest.doFinal(bArr, i, i2);
    }
}
