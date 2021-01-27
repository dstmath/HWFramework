package android.util;

import android.common.HwFrameworkFactory;
import android.content.Context;
import java.util.Date;
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

    @Deprecated
    public static boolean bdReport(Context context, int eventId) {
        return HwFrameworkFactory.getHwFlogManager().bdReport(context, eventId);
    }

    @Deprecated
    public static boolean bdReport(Context context, int eventId, String eventMsg) {
        return HwFrameworkFactory.getHwFlogManager().bdReport(context, eventId, eventMsg);
    }

    @Deprecated
    public static boolean bdReport(Context context, int eventId, JSONObject eventMsg) {
        return HwFrameworkFactory.getHwFlogManager().bdReport(context, eventId, eventMsg);
    }

    @Deprecated
    public static boolean bdReport(Context context, int eventId, JSONObject eventMsg, int priority) {
        return HwFrameworkFactory.getHwFlogManager().bdReport(context, eventId, eventMsg, priority);
    }

    public static void bdReport(int eventId) {
        HwFrameworkFactory.getHwFlogManager().bdReport(eventId);
    }

    public static void bdReport(int eventId, JSONObject eventMsg) {
        HwFrameworkFactory.getHwFlogManager().bdReport(eventId, eventMsg);
    }

    public static void bdReport(int eventId, String json) {
        HwFrameworkFactory.getHwFlogManager().bdReport(eventId, json);
    }

    public static void bdReport(int eventId, String json, Date date) {
        HwFrameworkFactory.getHwFlogManager().bdReport(eventId, json, date);
    }

    public static void bdReport(int eventId, String key, boolean value) {
        HwFrameworkFactory.getHwFlogManager().bdReport(eventId, key, value);
    }

    public static void bdReport(int eventId, String key, byte value) {
        HwFrameworkFactory.getHwFlogManager().bdReport(eventId, key, value);
    }

    public static void bdReport(int eventId, String key, int value) {
        HwFrameworkFactory.getHwFlogManager().bdReport(eventId, key, value);
    }

    public static void bdReport(int eventId, String key, long value) {
        HwFrameworkFactory.getHwFlogManager().bdReport(eventId, key, value);
    }

    public static void bdReport(int eventId, String key, float value) {
        HwFrameworkFactory.getHwFlogManager().bdReport(eventId, key, value);
    }

    public static void bdReport(int eventId, String key, String value) {
        HwFrameworkFactory.getHwFlogManager().bdReport(eventId, key, value);
    }
}
