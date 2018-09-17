package android.util;

import android.common.HwFrameworkFactory;
import android.content.Context;
import org.json.JSONObject;

public final class Flog {
    private static final String TAG = "Flog";

    private Flog() {
    }

    public static int v(int tag, String msg) {
        return HwFrameworkFactory.getHwFlogManager().slog(2, tag, msg);
    }

    public static int v(int tag, String msg, Throwable tr) {
        return HwFrameworkFactory.getHwFlogManager().slog(2, tag, msg, tr);
    }

    public static int d(int tag, String msg) {
        return HwFrameworkFactory.getHwFlogManager().slog(3, tag, msg);
    }

    public static int d(int tag, String msg, Throwable tr) {
        return HwFrameworkFactory.getHwFlogManager().slog(3, tag, msg, tr);
    }

    public static int i(int tag, String msg) {
        return HwFrameworkFactory.getHwFlogManager().slog(4, tag, msg);
    }

    public static int i(int tag, String msg, Throwable tr) {
        return HwFrameworkFactory.getHwFlogManager().slog(4, tag, msg, tr);
    }

    public static int w(int tag, String msg) {
        return HwFrameworkFactory.getHwFlogManager().slog(5, tag, msg);
    }

    public static int w(int tag, String msg, Throwable tr) {
        return HwFrameworkFactory.getHwFlogManager().slog(5, tag, msg, tr);
    }

    public static int e(int tag, String msg) {
        return HwFrameworkFactory.getHwFlogManager().slog(6, tag, msg);
    }

    public static int e(int tag, String msg, Throwable tr) {
        return HwFrameworkFactory.getHwFlogManager().slog(6, tag, msg, tr);
    }

    public static boolean bdReport(Context context, int eventID) {
        return HwFrameworkFactory.getHwFlogManager().bdReport(context, eventID);
    }

    public static boolean bdReport(Context context, int eventID, String eventMsg) {
        return HwFrameworkFactory.getHwFlogManager().bdReport(context, eventID, eventMsg);
    }

    public static boolean bdReport(Context context, int eventID, JSONObject eventMsg) {
        return HwFrameworkFactory.getHwFlogManager().bdReport(context, eventID, eventMsg);
    }

    public static boolean bdReport(Context context, int eventID, JSONObject eventMsg, int priority) {
        return HwFrameworkFactory.getHwFlogManager().bdReport(context, eventID, eventMsg, priority);
    }
}
