package android.rms.iaware;

import android.os.Parcel;

public class IAwareSdkCore {
    private static String TAG = "iAwareSdkCore";
    private static boolean mNativeAvailabe;

    private static native boolean nativeHandleEvent(int i, Parcel parcel, Parcel parcel2, int i2);

    static {
        mNativeAvailabe = true;
        try {
            AwareLog.d(TAG, "Load libiAwareSdk_jni.so");
            System.loadLibrary("iAwareSdk_jni");
        } catch (UnsatisfiedLinkError e) {
            AwareLog.e(TAG, "ERROR: Could not load libiAwareSdk_jni.so");
            mNativeAvailabe = false;
        }
    }

    public static boolean handleEvent(int code, Parcel data, Parcel reply, int extraInfo) {
        if (mNativeAvailabe) {
            return nativeHandleEvent(code, data, reply, extraInfo);
        }
        return false;
    }

    public static boolean handleEvent(int code, Parcel data, Parcel reply) {
        return handleEvent(code, data, reply, -1);
    }
}
