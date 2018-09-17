package huawei.android.common;

import android.common.HwFrameworkMonitor;
import android.content.Intent;
import android.os.Bundle;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Log;
import android.util.Slog;

public class HwFrameworkMonitorImpl implements HwFrameworkMonitor {
    private static final int MAX_REASON_LEN = 512;
    private static final String TAG = "HwFrameworkMonitor";
    private static HwFrameworkMonitorImpl mInstance = null;
    private EventStream mEventStream = null;

    private HwFrameworkMonitorImpl() {
    }

    public static synchronized HwFrameworkMonitorImpl getInstance() {
        HwFrameworkMonitorImpl hwFrameworkMonitorImpl;
        synchronized (HwFrameworkMonitorImpl.class) {
            if (mInstance == null) {
                mInstance = new HwFrameworkMonitorImpl();
            }
            hwFrameworkMonitorImpl = mInstance;
        }
        return hwFrameworkMonitorImpl;
    }

    public boolean monitor(int sceneId, Bundle params) {
        this.mEventStream = IMonitor.openEventStream(sceneId);
        if (this.mEventStream == null) {
            return false;
        }
        if (params != null) {
            switch (sceneId) {
                case 907034001:
                    int errorType = params.getInt("errorType", 1001);
                    this.mEventStream.setParam((short) 0, errorType);
                    Exception e = (Exception) params.getSerializable("reason");
                    if (e == null) {
                        e = new Exception();
                    }
                    String reason = Log.getStackTraceString(e).trim();
                    if (reason.length() > 512) {
                        reason = reason.substring(0, 512);
                    }
                    this.mEventStream.setParam((short) 1, reason);
                    Slog.i(TAG, "monitorCheckPassword: errorType=" + errorType + ", reason=" + reason);
                    break;
                case 907400000:
                    this.mEventStream.setParam((short) 0, params.getString("package", "unknown"));
                    this.mEventStream.setParam((short) 1, params.getString("versionName", "unknown"));
                    this.mEventStream.setParam((short) 3, params.getString("extra", "unknown"));
                    break;
                case 907400002:
                    this.mEventStream.setParam((short) 0, params.getString("package", "unknown"));
                    this.mEventStream.setParam((short) 1, params.getString("versionName", "unknown"));
                    this.mEventStream.setParam((short) 3, params.getString("action", "unknown"));
                    this.mEventStream.setParam((short) 4, params.getInt("actionCount", 0));
                    this.mEventStream.setParam((short) 5, Boolean.valueOf(params.getBoolean("mmsFlag", false)));
                    this.mEventStream.setParam((short) 6, params.getString("receiver", "unknown"));
                    this.mEventStream.setParam((short) 7, params.getString("package", "unknown"));
                    break;
                case 907400003:
                    this.mEventStream.setParam((short) 0, params.getString("package", "unknown"));
                    this.mEventStream.setParam((short) 1, params.getString("versionName", "unknown"));
                    this.mEventStream.setParam((short) 3, params.getString("action", "unknown"));
                    this.mEventStream.setParam((short) 4, params.getFloat("receiveTime", 0.0f));
                    this.mEventStream.setParam((short) 5, params.getString("receiver", "unknown"));
                    Object objIntent = params.getParcelable("intent");
                    if (objIntent != null) {
                        this.mEventStream.setParam((short) 6, ((Intent) objIntent).toString());
                        break;
                    }
                    break;
                case 907400016:
                    this.mEventStream.setParam((short) 0, params.getString("cpuState", "unknown"));
                    this.mEventStream.setParam((short) 1, params.getString("cpuTime", "unknown"));
                    this.mEventStream.setParam((short) 2, params.getString("extra", "unknown"));
                    break;
                case 907400018:
                    this.mEventStream.setParam((short) 0, params.getString("component", "unknown"));
                    this.mEventStream.setParam((short) 1, params.getString("reason", "unknown"));
                    break;
            }
        }
        boolean result = IMonitor.sendEvent(this.mEventStream);
        Slog.i(TAG, "Monitor for " + sceneId + ", result=" + result);
        IMonitor.closeEventStream(this.mEventStream);
        return result;
    }
}
