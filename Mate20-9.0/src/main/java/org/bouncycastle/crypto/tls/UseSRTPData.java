package org.bouncycastle.crypto.tls;

public class UseSRTPData {
    protected byte[] mki;
    protected int[] protectionProfiles;

    public UseSRTPData(int[] iArr, byte[] bArr) {
        if (iArr == null || iArr.length < 1 || iArr.length >= 32768) {
            throw new IllegalArgumentException("'protectionProfiles' must have length from 1 to (2^15 - 1)");
        }
        if (bArr == null) {
            bArr = TlsUtils.EMPTY_BYTES;
        } else if (bArr.length > 255) {
            throw new IllegalArgumentException("'mki' cannot be longer than 255 bytes");
        }
        this.protectionProfiles = iArr;
        this.mki = bArr;
    }

    public byte[] getMki() {
        return this.mki;
    }

    public int[] getProtectionProfiles() {
        return this.protectionProfiles;
    }
}
