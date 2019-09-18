package huawei.android.utils;

import android.common.HwFlogManager;
import android.content.Context;
import com.huawei.bd.Reporter;
import org.json.JSONObject;

public class HwFlogManagerImpl implements HwFlogManager {
    private static final boolean LOCAL_LOGV = false;
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
        switch (priority) {
            case 2:
                return HwCoreServicesLog.v(tag, msg);
            case 3:
                return HwCoreServicesLog.d(tag, msg);
            case 4:
                return HwCoreServicesLog.i(tag, msg);
            case 5:
                return HwCoreServicesLog.w(tag, msg);
            case 6:
                return HwCoreServicesLog.e(tag, msg);
            default:
                return -1;
        }
    }

    public int slog(int priority, int tag, String msg, Throwable tr) {
        switch (priority) {
            case 2:
                return HwCoreServicesLog.v(tag, msg, tr);
            case 3:
                return HwCoreServicesLog.d(tag, msg, tr);
            case 4:
                return HwCoreServicesLog.i(tag, msg, tr);
            case 5:
                return HwCoreServicesLog.w(tag, msg, tr);
            case 6:
                return HwCoreServicesLog.e(tag, msg, tr);
            default:
                return -1;
        }
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
