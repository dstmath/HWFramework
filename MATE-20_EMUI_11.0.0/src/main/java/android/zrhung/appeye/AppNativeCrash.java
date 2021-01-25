package android.zrhung.appeye;

import android.util.Log;

public class AppNativeCrash {
    private static final String TAG = "AppNativeCrash";
    private boolean isEnableNativeCrashHandle = true;
    private boolean isNativeHandlerInited = false;

    private native int nregisterNativeCrashHandler(Object obj);

    public void initNativeCrashHandler(Object singleton) {
        if (!this.isNativeHandlerInited) {
            this.isNativeHandlerInited = true;
            try {
                System.loadLibrary("nativedfrappeyehandler_jni");
            } catch (UnsatisfiedLinkError e) {
                this.isEnableNativeCrashHandle = false;
                Log.e(TAG, "nativedfrappeyehandler_jni library not found!");
            } catch (Exception e2) {
                Log.e(TAG, "unexcepted exception!");
                this.isEnableNativeCrashHandle = false;
            }
            if (this.isEnableNativeCrashHandle && singleton != null && nregisterNativeCrashHandler(singleton) == 0) {
                this.isEnableNativeCrashHandle = false;
            }
        }
    }
}
