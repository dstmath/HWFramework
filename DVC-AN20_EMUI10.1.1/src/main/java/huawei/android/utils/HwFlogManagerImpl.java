package huawei.android.utils;

import android.common.HwFlogManager;
import android.content.Context;
import com.huawei.bd.Reporter;
import org.json.JSONObject;

public class HwFlogManagerImpl implements HwFlogManager {
    private static final String TAG = "HwFlogManagerImpl";
    private static HwFlogManager mHwFlogManager = null;

    private HwFlogManagerImpl() {
    }

    public static HwFlogManager getDefault() {
        if (mHwFlogManager == null) {
            mHwFlogManager = new HwFlogManagerImpl();
        }
        return mHwFlogManager;
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
        Reporter.c(context, eventID);
        return true;
    }

    public boolean bdReport(Context context, int eventID, String eventMsg) {
        Reporter.e(context, eventID, eventMsg);
        return true;
    }

    public boolean bdReport(Context context, int eventID, JSONObject eventMsg) {
        Reporter.j(context, eventID, eventMsg);
        return true;
    }

    public boolean bdReport(Context context, int eventID, JSONObject eventMsg, int priority) {
        Reporter.j(context, eventID, eventMsg, priority);
        return true;
    }
}
