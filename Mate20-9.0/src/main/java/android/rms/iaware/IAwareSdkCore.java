package android.rms.iaware;

import android.os.Parcel;

public class IAwareSdkCore {
    static final int FIRST_ASYNC_CALL_TRANSACTION = 10001;
    static final int FIRST_SYNC_CALL_TRANSACTION = 1;
    static final int LAST_ASYNC_CALL_TRANSACTION = 16777215;
    static final int LAST_SYNC_CALL_TRANSACTION = 10000;
    private static String TAG = "iAwareSdkCore";
    private static boolean mNativeAvailabe;

    private static native boolean nativeHandleEvent(int i, Parcel parcel, Parcel parcel2, int i2);

    static {
        mNativeAvailabe = false;
        try {
            AwareLog.d(TAG, "Load libiAwareSdk_jni.so");
            System.loadLibrary("iAwareSdk_jni");
            mNativeAvailabe = true;
        } catch (UnsatisfiedLinkError e) {
            AwareLog.e(TAG, "ERROR: Could not load libiAwareSdk_jni.so");
        }
    }

    public static boolean handleEvent(int code, Parcel data, Parcel reply, int extraInfo) {
        if (!mNativeAvailabe) {
            return false;
        }
        return nativeHandleEvent(code, data, reply, extraInfo);
    }

    public static boolean handleEvent(int code, Parcel data, Parcel reply) {
        return handleEvent(code, data, reply, -1);
    }
}
