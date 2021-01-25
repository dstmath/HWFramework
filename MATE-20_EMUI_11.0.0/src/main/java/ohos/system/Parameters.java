package ohos.system;

import ohos.annotation.SystemApi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

@SystemApi
public final class Parameters {
    public static final int HILOG_DOMAIN = 218115072;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218115072, Parameters.class.getSimpleName());

    private static native String nativeGet(String str, String str2);

    private static native boolean nativeGetBoolean(String str, boolean z);

    private static native int nativeGetInt(String str, int i);

    private static native long nativeGetLong(String str, long j);

    private static native String nativeGetNoDefault(String str);

    private static native boolean nativeSet(String str, String str2);

    static {
        try {
            System.loadLibrary("syspara_jni.z");
        } catch (UnsatisfiedLinkError e) {
            HiLog.error(TAG, "load syspara_jni.z UnsatisfiedLinkError: %{public}s", e);
        }
    }

    public static String get(String str) {
        return (str == null || str.trim().length() == 0) ? "" : nativeGetNoDefault(str.trim());
    }

    public static String get(String str, String str2) {
        if (str == null || str.trim().length() == 0) {
            return str2 == null ? "" : str2;
        }
        return nativeGet(str.trim(), str2);
    }

    public static int getInt(String str, int i) {
        return (str == null || str.trim().length() == 0) ? i : nativeGetInt(str.trim(), i);
    }

    public static long getLong(String str, long j) {
        return (str == null || str.trim().length() == 0) ? j : nativeGetLong(str.trim(), j);
    }

    public static boolean getBoolean(String str, boolean z) {
        return (str == null || str.trim().length() == 0) ? z : nativeGetBoolean(str.trim(), z);
    }

    public static boolean set(String str, String str2) {
        if (str == null || str.trim().length() == 0) {
            return false;
        }
        return nativeSet(str.trim(), str2);
    }
}
