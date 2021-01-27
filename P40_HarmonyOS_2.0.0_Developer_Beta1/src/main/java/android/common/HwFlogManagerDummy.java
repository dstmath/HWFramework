package android.common;

import android.content.Context;
import java.util.Date;
import org.json.JSONObject;

public class HwFlogManagerDummy implements HwFlogManager {
    private static HwFlogManager sHwFlogManager = null;

    private HwFlogManagerDummy() {
    }

    public static HwFlogManager getDefault() {
        if (sHwFlogManager == null) {
            sHwFlogManager = new HwFlogManagerDummy();
        }
        return sHwFlogManager;
    }

    @Override // android.common.HwFlogManager
    public int slog(int priority, int tag, String msg) {
        return 0;
    }

    @Override // android.common.HwFlogManager
    public int slog(int priority, int tag, String msg, Throwable tr) {
        return 0;
    }

    @Override // android.common.HwFlogManager
    public int slogv(String tag, String msg) {
        return 0;
    }

    @Override // android.common.HwFlogManager
    public int slogd(String tag, String msg) {
        return 0;
    }

    @Override // android.common.HwFlogManager
    public boolean handleLogRequest(String[] args) {
        return false;
    }

    @Override // android.common.HwFlogManager
    public boolean bdReport(Context context, int eventId) {
        return false;
    }

    @Override // android.common.HwFlogManager
    public boolean bdReport(Context context, int eventId, String eventMsg) {
        return false;
    }

    @Override // android.common.HwFlogManager
    public boolean bdReport(Context context, int eventId, JSONObject eventMsg) {
        return false;
    }

    @Override // android.common.HwFlogManager
    public boolean bdReport(Context context, int eventId, JSONObject eventMsg, int priority) {
        return false;
    }

    @Override // android.common.HwFlogManager
    public void bdReport(int eventId) {
    }

    @Override // android.common.HwFlogManager
    public void bdReport(int eventId, JSONObject eventMsg) {
    }

    @Override // android.common.HwFlogManager
    public void bdReport(int eventId, String json) {
    }

    @Override // android.common.HwFlogManager
    public void bdReport(int eventId, String json, Date date) {
    }

    @Override // android.common.HwFlogManager
    public void bdReport(int eventId, String key, boolean value) {
    }

    @Override // android.common.HwFlogManager
    public void bdReport(int eventId, String key, byte value) {
    }

    @Override // android.common.HwFlogManager
    public void bdReport(int eventId, String key, int value) {
    }

    @Override // android.common.HwFlogManager
    public void bdReport(int eventId, String key, long value) {
    }

    @Override // android.common.HwFlogManager
    public void bdReport(int eventId, String key, float value) {
    }

    @Override // android.common.HwFlogManager
    public void bdReport(int eventId, String key, String value) {
    }
}
