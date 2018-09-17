package com.android.org.conscrypt;

class NativeCryptoJni {
    public static void init() {
        System.loadLibrary("javacrypto");
    }

    private NativeCryptoJni() {
    }
}
