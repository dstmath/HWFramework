package android.os;

import android.util.MutableInt;
import com.android.internal.annotations.GuardedBy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class SystemProperties {
    public static final int PROP_NAME_MAX = Integer.MAX_VALUE;
    public static final int PROP_VALUE_MAX = 91;
    private static final String TAG = "SystemProperties";
    private static final boolean TRACK_KEY_ACCESS = false;
    private static final ArrayList<Runnable> sChangeCallbacks = new ArrayList();
    @GuardedBy("sRoReads")
    private static final HashMap<String, MutableInt> sRoReads = null;

    private static native void native_add_change_callback();

    private static native String native_get(String str);

    private static native String native_get(String str, String str2);

    private static native boolean native_get_boolean(String str, boolean z);

    private static native int native_get_int(String str, int i);

    private static native long native_get_long(String str, long j);

    private static native void native_report_sysprop_change();

    private static native void native_set(String str, String str2);

    private static void onKeyAccess(String key) {
    }

    public static String get(String key) {
        return native_get(key);
    }

    public static String get(String key, String def) {
        return native_get(key, def);
    }

    public static int getInt(String key, int def) {
        return native_get_int(key, def);
    }

    public static long getLong(String key, long def) {
        return native_get_long(key, def);
    }

    public static boolean getBoolean(String key, boolean def) {
        return native_get_boolean(key, def);
    }

    public static void set(String key, String val) {
        if (val == null || val.length() <= 91) {
            native_set(key, val);
            return;
        }
        throw newValueTooLargeException(key, val);
    }

    public static void addChangeCallback(Runnable callback) {
        synchronized (sChangeCallbacks) {
            if (sChangeCallbacks.size() == 0) {
                native_add_change_callback();
            }
            sChangeCallbacks.add(callback);
        }
    }

    public static boolean getRTLFlag() {
        if (Locale.getDefault().getLanguage().contains("ar") || Locale.getDefault().getLanguage().contains("iw") || Locale.getDefault().getLanguage().contains("fa") || Locale.getDefault().getLanguage().contains("ur")) {
            return true;
        }
        return Locale.getDefault().getLanguage().contains("ug");
    }

    static void callChangeCallbacks() {
        synchronized (sChangeCallbacks) {
            if (sChangeCallbacks.size() == 0) {
                return;
            }
            ArrayList<Runnable> callbacks = new ArrayList(sChangeCallbacks);
            for (int i = 0; i < callbacks.size(); i++) {
                ((Runnable) callbacks.get(i)).run();
            }
        }
    }

    private static IllegalArgumentException newValueTooLargeException(String key, String value) {
        return new IllegalArgumentException("value of system property '" + key + "' is longer than " + 91 + " characters: " + value);
    }

    public static void reportSyspropChanged() {
        native_report_sysprop_change();
    }
}
