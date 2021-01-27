package ohos.msdp.devicestatus;

import android.util.Log;

public class LogUtils {
    private static final String TAG = "DeviceStatusSdk";

    private LogUtils() {
    }

    public static void v(String str, String str2) {
        Log.v(str, str2);
    }

    public static void d(String str, String str2) {
        Log.d(str, str2);
    }

    public static void i(String str, String str2) {
        Log.i(str, str2);
    }

    public static void w(String str, String str2) {
        Log.w(str, str2);
    }

    public static void e(String str, String str2) {
        Log.e(str, str2);
    }
}
