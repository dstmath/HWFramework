package huawei.com.android.server.policy.recsys;

import android.text.TextUtils;
import android.util.Log;

public class HwLog {
    private static final String APPLICATION_NAME = "HwRecSys_";
    private static final boolean HWLOG = true;

    public static void v(String tag, String msg) {
        tag = APPLICATION_NAME + tag;
        if (TextUtils.isEmpty(msg)) {
            msg = "no msg";
        }
        Log.v(tag, msg);
    }

    public static void d(String tag, String msg) {
        tag = APPLICATION_NAME + tag;
        if (TextUtils.isEmpty(msg)) {
            msg = "no msg";
        }
        Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        tag = APPLICATION_NAME + tag;
        if (TextUtils.isEmpty(msg)) {
            msg = "no msg";
        }
        Log.e(tag, msg);
    }

    public static void i(String tag, String msg) {
        tag = APPLICATION_NAME + tag;
        if (TextUtils.isEmpty(msg)) {
            msg = "no msg";
        }
        Log.i(tag, msg);
    }

    public static void i(String tag, String msg, Exception ex) {
        tag = APPLICATION_NAME + tag;
        if (TextUtils.isEmpty(msg)) {
            msg = "no msg";
        }
        Log.i(tag, msg, ex);
    }

    public static void w(String tag, String msg) {
        tag = APPLICATION_NAME + tag;
        if (TextUtils.isEmpty(msg)) {
            msg = "no msg";
        }
        Log.w(tag, msg);
    }
}
