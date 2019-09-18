package com.android.server.security.ukey.jni;

import android.util.Log;
import java.nio.charset.Charset;

public class UKeyJNI {
    public static final String TAG = UKeyJNI.class.getSimpleName();

    public static final native int nativeIsUKeySwitchDisabled(byte[] bArr);

    public static final native int nativeSetUKeySwitchDisabled(byte[] bArr, byte[] bArr2, boolean z);

    public static final native int nativeStart();

    public static final native void nativeStop();

    static {
        try {
            System.loadLibrary("ukeyjni_ca");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "error, load ukeyjni_ca failed");
        }
    }

    public static final int setUKeySwitchDisabled(String packageName, String ukeyId, boolean isEnabled) {
        int result = -1;
        if (packageName == null || ukeyId == null) {
            return -1;
        }
        synchronized (UKeyJNI.class) {
            try {
                result = nativeSetUKeySwitchDisabled(packageName.getBytes(Charset.forName("UTF-8")), ukeyId.getBytes(Charset.forName("UTF-8")), isEnabled);
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "error, setUKeySwitchEnabled failed");
            }
        }
        return result;
    }

    public static final int isUKeySwitchDisabled(String packageName) {
        if (packageName == null) {
            return -1;
        }
        int result = -1;
        synchronized (UKeyJNI.class) {
            try {
                result = nativeIsUKeySwitchDisabled(packageName.getBytes(Charset.forName("UTF-8")));
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "error, setUKeySwitchEnabled failed");
            }
        }
        return result;
    }

    public static final int start() {
        int result;
        synchronized (UKeyJNI.class) {
            result = -1;
            try {
                result = nativeStart();
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "error, nativeStart failed");
            }
        }
        return result;
    }

    public static final void stop() {
        synchronized (UKeyJNI.class) {
            try {
                nativeStop();
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "error, nativeStop failed");
            }
        }
    }
}
