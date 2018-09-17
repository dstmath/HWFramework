package android.media;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityThread;
import android.content.Context;
import android.os.FreezeScreenScene;
import android.util.Log;
import java.util.List;

public class HwMediaMonitorImpl implements IHwMediaMonitor {
    private static final String TAG = "HwMediaMonitorImpl";
    private static IHwMediaMonitor mHwMediaMonitor = null;
    public final int STATUS_ERROR = 1;
    public final int STATUS_OK = 0;
    public final int STATUS_SERVER_DIED = 100;
    private ErrorCallback mErrorCallback;

    public interface ErrorCallback {
        void onError(int i);
    }

    private native int forceLogSend_native(int i);

    private native int writeBigData_native(int i, int i2);

    private native int writeBigData_native(int i, int i2, int i3, int i4);

    private native int writeKpis_native(String str);

    private native int writeLogMsg_native(int i, int i2, int i3, String str);

    private native int writeLogMsg_native(int i, int i2, int i3, String str, int i4, String str2, int i5);

    private native int writeLogMsg_native(int i, int i2, String str);

    private native int writeMediaBigData_native(int i, int i2, String str);

    public native int checkAudioFlinger();

    public native int systemReady();

    static {
        System.loadLibrary("mediamonitor_jni");
    }

    private HwMediaMonitorImpl() {
    }

    public static IHwMediaMonitor getDefault() {
        IHwMediaMonitor iHwMediaMonitor;
        synchronized (HwMediaMonitorImpl.class) {
            if (mHwMediaMonitor == null) {
                mHwMediaMonitor = new HwMediaMonitorImpl();
            }
            iHwMediaMonitor = mHwMediaMonitor;
        }
        return iHwMediaMonitor;
    }

    public int writeLogMsg(int priority, int type, String msg) {
        return writeLogMsg_native(priority, type, msg);
    }

    public int writeLogMsg(int eventId, int eventLevel, int subType, String reason) {
        return writeLogMsg_native(eventId, eventLevel, subType, reason);
    }

    public int writeLogMsg(int eventId, int eventLevel, int subType, String reason, int paraInt, String paraChar, int associatedEeventID) {
        return writeLogMsg_native(eventId, eventLevel, subType, reason, paraInt, paraChar, associatedEeventID);
    }

    public int writeKpis(String kpis) {
        return writeKpis_native(kpis);
    }

    public int writeBigData(int eventId, int subType) {
        return writeBigData_native(eventId, subType);
    }

    public int writeBigData(int eventId, int subType, int ext1, int ext2) {
        return writeBigData_native(eventId, subType, ext1, ext2);
    }

    public int writeMediaBigData(int pid, int type, String msg) {
        return writeMediaBigData_native(pid, type, msg);
    }

    public void writeMediaBigDataByReportInf(int pid, int type, String msg) {
    }

    private String getPackageNameByPid(int pid) {
        if (pid <= 0) {
            Log.i(TAG, "getPackageNameByPid  pid<=0");
            return null;
        }
        Context context = ActivityThread.currentApplication();
        if (context == null) {
            return null;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService(FreezeScreenScene.ACTIVITY_PARAM);
        if (activityManager == null) {
            Log.i(TAG, "getPackageNameByPid  activityManager == null");
            return null;
        }
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            Log.i(TAG, "getPackageNameByPid  appProcesses == null");
            return null;
        }
        String packageName = null;
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.pid == pid) {
                packageName = appProcess.processName;
                break;
            }
        }
        return packageName;
    }

    public int forceLogSend(int level) {
        return forceLogSend_native(level);
    }

    private boolean checkMediaLogPermission() {
        return true;
    }

    public void setErrorCallback(ErrorCallback cb) {
        synchronized (HwMediaMonitorImpl.class) {
            this.mErrorCallback = cb;
            if (cb != null) {
                cb.onError(checkAudioFlinger());
            }
        }
    }

    private void errorCallbackFromNative(int error) {
        ErrorCallback errorCallback = null;
        synchronized (HwMediaMonitorImpl.class) {
            if (this.mErrorCallback != null) {
                errorCallback = this.mErrorCallback;
            }
        }
        if (errorCallback != null) {
            errorCallback.onError(error);
        }
    }
}
