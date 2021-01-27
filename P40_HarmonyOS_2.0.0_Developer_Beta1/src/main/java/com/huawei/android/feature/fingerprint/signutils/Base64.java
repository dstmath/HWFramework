package com.huawei.android.feature.fingerprint.signutils;

import android.util.Log;
import com.huawei.android.feature.BuildConfig;

public abstract class Base64 {
    private static final String TAG = "Base64";

    public static byte[] decode(String str) {
        if (str != null) {
            try {
                byte[] decode = android.util.Base64.decode(str, 2);
                if (decode != null) {
                    return decode;
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Decoding with Base64 IllegalArgumentException:", e);
            }
        }
        return new byte[0];
    }

    public static String encode(byte[] bArr) {
        if (bArr != null) {
            try {
                String encodeToString = android.util.Base64.encodeToString(bArr, 2);
                if (encodeToString != null) {
                    return encodeToString;
                }
            } catch (AssertionError e) {
                Log.e(TAG, "An exception occurred while encoding with Base64,AssertionError:", e);
            }
        }
        return BuildConfig.FLAVOR;
    }
}
