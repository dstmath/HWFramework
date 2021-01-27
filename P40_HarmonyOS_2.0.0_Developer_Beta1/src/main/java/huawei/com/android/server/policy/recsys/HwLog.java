package huawei.com.android.server.policy.recsys;

import android.text.TextUtils;
import android.util.Log;

public class HwLog {
    private static final String APPLICATION_NAME = "HwRecSys_";
    private static final boolean HW_LOG = true;
    private static final String NO_MSG = "no msg";

    public static void v(String tag, String msg) {
        Log.v(APPLICATION_NAME + tag, TextUtils.isEmpty(msg) ? NO_MSG : msg);
    }

    public static void d(String tag, String msg) {
        Log.d(APPLICATION_NAME + tag, TextUtils.isEmpty(msg) ? NO_MSG : msg);
    }

    public static void e(String tag, String msg) {
        Log.e(APPLICATION_NAME + tag, TextUtils.isEmpty(msg) ? NO_MSG : msg);
    }

    public static void i(String tag, String msg) {
        Log.i(APPLICATION_NAME + tag, TextUtils.isEmpty(msg) ? NO_MSG : msg);
    }

    public static void i(String tag, String msg, Exception ex) {
        Log.i(APPLICATION_NAME + tag, TextUtils.isEmpty(msg) ? NO_MSG : msg, ex);
    }

    public static void w(String tag, String msg) {
        Log.w(APPLICATION_NAME + tag, TextUtils.isEmpty(msg) ? NO_MSG : msg);
    }
}
