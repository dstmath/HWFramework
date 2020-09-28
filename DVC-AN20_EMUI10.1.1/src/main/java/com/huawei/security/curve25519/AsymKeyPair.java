package com.huawei.security.curve25519;

import java.util.Arrays;

public class AsymKeyPair {
    private byte[] mPrivateKey;
    private byte[] mPublicKey;

    public byte[] getPrivateKey() {
        byte[] bArr = this.mPrivateKey;
        return bArr == null ? new byte[0] : Arrays.copyOf(bArr, bArr.length);
    }

    public void setPrivateKey(byte[] privateKey) {
        this.mPrivateKey = privateKey == null ? new byte[0] : Arrays.copyOf(privateKey, privateKey.length);
    }

    public byte[] getPublicKey() {
        byte[] bArr = this.mPublicKey;
        return bArr == null ? new byte[0] : Arrays.copyOf(bArr, bArr.length);
    }

    public void setPublicKey(byte[] publicKey) {
        this.mPublicKey = publicKey == null ? new byte[0] : Arrays.copyOf(publicKey, publicKey.length);
    }
}
