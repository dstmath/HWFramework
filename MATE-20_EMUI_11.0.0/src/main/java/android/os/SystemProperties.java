package android.os;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.util.MutableInt;
import com.android.internal.annotations.GuardedBy;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import libcore.util.HexEncoding;

@SystemApi
public class SystemProperties {
    @UnsupportedAppUsage
    public static final int PROP_NAME_MAX = Integer.MAX_VALUE;
    public static final int PROP_VALUE_MAX = 91;
    private static final String TAG = "SystemProperties";
    private static final boolean TRACK_KEY_ACCESS = false;
    @UnsupportedAppUsage
    @GuardedBy({"sChangeCallbacks"})
    private static final ArrayList<Runnable> sChangeCallbacks = new ArrayList<>();
    @GuardedBy({"sRoReads"})
    private static final HashMap<String, MutableInt> sRoReads = null;

    private static native void native_add_change_callback();

    @UnsupportedAppUsage
    private static native String native_get(String str);

    private static native String native_get(String str, String str2);

    private static native boolean native_get_boolean(String str, boolean z);

    private static native int native_get_int(String str, int i);

    @UnsupportedAppUsage
    private static native long native_get_long(String str, long j);

    private static native void native_report_sysprop_change();

    private static native void native_set(String str, String str2);

    private static void onKeyAccess(String key) {
    }

    @SystemApi
    public static String get(String key) {
        return native_get(key);
    }

    @SystemApi
    public static String get(String key, String def) {
        return native_get(key, def);
    }

    @SystemApi
    public static int getInt(String key, int def) {
        return native_get_int(key, def);
    }

    @SystemApi
    public static long getLong(String key, long def) {
        return native_get_long(key, def);
    }

    @SystemApi
    public static boolean getBoolean(String key, boolean def) {
        return native_get_boolean(key, def);
    }

    @UnsupportedAppUsage
    public static void set(String key, String val) {
        if (val == null || val.startsWith("ro.") || val.length() <= 91) {
            native_set(key, val);
            return;
        }
        throw new IllegalArgumentException("value of system property '" + key + "' is longer than 91 characters: " + val);
    }

    @UnsupportedAppUsage
    public static void addChangeCallback(Runnable callback) {
        synchronized (sChangeCallbacks) {
            if (sChangeCallbacks.size() == 0) {
                native_add_change_callback();
            }
            sChangeCallbacks.add(callback);
        }
    }

    public static boolean getRTLFlag() {
        return Locale.getDefault().getLanguage().contains("ar") || Locale.getDefault().getLanguage().contains("iw") || Locale.getDefault().getLanguage().contains("fa") || Locale.getDefault().getLanguage().contains("ur") || Locale.getDefault().getLanguage().contains("ug");
    }

    private static void callChangeCallbacks() {
        synchronized (sChangeCallbacks) {
            if (sChangeCallbacks.size() != 0) {
                ArrayList<Runnable> callbacks = new ArrayList<>(sChangeCallbacks);
                long token = Binder.clearCallingIdentity();
                for (int i = 0; i < callbacks.size(); i++) {
                    try {
                        try {
                            callbacks.get(i).run();
                        } catch (Throwable t) {
                            Log.wtf(TAG, "Exception in SystemProperties change callback", t);
                        }
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                }
            }
        }
    }

    @UnsupportedAppUsage
    public static void reportSyspropChanged() {
        native_report_sysprop_change();
    }

    public static String digestOf(String... keys) {
        Arrays.sort(keys);
        try {
            MessageDigest digest = MessageDigest.getInstance(KeyProperties.DIGEST_SHA1);
            for (String key : keys) {
                digest.update((key + "=" + get(key) + "\n").getBytes(StandardCharsets.UTF_8));
            }
            return HexEncoding.encodeToString(digest.digest()).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @UnsupportedAppUsage
    private SystemProperties() {
    }
}
