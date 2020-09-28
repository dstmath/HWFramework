package huawei.android.security.facerecognition.utils;

import android.util.Log;
import com.huawei.util.LogEx;

public class LogUtil {
    private static final String SEPARATOR = " - ";
    private static final String TAG = "FaceR";
    private static boolean debugSwitch = false;
    private static boolean errorSwitch = false;
    private static boolean infoSwitch = false;
    private static boolean switchInited = false;

    public static void v(String tag, String msg) {
        if (debugLoggable()) {
            Log.v(TAG, tag + SEPARATOR + msg);
        }
    }

    public static void v(String tag, String... msg) {
        if (debugLoggable()) {
            Log.v(TAG, tag + SEPARATOR + appendString(msg));
        }
    }

    public static void d(String tag, String msg) {
        if (debugLoggable()) {
            Log.d(TAG, tag + SEPARATOR + msg);
        }
    }

    public static void d(String tag, String... msg) {
        if (debugLoggable()) {
            Log.d(TAG, tag + SEPARATOR + appendString(msg));
        }
    }

    public static void i(String tag, String msg) {
        if (infoLoggable()) {
            Log.i(TAG, tag + SEPARATOR + msg);
        }
    }

    public static void i(String tag, String... msg) {
        if (infoLoggable()) {
            Log.i(TAG, tag + SEPARATOR + appendString(msg));
        }
    }

    public static void w(String tag, String msg) {
        if (errorLoggable()) {
            Log.w(TAG, tag + SEPARATOR + msg);
        }
    }

    public static void w(String tag, String... msg) {
        w(tag, appendString(msg));
    }

    public static void e(String tag, String msg) {
        if (errorLoggable()) {
            Log.e(TAG, tag + SEPARATOR + msg);
        }
    }

    private static String appendString(String... msg) {
        StringBuilder builder = new StringBuilder();
        for (String s : msg) {
            builder.append(s);
            builder.append(" ");
        }
        return builder.toString().trim();
    }

    private static boolean debugLoggable() {
        if (!switchInited) {
            doInitSwitch();
        }
        return debugSwitch;
    }

    private static boolean infoLoggable() {
        if (!switchInited) {
            doInitSwitch();
        }
        return infoSwitch;
    }

    private static boolean errorLoggable() {
        if (!switchInited) {
            doInitSwitch();
        }
        return errorSwitch;
    }

    private static synchronized void doInitSwitch() {
        boolean z;
        synchronized (LogUtil.class) {
            if (!switchInited) {
                boolean z2 = false;
                if (!LogEx.getLogHWInfo()) {
                    if (!LogEx.getHWModuleLog() || !Log.isLoggable(TAG, 3)) {
                        z = false;
                        debugSwitch = z;
                        if (LogEx.getLogHWInfo() || (LogEx.getHWModuleLog() && Log.isLoggable(TAG, 4))) {
                            z2 = true;
                        }
                        infoSwitch = z2;
                        errorSwitch = true;
                        Log.i(TAG, "debugSwitch:" + debugSwitch + ", infoSwitch:" + infoSwitch + ", errorSwitch:" + errorSwitch);
                        switchInited = true;
                    }
                }
                z = true;
                debugSwitch = z;
                z2 = true;
                infoSwitch = z2;
                errorSwitch = true;
                Log.i(TAG, "debugSwitch:" + debugSwitch + ", infoSwitch:" + infoSwitch + ", errorSwitch:" + errorSwitch);
                switchInited = true;
            }
        }
    }
}
