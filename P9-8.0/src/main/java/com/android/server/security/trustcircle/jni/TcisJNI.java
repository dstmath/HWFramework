package com.android.server.security.trustcircle.jni;

import android.content.Context;
import com.android.server.security.trustcircle.utils.LogHelper;

public class TcisJNI {
    public static final String TAG = TcisJNI.class.getSimpleName();

    public static final native byte[] nativeProcessCmd(Context context, byte[] bArr);

    public static final native int nativeStart();

    public static final native void nativeStop();

    static {
        try {
            System.loadLibrary("tcisjni_ca");
        } catch (UnsatisfiedLinkError e) {
            LogHelper.e(TAG, "error, load tcisjni_ca failed");
        }
    }

    public static final byte[] processCmd(Context context, byte[] param) {
        byte[] response;
        synchronized (TcisJNI.class) {
            try {
                response = nativeProcessCmd(context, param);
            } catch (UnsatisfiedLinkError e) {
                LogHelper.e(TAG, "error, processCmd failed, " + e.getMessage());
                response = new byte[0];
            }
        }
        return response;
    }

    public static final int start() {
        int result;
        synchronized (TcisJNI.class) {
            result = -1;
            try {
                result = nativeStart();
            } catch (UnsatisfiedLinkError e) {
                LogHelper.e(TAG, "error, nativeStart failed, " + e.getMessage());
            }
        }
        return result;
    }

    public static final void stop() {
        synchronized (TcisJNI.class) {
            try {
                nativeStop();
            } catch (UnsatisfiedLinkError e) {
                LogHelper.e(TAG, "error, nativeStop failed, " + e.getMessage());
            }
        }
        return;
    }
}
