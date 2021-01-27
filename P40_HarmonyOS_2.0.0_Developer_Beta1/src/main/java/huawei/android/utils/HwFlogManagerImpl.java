package huawei.android.utils;

import android.common.HwFlogManager;
import android.content.Context;
import huawei.hiview.HiEvent;
import huawei.hiview.HiView;
import java.util.Date;
import org.json.JSONObject;

public class HwFlogManagerImpl implements HwFlogManager {
    private static final String TAG = "HwFlogManagerImpl";
    private static HwFlogManager sHwFlogManager = null;

    private HwFlogManagerImpl() {
    }

    public static synchronized HwFlogManager getDefault() {
        HwFlogManager hwFlogManager;
        synchronized (HwFlogManagerImpl.class) {
            if (sHwFlogManager == null) {
                sHwFlogManager = new HwFlogManagerImpl();
            }
            hwFlogManager = sHwFlogManager;
        }
        return hwFlogManager;
    }

    public int slogv(String tag, String msg) {
        return HwCoreServicesLog.v(tag, msg);
    }

    public int slogd(String tag, String msg) {
        return HwCoreServicesLog.d(tag, msg);
    }

    public boolean handleLogRequest(String[] args) {
        return HwCoreServicesLog.handleLogRequest(args);
    }

    public int slog(int priority, int tag, String msg) {
        if (priority == 2) {
            return HwCoreServicesLog.v(tag, msg);
        }
        if (priority == 3) {
            return HwCoreServicesLog.d(tag, msg);
        }
        if (priority == 4) {
            return HwCoreServicesLog.i(tag, msg);
        }
        if (priority == 5) {
            return HwCoreServicesLog.w(tag, msg);
        }
        if (priority != 6) {
            return -1;
        }
        return HwCoreServicesLog.e(tag, msg);
    }

    public int slog(int priority, int tag, String msg, Throwable tr) {
        if (priority == 2) {
            return HwCoreServicesLog.v(tag, msg, tr);
        }
        if (priority == 3) {
            return HwCoreServicesLog.d(tag, msg, tr);
        }
        if (priority == 4) {
            return HwCoreServicesLog.i(tag, msg, tr);
        }
        if (priority == 5) {
            return HwCoreServicesLog.w(tag, msg, tr);
        }
        if (priority != 6) {
            return -1;
        }
        return HwCoreServicesLog.e(tag, msg, tr);
    }

    public boolean bdReport(Context context, int eventID) {
        bdReport(eventID);
        return true;
    }

    public boolean bdReport(Context context, int eventID, String eventMsg) {
        bdReport(eventID, eventMsg);
        return true;
    }

    public boolean bdReport(Context context, int eventID, JSONObject eventMsg) {
        bdReport(eventID, eventMsg);
        return true;
    }

    public boolean bdReport(Context context, int eventID, JSONObject eventMsg, int priority) {
        bdReport(eventID, eventMsg);
        return true;
    }

    public void bdReport(int eventId) {
        HiView.report(new HiEvent(eventId));
    }

    public void bdReport(int eventId, JSONObject eventMsg) {
        HiView.report(HiView.byJson(eventId, eventMsg));
    }

    public void bdReport(int eventId, String json) {
        HiView.report(HiView.byJson(eventId, json));
    }

    public void bdReport(int eventId, String json, Date date) {
        HiView.report(HiView.byJson(eventId, json).setTime(date));
    }

    public void bdReport(int eventId, String key, boolean value) {
        HiView.report(HiView.byPair(eventId, key, value));
    }

    public void bdReport(int eventId, String key, byte value) {
        HiView.report(HiView.byPair(eventId, key, value));
    }

    public void bdReport(int eventId, String key, int value) {
        HiView.report(HiView.byPair(eventId, key, value));
    }

    public void bdReport(int eventId, String key, long value) {
        HiView.report(HiView.byPair(eventId, key, value));
    }

    public void bdReport(int eventId, String key, float value) {
        HiView.report(HiView.byPair(eventId, key, value));
    }

    public void bdReport(int eventId, String key, String value) {
        HiView.report(HiView.byPair(eventId, key, value));
    }
}
