package com.huawei.security.curve25519;

public class X25519Algorithm {
    private static native byte[] computeSharedSecretNative(byte[] bArr, byte[] bArr2);

    private static native AsymKeyPair keypairNative();

    private X25519Algorithm() {
    }

    static {
        System.loadLibrary("curve25519_jni");
    }

    public static AsymKeyPair keypair() {
        return keypairNative();
    }

    public static byte[] computeSharedSecret(byte[] selfPrivateKey, byte[] peerPublicKey) {
        return computeSharedSecretNative(selfPrivateKey, peerPublicKey);
    }
}
