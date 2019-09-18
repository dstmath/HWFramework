package android.rms.iaware;

import android.util.Log;

public final class AwareLog {
    private static final boolean HWDBG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static final boolean HWLOGW_E = true;
    private static final boolean HWVERBOSE = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 2)));
    private static final String TAG = "AwareLog";

    static {
        boolean z = true;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(TAG, 4))) {
            z = false;
        }
        HWFLOW = z;
    }

    public static void v(String tag, String msg) {
        if (HWVERBOSE) {
            Log.v(TAG, tag + ": " + msg);
        }
    }

    public static void d(String tag, String msg) {
        if (HWDBG) {
            Log.d(TAG, tag + ": " + msg);
        }
    }

    public static void i(String tag, String msg) {
        if (HWFLOW) {
            Log.i(TAG, tag + ": " + msg);
        }
    }

    public static void w(String tag, String msg) {
        Log.w(TAG, tag + ": " + msg);
    }

    public static void e(String tag, String msg) {
        Log.e(TAG, tag + ": " + msg);
    }
}
