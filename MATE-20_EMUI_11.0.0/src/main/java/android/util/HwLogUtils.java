package android.util;

import android.os.SystemClock;
import android.os.SystemProperties;

public final class HwLogUtils {
    private static final int EVENT_MAXTIME = 500;
    private static final boolean IS_DEBUG_VERSION;
    private static final String TAG = "HwLogUtils";

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        IS_DEBUG_VERSION = z;
    }

    private HwLogUtils() {
    }

    public static boolean isDebugVersion() {
        return IS_DEBUG_VERSION;
    }

    public static void checkTime(long startTime, String where, Object name) {
        long now = SystemClock.uptimeMillis();
        if (now - startTime > 500) {
            Slog.w(TAG, "Slow operation : " + (now - startTime) + "ms so far, " + where + " object: " + name);
        }
    }
}
