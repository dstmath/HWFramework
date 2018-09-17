package com.android.server.security.ccmode;

public class CCModeFuncInternal {
    public static native boolean native_boringSslSelftest();

    public static native boolean native_dxCryptoSelftest();

    public static native boolean native_kernelCryptoSelftest();

    static {
        System.loadLibrary("HwCustMdppSelftest");
    }
}
