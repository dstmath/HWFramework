package android.hwrme;

import android.util.Log;

public final class HwResMngEngine {
    public static final int ACTION_APP_REMOVED = 4;
    public static final int ACTION_APP_STARTUP_BEGIN = 0;
    public static final int ACTION_APP_STARTUP_END = 1;
    public static final int ACTION_SCREEN_OFF = 3;
    public static final int ACTION_SCREEN_ON = 2;
    private static final String TAG = "hwrme javaFilter";
    private static HwResMngEngine hwResMngEng = null;

    private static native void nativeHwResMngMmEvent(int i, int i2);

    static {
        try {
            System.loadLibrary("hwrme_jni");
        } catch (UnsatisfiedLinkError e) {
            Log.e("promm", "hwrme_jni not found!");
        }
    }

    public static synchronized HwResMngEngine getInstance() {
        HwResMngEngine hwResMngEngine;
        synchronized (HwResMngEngine.class) {
            if (hwResMngEng == null) {
                hwResMngEng = new HwResMngEngine();
            }
            hwResMngEngine = hwResMngEng;
        }
        return hwResMngEngine;
    }

    public void sendMmEvent(int event, int val) {
        nativeHwResMngMmEvent(event, val);
    }
}
