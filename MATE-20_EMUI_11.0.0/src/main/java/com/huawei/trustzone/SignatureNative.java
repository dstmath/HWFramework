package com.huawei.trustzone;

import android.util.Log;
import com.huawei.annotation.HwSystemApi;

public class SignatureNative {
    private static final String TAG = "SignatureNative";

    @HwSystemApi
    public static native int checkDeviceKey();

    @HwSystemApi
    public static native int sign(byte[] bArr, int i, byte[] bArr2);

    @HwSystemApi
    public static native int teecClose();

    @HwSystemApi
    public static native int teecInit();

    static {
        try {
            System.loadLibrary("trustzone_signature");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "LoadLibrary occurs error " + e.toString());
        }
    }
}
